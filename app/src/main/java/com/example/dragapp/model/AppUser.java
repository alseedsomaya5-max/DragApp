package com.example.dragapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * مستخدم (تسجيل): الاسم الكامل، تاريخ الميلاد، رقم الهوية ٩ أرقام، صورة، هاتف، كلمة سر.
 */
public class AppUser implements Serializable {

    @SerializedName(value = "$id", alternate = {"id", "userId"})
    private String id;

    private String fullName;
    private String dateOfBirth;  // بصيغة yyyy-MM-dd
    private String idNumber;    // 9 أرقام
    private String photoUrl;
    private String phone;
    /** لا يُخزَّن في قاعدة البيانات؛ يُستخدم عند التسجيل فقط */
    private transient String password;

    public AppUser() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
