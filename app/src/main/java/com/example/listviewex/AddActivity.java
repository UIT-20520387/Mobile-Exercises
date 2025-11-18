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

public class AddActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDeadline;
    private CheckBox cbStatus;
    private Button btnSave;
    private Date selectedDeadline; // Biến lưu trữ đối tượng Date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add); // Giả định layout là activity_add.xml

        // Ánh xạ View
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDeadline = findViewById(R.id.etDeadline);
        btnSave = findViewById(R.id.btnSave);
        cbStatus = findViewById(R.id.cbStatus);

        // Theo yêu cầu: Khi thêm mới luôn là Progress.
        cbStatus.setVisibility(View.GONE);

        // Xử lý chọn ngày (DatePickerDialog)
        etDeadline.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện Lưu
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Lưu Date đã chọn vào Calendar
                        c.set(year, monthOfYear, dayOfMonth);
                        selectedDeadline = c.getTime(); // Lưu đối tượng Date

                        // Hiển thị ngày đã chọn theo định dạng dd/MM/yyyy
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDeadline.setText(sdf.format(selectedDeadline));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void handleSave() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || selectedDeadline == null) {
            Toast.makeText(this, "Vui lòng nhập Tiêu đề và chọn Ngày hết hạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng ToDo mới
        // Lưu ý: Order và Description không có trong Model hiện tại, nên tôi sẽ dùng Title và bỏ qua Description
        // Status mặc định là Progress (false)
        ToDo newToDo = new ToDo(
                "", // Order sẽ được gán lại trong MainActivity
                title,
                description,
                selectedDeadline,
                false // Mặc định là Progress (chưa checked)
        );

        // Gửi đối tượng ToDo mới về MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.KEY_TODO_ITEM, newToDo);

        setResult(RESULT_OK, resultIntent); // Đánh dấu thành công
        finish(); // Quay lại màn hình chính
    }
}