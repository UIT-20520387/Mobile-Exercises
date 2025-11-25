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
            intent.putExtra(EditActivity.KEY_TASK_INDEX, position);
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
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            ToDo todoItem = (ToDo) data.getSerializableExtra(KEY_TODO_ITEM);

                            // Lấy vị trí Task được trả về (Chỉ có trong EditActivity)
                            int taskIndex = data.getIntExtra(EditActivity.KEY_TASK_INDEX, -1);

                            if (todoItem != null) {
                                if (taskIndex != -1) {
                                    // 1. TRƯỜNG HỢP CẬP NHẬT (EDIT)
                                    // taskIndex có giá trị, thay thế đối tượng cũ bằng đối tượng mới
                                    if (taskIndex >= 0 && taskIndex < todos.size()) {
                                        // Giữ nguyên Order và cập nhật đối tượng
                                        todoItem.setOrder("#" + (taskIndex + 1));
                                        todos.set(taskIndex, todoItem);
                                        Toast.makeText(MainActivity.this, "Đã cập nhật công việc!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // 2. TRƯỜNG HỢP THÊM MỚI (ADD)
                                    // taskIndex là -1, thêm vào cuối danh sách
                                    todoItem.setOrder("#" + (todos.size() + 1));
                                    todos.add(todoItem);
                                    Toast.makeText(MainActivity.this, "Đã thêm công việc mới!", Toast.LENGTH_SHORT).show();
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
}





