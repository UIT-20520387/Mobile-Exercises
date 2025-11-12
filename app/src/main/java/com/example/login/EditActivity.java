package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.example.login.MainActivity.SinhVien; // Import lớp SinhVien

public class EditActivity extends AppCompatActivity {

    private TextInputEditText inputEditMSSV, inputEditHoTen, inputEditNgaySinh, inputEditAvatarUrl;
    private Button btnSave, btnCancel;
    private SinhVien currentStudent; // Sinh viên hiện tại cần chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Ánh xạ View
        inputEditMSSV = findViewById(R.id.inputEditMSSV);
        inputEditHoTen = findViewById(R.id.inputEditHoTen);
        inputEditNgaySinh = findViewById(R.id.inputEditNgaySinh);
        inputEditAvatarUrl = findViewById(R.id.inputEditAvatarUrl);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Vô hiệu hóa chỉnh sửa cho MSSV
        inputEditMSSV.setEnabled(false);
        // Có thể thêm kiểu hiển thị disable (màu xám) cho TextInputLayout nếu cần

        // Nhận MSSV được truyền từ HomeActivity
        String editMssv = getIntent().getStringExtra("EDIT_MSSV");

        // Tải dữ liệu sinh viên
        currentStudent = findStudentByMssv(editMssv);

        if (currentStudent != null) {
            displayCurrentInfo(currentStudent);
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu để chỉnh sửa.", Toast.LENGTH_SHORT).show();
            // Trả về kết quả HỦY BỎ và đóng Activity
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        // 3. Thiết lập sự kiện
        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> handleCancel());
    }

    private void displayCurrentInfo(SinhVien sv) {
        inputEditMSSV.setText(sv.getMssv());
        inputEditHoTen.setText(sv.getName());
        inputEditNgaySinh.setText(sv.getDoB());
        inputEditAvatarUrl.setText(sv.getAvatarUrl());
    }

    private SinhVien findStudentByMssv(String mssv) {
        // Tìm sinh viên trong danh sách tĩnh của MainActivity
        for (SinhVien sv : MainActivity.sinhVienList) {
            if (sv.getMssv().equals(mssv)) {
                return sv;
            }
        }
        return null;
    }

    private void handleSave() {
        String newHoTen = inputEditHoTen.getText().toString().trim();
        String newNgaySinh = inputEditNgaySinh.getText().toString().trim();
        String newAvatarUrl = inputEditAvatarUrl.getText().toString().trim();

        if (newHoTen.isEmpty() || newNgaySinh.isEmpty()) {
            Toast.makeText(this, "Họ tên và Ngày sinh không được để trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Cập nhật dữ liệu vào đối tượng static (thay đổi tạm thời)
        currentStudent.setName(newHoTen);
        currentStudent.setDoB(newNgaySinh);
        currentStudent.setAvatarUrl(newAvatarUrl);

        // 2. Trả về kết quả OK cho HomeActivity
        setResult(Activity.RESULT_OK);

        // 3. Đóng Activity
        finish();
    }

    private void handleCancel() {
        // Trả về kết quả HỦY BỎ cho HomeActivity
        setResult(Activity.RESULT_CANCELED);
        // Đóng Activity
        finish();
    }
}