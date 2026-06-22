package com.example.dragapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RegisterEmployeeFragment extends Fragment {

    public static final String COLLECTION_USERS = "users";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputEditText inputFullName, inputPhone, inputPassword, inputConfirmPassword, inputDob;
    private MaterialButton btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputFullName = view.findViewById(R.id.input_full_name);
        inputPhone = view.findViewById(R.id.input_phone);
        inputPassword = view.findViewById(R.id.input_password);
        inputConfirmPassword = view.findViewById(R.id.input_confirm_password);
        inputDob = view.findViewById(R.id.input_dob);
        btnRegister = view.findViewById(R.id.btn_register_employee);

        inputDob.setOnClickListener(v -> showDatePicker());
        btnRegister.setOnClickListener(v -> submit());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            inputDob.setText(date);
        }, calendar.get(Calendar.YEAR) - 25, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submit() {
        String fullName = getText(inputFullName);
        String phone = getText(inputPhone);
        String dob = getText(inputDob);
        String password = getText(inputPassword);
        String confirm = getText(inputConfirmPassword);

        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_full_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_phone_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (dob.isEmpty()) {
            Toast.makeText(requireContext(), "تاريخ الميلاد مطلوب", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_password_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(requireContext(), R.string.error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(requireContext(), R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        User emp = new User();
        emp.setId(UUID.randomUUID().toString());
        emp.setUserId(emp.getId());
        emp.setName(fullName);
        emp.setPhone(phone);
        emp.setDateOfBirth(dob);
        emp.setPassword(password);
        emp.setUserType("employee");

        btnRegister.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.saveData(emp, COLLECTION_USERS, null);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (res != null && res.success) {
                    Toast.makeText(requireContext(), R.string.registration_success, Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                } else {
                    final String errorDetail = (res != null && res.message != null) ? res.message : "فشل الحفظ";
                    final String fullError = (res != null) ? "ErrorCode: " + res.errorCode + "\nMsg: " + res.message : "No response";

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("فشل التسجيل")
                        .setMessage("حدث خطأ أثناء حفظ بيانات الموظف.\n\nالسبب: " + errorDetail)
                        .setNeutralButton("نسخ الخطأ", (dialog, which) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Appwrite Error", fullError);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(requireContext(), "تم نسخ تفاصيل الخطأ", Toast.LENGTH_SHORT).show();
                        })
                        .setPositiveButton("حسناً", null)
                        .show();
                }
            });
        }).start();
    }

    private String getText(TextInputEditText ed) {
        return ed != null && ed.getText() != null ? ed.getText().toString().trim() : "";
    }
}
