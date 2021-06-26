package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 26-02-2018.
 */

public class VoVesselAsset implements Serializable {

    String vessel_local_id = "";
    String vessel_server_id = "";
    String vessel_succorfish_id = "";
    String vessel_account_id = "";
    String vessel_name = "";
    String vessel_reg_no = "";
    String vessel_port_no = "";
    String isSync = "";

    public String getVessel_local_id() {
        return vessel_local_id;
    }

    public void setVessel_local_id(String vessel_local_id) {
        this.vessel_local_id = vessel_local_id;
    }

    public String getVessel_server_id() {
        return vessel_server_id;
    }

    public void setVessel_server_id(String vessel_server_id) {
        this.vessel_server_id = vessel_server_id;
    }

    public String getVessel_name() {
        return vessel_name;
    }

    public void setVessel_name(String vessel_name) {
        this.vessel_name = vessel_name;
    }

    public String getVessel_reg_no() {
        return vessel_reg_no;
    }

    public void setVessel_reg_no(String vessel_reg_no) {
        this.vessel_reg_no = vessel_reg_no;
    }

    public String getVessel_port_no() {
        return vessel_port_no;
    }

    public void setVessel_port_no(String vessel_port_no) {
        this.vessel_port_no = vessel_port_no;
    }

    public String getIsSync() {
        return isSync;
    }

    public void setIsSync(String isSync) {
        this.isSync = isSync;
    }

    public String getVessel_succorfish_id() {
        return vessel_succorfish_id;
    }

    public void setVessel_succorfish_id(String vessel_succorfish_id) {
        this.vessel_succorfish_id = vessel_succorfish_id;
    }

    public String getVessel_account_id() {
        return vessel_account_id;
    }

    public void setVessel_account_id(String vessel_account_id) {
        this.vessel_account_id = vessel_account_id;
    }
}
