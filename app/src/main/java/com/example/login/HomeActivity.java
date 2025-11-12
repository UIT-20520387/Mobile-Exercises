package com.example.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import android.widget.ImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.login.MainActivity.SinhVien;

public class HomeActivity extends AppCompatActivity {
    private TextView tvHoTen, tvMSSV, tvNgaySinh;
    private ImageView imageViewAvatar;
    private Button btnLogout, btnEditProfile;
    private String loggedInMssv;

    // Sử dụng ActivityResultLauncher để xử lý kết quả từ EditActivity
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ
        tvHoTen = findViewById(R.id.tvHoTen);
        tvMSSV = findViewById(R.id.tvMSSV);
        tvNgaySinh = findViewById(R.id.tvNgaySinh);
        // imageViewAvatar = findViewById(R.id.imageViewAvatar);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);

        // Lấy MSSV từ Intent (được truyền từ MainActivity)
        loggedInMssv = getIntent().getStringExtra("LOGGED_IN_MSSV");

        // Khởi tạo Launcher
        setupEditProfileLauncher();

        // Hiển thị dữ liệu lần đầu
        displayStudentInfo();

        // Thiết lập sự kiện Đăng xuất
        btnLogout.setOnClickListener(v -> handleLogout());

        // Thiết lập sự kiện Chỉnh sửa
        btnEditProfile.setOnClickListener(v -> startEditActivity());

    }

    // Logic Khởi tạo ActivityResultLauncher
    private void setupEditProfileLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Dữ liệu đã được lưu và cập nhật vào danh sách static trong MainActivity
                            Toast.makeText(HomeActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            displayStudentInfo(); // Tải lại thông tin mới
                        }
                        // Nếu RESULT_CANCELED (hủy bỏ) thì không làm gì cả
                    }
                });
    }

    private void startEditActivity() {
        if (loggedInMssv == null) return;

        Intent intent = new Intent(HomeActivity.this, EditActivity.class);
        intent.putExtra("EDIT_MSSV", loggedInMssv);

        // Khởi chạy EditActivity và chờ kết quả
        editProfileLauncher.launch(intent);
    }

    // Phương thức tìm và hiển thị thông tin sinh viên hiện tại
    private void displayStudentInfo() {
        SinhVien sv = findStudentByMssv(loggedInMssv);

        if (sv != null) {
            tvHoTen.setText("Họ tên: " + sv.getName());
            tvMSSV.setText("MSSV: " + sv.getMssv());
            tvNgaySinh.setText("Ngày sinh: " + sv.getDoB());

            // LOGIC HIỂN THỊ AVATAR
            Glide.with(this)
                    .load(sv.getAvatarUrl()) // Load từ URL
                    .circleCrop() // Bo tròn ảnh
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh tạm thời khi đang tải
                    .into(imageViewAvatar); // Gán vào ImageView

        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu sinh viên.", Toast.LENGTH_LONG).show();
            handleLogout();
        }
    }

    private SinhVien findStudentByMssv(String mssv) {
        for (SinhVien sv : MainActivity.sinhVienList) {
            if (sv.getMssv().equals(mssv)) {
                return sv;
            }
        }
        return null;
    }

    private void handleLogout() {
        // Sử dụng cờ CLEAR_TOP để quay về Activity gốc (MainActivity)
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        Toast.makeText(this, "Đã đăng xuất.", Toast.LENGTH_SHORT).show();
        finish(); // Đóng HomeActivity
    }
}