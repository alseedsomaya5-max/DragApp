package com.example.dragapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Patient implements Serializable {

    /** يطابق $id من Appwrite أو patientId أو id */
    @SerializedName(value = "$id", alternate = {"patientId", "id"})
    private String id;
    private String name;

    /** يُرسل إلى Appwrite إذا كانت المجموعة تتطلب حقل title (عنوان) */
    private String title;

    public Patient() {
    }

    public Patient(String id, String name) {
        this.id = id;
        this.name = name;
        this.title = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (name != null && !name.isEmpty()) return name;
        return title != null ? title : "";
    }

    public void setName(String name) {
        this.name = name;
        this.title = name;
    }

    public String getTitle() {
        return title != null ? title : name;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
