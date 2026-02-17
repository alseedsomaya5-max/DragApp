package com.example.dragapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.R;
import com.example.dragapp.model.Patient;
import com.google.android.material.button.MaterialButton;

public class PatientDetailFragment extends Fragment {

    private static final String ARG_PATIENT = "patient";

    private Patient patient;
    private LinearLayout medicationsContainer;
    private TextView emptyMedications;
    private MaterialButton btnAddMedication;

    public static PatientDetailFragment newInstance(Patient patient) {
        PatientDetailFragment f = new PatientDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PATIENT, patient);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patient = (Patient) getArguments().getSerializable(ARG_PATIENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        medicationsContainer = view.findViewById(R.id.medications_container);
        emptyMedications = view.findViewById(R.id.empty_medications);
        btnAddMedication = view.findViewById(R.id.btn_add_medication);

        btnAddMedication.setOnClickListener(v -> {
            if (getActivity() instanceof HomeFragment.NavListener && patient != null) {
                ((HomeFragment.NavListener) getActivity()).openAddMedication(patient);
            }
        });

        emptyMedications.setVisibility(View.VISIBLE);
    }
}
