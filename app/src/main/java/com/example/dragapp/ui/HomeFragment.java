package com.example.dragapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.example.dragapp.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface NavListener {
        void openPatientDetail(User patient);
        void openAddPatient();
        void openAddMedication(User patient);
    }

    private LinearLayout patientsContainer;
    private TextView emptyPatients;
    private MaterialButton btnAddPatient;
    private final List<User> patients = new ArrayList<>();

    /** نفس اسم المجموعة المستخدم في AddPatientFragment */
    private static final String PATIENTS_COLLECTION = "patients";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patientsContainer = view.findViewById(R.id.patients_container);
        emptyPatients = view.findViewById(R.id.empty_patients);
        btnAddPatient = view.findViewById(R.id.btn_add_patient);

        btnAddPatient.setOnClickListener(v -> {
            if (getActivity() instanceof NavListener) {
                ((NavListener) getActivity()).openAddPatient();
            }
        });

        getParentFragmentManager().setFragmentResultListener(AddPatientFragment.REQUEST_KEY_ADD_PATIENT, getViewLifecycleOwner(), (key, bundle) -> {
            User p = (User) bundle.getSerializable(AddPatientFragment.KEY_PATIENT);
            addPatient(p);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPatientsFromAppwrite();
    }

    /** جلب قائمة المرضى من Appwrite في الخلفية وتحديث الواجهة */
    private void loadPatientsFromAppwrite() {
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<User>> res = dal.getData(PATIENTS_COLLECTION, null, User.class);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (res != null && res.success && res.data != null) {
                    patients.clear();
                    for (User p : res.data) {
                        patients.add(p);
                    }
                    refreshPatientList();
                }
            });
        }).start();
    }

    private void refreshPatientList() {
        patientsContainer.removeAllViews();
        if (patients.isEmpty()) {
            emptyPatients.setVisibility(View.VISIBLE);
        } else {
            emptyPatients.setVisibility(View.GONE);
            for (User p : patients) {
                View row = getLayoutInflater().inflate(R.layout.item_patient, patientsContainer, false);
                TextView text = row.findViewById(R.id.patient_name_text);
                View btnDelete = row.findViewById(R.id.btn_delete_patient);
                
                String displayName = p.getName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = p.getTitle();
                }
                if (displayName == null) displayName = "";
                text.setText(displayName);

                row.setOnClickListener(v -> {
                    if (getActivity() instanceof NavListener) {
                        ((NavListener) getActivity()).openPatientDetail(p);
                    }
                });

                final String finalName = displayName;
                btnDelete.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("حذف مريض")
                            .setMessage("هل أنت متأكد من حذف " + finalName + "؟ سيتم حذف جميع بياناته نهائياً.")
                            .setPositiveButton("حذف", (dialog, which) -> deletePatient(p))
                            .setNegativeButton("إلغاء", null)
                            .show();
                });

                patientsContainer.addView(row);
            }
        }
    }

    private void deletePatient(User patient) {
        if (patient.getId() == null || patient.getId().isEmpty()) {
            Toast.makeText(requireContext(), "خطأ: معرف المريض غير موجود", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<Void> res = dal.deleteData(PATIENTS_COLLECTION, patient.getId(), null);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (res != null && res.success) {
                    Toast.makeText(requireContext(), "تم حذف المريض بنجاح", Toast.LENGTH_SHORT).show();
                    patients.remove(patient);
                    refreshPatientList();
                } else {
                    String msg = (res != null && res.message != null) ? res.message : "فشل غير معروف";
                    android.util.Log.e("HomeFragment", "Delete failed: " + msg);
                    Toast.makeText(requireContext(), "فشل الحفظ: " + msg, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    public void addPatient(User patient) {
        if (patient != null) {
            patients.add(patient);
            refreshPatientList();
        }
    }
}
