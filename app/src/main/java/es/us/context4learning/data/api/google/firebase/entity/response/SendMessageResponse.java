package es.us.context4learning.data.api.google.firebase.entity.response;

import com.google.gson.annotations.SerializedName;

public class SendMessageResponse {

    @SerializedName("message_id")
    private String messageId;

    public SendMessageResponse() {
    }

    public SendMessageResponse(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
