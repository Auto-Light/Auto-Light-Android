package com.example.autolight_android.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 4; // 버전 3으로 업데이트
    private static final String DB_NAME = "standard.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Standard (id INTEGER PRIMARY KEY AUTOINCREMENT, stLight INTEGER NOT NULL)");
        db.execSQL("INSERT INTO Standard (stLight) VALUES(128);"); // 초기화
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Standard");
        onCreate(db);
    }

    // SELECT문 - 기준 조명 밝기값, 최근 조명 다이얼값 가져오기
    public StandardItem getStandard() {
        StandardItem standardItem = new StandardItem();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Standard ORDER BY id ASC", null);

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") int stLight = cursor.getInt(cursor.getColumnIndex("stLight"));

                standardItem.setId(id);
                standardItem.setStLight(stLight);
            }
        }
        cursor.close();

        return standardItem;
    }

    // UPDATE문 - 기준 조명 밝기값 수정
    public void updateStLight(int _id, int _stLight) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE Standard SET (stLight) = ('"+ _stLight +"') WHERE (id) = ('"+ _id +"');");
        db.close();
    }
}