package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
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


public class ConfigWeb extends AppCompatActivity {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private EditText edtChave;
    private Button btsalvhost;
    public SharedPreferences prefs;
    public String ChaveAcesso;
    ProgressDialog DialogECB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_configweb);

        btsalvhost = (Button) findViewById(R.id.btsalvhost);
        edtChave = (EditText) findViewById(R.id.edthost);

        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        ChaveAcesso = prefs.getString("ChaveAcesso", null);

        if (ChaveAcesso != null) {
            edtChave.setText(ChaveAcesso);
        }

        btsalvhost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogECB = new ProgressDialog(ConfigWeb.this);
                DialogECB.setTitle("Aguarde");
                DialogECB.setMessage("Validando licença...");
                DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                DialogECB.setIcon(R.drawable.icon_sync);
                DialogECB.show();

                String RetHost = null;
                if (edtChave.getText().length() == 0) {
                    DialogECB.dismiss();
                    edtChave.setError("Digite o caminho do host!");
                    edtChave.requestFocus();
                    return;
                }else{
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "CarregaHostCliente");
                    soap.addProperty("Chave",edtChave.getText().toString());
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.setOutputSoapObject(soap);
                    HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSHOST);

                    try {
                        Boolean ConexOk = Util.checarConexaoCelular(ConfigWeb.this);
                        if (ConexOk == true) {
                            Envio.call("", envelope);
                            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                            RetHost = (String) envelope.getResponse();
                            System.out.println("Response :" + resultsRequestSOAP.toString());

                            if (RetHost.equals("0")){
                                DialogECB.cancel();
                                Toast.makeText(ConfigWeb.this, "Host não encontrado!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }else {
                            DialogECB.cancel();
                            Toast.makeText(ConfigWeb.this, "Sem conexão com a internet. Verifique!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        DialogECB.dismiss();
                        System.out.println("Error" + e);
                    }
                }
                if (RetHost == null) {
                    DialogECB.cancel();
                    Toast.makeText(ConfigWeb.this, "Não foi possível validar a licença. Verifique!", Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogECB.dismiss();
                SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
                editorhost.putString("ChaveAcesso", edtChave.getText().toString());
                editorhost.putString("host", RetHost);
                editorhost.apply();
                Toast.makeText(ConfigWeb.this, "Host Salvo com Sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}