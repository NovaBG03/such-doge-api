package xyz.suchdoge.webapi.model.blockchain.transaction;

import org.json.simple.JSONObject;

public class PreparedTransaction {
    private final JSONObject preparedTransactionJSONObject;

    public PreparedTransaction(JSONObject preparedTransactionJSONObject) {
        this.preparedTransactionJSONObject = preparedTransactionJSONObject;
    }

    public JSONObject getJSONObject() {
        return this.preparedTransactionJSONObject;
    }
}
