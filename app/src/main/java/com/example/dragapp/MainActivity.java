package com.example.dragapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dragapp.alarm.NotificationHelper;
import com.example.dragapp.model.User;
import com.example.dragapp.ui.AddMedicationFragment;
import com.example.dragapp.ui.AddPatientFragment;
import com.example.dragapp.ui.HomeFragment;
import com.example.dragapp.ui.LoginFragment;
import com.example.dragapp.ui.MoreFragment;
import com.example.dragapp.ui.PatientDetailFragment;
import com.example.dragapp.ui.ProfileFragment;
import com.example.dragapp.ui.RegisterEmployeeFragment;
import com.example.dragapp.ui.RegisterUserFragment;
import com.example.dragapp.ui.RemindersFragment;
import com.example.dragapp.ui.CustomAppHeader;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements HomeFragment.NavListener, MoreFragment.Callbacks, ProfileFragment.ProfileListener, LoginFragment.LoginListener {

    private CustomAppHeader customHeader;
    private BottomNavigationView bottomNav;
    private int currentNavId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // تفعيل edge-to-edge display بعد إعداد setContentView
        setupEdgeToEdge();
        
        NotificationHelper.createChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        customHeader = findViewById(R.id.custom_header);
        customHeader.setBackButtonListener(v -> onBackPressed());

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
            } else if (id == R.id.nav_profile) {
                clearStackAndShow(new ProfileFragment(), getString(R.string.profile_title));
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
        customHeader.setTitle(title);
        updateToolbar();
    }

    private void updateToolbar() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_host);
        boolean showBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        customHeader.showBackButton(showBack);
        
        if (f instanceof HomeFragment) {
            customHeader.setTitle(getString(R.string.page_home_title));
            customHeader.setSubtitle(getString(R.string.home_subtitle));
        } else if (f instanceof RemindersFragment) {
            customHeader.setTitle(getString(R.string.all_reminders_title));
            customHeader.setSubtitle(getString(R.string.reminders_subtitle));
        } else if (f instanceof ProfileFragment) {
            customHeader.setTitle(getString(R.string.profile_title));
            customHeader.setSubtitle(getString(R.string.profile_subtitle));
        } else if (f instanceof LoginFragment) {
            customHeader.setTitle("تسجيل الدخول");
            customHeader.setSubtitle("أدخل بياناتك للدخول");
        } else if (f instanceof MoreFragment) {
            customHeader.setTitle(getString(R.string.more_title));
            customHeader.setSubtitle(getString(R.string.more_subtitle));
        } else if (f instanceof PatientDetailFragment && f.getArguments() != null) {
            User p = (User) f.getArguments().getSerializable("patient");
            String title = p != null && p.getName() != null ? p.getName() : getString(R.string.page_patient_title);
            customHeader.setTitle(title);
            customHeader.setSubtitle(getString(R.string.medications));
        } else if (f instanceof AddPatientFragment) {
            customHeader.setTitle(getString(R.string.add_patient));
            customHeader.setSubtitle(getString(R.string.add_patient_subtitle));
        } else if (f instanceof AddMedicationFragment) {
            customHeader.setTitle(getString(R.string.add_medication));
            customHeader.setSubtitle(getString(R.string.add_medication_subtitle));
        } else if (f instanceof RegisterUserFragment) {
            customHeader.setTitle(getString(R.string.register_user));
            customHeader.setSubtitle(getString(R.string.register_user_subtitle));
        } else if (f instanceof RegisterEmployeeFragment) {
            customHeader.setTitle(getString(R.string.register_employee));
            customHeader.setSubtitle(getString(R.string.register_employee_subtitle));
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
    public void openPatientDetail(User patient) {
        String title = patient.getName() != null ? patient.getName() : getString(R.string.page_patient_title);
        replaceFragment(PatientDetailFragment.newInstance(patient), true, title);
    }

    @Override
    public void openAddPatient() {
        replaceFragment(new AddPatientFragment(), true, getString(R.string.add_patient));
    }

    @Override
    public void openAddMedication(User patient) {
        replaceFragment(AddMedicationFragment.newInstance(patient), true, getString(R.string.add_medication));
    }

    @Override
    public void onLoginRequested() {
        replaceFragment(new LoginFragment(), true, "تسجيل الدخول");
    }

    @Override
    public void onRegisterRequested() {
        replaceFragment(new RegisterUserFragment(), true, getString(R.string.register_user));
    }

    @Override
    public void onEditProfileRequested() {
        // استخدم وضع التعديل مع بيانات المستخدم الحالي
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "");
        replaceFragment(RegisterUserFragment.newInstanceForEdit(userPhone), true, getString(R.string.edit_profile));
    }

    private void replaceFragment(@NonNull Fragment fragment, boolean addToBackStack, String title) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.nav_host, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
        customHeader.setTitle(title);
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

    private void setupEdgeToEdge() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
                // استخدم post للتأكد من أن الـ Window جاهز
                getWindow().getDecorView().post(() -> {
                    try {
                        WindowInsetsController controller = getWindow().getInsetsController();
                        if (controller != null) {
                            controller.hide(WindowInsets.Type.statusBars());
                            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        }
                    } catch (Exception e) {
                        // في حالة الخطأ، استخدم الطريقة القديمة
                        fallbackToLegacyMode();
                    }
                });
            } else {
                fallbackToLegacyMode();
            }
        } catch (Exception e) {
            // في حالة أي خطأ، استخدم الوضع الافتراضي
            fallbackToLegacyMode();
        }
    }

    private void fallbackToLegacyMode() {
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }
}
