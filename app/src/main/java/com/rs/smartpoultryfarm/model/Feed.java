package com.rs.smartpoultryfarm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Feed implements Serializable {
    @Expose
    @SerializedName("field1")
    private String field1;
    @SerializedName("field2")
    @Expose
    private String field2;
    @SerializedName("field3")
    @Expose
    private String field3;

    public Feed() {}

    public Feed(double temperature, double humidity, double airQuality) {
        this.field1 = String.valueOf(temperature);
        this.field2 = String.valueOf(humidity);
        this.field3 = String.valueOf(airQuality);
    }

    public String getFieldOne() {
        return field1;
    }

    public String getFieldTwo() {
        return field2;
    }

    public String getFieldThree() {
        return field3;
    }
}
