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
 * Created by eduardo.costa on 10/11/2016.
 */


public class act_TH_obsclie extends Fragment {

    int sCodCliente;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_obs_cliente,container,false);
        ctx = getContext();


        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView TAG_OBSCLIENTE = (TextView) v.findViewById(R.id.txt_obs_clientes);


        //Intent intent = act.getIntent();
        Intent intent = ((actDadosCliente) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodCliente = params.getInt(getString(R.string.intent_codcliente));
            }
        }

        try {
            Cursor CursorClie = DB.rawQuery(" SELECT OBS FROM CLIENTES " +
                    " WHERE CODCLIE_INT = " + sCodCliente, null);

            if (CursorClie.getCount() > 0) {
                CursorClie.moveToFirst();
                do {
                    TAG_OBSCLIENTE.setText("Observações: "+CursorClie.getString(CursorClie.getColumnIndex("OBS")));

                }
                while (CursorClie.moveToNext());
                CursorClie.close();
            }
        }catch (Exception E){
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
        return v;
    }

}

