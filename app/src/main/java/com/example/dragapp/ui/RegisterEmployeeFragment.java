package com.example.dragapp.ui;

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
import com.example.dragapp.model.Employee;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.UUID;

public class RegisterEmployeeFragment extends Fragment {

    public static final String COLLECTION_EMPLOYEES = "employees";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputEditText inputFullName, inputPhone, inputPassword, inputConfirmPassword;
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
        btnRegister = view.findViewById(R.id.btn_register_employee);

        btnRegister.setOnClickListener(v -> submit());
    }

    private void submit() {
        String fullName = getText(inputFullName);
        String phone = getText(inputPhone);
        String password = getText(inputPassword);
        String confirm = getText(inputConfirmPassword);

        if (fullName == null || fullName.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_full_name_required, Toast.LENGTH_SHORT).show();
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

        Employee emp = new Employee();
        emp.setId(UUID.randomUUID().toString());
        emp.setFullName(fullName.trim());
        emp.setPhone(phone.trim());
        emp.setPassword(password);

        btnRegister.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<Employee>> res = dal.saveData(emp, COLLECTION_EMPLOYEES, null);
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
