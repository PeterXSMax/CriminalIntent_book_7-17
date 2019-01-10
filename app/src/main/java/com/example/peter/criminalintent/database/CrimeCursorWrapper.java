package com.example.peter.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.example.peter.criminalintent.database.CrimeDbSchama.CrimeTable;

import com.example.peter.criminalintent.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Crime getCrime(){
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int  isSloved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setmTitle(title);
        crime.setmDate(new Date(date));
        crime.setmSloved(isSloved != 0);
        crime.setmSuspect(suspect);

        return crime;

//        return null;
    }
}
