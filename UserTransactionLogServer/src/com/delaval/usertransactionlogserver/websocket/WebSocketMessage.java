package com.delaval.usertransactionlogserver.websocket;

/**
 * Dataholder for the message from the webclient
 */
public class WebSocketMessage {

    private String jsonContent;
    private String client;
    private String username;
    private String messType;
    private String target;

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessType(String messType) {
        this.messType = messType;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public String getClient() {
        return client;
    }

    public String getTarget() {
        return target;
    }

    public String getUsername() {
        return username;

    }

    public String getMessType() {
        return messType;
    }

    public String getType() {

        return messType;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Messtype:").append(getType())
                .append(", client:").append(getClient())
                .append(", username:").append(getUsername())
                .append(", target:").append(getTarget())
                .append(", Content:").append(getJsonContent());
        return sb.toString();
    }
}
