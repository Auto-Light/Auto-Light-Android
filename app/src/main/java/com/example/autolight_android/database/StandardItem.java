package com.example.autolight_android.database;

public class StandardItem {
    private int id;         // 저장된 밝기의 id
    private int stLight;    // 기준 밝기값

    public StandardItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStLight() {
        return stLight;
    }

    public void setStLight(int stLight) {
        this.stLight = stLight;
    }

}