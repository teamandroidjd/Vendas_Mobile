package com.jdsystem.br.vendasmobile.Util;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by AL on 11/06/2017.
 */

public class RateDialogManager {
    public static void showRateDialog(
            Context context,
            Bundle savedInstanceState) {
        RateSPManager.updateLaunchTimes(context, savedInstanceState);
        FragmentManager fm = getFragManager(context);
        if (RateSPManager.canShowDialog(context)
                && fm.findFragmentByTag(RateDialogFrag.KEY) == null) {
            RateDialogFrag dialog = new RateDialogFrag();
            dialog.setCancelable(false);
            dialog.show(fm, RateDialogFrag.KEY);
        }
    }

    private static FragmentManager getFragManager(Context context) {
        AppCompatActivity activity = (AppCompatActivity) context;
        return activity.getFragmentManager();
    }

    public static void showRateDialogPlayStore(Context context) {
        FragmentManager fm = getFragManager(context);
        RateDialogFrag dialog = new RateDialogPlayStoreFrag();
        dialog.setCancelable(false);
        dialog.show(fm, RateDialogFrag.KEY);
    }

    public static void showRateDialogFeedback(Context context, float rating) {
        FragmentManager fm = getFragManager(context);
        RateDialogFeedbackFrag dialog = new RateDialogFeedbackFrag();
        dialog.setRating(rating);
        dialog.setCancelable(false);
        dialog.show(fm, RateDialogFrag.KEY);
    }
}
