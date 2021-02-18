package com.realapps.chat.ui.ui.view.java.model;

/**
 * Created by inextrix on 3/4/18.
 */

public class ConferenceContactPojo {

    String name,number,country;
    public ConferenceContactPojo() {
    }

    public ConferenceContactPojo(String name, String number, String country) {
        this.name=name;
        this.number=number;
        this.country=country;

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


}
