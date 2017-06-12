package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class TH_ObsClie extends Fragment {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    String CodCliente, codVendedor, URLPrincipal, usuario, senha;
    SQLiteDatabase DB;
    int idPerfil;
    private Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_obs_cliente, container, false);
        Context ctx = getContext();

        prefs = ctx.getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView TAG_OBSCLIENTE = (TextView) v.findViewById(R.id.txt_obs_clientes);


        //Intent intent = act.getIntent();
        Intent intent = ((DadosCliente) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                CodCliente = params.getString(getString(R.string.intent_codcliente));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));

            }
        }
        try {
            Cursor CursorClie = DB.rawQuery(" SELECT OBS, CODPERFIL FROM CLIENTES " +
                    " WHERE CODCLIE_INT = " + Integer.parseInt(CodCliente) + " AND CODPERFIL = " + idPerfil, null);

            if (CursorClie.getCount() > 0) {
                CursorClie.moveToFirst();
                do {
                    TAG_OBSCLIENTE.setText("Observações: " + CursorClie.getString(CursorClie.getColumnIndex("OBS")));

                }
                while (CursorClie.moveToNext());
                CursorClie.close();
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
        return v;
    }

}

