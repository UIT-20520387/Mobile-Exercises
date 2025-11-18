package com.example.listviewex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<ToDo> todos;
    private ToDoListAdapter adapter;
    private ListView list;
    private Button addBtn;

    // Hằng số Keys cho việc truyền dữ liệu
    public static final String KEY_TODO_ITEM = "TODO_ITEM";
    public static final String KEY_TODO_POSITION = "TODO_POSITION";

    // Khai báo Launcher chung cho cả Add và Edit
    private ActivityResultLauncher<Intent> todoResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo danh sách công việc RỖNG
        todos = new ArrayList<ToDo>();

        list = (ListView) findViewById(R.id.todoLV);
        adapter = new ToDoListAdapter(this, R.layout.todo_list_item, todos);
        list.setAdapter(adapter);

        addBtn = findViewById(R.id.addBtn);

        // Thiết lập Launcher
        setupToDoResultLauncher();

        //Xử lý sự kiện click nút +
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            todoResultLauncher.launch(intent); // Khởi chạy Activity và chờ kết quả
        });

        // Xử lý click Item (Sửa)
        list.setOnItemClickListener((parent, view, position, id) -> {
            ToDo taskToEdit = todos.get(position);
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra(KEY_TODO_ITEM, taskToEdit);
            intent.putExtra(KEY_TODO_POSITION, position); // Truyền vị trí để biết item nào cần cập nhật
            todoResultLauncher.launch(intent);
        });
    }

    private void setupToDoResultLauncher() {
        todoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                ToDo newOrUpdatedToDo = (ToDo) data.getSerializableExtra(KEY_TODO_ITEM);
                                // Lấy vị trí: -1 nếu là Add, vị trí thực nếu là Edit
                                int position = data.getIntExtra(KEY_TODO_POSITION, -1);

                                if (newOrUpdatedToDo != null) {
                                    if (position == -1) {
                                        // MODE: ADD NEW TASK
                                        newOrUpdatedToDo.setOrder("#" + (todos.size() + 1));
                                        todos.add(newOrUpdatedToDo);
                                        Toast.makeText(MainActivity.this, "Đã thêm công việc mới!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // MODE: EDIT EXISTING TASK
                                        todos.set(position, newOrUpdatedToDo);
                                        Toast.makeText(MainActivity.this, "Đã cập nhật công việc #" + (position + 1) + "!", Toast.LENGTH_SHORT).show();
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }
}

class ToDoListAdapter extends ArrayAdapter<ToDo>{
    int resource;
    private SimpleDateFormat dateFormatter;
    public ToDoListAdapter(Context context, int resource, List<ToDo> todos) {
        super(context, resource, todos);
        this.resource = resource;
        // Khởi tạo SimpleDateFormat ở cấp độ class (tối ưu hóa hiệu suất)
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
            CheckBox isCheckedTextView = (CheckBox) v.findViewById(R.id.checkboxCompleted);

            Date deadlineDate = t.getDeadline();


            if(orderTextView!=null)
                orderTextView.setText(t.getOrder());
            if(titleTextView!=null)
                titleTextView.setText(t.getTitle());
            if (deadlineTextView!=null) {
                String formattedDate = dateFormatter.format(deadlineDate);
                deadlineTextView.setText(formattedDate);
            }

            // Ngắt kết nối listener cũ trước (tránh lỗi lặp lại sự kiện do tái chế view)
            isCheckedTextView.setOnCheckedChangeListener(null);

            // Gán trạng thái hiện tại từ model (t.isChecked()) cho CheckBox
            isCheckedTextView.setChecked(t.isChecked());

            // Định dạng UI dựa trên trạng thái hiện tại (Đảm bảo đúng màu và gạch ngang)
            if (t.isChecked()) {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                deadlineTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            } else {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                deadlineTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            }

            // Gán listener mới
            isCheckedTextView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Chỉ cập nhật đối tượng ToDo hiện tại
                t.setChecked(isChecked);
                // Yêu cầu Adapter cập nhật lại giao diện (sẽ gọi getView cho các view hiển thị)
                notifyDataSetChanged();
            });
        }
        return v;
    }
}

class ToDo implements Serializable{
    private String order;
    private String title;
    private String description;
    private Date deadline;
    private boolean isChecked;

    // Constructor
    public ToDo(String order, String title, String description, Date deadline, boolean isChecked){
        this.order = order;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.isChecked = isChecked;
    }

    // Getters
    public String getOrder() {
        return order;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isChecked() {
        return isChecked;
    }

    // Setters
    public void setOrder(String order){
        this.order = order;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setDeadline(Date deadline){
        this.deadline = deadline;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }
}

