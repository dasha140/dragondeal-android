package com.elvsf.dragondeal.model;

public class MenuItemModel {

    private final String title;
    private final int iconResId;

    public MenuItemModel(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}