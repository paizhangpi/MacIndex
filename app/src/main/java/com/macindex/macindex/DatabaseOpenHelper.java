package com.macindex.macindex;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "specs.db";
    DatabaseOpenHelper(final Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }
}
