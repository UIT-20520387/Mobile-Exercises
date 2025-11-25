package com.example.listviewex;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {

    private ListView lvContacts;
    private ArrayList<String> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        lvContacts = findViewById(R.id.contactLV); // Đảm bảo ID đúng trong XML
        contactList = new ArrayList<>();

        loadContacts();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        lvContacts.setAdapter(adapter);

        // Xử lý chọn item
        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            String selectedContact = contactList.get(position);

            // Trả dữ liệu về màn hình cũ
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CONTACT", selectedContact);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadContacts() {
        // Lấy cả Tên và SĐT
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (nameIdx >= 0 && phoneIdx >= 0) {
                    String name = cursor.getString(nameIdx);
                    String phone = cursor.getString(phoneIdx);
                    // Định dạng hiển thị: "0912345678 - Nguyễn Văn A"
                    contactList.add(phone + " - " + name);
                }
            }
            cursor.close();
        }
    }
}
