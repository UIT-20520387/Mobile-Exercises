package com.example.listviewex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDeadline;
    private CheckBox cbStatus;
    private Button btnEdit;
    private ToDo originalToDo;
    private int originalPosition;
    private ImageButton btnSelectContact;
    private TextView tvSelectedContact;

    private ToDo currentToDo; // Đối tượng ToDo cũ
    private int taskIndex; // Vị trí Task trong danh sách
    private Date selectedDeadline;
    private String currentContactInfo = "";

    private ActivityResultLauncher<Intent> contactLauncher;

    public static final String KEY_TASK_INDEX = "task_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add); // Sử dụng lại layout activity_add

        // Ánh xạ View
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDeadline = findViewById(R.id.etDeadline);
        btnEdit = findViewById(R.id.btnSave); // Sử dụng lại ID btnSave
        cbStatus = findViewById(R.id.cbStatus);
        tvSelectedContact = findViewById(R.id.tvSelectedContact);
        btnSelectContact = findViewById(R.id.btnSelectContact);

        cbStatus.setVisibility(View.VISIBLE);
        // Đổi text button từ Save thành Edit
        btnEdit.setText("EDIT");

        // Nhận dữ liệu cũ từ MainActivity
        Intent intent = getIntent();
        currentToDo = (ToDo) intent.getSerializableExtra(MainActivity.KEY_TODO_ITEM);
        taskIndex = intent.getIntExtra(KEY_TASK_INDEX, -1);

        // Tải dữ liệu lên Form
        loadOldData();

        // Cấu hình Launcher và Event
        setupContactLauncher();

        // Xử lý chọn ngày (DatePickerDialog)
        etDeadline.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện chọn Contact
        btnSelectContact.setOnClickListener(v -> checkPermissionAndOpenContactList());

        // Xử lý sự kiện Sửa (Edit)
        btnEdit.setOnClickListener(v -> handleEdit());
    }

    private void loadTaskData(ToDo task) {
        if (currentToDo != null) {
            etTitle.setText(currentToDo.getTitle());
            etDescription.setText(currentToDo.getDescription());
            cbStatus.setChecked(currentToDo.isChecked());
            selectedDeadline = currentToDo.getDeadline();

            // Xử lý Contact Info
            currentContactInfo = currentToDo.getContactInfo();
            if (currentContactInfo != null && !currentContactInfo.isEmpty()) {
                tvSelectedContact.setText(currentContactInfo);
            } else {
                tvSelectedContact.setText("None");
            }

            // Định dạng và hiển thị ngày tháng
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDeadline.setText(sdf.format(selectedDeadline));
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        c.setTime(selectedDeadline); // Bắt đầu từ ngày hiện tại của task

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year1, monthOfYear, dayOfMonth);
                    selectedDeadline = newDate.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDeadline.setText(sdf.format(selectedDeadline));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadOldData() {
        if (currentToDo != null) {
            etTitle.setText(currentToDo.getTitle());
            etDescription.setText(currentToDo.getDescription());
            cbStatus.setChecked(currentToDo.isChecked());
            selectedDeadline = currentToDo.getDeadline();

            // Xử lý Contact Info
            currentContactInfo = currentToDo.getContactInfo();
            if (currentContactInfo != null && !currentContactInfo.isEmpty()) {
                tvSelectedContact.setText(currentContactInfo);
            } else {
                tvSelectedContact.setText("None");
            }

            // Định dạng và hiển thị ngày tháng
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDeadline.setText(sdf.format(selectedDeadline));
        }
    }

    // --- LOGIC DANH BẠ (Giống hệt AddActivity) ---

    private void setupContactLauncher() {
        contactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Lấy chuỗi "SĐT - Tên" được trả về
                        String contact = result.getData().getStringExtra("SELECTED_CONTACT");
                        currentContactInfo = contact; // Cập nhật biến lưu trữ
                        tvSelectedContact.setText(contact); // Hiển thị lên UI
                    }
                }
        );
    }

    private void checkPermissionAndOpenContactList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            Intent intent = new Intent(EditActivity.this, ContactListActivity.class);
            contactLauncher.launch(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(EditActivity.this, ContactListActivity.class);
            contactLauncher.launch(intent);
        }
    }

    private void handleEdit() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isDone = cbStatus.isChecked();

        if (title.isEmpty() || selectedDeadline == null) {
            Toast.makeText(this, "Vui lòng nhập Tiêu đề và chọn Ngày hết hạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng ToDo Đã Cập Nhật
        ToDo updatedToDo = new ToDo(
                currentToDo.getOrder(), // Giữ nguyên Order (ví dụ: #1)
                title,
                description,
                selectedDeadline,
                isDone,
                currentContactInfo
        );

        // 2. Gửi đối tượng đã cập nhật VÀ vị trí về MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.KEY_TODO_ITEM, updatedToDo);
        resultIntent.putExtra(KEY_TASK_INDEX, taskIndex);
//        resultIntent.putExtra(MainActivity.KEY_TODO_POSITION, originalPosition); // Gửi vị trí

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}