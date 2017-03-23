package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class ConfigWeb extends AppCompatActivity implements Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private EditText edtChave;
    private Button btsalvhost;
    public SharedPreferences prefs;
    public String ChaveAcesso;
    ProgressDialog DialogECB;
    private Handler hd = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_configweb);

        declaraobjetos();
        carregarpreferencias();

        if (ChaveAcesso != null) {
            edtChave.setText(ChaveAcesso);
        }
    }
    public void SalvarHost(View view){
        if (edtChave.getText().length() == 0) {
            edtChave.setError("Digite o caminho do host!");
            edtChave.requestFocus();
            return;
        }
        DialogECB = new ProgressDialog(ConfigWeb.this);
        DialogECB.setTitle("Aguarde");
        DialogECB.setMessage("Validando licença...");
        DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.show();

        Thread td = new Thread(ConfigWeb.this);
        td.start();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        ChaveAcesso = prefs.getString("ChaveAcesso", null);
    }

    private void declaraobjetos() {
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
        String RetHost = null;
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
                        Toast.makeText(ConfigWeb.this, "Falha de comunicação com o servidor, tente novamente!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ConfigWeb.this, "Falha no retorno da informações do servidor, tente novamente!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                return;
            }
            if (RetHost.equals("0")) {
                DialogECB.dismiss();
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder validuser = new AlertDialog.Builder(ConfigWeb.this);
                        validuser.setTitle(R.string.app_namesair);
                        validuser.setIcon(R.drawable.logo_ico);
                        validuser.setMessage("Não foi possivel validar a licença. Tente novamente!")
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
                        validuser.setMessage("Não foi possível validar a licença devido a uma falha de comunicação com o servidor. Tente novamente!")
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
                    validuser.setMessage("Sem conexão com a internet. Verifique!")
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
        DialogECB.dismiss();
        SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
        editorhost.putString("ChaveAcesso", edtChave.getText().toString());
        editorhost.putString("host", RetHost);
        editorhost.apply();
        hd.post(new Runnable() {
            public void run() {
                Toast.makeText(ConfigWeb.this, "Licença validada com sucesso", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}