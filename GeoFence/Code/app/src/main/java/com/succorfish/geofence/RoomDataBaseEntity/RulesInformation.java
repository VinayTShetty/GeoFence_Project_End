package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Rule_info_Table")
public class RulesInformation implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "Rule")
    private String Rule="";
    @ColumnInfo(name = "Rule_ID")
    private String Rule_ID="";
    @ColumnInfo(name = "description")
    private String description="";

    public RulesInformation(String rule, String rule_ID, String description) {
        Rule = rule;
        Rule_ID = rule_ID;
        this.description = description;
    }

    public RulesInformation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRule() {
        return Rule;
    }

    public void setRule(String rule) {
        Rule = rule;
    }

    public String getRule_ID() {
        return Rule_ID;
    }

    public void setRule_ID(String rule_ID) {
        Rule_ID = rule_ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
