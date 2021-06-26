package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 28-02-2018.
 */

public class VoVessel implements Serializable {

    String id = "";
    String name = "";
    String registry = "";
    String port = "";
    String regNo = "";
    String accountId = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getVessel_port_no() {
        return port;
    }

    public void setVessel_port_no(String vessel_port_no) {
        this.port = vessel_port_no;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
