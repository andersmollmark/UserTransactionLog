package com.delaval.usertransactionlogserver.websocket;

/**
 * Dataholder for the message from the webclient
 */
public class WebSocketMessage extends WebSocketType{

    private String jsonContent;
    private String client;
    private String username;
    private String target;

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
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
        return username.toLowerCase();

    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
        sb.append(", Messtype:").append(getType())
                .append(", client:").append(getClient())
                .append(", username:").append(getUsername())
                .append(", target:").append(getTarget())
                .append(", content:").append(getJsonContent());
        return sb.toString();
    }
}
