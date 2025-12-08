package com.example.listviewex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ToDoDAO extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ToDoDAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE todos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "orderCol TEXT, " +
                        "title TEXT, " +
                        "description TEXT, " +
                        "deadline TEXT, " +
                        "isChecked INTEGER, " +
                        "contactInfo TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS todos");
        onCreate(db);
    }

    // INSERT ToDo
    public long insert(ToDo todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("orderCol", todo.getOrder());
        cv.put("title", todo.getTitle());
        cv.put("description", todo.getDescription());
        cv.put("deadline", sdf.format(todo.getDeadline()));
        cv.put("isChecked", todo.isChecked() ? 1 : 0);
        cv.put("contactInfo", todo.getContactInfo());

        return db.insert("todos", null, cv);
    }

    // UPDATE
    public int update(ToDo todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("orderCol", todo.getOrder());
        cv.put("title", todo.getTitle());
        cv.put("description", todo.getDescription());
        cv.put("deadline", sdf.format(todo.getDeadline()));
        cv.put("isChecked", todo.isChecked() ? 1 : 0);
        cv.put("contactInfo", todo.getContactInfo());

        return db.update("todos", cv, "id = ?", new String[]{String.valueOf(todo.getId())});
    }

    // DELETE
    public int delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("todos", "id = ?", new String[]{String.valueOf(id)});
    }

    // GET ALL
    public ArrayList<ToDo> getAll() {
        ArrayList<ToDo> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM todos ORDER BY id ASC", null);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                String order = c.getString(1);
                String title = c.getString(2);
                String description = c.getString(3);
                String dl = c.getString(4);
                boolean isChecked = c.getInt(5) == 1;
                String contactInfo = c.getString(6);

                Date deadline;
                try {
                    deadline = sdf.parse(dl);
                } catch (ParseException e) {
                    deadline = new Date();
                }

                ToDo todo = new ToDo(id, order, title, description, deadline, isChecked, contactInfo);
                list.add(todo);

            } while (c.moveToNext());
        }

        c.close();
        return list;
    }
}
