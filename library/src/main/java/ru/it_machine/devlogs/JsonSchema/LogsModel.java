package ru.it_machine.devlogs.JsonSchema;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class LogsModel {

    @SerializedName("logs")
    @Expose
    public List<LogModel> logs = new LinkedList<>();

}