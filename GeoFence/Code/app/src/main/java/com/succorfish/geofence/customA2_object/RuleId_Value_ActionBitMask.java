package com.succorfish.geofence.customA2_object;

import java.io.Serializable;

public class RuleId_Value_ActionBitMask implements Serializable {
    private int ruleId;
    private String value;
    private String actionBitMask;

  public RuleId_Value_ActionBitMask(int loc_ruleId,String loc_value,String loc_actionBitMask){
        this.ruleId=loc_ruleId;
        this.value=loc_value;
        this.actionBitMask=loc_actionBitMask;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getActionBitMask() {
        return actionBitMask;
    }

    public void setActionBitMask(String actionBitMask) {
        this.actionBitMask = actionBitMask;
    }
}
