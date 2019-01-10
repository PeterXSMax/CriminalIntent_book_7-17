package com.example.peter.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.peter.criminalintent.database.CrimeBaseHelper;
import com.example.peter.criminalintent.database.CrimeCursorWrapper;
import com.example.peter.criminalintent.database.CrimeDbSchama;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.peter.criminalintent.database.CrimeDbSchama.CrimeTable;

public class CrimeLab {
    private static CrimeLab sCrimeLab;//看到这个s的前缀默认是一个静态的变量

//    private List<Crime> mCrimes;
    private Context mContext;
    private SQLiteDatabase mDatabase;


    public static CrimeLab get(Context context){
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

//        mCrimes = new ArrayList<>();
    }

    public List<Crime> getCrimes(){
//        return null;
        List<Crime> crimes =  new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
    }

        return crimes;
    }

    public Crime getCrime(UUID id){
//        for(Crime crime : mCrimes){
//            if(crime.getmId().equals(id)){
//                return crime;
//            }
//        }
//        return null;
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",new String[]{id.toString()}
        );

        try {
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        }finally {
            cursor.close();
        }
    }

    public void addCrime(Crime c){
//        mCrimes.add(c);
        ContentValues values = getContentValues(c);

        mDatabase.insert(CrimeTable.NAME, null, values);

    }

    public void updateCrime(Crime c){
        String uuidString = c.getmId().toString();
        ContentValues values = getContentValues(c);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(Crime crime){

        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getmId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getmTitle());
        values.put(CrimeTable.Cols.DATE, crime.getmDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.ismSloved()?1:0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getmSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime crime){
        File externalFileDir = mContext
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(externalFileDir == null){
            return null;
        }

        return new File(externalFileDir, crime.getPhotoFilename());
    }


}
