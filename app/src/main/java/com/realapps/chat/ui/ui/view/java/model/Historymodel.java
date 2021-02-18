package com.realapps.chat.ui.ui.view.java.model;

/**
 * Created by Himadri on 19/7/17.
 */

public class Historymodel {
    private String callstart;
    private String debit;
    private String pattern;
    private String notes;
    private String calltype;
    private String callerid;
    private String callednum;
    private String billseconds;
    private String disposition;
    private boolean isSelected = false;
    private String cost;

    private String calldate;
    private String callstatus;
    private String country;
    private String uniqueId;
    private String userName;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }


    public String getCallstart() {
        return callstart;
    }

    public void setCallstart(String callstart) {
        this.callstart = callstart;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public String getCallednum() {
        return callednum;
    }

    public void setCallednum(String callednum) {
        this.callednum = callednum;
    }

    public String getBillseconds() {
        return billseconds;
    }

    public void setBillseconds(String billseconds) {
        this.billseconds = billseconds;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getCalldate() {
        return calldate;
    }

    public void setCalldate(String calldate) {
        this.calldate = calldate;
    }


    public String getCallstatus() {
        return callstatus;
    }

    public void setCallstatus(String callstatus) {
        this.callstatus = callstatus;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String country) {
        this.uniqueId = country;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Historymodel() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Historymodel(String callstart, String debit, String pattern, String notes,
                        String calltype, String callerid, String callednum,
                        String billseconds, String disposition) {
        this.callstart = callstart;
        this.debit = debit;
        this.pattern = pattern;
        this.notes = notes;
        this.calltype = calltype;
        this.callerid = callerid;
        this.callednum = callednum;
        this.billseconds = billseconds;
        this.disposition = disposition;

    }
}
