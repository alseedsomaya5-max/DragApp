package com.example.dragapp.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.Medication;
import com.example.dragapp.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMedicationFragment extends Fragment {

    private static final String ARG_PATIENT = "patient";
    private static final String ARG_MEDICATION = "medication";
    private static final String MEDICATIONS_COLLECTION = "medications";

    private User patient;
    private Medication editMedication;
    private TextInputEditText medicationNameInput;
    private TextInputEditText dosageInput;
    private TextInputEditText timeInput;
    private ImageView photoPreview;
    private MaterialButton btnSave;
    private Bitmap selectedBitmap;
    private Uri photoUri;
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(requireContext(), "يجب الموافقة على صلاحية الكاميرا لالتقاط الصورة", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isGranted()) {
                    if (isSuccess && currentPhotoPath != null) {
                        try {
                            selectedBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                            photoPreview.setImageBitmap(selectedBitmap);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "خطأ في عرض الصورة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                        selectedBitmap = BitmapFactory.decodeStream(inputStream);
                        photoPreview.setImageBitmap(selectedBitmap);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "خطأ في تحميل الصورة من المعرض", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean isGranted() {
        return getContext() != null;
    }

    public static AddMedicationFragment newInstance(User patient) {
        return newInstance(patient, null);
    }

    public static AddMedicationFragment newInstance(User patient, Medication medication) {
        AddMedicationFragment f = new AddMedicationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PATIENT, patient);
        if (medication != null) {
            args.putSerializable(ARG_MEDICATION, medication);
        }
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patient = (User) getArguments().getSerializable(ARG_PATIENT);
            editMedication = (Medication) getArguments().getSerializable(ARG_MEDICATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_medication, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        medicationNameInput = view.findViewById(R.id.medication_name_input);
        dosageInput = view.findViewById(R.id.dosage_input);
        timeInput = view.findViewById(R.id.time_input);
        photoPreview = view.findViewById(R.id.medication_photo);
        MaterialButton btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        btnSave = view.findViewById(R.id.btn_save_medication);

        if (editMedication != null) {
            medicationNameInput.setText(editMedication.getName());
            dosageInput.setText(editMedication.getDosage());
            timeInput.setText(editMedication.getTime());
            btnSave.setText("تعديل المنبه");
            
            if (editMedication.getPhotoUrl() != null && !editMedication.getPhotoUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(editMedication.getPhotoUrl())
                        .into(photoPreview);
            }
        }

        timeInput.setOnClickListener(v -> showTimePicker());
        btnTakePhoto.setOnClickListener(v -> showImagePickerDialog());
        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void showImagePickerDialog() {
        String[] options = {"التقاط صورة بالكاميرا", "اختيار من معرض الصور"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("صورة الدواء")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timeInput.setText(time);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);
                cameraLauncher.launch(photoUri);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "فشل فتح الكاميرا: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void saveMedication() {
        String name = medicationNameInput.getText() != null ? medicationNameInput.getText().toString().trim() : "";
        String dosage = dosageInput.getText() != null ? dosageInput.getText().toString().trim() : "";
        String time = timeInput.getText() != null ? timeInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.medication_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (patient == null) {
            Toast.makeText(requireContext(), "خطأ: لم يتم اختيار مريض", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            String photoUrl = editMedication != null ? editMedication.getPhotoUrl() : null;

            if (selectedBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] data = baos.toByteArray();
                
                DALAppWriteConnection.OperationResult<DALAppWriteConnection.FileInfo> uploadRes = 
                    dal.uploadFile(data, "med_" + System.currentTimeMillis() + ".jpg", "image/jpeg", null);
                
                if (uploadRes != null && uploadRes.success && uploadRes.data != null) {
                    photoUrl = uploadRes.data.fileUrl;
                } else {
                    final String uploadError = (uploadRes != null) ? uploadRes.message : "خطأ غير معروف في رفع الصورة";
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                .setTitle("فشل رفع الصورة")
                                .setMessage("لم نتمكن من رفع صورة الدواء.\n\nالسبب: " + uploadError)
                                .setPositiveButton("حسناً", null)
                                .show();
                        });
                    }
                    return; // توقف عن الحفظ إذا فشل الرفع وكان هناك صورة مختارة
                }
            }

            Medication med;
            if (editMedication != null) {
                med = editMedication;
                med.setName(name);
                med.setDosage(dosage);
                med.setTime(time);
            } else {
                med = new Medication(patient.getId(), name, dosage, time);
            }
            med.setPhotoUrl(photoUrl);
            
            DALAppWriteConnection.OperationResult<?> res;
            if (editMedication != null) {
                res = dal.updateData(med, MEDICATIONS_COLLECTION, med.getId(), MEDICATIONS_COLLECTION);
            } else {
                res = dal.saveData(med, MEDICATIONS_COLLECTION, null);
            }
            
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnSave.setEnabled(true);
                if (res != null && res.success) {
                    Toast.makeText(requireContext(), editMedication != null ? "تم تعديل المنبه بنجاح" : "تم حفظ المنبه بنجاح", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    final String errorDetail = (res != null && res.message != null) ? res.message : "فشل الاتصال بالسيرفر";
                    final String fullError = (res != null) ? "ErrorCode: " + res.errorCode + "\nMsg: " + res.message : "No response";

                    getActivity().runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("فشل في الحفظ")
                            .setMessage("حدث خطأ أثناء حفظ بيانات الدواء.\n\nالسبب: " + errorDetail)
                            .setNeutralButton("نسخ الخطأ", (dialog, which) -> {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Appwrite Error", fullError);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(requireContext(), "تم نسخ تفاصيل الخطأ", Toast.LENGTH_SHORT).show();
                            })
                            .setPositiveButton("حسناً", null)
                            .show();
                    });
                }
            });
        }).start();
    }
}
