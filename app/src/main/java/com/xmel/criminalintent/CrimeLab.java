package com.xmel.criminalintent;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CrimeLab {

    public static final String TAG = "CrimeLab";
    public static final String FILENAME = "crimes.json";

    private ArrayList<Crime> mCrimes;
    private CriminalIntentJSONSerializer mSerializer;

    private static CrimeLab sCrimeLab;
    private Context mAppContext;

    private CrimeLab(Context appContext) {
        mAppContext = appContext;

        mSerializer = new CriminalIntentJSONSerializer(mAppContext, FILENAME);

        try {
            mCrimes = mSerializer.loadCrimes();
        } catch (Exception e) {
            mCrimes = new ArrayList<Crime>();
        }
    }

    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    public ArrayList<Crime> getCrimes() {
        return mCrimes;
    }


    public Crime getCrime(UUID id){
        for (Crime c : mCrimes) {
            if (c.getId().equals(id)) {
                return c;
            }

        }
        return null;
    }

    public static CrimeLab get(Context c) {
        if (sCrimeLab == null)
        {
            sCrimeLab = new CrimeLab(c.getApplicationContext());
        }
        return  sCrimeLab;
    }

    public boolean saveCrimes(){
        try {
            Log.d(TAG, "1crimes saved to file");
            mSerializer.saveCrimes(mCrimes);
            Log.d(TAG, "2crimes saved to file");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "error saving crimes");
            e.printStackTrace();
            return false;
        }

    }


}
