package com.example.listviewex;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ToDoListAdapter extends ArrayAdapter<ToDo> {
    int resource;
    private SimpleDateFormat dateFormatter;
    private ToDoDAO dao;

    public ToDoListAdapter(Context context, int resource, List<ToDo> todos, ToDoDAO dao) {
        super(context, resource, todos);
        this.resource = resource;
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.dao = dao;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(this.getContext());
            v = vi.inflate(this.resource,null);
        }
        ToDo t = getItem(position);

        if (t!=null){
            TextView orderTextView = (TextView) v.findViewById(R.id.order);
            TextView titleTextView = (TextView) v.findViewById(R.id.title);
            TextView deadlineTextView = (TextView) v.findViewById(R.id.deadline);
            CheckBox cbSelect = v.findViewById(R.id.cbSelect);
            RadioButton radioStatus = v.findViewById(R.id.radioStatus);
            Date deadlineDate = t.getDeadline();


            if(orderTextView!=null)
                orderTextView.setText(t.getOrder());
            if(titleTextView!=null)
                titleTextView.setText(t.getTitle());
            if (deadlineTextView!=null) {
                String formattedDate = dateFormatter.format(deadlineDate);
                deadlineTextView.setText(formattedDate);
            }

            // --- XỬ LÝ CHECKBOX SELECTION (ĐẦU DÒNG) ---
            cbSelect.setOnCheckedChangeListener(null); // Reset listener
            cbSelect.setChecked(t.isSelected());       // Gán state từ model
            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                t.setSelected(isChecked); // Cập nhật model
            });

            // --- XỬ LÝ RADIO BUTTON STATUS (CUỐI DÒNG) ---
            if (t.isChecked()) {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                deadlineTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                radioStatus.setChecked(true);
            } else {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                deadlineTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                radioStatus.setChecked(false);
            }

            // Xử lý sự kiện click RadioButton
            radioStatus.setOnClickListener(view -> {
                boolean newState = !t.isChecked();
                t.setChecked(newState);

                // persist ngay
                if (t.getId() != -1) {
                    dao.update(t);
                }
                notifyDataSetChanged();
            });
        }
        return v;
    }
}
