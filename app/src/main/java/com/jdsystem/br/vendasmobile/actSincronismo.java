package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.Thread.sleep;

public class actSincronismo extends AppCompatActivity implements Runnable {

    Handler hd;
    ProgressBar PrgGeral;
    int it;
    ProgressDialog Dialog;
    Handler progressHandler;
    SQLiteDatabase DB;
    Button btnSinc;
    String usuario, senha, sCodVend, URLPrincipal;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_sincronismo);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
            }
        }
        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

        btnSinc = (Button) findViewById(R.id.btnSincronizar);

        btnSinc.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Dialog = new ProgressDialog(actSincronismo.this);
                                           Dialog.setTitle("Aguarde...");
                                           Dialog.setMessage("Sincronizando Tabelas");
                                           Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                           Dialog.setProgress(0);
                                           Dialog.setMax(0);
                                           Dialog.show();

                                           hd = new Handler();
                                           Thread td = new Thread(actSincronismo.this);
                                           td.start();
                                       }
                                   }
        );
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void run() {

        Cursor CursosParam = DB.rawQuery(" SELECT DT_ULT_ATU FROM PARAMAPP ", null);
        CursosParam.moveToFirst();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date DataUlt = null;
        try {
            if (CursosParam.getCount() > 0) {
                DataUlt = sdf.parse(CursosParam.getString(0));
            }else{
                DataUlt = sdf.parse("01/01/2000 12:20:30");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        String DataUlt2 = sdf.format(DataUlt);

        SharedPreferences prefs = getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);
        DB.execSQL(" UPDATE PARAMAPP SET DT_ULT_ATU = " + "DATETIME();");

        SincronizarClientes(sCodVend, "9D51E0508F3B9D59F252", "0B65", DataUlt2);

        DB.execSQL(" UPDATE PARAMAPP SET DT_ULT_ATU = " + "DATETIME();");
    }



    public void SincronizarClientes(String sCodVend, String nUsuario, String nSenha, String DtUlt) {
        String METHOD_NAME = "Carregar";
        String TAG_CLIENTESINFO = "clientes";

        String TAG_CODIGO = "codigo";
        String TAG_RAZAOSOCIAL = "razao_social";
        String TAG_NOMEFANTASIA = "nome_fantasia";
        String TAG_TIPO = "tipo";
        String TAG_CNPJCPF = "cnpj_cpf";
        String TAG_INSCESTADUAL = "inscricao_estadual";
        String TAG_LOGRADOURO = "Logradouro";
        String TAG_NUMERO = "numero";
        String TAG_COMPLEMENTO = "complemento";
        String TAG_BAIRRO = "bairro";
        String TAG_CIDADE = "cidade";
        String TAG_ESTADO = "estado";
        String TAG_CEP = "cep";
        String TAG_OBS = "observacao";
        String TAG_EMAILS = "emails";
        String TAG_ATIVO = "ativo";
        String TAG_TELEFONES = "telefones";
        String TAG_NOMEROTEL = "numero";

        String CodVendedor = sCodVend;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        //soap.addProperty("aParam", "V" + CodVendedor + "%01/01/2016 12:20:30");
        soap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
        soap.addProperty("aUsuario", nUsuario);
        soap.addProperty("aSenha", nSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);
        String RetClientes = null;

        try {
            Boolean ConexOk = true;
            //Boolean ConexOk = actLogin.VerificaConexao();
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetClientes = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            String SHA1Ret = RetClientes.substring(0, 40);
            String ArrayClientes = RetClientes.substring(40, (RetClientes.length()));

            JSONObject jsonObj = new JSONObject(ArrayClientes);
            JSONArray pedidosblq = jsonObj.getJSONArray(TAG_CLIENTESINFO);

            int jumpTime = 0;
            final int totalProgressTime = pedidosblq.length();
            Dialog.setMax(totalProgressTime);

            for (int i = 0; i < pedidosblq.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        //sleep(200);
                        JSONObject c = pedidosblq.getJSONObject(jumpTime);
                        jumpTime += 1;
                        Dialog.setProgress(jumpTime);

                        Cursor cursor = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                                " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                                " CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO FROM CLIENTES WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'", null);

                        String CodEstado = RetornaEstado(c.getString(TAG_ESTADO));
                        int CodCidade = RetornaCidade(c.getString(TAG_CIDADE), CodEstado);
                        int CodBairro = RetornaBairro(c.getString(TAG_BAIRRO), CodCidade);

                        try {
                            if (cursor.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + c.getString(TAG_RAZAOSOCIAL) +
                                        "', NOMEFAN = '" + c.getString(TAG_NOMEFANTASIA) +
                                        "', INSCREST = '" + c.getString(TAG_INSCESTADUAL) + "', EMAIL = '" + c.getString(TAG_EMAILS) +
                                        "', TEL1 = '" + c.getString(TAG_NOMEROTEL) + "', TEL2 = '', ENDERECO = '" + c.getString(TAG_LOGRADOURO) +
                                        "', NUMERO = '" + c.getString(TAG_NUMERO) + "', COMPLEMENT = '" + c.getString(TAG_COMPLEMENTO) +
                                        "', CODBAIRRO = '" + CodBairro + "', OBS = '" + c.getString(TAG_OBS) + "', CODCIDADE = '" + CodCidade + "', UF = '" + CodEstado +
                                        "', CEP = '" + c.getString(TAG_CEP) + "',CODCLIE_EXT = '" + c.getString(TAG_CODIGO) + "', " +
                                        " TIPOPESSOA = '" + c.getString(TAG_TIPO) + "', ATIVO = '" + c.getString(TAG_ATIVO) + "'" +
                                        "', CODVENDEDOR = '" + CodVendedor + "'" +
                                        " WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'");
                            } else {
                                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                                        "CEP,CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO) VALUES(" +
                                        "'" + c.getString(TAG_CNPJCPF) + "','" + c.getString(TAG_RAZAOSOCIAL) +
                                        "',' " + c.getString(TAG_NOMEFANTASIA) + "',' " + c.getString(TAG_INSCESTADUAL) + "',' " + c.getString(TAG_EMAILS) +
                                        "',' " + c.getString(TAG_NOMEROTEL) + "', '', '" + c.getString(TAG_LOGRADOURO) +
                                        "',' " + c.getString(TAG_NUMERO) + "', '" + c.getString(TAG_COMPLEMENTO) +
                                        "','" + CodBairro + "',' " + c.getString(TAG_OBS) + "','" + CodCidade + "',' " + CodEstado +
                                        "',' " + c.getString(TAG_CEP) + "', '" + c.getString(TAG_CODIGO) +
                                        "','" + CodVendedor + "','" + c.getString(TAG_TIPO) + "','" + c.getString(TAG_ATIVO) + "');");
                            }
                            cursor.close();
                        } catch (Exception E) {
                            System.out.println("Error" + E);
                        }
                    } catch (Exception E) {//InterruptedException e) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Dialog.isShowing())
            Dialog.dismiss();
    }


    private int RetornaBairro(String NomeBairro, int CodCidade) {
        int Bairro = 0;
        try {
            Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, CODCIDADE, DESCRICAO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
            if (CursorBairro.getCount() > 0) {
                CursorBairro.moveToFirst();
                Bairro = CursorBairro.getInt(CursorBairro.getColumnIndex("CODBAIRRO"));
            } else {
                DB.execSQL(" INSERT INTO BAIRROS (CODCIDADE, DESCRICAO )" +
                        " VALUES('" + CodCidade + "','" + NomeBairro + "');");
                Cursor CursorBairro2 = DB.rawQuery(" SELECT CODBAIRRO, CODCIDADE, DESCRICAO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
                CursorBairro2.moveToFirst();
                Bairro = CursorBairro2.getInt(CursorBairro2.getColumnIndex("CODBAIRRO"));
                CursorBairro2.close();
            }
            CursorBairro.close();
            return Bairro;
        } catch (Exception E) {
            return Bairro;
        }
    }

    private int RetornaCidade(String NomeCidade, String NomeEstado) {
        int Cidade = 0;
        try {
            Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
            if (CursorCidade.getCount() > 0) {
                CursorCidade.moveToFirst();
                Cidade = CursorCidade.getInt(CursorCidade.getColumnIndex("CODCIDADE"));
            } else {
                DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF)" +
                        " VALUES('" + NomeCidade + "','" + NomeEstado + "');");
                Cursor CursorCidade2 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                CursorCidade2.moveToFirst();
                Cidade = CursorCidade2.getInt(CursorCidade2.getColumnIndex("CODCIDADE"));
                CursorCidade2.close();
            }
            CursorCidade.close();
            return Cidade;
        } catch (Exception E) {
            return Cidade;
        }
    }

    private String RetornaEstado(String NomeEstado) {
        String Estado = null;
        try {
            Cursor CursosEstado = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + NomeEstado + "'", null);
            if (CursosEstado.getCount() > 0) {
                CursosEstado.moveToFirst();
                Estado = CursosEstado.getString(CursosEstado.getColumnIndex("UF"));
            } else {
                DB.execSQL("INSERT INTO ESTADOS VALUES('" + NomeEstado + "','" + NomeEstado + "');");
                Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + NomeEstado + "'", null);
                cursor1.moveToFirst();
                Estado = cursor1.getString(cursor1.getColumnIndex("UF"));
                cursor1.close();
            }
            CursosEstado.close();


            return Estado;
        } catch (Exception E) {
            return null;
        }
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("actSincronismo Page") // TODO: Define a title for the content shown.
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
