package xyz.suchdoge.webapi.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.blockIo.BlockIo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.blockchain.*;
import xyz.suchdoge.webapi.model.blockchain.response.*;

import java.util.Map;

@Service
public class DogeBlockchainService {
    private final BlockIo blockIo;
    private final ObjectMapper objectMapper;

    @Value("${BLOCK_IO_APP_WALLET_LABEL}")
    private String appWalletLabel;

    public DogeBlockchainService(@Qualifier("dogeBlockIo") BlockIo blockIo, ObjectMapper objectMapper) {
        this.blockIo = blockIo;
        this.objectMapper = objectMapper;
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
        return this.getWallet(this.appWalletLabel);
    }

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

            throw new DogeHttpException(defaultMessage, HttpStatus.BAD_REQUEST);
        }
    }
}
