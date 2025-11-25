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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<ToDo> todos;
    private ToDoListAdapter adapter;
    private ListView list;

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

        registerForContextMenu(list);
        // Thiết lập Launcher
        setupToDoResultLauncher();
    }

    // --- 1. OPTION MENU (Góc trên bên phải) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_new) {
            // Chức năng: Thêm mới (Thay thế nút + cũ)
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            todoResultLauncher.launch(intent);
            return true;
        } else if (id == R.id.menu_select_all) {
            // Chức năng: Chọn tất cả Checkbox đầu dòng
            selectAllTasks();
            return true;
        } else if (id == R.id.menu_delete) {
            // Chức năng: Xóa các task đang được chọn
            deleteSelectedTasks();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // --- CONTEXT MENU (Nhấn giữ item) ---

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.todoLV) {
            getMenuInflater().inflate(R.menu.menu_context, menu);
//            menu.setHeaderTitle("Chọn thao tác");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Lấy thông tin vị trí item được nhấn giữ
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int id = item.getItemId();

        if (id == R.id.context_edit) {
            // Chức năng: Sửa task
            ToDo taskToEdit = todos.get(position);
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra(KEY_TODO_ITEM, taskToEdit);
            intent.putExtra(KEY_TODO_POSITION, position);
            todoResultLauncher.launch(intent);
            return true;
        } else if (id == R.id.context_delete) {
            // Chức năng: Xóa task này
            deleteSingleTask(position);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    // --- CÁC HÀM XỬ LÝ LOGIC ---

    private void selectAllTasks() {
        // Kiểm tra xem có phải tất cả đang được chọn không để toggle (chọn hết / bỏ chọn hết)
        boolean allSelected = true;
        for (ToDo todo : todos) {
            if (!todo.isSelected()) {
                allSelected = false;
                break;
            }
        }

        // Nếu tất cả đã chọn -> Bỏ chọn hết. Ngược lại -> Chọn hết.
        boolean targetState = !allSelected;
        for (ToDo todo : todos) {
            todo.setSelected(targetState);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteSelectedTasks() {
        // Sử dụng Iterator để xóa an toàn trong khi duyệt danh sách
        Iterator<ToDo> iterator = todos.iterator();
        boolean hasDeleted = false;
        while (iterator.hasNext()) {
            ToDo todo = iterator.next();
            if (todo.isSelected()) {
                iterator.remove();
                hasDeleted = true;
            }
        }

        if (hasDeleted) {
            updateOrderNumbers(); // Cập nhật lại số thứ tự #1, #2...
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa các mục đã chọn.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Chưa chọn mục nào để xóa.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSingleTask(int position) {
        todos.remove(position);
        updateOrderNumbers();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã xóa công việc.", Toast.LENGTH_SHORT).show();
    }

    private void updateOrderNumbers() {
        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setOrder("#" + (i + 1));
        }
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
                                        newOrUpdatedToDo.setSelected(todos.get(position).isSelected());
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
                boolean newState = !t.isChecked(); // Đảo ngược trạng thái hiện tại
                t.setChecked(newState);

                // Cập nhật lại UI ngay lập tức
                radioStatus.setChecked(newState);
                notifyDataSetChanged(); // Refresh lại list để cập nhật màu chữ/gạch ngang
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
    private boolean isSelected;

    // Constructor
    public ToDo(String order, String title, String description, Date deadline, boolean isChecked){
        this.order = order;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.isChecked = isChecked;
        this.isSelected = false;
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

    public boolean isSelected() { return isSelected; }

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

    public void setSelected(boolean selected) { isSelected = selected; }
}

