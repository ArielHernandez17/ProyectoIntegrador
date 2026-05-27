package com.aragon.proyectointegrador;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventario.db";
    private static final int DATABASE_VERSION = 2;

    public AdminSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE articulos (" +
                "codigo INTEGER PRIMARY KEY, " +
                "descripcion TEXT NOT NULL, " +
                "precio REAL NOT NULL, " +
                "stock INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS articulos");
        onCreate(db);
    }
}