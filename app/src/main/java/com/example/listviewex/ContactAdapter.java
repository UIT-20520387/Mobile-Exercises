package com.example.listviewex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    public ContactAdapter(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            // Sử dụng layout mặc định simple_list_item_1 cho đơn giản
            view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Contact contact = getItem(position);

        if (contact != null) {
            TextView textView = view.findViewById(android.R.id.text1);

            // ĐỊNH DẠNG CHUỖI THEO YÊU CẦU: "#1 098xxx Nguyễn Văn A"
            String displayText = "#" + contact.getId() + " - " + contact.getPhoneNumber() + " - " + contact.getName();

            textView.setText(displayText);
        }

        return view;
    }
}
