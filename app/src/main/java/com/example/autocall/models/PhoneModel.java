package com.example.autocall.models;

import java.io.Serializable;

public class PhoneModel implements Serializable {
    private int id;
    private String name;
    private String phone_Number;

    public PhoneModel() {
    }

    public PhoneModel(int id, String name, String phone_Number) {
        this.id = id;
        this.name = name;
        this.phone_Number = phone_Number;
    }

    public PhoneModel(String name, String phone_Number) {
        this.id = 1;
        this.name = name;
        this.phone_Number = phone_Number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_Number() {
        return phone_Number;
    }

    public void setPhone_Number(String phone_Number) {
        this.phone_Number = phone_Number;
    }

}
