package com.example.gokul.attendence.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login {


    @Expose
    @SerializedName("access_token")
    private String access_token;
    @Expose
    @SerializedName("gender")
    private String gender;
    @Expose
    @SerializedName("phone")
    private String phone;
    @Expose
    @SerializedName("email")
    private String email;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("empId")
    private int empId;

    public Login(String access_token, String gender, String phone, String email, String name, int empId) {
        this.access_token = access_token;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.name = name;
        this.empId = empId;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public int getEmpId() {
        return empId;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }
}
