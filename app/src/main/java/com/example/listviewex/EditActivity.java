package com.example.listviewex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDeadline;
    private CheckBox cbStatus;
    private Button btnEdit;
    private Date selectedDeadline;
    private ToDo originalToDo;
    private int originalPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add); // Sử dụng lại layout activity_add

        // 1. Lấy dữ liệu task và vị trí
        Intent intent = getIntent();
        originalToDo = (ToDo) intent.getSerializableExtra(MainActivity.KEY_TODO_ITEM);
        originalPosition = intent.getIntExtra(MainActivity.KEY_TODO_POSITION, -1);

        if (originalToDo == null || originalPosition == -1) {
            Toast.makeText(this, "Lỗi tải dữ liệu task.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Ánh xạ View
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDeadline = findViewById(R.id.etDeadline);
        btnEdit = findViewById(R.id.btnSave); // Sử dụng lại ID btnSave
        cbStatus = findViewById(R.id.cbStatus);

        // 3. Tải dữ liệu lên Form và cấu hình UI
        loadTaskData(originalToDo);

        // 4. Xử lý chọn ngày (DatePickerDialog)
        etDeadline.setOnClickListener(v -> showDatePickerDialog());

        // 5. Xử lý sự kiện Sửa (Edit)
        btnEdit.setOnClickListener(v -> handleEdit());
    }

    private void loadTaskData(ToDo task) {
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());

        // Hiện Checkbox Status và gán trạng thái
        cbStatus.setVisibility(View.VISIBLE);
        cbStatus.setChecked(task.isChecked());
        cbStatus.setText("Hoàn thành (Done)");

        selectedDeadline = task.getDeadline();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDeadline.setText(sdf.format(selectedDeadline));

        // Đổi text button từ Save thành Edit
        btnEdit.setText("SỬA CÔNG VIỆC");
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

    private void handleEdit() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isDone = cbStatus.isChecked();

        if (title.isEmpty() || selectedDeadline == null) {
            Toast.makeText(this, "Vui lòng nhập Tiêu đề và chọn Ngày hết hạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo đối tượng ToDo Đã Cập Nhật
        ToDo updatedToDo = new ToDo(
                originalToDo.getOrder(), // Giữ nguyên Order (ví dụ: #1)
                title,
                description,
                selectedDeadline,
                isDone
        );

        // 2. Gửi đối tượng đã cập nhật VÀ vị trí về MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.KEY_TODO_ITEM, updatedToDo);
        resultIntent.putExtra(MainActivity.KEY_TODO_POSITION, originalPosition); // Gửi vị trí

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}