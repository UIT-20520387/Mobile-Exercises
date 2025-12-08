package com.example.listviewex;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<ToDo> todos;
    private ToDoListAdapter adapter;
    private ListView list;

    private ToDoDAO todoDAO;

    // Hằng số Keys cho việc truyền dữ liệu
    public static final String KEY_TODO_ITEM = "TODO_ITEM";
    public static final String KEY_TODO_POSITION = "TODO_POSITION";

    // Khai báo Launcher chung cho cả Add và Edit
    private ActivityResultLauncher<Intent> todoResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo DAO (SQLite)
        todoDAO = new ToDoDAO(this);

        // Lấy dữ liệu từ DB thay vì tạo ArrayList rỗng
        todos = todoDAO.getAll();

        list = (ListView) findViewById(R.id.todoLV);

        adapter = new ToDoListAdapter(this, R.layout.todo_list_item, todos, todoDAO);

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
            // Chức năng: Thêm mới
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
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Lấy thông tin vị trí item được nhấn giữ
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int id = item.getItemId();

        if (id == R.id.context_edit) {
            // Sửa task
            ToDo taskToEdit = todos.get(position);

            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra(KEY_TODO_ITEM, taskToEdit);
            intent.putExtra(EditActivity.KEY_TASK_INDEX, position);
            todoResultLauncher.launch(intent);
            return true;
        } else if (id == R.id.context_delete) {
            // Xóa task này
            deleteSingleTask(position);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    // --- CÁC HÀM XỬ LÝ LOGIC ---

    private void selectAllTasks() {
        boolean allSelected = true;
        for (ToDo todo : todos) {
            if (!todo.isSelected()) {
                allSelected = false;
                break;
            }
        }

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
                // Xóa trong DB trước
                if (todo.getId() != -1) {
                    todoDAO.delete(todo.getId());
                }
                iterator.remove();
                hasDeleted = true;
            }
        }

        if (hasDeleted) {
            updateOrderNumbersAndPersist(); // Cập nhật lại order và lưu vào DB
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa các mục đã chọn.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Chưa chọn mục nào để xóa.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSingleTask(int position) {
        ToDo t = todos.get(position);
        if (t.getId() != -1) {
            todoDAO.delete(t.getId());
        }
        todos.remove(position);
        updateOrderNumbersAndPersist();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã xóa công việc.", Toast.LENGTH_SHORT).show();
    }

    private void updateOrderNumbersAndPersist() {
        for (int i = 0; i < todos.size(); i++) {
            ToDo t = todos.get(i);
            String newOrder = "#" + (i + 1);
            t.setOrder(newOrder);
            // lưu orderIndex vào DB (ghi đè)
            if (t.getId() != -1) {
                // đảm bảo orderIndex lưu đúng số (bỏ ký tự '#')
                try {
                    // lưu orderIndex như số nguyên (1-based)
                    t.setOrder(String.valueOf(i + 1)); // tạm set order thành "1","2",... để DAO dùng parse
                    todoDAO.update(t);
                    // Sau update, đưa lại format hiển thị "#n"
                    t.setOrder("#" + (i + 1));
                } catch (Exception e) {
                    // nếu parse lỗi thì bỏ qua
                }
            }
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
                                    // TRƯỜNG HỢP CẬP NHẬT (EDIT)
                                    if (taskIndex >= 0 && taskIndex < todos.size()) {
                                        // Update vào DB
                                        if (todoItem.getId() != -1) {
                                            todoDAO.update(todoItem);
                                        } else {
                                            // nếu object không có id (cộp từ AddActivity mà bạn gửi back), insert và set id
                                            long newId = todoDAO.insert(todoItem);
                                            todoItem.setId((int)newId);
                                        }
                                        // Giữ nguyên Order hiển thị
                                        todoItem.setOrder("#" + (taskIndex + 1));
                                        todos.set(taskIndex, todoItem);
                                        Toast.makeText(MainActivity.this, "Đã cập nhật công việc!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // TRƯỜNG HỢP THÊM MỚI (ADD)
                                    // Insert vào DB và set id
                                    long newId = todoDAO.insert(todoItem);
                                    todoItem.setId((int)newId);

                                    // đặt order để hiển thị
                                    todoItem.setOrder("#" + (todos.size() + 1));
                                    todos.add(todoItem);
                                    Toast.makeText(MainActivity.this, "Đã thêm công việc mới!", Toast.LENGTH_SHORT).show();
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                // fallback: nếu không có todoItem, reload từ DB
                                reloadFromDB();
                            }
                        }
                    }
                });
    }

    private void reloadFromDB() {
        todos.clear();
        todos.addAll(todoDAO.getAll());
        // convert orderIndex to display "#n"
        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setOrder("#" + (i + 1));
        }
        adapter.notifyDataSetChanged();
    }
}
