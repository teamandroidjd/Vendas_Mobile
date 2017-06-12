package com.jdsystem.br.vendasmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class TH_ObsContato extends Fragment {
    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    String obsContato, codVendedor, URLPrincipal, usuario, senha;
    int sCodContato;
    SQLiteDatabase DB;
    int idPerfil;
    TextView TAG_OBSCONTATO, dlgObsContato;
    EditText obsDlgContato;
    LinearLayout linearLayout;
    private Context ctx;
    private Activity act;
    private AlertDialog alerta;

    private static AlertDialog alteraObsContato(View vi, Context ctx) {

        @SuppressLint("InflateParams") View fragView = (LayoutInflater.from(ctx)).inflate(R.layout.input_obs_contato, null);
        EditText obsDlgContato = (EditText) vi.findViewById(R.id.inputobscontato);

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);
        alertBuilder.setView(fragView);
        alertBuilder.setCancelable(true)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancelar", null)
                .setView(fragView);

        final AlertDialog mAlertDialog = alertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                mAlertDialog.show();
            }
        });
        return mAlertDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_obs_contato, container, false);
        ctx = getContext();

        prefs = ctx.getSharedPreferences(CONFIG_HOST, ctx.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        DB = new ConfigDB(ctx).getReadableDatabase();

        TAG_OBSCONTATO = (TextView) v.findViewById(R.id.txt_obs_contatos);
        TAG_OBSCONTATO.setMovementMethod(new ScrollingMovementMethod());
        linearLayout = (LinearLayout) v.findViewById(R.id.ll_obs);

        //Intent intent = act.getIntent();
        Intent intent = ((DadosContato) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodContato = params.getInt(getString(R.string.intent_codcontato));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }

        FloatingActionButton fabCadProdCont = (FloatingActionButton) v.findViewById(R.id.cad_obs_contato);
        fabCadProdCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("InflateParams") View fragView = (LayoutInflater.from(ctx)).inflate(R.layout.input_obs_contato, null);
                obsDlgContato = (EditText) fragView.findViewById(R.id.inputobscontato);
                carregaObsContato();
                obsDlgContato.setText(obsContato);

                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);
                alertBuilder.setView(fragView);
                alertBuilder.setCancelable(false)
                        .setPositiveButton("Ok", null)
                        .setNegativeButton("Cancelar", null)
                        .setView(fragView);

                final AlertDialog mAlertDialog = alertBuilder.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                salvarObsAlterada();
                                carregaObsContato();
                                if (mAlertDialog.isShowing()) {
                                    mAlertDialog.dismiss();
                                }
                            }
                        });
                        Button btnCancelar = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                builder.setMessage("Deseja cancelar esta alteração?");
                                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (alerta.isShowing()) {
                                            alerta.dismiss();
                                        }
                                        if (mAlertDialog.isShowing()) {
                                            mAlertDialog.dismiss();
                                        }
                                    }
                                })
                                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (alerta.isShowing()) {
                                                    alerta.dismiss();
                                                }
                                            }
                                        });
                                alerta = builder.create();
                                alerta.show();
                            }
                        });
                        mAlertDialog.show();
                    }
                });
                mAlertDialog.show();
            }
        });
        carregaObsContato();
        //dlgObsContato = (TextView) v.findViewById(R.id.lblNomeFanClie);
        return v;
    }

    private void carregaObsContato() {
        try {
            Cursor CursorClie = DB.rawQuery(" SELECT CONTATO.OBS, CONTATO.CODPERFIL " +
                    "FROM CONTATO " +
                    "LEFT OUTER JOIN CLIENTES ON CONTATO.CODCLIENTE = CLIENTES.CODCLIE_INT " +
                    "WHERE CODCONTATO_INT = '" + sCodContato + "' AND CONTATO.CODPERFIL = " + idPerfil, null);

            if (CursorClie.getCount() > 0) {
                CursorClie.moveToFirst();

                obsContato = CursorClie.getString(CursorClie.getColumnIndex("OBS"));

                if ((obsContato == null) || obsContato.equals("")) {
                    TAG_OBSCONTATO.setText("Nenhuma observação para este contato!");
                } else {
                    TAG_OBSCONTATO.setText("Observações: \n" + obsContato);
                }

                CursorClie.close();
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void recuperaObs() {
        Cursor cursor = DB.rawQuery("select contato.obs, contato.codcontato_int, contato.codperfil " +
                "from contato " +
                "where contato.codinterno = " + sCodContato, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            obsContato = cursor.getString(cursor.getColumnIndex("contato.obs"));
            cursor.close();
        }
    }

    private void salvarObsAlterada() {
        try {
            DB.execSQL("update contato set obs = '" + obsDlgContato.getText().toString() + "' " +
                    "where contato.codcontato_int = " + sCodContato + " and contato.codperfil = " + idPerfil);
        } catch (Exception E) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(getContext(), "Houve um problema ao alterar a observação do cliente. " +
                        "Entre em contato com a JD System", Toast.LENGTH_SHORT);
            }
            E.toString();
        }


    }

}

//txt_obs_contatos