package com.example.listviewex;

import java.io.Serializable;
import java.util.Date;

public class ToDo implements Serializable {
    private int id;
    private String order;
    private String title;
    private String description;
    private Date deadline;
    private boolean isChecked;
    private boolean isSelected;
    private String contactInfo;

    // Constructor
    public ToDo(int id, String order, String title, String description, Date deadline, boolean isChecked, String contactInfo){
        this.id = id;
        this.order = order;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.isChecked = isChecked;
        this.isSelected = false;
        this.contactInfo = contactInfo;
    }

    // Constructor không có id (dùng khi thêm mới)
    public ToDo(String order, String title, String description, Date deadline, boolean isChecked, String contactInfo) {
        this(-1, order, title, description, deadline, isChecked, contactInfo);
    }

    // Getters
    public int getId() { return id;}

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

    public String getContactInfo() { return contactInfo; }

    // Setters
    public void setId(int id) { this.id = id; }

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

    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}
