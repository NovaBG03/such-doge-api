package xyz.suchdoge.webapi.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lib.blockIo.BlockIo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.blockchain.TransactionRequirementsResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.blockchain.*;
import xyz.suchdoge.webapi.model.blockchain.response.*;
import xyz.suchdoge.webapi.model.blockchain.transaction.PreparedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SignedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;

import java.util.Map;

/**
 * Service for managing doge blockchain wallets.
 * @author Nikita
 */
@Service
public class DogeBlockchainService {
    private final BlockIo blockIo;
    private final ObjectMapper objectMapper;
    private final DogeBlockchainProps dogeBlockchainProps;

    /**
     * Constructs new instance with needed dependencies.
     */
    public DogeBlockchainService(@Qualifier("dogeBlockIo") BlockIo blockIo,
                                 ObjectMapper objectMapper,
                                 DogeBlockchainProps dogeBlockchainProps) {
        this.blockIo = blockIo;
        this.objectMapper = objectMapper;
        this.dogeBlockchainProps = dogeBlockchainProps;
    }

    /**
     * Get sum of balances of all addresses.
     *
     * @return sum of balances of all addresses.
     * @throws DogeHttpException when can not get the balance.
     */
    public Balance getBalance() throws DogeHttpException {
        final String jsonResponse;
        try {
            jsonResponse = this.blockIo.GetBalance(null).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_GET_BALANCE", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_GET_APP_WALLET");

        BalanceResponse walletResponse = this.parseJsonObject(jsonResponse, BalanceResponse.class);
        return walletResponse.getData();
    }

    /**
     * Get the application address.
     *
     * @return the application address.
     * @throws DogeHttpException when can not get the address.
     */
    public Wallet getAppWallet() throws DogeHttpException {
        return this.getWallet(this.dogeBlockchainProps.getAppWalletLabel());
    }

    /**
     * Get wallet by label.
     *
     * @param label of the wallet.
     * @return a doge coin address related to the label.
     * @throws DogeHttpException when can not get the address.
     */
    public Wallet getWallet(String label) throws DogeHttpException {
        final String jsonResponse;
        try {
            final JSONObject params = new JSONObject(Map.of("label", label));
            jsonResponse = this.blockIo.GetAddressByLabel(params).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_GET_ADDRESS", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_GET_ADDRESS");

        WalletResponse walletResponse = this.parseJsonObject(jsonResponse, WalletResponse.class);
        return walletResponse.getData();
    }

    /**
     * Create wallet with attached label.
     *
     * @param label will be used to associate user with the address.
     * @return newly generated doge coin address.
     * @throws DogeHttpException when can not create new address.
     */
    public Address createWallet(String label) throws DogeHttpException {
        final String jsonResponse;
        try {
            final JSONObject params = new JSONObject(Map.of("label", label));
            jsonResponse = this.blockIo.GetNewAddress(params).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_CREATE_NEW_ADDRESS", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_CREATE_NEW_ADDRESS");

        AddressResponse addressResponse = this.parseJsonObject(jsonResponse, AddressResponse.class);
        return addressResponse.getData();
    }

    /**
     * Create or get address by label
     *
     * @param label of the wallet to get or create.
     * @return a doge coin address related to the label.
     * @throws DogeHttpException when can not get or create the address.
     */
    public Address createOrGetAddress(String label) throws DogeHttpException {
        Address address;
        try {
            // try to create new wallet
            address = this.createWallet(label);
        } catch (Exception e) {
            // get wallet if already exists one
            try {
                address = this.getWallet(label).getAddress();
            } catch (Exception ex) {
                throw new DogeHttpException("CAN_NOT_GET_ADDRESS", HttpStatus.BAD_REQUEST);
            }
        }

        return address;
    }

    /**
     * Calculate transaction fee for a specific priority.
     *
     * @param amount   amount to be transferred.
     * @param toLabel  label of the receiver address.
     * @param priority transaction priority
     * @return calculated transaction fee.
     * @throws DogeHttpException when can not calculate transaction fee.
     */
    public TransactionFee calculateTransactionFee(Double amount, String toLabel, TransactionPriority priority) throws DogeHttpException {
        this.validateTransactionAmount(amount);

        final String jsonResponse;
        try {
            final JSONObject params = new JSONObject(Map.of(
                    "amounts", String.format("%.8f", amount) + "," + String.format("%.8f", calculateAdditionalFee(amount)),
                    "to_labels", toLabel + "," + this.dogeBlockchainProps.getAppWalletLabel(),
                    "priority", priority.toString().toLowerCase()
            ));
            jsonResponse = this.blockIo.GetNetworkFeeEstimate(params).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_CALCULATE_NETWORK_FEE", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_CALCULATE_NETWORK_FEE");

        NetworkFeeResponse networkFeeResponse = this.parseJsonObject(jsonResponse, NetworkFeeResponse.class);
        TransactionFee networkFee = networkFeeResponse.getData();
        networkFee.addFee(this.calculateAdditionalFee(amount));
        return networkFee;
    }

    /**
     * Calculate additional transaction fee.
     *
     * @param transactionAmount amount to be transferred.
     * @return calculated additional transaction fee.
     */
    public Double calculateAdditionalFee(Double transactionAmount) {
        return transactionAmount * this.dogeBlockchainProps.getTransactionFeePercent() / 100d;
    }

    /**
     * Get transaction requirements.
     *
     * @return transaction requirements.
     */
    public TransactionRequirementsResponseDto getTransactionRequirements() {
        return TransactionRequirementsResponseDto.builder()
                .minTransactionAmount(this.dogeBlockchainProps.getMinTransactionAmount())
                .maxTransactionAmount(this.dogeBlockchainProps.getMaxTransactionAmount())
                .transactionFeePercent(this.dogeBlockchainProps.getTransactionFeePercent())
                .network(Network.DOGETEST.toString()) // todo remove property or fix it
                .build();
    }

    /**
     * Validate an address.
     *
     * @param address to validate.
     * @return whether the address is valid or not.
     * @throws DogeHttpException when can not validate the address.
     */
    public ValidatedAddress validateAddress(String address) throws DogeHttpException {
        final String jsonResponse;
        try {
            final JSONObject params = new JSONObject(Map.of("address", address));
            jsonResponse = this.blockIo.IsValidAddress(params).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_VALIDATE_ADDRESS", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_VALIDATE_ADDRESS");

        ValidatedAddressResponse validatedAddressResponse =
                this.parseJsonObject(jsonResponse, ValidatedAddressResponse.class);
        return validatedAddressResponse.getData();
    }

    /**
     * Prepare blockchain transaction.
     *
     * @param amount    amount to be transferred.
     * @param fromLabel label of the sender address.
     * @param toLabel   label of the receiver address.
     * @param priority  transaction priority
     * @return prepared transaction.
     * @throws DogeHttpException when can not prepare transaction.
     */
    public PreparedTransaction prepareTransaction(Double amount, String fromLabel, String toLabel, TransactionPriority priority) throws DogeHttpException {
        final JSONObject jsonObjResponse;
        try {
            final JSONObject params = new JSONObject(Map.of(
                    "amounts", String.format("%.8f", amount) + "," + String.format("%.8f", this.calculateAdditionalFee(amount)),
                    "from_labels", fromLabel + "," + fromLabel,
                    "to_labels", toLabel + "," + this.dogeBlockchainProps.getAppWalletLabel(),
                    "priority", priority.toString().toLowerCase()
            ));
            jsonObjResponse = this.blockIo.PrepareTransaction(params);
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_PREPARE_TRANSACTION", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonObjResponse.toString(), "CAN_NOT_PREPARE_TRANSACTION");

        return new PreparedTransaction(jsonObjResponse);
    }

    /**
     * Summarize transaction.
     *
     * @param preparedTransaction transaction to be summarized.
     * @return summarized transaction.
     * @throws DogeHttpException when can not summarize transaction.
     */
    public SummarizedTransaction summarizePreparedTransaction(PreparedTransaction preparedTransaction) throws DogeHttpException {
        String jsonResponse = blockIo.SummarizePreparedTransaction(preparedTransaction.getJSONObject()).toString();
        return this.parseJsonObject(jsonResponse, SummarizedTransaction.class);
    }

    /**
     * Sign transaction.
     *
     * @param preparedTransaction transaction te be signed.
     * @return signed transaction.
     * @throws DogeHttpException when can not sign transaction.
     */
    public SignedTransaction signTransaction(PreparedTransaction preparedTransaction) throws DogeHttpException {
        final String jsonResponse;
        try {
            jsonResponse = blockIo.CreateAndSignTransaction(preparedTransaction.getJSONObject()).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_SIGN_TRANSACTION", HttpStatus.BAD_REQUEST);
        }

        return this.parseJsonObject(jsonResponse, SignedTransaction.class);
    }

    /**
     * Submit transaction.
     *
     * @param signedTransaction transaction to be submitted.
     * @return submitted transaction.
     * @throws DogeHttpException when can not submit transaction.
     */
    public SubmittedTransaction submitTransaction(SignedTransaction signedTransaction) throws DogeHttpException {
        final String jsonResponse;
        try {
            final String singedTransactionStr = this.objectMapper.writeValueAsString(signedTransaction);
            final JSONObject singedTransactionJSONObject = (JSONObject) new JSONParser().parse(singedTransactionStr);
            final JSONObject params = new JSONObject(Map.of("transaction_data", singedTransactionJSONObject));
            jsonResponse = blockIo.SubmitTransaction(params).toString();
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_SUBMIT_TRANSACTION", HttpStatus.BAD_REQUEST);
        }

        this.checkForErrors(jsonResponse, "CAN_NOT_SUBMIT_TRANSACTION");

        SubmittedTransactionResponse submittedTransactionResponse =
                this.parseJsonObject(jsonResponse, SubmittedTransactionResponse.class);
        return submittedTransactionResponse.getData();
    }

    /**
     * Validate transaction amount is not too low or too high.
     *
     * @param amount to be transferred.
     */
    public void validateTransactionAmount(Double amount) {
        if (amount < this.dogeBlockchainProps.getMinTransactionAmount()) {
            throw new DogeHttpException("TRANSACTION_AMOUNT_TOO_LOW", HttpStatus.BAD_REQUEST);
        }
        if (amount > this.dogeBlockchainProps.getMaxTransactionAmount()) {
            throw new DogeHttpException("TRANSACTION_AMOUNT_TOO_HIGH", HttpStatus.BAD_REQUEST);
        }
    }

    private void checkForErrors(String jsonResponse, String defaultMessage) {
        BlockIoResponse<?> response = this.parseJsonObject(jsonResponse, BlockIoResponse.class);

        if (response.getStatus().equals(Status.FAIL)) {
            BlockErrorResponse errorResponse = this.parseJsonObject(jsonResponse, BlockErrorResponse.class);
            final String errMessage = errorResponse.getData().getMessage();
            if (errMessage.startsWith("Label already exists")) {
                throw new DogeHttpException("ADDRESS_LABEL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            }
            if (errMessage.startsWith("Label does not exist")) {
                throw new DogeHttpException("ADDRESS_LABEL_DOES_NOT_EXISTS", HttpStatus.BAD_REQUEST);
            }
            if (errMessage.startsWith("Invalid value for parameter AMOUNTS provided.")) {
                throw new DogeHttpException("TRANSACTION_AMOUNT_INVALID", HttpStatus.BAD_REQUEST);
            }
            if (errMessage.contains("Maximum withdrawable balance is")) {
                TransactionFeeError transactionFeeError =
                        this.parseJsonObject(jsonResponse, TransactionFeeErrorResponse.class).getData();
                final Double maxWithdrawalBalance = transactionFeeError.getMaxWithdrawalFee();
                throw new DogeHttpException(
                        "MAX_TRANSACTION_AMOUNT_IS_" + String.format("%.8f", maxWithdrawalBalance),
                        HttpStatus.BAD_REQUEST);
            }

            throw new DogeHttpException(defaultMessage, HttpStatus.BAD_REQUEST);
        }
    }

    private <T> T parseJsonObject(String jsonResponse, Class<T> valueType) {
        try {
            return this.objectMapper.readValue(jsonResponse, valueType);
        } catch (JsonProcessingException e) {
            throw new DogeHttpException("INTERNAL_SERVER_PARSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
