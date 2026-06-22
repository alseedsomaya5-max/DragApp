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

public class AddPatientFragment extends Fragment {

    private TextInputEditText nameInput;
    private TextInputEditText dobInput;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameInput = view.findViewById(R.id.patient_name_input);
        dobInput = view.findViewById(R.id.patient_dob_input);
        btnSave = view.findViewById(R.id.btn_save_patient);

        dobInput.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> savePatient());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    dobInput.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    public static final String REQUEST_KEY_ADD_PATIENT = "add_patient";
    public static final String KEY_PATIENT = "patient";

    /** اسم المجموعة (Collection) في Appwrite للمرضى */
    public static final String PATIENTS_COLLECTION = "patients";

    private void savePatient() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String dob = dobInput.getText() != null ? dobInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "يرجى إدخال اسم المريض", Toast.LENGTH_SHORT).show();
            return;
        }

        User patient = new User();
        String uuid = UUID.randomUUID().toString();
        patient.setId(uuid);
        patient.setUserId(uuid); // حقل أساسي لـ Appwrite
        patient.setName(name);
        patient.setTitle(name); // تعيين حقل title المطلوب من قبل Appwrite
        patient.setDateOfBirth(dob);
        
        // تعيين قيم افتراضية للحقول المطلوبة لتجنب أخطاء السيرفر
        patient.setPhone("0500000000");
        patient.setEmail(uuid + "@patients.app");
        patient.setPassword("no-password");

        btnSave.setEnabled(false);

        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.saveData(patient, PATIENTS_COLLECTION, null);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnSave.setEnabled(true);
                if (res != null && res.success) {
                    // Success
                    Bundle result = new Bundle();
                    result.putSerializable(KEY_PATIENT, patient);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY_ADD_PATIENT, result);
                    
                    Toast.makeText(requireContext(), "تم إضافة المريض بنجاح", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    final String errorDetail = (res != null && res.message != null) ? res.message : "فشل الاتصال";
                    final String fullError = (res != null) ? "ErrorCode: " + res.errorCode + "\nMsg: " + res.message : "No response";

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("فشل إضافة المريض")
                        .setMessage("حدث خطأ أثناء حفظ بيانات المريض.\n\nالسبب: " + errorDetail)
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
}
