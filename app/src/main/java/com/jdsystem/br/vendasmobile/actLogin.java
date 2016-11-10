package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class actLogin extends AppCompatActivity implements Runnable {
    public static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String METHOD_NAME = "Login";

    private GoogleApiClient client;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGB_565);
    }

    public boolean onCreateOptionMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_act_principal, menu);
        return true;
    }

    private EditText edtUsuario;
    private EditText edtSenha;
    private Button btnEntrar;
    private CheckBox cbGravSenha;
    private ProgressDialog Dialogo;
    private Handler handler = new Handler();
    public String Retorno = "0";
    public SharedPreferences prefs;
    public String usuario;
    public String senha;
    public String URLPrincipal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_act_login);

        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        edtUsuario = (EditText) findViewById(R.id.edtUsuario);
        edtSenha = (EditText) findViewById(R.id.edtSenha);
        cbGravSenha = (CheckBox) findViewById(R.id.cbGravSenha);

        prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);
        if (usuario != null) {
            edtUsuario.setText(usuario);
        }
        if (senha != null) {
            edtSenha.setText(senha);
            cbGravSenha.setChecked(true);
        }

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUsuario.getText().length() == 0) {
                    edtUsuario.setError("Digite o nome do Usuário!");
                    edtUsuario.requestFocus();
                    return;

                } else if (edtSenha.getText().length() == 0) {
                    edtSenha.setError("Digite a Senha!");
                    edtSenha.requestFocus();
                    return;
                }
                SharedPreferences.Editor editor = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE).edit();
                editor.putString("usuario", edtUsuario.getText().toString());
                if (cbGravSenha.isChecked()) {
                    editor.putString("senha", edtSenha.getText().toString());
                } else {
                    editor.putString("senha", null);
                }
                editor.commit();

                Dialogo = new ProgressDialog(actLogin.this);
                Dialogo.setIndeterminate(true);
                Dialogo.setMessage("Verificando Usuário");
                Dialogo.setTitle("Aguarde");
                Dialogo.show();

                Thread thread = new Thread(actLogin.this);
                thread.start();
            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        if (URLPrincipal == null) {
            Intent intent = new Intent(getApplicationContext(), ConfigWeb.class);
            startActivity(intent);
        }
    }

    public boolean VerificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
        builder.setTitle(R.string.app_namesair);
        builder.setIcon(R.drawable.logo_ico);
        builder.setMessage("Deseja realmente sair?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
                                 URLPrincipal = prefs.getString("host", null);

                                 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                 StrictMode.setThreadPolicy(policy);

                                 SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                 soap.addProperty("aUsuario", edtUsuario.getText().toString());
                                 soap.addProperty("aSenha", edtSenha.getText().toString());
                                 SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                 envelope.setOutputSoapObject(soap);
                                 HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                                 String CodVendedor;
                                 try {
                                     Boolean ConexOk = VerificaConexao();
                                     if (ConexOk == true) {
                                         Envio.call("", envelope);

                                         SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                         CodVendedor = (String) envelope.getResponse();
                                         System.out.println("Response::" + resultsRequestSOAP.toString());
                                         if (CodVendedor != "0") {
                                             Intent intent = new Intent(getApplicationContext(), actListPedidos.class);
                                             Bundle params = new Bundle();
                                             params.putString("codvendedor", Retorno);
                                             params.putString("urlPrincipal", URLPrincipal);
                                             intent.putExtras(params);
                                             startActivity(intent);
                                         } else {
                                             Dialogo.dismiss();
                                             Toast.makeText(actLogin.this, "Usuário ou Senha inválidos!", Toast.LENGTH_LONG).show();
                                             return;
                                         }
                                     } else {
                                         Dialogo.dismiss();
                                         AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
                                         builder.setTitle(R.string.app_namesair);
                                         builder.setIcon(R.drawable.logo_ico);
                                         builder.setMessage("Sem Conexão com a Internet, Verifique!")
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
                                         AlertDialog alert = builder.create();
                                         alert.show();
                                     }
                                 } catch (Exception E) {

                                 }
                             } catch (Exception E) {

                             }
                         }
                     }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_act_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.act_configweb) {
            Intent intent = new Intent(getApplicationContext(), ConfigWeb.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("actLogin Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
