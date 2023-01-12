package com.example.autolight_android.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 5; // 버전 5로 업데이트
    private static final String DB_NAME = "standard.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Standard (id INTEGER PRIMARY KEY, stLight INTEGER NOT NULL)");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(1, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(2, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(3, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(4, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(5, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(6, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(7, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(8, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(9, 128);");
        db.execSQL("INSERT INTO Standard (id, stLight) VALUES(10, 128);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Standard");
        onCreate(db);
    }

    // SELECT문 - 기준 조명 밝기값, 최근 조명 다이얼값 가져오기
    public StandardItem getStandard(int _id) {
        String userID = Integer.toString(_id);
        StandardItem standardItem = new StandardItem();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Standard WHERE id = ?;", new String[]{userID});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") int stLight = cursor.getInt(cursor.getColumnIndex("stLight"));

                standardItem.setId(id);
                standardItem.setStLight(stLight);
            }
        }
        else {
            cursor.close();
            return null;
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