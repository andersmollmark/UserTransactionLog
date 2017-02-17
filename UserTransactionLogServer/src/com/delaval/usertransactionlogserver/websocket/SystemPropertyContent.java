package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.util.DateUtil;

/**
 * Describes the content of the change-configuration for the delete-event
 */
public class SystemPropertyContent implements JsonContent{

    private String value;
    private String name;
    private String timestamp;

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SystemPropertyContent:")
                .append("name:").append(getName())
                .append("value:").append(getValue())
                .append(", time:").append(getTimestamp() != null ? DateUtil.formatTimeStamp(Long.parseLong(getTimestamp())): "null");
        return sb.toString();
    }


}
