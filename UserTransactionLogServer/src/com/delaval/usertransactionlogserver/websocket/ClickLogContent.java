package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.util.DateUtil;

/**
 * Describes the content of the log from the webclient.
 */
public class ClickLogContent implements JsonContent {

    private String x;
    private String y;
    private String elementId;
    private String cssClassName;
    private String timestamp;
    private String tab;
    private String host;

    public void setX(String x) {
        this.x = x;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getX() {
        return x;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getY() {
        return y;
    }

    public String getElementId() {
        return elementId;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public String getTab() {
        return tab;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClickLogContent:")
          .append("x-pos:").append(getX())
          .append(", y-pos:").append(getY())
          .append(", elementId:").append(getElementId())
          .append(", cssClassName:").append(getCssClassName())
          .append(", tab:").append(getTab())
          .append(", host:").append(getHost())
          .append(", time:").append(DateUtil.formatTimeStamp(Long.parseLong(getTimestamp())));
        return sb.toString();
    }


}
