package com.example.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;

    // Mảng dữ liệu SinhVien cố định
    public static List<SinhVien> sinhVienList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo dữ liệu cứng
        if (sinhVienList.isEmpty()) {
            createSinhVienData();
        }

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> handleLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        createSinhVienData();
    }

    private void createSinhVienData() {
        sinhVienList = new ArrayList<>();

        // Link ảnh Google Drive (Đã chia sẻ công khai)
        String url1 = "https://drive.google.com/uc?export=download&id=1eoDureX-Sb3rpZpu7hkbs4cjEtcLbP2J";
        String url2 = "https://drive.google.com/uc?export=download&id=1nfbYmIjlRUEHbHHNteeOzoh_ZJnQUeNf";
        String url3 = "https://drive.google.com/uc?export=download&id=1XG0aAB0_uvdFYCwzTGoPUFMhX-GyJ1rf";
        String url4 = "https://drive.google.com/uc?export=download&id=1HBx0sUWPneZWmS41B9SgWk6epev5LmJD";


        sinhVienList.add(new SinhVien("20520387", "pass1", "Nguyễn Đông Anh", "02/05/2002", url1));
        sinhVienList.add(new SinhVien("20520388", "pass2", "Trần Thị B", "20/12/2001", url2));
        sinhVienList.add(new SinhVien("20520389", "pass3", "Lê Văn C", "01/01/2003",url3));
        sinhVienList.add(new SinhVien("20520390", "pass4", "Phạm Thị D", "10/08/2002",url4));
    }

    private void handleLogin() {
        String mssv = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (mssv.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập MSSV và Mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm sinh viên trong danh sách
        SinhVien authenticatedSV = null;
        for (SinhVien sv : sinhVienList) {
            if (sv.getMssv().equals(mssv) && sv.getPassword().equals(password)) {
                authenticatedSV = sv;
                break;
            }
        }

        if (authenticatedSV != null) {
            // Đăng nhập thành công: Chuyển sang HomeActivity và truyền đối tượng
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("LOGGED_IN_MSSV", authenticatedSV.getMssv()); // Truyền đối tượng Serializable
            startActivity(intent);
            finish(); // Đóng MainActivity để người dùng không quay lại màn hình đăng nhập
        } else {
            // Đăng nhập thất bại
            Toast.makeText(this, "MSSV hoặc Mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Phương thức này xử lý dữ liệu trả về từ HomeActivity
     * Nếu HomeActivity có thay đổi, nó sẽ trả về đối tượng SinhVien đã cập nhật
     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK) {
//            if (data != null && data.hasExtra(KEY_SINH_VIEN)) {
//                SinhVien updatedSv = (SinhVien) data.getSerializableExtra(KEY_SINH_VIEN);
//
//                // Cập nhật sinh viên trong mảng cứng (quan trọng!)
//                for (int i = 0; i < sinhVienList.size(); i++) {
//                    if (sinhVienList.get(i).getMssv().equals(updatedSv.getMssv())) {
//                        sinhVienList.set(i, updatedSv);
//                        break;
//                    }
//                }
//                Toast.makeText(this, "Dữ liệu sinh viên đã được cập nhật.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    public static class SinhVien implements Serializable {
        private String mssv;
        private String password;
        private String name;
        private String dob;
        private String avatarUrl;

        // Constructor
        public SinhVien(String mssv, String password, String name, String dob, String avatarUrl){
            this.mssv = mssv;
            this.password = password;
            this.name = name;
            this.dob = dob;
            this.avatarUrl = avatarUrl;
        }

        // Getters
        public String getMssv() {
            return mssv;
        }

        public String getPassword() {
            return password;
        }

        public String getName() {
            return name;
        }

        public String getDoB() {
            return dob;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        // Setters
        public void setName(String name) {
            this.name = name;
        }
        public void setDoB(String dob) {
            this.dob = dob;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}