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
import com.example.dragapp.model.Patient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface NavListener {
        void openPatientDetail(Patient patient);
        void openAddPatient();
        void openAddMedication(Patient patient);
    }

    private LinearLayout patientsContainer;
    private TextView emptyPatients;
    private MaterialButton btnAddPatient;
    private final List<Patient> patients = new ArrayList<>();

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
            Patient p = (Patient) bundle.getSerializable(AddPatientFragment.KEY_PATIENT);
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
            DALAppWriteConnection.OperationResult<ArrayList<Patient>> res = dal.getData(PATIENTS_COLLECTION, null, Patient.class);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (res != null && res.success && res.data != null) {
                    patients.clear();
                    patients.addAll(res.data);
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
            for (Patient p : patients) {
                View row = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, patientsContainer, false);
                TextView text = row.findViewById(android.R.id.text1);
                text.setText(p.getName() != null ? p.getName() : "");
                row.setOnClickListener(v -> {
                    if (getActivity() instanceof NavListener) {
                        ((NavListener) getActivity()).openPatientDetail(p);
                    }
                });
                patientsContainer.addView(row);
            }
        }
    }

    public void addPatient(Patient patient) {
        if (patient != null) {
            patients.add(patient);
            refreshPatientList();
        }
    }
}
