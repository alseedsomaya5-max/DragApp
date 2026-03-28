package com.example.dragapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.example.dragapp.DALAppWriteConnection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    private static final String COLLECTION_USERS = "users";
    
    private TextInputEditText inputPhone, inputPassword;
    private MaterialButton btnLogin, btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        inputPhone = view.findViewById(R.id.input_phone);
        inputPassword = view.findViewById(R.id.input_password);
        btnLogin = view.findViewById(R.id.btn_login);
        btnRegister = view.findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            if (getActivity() instanceof LoginListener) {
                ((LoginListener) getActivity()).onRegisterRequested();
            }
        });
    }

    private void attemptLogin() {
        String phone = getText(inputPhone);
        String password = getText(inputPassword);

        // التحقق من المدخلات
        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_phone_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password == null || password.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_password_required, Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        
        // البحث عن المستخدم في Appwrite
        new Thread(() -> {
            try {
                DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData(COLLECTION_USERS, null, User.class);
                
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    
                    if (res != null && res.success && res.data != null) {
                        User foundUser = null;
                        for (User user : res.data) {
                            if (user.getPhone() != null && user.getPhone().equals(phone.trim()) &&
                                user.getPassword() != null && user.getPassword().equals(password)) {
                                foundUser = user;
                                break;
                            }
                        }
                        
                        if (foundUser != null) {
                            // تسجيل الدخول ناجح - حفظ البيانات محلياً
                            ProfileFragment.saveUserInfo(requireContext(), 
                                foundUser.getName(), 
                                foundUser.getPhone() + "@medicines.app", 
                                foundUser.getPhone());
                            
                            // حفظ كلمة المرور للاستخدام في التعديل
                            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("user_password", foundUser.getPassword());
                            editor.apply();
                            
                            Toast.makeText(requireContext(), "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                            
                            // العودة للبروفايل
                            if (getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        } else {
                            Toast.makeText(requireContext(), "رقم الهاتف أو كلمة السر غير صحيحة", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.error_no_internet, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(requireContext(), "حدث خطأ أثناء تسجيل الدخول", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private String getText(TextInputEditText ed) {
        return ed.getText() != null ? ed.getText().toString() : "";
    }

    public interface LoginListener {
        void onRegisterRequested();
    }
}
