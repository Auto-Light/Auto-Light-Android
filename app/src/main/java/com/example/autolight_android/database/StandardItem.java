package com.example.autolight_android.database;

public class StandardItem {
    private int id;         // 저장된 밝기의 id
    private int stLight;    // 기준 밝기값
    private int lampDial;   // 최근 조명 다이얼 값

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

    public int getLampDial() { return lampDial; }

    public void setLampDial(int lampDial) { this.lampDial = lampDial; }
}