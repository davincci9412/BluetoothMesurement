package com.panda.bluealarm;

/**
 * Created by Panda on 1/23/2017.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class PreferenceHelper
{
    private SharedPreferences app_prefs;
    private final String PREF_NAME = "bluealarm";
    private final String ISDETECTING= "isFirst";


    public PreferenceHelper(Context context)
    {
        app_prefs = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
    }

    public void putIsDetecting(boolean isDetecting)
    {
        Editor edit = app_prefs.edit();
        edit.putBoolean(ISDETECTING, isDetecting);
        edit.commit();
    }
    public boolean getIsDetecting( )
    {
        return app_prefs.getBoolean(ISDETECTING, false);
    }
}
