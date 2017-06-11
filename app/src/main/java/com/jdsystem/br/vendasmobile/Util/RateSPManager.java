package com.jdsystem.br.vendasmobile.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by AL on 11/06/2017.
 */

public class RateSPManager {
    private static final String LAUNCH_TIMES_KEY = "launch_times_key";
    private static final int LAUNCH_TIMES = 3;
    private static final String TIME_KEY = "time_key";
    private static final int DAYS_DELAY = 3 * (24 * 60 * 60 * 1000);
    private static final String NEVER_ASK_KEY = "never_ask_key";

    private static SharedPreferences getSP(Context c) {
        return c.getSharedPreferences("prefereces", Context.MODE_PRIVATE);
    }

    public static void updateLaunchTimes(Context c) {
        SharedPreferences sp = getSP(c);
        sp.edit().putInt(LAUNCH_TIMES_KEY, 0).apply();
    }

    public static void updateLaunchTimes(Context c, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        }
        SharedPreferences sp = getSP(c);
        int launchTimes = sp.getInt(LAUNCH_TIMES_KEY, 0);
        sp.edit().putInt(LAUNCH_TIMES_KEY, launchTimes + 1).apply();
    }

    private static boolean isLaunchTimesValid(Context c) {
        SharedPreferences sp = getSP(c);
        int launchTimes = sp.getInt(LAUNCH_TIMES_KEY, 0);
        return launchTimes > 0 && launchTimes % LAUNCH_TIMES == 0;
    }

    public static void updateTime(Context c) {
        SharedPreferences sp = getSP(c);
        sp.edit().putLong(TIME_KEY, System.currentTimeMillis() + DAYS_DELAY).apply();
    }

    private static boolean isTimeValid(Context c) {
        SharedPreferences sp = getSP(c);
        Long time = sp.getLong(TIME_KEY, 0);
        /*CASO SEJA A PRIMEIRA VEZ QUE O DEVICE ESTEJA SENDO UTILIZADO.*/
        if (time == 0) {
            updateTime(c);
            time = sp.getLong(TIME_KEY, 0);
        }
        return time < System.currentTimeMillis();
    }

    public static void neverAskAgain(Context c) {
        SharedPreferences sp = getSP(c);
        sp.edit().putBoolean(NEVER_ASK_KEY, true).apply();
    }

    private static boolean isNeverAskAgain(Context c) {
        SharedPreferences sp = getSP(c);
        return sp.getBoolean(NEVER_ASK_KEY, false);
    }

    public static boolean canShowDialog( Context c ){
        return !isNeverAskAgain(c) && ( isTimeValid(c) || isLaunchTimesValid(c) );
        }
}
