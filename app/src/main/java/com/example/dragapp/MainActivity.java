package com.example.dragapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dragapp.alarm.NotificationHelper;
import com.example.dragapp.model.Patient;
import com.example.dragapp.ui.AddMedicationFragment;
import com.example.dragapp.ui.AddPatientFragment;
import com.example.dragapp.ui.HomeFragment;
import com.example.dragapp.ui.MoreFragment;
import com.example.dragapp.ui.PatientDetailFragment;
import com.example.dragapp.ui.RegisterEmployeeFragment;
import com.example.dragapp.ui.RegisterUserFragment;
import com.example.dragapp.ui.RemindersFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements HomeFragment.NavListener, MoreFragment.Callbacks {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private int currentNavId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.createChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentNavId) return true;
            currentNavId = id;
            if (id == R.id.nav_home) {
                clearStackAndShow(new HomeFragment(), getString(R.string.page_home_title));
            } else if (id == R.id.nav_reminders) {
                clearStackAndShow(new RemindersFragment(), getString(R.string.all_reminders_title));
            } else if (id == R.id.nav_more) {
                clearStackAndShow(new MoreFragment(), getString(R.string.more_title));
            }
            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateToolbar);

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), false, getString(R.string.page_home_title));
        }
    }

    private void clearStackAndShow(Fragment fragment, String title) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.nav_host, fragment);
        tx.commit();
        toolbar.setTitle(title);
        updateToolbar();
    }

    private void updateToolbar() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Drawable backIcon = count > 0 ? ContextCompat.getDrawable(this, android.R.drawable.ic_menu_revert) : null;
        toolbar.setNavigationIcon(backIcon);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (f instanceof HomeFragment) {
            toolbar.setTitle(R.string.page_home_title);
        } else if (f instanceof RemindersFragment) {
            toolbar.setTitle(R.string.all_reminders_title);
        } else if (f instanceof MoreFragment) {
            toolbar.setTitle(R.string.more_title);
        } else if (f instanceof PatientDetailFragment && f.getArguments() != null) {
            Patient p = (Patient) f.getArguments().getSerializable("patient");
            toolbar.setTitle(p != null && p.getName() != null ? p.getName() : getString(R.string.page_patient_title));
        } else if (f instanceof AddPatientFragment) {
            toolbar.setTitle(R.string.add_patient);
        } else if (f instanceof AddMedicationFragment) {
            toolbar.setTitle(R.string.add_medication);
        } else if (f instanceof RegisterUserFragment) {
            toolbar.setTitle(R.string.register_user);
        } else if (f instanceof RegisterEmployeeFragment) {
            toolbar.setTitle(R.string.register_employee);
        }
    }

    @Override
    public void openRegisterUser() {
        replaceFragment(new RegisterUserFragment(), true, getString(R.string.register_user));
    }

    @Override
    public void openRegisterEmployee() {
        replaceFragment(new RegisterEmployeeFragment(), true, getString(R.string.register_employee));
    }

    @Override
    public void openPatientDetail(Patient patient) {
        String title = patient.getName() != null ? patient.getName() : getString(R.string.page_patient_title);
        replaceFragment(PatientDetailFragment.newInstance(patient), true, title);
    }

    @Override
    public void openAddPatient() {
        replaceFragment(new AddPatientFragment(), true, getString(R.string.add_patient));
    }

    @Override
    public void openAddMedication(Patient patient) {
        replaceFragment(AddMedicationFragment.newInstance(patient), true, getString(R.string.add_medication));
    }

    private void replaceFragment(@NonNull Fragment fragment, boolean addToBackStack, String title) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.nav_host, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
        toolbar.setTitle(title);
        updateToolbar();
        bottomNav.setSelectedItemId(currentNavId);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
