package com.example.dragapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.R;
import com.example.dragapp.model.Patient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddMedicationFragment extends Fragment {

    private static final String ARG_PATIENT = "patient";

    private Patient patient;
    private TextInputEditText medicationNameInput;
    private TextInputEditText dosageInput;
    private TextInputEditText timeInput;
    private MaterialButton btnSave;

    public static AddMedicationFragment newInstance(Patient patient) {
        AddMedicationFragment f = new AddMedicationFragment();
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
        return inflater.inflate(R.layout.fragment_add_medication, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        medicationNameInput = view.findViewById(R.id.medication_name_input);
        dosageInput = view.findViewById(R.id.dosage_input);
        timeInput = view.findViewById(R.id.time_input);
        btnSave = view.findViewById(R.id.btn_save_medication);

        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void saveMedication() {
        String name = medicationNameInput.getText() != null ? medicationNameInput.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.medication_name, Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: persist medication and schedule reminder
        Toast.makeText(requireContext(), R.string.save, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
