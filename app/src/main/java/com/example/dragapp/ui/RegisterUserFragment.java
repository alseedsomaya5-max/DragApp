package com.example.dragapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.AppUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RegisterUserFragment extends Fragment {

    public static final String COLLECTION_APP_USERS = "app_users";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputEditText inputFullName, inputDob, inputIdNumber, inputPhone, inputPassword, inputConfirmPassword;
    private ImageView userPhotoPreview;
    private MaterialButton btnTakePhoto, btnRegister;
    private String photoFilePath; // مسار الصورة إن وُجدت

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputFullName = view.findViewById(R.id.input_full_name);
        inputDob = view.findViewById(R.id.input_dob);
        inputIdNumber = view.findViewById(R.id.input_id_number);
        inputPhone = view.findViewById(R.id.input_phone);
        inputPassword = view.findViewById(R.id.input_password);
        inputConfirmPassword = view.findViewById(R.id.input_confirm_password);
        userPhotoPreview = view.findViewById(R.id.user_photo_preview);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        btnRegister = view.findViewById(R.id.btn_register_user);

        inputDob.setOnClickListener(v -> showDatePicker());
        btnTakePhoto.setOnClickListener(v -> openPhotoPicker());
        btnRegister.setOnClickListener(v -> submit());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog d = new DatePickerDialog(requireContext(), (picker, y, m, d1) -> {
            c.set(y, m, d1);
            inputDob.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime()));
        }, year, month, day);
        d.getDatePicker().setMaxDate(System.currentTimeMillis());
        d.show();
    }

    private void openPhotoPicker() {
        // اختياري: فتح الكاميرا أو المعرض. حالياً نترك الزر للاستخدام لاحقاً
        Toast.makeText(requireContext(), R.string.take_photo, Toast.LENGTH_SHORT).show();
    }

    private boolean validateDob(String dob) {
        if (dob == null || dob.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            java.util.Date date = sdf.parse(dob.trim());
            if (date == null) return false;
            return !date.after(new java.util.Date());
        } catch (Exception e) {
            return false;
        }
    }

    private void submit() {
        String fullName = getText(inputFullName);
        String dob = getText(inputDob);
        String idNumber = getText(inputIdNumber);
        String phone = getText(inputPhone);
        String password = getText(inputPassword);
        String confirm = getText(inputConfirmPassword);

        if (fullName == null || fullName.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_full_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (dob == null || dob.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_date_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateDob(dob)) {
            Toast.makeText(requireContext(), R.string.error_date_future, Toast.LENGTH_SHORT).show();
            return;
        }
        if (idNumber == null || !idNumber.matches("\\d{9}")) {
            Toast.makeText(requireContext(), R.string.error_id_must_9_digits, Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_phone_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.isEmpty()) {
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

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setFullName(fullName.trim());
        user.setDateOfBirth(dob.trim());
        user.setIdNumber(idNumber.trim());
        user.setPhotoUrl(photoFilePath != null ? photoFilePath : "");
        user.setPhone(phone.trim());
        user.setPassword(password);

        btnRegister.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<AppUser>> res = dal.saveData(user, COLLECTION_APP_USERS, null);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (res != null && res.success) {
                    Toast.makeText(requireContext(), R.string.registration_success, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    String msg = res != null && res.message != null ? res.message : getString(R.string.save);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private String getText(TextInputEditText ed) {
        return ed.getText() != null ? ed.getText().toString() : "";
    }
}
