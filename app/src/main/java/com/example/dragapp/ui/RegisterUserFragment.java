package com.example.dragapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.example.dragapp.ui.ProfileFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class RegisterUserFragment extends Fragment {

    public static final String COLLECTION_USERS = "users";
    public static final String ARG_EDIT_MODE = "edit_mode";
    public static final String ARG_USER_ID = "user_id";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private TextInputEditText inputFullName, inputIdNumber, inputPhone, inputEmail, inputPassword, inputConfirmPassword;
    private ImageView userPhotoPreview;
    private MaterialButton btnTakePhoto, btnRegister;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private com.google.android.material.textfield.TextInputLayout passwordLayout, confirmPasswordLayout;
    private boolean isEditMode = false;
    private String currentUserId = "";
    private String photoFilePath = null;

    // Spinners for date of birth
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

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
        // userPhotoPreview = view.findViewById(R.id.user_photo_preview); // Removed
        // btnTakePhoto = view.findViewById(R.id.btn_take_photo); // Removed
        btnRegister = view.findViewById(R.id.btn_register_user);
        
        // Password layouts for hiding in edit mode
        passwordLayout = view.findViewById(R.id.password_layout);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        
        // Initialize spinners
        spinnerDay = view.findViewById(R.id.spinner_day);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        
        setupDateSpinners();
    }

    private void setupClickListeners() {
        if (btnTakePhoto != null) {
            btnTakePhoto.setOnClickListener(v -> {
                // TODO: Implement photo picker
                Toast.makeText(requireContext(), "اختيار الصورة قيد التطوير", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> submit());
        }
    }

    private void setupDateSpinners() {
        // Setup day spinner (1-31)
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);
        
        // Setup month spinner
        String[] months = {"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", 
                          "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
        monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
        
        // Setup year spinner (1950-2020)
        String[] years = new String[71]; // 2020 - 1950 + 1 = 71
        for (int i = 0; i < 71; i++) {
            years[i] = String.valueOf(2020 - i);
        }
        yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        
        // Set default selections (January 1, 2000)
        spinnerDay.setSelection(0); // Day 1
        spinnerMonth.setSelection(0); // January
        spinnerYear.setSelection(20); // Year 2000 (2020 - 20 = 2000)
    }

    private String getSelectedDate() {
        int day = spinnerDay.getSelectedItemPosition() + 1;
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = 2020 - spinnerYear.getSelectedItemPosition();
        
        // Format as YYYY-MM-DD
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    private void setDateFromSpinner(String dateString) {
        try {
            // Expected format: YYYY-MM-DD
            String[] parts = dateString.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                
                // Set day spinner (1-31)
                if (day >= 1 && day <= 31) {
                    spinnerDay.setSelection(day - 1);
                }
                
                // Set month spinner (1-12)
                if (month >= 1 && month <= 12) {
                    spinnerMonth.setSelection(month - 1);
                }
                
                // Set year spinner (1950-2020)
                if (year >= 1950 && year <= 2020) {
                    int yearPosition = 2020 - year;
                    if (yearPosition >= 0 && yearPosition < 71) {
                        spinnerYear.setSelection(yearPosition);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RegisterUserFragment", "Error parsing date: " + dateString, e);
        }
    }

    private void updateUIForEditMode() {
        if (btnRegister != null) {
            btnRegister.setText("تحديث البيانات");
        }
        // إخفاء حقول كلمة السر في وضع التعديل
        if (passwordLayout != null && confirmPasswordLayout != null) {
            passwordLayout.setVisibility(View.GONE);
            confirmPasswordLayout.setVisibility(View.GONE);
        }
        // إخفاء زر الصورة في وضع التعديل
        if (btnTakePhoto != null) {
            btnTakePhoto.setVisibility(View.GONE);
        }
        if (userPhotoPreview != null) {
            userPhotoPreview.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        android.util.Log.d("RegisterUserFragment", "Loading user data. isEditMode: " + isEditMode + ", currentUserId: " + currentUserId);
        
        // في وضع التعديل، نستخدم currentUserId كرقم هاتف مباشرة
        if (isEditMode && currentUserId != null && !currentUserId.isEmpty()) {
            loadUserByPhone(currentUserId);
            return;
        }
        
        if (currentUserId == null) {
            // استرجاع بيانات المستخدم الحالي من SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
            String phone = prefs.getString("user_phone", "");
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
            String userName = prefs.getString("user_name", "");
            
            android.util.Log.d("RegisterUserFragment", "Phone from prefs: " + phone + ", isLoggedIn: " + isLoggedIn + ", userName: " + userName);
            
            if (!phone.isEmpty() && isLoggedIn && !userName.isEmpty()) {
                loadUserByPhone(phone);
            } else {
                android.util.Log.d("RegisterUserFragment", "User not properly logged in - missing data");
                if (isEditMode) {
                    // في وضع التعديل، يجب أن يكون المستخدم مسجل دخول
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "يجب تسجيل الدخول أولاً لتعديل البروفايل", Toast.LENGTH_LONG).show();
                            // العودة للبروفايل للتسجيل
                            if (getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        });
                    }
                } else {
                    // في وضع التسجيل الجديد، هذا طبيعي
                    clearAllFields();
                }
            }
        } else {
            loadUserById(currentUserId);
        }
    }

    private void clearAllFields() {
        if (inputFullName != null) inputFullName.setText("");
        if (inputIdNumber != null) inputIdNumber.setText("");
        if (inputPhone != null) inputPhone.setText("");
        if (inputPassword != null) inputPassword.setText("");
        if (inputConfirmPassword != null) inputConfirmPassword.setText("");
        
        // Reset spinners to default values
        if (spinnerDay != null) spinnerDay.setSelection(0); // Day 1
        if (spinnerMonth != null) spinnerMonth.setSelection(0); // January
        if (spinnerYear != null) spinnerYear.setSelection(20); // Year 2000
    }

    private void loadUserByPhone(String phone) {
        android.util.Log.d("RegisterUserFragment", "Searching for user with phone: " + phone);
        
        // التأكد من أن رقم الهاتف لا يبدأ بصفر
        final String searchPhone = phone.startsWith("0") ? phone.substring(1) : phone;
        
        new Thread(() -> {
            try {
                DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData(COLLECTION_USERS, null, User.class);
                
                android.util.Log.d("RegisterUserFragment", "Data result: " + (res != null ? "success=" + res.success : "null"));
                
                if (res != null && res.success && res.data != null) {
                    android.util.Log.d("RegisterUserFragment", "Found " + res.data.size() + " users");
                    
                    boolean found = false;
                    for (User user : res.data) {
                        String userPhone = user.getPhone();
                        android.util.Log.d("RegisterUserFragment", "Checking user: " + userPhone + " vs " + phone);
                        
                        // البحث عن تطابق مع أو بدون الصفر الأول
                        if (phone.equals(userPhone) || searchPhone.equals(userPhone)) {
                            android.util.Log.d("RegisterUserFragment", "Found matching user: " + user.getName());
                            // تم العثور على المستخدم
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> populateUserData(user));
                            }
                            found = true;
                            break; // إنهاء البحث بعد العثور على المستخدم
                        }
                    }
                    
                    if (!found) {
                        android.util.Log.d("RegisterUserFragment", "No matching user found - user may not be registered");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "لم يتم العثور على بياناتك. يرجى التسجيل مرة أخرى.", Toast.LENGTH_LONG).show();
                                // العودة للبروفايل
                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
                            });
                        }
                    }
                } else {
                    android.util.Log.d("RegisterUserFragment", "No data found or request failed");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "فشل الاتصال بالخادم. يرجى المحاولة مرة أخرى.", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("RegisterUserFragment", "Error loading user data", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "حدث خطأ أثناء تحميل البيانات.", Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void loadUserById(String userId) {
        // TODO: Load user by ID
    }

    private void populateUserData(User user) {
        android.util.Log.d("RegisterUserFragment", "Populating user data for: " + user.getName());
        
        if (inputFullName != null && user.getName() != null) {
            inputFullName.setText(user.getName());
            android.util.Log.d("RegisterUserFragment", "Set name: " + user.getName());
        }
        if (user.getDateOfBirth() != null) {
            setDateFromSpinner(user.getDateOfBirth());
            android.util.Log.d("RegisterUserFragment", "Set DOB: " + user.getDateOfBirth());
        }
        if (inputIdNumber != null && user.getIdNumber() != null) {
            inputIdNumber.setText(user.getIdNumber());
            android.util.Log.d("RegisterUserFragment", "Set ID: " + user.getIdNumber());
        }
        if (inputPhone != null && user.getPhone() != null) {
            inputPhone.setText(user.getPhone());
            android.util.Log.d("RegisterUserFragment", "Set phone: " + user.getPhone());
        }
        if (inputEmail != null && user.getEmail() != null) {
            inputEmail.setText(user.getEmail());
            android.util.Log.d("RegisterUserFragment", "Set email: " + user.getEmail());
        }
        // TODO: Load user photo if available
        
        android.util.Log.d("RegisterUserFragment", "User data populated successfully");
    }

    private void submit() {
        String fullName = getText(inputFullName);
        String idNumber = getText(inputIdNumber);
        String phone = getText(inputPhone);
        String email = getText(inputEmail);
        String dob = getSelectedDate();
        String password = getText(inputPassword);
        String confirm = getText(inputConfirmPassword);

        android.util.Log.d("RegisterUserFragment", "Submitting form - Name: " + fullName + ", DOB: " + dob + ", Phone: " + phone);

        // التحقق من المدخلات
        if (fullName == null || fullName.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_full_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_phone_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من صحة رقم الهاتف
        if (!phone.startsWith("05")) {
            Toast.makeText(requireContext(), "رقم الهاتف يجب أن يبدأ ب 05", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() != 10) {
            Toast.makeText(requireContext(), "رقم الهاتف يجب أن يكون 10 أرقام", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dob == null || dob.trim().isEmpty()) {
            Toast.makeText(requireContext(), "تاريخ الميلاد مطلوب", Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من عمر المستخدم (18 سنة فأكثر)
        try {
            String[] dobParts = dob.split("-");
            int birthYear = Integer.parseInt(dobParts[0]);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            int age = currentYear - birthYear;
            
            if (age < 18) {
                Toast.makeText(requireContext(), "يجب أن يكون عمر المستخدم 18 سنة فأكثر", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "تاريخ الميلاد غير صحيح", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idNumber == null || idNumber.trim().isEmpty()) {
            Toast.makeText(requireContext(), "رقم الهوية مطلوب", Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من صحة البريد الإلكتروني
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(requireContext(), "البريد الإلكتروني مطلوب", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), "البريد الإلكتروني غير صحيح", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && (password == null || password.isEmpty())) {
            // في وضع التعديل، إذا كانت كلمة المرور فارغة، لا نحدثها
            password = null;
        } else if (!isEditMode && (password == null || password.isEmpty())) {
            Toast.makeText(requireContext(), R.string.error_password_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password != null && password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(requireContext(), R.string.error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        if (isEditMode) {
            // في وضع التعديل، نحافظ على نفس الـ ID ونبحث عن المستخدم الحالي
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
            String currentPhone = prefs.getString("user_phone", "");
            String currentPassword = prefs.getString("user_password", ""); // استرجاع كلمة المرور الحالية
            user.setPhone(currentPhone);
            // تعيين userId مطلوب من Appwrite
            user.setUserId(currentPhone); // استخدام رقم الهاتف كـ userId
            user.setPassword(currentPassword); // تعيين كلمة المرور الحالية (مطلوبة من Appwrite)
            // TODO: Load existing user data and update only the changed fields
        } else {
            String userId = UUID.randomUUID().toString();
            user.setId(userId);
            user.setUserId(userId); // تعيين userId مطلوب من Appwrite
            user.setPassword(password);
        }
        
        user.setName(fullName.trim());
        user.setDateOfBirth(dob.trim());
        user.setIdNumber(idNumber.trim());
        user.setPhotoUrl(photoFilePath != null ? photoFilePath : "");
        user.setPhone(phone.trim());
        user.setEmail(email.trim()); // Use custom email input

        btnRegister.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            
            if (isEditMode) {
                // تحديث بيانات المستخدم الموجود - نستخدم رقم الهاتف الأصلي
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
                String originalPhone = prefs.getString("user_phone", "");
                DALAppWriteConnection.OperationResult<User> updateRes = dal.updateData(user, COLLECTION_USERS, originalPhone, COLLECTION_USERS);
                
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (btnRegister != null) {
                        btnRegister.setEnabled(true);
                    }
                    if (updateRes != null && updateRes.success) {
                        Toast.makeText(requireContext(), "تم تحديث البيانات بنجاح", Toast.LENGTH_SHORT).show();
                        // تحديث بيانات SharedPreferences
                        ProfileFragment.saveUserInfo(requireContext(), 
                            user.getName(), 
                            user.getPhone() + "@medicines.app", 
                            user.getPhone());
                        // العودة للشاشة السابقة
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.error_no_internet, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // حفظ مستخدم جديد
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.saveData(user, COLLECTION_USERS, null);
                
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (btnRegister != null) {
                        btnRegister.setEnabled(true);
                    }
                    if (res != null && res.success) {
                        // حفظ بيانات المستخدم في SharedPreferences فقط للتسجيل الجديد
                        ProfileFragment.saveUserInfo(requireContext(), 
                            user.getName(), 
                            user.getPhone() + "@medicines.app", 
                            user.getPhone());
                        
                        Toast.makeText(requireContext(), getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                        // العودة للشاشة السابقة
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.error_no_internet, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
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

    private String getText(TextInputEditText ed) {
        return ed.getText() != null ? ed.getText().toString() : "";
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
