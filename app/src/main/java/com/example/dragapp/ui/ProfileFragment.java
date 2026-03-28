package com.example.dragapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.example.dragapp.DALAppWriteConnection;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ProfileFragment extends Fragment implements LoginFragment.LoginListener {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";

    private LinearLayout loggedInLayout;
    private LinearLayout notLoggedInLayout;
    private TextView welcomeText;
    private TextView userNameText;
    private TextView userEmailText;
    private TextView userPhoneText;
    private TextView userNameDisplay;
    private TextView userEmailDisplay;
    private ImageView userProfileImage;
    private ImageView cameraIcon;
    private LinearLayout editProfileContainer;
    private LinearLayout changePasswordContainer;
    private LinearLayout logoutContainer;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialButton logoutButton;
    private MaterialButton editProfileButton;

    // Constants for image picker
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int PERMISSION_CAMERA = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        updateUIBasedOnLoginStatus();
    }

    private void initViews(View view) {
        loggedInLayout = view.findViewById(R.id.logged_in_layout);
        notLoggedInLayout = view.findViewById(R.id.not_logged_in_layout);
        userNameText = view.findViewById(R.id.user_name_text);
        userEmailText = view.findViewById(R.id.user_email_text);
        userPhoneText = view.findViewById(R.id.user_phone_text);
        userNameDisplay = view.findViewById(R.id.user_name_display);
        userEmailDisplay = view.findViewById(R.id.user_email_display);
        userProfileImage = view.findViewById(R.id.user_profile_image);
        cameraIcon = view.findViewById(R.id.camera_icon);
        editProfileContainer = view.findViewById(R.id.btn_edit_profile_container);
        changePasswordContainer = view.findViewById(R.id.btn_change_password_container);
        logoutContainer = view.findViewById(R.id.btn_logout_container);
        loginButton = view.findViewById(R.id.btn_login);
        registerButton = view.findViewById(R.id.btn_register);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            if (getActivity() instanceof ProfileListener) {
                ((ProfileListener) getActivity()).onLoginRequested();
            }
        });

        registerButton.setOnClickListener(v -> {
            if (getActivity() instanceof ProfileListener) {
                ((ProfileListener) getActivity()).onRegisterRequested();
            }
        });

        
        // New click listeners for improved UI
        editProfileContainer.setOnClickListener(v -> {
            if (getActivity() instanceof ProfileListener) {
                ((ProfileListener) getActivity()).onEditProfileRequested();
            }
        });

        changePasswordContainer.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        logoutContainer.setOnClickListener(v -> {
            logoutUser();
            updateUIBasedOnLoginStatus();
        });

        cameraIcon.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        userProfileImage.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        userEmailText.setOnClickListener(v -> {
            if (getActivity() instanceof ProfileListener) {
                ((ProfileListener) getActivity()).onEditProfileRequested();
            }
        });
    }

    private void updateUIBasedOnLoginStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            showLoggedInUI();
        } else {
            showNotLoggedInUI();
        }
    }

    private void showLoggedInUI() {
        loggedInLayout.setVisibility(View.VISIBLE);
        notLoggedInLayout.setVisibility(View.GONE);

        // Load fresh data directly from Appwrite
        loadUserDataFromAppwrite();
    }
    
    private void loadUserDataFromAppwrite() {
        // Get current user phone from SharedPreferences (only for identification)
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userPhone = prefs.getString(KEY_USER_PHONE, "");
        
        if (userPhone.isEmpty()) {
            // If no phone stored, show not logged in
            showNotLoggedInUI();
            return;
        }
        
        new Thread(() -> {
            try {
                DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData("users", null, User.class);
                
                if (res != null && res.success && res.data != null) {
                    User currentUser = null;
                    for (User user : res.data) {
                        if (userPhone.equals(user.getPhone())) {
                            currentUser = user;
                            break;
                        }
                    }
                    
                    if (currentUser != null && getActivity() != null) {
                        // Make variables effectively final for lambda
                        final User finalUser = currentUser;
                        final String finalUserPhone = userPhone;
                        
                        getActivity().runOnUiThread(() -> {
                            // Update UI directly with data from Appwrite
                            userNameText.setText(finalUser.getName());
                            userEmailText.setText(finalUser.getEmail());
                            userPhoneText.setText(finalUser.getPhone());
                            
                            // Update new UI elements
                            userNameDisplay.setText(finalUser.getName());
                            userEmailDisplay.setText(finalUser.getEmail());
                            
                            // Load user profile image if available
                            loadUserProfileImage(finalUserPhone);
                        });
                    } else {
                        // User not found in database, show not logged in
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showNotLoggedInUI();
                            });
                        }
                    }
                } else {
                    // Failed to load data, show error
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "فشل في تحميل بيانات المستخدم", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "خطأ في الاتصال بالخادم", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void showNotLoggedInUI() {
        loggedInLayout.setVisibility(View.GONE);
        notLoggedInLayout.setVisibility(View.VISIBLE);
    }

    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_PHONE);
        editor.apply();
    }

    public static void saveUserInfo(Context context, String name, String email, String phone) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    @Override
    public void onRegisterRequested() {
        if (getActivity() instanceof ProfileListener) {
            ((ProfileListener) getActivity()).onRegisterRequested();
        }
    }

    private void loadUserProfileImage(String userPhone) {
        // First, check if we have a saved image in SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedImagePath = prefs.getString("user_profile_image_path", "");
        
        if (!savedImagePath.isEmpty()) {
            try {
                // Load image from local storage
                java.io.File imgFile = new java.io.File(savedImagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(savedImagePath);
                    if (bitmap != null) {
                        userProfileImage.setImageBitmap(bitmap);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // If no local image, check Appwrite
        new Thread(() -> {
            try {
                DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData("users", null, User.class);
                
                if (res != null && res.success && res.data != null) {
                    for (User user : res.data) {
                        if (userPhone.equals(user.getPhone()) && user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            // Load image from URL or set a placeholder
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // For now, set a placeholder. In real implementation, use image loading library
                                    userProfileImage.setImageResource(android.R.drawable.ic_menu_myplaces);
                                });
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showImagePickerDialog() {
        String[] options = {"التقاط صورة", "اختيار من المعرض"};
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("تغيير الصورة الشخصية")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Camera option
                    checkCameraPermission();
                } else if (which == 1) {
                    // Gallery option
                    openGallery();
                }
            })
            .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void showChangePasswordDialog() {
        // Create custom dialog for changing password
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("تغيير كلمة المرور");
        
        // Create layout for dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        // Current password input
        android.widget.TextView currentPasswordLabel = new android.widget.TextView(requireContext());
        currentPasswordLabel.setText("كلمة المرور الحالية:");
        currentPasswordLabel.setTextSize(16);
        layout.addView(currentPasswordLabel);
        
        android.widget.EditText currentPasswordInput = new android.widget.EditText(requireContext());
        currentPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        currentPasswordInput.setHint("أدخل كلمة المرور الحالية");
        layout.addView(currentPasswordInput);
        
        // New password input
        android.widget.TextView newPasswordLabel = new android.widget.TextView(requireContext());
        newPasswordLabel.setText("كلمة المرور الجديدة:");
        newPasswordLabel.setTextSize(16);
        android.widget.LinearLayout.LayoutParams params1 = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params1.setMargins(0, 20, 0, 0);
        newPasswordLabel.setLayoutParams(params1);
        layout.addView(newPasswordLabel);
        
        android.widget.EditText newPasswordInput = new android.widget.EditText(requireContext());
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setHint("أدخل كلمة المرور الجديدة");
        layout.addView(newPasswordInput);
        
        // Confirm new password input
        android.widget.TextView confirmNewPasswordLabel = new android.widget.TextView(requireContext());
        confirmNewPasswordLabel.setText("تأكيد كلمة المرور الجديدة:");
        confirmNewPasswordLabel.setTextSize(16);
        android.widget.LinearLayout.LayoutParams params2 = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params2.setMargins(0, 20, 0, 0);
        confirmNewPasswordLabel.setLayoutParams(params2);
        layout.addView(confirmNewPasswordLabel);
        
        android.widget.EditText confirmNewPasswordInput = new android.widget.EditText(requireContext());
        confirmNewPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmNewPasswordInput.setHint("أعد إدخال كلمة المرور الجديدة");
        layout.addView(confirmNewPasswordInput);
        
        builder.setView(layout);
        
        // Set buttons
        builder.setPositiveButton("تغيير", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();
            
            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(requireContext(), "جميع الحقول مطلوبة", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(requireContext(), "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(requireContext(), "كلمات المرور الجديدة غير متطابقة", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Change password
            changePassword(currentPassword, newPassword);
        });
        
        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void changePassword(String currentPassword, String newPassword) {
        // Show loading
        Toast.makeText(requireContext(), "جاري تغيير كلمة المرور...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // Get current user data
                SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String userPhone = prefs.getString(KEY_USER_PHONE, "");
                
                if (userPhone.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "لم يتم العثور على بيانات المستخدم", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }
                
                // Verify current password and update
                DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
                DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData("users", null, User.class);
                
                if (res != null && res.success && res.data != null) {
                    User currentUser = null;
                    for (User user : res.data) {
                        if (userPhone.equals(user.getPhone())) {
                            currentUser = user;
                            break;
                        }
                    }
                    
                    if (currentUser != null) {
                        // Verify current password
                        if (currentPassword.equals(currentUser.getPassword())) {
                            // Update password
                            currentUser.setPassword(newPassword);
                            
                            // Save to Appwrite
                            DALAppWriteConnection.OperationResult<User> updateRes = dal.updateData(currentUser, "users", currentUser.getPhone(), "users");
                            
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (updateRes != null && updateRes.success) {
                                        // Update saved password in SharedPreferences
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("user_password", newPassword);
                                        editor.apply();
                                        
                                        Toast.makeText(requireContext(), "تم تغيير كلمة المرور بنجاح", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(requireContext(), "فشل في تغيير كلمة المرور", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "كلمة المرور الحالية غير صحيحة", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "لم يتم العثور على المستخدم", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "فشل في الاتصال بالخادم", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "يجب السماح بالوصول للكاميرا", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    userProfileImage.setImageBitmap(imageBitmap);
                    saveUserProfileImage(imageBitmap);
                }
            } else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        userProfileImage.setImageBitmap(bitmap);
                        saveUserProfileImage(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveUserProfileImage(Bitmap bitmap) {
        // Save image to local storage and update SharedPreferences
        new Thread(() -> {
            try {
                // Create a directory for user images if it doesn't exist
                java.io.File directory = new java.io.File(requireContext().getFilesDir(), "profile_images");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                
                // Create a unique file name
                String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
                java.io.File imageFile = new java.io.File(directory, fileName);
                
                // Save bitmap to file
                java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                
                // Save the file path to SharedPreferences
                SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_profile_image_path", imageFile.getAbsolutePath());
                editor.apply();
                
                // Also update the user in Appwrite if needed
                updateUserProfileImageInAppwrite(imageFile.getAbsolutePath());
                
                // Show success message on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "تم تحديث الصورة الشخصية", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "حدث خطأ عند حفظ الصورة", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }
    
    private void updateUserProfileImageInAppwrite(String imagePath) {
        // This would upload the image to Appwrite and update the user's photoUrl
        // For now, we'll just save locally
        // In a real implementation, you would upload the file to Appwrite storage
        // and update the user document with the new photoUrl
    }

    public interface ProfileListener {
        void onLoginRequested();
        void onRegisterRequested();
        void onEditProfileRequested();
    }
}
