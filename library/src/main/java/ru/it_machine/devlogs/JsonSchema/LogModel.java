package ru.it_machine.devlogs.JsonSchema;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LogModel {

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("value")
    @Expose
    public String value;

    @SerializedName("date")
    @Expose
    public long date;
}