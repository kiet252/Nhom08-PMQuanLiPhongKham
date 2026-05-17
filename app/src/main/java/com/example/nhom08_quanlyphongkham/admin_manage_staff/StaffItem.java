package com.example.nhom08_quanlyphongkham.admin_manage_staff;

public class StaffItem {
    private String id;
    private String name;
    private String Role;
    private String avatar;
    public StaffItem(String id, String name, String Role, String avatar) {
        this.id = id;
        this.name = name;
        this.Role = Role;
        this.avatar = avatar;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getRole() {
        return Role;
    }
    public String getAvatar() {
        return avatar;
    }
}
