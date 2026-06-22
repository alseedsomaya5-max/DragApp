package com.example.dragapp.ui;

import android.app.DatePickerDialog;
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

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RegisterUserFragment extends Fragment {

    public static final String COLLECTION_USERS = "users";
    public static final String ARG_EDIT_MODE = "edit_mode";
    public static final String ARG_USER_ID = "user_id";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private TextInputEditText inputFullName, inputIdNumber, inputPhone, inputEmail, inputPassword, inputConfirmPassword, inputDob;
    private MaterialButton btnRegister;
    private com.google.android.material.textfield.TextInputLayout passwordLayout, confirmPasswordLayout;
    private boolean isEditMode = false;
    private String currentUserId = "";
    private String photoFilePath = "";

    public static RegisterUserFragment newInstance() {
        return new RegisterUserFragment();
    }

    public static RegisterUserFragment newInstanceForEdit(String userId) {
        RegisterUserFragment fragment = new RegisterUserFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_EDIT_MODE, true);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean(ARG_EDIT_MODE, false);
            currentUserId = args.getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        
        if (isEditMode) {
            loadUserData();
            updateUIForEditMode();
        }
    }

    private void initViews(View view) {
        inputFullName = view.findViewById(R.id.input_full_name);
        inputIdNumber = view.findViewById(R.id.input_id_number);
        inputPhone = view.findViewById(R.id.input_phone);
        inputEmail = view.findViewById(R.id.input_email);
        inputPassword = view.findViewById(R.id.input_password);
        inputConfirmPassword = view.findViewById(R.id.input_confirm_password);
        inputDob = view.findViewById(R.id.input_dob);
        btnRegister = view.findViewById(R.id.btn_register_user);
        
        passwordLayout = view.findViewById(R.id.password_layout);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);

        inputDob.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 20;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            inputDob.setText(date);
        }, year, month, day).show();
    }

    private void setupClickListeners() {
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> submit());
        }
    }

    private void updateUIForEditMode() {
        if (btnRegister != null) {
            btnRegister.setText("تحديث البيانات");
        }
        if (passwordLayout != null && confirmPasswordLayout != null) {
            passwordLayout.setVisibility(View.GONE);
            confirmPasswordLayout.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        if (isEditMode && currentUserId != null && !currentUserId.isEmpty()) {
            loadUserByPhone(currentUserId);
        } else {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String phone = prefs.getString("user_phone", "");
            if (!phone.isEmpty()) {
                loadUserByPhone(phone);
            }
        }
    }

    private void loadUserByPhone(String phone) {
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData(COLLECTION_USERS, null, User.class);
            
            if (res != null && res.success && res.data != null) {
                for (User user : res.data) {
                    if (phone.equals(user.getPhone())) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> populateUserData(user));
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    private void populateUserData(User user) {
        if (inputFullName != null && user.getName() != null) inputFullName.setText(user.getName());
        if (inputDob != null && user.getDateOfBirth() != null) inputDob.setText(user.getDateOfBirth());
        if (inputIdNumber != null && user.getIdNumber() != null) inputIdNumber.setText(user.getIdNumber());
        if (inputPhone != null && user.getPhone() != null) inputPhone.setText(user.getPhone());
        if (inputEmail != null && user.getEmail() != null) inputEmail.setText(user.getEmail());
    }

    private void submit() {
        String fullName = getText(inputFullName);
        String idNumber = getText(inputIdNumber);
        String phone = getText(inputPhone);
        String email = getText(inputEmail);
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

        if (!isEditMode && password.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_password_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEditMode && !password.equals(confirm)) {
            Toast.makeText(requireContext(), R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        if (isEditMode) {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            user.setPhone(prefs.getString("user_phone", ""));
            user.setPassword(prefs.getString("user_password", ""));
        } else {
            String userId = UUID.randomUUID().toString();
            user.setId(userId);
            user.setUserId(userId);
            user.setPassword(password);
        }
        
        user.setName(fullName);
        user.setDateOfBirth(dob);
        user.setIdNumber(idNumber);
        user.setPhone(phone);
        user.setEmail(email);

        btnRegister.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<?> res;
            
            if (isEditMode) {
                res = dal.updateData(user, COLLECTION_USERS, user.getPhone(), COLLECTION_USERS);
            } else {
                res = dal.saveData(user, COLLECTION_USERS, null);
            }
            
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (res != null && res.success) {
                    ProfileFragment.saveUserInfo(requireContext(), user.getName(), user.getEmail(), user.getPhone());
                    Toast.makeText(requireContext(), isEditMode ? "تم التحديث بنجاح" : "تم التسجيل بنجاح", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                } else {
                    final String errorDetail = (res != null && res.message != null) ? res.message : "فشل الاتصال";
                    final String fullError = (res != null) ? "ErrorCode: " + res.errorCode + "\nMsg: " + res.message : "No response";

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("فشل في العملية")
                        .setMessage("حدث خطأ أثناء معالجة بياناتك.\n\nالسبب: " + errorDetail)
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
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
