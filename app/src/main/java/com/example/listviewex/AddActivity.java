package com.example.listviewex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDeadline;
    private CheckBox cbStatus;
    private Button btnSave;
    private Date selectedDeadline; // Biến lưu trữ đối tượng Date
    private TextView tvSelectedContact;
    private ImageButton btnSelectContact;
    private String currentContactInfo = "";

    private ActivityResultLauncher<Intent> contactLauncher;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ArrayList<Contact> contactList;
    private ContactAdapter adapter;

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
        tvSelectedContact = findViewById(R.id.tvSelectedContact);
        btnSelectContact = findViewById(R.id.btnSelectContact);

        // Theo yêu cầu: Khi thêm mới luôn là Progress.
        cbStatus.setVisibility(View.GONE);

        // Xử lý chọn ngày (DatePickerDialog)
        etDeadline.setOnClickListener(v -> showDatePickerDialog());


        contactList = new ArrayList<>();

        // Gán adapter
        adapter = new ContactAdapter(this, android.R.layout.simple_list_item_1, contactList);

        // Cấu hình Launcher nhận kết quả từ ContactListActivity
        contactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Lấy chuỗi "SĐT - Tên" được trả về
                        String contact = result.getData().getStringExtra("SELECTED_CONTACT");
                        currentContactInfo = contact;
                        tvSelectedContact.setText(contact); // Hiển thị lên UI
                    }
                }
        );

        // Xử lý sự kiện click nút ">"
        btnSelectContact.setOnClickListener(v -> checkPermissionAndOpenContactList());

        // Xử lý sự kiện Lưu
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void checkPermissionAndOpenContactList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            // Mở màn hình danh sách contact riêng biệt
            Intent intent = new Intent(AddActivity.this, ContactListActivity.class);
            contactLauncher.launch(intent);
        }
    }

    // Xử lý kết quả xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền Danh bạ để hoạt động!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadContacts() {
        // Sử dụng URI CommonDataKinds.Phone để lấy cả Tên và SĐT
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            contactList.clear();
            int idCounter = 1; // Tạo ID giả lập (#1, #2...)

            while (cursor.moveToNext()) {
                // Lấy Tên
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = (nameIndex != -1) ? cursor.getString(nameIndex) : "No Name";

                // Lấy SĐT
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phone = (phoneIndex != -1) ? cursor.getString(phoneIndex) : "No Phone";

                // Thêm vào list
                contactList.add(new Contact(idCounter++, name, phone));
            }
            cursor.close();

            // Cập nhật giao diện
            adapter.notifyDataSetChanged();
        }
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
        ToDo newToDo = new ToDo(
                "", // Order sẽ được gán lại trong MainActivity
                title,
                description,
                selectedDeadline,
                false, // Mặc định là Progress (chưa checked)
                currentContactInfo

        );

        // Gửi đối tượng ToDo mới về MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.KEY_TODO_ITEM, newToDo);

        setResult(RESULT_OK, resultIntent); // Đánh dấu thành công
        finish(); // Quay lại màn hình chính
    }
}



