package com.delaval.usertransactionlogserver.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by delaval on 12/8/2015.
 */
public class ServletHelper {

    public enum ClickLogValues {
        CLIENT("client"),
        USERNAME("username"),
        TARGET("target"),
        CSS("cssClass"),
        ELEMENT_ID("elementId"),
        X_POS("xPos"),
        Y_POS("yPos"),
        HOST("host"),
        TAB("tab");

        private String myValue;

        ClickLogValues(String myValue){
            this.myValue = myValue;
        }

        public String getMyValue(){
            return myValue;
        }
    }

    public enum EventLogValues {
        CLIENT("client"),
        NAME("eventname"),
        CATEGORY("category"),
        LABEL("label"),
        HOST("host"),
        TAB("tab");

        private String myValue;

        EventLogValues(String myValue){
            this.myValue = myValue;
        }

        public String getMyValue(){
            return myValue;
        }
    }


    public String getTDTag(String content){
        return  "<td>" + content + "</td>";
    }

    public String getTRTag(String content){
        return "<tr>" + content + "</tr>";
    }

    public String getTHTag(String content){
        return "<th>" + content + "</th>";
    }

    public String getInputTextTag(String name){
        return getInputTextTag(name, false);
    }

    public String getDisabledInputTextTag(String name){
        return getInputTextTag(name, true);
    }


    private String getInputTextTag(String name, boolean disabled){
        if(disabled) {
            return "<td>" + name + ":</td>" + "<td><input type=\"text\" name=\"" + name + "\" disabled=disabled value=\"" + name + "\"></td>";
        }
        return "<td>" + name + ":</td>" + "<td><input type=\"text\" name=\"" + name + "\"></td>";
    }

    public String getParam(HttpServletRequest request, String name){
        return request.getParameter(name) != null && request.getParameter(name).length() > 0 ? request.getParameter(name) : "dummy" + name;
    }

    public boolean existParam(HttpServletRequest request, String name){
        String param = getParam(request, name);
        return param != null && param.length()>0;
    }

}
