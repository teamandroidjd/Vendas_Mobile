package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class act_TH_horarios_contatos extends Fragment {
    int sCodCliente;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_horarios_contato,container,false);
        ctx = getContext();


        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView TAG_HORARIOSCLIENTE = (TextView) v.findViewById(R.id.txt_horarios_contatos);
        return v;
    }
}
