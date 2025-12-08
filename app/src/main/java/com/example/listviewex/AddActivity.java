package com.example.listviewex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDeadline;
    private CheckBox cbStatus;
    private Button btnSave;
    private Date selectedDeadline; // Biến lưu trữ Date
    private TextView tvSelectedContact;
    private ImageButton btnSelectContact;
    private String currentContactInfo = "";

    private ActivityResultLauncher<Intent> contactLauncher;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ArrayList<Contact> contactList; // nếu bạn dùng danh sách contact
    // private ContactAdapter adapter; // bạn có thể giữ adapter nếu cần hiển thị

    private ToDoDAO todoDAO;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add); // giữ layout bạn đang dùng (có các id etTitle, etDescription, ...)

        // Ánh xạ view theo layout bạn đã gửi
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDeadline = findViewById(R.id.etDeadline);
        btnSave = findViewById(R.id.btnSave);
        cbStatus = findViewById(R.id.cbStatus);
        tvSelectedContact = findViewById(R.id.tvSelectedContact);
        btnSelectContact = findViewById(R.id.btnSelectContact);

        // DAO (sử dụng ToDoDAO của bạn)
        todoDAO = new ToDoDAO(this);

        // Ẩn cbStatus khi thêm mới (theo logic ban đầu)
        cbStatus.setVisibility(View.GONE);

        // Date picker
        etDeadline.setOnClickListener(v -> showDatePickerDialog());

        // Contact launcher: mở ContactListActivity (nếu bạn có)
        contactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String contact = result.getData().getStringExtra("SELECTED_CONTACT");
                        if (contact != null) {
                            currentContactInfo = contact;
                            tvSelectedContact.setText(contact);
                        }
                    }
                }
        );

        btnSelectContact.setOnClickListener(v -> checkPermissionAndOpenContactList());

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int y, int m, int d) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(y, m, d, 0, 0, 0);
                    selectedDeadline = cal.getTime();
                    etDeadline.setText(sdf.format(selectedDeadline));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void checkPermissionAndOpenContactList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(AddActivity.this, ContactListActivity.class);
            contactLauncher.launch(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nếu bạn muốn load contacts trực tiếp trong activity này:
                loadContacts(); // tùy: nếu bạn không dùng, có thể bỏ
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền Danh bạ để hoạt động!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // (Tuỳ) hàm load contacts nếu bạn muốn
    private void loadContacts() {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
        );

        if (cursor != null) {
            contactList = new ArrayList<>();
            int idCounter = 1;
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = (nameIndex != -1) ? cursor.getString(nameIndex) : "No Name";

                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phone = (phoneIndex != -1) ? cursor.getString(phoneIndex) : "No Phone";

                contactList.add(new Contact(idCounter++, name, phone));
            }
            cursor.close();
            // nếu có adapter: adapter.notifyDataSetChanged();
        }
    }

    private void handleSave() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tiêu đề.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDeadline == null) {
            Toast.makeText(this, "Vui lòng chọn Ngày hết hạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo ToDo sử dụng constructor bạn có (không id)
        ToDo newToDo = new ToDo(
                "", // order (MainActivity sẽ cập nhật order sau khi reload)
                title,
                description,
                selectedDeadline,
                false, // mặc định chưa checked
                currentContactInfo
        );

        // Insert vào DB
        long newId = todoDAO.insert(newToDo);

        if (newId == -1) {
            // insert failed
            Toast.makeText(this, "Lỗi khi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // set id trên object (tùy dùng sau này)
        newToDo.setId((int) newId);

        // Trả về MainActivity: gửi một Intent rỗng để MainActivity nhận data != null
        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    }
}
