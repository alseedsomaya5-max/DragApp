package com.example.dragapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * موظف: الاسم الكامل، رقم الهاتف، كلمة السر.
 */
public class Employee implements Serializable {

    @SerializedName(value = "$id", alternate = {"id", "employeeId"})
    private String id;

    private String fullName;
    private String phone;
    /** لا يُخزَّن في قاعدة البيانات؛ يُستخدم عند التسجيل فقط */
    private transient String password;

    public Employee() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
