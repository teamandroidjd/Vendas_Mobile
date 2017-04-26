package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;


public class ConfigWeb extends AppCompatActivity implements Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private EditText edtChave;
    private Button btsalvhost;
    public SharedPreferences prefs;
    public String ChaveAcesso, RetHost,host;
    public int idPerfil;
    ProgressDialog DialogECB;
    private SQLiteDatabase DB;
    private Handler hd = new Handler();
    TextView licenca1, licenca2, licenca3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_configweb);

        declaraobjetos();
        carregarpreferencias();


        if (ChaveAcesso != null) {
            edtChave.setText(ChaveAcesso);
        } else {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        carregarlicencas();
    }

    public void SalvarHost(View view) {
        if (edtChave.getText().length() == 0) {
            edtChave.setError(getString(R.string.enter_host));
            edtChave.requestFocus();
            return;
        }
        DialogECB = new ProgressDialog(ConfigWeb.this);
        DialogECB.setTitle(getString(R.string.wait));
        DialogECB.setMessage(getString(R.string.valid_license));
        DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.show();

        Thread td = new Thread(ConfigWeb.this);
        td.start();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        ChaveAcesso = prefs.getString(getString(R.string.password), null);
    }

    private void declaraobjetos() {
        licenca1 = (TextView) findViewById(R.id.txtlicenca1);
        licenca2 = (TextView) findViewById(R.id.txtlicenca2);
        licenca3 = (TextView) findViewById(R.id.txtlicenca3);
        DB = new ConfigDB(this).getReadableDatabase();
        btsalvhost = (Button) findViewById(R.id.btsalvhost);
        edtChave = (EditText) findViewById(R.id.edthost);
    }

    @Override
    public void run() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "CarregaHostCliente");
        soap.addProperty("Chave", edtChave.getText().toString());
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSHOST);
        RetHost = null;
        Boolean ConexOk = Util.checarConexaoCelular(ConfigWeb.this);
        if (ConexOk == true) {
            try {
                Envio.call("", envelope);
            } catch (Exception e) {
                e.toString();
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConfigWeb.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                return;
            }
            try {
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                RetHost = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            } catch (Exception e) {
                e.toString();
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConfigWeb.this, R.string.failed_return, Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                return;
            }
            if (RetHost.equals("{\"perfil\":[0]}")) {
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder validuser = new AlertDialog.Builder(ConfigWeb.this);
                        validuser.setTitle(R.string.app_namesair);
                        validuser.setIcon(R.drawable.logo_ico);
                        validuser.setMessage(R.string.not_validate_license)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                });
                        AlertDialog alert = validuser.create();
                        alert.show();
                    }
                });
                return;
            } else if (RetHost == null) {
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder validuser = new AlertDialog.Builder(ConfigWeb.this);
                        validuser.setTitle(R.string.app_namesair);
                        validuser.setIcon(R.drawable.logo_ico);
                        validuser.setMessage(R.string.not_valid_license_failure_communicate)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                });
                        AlertDialog alert = validuser.create();
                        alert.show();
                    }
                });
                return;
            }
        } else {
            DialogECB.dismiss();
            hd.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder validuser = new AlertDialog.Builder(ConfigWeb.this);
                    validuser.setTitle(R.string.app_namesair);
                    validuser.setIcon(R.drawable.logo_ico);
                    validuser.setMessage(R.string.no_connection)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            })
                            .setNegativeButton("Configurações", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                    startActivity(intent);
                                }
                            });

                    AlertDialog alert = validuser.create();
                    alert.show();
                }
            });
            return;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetHost);
            JSONArray infolicenca = jsonObj.getJSONArray("perfil");
            JSONObject c = infolicenca.getJSONObject(0);
            String licenca = c.getString("codlicenca");
            String perfil = c.getString("nomeempresa");
            host = c.getString("host");

         if ((gravarperfil(host, perfil, licenca)) == false){
             return;
         }
        } catch (Exception e) {
            e.toString();
        }
        DialogECB.dismiss();
        gravarpreferencias();
        hd.post(new Runnable() {
            public void run() {
                Toast.makeText(ConfigWeb.this, R.string.license_validated, Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    private void gravarpreferencias() {
        try {
            Cursor idlicenca = DB.rawQuery("SELECT LICENCA,CODPERFIL FROM PERFIL WHERE LICENCA = '" + edtChave.getText().toString() + "'", null);
            idlicenca.moveToFirst();
            if (idlicenca.getCount() > 0) {
                idPerfil = idlicenca.getInt(idlicenca.getColumnIndex("CODPERFIL"));
            }
        }catch (Exception e){
            e.toString();
        }
        SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
        editorhost.putString("ChaveAcesso", edtChave.getText().toString());
        editorhost.putString("host", host);
        editorhost.putInt("idperfil",idPerfil);
        editorhost.apply();
    }

    private boolean gravarperfil(String host, String perfil, String licenca) {
        try {
            Cursor cursorlicenca = DB.rawQuery("SELECT LICENCA,NOMEPERFIL FROM PERFIL WHERE LICENCA = '" + edtChave.getText().toString() + "'", null);
            cursorlicenca.moveToFirst();
            if (cursorlicenca.getCount() > 0) {
                final String nomePerfil = cursorlicenca.getString(cursorlicenca.getColumnIndex("NOMEPERFIL")).trim();
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConfigWeb.this, "Licença já registrada para " + nomePerfil + ". Verifique!", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                DB.execSQL("INSERT INTO PERFIL(LICENCA,HOST,NOMEPERFIL) VALUES('" + edtChave.getText().toString() + "','" + host + "','" + perfil + "');");
                return true;
            }

        } catch (Exception e) {
            e.toString();
        }
        return false;
    }

    private void carregarlicencas(){
        String nomeperfil1 = null;
        String nomeperfil2 = null;
        String nomeperfil3 = null;
        List<String> DadosListPerfil = new ArrayList<String>();
        Cursor cursorPerfil = DB.rawQuery("SELECT * FROM PERFIL", null);
        cursorPerfil.moveToFirst();
        if (cursorPerfil.getCount() > 0) {

            do {
                DadosListPerfil.add(cursorPerfil.getString(cursorPerfil.getColumnIndex("NOMEPERFIL"))+" licença "+cursorPerfil.getString(cursorPerfil.getColumnIndex("LICENCA")));
            } while (cursorPerfil.moveToNext());
            int i = DadosListPerfil.size();
            switch (i){
                case 1:
                    nomeperfil1 = DadosListPerfil.get(0);
                    licenca1.setText("Aplicativo registrado para a "+nomeperfil1);
                    licenca2.setVisibility(View.GONE);
                    licenca3.setVisibility(View.GONE);
                    edtChave = null;

                    break;
                case 2:
                    nomeperfil1 = DadosListPerfil.get(0);
                    licenca1.setText("Aplicativo registrado para a "+nomeperfil1);
                    nomeperfil2 = DadosListPerfil.get(1);
                    licenca2.setText("Aplicativo registrado para a "+nomeperfil2);
                    licenca3.setVisibility(View.GONE);
                    break;
                case 3:
                    nomeperfil1 = DadosListPerfil.get(0);
                    licenca1.setText("Aplicativo registrado para a "+nomeperfil1);
                    nomeperfil2 = DadosListPerfil.get(1);
                    licenca2.setText("Aplicativo registrado para a "+nomeperfil2);
                    nomeperfil3 = DadosListPerfil.get(2);
                    licenca3.setText("Aplicativo registrado para a "+nomeperfil3);
            }

        }
    }

}