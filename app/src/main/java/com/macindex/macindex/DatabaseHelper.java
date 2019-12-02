package com.macindex.macindex;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper {
    private final Context context;
    private SQLiteDatabase database;
    private DatabaseOpenHelper dbHelper;

    public DatabaseHelper(Context context){
        this.context = context;
        dbHelper = new DatabaseOpenHelper(context);
    }

    public DatabaseHelper open() throws SQLException
    {
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getReadableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }
}