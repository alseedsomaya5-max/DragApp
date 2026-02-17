package com.example.dragapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.R;
import com.google.android.material.button.MaterialButton;

public class MoreFragment extends Fragment {

    public interface Callbacks {
        void openRegisterUser();
        void openRegisterEmployee();
    }

    private MaterialButton btnRegisterUser;
    private MaterialButton btnRegisterEmployee;
    private MaterialButton btnCheckAppwrite;
    private TextView connectionStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnRegisterUser = view.findViewById(R.id.btn_register_user);
        btnRegisterEmployee = view.findViewById(R.id.btn_register_employee);
        btnCheckAppwrite = view.findViewById(R.id.btn_check_appwrite);
        connectionStatus = view.findViewById(R.id.connection_status);

        btnRegisterUser.setOnClickListener(v -> {
            if (getActivity() instanceof Callbacks) {
                ((Callbacks) getActivity()).openRegisterUser();
            }
        });
        btnRegisterEmployee.setOnClickListener(v -> {
            if (getActivity() instanceof Callbacks) {
                ((Callbacks) getActivity()).openRegisterEmployee();
            }
        });
        btnCheckAppwrite.setOnClickListener(v -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            String status = dal.checkConnectionStatus();
            connectionStatus.setText(status);
        });
    }
}
