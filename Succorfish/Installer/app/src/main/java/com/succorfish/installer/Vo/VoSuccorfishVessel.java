package com.succorfish.installer.Vo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoSuccorfishVessel {

    @SerializedName("accountId")
    @Expose
    private String account_Id;
    @SerializedName("id")
    @Expose
    private String Id;
    @SerializedName("name")
    @Expose
    private String Name;
    @SerializedName("regNo")
    @Expose
    private String reg_no;


    public String getAccount_Id() {
        return account_Id;
    }

    public void setAccount_Id(String account_Id) {
        this.account_Id = account_Id;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getReg_no() {
        return reg_no;
    }

    public void setReg_no(String reg_no) {
        this.reg_no = reg_no;
    }
}
