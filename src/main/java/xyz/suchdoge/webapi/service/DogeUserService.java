package xyz.suchdoge.webapi.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.blockchain.Address;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.security.DogeUserDetails;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;
import xyz.suchdoge.webapi.service.imageGenerator.ImageGeneratorService;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.storage.StoragePath;
import xyz.suchdoge.webapi.service.validator.DogeUserVerifier;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;

/**
 * Service for common user operations.
 *
 * @author Nikita
 */
@Service
public class DogeUserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeRoleRepository dogeRoleRepository;
    private final CloudStorageService cloudStorageService;
    private final ImageGeneratorService imageGeneratorService;
    private final DogeBlockchainService blockchainService;
    private final DogeUserVerifier dogeUserVerifier;
    private final ModelValidatorService modelValidatorService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructs new instance with needed dependencies.
     */
    public DogeUserService(PasswordEncoder passwordEncoder,
                           DogeUserRepository dogeUserRepository,
                           DogeRoleRepository dogeRoleRepository,
                           CloudStorageService cloudStorageService,
                           ImageGeneratorService imageGeneratorService,
                           DogeBlockchainService blockchainService,
                           DogeUserVerifier dogeUserVerifier,
                           ModelValidatorService modelValidatorService,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
        this.cloudStorageService = cloudStorageService;
        this.imageGeneratorService = imageGeneratorService;
        this.blockchainService = blockchainService;
        this.dogeUserVerifier = dogeUserVerifier;
        this.modelValidatorService = modelValidatorService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Retrieves a specific user from the database and creates UserDetails from it.
     * Mostly used by the framework.
     *
     * @param username username to search for.
     * @return UserDetails wrapper of user from the database.
     * @throws UsernameNotFoundException if the user does not exist.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new DogeUserDetails(getUserByUsername(username));
    }

    /**
     * Retrieves a specific user from the database.
     *
     * @param username user to search for.
     * @return user from the database.
     * @throws UsernameNotFoundException if the user does not exist.
     */
    public DogeUser getUserByUsername(String username) throws UsernameNotFoundException {
        return dogeUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /**
     * Retrieves a confirmed user from the database.
     *
     * @param username username to search for.
     * @return confirmed user from the database.
     * @throws UsernameNotFoundException if the user does not exist.
     * @throws DogeHttpException         USER_NOT_CONFIRMED if user is found but not confirmed.
     */
    public DogeUser getConfirmedUser(String username) throws DogeHttpException, UsernameNotFoundException {
        final DogeUser user = this.getUserByUsername(username);
        if (!user.isConfirmed()) {
            throw new DogeHttpException("USER_NOT_CONFIRMED", HttpStatus.METHOD_NOT_ALLOWED);
        }
        return user;
    }

    /**
     * Creates new user instance and save it to the database.
     *
     * @param username new account's username.
     * @param email    email associated with the new account.
     * @param password password for the new account.
     * @return saved to the database user.
     * @throws DogeHttpException when can not create new user.
     */
    @Transactional
    public DogeUser createUser(String username, String email, String password) throws DogeHttpException {
        // validate username, email and password
        dogeUserVerifier.verifyUsername(username);
        dogeUserVerifier.verifyEmail(email);
        dogeUserVerifier.verifyPassword(password);

        // create user instance
        final DogeUser user = DogeUser.builder()
                .username(username.trim())
                .email(email.trim())
                .encodedPassword(passwordEncoder.encode(password))
                .build();

        // mark user account as not confirmed
        final DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER);
        user.addRole(userRole);

        // validate user and save it to the database
        modelValidatorService.validate(user);
        DogeUser savedUser = dogeUserRepository.save(user);

        // create user wallet
        final Address address = this.blockchainService.createOrGetAddress(savedUser.getUsername());
        savedUser = this.changeDogePublicKey(address.getValue(), savedUser);

        // generate personalized profile pic and save it to cloud storage
        try {
            final byte[] profilePic = this.imageGeneratorService.generateProfilePic(savedUser.getUsername());
            this.cloudStorageService.upload(profilePic, savedUser.getUsername() + ".png", StoragePath.USER);
        } catch (Exception e) {
            // skip profile pic generation
        }

        return savedUser;
    }

    /**
     * Change email of a specific user account.
     *
     * @param newEmail new email to associate with the user account.
     * @param user     user to be updated.
     * @return updated user.
     * @throws DogeHttpException DOGE_USER_EMAIL_INVALID if is not valid email.
     * @throws DogeHttpException DOGE_USER_EMAIL_EXISTS if account with this email already exists.
     */
    public DogeUser changeUserEmail(String newEmail, DogeUser user) throws DogeHttpException {
        // validate the new email
        this.dogeUserVerifier.verifyEmail(newEmail);

        // update email
        user.setEmail(newEmail.trim());

        // mark user account as not confirmed
        user.removeRole(DogeRoleLevel.USER);
        user.addRole(this.dogeRoleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER));

        // validate user and save it to the database
        this.modelValidatorService.validate(user);
        final DogeUser updatedUser = dogeUserRepository.save(user);

        // publish email confirmation needed event
        this.applicationEventPublisher.publishEvent(new OnEmailConfirmationNeededEvent(this, updatedUser));

        return updatedUser;
    }

    /**
     * Change doge public key of a specific user account.
     *
     * @param newDogePublicKey new doge public key to associate with the user account.
     * @param user             user to be updated.
     * @return updated user.
     * @throws DogeHttpException DOGE_USER_PUBLIC_KEY_INVALID if new doge public key is not.
     */
    public DogeUser changeDogePublicKey(String newDogePublicKey, DogeUser user) throws DogeHttpException {
        // todo validate

        // update public key
        user.setDogePublicKey(newDogePublicKey.trim());

        // validate user and save it to the database
        this.modelValidatorService.validate(user);
        return dogeUserRepository.save(user);
    }

    /**
     * Change password of a specific user account
     *
     * @param oldPassword     old password
     * @param newPassword     new password
     * @param confirmPassword confirm new password
     * @param username        username of the user whose password to change
     * @throws DogeHttpException PASSWORDS_DOES_NOT_MATCH if new password and confirm password does not match
     * @throws DogeHttpException WRONG_OLD_PASSWORD if old password is wrong
     * @throws DogeHttpException NEW_PASSWORD_AND_OLD_PASSWORD_ARE_THE_SAME if old password and new password are the same
     */
    public void changePassword(String oldPassword, String newPassword, String confirmPassword, String username)
            throws DogeHttpException {
        // retrieve user from database
        final DogeUser user = this.getUserByUsername(username);

        // check if new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            throw new DogeHttpException("PASSWORDS_DOES_NOT_MATCH", HttpStatus.BAD_REQUEST);
        }

        // check if old password is correct
        if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
            throw new DogeHttpException("WRONG_OLD_PASSWORD", HttpStatus.BAD_REQUEST);
        }

        // check if new password is different from the old one
        if (passwordEncoder.matches(newPassword, user.getEncodedPassword())) {
            throw new DogeHttpException("NEW_PASSWORD_AND_OLD_PASSWORD_ARE_THE_SAME", HttpStatus.BAD_REQUEST);
        }

        // validate the new password
        this.dogeUserVerifier.verifyPassword(newPassword);

        // update password
        user.setEncodedPassword(this.passwordEncoder.encode(newPassword));

        // validate user and save it to the database
        this.modelValidatorService.validate(user);
        dogeUserRepository.save(user);
    }

    /**
     * Change profile picture of a specific user account
     *
     * @param image    new profile image multipart file
     * @param username username of the user whose image to update
     * @throws DogeHttpException CAN_NOT_READ_IMAGE_BYTES if multipart file bytes can't be read
     * @throws DogeHttpException CAN_NOT_SAVE_IMAGE if image can not be uploaded to the cloud
     */
    public void setProfileImage(MultipartFile image, String username) {
        // retrieve confirmed user from database
        final DogeUser user = this.getConfirmedUser(username);

        try {
            // try to upload image bytes to cloud storage
            final String imageId = user.getUsername() + ".png";
            this.cloudStorageService.upload(image.getBytes(), imageId, StoragePath.USER);
        } catch (IOException e) {
            throw new DogeHttpException("CAN_NOT_READ_IMAGE_BYTES", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_SAVE_IMAGE", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
