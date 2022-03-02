package xyz.suchdoge.webapi.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.blockIo.BlockIo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.blockchain.TransactionRequirementsResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.blockchain.*;
import xyz.suchdoge.webapi.model.blockchain.response.*;

import java.util.Map;

@Service
public class DogeBlockchainService {
    private final BlockIo blockIo;
    private final ObjectMapper objectMapper;
    private final DogeConstantsConfig dogeConstantsConfig;

    public DogeBlockchainService(@Qualifier("dogeBlockIo") BlockIo blockIo,
                                 ObjectMapper objectMapper,
                                 DogeConstantsConfig dogeConstantsConfig) {
        this.blockIo = blockIo;
        this.objectMapper = objectMapper;
        this.dogeConstantsConfig = dogeConstantsConfig;
    }

    /**
     * @return sum of balances of all addresses/users.
     * @throws Exception when can not get the balance.
     */
    public Balance getBalance() throws Exception {
        String jsonResponse = this.blockIo.GetBalance(null).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_GET_APP_WALLET");
        BlockIoResponse<Balance> walletResponse = this.objectMapper.readValue(jsonResponse, BalanceResponse.class);
        return walletResponse.getData();
    }

    /**
     * @return newly generated doge coin address.
     * @throws Exception when can not create new address.
     */
    public Address createWallet() throws Exception {
        String jsonResponse = this.blockIo.GetNewAddress(null).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_CREATE_NEW_WALLET");

        AddressResponse addressResponse = this.objectMapper.readValue(jsonResponse, AddressResponse.class);
        return addressResponse.getData();
    }

    /**
     * @param label will be used to associate user with the address.
     * @return newly generated doge coin address.
     * @throws Exception when can not create new address.
     */
    public Address createWallet(String label) throws Exception {
        final JSONObject params = new JSONObject(Map.of("label", label));
        String jsonResponse = this.blockIo.GetNewAddress(params).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_CREATE_NEW_ADDRESS");

        AddressResponse addressResponse = this.objectMapper.readValue(jsonResponse, AddressResponse.class);
        return addressResponse.getData();
    }

    /**
     * @param label of the wallet to get
     * @return a doge coin address related to the label
     * @throws Exception when can not get the address.
     */
    public Wallet getWallet(String label) throws Exception {
        final JSONObject params = new JSONObject(Map.of("label", label));
        String jsonResponse = this.blockIo.GetAddressByLabel(params).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_GET_ADDRESS");
        WalletResponse walletResponse = this.objectMapper.readValue(jsonResponse, WalletResponse.class);
        return walletResponse.getData();
    }

    /**
     * @return the application doge coin address
     * @throws Exception when can not get the address.
     */
    public Wallet getAppWallet() throws Exception {
        return this.getWallet(this.dogeConstantsConfig.getAppWalletLabel());
    }

    public NetworkFee calculateNetworkFee(Double amount, String toLabel, TransactionPriority priority) throws Exception {
        this.validateTransactionAmount(amount);
        final JSONObject params = new JSONObject(Map.of(
                "amounts", amount.toString(),
                "to_labels", toLabel,
                "priority", priority.toString().toLowerCase()
        ));
        String jsonResponse = this.blockIo.GetNetworkFeeEstimate(params).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_CALCULATE_NETWORK_FEE");
        NetworkFeeResponse networkFeeResponse = this.objectMapper.readValue(jsonResponse, NetworkFeeResponse.class);

        NetworkFee networkFee = networkFeeResponse.getData();
        networkFee.addAdditionalFee(amount * this.dogeConstantsConfig.getTransactionFeePercent() / 100d);
        return networkFee;
    }

    public TransactionRequirementsResponseDto getTransactionRequirements() {
        return TransactionRequirementsResponseDto.builder()
                .minTransactionAmount(this.dogeConstantsConfig.getMinTransactionAmount())
                .maxTransactionAmount(this.dogeConstantsConfig.getMaxTransactionAmount())
                .transactionFeePercent(this.dogeConstantsConfig.getTransactionFeePercent())
                .network(Network.DOGETEST.toString())
                .build();
    }

    public ValidatedAddress validateAddress(String address) throws Exception {
        final JSONObject params = new JSONObject(Map.of("address", address));
        String jsonResponse = this.blockIo.IsValidAddress(params).toString();
        this.checkForErrors(jsonResponse, "CAN_NOT_VALIDATE_ADDRESS");
        ValidatedAddressResponse validatedAddressResponse =
                this.objectMapper.readValue(jsonResponse, ValidatedAddressResponse.class);
        return validatedAddressResponse.getData();
    }

//    public void prepareTransaction(String amount, String fromLabel, String toLabel) throws Exception {
//        final JSONObject params = new JSONObject(Map.of(
//                "amounts", amount,
//                "from_labels", fromLabel,
//                "to_labels", toLabel));
//        String jsonResponse = this.blockIo.PrepareTransaction(params).toString();
//    }

    private void checkForErrors(String jsonResponse, String defaultMessage) throws Exception {
        BlockIoResponse<?> response = this.objectMapper.readValue(jsonResponse, BlockIoResponse.class);

        if (response.getStatus().equals(Status.FAIL)) {
            BlockErrorResponse errorResponse = this.objectMapper.readValue(jsonResponse, BlockErrorResponse.class);

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

            System.out.println(jsonResponse);

            throw new DogeHttpException(defaultMessage, HttpStatus.BAD_REQUEST);
        }
    }


    private void validateTransactionAmount(Double amount) {
        if (amount < this.dogeConstantsConfig.getMinTransactionAmount()) {
            throw new DogeHttpException("TRANSACTION_AMOUNT_TOO_LOW", HttpStatus.BAD_REQUEST);
        }
        if (amount > this.dogeConstantsConfig.getMaxTransactionAmount()) {
            throw new DogeHttpException("TRANSACTION_AMOUNT_TOO_HIGH", HttpStatus.BAD_REQUEST);
        }
    }
}
