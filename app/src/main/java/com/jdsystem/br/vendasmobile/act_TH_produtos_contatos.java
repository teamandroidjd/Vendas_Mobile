package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class act_TH_produtos_contatos extends Fragment {
    int sCodCliente;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_produtos_contatos,container,false);
        ctx = getContext();

        return v;
    }
}
