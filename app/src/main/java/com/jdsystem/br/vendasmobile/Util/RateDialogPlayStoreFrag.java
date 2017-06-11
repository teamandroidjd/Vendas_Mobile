package com.jdsystem.br.vendasmobile.Util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.R;

/**
 * Created by AL on 11/06/2017.
 */

public class RateDialogPlayStoreFrag extends RateDialogFrag {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rate_dialog_play_store, container);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_yes) {
            String packageName = getActivity().getPackageName();
            Intent it;
            try {
                it = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName));
                startActivity(it);
            } catch (android.content.ActivityNotFoundException e) {
                it = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/" +
                                "details?id=" + packageName));
                startActivity(it);
            }
        }
        dismiss();
    }
}
