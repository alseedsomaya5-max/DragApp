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
import com.example.dragapp.model.Patient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.UUID;

public class AddPatientFragment extends Fragment {

    private TextInputEditText nameInput;
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
        btnSave = view.findViewById(R.id.btn_save_patient);

        btnSave.setOnClickListener(v -> savePatient());
    }

    public static final String REQUEST_KEY_ADD_PATIENT = "add_patient";
    public static final String KEY_PATIENT = "patient";

    /** اسم المجموعة (Collection) في Appwrite للمرضى */
    public static final String PATIENTS_COLLECTION = "patients";

    private void savePatient() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.patient_name, Toast.LENGTH_SHORT).show();
            return;
        }
        Patient patient = new Patient(UUID.randomUUID().toString(), name);
        btnSave.setEnabled(false);

        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<ArrayList<Patient>> res = dal.saveData(patient, PATIENTS_COLLECTION, null);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                btnSave.setEnabled(true);
                if (res != null && res.success && res.data != null && !res.data.isEmpty()) {
                    Patient saved = res.data.get(0);
                    Bundle result = new Bundle();
                    result.putSerializable(KEY_PATIENT, saved);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY_ADD_PATIENT, result);
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(requireContext(), R.string.save, Toast.LENGTH_SHORT).show();
                } else {
                    String msg = res != null && res.message != null ? res.message : getString(R.string.save);
                    if (msg != null && (msg.contains("Unable to resolve host") || msg.contains("No address associated"))) {
                        msg = getString(R.string.error_no_internet);
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
