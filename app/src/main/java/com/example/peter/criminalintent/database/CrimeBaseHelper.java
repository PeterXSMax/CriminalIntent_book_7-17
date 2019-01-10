package com.example.peter.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.peter.criminalintent.Crime;
import com.example.peter.criminalintent.database.CrimeDbSchama.*;

public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATEBASE_NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context){
        super(context, DATEBASE_NAME, null, VERSION);
    }

    @Override
    //创建数据库
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + CrimeTable.NAME + "(" + " _id integer primary key autoincrement, "+
                CrimeTable.Cols.UUID + ", " +
                CrimeTable.Cols.TITLE + ", " +
                CrimeTable.Cols.DATE + ", " +
                CrimeTable.Cols.SOLVED + ", "+
                CrimeTable.Cols.SUSPECT + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
