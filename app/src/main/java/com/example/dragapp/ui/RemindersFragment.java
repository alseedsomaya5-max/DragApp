package com.example.dragapp.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.dragapp.DALAppWriteConnection;
import com.example.dragapp.MainActivity;
import com.example.dragapp.R;
import com.example.dragapp.model.Medication;
import com.example.dragapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RemindersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private final List<User> patients = new ArrayList<>();
    private final List<Medication> medications = new ArrayList<>();
    private MedicationAdapter adapter;

    /** نفس اسم المجموعة المستخدم في AddMedicationFragment */
    private static final String MEDICATIONS_COLLECTION = "medications";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.reminders_recycler_view);
        emptyText = view.findViewById(R.id.empty_reminders);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_reminder);

        adapter = new MedicationAdapter(medications);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            if (patients.isEmpty()) {
                Toast.makeText(getContext(), "لا يوجد مرضى مضافون. يرجى إضافة مريض أولاً.", Toast.LENGTH_LONG).show();
            } else {
                showPatientPickerDialog();
            }
        });
        
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            
            // جلب المرضى
            DALAppWriteConnection.OperationResult<ArrayList<User>> patientRes = dal.getData("patients", null, User.class);
            if (patientRes != null && patientRes.success && patientRes.data != null) {
                patients.clear();
                patients.addAll(patientRes.data);
            }

            // جلب المنبهات
            DALAppWriteConnection.OperationResult<ArrayList<Medication>> medRes = dal.getData(MEDICATIONS_COLLECTION, null, Medication.class);
            
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (medRes != null && medRes.success && medRes.data != null) {
                    medications.clear();
                    medications.addAll(medRes.data);
                    adapter.notifyDataSetChanged();
                }
                updateEmptyState();
            });
        }).start();
    }

    private void deleteMedication(Medication med) {
        if (med.getId() == null) return;

        new Thread(() -> {
            DALAppWriteConnection dal = new DALAppWriteConnection(requireContext());
            DALAppWriteConnection.OperationResult<Void> res = dal.deleteData(MEDICATIONS_COLLECTION, med.getId(), null);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (res != null && res.success) {
                    Toast.makeText(getContext(), "تم حذف المنبه بنجاح", Toast.LENGTH_SHORT).show();
                    medications.remove(med);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(getContext(), "فشل الحذف: " + (res != null ? res.message : ""), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void showPatientPickerDialog() {
        String[] names = new String[patients.size()];
        for (int i = 0; i < patients.size(); i++) {
            names[i] = patients.get(i).getName() != null ? patients.get(i).getName() : "بدون اسم";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("اختر المريض لإضافة منبه له")
                .setItems(names, (dialog, which) -> {
                    User selectedPatient = patients.get(which);
                    if (getActivity() instanceof HomeFragment.NavListener) {
                        ((HomeFragment.NavListener) getActivity()).openAddMedication(selectedPatient);
                    }
                })
                .show();
    }

    private void updateEmptyState() {
        if (medications.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
        private final List<Medication> list;

        MedicationAdapter(List<Medication> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Medication med = list.get(position);
            holder.nameText.setText(med.getName());
            holder.infoText.setText("الجرعة: " + med.getDosage() + " | الوقت: " + med.getTime());

            if (med.getPhotoUrl() != null && !med.getPhotoUrl().isEmpty()) {
                // إرسال مفاتيح الأمان في الهيدر لضمان فتح الصور المحمية
                GlideUrl glideUrl = new GlideUrl(med.getPhotoUrl(), new LazyHeaders.Builder()
                        .addHeader("X-Appwrite-Project", DALAppWriteConnection.PROJECT_ID)
                        .addHeader("X-Appwrite-Key", DALAppWriteConnection.API_KEY)
                        .build());

                Glide.with(holder.itemView.getContext())
                        .load(glideUrl)
                        .placeholder(android.R.drawable.ic_menu_camera)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(holder.photoView);
            } else {
                holder.photoView.setImageResource(android.R.drawable.ic_menu_camera);
            }

            holder.btnEdit.setOnClickListener(v -> {
                // العثور على المريض المرتبط بهذا الدواء (اختياري، أو تمرير null إذا كان AddMedicationFragment يدعم ذلك)
                User patient = null;
                for (User p : patients) {
                    if (p.getId() != null && p.getId().equals(med.getPatientId())) {
                        patient = p;
                        break;
                    }
                }
                if (getActivity() instanceof MainActivity && patient != null) {
                    ((MainActivity) getActivity()).openEditMedication(patient, med);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("حذف المنبه")
                        .setMessage("هل أنت متأكد من حذف منبه دواء " + med.getName() + "؟")
                        .setPositiveButton("حذف", (dialog, which) -> deleteMedication(med))
                        .setNegativeButton("إلغاء", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, infoText;
            ImageView photoView;
            View btnEdit, btnDelete;

            ViewHolder(View v) {
                super(v);
                nameText = v.findViewById(R.id.item_medication_name);
                infoText = v.findViewById(R.id.item_medication_dosage_time);
                photoView = v.findViewById(R.id.item_medication_photo);
                btnEdit = v.findViewById(R.id.btn_edit_medication);
                btnDelete = v.findViewById(R.id.btn_delete_medication);
            }
        }
    }
}
