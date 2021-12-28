package com.rs.smartpoultryfarm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Feed implements Serializable {
    @SerializedName("field1")
    @Expose
    private String field1;
    @SerializedName("field2")
    @Expose
    private String field2;
    @SerializedName("field3")
    @Expose
    private String field3;
    @SerializedName("field4")
    @Expose
    private String field4;

    public Feed() {}

    public Feed(double temperature, double humidity, double airQuality) {
        this.field1 = String.valueOf(temperature);
        this.field2 = String.valueOf(humidity);
        this.field3 = String.valueOf(airQuality);
    }

    public Feed(String light1, String light2) {
        this.field1 = String.valueOf(light1);
        this.field2 = String.valueOf(light2);
    }

    public String getField1() {
        return field1;
    }

    public String getField2() {
        return field2;
    }

    public String getField3() {
        return field3;
    }

    public String getField4() {
        return field4;
    }
}
