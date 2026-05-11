package com.example.nhom08_quanlyphongkham;

public class StaffItem {
    private String id;
    private String name;
    private String Role;
    public StaffItem(String id, String name, String Role) {
        this.id = id;
        this.name = name;
        this.Role = Role;
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
}
