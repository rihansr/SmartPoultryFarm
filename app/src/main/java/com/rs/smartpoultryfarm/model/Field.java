package com.rs.smartpoultryfarm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Field implements Serializable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("icon")
    @Expose
    private int icon;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("curValue")
    @Expose
    private String curValue;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("prevValue")
    @Expose
    private String prevValue;
    @SerializedName("status")
    @Expose
    private String status;

    public Field() {}



    public Field(String id, int icon, String label, String unit) {
        this.id = id;
        this.icon = icon;
        this.label = label;
        this.unit = unit;
    }

    public Field(String id, String label, String status) {
        this.id = id;
        this.label = label;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCurValue() {
        return curValue;
    }

    public void setCurValue(String curValue) {
        this.curValue = curValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPrevValue() {
        return prevValue;
    }

    public void setPrevValue(String prevValue) {
        this.prevValue = prevValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
