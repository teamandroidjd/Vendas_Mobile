package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class act_TH_obscontato extends Fragment {
    String  obsContato,codVendedor,URLPrincipal,usuario,senha;
    int sCodContato;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    public SharedPreferences prefs;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    int idPerfil;
    TextView TAG_OBSCONTATO;
    LinearLayout linearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_obs_contato, container, false);
        ctx = getContext();

        prefs = ctx.getSharedPreferences(CONFIG_HOST, ctx.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        DB = new ConfigDB(ctx).getReadableDatabase();

        TAG_OBSCONTATO = (TextView) v.findViewById(R.id.txt_obs_contatos);
        linearLayout = (LinearLayout) v.findViewById(R.id.ll_obs);

        //Intent intent = act.getIntent();
        Intent intent = ((DadosContato) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodContato = params.getInt("codContato");
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }

        FloatingActionButton fabCadProdCont = (FloatingActionButton) v.findViewById(R.id.cad_produtos_contatos);
        fabCadProdCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        carregaObsContato();

        return v;
    }

    private void carregaObsContato(){
        try {
            Cursor CursorClie = DB.rawQuery(" SELECT CONTATO.OBS, CONTATO.CODPERFIL " +
                    "FROM CONTATO " +
                    "LEFT OUTER JOIN CLIENTES ON CONTATO.CODCLIENTE = CLIENTES.CODCLIE_INT " +
                    "WHERE CODCONTATO_INT = '" + sCodContato + "' AND CONTATO.CODPERFIL = "+idPerfil, null);

            if (CursorClie.getCount() > 0) {
                CursorClie.moveToFirst();

                obsContato = CursorClie.getString(CursorClie.getColumnIndex("OBS"));

                if ((obsContato == null) || obsContato.equals("")) {
                    TAG_OBSCONTATO.setText("Nenhuma observação para este contato!");
                } else {
                    TAG_OBSCONTATO.setText("Observações\n" + obsContato);
                }

                CursorClie.close();
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
