package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Sincronismo extends AppCompatActivity implements Runnable, NavigationView.OnNavigationItemSelectedListener {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    public static final String METHOD_CONTATOENVIO = "CadastrarContato";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public static final String METHOD_NAME_AGENDA = "ENVIAR_AGENDA";
    private static SQLiteDatabase DB;
    private static String usuario,senha,codVendedor,URLPrincipal;
    //private static Context ctx;
    private static BaseFont bfBold;
    private static BaseFont bf;
    private static int pageNumber = 0;
    public SharedPreferences prefs;
    Handler hd;
    //Button btnSinc;
    Toolbar toolbar;
    private int idPerfil;
    private ProgressDialog Dialog, DialogECB;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sincronismo);
        declaraObjetos();
        carregaPreferencias();
        carregaUsuarioLogado();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(Sincronismo.this);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }
    }

    @Override
    public void run() {

        Configuration configuration = getResources().getConfiguration();

        if (Dialog.isShowing() && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try {
            sincronizaParametros(usuario, senha, this, Dialog, DialogECB, hd);
            sincronizaClientes(codVendedor, this, usuario, senha, 0, Dialog, DialogECB, hd);
            sincronizaContatosOutros(this,usuario,senha,0,Dialog,DialogECB,hd);
            sincronizaProdutos(this, usuario, senha, 0, Dialog, DialogECB, hd);
            sincronizaDescricaoTabelas(usuario, senha, this, Dialog, DialogECB, hd);
            sincronizaBloqueios(usuario, senha, this, Dialog, DialogECB, hd);
            sincronizaClientesEnvio("0", this, usuario, senha, Dialog, DialogECB, hd);
            sincronizaPedidosEnvio(usuario, senha, this, "0", Dialog, DialogECB, hd);
            sincronizaContatosEnvio(this, usuario, senha, DialogECB, Dialog, hd);
            sincronizaFormasPagamento(usuario, senha, this, Dialog, DialogECB, hd);
        } catch (Exception e) {
            e.toString();
            Dialog.dismiss();
            hd.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Sincronismo.this, "Falha no processo de sincronização. Tente novamente!", Toast.LENGTH_SHORT).show();
                }
            });

        }
        Intent i = new Intent(Sincronismo.this, ConsultaPedidos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        i.putExtras(params);
        startActivity(i);
        finish();
    }

    protected void sincronizar(View view) {
        hd = new Handler();
        DialogECB = new ProgressDialog(Sincronismo.this);
        Dialog = new ProgressDialog(Sincronismo.this);
        Dialog.setTitle(R.string.wait);
        Dialog.setMessage("");
        Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        Dialog.setProgress(0);
        Dialog.setIcon(R.drawable.icon_sync);
        Dialog.setMax(0);
        Dialog.setCancelable(false);
        Dialog.show();
        Boolean ConexOk = Util.checarConexaoCelular(Sincronismo.this);

        if (ConexOk) {
            Thread td = new Thread(Sincronismo.this);
            td.start();
        } else {
            Dialog.cancel();
            AlertDialog.Builder builder = new AlertDialog.Builder(Sincronismo.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage(R.string.msg_no_connection_sinc)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = (Sincronismo.this).getIntent();
                            (Sincronismo.this).finish();
                            startActivity(intent);
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

    }

    public static String sincronizaParametros(String sUsuario, String sSenha, final Context ctxEnv, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        String sincparaetrosstatic = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "RetornaParametros");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS, 10000);
        String RetParamApp = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk) {

                int i = 0;
                do {

                    try {
                        if (i > 0) {
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (ConexOk) {

                            try {
                                if (i == 0) {
                                    Envio.call("", envelope);

                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                    RetParamApp = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                } else {
                                    SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "RetornaParametros");
                                    newsoap.addProperty("aUsuario", usuario);
                                    newsoap.addProperty("aSenha", senha);
                                    SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                    newenvelope.setOutputSoapObject(newsoap);
                                    HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                                    newEnvio.call("", newenvelope);

                                    SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                    RetParamApp = (String) newenvelope.getResponse();
                                    System.out.println("Response :" + newresultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                e.toString();
                                sincparaetrosstatic = ctxEnv.getString(R.string.failure_communicate);
                            }
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                    i = i + 1;
                } while (RetParamApp == null && i <= 6);
            } else {
                sincparaetrosstatic = ctxEnv.getString(R.string.no_connection);
                return sincparaetrosstatic;
            }
            if (RetParamApp == null) {
                sincparaetrosstatic = ctxEnv.getString(R.string.failure_communicate);
                return sincparaetrosstatic;
            }
        } catch (Exception e) {
            e.toString();
            sincparaetrosstatic = ctxEnv.getString(R.string.failure_communicate);
            return sincparaetrosstatic;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetParamApp);
            JSONArray JParamApp = jsonObj.getJSONArray("param_app");

            int jumpTime = 0;
            int totalProgressTime = JParamApp.length();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JParamApp.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JParamApp.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxEnv.getString(R.string.updating_parameters));
                                }
                            });
                        }
                        Double PercDescMax = c.getDouble("percdescmaxped");
                        String habitemnegativo = c.getString("habitemnegativo").trim();
                        String habcritsitclie = c.getString("habcritsitclie").trim();
                        String habcritqtditens = c.getString("habcritqtditens").trim();
                        String habcliexvend = c.getString("habcliexvend").trim();
                        String habqtdminvenda = c.getString("habqtdminvenda").trim();
                        if (habqtdminvenda.equals("True")) {
                            habqtdminvenda = "S";
                        } else {
                            habqtdminvenda = "N";
                        }
                        String habaltprecovenda = c.getString("habaltprecovenda").trim();
                        if (habaltprecovenda.equals("False") || habaltprecovenda.equals("")) {
                            habaltprecovenda = "N";
                        } else {
                            habaltprecovenda = "S";
                        }
                        String tbminvenda = c.getString("tbminvenda").trim();
                        if (tbminvenda.equals(" ") || tbminvenda.equals("False")) {
                            tbminvenda = "0";
                        }
                        String tipopermissaocadclie = c.getString("tipopermissaocadclie").trim(); //1 - nenhum usuario pode cadastrar cliente. 2 - todos podem
                        //cadastrar clientes 3 - verificar a permissão de usuário se pode ou não cadastrar.


                        Cursor CursorParam = DB.rawQuery(" SELECT CODPERFIL,HABALTPRECOVENDA,VLMINVENDA,HABCLIEXVEND,HABCADASTRO_CLIE,HABCONTROLQTDMINVEND,PERCACRESC, HABITEMNEGATIVO, HABCRITSITCLIE, TIPOCRITICQTDITEM FROM PARAMAPP WHERE CODPERFIL = " + idPerfil + "", null);
                        CursorParam.moveToFirst();
                        if (CursorParam.getCount() > 0) {
                            if(!habcliexvend.equals(CursorParam.getString(CursorParam.getColumnIndex("HABCLIEXVEND")))){
                                try {
                                    DB.execSQL("UPDATE USUARIOS SET DT_ULT_ATU_CLIE = "+null+" WHERE CODPERFIL = " + idPerfil + " AND USUARIO = '" + usuario + "' AND SENHA = '" + senha + "'");
                                } catch (Exception e) {
                                    e.toString();
                                }
                            }
                            DB.execSQL(" UPDATE PARAMAPP SET PERCACRESC = '" + PercDescMax +
                                    "', HABITEMNEGATIVO = '" + habitemnegativo.trim() +
                                    "', HABCADASTRO_CLIE = '" + tipopermissaocadclie.trim() +
                                    "', HABCONTROLQTDMINVEND = '" + habqtdminvenda.trim() +
                                    "', HABCRITSITCLIE = '" + habcritsitclie.trim() +
                                    "', HABCLIEXVEND = '" + habcliexvend.trim() +
                                    "', CODPERFIL = '" + idPerfil +
                                    "', TIPOCRITICQTDITEM = '" + habcritqtditens.trim() +
                                    "', HABALTPRECOVENDA = '" + habaltprecovenda.trim() +
                                    "', VLMINVENDA = '" + tbminvenda.trim() +
                                    "' WHERE CODPERFIL = " + idPerfil);
                        } else {

                            DB.execSQL(" INSERT INTO PARAMAPP (PERCACRESC,HABALTPRECOVENDA,VLMINVENDA,HABCLIEXVEND,HABCADASTRO_CLIE," +
                                    "HABCONTROLQTDMINVEND,HABITEMNEGATIVO, HABCRITSITCLIE, CODPERFIL, TIPOCRITICQTDITEM)" +
                                    " VALUES(" + "'" + PercDescMax + "'," +
                                    " '" + habaltprecovenda.trim() + "'," +
                                    " '" + tbminvenda.trim() + "'," +
                                    " '" + habcliexvend.trim() + "'," +
                                    " '" + tipopermissaocadclie.trim() + "'," +
                                    " '" + habqtdminvenda.trim() + "'," +
                                    " '" + habitemnegativo.trim() + "'," +
                                    " '" + habcritsitclie.trim() + "'," +
                                    " '" + idPerfil + "'," +
                                    " '" + habcritqtditens.trim() + "');");
                        }
                        sincparaetrosstatic = "OK";
                        CursorParam.close();


                    } catch (Exception E) {
                        E.toString();
                    }
                }
                if (dialog != null) {
                    jumpTime = 0;
                    totalProgressTime = 0;
                    dialog.setProgress(jumpTime);
                    dialog.setMax(totalProgressTime);
                }
            }
        } catch (JSONException e) {
            e.toString();
            return sincparaetrosstatic;
        }
        return sincparaetrosstatic;
    }

    public static String sincronizaClientes(String sCodVend, final Context ctxEnvClie, String user, String pass, int Codclie, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxEnvClie.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

        String sinccliestatic = null;

        String METHOD_NAME = "Carregar";
        String TAG_CLIENTESINFO = "clientes";
        String TAG_TELEFONESINFO = "telefones";
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
        String TAG_RG = "identidade";
        String TAG_OBS = "observacao";
        String TAG_EMAILS = "emails";
        String TAG_ATIVO = "ativo";
        String TAG_BLOQUEIO = "bloqueio";
        String TAG_LIMITECRED = "limitecredito";

        String DtUlt = null;
        try {
            if (Codclie == 0) {
                Cursor cursorparamapp = DB.rawQuery("SELECT DT_ULT_ATU_CLIE FROM USUARIOS WHERE CODPERFIL = " + idPerfil + " AND USUARIO = '" + user + "' AND SENHA = '" + pass + "'", null);
                cursorparamapp.moveToFirst();

                DtUlt = cursorparamapp.getString(cursorparamapp.getColumnIndex("DT_ULT_ATU_CLIE"));
                if (DtUlt == null || DtUlt.equals("null")) {
                    DtUlt = "01/01/2000 10:00:00";
                }
                cursorparamapp.close();
            }
        } catch (Exception e) {
            e.toString();
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        if (senha != null) {
            if (Codclie == 0) {
                soap.addProperty("aParam", "V" + sCodVend + "%" + DtUlt);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            } else {
                soap.addProperty("aParam", "C" + Codclie);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            }
        } else {
            if (Codclie == 0) {
                soap.addProperty("aParam", "V" + sCodVend + "%" + DtUlt);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            } else {
                soap.addProperty("aParam", "C" + Codclie);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            }
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
        String RetClientes = null;

        Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
        if (ConexOk) {
            try {
                Cursor cursorVerificaClie = DB.rawQuery("SELECT * FROM CLIENTES WHERE CODPERFIL =" + idPerfil, null);
                if (cursorVerificaClie.getCount() == 0 && DialogECB != null && DtUlt.equals("01/01/2000 10:00:00")) {
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogECB.setTitle(R.string.wait);
                            DialogECB.setMessage(ctxEnvClie.getString(R.string.primeira_sync_clientes));
                            DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            DialogECB.setIcon(R.drawable.icon_sync);
                            DialogECB.setCancelable(false);
                            DialogECB.show();
                        }
                    });
                    int i = 0;
                    do {
                        try {
                            if (i > 0) {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        public void run() {
                                            DialogECB.setMessage(ctxEnvClie.getString(R.string.primeira_sync_clientes));
                                        }
                                    });
                                }
                                Thread.sleep(500);
                            }
                            final int y = i;
                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetClientes = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        newsoap.addProperty("aParam", "V" + sCodVend + "%" + DtUlt);
                                        newsoap.addProperty("aUsuario", user);
                                        newsoap.addProperty("aSenha", pass);
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        envelope.setOutputSoapObject(soap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetClientes = (String) newenvelope.getResponse();
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxEnvClie, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                cursorVerificaClie.close();
                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxEnvClie, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetClientes == null && i <= 6);
                    if (DialogECB != null) {
                        DialogECB.dismiss();
                    }
                } else {
                    int i = 0;
                    if (hd != null) {
                        hd.post(new Runnable() {
                            public void run() {
                                dialog.setMessage("Por favor aguarde, realizando conexão com o servidor...");
                            }
                        });
                    }
                    do {
                        try {
                            if (i > 0) {
                                Thread.sleep(500);
                            }
                            final int y = i;
                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetClientes = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        if (senha != null) {
                                            if (Codclie == 0) {
                                                newsoap.addProperty("aParam", "V" + sCodVend + "%" + DtUlt);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);
                                            } else {
                                                newsoap.addProperty("aParam", "C" + Codclie);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);
                                            }
                                        } else {
                                            if (Codclie == 0) {
                                                newsoap.addProperty("aParam", "V" + sCodVend + "%" + DtUlt);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);
                                            } else {
                                                newsoap.addProperty("aParam", "C" + Codclie);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);
                                            }
                                        }
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        newenvelope.setOutputSoapObject(newsoap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetClientes = (String) newenvelope.getResponse();
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxEnvClie, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }

                                cursorVerificaClie.close();

                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxEnvClie, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetClientes == null && i <= 6);
                }
            } catch (Exception e) {
                e.toString();
                if (hd != null) {
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctxEnvClie, R.string.failure_communicate, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

        } else {
            if (hd != null) {
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctxEnvClie, R.string.no_connection, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        if (RetClientes.equals("0")) {
            sinccliestatic = ctxEnvClie.getString(R.string.syn_clients_successfully);
            return sinccliestatic;
        } else if (RetClientes == null) {
            sinccliestatic = ctxEnvClie.getString(R.string.failure_communicate);
            return sinccliestatic;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetClientes);
            JSONArray pedidosblq = jsonObj.getJSONArray(TAG_CLIENTESINFO);

            int jumpTime = 0;
            final int totalProgressTime = pedidosblq.length();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }

            String CodCliente = null;
            String CodClieExt = null;

            DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

            for (int i = 0; i < pedidosblq.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = pedidosblq.getJSONObject(jumpTime);

                        String Telefone = c.getString(TAG_TELEFONESINFO);
                        Telefone = "{\"telefones\":" + Telefone + "\t}";
                        JSONObject ObjTel = new JSONObject(Telefone);
                        JSONArray Telef = ObjTel.getJSONArray("telefones");
                        String Tel1 = null;
                        String Tel2 = null;
                        String Tel3 = null;

                        for (int t = 0; t < Telef.length(); t++) {
                            JSONObject tt = Telef.getJSONObject(t);
                            if (t == 0) {
                                Tel1 = tt.getString("numero");
                            }
                            if (t == 1) {
                                Tel2 = tt.getString("numero");
                            }
                            if (t == 2) {
                                Tel3 = tt.getString("numero");
                            }
                        }

                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                        }
                        if (hd != null) {
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxEnvClie.getString(R.string.sync_clients));
                                }
                            });
                        }

                        Cursor cursor = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CODCLIE_EXT = '" + c.getString(TAG_CODIGO) + "' AND CODPERFIL = " + idPerfil + "", null);
                        cursor.moveToFirst();
                        String CodEstado = RetornaEstado(c.getString(TAG_ESTADO), ctxEnvClie);
                        int CodCidade = RetornaCidade(c.getString(TAG_CIDADE), CodEstado, ctxEnvClie);
                        int CodBairro = RetornaBairro(c.getString(TAG_BAIRRO), CodCidade, ctxEnvClie);
                        try {
                            if (cursor.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + c.getString(TAG_RAZAOSOCIAL).trim().replace("'", "''") +
                                        "', NOMEFAN = '" + c.getString(TAG_NOMEFANTASIA).trim().replace("'", "''") +
                                        "', REGIDENT = '" + c.getString(TAG_RG).trim() +
                                        "', LIMITECRED = '" + c.getString(TAG_LIMITECRED) +
                                        "', BLOQUEIO = '" + c.getString(TAG_BLOQUEIO) +
                                        "', INSCREST = '" + c.getString(TAG_INSCESTADUAL) +
                                        "', EMAIL = '" + c.getString(TAG_EMAILS) +
                                        "', TEL1 = '" + Tel1 +
                                        "', TEL2 = '" + Tel2 +
                                        "', ENDERECO = '" + c.getString(TAG_LOGRADOURO).trim().replace("'", "''") +
                                        "', NUMERO = '" + c.getString(TAG_NUMERO) +
                                        "', COMPLEMENT = '" + c.getString(TAG_COMPLEMENTO).trim().replace("'", "''") +
                                        "', CODBAIRRO = '" + CodBairro +
                                        "', OBS = '" + c.getString(TAG_OBS).trim().replace("'", "''") +
                                        "', CODCIDADE = '" + CodCidade +
                                        "', UF = '" + CodEstado +
                                        "', CEP = '" + c.getString(TAG_CEP) +
                                        "', CODCLIE_EXT = '" + c.getString(TAG_CODIGO) +
                                        "', TIPOPESSOA = '" + c.getString(TAG_TIPO) +
                                        "', ATIVO = '" + c.getString(TAG_ATIVO) +
                                        "', CODPERFIL = " + idPerfil +
                                        ",  CODVENDEDOR = " + sCodVend +
                                        ",  FLAGINTEGRADO = '2' " +
                                        "   WHERE CODCLIE_EXT = '" + c.getString(TAG_CODIGO) +
                                        "'  AND CODPERFIL = " + idPerfil + "");
                            } else {
                                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, REGIDENT, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                                        "CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA,LIMITECRED,BLOQUEIO, ATIVO, CODPERFIL, FLAGINTEGRADO) VALUES(" +
                                        "'" + c.getString(TAG_CNPJCPF) + "'" +
                                        ",'" + c.getString(TAG_RAZAOSOCIAL).trim().replace("'", "''") +
                                        "','" + c.getString(TAG_RG).trim() +
                                        "',' " + c.getString(TAG_NOMEFANTASIA).trim().replace("'", "''") +
                                        "',' " + c.getString(TAG_INSCESTADUAL) +
                                        "',' " + c.getString(TAG_EMAILS) +
                                        "',' " + Tel1 +
                                        "', '" + Tel2 +
                                        "', '" + c.getString(TAG_LOGRADOURO).trim().replace("'", "''") +
                                        "',' " + c.getString(TAG_NUMERO).trim() +
                                        "', '" + c.getString(TAG_COMPLEMENTO).trim().replace("'", "''") +
                                        "',' " + CodBairro +
                                        "','" + c.getString(TAG_OBS).trim().replace("'", "''") +
                                        "','" + CodCidade +
                                        "','" + CodEstado +
                                        "',' " + c.getString(TAG_CEP) +
                                        "', '" + c.getString(TAG_CODIGO) +
                                        "',' " + sCodVend +
                                        "','" + c.getString(TAG_TIPO) +
                                        "','" + c.getString(TAG_LIMITECRED) +
                                        "','" + c.getString(TAG_BLOQUEIO) +
                                        "','" + c.getString(TAG_ATIVO) +
                                        "', '" + idPerfil +
                                        "',' " + "2" + "');"); // FLAGINTEGRADO = 2, Significa que o cliente já está integrado e existe na base da retaguarda.
                                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + c.getString(TAG_RAZAOSOCIAL).trim().replace("'", "''") +
                                        "', NOMEFAN = '" + c.getString(TAG_NOMEFANTASIA).trim().replace("'", "''") +
                                        "', REGIDENT = '" + c.getString(TAG_RG).trim() +
                                        "', LIMITECRED = '" + c.getString(TAG_LIMITECRED) +
                                        "', BLOQUEIO = '" + c.getString(TAG_BLOQUEIO) +
                                        "', INSCREST = '" + c.getString(TAG_INSCESTADUAL) +
                                        "', EMAIL = '" + c.getString(TAG_EMAILS) +
                                        "', TEL1 = '" + Tel1 +
                                        "', TEL2 = '" + Tel2 +
                                        "', ENDERECO = '" + c.getString(TAG_LOGRADOURO).trim().replace("'", "''") +
                                        "', NUMERO = '" + c.getString(TAG_NUMERO) +
                                        "', COMPLEMENT = '" + c.getString(TAG_COMPLEMENTO).trim().replace("'", "''") +
                                        "', CODBAIRRO = '" + CodBairro +
                                        "', OBS = '" + c.getString(TAG_OBS).trim().replace("'", "''") +
                                        "', CODCIDADE = '" + CodCidade +
                                        "', UF = '" + CodEstado +
                                        "', CEP = '" + c.getString(TAG_CEP) +
                                        "', CODCLIE_EXT = '" + c.getString(TAG_CODIGO) +
                                        "', TIPOPESSOA = '" + c.getString(TAG_TIPO) +
                                        "', ATIVO = '" + c.getString(TAG_ATIVO) +
                                        "', CODPERFIL = " + idPerfil +
                                        ",  CODVENDEDOR = " + sCodVend +
                                        ",  FLAGINTEGRADO = '2' " +
                                        "   WHERE CODCLIE_EXT = '" + c.getString(TAG_CODIGO) +
                                        "'  AND CODPERFIL = " + idPerfil + "");
                            }
                            cursor.close();
                        } catch (Exception E) {
                            E.toString();
                        }

                            Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CODCLIE_EXT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CODCLIE_EXT = '" + c.getString(TAG_CODIGO) + "'", null);
                            cursor1.moveToFirst();
                            CodCliente = cursor1.getString(cursor1.getColumnIndex("CODCLIE_INT"));
                            CodClieExt = cursor1.getString(cursor1.getColumnIndex("CODCLIE_EXT"));


                            cursor1.close();

                        } catch (Exception E) {
                            E.toString();
                        }

                        //sincronizaContatos(ctxEnvClie, user, pass, RetClientes, Integer.parseInt(CodCliente), jumpTime);
                        /*if (CodClieExt == null) {
                            Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " + CodCliente, null);
                            CursorContatosEnv.moveToFirst();
                            if ((CursorContatosEnv.getCount() > 0)) {
                                DB.execSQL("DELETE FROM CONTATO WHERE CODCLIENTE = " + CodCliente);
                                CursorContatosEnv.close();
                            }
                        } else {
                            Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIE_EXT = " + CodClieExt, null);
                            CursorContatosEnv.moveToFirst();
                            if ((CursorContatosEnv.getCount() > 0)) {
                                DB.execSQL("DELETE FROM CONTATO WHERE CODCLIE_EXT = " + CodClieExt);
                                CursorContatosEnv.close();
                            }
                        }
                        String Contatos = c.getString(TAG_CONTATOSINFO);
                        Contatos = "{\"contatos\":" + Contatos + "\t}";
                        JSONObject ObjCont = new JSONObject(Contatos);
                        JSONArray Cont = ObjCont.getJSONArray("contatos");
                        String nomeContato = null,
                                cargoContato = null,
                                atvCargoContato = null,
                                codExtContato = null,
                                emailContato = null,
                                tel1Contato = null,
                                tel2Contato = null,
                                bairroContato = null,
                                documentoContato = null,
                                dataAnivContato = null,
                                endContato = null,
                                complContato = null,
                                ufContato = null,
                                obsContato = null,
                                cepContato = null,
                                cidadeContato = null,
                                tipoContato = null,
                                setorContato = null;
                        String codCargoContato = null;


                        try {
                            for (int co = 0; co < Cont.length(); co++) {
                                JSONObject cc = Cont.getJSONObject(co);
                                if (co == 0) {
                                    nomeContato = cc.getString("nome");
                                    cargoContato = cc.getString("cargo");
                                    codCargoContato = cc.getString("codcargo");
                                    atvCargoContato = cc.getString("cargoativo");
                                    emailContato = cc.getString("email");
                                    codExtContato = cc.getString("CodContato");
                                    documentoContato = cc.getString("Documento");
                                    dataAnivContato = cc.getString("DataAniversario");
                                    endContato = cc.getString("Logradouro");
                                    complContato = cc.getString("complemento");
                                    bairroContato = cc.getString("Bairro");
                                    cidadeContato = cc.getString("Cidade");
                                    cepContato = cc.getString("cep");
                                    ufContato = cc.getString("uf");
                                    obsContato = cc.getString("observacao");
                                    tipoContato = "C";

                                    String TelCont1 = c.getString("telefones");
                                    TelCont1 = "{\"telefones\":" + TelCont1 + "\t}";
                                    JSONObject ObjTelC1 = new JSONObject(TelCont1);
                                    JSONArray TelefC1 = ObjTelC1.getJSONArray("telefones");

                                    for (int tc1 = 0; tc1 < TelefC1.length(); tc1++) {
                                        JSONObject tt1 = TelefC1.getJSONObject(tc1);
                                        if (tc1 == 0) {
                                            tel1Contato = tt1.getString("numero");
                                        }
                                        if (tc1 == 1) {
                                            tel2Contato = tt1.getString("numero");
                                        }
                                    }
                                }
                                if (co == 1) {
                                    nomeContato = cc.getString("nome");
                                    cargoContato = cc.getString("cargo");
                                    codCargoContato = cc.getString("codcargo");
                                    atvCargoContato = cc.getString("cargoativo");
                                    emailContato = cc.getString("email");
                                    codExtContato = cc.getString("CodContato");
                                    documentoContato = cc.getString("Documento");
                                    dataAnivContato = cc.getString("DataAniversario");
                                    endContato = cc.getString("Logradouro");
                                    complContato = cc.getString("complemento");
                                    bairroContato = cc.getString("Bairro");
                                    cidadeContato = cc.getString("Cidade");
                                    cepContato = cc.getString("cep");
                                    ufContato = cc.getString("uf");
                                    obsContato = cc.getString("observacao");
                                    tipoContato = "C";

                                    String TelCont2 = c.getString("telefones");
                                    TelCont2 = "{\"telefones\":" + TelCont2 + "\t}";
                                    JSONObject ObjTelC2 = new JSONObject(TelCont2);
                                    JSONArray TelefC2 = ObjTelC2.getJSONArray("telefones");

                                    for (int tc2 = 0; tc2 < TelefC2.length(); tc2++) {
                                        JSONObject tt2 = Telef.getJSONObject(tc2);
                                        if (tc2 == 0) {
                                            tel1Contato = tt2.getString("numero");
                                        }
                                        if (tc2 == 1) {
                                            tel2Contato = tt2.getString("numero");
                                        }
                                    }
                                }
                                if (co == 2) {
                                    nomeContato = cc.getString("nome");
                                    cargoContato = cc.getString("cargo");
                                    codCargoContato = cc.getString("codcargo");
                                    atvCargoContato = cc.getString("cargoativo");
                                    emailContato = cc.getString("email");
                                    codExtContato = cc.getString("CodContato");
                                    documentoContato = cc.getString("Documento");
                                    dataAnivContato = cc.getString("DataAniversario");
                                    endContato = cc.getString("Logradouro");
                                    complContato = cc.getString("complemento");
                                    bairroContato = cc.getString("Bairro");
                                    cidadeContato = cc.getString("Cidade");
                                    cepContato = cc.getString("cep");
                                    ufContato = cc.getString("uf");
                                    obsContato = cc.getString("observacao");
                                    tipoContato = "C";

                                    String TelCont3 = c.getString("telefones");
                                    TelCont3 = "{\"telefones\":" + TelCont3 + "\t}";
                                    JSONObject ObjTelC3 = new JSONObject(TelCont3);
                                    JSONArray TelefC3 = ObjTelC3.getJSONArray("telefones");

                                    for (int tc3 = 0; tc3 < TelefC3.length(); tc3++) {
                                        JSONObject tt3 = Telef.getJSONObject(tc3);
                                        if (tc3 == 0) {
                                            tel1Contato = tt3.getString("numero");
                                        }
                                        if (tc3 == 1) {
                                            tel2Contato = tt3.getString("numero");
                                        }
                                    }
                                }

                                String produtosCont = c.getString("itens_contato");
                                produtosCont = "{\"codigoitem\":" + produtosCont + "\t}";
                                JSONObject objProdCont = new JSONObject(produtosCont);
                                JSONArray prodCont = objProdCont.getJSONArray("itens_contato");

                                for(int pd = 0; pd < prodCont.length(); pd++){
                                    JSONObject pdt = prodCont.getJSONObject(pd);
                                }

                                try {
                                    if (!nomeContato.equals("0") || !cargoContato.equals("0") || !emailContato.equals("0") ||
                                            !tel1Contato.equals("0") || !tel2Contato.equals("0")) {
                                        DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, CODCLIENTE, CODCLIE_EXT, codcontato_ext, documento, " +
                                                "data, cep, endereco, complemento, uf, codvendedor, bairro, desc_cidade, tipo, obs, codperfil, CODCARGO_EXT, SETOR) VALUES(" +
                                                "'" + nomeContato.trim() + "','" + cargoContato.trim() +
                                                "',' " + emailContato.trim() + "',' " + tel1Contato + "',' " + tel2Contato + "'" +
                                                "," + CodCliente + ", '" + CodClieExt + "', '" + codExtContato + "', '" + documentoContato + "', '" +
                                                dataAnivContato + "', '" + cepContato + "', '" + endContato + "', '" + complContato + "', '" +
                                                ufContato + "', " + codVendedor + ", '" + bairroContato + "', '" + cidadeContato + "', '" +
                                                tipoContato + "', '" + obsContato + "', " + idPerfil + ", " + Integer.parseInt(codCargoContato) + ", '" + setorContato + "');");
                                    }
                                    try{
                                        if (!codCargoContato.equals("")){
                                            Util.atualizaCargoContato(cargoContato,codCargoContato,atvCargoContato,ctxEnvClie);
                                        }
                                    }catch(Exception e){
                                        e.toString();
                                    }
                                    //}
                                    sinccliestatic = ctxEnvClie.getString(R.string.syn_clients_successfully);
                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }
                            }
                        } catch (Exception e) {
                            e.toString();
                        }*/
                        sinccliestatic = ctxEnvClie.getString(R.string.syn_clients_successfully);
                }
            }
        } catch (JSONException e) {
            e.toString();
        }
        if (Codclie == 0 && sinccliestatic.equals(ctxEnvClie.getString(R.string.syn_clients_successfully))) {
            try {
                DtUlt = Util.DataHojeComHorasMinSecBR();
                DB.execSQL("UPDATE USUARIOS SET DT_ULT_ATU_CLIE = '" + DtUlt + "' WHERE CODPERFIL = " + idPerfil + " AND USUARIO = '" + user + "' AND SENHA = '" + pass + "'");
            } catch (Exception e) {
                e.toString();
            }
        }
        return sinccliestatic;
    }

    public static String sincronizaContatos(final Context ctxContato, String user, String pass, String result, int codCliente, int jumpTime) {

        SharedPreferences prefsHost = ctxContato.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxContato.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincContatoEnvio = "0";

        String METHOD_NAME = "CarregarContatos",
                CONTATO_INFO = "Contatos",
                PRODUTOS_INFO = "itens_contato",
                HORARIOS_INFO = "dias_contato",
                NOME_CONTATO = "nome",
                CARGO_CONTATO = "cargo",
                COD_CONTATO = "codcontat",
                TIPO_CONTATO = "tipo",
                CODCLIE_CONTATO = "codclie",
                CODVEND_CONTATO = "codvend",
                CODCARGO_CONTATO = "codcargo",
                CARGOATIVO_CONTATO = "cargoativo",
                SETOR_CONTATO = "setor",
                EMAIL_CONTATO = "email",
                TELEFONES_CONTATOS = "telefones",
                DOC_CONTATOS = "Documento",
                DATA_CONTATO = "DataAniversario",
                ENDERECO_CONTATO = "Logradouro",
                COMPL_CONTATO = "complemento",
                BAIRRO_CONTATO = "Bairro",
                CIDADE_COONTATO = "Cidade",
                CEP_CONTATO = "cep",
                UF_CONTATO = "uf",
                OBS_CONTATO = "observacao",
                CODITEM_CONTATO = "codigoitem";

        try {
            JSONObject jsonObj = new JSONObject(result);
            JSONArray arrayResult = jsonObj.getJSONArray("clientes");

            JSONObject cd = arrayResult.getJSONObject(jumpTime);

            String Contatos = cd.getString(CONTATO_INFO);
            Contatos = "{\"contatos\":" + Contatos + "\t}";
            JSONObject ObjCont = new JSONObject(Contatos);
            JSONArray Cont = ObjCont.getJSONArray("contatos");

            DB = new ConfigDB(ctxContato).getReadableDatabase();

            for (int i = 0; i < Cont.length(); i++) {
                    try {
                        JSONObject c = Cont.getJSONObject(i);

                        String Telefone = c.getString(TELEFONES_CONTATOS);
                        Telefone = "{\"telefones\":" + Telefone + "\t}";
                        JSONObject ObjTel = new JSONObject(Telefone);
                        JSONArray Telef = ObjTel.getJSONArray("telefones");
                        String Tel1 = null;
                        String Tel2 = null;
                        String Tel3 = null;

                        for (int t = 0; t < Telef.length(); t++) {
                            JSONObject tt = Telef.getJSONObject(t);
                            if (t == 0) {
                                Tel1 = tt.getString("numero");
                            }
                            if (t == 1) {
                                Tel2 = tt.getString("numero");
                            }
                            if (t == 2) {
                                Tel3 = tt.getString("numero");
                            }
                        }

                        Cursor cursorContato = DB.rawQuery("SELECT CONTATO.* " +
                                "FROM CONTATO WHERE CODCONTATO_EXT = " + c.getString(COD_CONTATO) + " and CODPERFIL = " + idPerfil, null);
                        cursorContato.moveToFirst();
                        //String CodEstado = RetornaEstado(c.getString(UF_CONTATO));

                        int codContato = c.getInt(COD_CONTATO);
                        int codCargoContato = 0;
                        if (c.getString(CODCARGO_CONTATO).equals("")) {
                            codCargoContato = 0;
                        } else {
                            codCargoContato = Integer.parseInt(c.getString(CODCARGO_CONTATO));
                        }
                        try {
                            if ((Contatos != null) || (!Contatos.equals(""))) {
                                if (cursorContato.getCount() > 0) {
                                    DB.execSQL(" UPDATE CONTATO SET CODCLIENTE = " + c.getString(CODCLIE_CONTATO) + ", " +
                                            "    CODCONTATO_EXT = " + c.getString(COD_CONTATO) + ", " +
                                            "    NOME = '" + c.getString(NOME_CONTATO) + "', " +
                                            "    CARGO = '" + c.getString(CARGO_CONTATO) + "', " +
                                            "    CODCARGO = " + codCargoContato + ", " +
                                            "    SETOR  = '" + c.getString(SETOR_CONTATO) + "', " +
                                            "    DOCUMENTO = '" + c.getString(DOC_CONTATOS) + "', " +
                                            "    DATA = '" + c.getString(DATA_CONTATO) + "', " +
                                            "    CEP = '" + c.getString(CEP_CONTATO) + "', " +
                                            "    ENDERECO = '" + c.getString(ENDERECO_CONTATO) + "', " +
                                            "    COMPLEMENTO = '" + c.getString(COMPL_CONTATO) + "', " +
                                            "    UF = '" + c.getString(UF_CONTATO) + "', " +
                                            "    CODVENDEDOR = " + c.getString(CODVEND_CONTATO) + ", " +
                                            "    BAIRRO = '" + c.getString(BAIRRO_CONTATO) + "', " +
                                            "    DESC_CIDADE = '" + c.getString(CIDADE_COONTATO) + "', " +
                                            "    EMAIL = '" + c.getString(EMAIL_CONTATO) + "', " +
                                            "    TIPO = '" + c.getString(TIPO_CONTATO) + "', " +
                                            "    OBS = '" + c.getString(OBS_CONTATO) + "', " +
                                            //"    CODCLIE_EXT = " + Codclie + ", " +
                                            "    FLAGINTEGRADO = 'S', " +
                                            "    CODPERFIL = " + idPerfil +
                                            " WHERE CODCONTATO_EXT = " + c.getString(COD_CONTATO) + " and CODPERFIL = " + idPerfil);
                                } else {
                                    DB.execSQL("INSERT INTO CONTATO (CODCLIENTE, CODCONTATO_EXT, NOME, CARGO, CODCARGO, SETOR, TEL1, TEL2, DOCUMENTO, " +
                                            "DATA, CEP, SETOR, " +
                                            "ENDERECO, COMPLEMENTO, UF, CODVENDEDOR, BAIRRO, DESC_CIDADE, EMAIL, TIPO, OBS, FLAGINTEGRADO, CODPERFIL) " +
                                            "VALUES(" +
                                            "" + c.getString(CODCLIE_CONTATO) +
                                            "," + c.getString(COD_CONTATO) +
                                            ",'" + c.getString(NOME_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(CARGO_CONTATO).trim().replace("'", "''") +
                                            "'," + codCargoContato +
                                            ",'" + c.getString(SETOR_CONTATO) +
                                            "','" + Tel1 +
                                            "','" + Tel2 +
                                            "','" + c.getString(DOC_CONTATOS).trim().replace("'", "''") +
                                            "','" + c.getString(DATA_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(CEP_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(SETOR_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(ENDERECO_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(COMPL_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(UF_CONTATO).trim().replace("'", "''") +
                                            "'," + c.getString(CODVEND_CONTATO) +
                                            ",'" + c.getString(BAIRRO_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(CIDADE_COONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(EMAIL_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(TIPO_CONTATO).trim().replace("'", "''") +
                                            "','" + c.getString(OBS_CONTATO).trim().replace("'", "''").replace("\n", "' '") +
                                            "','" + "S" +
                                            "'," + idPerfil + ");");
                                }
                            }

                            //-------------------CADASTRO E ATUALIZAÇÃO CARGOS----------------------------

                            //if(!c.getString(CODCARGO_CONTATO).equals("")) {
                            try {
                                    /*Cursor cursor1 = DB.rawQuery(" SELECT CODCARGO_EXT, ATIVO, DES_CARGO FROM CARGOS WHERE DES_CARGO = '" + c.getString(CARGO_CONTATO) + "' " +
                                            " and CODCARGO_EXT = " + c.getInt(CODCARGO_CONTATO), null);
                                    cursor1.moveToFirst();*/

                                //if (cursor1.getCount() > 0) {
                                String desc_cargo = c.getString(CARGO_CONTATO);
                                String codCargo = c.getString(CODCARGO_CONTATO);
                                String ativCargo = c.getString(CARGOATIVO_CONTATO);

                                Util.atualizaCargoContato(desc_cargo, codCargo, ativCargo, ctxContato);
                                //}
                                //cursor1.close();
                            } catch (Exception E) {
                                E.toString();
                            }
                            //}

                            //-------------------CADASTRO DOS ITENS DO CONTATO----------------------------

                            String Produtos = c.getString(PRODUTOS_INFO);
                            Produtos = "{\"itens_contato\":" + Produtos + "\t}";
                            JSONObject ObjProd = new JSONObject(Produtos);
                            JSONArray Prod = ObjProd.getJSONArray("itens_contato");


                            Cursor cursProd = null;
                            for (int iProd = 0; iProd < Prod.length(); iProd++) {

                                JSONObject pp = Prod.getJSONObject(iProd);

                                int codProdCont = pp.getInt("codigoitem");

                                DB = new ConfigDB(ctxContato).getReadableDatabase();
                                cursProd = DB.rawQuery("select cod_externo_contato, cod_item " +
                                        "from produtos_contatos " +
                                        "where cod_externo_contato = " + c.getString(COD_CONTATO) + ", and " +
                                        "cod_item = " + codProdCont, null);
                                if (cursProd.getCount() == 0) {
                                    DB.execSQL("insert into produtos_contatos (cod_externo_contato, cod_item) values (" +
                                            c.getString(COD_CONTATO) + "," +
                                            codProdCont);
                                }
                                cursProd.close();
                            }

                            //-------------------------------CADASTRO DE DIAS AGENDA-------------------------------

                            String Agenda = c.getString(PRODUTOS_INFO);
                            Agenda = "{\"dias_contato\":" + Agenda + "\t}";
                            JSONObject ObjAg = new JSONObject(Agenda);
                            JSONArray Ag = ObjAg.getJSONArray("dias_contato");

                            Cursor cursAg = null;
                            for (int iAg = 0; iAg < Ag.length(); iAg++) {

                                JSONObject pp = Prod.getJSONObject(iAg);

                                int codDiaSemana = pp.getInt("cod_dia_semana");
                                String hrInicio = pp.getString("hora_inicio");
                                String hrFinal = pp.getString("hora_saida");

                                Util.gravaHorariosContatos(ctxContato, hrInicio, hrFinal, codDiaSemana, codContato);
                            }
                            cursorContato.close();

                        } catch (Exception E) {
                            E.toString();
                        }
                    } catch (Exception E) {
                        E.toString();
                    }

            }
        } catch (
                JSONException e)

        {
            e.toString();
        }
        return sincContatoEnvio;
    }

    public static String sincronizaProdutos(final Context ctxSincProd, String user, String pass, int codItem, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefsHost = ctxSincProd.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxSincProd.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincprodstatic = "0";
        String DtUltItem = null;

        DB = new ConfigDB(ctxSincProd).getReadableDatabase();

        String METHOD_NAME = "Carregar";
        String TAG_PRODUTOSINFO = "produtos";
        String TAG_CODIGOITEM = "codigoitem";
        String TAG_CODMANUAL = "coditemanual";
        String TAG_DESCRICAO = "descricao";
        String TAG_UNIVENDA = "univenda";
        String TAG_VLVENDA1 = "vlvenda1";
        String TAG_VLVENDA2 = "vlvenda2";
        String TAG_VLVENDA3 = "vlvenda3";
        String TAG_VLVENDA4 = "vlvenda4";
        String TAG_VLVENDA5 = "vlvenda5";
        String TAG_VLVENDAP1 = "vlvendap1";
        String TAG_VLVENDAP2 = "vlvendap2";
        String TAG_TABELAPADRAO = "tabelapadrao";
        String TAG_MARCA = "marca";
        String TAG_CLASSE = "classe";
        String TAG_FABRICANTE = "fabricante";
        String TAG_FORNECEDOR = "fornecedor";
        String TAG_APRESENTACAO = "apresentacao";
        String TAG_ATIVO = "ativo";
        String TAG_QTDESTOQUE = "qtd_disponivel";
        String TAG_QTDMINVEND = "qtdminvenda";

        if (codItem == 0) {
            Cursor cursorparamapp = DB.rawQuery("SELECT DT_ULT_ITENS, CODPERFIL FROM PARAMAPP WHERE CODPERFIL = " + idPerfil, null);
            cursorparamapp.moveToFirst();
            DtUltItem = cursorparamapp.getString(cursorparamapp.getColumnIndex("DT_ULT_ITENS"));
            if (DtUltItem == null) {
                DtUltItem = "01/01/2000 10:00:00";
            }
            cursorparamapp.close();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        if (senha != null) {
            if (codItem == 0) {
                soap.addProperty("aParam", "D" + DtUltItem);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            } else {
                soap.addProperty("aParam", "I" + codItem);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            }
        } else {
            if (codItem == 0) {
                soap.addProperty("aParam", "D" + DtUltItem);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            } else {
                soap.addProperty("aParam", "I" + codItem);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            }
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
        String RetProdutos = null;


        Boolean ConexOk = Util.checarConexaoCelular(ctxSincProd);
        if (ConexOk) {
            try {
                Cursor cursorVerificaProd = DB.rawQuery("SELECT * FROM ITENS WHERE CODPERFIL =" + idPerfil, null);
                if (cursorVerificaProd.getCount() == 0 && DialogECB != null) {
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogECB.setTitle(R.string.wait);
                            DialogECB.setMessage(ctxSincProd.getString(R.string.primeira_sync_itens));
                            DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            DialogECB.setIcon(R.drawable.icon_sync);
                            DialogECB.setCancelable(false);
                            DialogECB.show();
                        }
                    });
                    int i = 0;
                    do {
                        try {
                            if (i > 0) {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        public void run() {
                                            DialogECB.setMessage(ctxSincProd.getString(R.string.primeira_sync_itens));
                                        }
                                    });
                                }
                                Thread.sleep(500);
                            }
                            final int y = i;
                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetProdutos = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        newsoap.addProperty("aParam", "D" + DtUltItem);
                                        newsoap.addProperty("aUsuario", user);
                                        newsoap.addProperty("aSenha", pass);
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        envelope.setOutputSoapObject(soap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetProdutos = (String) newenvelope.getResponse();
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxSincProd, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                cursorVerificaProd.close();
                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxSincProd, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetProdutos == null && i <= 6);
                    if (DialogECB != null) {
                        DialogECB.dismiss();
                    }
                } else {
                    int i = 0;
                    do {
                        try {
                            if (i > 0) {
                                Thread.sleep(500);
                            }
                            final int y = i;
                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetProdutos = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        if (senha != null) {
                                            if (codItem == 0) {
                                                newsoap.addProperty("aParam", "D" + DtUltItem);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);

                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetProdutos = (String) newenvelope.getResponse();
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            } else {
                                                newsoap.addProperty("aParam", "I" + codItem);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);

                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetProdutos = (String) newenvelope.getResponse();
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            }
                                        } else {
                                            if (codItem == 0) {
                                                newsoap.addProperty("aParam", "D" + DtUltItem);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);

                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetProdutos = (String) newenvelope.getResponse();
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            } else {
                                                newsoap.addProperty("aParam", "I" + codItem);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);

                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 900000);
                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetProdutos = (String) newenvelope.getResponse();
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxSincProd, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                cursorVerificaProd.close();

                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxSincProd, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetProdutos == null && i <= 6);
                }


            } catch (Exception e) {
                e.toString();
                if (hd != null) {
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctxSincProd, R.string.failure_communicate, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

        } else {
            if (hd != null) {
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctxSincProd, R.string.no_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        if (RetProdutos.equals("0")) {
            sincprodstatic = ctxSincProd.getString(R.string.sync_products_successfully);
            return sincprodstatic;
        } else if (RetProdutos == null) {
            sincprodstatic = ctxSincProd.getString(R.string.failure_communicate);
            return sincprodstatic;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetProdutos);
            JSONArray ProdItens = jsonObj.getJSONArray(TAG_PRODUTOSINFO);

            int jumpTime = 0;
            final int totalProgressTime = ProdItens.length();
            if (dialog != null) {
                dialog.setMax(totalProgressTime);
                dialog.setProgress(jumpTime);
            }
            for (int k = 0; k < ProdItens.length(); k++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject CItens = ProdItens.getJSONObject(jumpTime);

                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                        }
                        if (hd != null) {
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxSincProd.getString(R.string.sync_products));
                                }
                            });
                        }
                        String Ativo = CItens.getString(TAG_ATIVO);

                        if (Ativo.equals("true")) {
                            Ativo = "S";
                        } else {
                            Ativo = "N";
                        }
                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM) + " AND CODPERFIL = " + idPerfil + "", null);

                        try {
                            if (CursItens.getCount() > 0) {
                                CursItens.moveToFirst();
                                DB.execSQL(" UPDATE ITENS SET CODITEMANUAL = '" + CItens.getString(TAG_CODMANUAL).trim().replace("'", "''") +
                                        "', DESCRICAO = '" + CItens.getString(TAG_DESCRICAO).replace("'", "''") +
                                        "', FABRICANTE = '" + CItens.getString(TAG_FABRICANTE).trim().replace("'", "''") +
                                        "', FORNECEDOR = '" + CItens.getString(TAG_FORNECEDOR).trim().replace("'", "''") +
                                        "', CLASSE = '" + CItens.getString(TAG_CLASSE).trim().replace("'", "''") +
                                        "', MARCA = '" + CItens.getString(TAG_MARCA).trim().replace("'", "''") +
                                        "', UNIVENDA = '" + CItens.getString(TAG_UNIVENDA).trim().replace("'", "''") +
                                        "', QTDMINVEND = '" + CItens.getString(TAG_QTDMINVEND) +
                                        "', VLVENDA1 = '" + CItens.getString(TAG_VLVENDA1).trim() +
                                        "', VLVENDA2 = '" + CItens.getString(TAG_VLVENDA2).trim() +
                                        "', VLVENDA3 = '" + CItens.getString(TAG_VLVENDA3).trim() +
                                        "', VLVENDA4 = '" + CItens.getString(TAG_VLVENDA4).trim() +
                                        "', VLVENDA5 = '" + CItens.getString(TAG_VLVENDA5).trim() +
                                        "', VLVENDAP1 = '" + CItens.getString(TAG_VLVENDAP1).trim() +
                                        "', TABELAPADRAO = '" + CItens.getString(TAG_TABELAPADRAO).trim().replace("'", "''") +
                                        "', VLVENDAP2 = '" + CItens.getString(TAG_VLVENDAP2).trim() +
                                        "', ATIVO = '" + Ativo +
                                        "', CODPERFIL = " + idPerfil +
                                        ", QTDESTPROD = '" + CItens.getString(TAG_QTDESTOQUE) +
                                        "', APRESENTACAO = '" + CItens.getString(TAG_APRESENTACAO).trim().replace("'", "''") + "'" +
                                        "   WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM) +
                                        "   AND CODPERFIL = " + idPerfil + "");
                            } else {
                                DB.execSQL("INSERT INTO ITENS (CODIGOITEM, CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA, QTDMINVEND, " +
                                        "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2, TABELAPADRAO, " +
                                        "ATIVO,CODPERFIL, QTDESTPROD, APRESENTACAO) VALUES(" + "'" + CItens.getString(TAG_CODIGOITEM) +
                                        "',' " + CItens.getString(TAG_CODMANUAL).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_DESCRICAO).replace("'", "''") +
                                        "',' " + CItens.getString(TAG_FABRICANTE).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_FORNECEDOR).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_CLASSE).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_MARCA).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_UNIVENDA).trim().replace("'", "''") +
                                        "',' " + CItens.getString(TAG_QTDMINVEND).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDA1).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDA2).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDA3).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDA4).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDA5).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDAP1).trim() +
                                        "',' " + CItens.getString(TAG_VLVENDAP2).trim() +
                                        "',' " + CItens.getString(TAG_TABELAPADRAO).trim().replace("'", "''") +
                                        "',' " + Ativo +
                                        "',  " + idPerfil +
                                        " ,' " + CItens.getString(TAG_QTDESTOQUE) +
                                        "',' " + CItens.getString(TAG_APRESENTACAO).trim().replace("'", "''") + "');");

                                //está tendo que atualizar cadas item que é incluso para tirar os espaçõs em alguns campos, pois somente na inserção não tira.
                                DB.execSQL(" UPDATE ITENS SET CODITEMANUAL = '" + CItens.getString(TAG_CODMANUAL).trim().replace("'", "''") +
                                        "', DESCRICAO = '" + CItens.getString(TAG_DESCRICAO).trim().replace("'", "''") +
                                        "', FABRICANTE = '" + CItens.getString(TAG_FABRICANTE).trim().replace("'", "''") +
                                        "', FORNECEDOR = '" + CItens.getString(TAG_FORNECEDOR).trim().replace("'", "''") +
                                        "', CLASSE = '" + CItens.getString(TAG_CLASSE).trim().replace("'", "''") +
                                        "', MARCA = '" + CItens.getString(TAG_MARCA).trim().replace("'", "''") +
                                        "', UNIVENDA = '" + CItens.getString(TAG_UNIVENDA).trim().replace("'", "''") +
                                        "', QTDMINVEND = '" + CItens.getString(TAG_QTDMINVEND) +
                                        "', VLVENDA1 = '" + CItens.getString(TAG_VLVENDA1).trim() +
                                        "', VLVENDA2 = '" + CItens.getString(TAG_VLVENDA2).trim() +
                                        "', VLVENDA3 = '" + CItens.getString(TAG_VLVENDA3).trim() +
                                        "', VLVENDA4 = '" + CItens.getString(TAG_VLVENDA4).trim() +
                                        "', VLVENDA5 = '" + CItens.getString(TAG_VLVENDA5).trim() +
                                        "', VLVENDAP1 = '" + CItens.getString(TAG_VLVENDAP1).trim() +
                                        "', VLVENDAP2 = '" + CItens.getString(TAG_VLVENDAP2).trim() +
                                        "', TABELAPADRAO = '" + CItens.getString(TAG_TABELAPADRAO).trim().replace("'", "''") +
                                        "', ATIVO = '" + Ativo +
                                        "', CODPERFIL = " + idPerfil +
                                        " , QTDESTPROD = '" + CItens.getString(TAG_QTDESTOQUE) +
                                        "', APRESENTACAO = '" + CItens.getString(TAG_APRESENTACAO).trim().replace("'", "''") +
                                        "'  WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM) +
                                        "   AND CODPERFIL = " + idPerfil + "");
                            }
                            CursItens.close();

                        } catch (Exception E) {
                            E.toString();
                        }

                    } catch (Exception E) {
                        E.toString();
                    }
                }

            }
            sincprodstatic = ctxSincProd.getString(R.string.sync_products_successfully);
        } catch (Exception E) {
            E.toString();
            sincprodstatic = "Falha no jsonObj";
            return sincprodstatic;
        }
        if (codItem == 0 && sincprodstatic.equals(ctxSincProd.getString(R.string.sync_products_successfully))) {
            try {
                DtUltItem = Util.DataHojeComHorasMinSecBR();
                DB.execSQL("UPDATE PARAMAPP SET DT_ULT_ITENS = '" + DtUltItem + "' WHERE CODPERFIL = " + idPerfil);
            } catch (Exception e) {
                e.toString();
            }
        }
        return sincprodstatic;
    }

    public static String sincronizaDescricaoTabelas(String sUsuario, String sSenha, final Context
            ctxEnv, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        String sinctabelasstatic = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "CarregaNomeTabelas");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 10000);
        String RetDescTabelas = null;

        Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
        if (ConexOk) {

            int i = 0;
            do {

                try {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (ConexOk) {

                        try {
                            if (i == 0) {
                                Envio.call("", envelope);

                                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                RetDescTabelas = (String) envelope.getResponse();
                                System.out.println("Response :" + resultsRequestSOAP.toString());
                            } else {
                                SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "CarregaNomeTabelas");
                                newsoap.addProperty("aUsuario", usuario);
                                newsoap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                newenvelope.setOutputSoapObject(newsoap);
                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS);
                                newEnvio.call("", newenvelope);

                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                RetDescTabelas = (String) newenvelope.getResponse();
                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                            }
                        } catch (Exception e) {
                            e.toString();
                            sinctabelasstatic = ctxEnv.getString(R.string.failure_communicate);
                        }
                    } else {
                        sinctabelasstatic = ctxEnv.getString(R.string.no_connection);
                    }
                } catch (Exception e) {
                    System.out.println("Error na solicitação" + e);
                }
                i = i + 1;
            } while (RetDescTabelas == null && i <= 6);

        } else {
            sinctabelasstatic = ctxEnv.getString(R.string.no_connection);
            return sinctabelasstatic;
        }
        if (RetDescTabelas == null) {
            sinctabelasstatic = ctxEnv.getString(R.string.failure_communicate);
            return sinctabelasstatic;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetDescTabelas);
            JSONArray JParamApp = jsonObj.getJSONArray("tabelas");

            int jumpTime = 0;
            final int totalProgressTime = JParamApp.length();
            DB = new ConfigDB(ctxEnv).getReadableDatabase();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }

            for (int i = 0; i < JParamApp.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JParamApp.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxEnv.getString(R.string.updating_tables));
                                }
                            });
                        }
                        String DescTab1 = c.getString("nometab1");
                        String DescTab2 = c.getString("nometab2");
                        String DescTab3 = c.getString("mometab3");
                        String DescTab4 = c.getString("nometab4");
                        String DescTab5 = c.getString("nometab5");
                        String DescTab6 = c.getString("nometabp1");
                        String DescTab7 = c.getString("nometabp2");

                        Cursor CursorTabela = DB.rawQuery(" SELECT CODPERFIL, DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP WHERE CODPERFIL = " + idPerfil + "", null);
                        CursorTabela.moveToFirst();
                        if (CursorTabela.getCount() > 0) {
                            DB.execSQL(" UPDATE PARAMAPP SET DESCRICAOTAB1 = '" + DescTab1.trim() +
                                    "', DESCRICAOTAB2 = '" + DescTab2.trim() +
                                    "', DESCRICAOTAB3 = '" + DescTab3.trim() +
                                    "', DESCRICAOTAB4 = '" + DescTab4.trim() +
                                    "', DESCRICAOTAB5 = '" + DescTab5.trim() +
                                    "', DESCRICAOTAB6 = '" + DescTab6.trim() +
                                    "', DESCRICAOTAB7 = '" + DescTab7.trim() +
                                    "' WHERE CODPERFIL = " + idPerfil);
                        } else {

                            DB.execSQL(" INSERT INTO PARAMAPP (DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7,CODPERFIL)" +
                                    " VALUES(" + "'" + DescTab1.trim() + "','" + DescTab2.trim() + "','" + DescTab3.trim() + "','" + DescTab4.trim() + "','" + DescTab5.trim() + "','" + DescTab6.trim() + "','" + DescTab7.trim() + "', " + idPerfil + " );");
                        }
                        sinctabelasstatic = "OK";
                        CursorTabela.close();
                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
        } catch (JSONException e) {
            e.toString();
        }
        return sinctabelasstatic;
    }

    public static String sincronizaBloqueios(String sUsuario, String sSenha, final Context ctxEnv, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        String sincbloqstatic = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "RetornaCadBloqueios");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS, 10000);
        String RetBloqueios = null;

        Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
        if (ConexOk) {

            int i = 0;
            do {

                try {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (ConexOk) {

                        try {
                            if (i == 0) {
                                Envio.call("", envelope);

                                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                RetBloqueios = (String) envelope.getResponse();
                                System.out.println("Response :" + resultsRequestSOAP.toString());
                            } else {
                                SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "RetornaCadBloqueios");
                                newsoap.addProperty("aUsuario", usuario);
                                newsoap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                newenvelope.setOutputSoapObject(newsoap);
                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                                newEnvio.call("", newenvelope);

                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                RetBloqueios = (String) newenvelope.getResponse();
                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                            }
                        } catch (Exception e) {
                            e.toString();
                            sincbloqstatic = ctxEnv.getString(R.string.failure_communicate);
                        }

                    } else {
                        sincbloqstatic = ctxEnv.getString(R.string.no_connection);
                    }
                } catch (Exception e) {
                    System.out.println("Error na solicitação" + e);
                }
                i = i + 1;
            } while (RetBloqueios == null && i <= 6);


        } else {
            sincbloqstatic = ctxEnv.getString(R.string.no_connection);
            return sincbloqstatic;
        }
        if (RetBloqueios == null) {
            sincbloqstatic = ctxEnv.getString(R.string.failure_communicate);
            return sincbloqstatic;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetBloqueios);
            JSONArray JBloqueios = jsonObj.getJSONArray("bloqueios");

            int jumpTime = 0;
            final int totalProgressTime = JBloqueios.length();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JBloqueios.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JBloqueios.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxEnv.getString(R.string.updating_locks));
                                }
                            });
                        }
                        String codblq = c.getString("codblq");
                        String descricao = c.getString("descricao");
                        String bloquear = c.getString("bloquear");
                        String liberar = c.getString("liberar");
                        String fpavista = c.getString("fpavista");


                        Cursor CursorBloqueio = DB.rawQuery(" SELECT CODBLOQ, CODPERFIL, DESCRICAO, BLOQUEAR, LIBERAR, FPAVISTA FROM BLOQCLIE WHERE CODBLOQ = " + codblq + " AND CODPERFIL = " + idPerfil, null);
                        if (CursorBloqueio.getCount() > 0) {
                            DB.execSQL(" UPDATE BLOQCLIE SET CODBLOQ = '" + codblq +
                                    "', DESCRICAO = '" + descricao +
                                    "', BLOQUEAR = '" + bloquear +
                                    "', LIBERAR = '" + liberar +
                                    "', FPAVISTA = '" + fpavista + "'" +
                                    " WHERE CODBLOQ = " + codblq + " AND CODPERFIL = " + idPerfil);
                        } else {
                            DB.execSQL(" INSERT INTO BLOQCLIE (CODBLOQ, CODPERFIL, DESCRICAO, BLOQUEAR, LIBERAR, FPAVISTA)" +
                                    " VALUES(" + codblq +
                                    "," + idPerfil +
                                    ",'" + descricao +
                                    "','" + bloquear +
                                    "','" + liberar +
                                    "','" + fpavista + "' );");
                        }
                        sincbloqstatic = "OK";
                        CursorBloqueio.close();

                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
        } catch (JSONException e) {
            e.toString();
        }
        return sincbloqstatic;
    }

    public static String sincronizaFormasPagamento(String sUsuario, String sSenha, final Context ctxEnv, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        String sincformpgto = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "CarregarFormasPagamento");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS, 10000);
        String RetFormpgto = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk) {

                int i = 0;
                do {

                    try {
                        if (i > 0) {
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (ConexOk) {

                            try {
                                if (i == 0) {
                                    Envio.call("", envelope);

                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                    RetFormpgto = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                } else {
                                    SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "sincronizaFormasPagamento");
                                    newsoap.addProperty("aUsuario", usuario);
                                    newsoap.addProperty("aSenha", senha);
                                    SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                    newenvelope.setOutputSoapObject(newsoap);
                                    HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                                    newEnvio.call("", newenvelope);

                                    SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                    RetFormpgto = (String) newenvelope.getResponse();
                                    System.out.println("Response :" + newresultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                e.toString();
                                sincformpgto = ctxEnv.getString(R.string.failure_communicate);
                            }
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                    i = i + 1;
                } while (RetFormpgto == null && i <= 6);
            } else {
                sincformpgto = ctxEnv.getString(R.string.no_connection);
                return sincformpgto;
            }
            if (RetFormpgto == null) {
                sincformpgto = ctxEnv.getString(R.string.failure_communicate);
                return sincformpgto;
            }
        } catch (Exception e) {
            e.toString();
            sincformpgto = ctxEnv.getString(R.string.failure_communicate);
            return sincformpgto;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetFormpgto);
            JSONArray JParamApp = jsonObj.getJSONArray("formaspagamento");

            int jumpTime = 0;
            final int totalProgressTime = JParamApp.length();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JParamApp.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JParamApp.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(ctxEnv.getString(R.string.updating_parameters));
                                }
                            });
                        }
                        String cod_formapg = c.getString("cod_formapg").trim();
                        String descricao = c.getString("descricao").trim();
                        String status = "I";

                        try {
                            if (jumpTime == 1) {
                                Cursor CursorFormpgto = DB.rawQuery(" SELECT * FROM FORMAPAGAMENTO WHERE CODPERFIL = " + idPerfil, null);
                                CursorFormpgto.moveToFirst();
                                if (CursorFormpgto.getCount() > 0) {
                                    DB.execSQL("UPDATE FORMAPAGAMENTO SET STATUS = '" + status + "' WHERE CODPERFIL = " + idPerfil);
                                }
                            }
                        } catch (Exception e) {
                            e.toString();
                        }

                        Cursor CursorFormpgto = DB.rawQuery(" SELECT CODPERFIL,DESCRICAO,CODEXTERNO FROM FORMAPAGAMENTO WHERE CODPERFIL = " + idPerfil + " AND CODEXTERNO = '" + cod_formapg + "'", null);
                        CursorFormpgto.moveToFirst();
                        if (CursorFormpgto.getCount() > 0) {
                            status = "A";
                            DB.execSQL(" UPDATE FORMAPAGAMENTO SET CODPERFIL = " + idPerfil +
                                    " , DESCRICAO = '" + descricao.trim() +
                                    "', CODEXTERNO = " + cod_formapg.trim() +
                                    ", STATUS = '" + status +
                                    "'  WHERE CODPERFIL = " + idPerfil + " AND CODEXTERNO = '" + cod_formapg + "'");
                        } else {
                            status = "A";
                            DB.execSQL(" INSERT INTO FORMAPAGAMENTO (CODPERFIL,DESCRICAO,CODEXTERNO,STATUS)" +
                                    " VALUES(" + "" + idPerfil + "," +
                                    " '" + descricao.trim() + "'," +
                                    " '" + cod_formapg.trim() + "'," +
                                    " '" + status + "');");
                        }
                        sincformpgto = "OK";
                        CursorFormpgto.close();


                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
        } catch (JSONException e) {
            e.toString();
            return sincformpgto;
        }
        return sincformpgto;
    }

    public static String sincronizaClientesEnvio(String CodClie_Int, final Context ctxEnvClie, String user, String pass, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxEnvClie.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincclieenvstatic = "0";
        String Jcliente = null;
        String METHOD_NAMEENVIO = "Cadastrar";
        DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

        try {
            Cursor CursorClieEnv = null;
            if (CodClie_Int.equals("0")) {
                CursorClieEnv = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON (CLIENTES.CODCIDADE = CIDADES.CODCIDADE) AND (CLIENTES.CODPERFIL = CIDADES.CODPERFIL) LEFT OUTER JOIN " +
                        " ESTADOS ON (CLIENTES.UF = ESTADOS.UF) AND (CLIENTES.CODPERFIL = ESTADOS.CODPERFIL) LEFT OUTER JOIN " +
                        " BAIRROS ON (CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO) AND (CLIENTES.CODPERFIL = BAIRROS.CODPERFIL) " +
                        " WHERE FLAGINTEGRADO = '1' AND CLIENTES.CODPERFIL = " + idPerfil + " " +
                        " ORDER BY NOMEFAN, NOMERAZAO ", null);
            } else {
                CursorClieEnv = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON (CLIENTES.CODCIDADE = CIDADES.CODCIDADE) AND (CLIENTES.CODPERFIL = CIDADES.CODPERFIL) LEFT OUTER JOIN " +
                        " ESTADOS ON (CLIENTES.UF = ESTADOS.UF) AND (CLIENTES.CODPERFIL = ESTADOS.CODPERFIL) LEFT OUTER JOIN " +
                        " BAIRROS ON (CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO) AND (CLIENTES.CODPERFIL = BAIRROS.CODPERFIL) " +
                        " WHERE  CODCLIE_INT = " + CodClie_Int + " AND CLIENTES.CODPERFIL = " + idPerfil, null);
            }


            String RetClieEnvio = null;
            int codContatoInt = 0;

            int jumpTime = 0;
            String CodClie_ext = null;
            String sexo = null;
            final int totalProgressTime = CursorClieEnv.getCount();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }

            CursorClieEnv.moveToFirst();
            if (CursorClieEnv.getCount() > 0) {
                CursorClieEnv.moveToFirst();
                do {
                    for (int i = 0; i < CursorClieEnv.getCount(); i++) {
                        //String OBS = CursorClieEnv.getString(CursorClieEnv.getColumnIndex("OBS")).trim().replaceAll("\n"," ");
                        //OBS = OBS.replaceAll("\n"," ");
                        do {
                            try {
                                jumpTime += 1;
                                if (dialog != null) {
                                    dialog.setProgress(jumpTime);
                                    hd.post(new Runnable() {
                                        public void run() {
                                            dialog.setMessage(ctxEnvClie.getString(R.string.updating_tables));
                                        }
                                    });
                                }
                                String bairro = CursorClieEnv.getString(CursorClieEnv.getColumnIndex("BAIRRO"));

                                Jcliente = "{razao_social: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMERAZAO")).trim() + "'," +
                                        "nome_fantasia: '" + (CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMEFAN"))).trim() + "'," +
                                        "tipo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TIPOPESSOA")) + "'," +
                                        "cnpj_cpf: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'," +
                                        "inscricao_estadual: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("INSCREST")) + "'," +
                                        "Logradouro: '" + (CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ENDERECO"))).trim() + "'," +
                                        "numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NUMERO")) + "'," +
                                        "codvendedor: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODVENDEDOR")) + "'," +
                                        "complemento: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("COMPLEMENT")) + "'," +
                                        "bairro: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("BAIRRO")) + "'," +
                                        "cidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CIDADE")) + "'," +
                                        "estado: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("UF")) + "'," +
                                        "cep: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CEP")) + "'," +
                                        "observacao: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("OBS")).trim().replaceAll("\n", " ") + "'," +
                                        "identidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("REGIDENT")) + "'," +
                                        "emails: [{email: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("EMAIL")) + "'}," +
                                        "{email: ''}]," +
                                        "ativo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ATIVO")) + "'," +
                                        "sexo: '" + "" + "'," +
                                        "estadocivil: '" + "" + "'," +
                                        "tipoplano: '" + "" + "'," +
                                        "telefones: [{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL1")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL2")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TELFAX")) + "'}]";

                                String Contatos = "";
                                Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " +
                                        CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODCLIE_INT")), null);

                                CursorContatosEnv.moveToFirst();
                                while (CursorContatosEnv.moveToNext()) {
                                    codContatoInt = CursorContatosEnv.getInt(CursorContatosEnv.getColumnIndex("CODCONTATO_INT"));

                                    Contatos = "{nome: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("NOME")) + "'," +
                                            "cargo: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CARGO")) + "'," +
                                            "tipo: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TIPO")) + "'," +
                                            "codclie: '" + CursorContatosEnv.getInt(CursorContatosEnv.getColumnIndex("CODCLIE_EXT")) + "'," +
                                            "codvend: '" + CursorContatosEnv.getInt(CursorContatosEnv.getColumnIndex("CODVENDEDOR")) + "'," +
                                            "bairro: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("BAIRRO")) + "'," +
                                            "Documento: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("DOCUMENTO")) + "'," +
                                            "DataAniversario: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("DATA")) + "'," +
                                            "Logradouro: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("ENDERECO")) + "'," +
                                            "complemento: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("COMPLEMENTO")) + "'," +
                                            "uf: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("UF")) + "'," +
                                            "observacao: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("OBS")) + "'," +
                                            "cep: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CEP")) + "'," +
                                            "Cidade: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("DESC_CIDADE")) + "'," +
                                            "emails: [{email: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("EMAIL")) + "'}]," +
                                            "telefones: [{numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL1")) + "'," +
                                            "numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL2")) + "'}]," +
                                            "dias_contato: [" + retornaJsonHorariosAgenda(CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CODCONTATO_INT")), ctxEnvClie) + "]," +
                                            "itens_contato: [" + retornaItens(CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CODCONTATO_INT")), ctxEnvClie) + "]},";
                                }
                                CursorContatosEnv.close();

                                if (Contatos != "") {
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";
                                } else {
                                    Jcliente = Jcliente + ",contatos: []"; /*"{nome: ''," +
                                            "cargo: ''," +
                                            "tipo: ''," +
                                            "codclie: ''," +
                                            "codvend: ''," +
                                            "bairro: ''," +
                                            "Documento: ''," +
                                            "DataAniversario: ''," +
                                            "Logradouro: ''," +
                                            "complemento: ''," +
                                            "uf: ''," +
                                            "observacao: ''," +
                                            "cep: ''," +
                                            "Cidade: ''," +
                                            "emails: [{email: ''}]," +
                                            "telefones: [{numero: ''," +
                                            "numero: ''}]," +
                                            "dias_contato: []," +
                                            "itens_contato: []},";
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";*/
                                }
                                String Dependentes = "{nome: ''," +
                                        "datanascimento: ''," +
                                        "redident: ''," +
                                        "codclie: ''}";
                                Jcliente = Jcliente + ",dependentes: " + "[" + Dependentes + "]}";

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                if (senha != null) {
                                    soap.addProperty("aJson", Jcliente);
                                    soap.addProperty("aUsuario", usuario);
                                    soap.addProperty("aSenha", senha);
                                } else {
                                    soap.addProperty("aJson", Jcliente);
                                    soap.addProperty("aUsuario", user);
                                    soap.addProperty("aSenha", pass);
                                }
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);

                                int j = 0;
                                do {
                                    try {
                                        if (j > 0) {
                                            Thread.sleep(500);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
                                        if (ConexOk) {
                                            if (j == 0) {
                                                Envio.call("", envelope);

                                                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                                RetClieEnvio = (String) envelope.getResponse();
                                                Util.gravaContatoSincronizado(ctxEnvClie, RetClieEnvio, codContatoInt);
                                                System.out.println("Response :" + resultsRequestSOAP.toString());
                                            } else {
                                                SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                                if (senha != null) {
                                                    newsoap.addProperty("aJson", Jcliente);
                                                    newsoap.addProperty("aUsuario", usuario);
                                                    newsoap.addProperty("aSenha", senha);
                                                } else {
                                                    newsoap.addProperty("aJson", Jcliente);
                                                    newsoap.addProperty("aUsuario", user);
                                                    newsoap.addProperty("aSenha", pass);
                                                }
                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);

                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetClieEnvio = (String) envelope.getResponse();
                                                Util.gravaContatoSincronizado(ctxEnvClie, RetClieEnvio, codContatoInt);
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            }

                                        } else {
                                            sincclieenvstatic = ctxEnvClie.getString(R.string.no_connection);
                                            return sincclieenvstatic;
                                        }
                                    } catch (Exception e) {
                                        e.toString();
                                        sincclieenvstatic = ctxEnvClie.getString(R.string.failure_communicate);
                                    }
                                    j += 1;
                                } while (RetClieEnvio == null && j <= 20);
                                if (RetClieEnvio == null) {
                                    sincclieenvstatic = ctxEnvClie.getString(R.string.failure_communicate);
                                    return sincclieenvstatic;
                                }
                                try {

                                } catch (Exception e) {
                                    sincclieenvstatic = ctxEnvClie.getString(R.string.failed_return);
                                    return sincclieenvstatic;
                                }
                            } catch (Exception E) {
                                E.toString();
                                return sincclieenvstatic;
                            }
                        }
                        while (jumpTime < totalProgressTime);
                    }
                    try {
                        if (!RetClieEnvio.equals("0")) {
                            Cursor CursClieAtu = DB.rawQuery(" SELECT * FROM CLIENTES WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "' AND CODPERFIL = " + idPerfil, null);
                            CursClieAtu.moveToFirst();
                            if (CursClieAtu.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET FLAGINTEGRADO = '2', CODCLIE_EXT = " + RetClieEnvio + " WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'AND CODPERFIL = " + idPerfil);
                            }
                            sincclieenvstatic = ctxEnvClie.getString(R.string.newcustomers_successfully);
                            CursClieAtu.close();
                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                }
                while (CursorClieEnv.moveToNext());
                CursorClieEnv.close();
            } else {
                sincclieenvstatic = ctxEnvClie.getString(R.string.no_new_clients);
                return sincclieenvstatic;
            }


        } catch (Exception E) {
            System.out.println("Error" + E);
        }

        return sincclieenvstatic;
    }

    public static String sincronizaContatosOutros(final Context ctxEnvCont, String user, String pass, int Codclie, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {

        SharedPreferences prefsHost = ctxEnvCont.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxEnvCont.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincContatoEnvio = "0";

        String METHOD_NAME = "CarregarContatos",
                CONTATO_INFO = "Contatos",
                PRODUTOS_INFO = "itens_contato",
                HORARIOS_INFO = "dias_contato",
                NOME_CONTATO = "nome",
                CARGO_CONTATO = "cargo",
                COD_CONTATO = "codcontat",
                TIPO_CONTATO = "tipo",
                CODCLIE_CONTATO = "codclie",
                CODVEND_CONTATO = "codvend",
                CODCARGO_CONTATO = "codcargo",
                CARGOATIVO_CONTATO = "cargoativo",
                SETOR_CONTATO = "setor",
                EMAIL_CONTATO = "email",
                TELEFONES_CONTATOS = "telefones",
                DOC_CONTATOS = "Documento",
                DATA_CONTATO = "DataAniversario",
                ENDERECO_CONTATO = "Logradouro",
                COMPL_CONTATO = "complemento",
                BAIRRO_CONTATO = "Bairro",
                CIDADE_COONTATO = "Cidade",
                CEP_CONTATO = "cep",
                UF_CONTATO = "uf",
                OBS_CONTATO = "observacao",
                CODITEM_CONTATO = "codigoitem";

        String CodVendedor = codVendedor;
        String DtUlt = null;
        try {
            //if (Codclie == 0) {

            Cursor cursorparamapp = DB.rawQuery("SELECT DT_ULT_ATU_CONT FROM USUARIOS WHERE CODPERFIL = " + idPerfil + " AND USUARIO = '" + user + "' AND SENHA = '" + pass + "'", null);
            cursorparamapp.moveToFirst();

            DtUlt = cursorparamapp.getString(cursorparamapp.getColumnIndex("DT_ULT_ATU_CONT"));
            if (DtUlt == null) {
                DtUlt = "01/01/2000 10:00:00";
            }
            cursorparamapp.close();
            //}
        } catch (Exception e) {
            e.toString();
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        if (senha != null) {
            if (Codclie == 0) {
                soap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            } else {
                soap.addProperty("aParam", "C" + Codclie);
                soap.addProperty("aUsuario", usuario);
                soap.addProperty("aSenha", senha);
            }
        } else {
            if (Codclie == 0) {
                soap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            } else {
                soap.addProperty("aParam", "C" + Codclie);
                soap.addProperty("aUsuario", user);
                soap.addProperty("aSenha", pass);
            }
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
        String RetContatos = null;

        Boolean ConexOk = Util.checarConexaoCelular(ctxEnvCont);

        if (ConexOk == true) {
            try {
                Cursor cursorVerificaCont = DB.rawQuery("SELECT * FROM CONTATO WHERE CODPERFIL =" + idPerfil, null);
                if (cursorVerificaCont.getCount() == 0 && DialogECB != null && DtUlt.equals("01/01/2000 10:00:00")) {
                    /*hd.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogECB.setTitle(R.string.wait);
                            DialogECB.setMessage(ctxEnvCont.getString(R.string.primeira_sync_contatos));
                            DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            DialogECB.setIcon(R.drawable.icon_sync);
                            DialogECB.setCancelable(false);
                            DialogECB.show();
                        }
                    });*/
                    int i = 0;
                    do {
                        try {
                            if (i > 0) {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        public void run() {
                                            DialogECB.setMessage(ctxEnvCont.getString(R.string.primeira_sync_contatos));
                                        }
                                    });
                                }
                                Thread.sleep(500);
                            }
                            final int y = i;

                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                DialogECB.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk == true) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetContatos = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        newsoap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
                                        newsoap.addProperty("aUsuario", user);
                                        newsoap.addProperty("aSenha", pass);
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        envelope.setOutputSoapObject(soap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetContatos = (String) newenvelope.getResponse();
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxEnvCont, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                cursorVerificaCont.close();
                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxEnvCont, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetContatos == null && i <= 6);
                    if (DialogECB != null) {
                        DialogECB.dismiss();
                    }
                } else {
                    int i = 0;
                    if (Codclie == 0) {
                        if (hd != null) {
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage("Por favor aguarde, realizando conexão com o servidor...");
                                }
                            });
                        }
                    }
                    do {
                        try {
                            if (i > 0) {
                                Thread.sleep(500);
                            }
                            final int y = i;
                            switch (i) {
                                case 1:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 2:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 5:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                                case 6:
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            public void run() {
                                                dialog.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... Tentativa " + y + "/6");
                                            }
                                        });
                                    }
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ConexOk == true) {

                                try {
                                    if (i == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetContatos = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                                        if (senha != null) {
                                            if (Codclie == 0) {
                                                newsoap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);
                                            } else {
                                                newsoap.addProperty("aParam", "C" + Codclie);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);
                                            }
                                        } else {
                                            if (Codclie == 0) {
                                                newsoap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);
                                            } else {
                                                newsoap.addProperty("aParam", "C" + Codclie);
                                                newsoap.addProperty("aUsuario", user);
                                                newsoap.addProperty("aSenha", pass);
                                            }
                                        }
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        newenvelope.setOutputSoapObject(newsoap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 900000);
                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetContatos = (String) newenvelope.getResponse();
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    if (hd != null) {
                                        hd.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ctxEnvCont, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }

                                cursorVerificaCont.close();

                            } else {
                                if (hd != null) {
                                    hd.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ctxEnvCont, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Erro na solicitação" + e);
                        }
                        i = i + 1;
                    } while (RetContatos == null && i <= 6);
                }
            } catch (Exception e) {
                e.toString();
                if (hd != null) {
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctxEnvCont, R.string.failure_communicate, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

        } else {
            if (hd != null) {
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctxEnvCont, R.string.no_connection, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        if (RetContatos.equals("0")) {
            sincContatoEnvio = ctxEnvCont.getString(R.string.syn_contatos_successfully);
            return sincContatoEnvio;
        } else if (RetContatos == null) {
            sincContatoEnvio = ctxEnvCont.getString(R.string.failure_communicate);
            return sincContatoEnvio;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetContatos);
            JSONArray pedidosblq = jsonObj.getJSONArray(CONTATO_INFO);


            int jumpTime = 0;
            final int totalProgressTime = pedidosblq.length();

            if (Codclie == 0) {
                if (dialog != null) {
                    dialog.setProgress(jumpTime);
                    dialog.setMax(totalProgressTime);
                }
            }
            String CodCliente = null;
            String CodClieExt = null;

            DB = new ConfigDB(ctxEnvCont).getReadableDatabase();

            for (int i = 0; i < pedidosblq.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = pedidosblq.getJSONObject(jumpTime);

                        String Telefone = c.getString(TELEFONES_CONTATOS);
                        Telefone = "{\"telefones\":" + Telefone + "\t}";
                        JSONObject ObjTel = new JSONObject(Telefone);
                        JSONArray Telef = ObjTel.getJSONArray("telefones");
                        String Tel1 = null;
                        String Tel2 = null;
                        String Tel3 = null;

                        for (int t = 0; t < Telef.length(); t++) {
                            JSONObject tt = Telef.getJSONObject(t);
                            if (t == 0) {
                                Tel1 = tt.getString("numero");
                            }
                            if (t == 1) {
                                Tel2 = tt.getString("numero");
                            }
                            if (t == 2) {
                                Tel3 = tt.getString("numero");
                            }
                        }

                        jumpTime += 1;
                        if (Codclie == 0) {
                            if (dialog != null) {
                                dialog.setProgress(jumpTime);
                                hd.post(new Runnable() {
                                    public void run() {
                                        dialog.setMessage(ctxEnvCont.getString(R.string.sync_contacts));
                                    }
                                });
                            }
                        }

                        Cursor cursorContato = DB.rawQuery("SELECT CONTATO.* " +
                                "FROM CONTATO WHERE CODCONTATO_EXT = " + c.getString(COD_CONTATO) + " and CODPERFIL = " + idPerfil, null);
                        cursorContato.moveToFirst();
                        //String CodEstado = RetornaEstado(c.getString(UF_CONTATO));

                        int codContato = c.getInt(COD_CONTATO);
                        int codCargoContato = 0;
                        if (c.getString(CODCARGO_CONTATO).equals("")) {
                            codCargoContato = 0;
                        } else {
                            codCargoContato = Integer.parseInt(c.getString(CODCARGO_CONTATO));
                        }
                        try {
                            if (cursorContato.getCount() > 0) {
                                DB.execSQL(" UPDATE CONTATO SET CODCLIENTE = " + c.getString(CODCLIE_CONTATO) + ", " +
                                        "    CODCONTATO_EXT = " + c.getString(COD_CONTATO) + ", " +
                                        "    NOME = '" + c.getString(NOME_CONTATO) + "', " +
                                        "    CARGO = '" + c.getString(CARGO_CONTATO) + "', " +
                                        "    CODCARGO = " + codCargoContato + ", " +
                                        "    SETOR  = '" + c.getString(SETOR_CONTATO) + "', " +
                                        "    DOCUMENTO = '" + c.getString(DOC_CONTATOS) + "', " +
                                        "    DATA = '" + c.getString(DATA_CONTATO) + "', " +
                                        "    CEP = '" + c.getString(CEP_CONTATO) + "', " +
                                        "    ENDERECO = '" + c.getString(ENDERECO_CONTATO) + "', " +
                                        "    COMPLEMENTO = '" + c.getString(COMPL_CONTATO) + "', " +
                                        "    UF = '" + c.getString(UF_CONTATO) + "', " +
                                        "    CODVENDEDOR = " + c.getString(CODVEND_CONTATO) + ", " +
                                        "    BAIRRO = '" + c.getString(BAIRRO_CONTATO) + "', " +
                                        "    DESC_CIDADE = '" + c.getString(CIDADE_COONTATO) + "', " +
                                        "    EMAIL = '" + c.getString(EMAIL_CONTATO) + "', " +
                                        "    TIPO = '" + c.getString(TIPO_CONTATO) + "', " +
                                        "    OBS = '" + c.getString(OBS_CONTATO) + "', " +
                                        //"    CODCLIE_EXT = " + Codclie + ", " +
                                        "    FLAGINTEGRADO = 'S', " +
                                        "    CODPERFIL = " + idPerfil +
                                        " WHERE CODCONTATO_EXT = " + c.getString(COD_CONTATO) + " and CODPERFIL = " + idPerfil);
                            } else {
                                DB.execSQL("INSERT INTO CONTATO (CODCLIENTE, CODCONTATO_EXT, NOME, CARGO, CODCARGO, SETOR, TEL1, TEL2, DOCUMENTO, " +
                                        "DATA, CEP, SETOR, " +
                                        "ENDERECO, COMPLEMENTO, UF, CODVENDEDOR, BAIRRO, DESC_CIDADE, EMAIL, TIPO, OBS, FLAGINTEGRADO, CODPERFIL) " +
                                        "VALUES(" +
                                        "" + c.getString(CODCLIE_CONTATO) +
                                        "," + c.getString(COD_CONTATO) +
                                        ",'" + c.getString(NOME_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(CARGO_CONTATO).trim().replace("'", "''") +
                                        "'," + codCargoContato +
                                        ",'" + c.getString(SETOR_CONTATO) +
                                        "','" + Tel1 +
                                        "','" + Tel2 +
                                        "','" + c.getString(DOC_CONTATOS).trim().replace("'", "''") +
                                        "','" + c.getString(DATA_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(CEP_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(SETOR_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(ENDERECO_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(COMPL_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(UF_CONTATO).trim().replace("'", "''") +
                                        "'," + c.getString(CODVEND_CONTATO) +
                                        ",'" + c.getString(BAIRRO_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(CIDADE_COONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(EMAIL_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(TIPO_CONTATO).trim().replace("'", "''") +
                                        "','" + c.getString(OBS_CONTATO).trim().replace("'", "''").replace("\n", "' '") +
                                        "','" + "S" +
                                        "'," + idPerfil + ");");
                            }

                            //-------------------CADASTRO E ATUALIZAÇÃO CARGOS----------------------------

                            //if(!c.getString(CODCARGO_CONTATO).equals("")) {
                            try {
                                    /*Cursor cursor1 = DB.rawQuery(" SELECT CODCARGO_EXT, ATIVO, DES_CARGO FROM CARGOS WHERE DES_CARGO = '" + c.getString(CARGO_CONTATO) + "' " +
                                            " and CODCARGO_EXT = " + c.getInt(CODCARGO_CONTATO), null);
                                    cursor1.moveToFirst();*/

                                //if (cursor1.getCount() > 0) {
                                String desc_cargo = c.getString(CARGO_CONTATO);
                                String codCargo = c.getString(CODCARGO_CONTATO);
                                String ativCargo = c.getString(CARGOATIVO_CONTATO);

                                Util.atualizaCargoContato(desc_cargo, codCargo, ativCargo, ctxEnvCont);
                                //}
                                //cursor1.close();
                            } catch (Exception E) {
                                E.toString();
                            }
                            //}

                            //-------------------CADASTRO DOS ITENS DO CONTATO----------------------------

                            String Produtos = c.getString(PRODUTOS_INFO);
                            Produtos = "{\"itens_contato\":" + Produtos + "\t}";
                            JSONObject ObjProd = new JSONObject(Produtos);
                            JSONArray Prod = ObjProd.getJSONArray("itens_contato");


                            Cursor cursProd = null;
                            for (int iProd = 0; iProd < Prod.length(); iProd++) {

                                JSONObject pp = Prod.getJSONObject(iProd);

                                int codProdCont = pp.getInt("codigoitem");

                                DB = new ConfigDB(ctxEnvCont).getReadableDatabase();
                                cursProd = DB.rawQuery("select cod_externo_contato, cod_item " +
                                        "from produtos_contatos " +
                                        "where cod_externo_contato = " + c.getString(COD_CONTATO) + " and " +
                                        "cod_item = " + codProdCont, null);
                                if (cursProd.getCount() == 0) {
                                    DB.execSQL("insert into produtos_contatos (cod_externo_contato, cod_item) values (" +
                                            c.getString(COD_CONTATO) + "," +
                                            codProdCont +");");
                                }
                                cursProd.close();
                            }

                            //-------------------------------CADASTRO DE DIAS AGENDA-------------------------------

                            String Agenda = c.getString(HORARIOS_INFO);
                            Agenda = "{\"dias_contato\":" + Agenda + "\t}";
                            JSONObject ObjAg = new JSONObject(Agenda);
                            JSONArray Ag = ObjAg.getJSONArray("dias_contato");

                            Cursor cursAg = null;
                            for (int iAg = 0; iAg < Ag.length(); iAg++) {

                                JSONObject pp = Ag.getJSONObject(iAg);

                                int codDiaSemana = pp.getInt("cod_dia_semana");
                                String hrInicio = pp.getString("hora_inicio");
                                String hrFinal = pp.getString("hora_final");

                                Util.gravaHorariosContatos(ctxEnvCont, hrInicio, hrFinal, codDiaSemana, codContato);
                            }
                            cursorContato.close();

                        } catch (Exception E) {
                            E.toString();
                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
        } catch (
                JSONException e)

        {
            e.toString();
        }

        try {
            if (Codclie == 0) {
                try {
                    DtUlt = Util.DataHojeComHorasMinSecBR();
                    DB.execSQL("UPDATE USUARIOS SET DT_ULT_ATU_CONT = '" + DtUlt + "' WHERE CODPERFIL = " + idPerfil + " AND USUARIO = '" + user + "' AND SENHA = '" + pass + "'");
                } catch (Exception e) {
                    e.toString();
                }
            }
        } catch (Exception E) {
            E.toString();
        }

        return sincContatoEnvio;

    }

    public static String sincronizaContatosEnvio(final Context ctxEnvClie, String user, String pass, final ProgressDialog DialogoECB, final ProgressDialog dialog, Handler hd) {

        SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxEnvClie.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincclieenvstatic = "0";
        String Jcliente = null;
        String METHOD_NAMEENVIO = "Cadastrar";
        DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

        String RetClieEnvio = null;
        int codContatoInt = 0;
        try {
            Cursor cursorContatos = DB.rawQuery("select NOME, CARGO, TIPO, CODCLIE_EXT, CODVENDEDOR, BAIRRO, DOCUMENTO, DATA, " +
                    "ENDERECO, COMPLEMENTO, UF, OBS, CEP, DESC_CIDADE, EMAIL, TEL1, TEL2, CODCONTATO_INT, SETOR, CODCARGO, " +
                    "CODCONTATO_EXT " +
                    "from CONTATO where FLAGINTEGRADO = 'N' AND CODPERFIL = " + idPerfil, null);

            String EnvioContato = "";
            int codContInt = 0;

            int jumpTime = 0;
            String CodClie_ext = null;
            String sexo = null;
            final int totalProgressTime = cursorContatos.getCount();
            if (dialog != null) {
                dialog.setProgress(jumpTime);
                dialog.setMax(totalProgressTime);
            }

            if (cursorContatos.getCount() > 0) {
                cursorContatos.moveToFirst();

                do {
                    try {
                        jumpTime += 1;
                        if (dialog != null) {
                            dialog.setProgress(jumpTime);
                            hd.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage("Sincronizando contatos...");
                                }
                            });
                        }

                        String NOME = cursorContatos.getString(cursorContatos.getColumnIndex("NOME"));
                        codContInt = cursorContatos.getInt(cursorContatos.getColumnIndex("CODCONTATO_INT"));
                        String CARGO = cursorContatos.getString(cursorContatos.getColumnIndex("CARGO"));
                        String CEP = cursorContatos.getString(cursorContatos.getColumnIndex("CEP")).replace("-", "");
                        String TEL1 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL1"));
                        String TEL2 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL2"));
                        String BAIRRO = cursorContatos.getString(cursorContatos.getColumnIndex("BAIRRO"));
                        String CODCONTATO = cursorContatos.getString(cursorContatos.getColumnIndex("CODCONTATO_EXT"));

                        EnvioContato = "{nome: '" + NOME + "'," +
                                "cargo: '" + CARGO + "'," +
                                "codcargo: '" + cursorContatos.getString(cursorContatos.getColumnIndex("CODCARGO")) + "'," +
                                "tipo: '" + cursorContatos.getString(cursorContatos.getColumnIndex("TIPO")) + "'," +
                                "codclie: '" + cursorContatos.getInt(cursorContatos.getColumnIndex("CODCLIE_EXT")) + "'," +
                                "codvend: '" + cursorContatos.getInt(cursorContatos.getColumnIndex("CODVENDEDOR")) + "'," +
                                "bairro: '" + BAIRRO + "'," +
                                "Documento: '" + cursorContatos.getString(cursorContatos.getColumnIndex("DOCUMENTO")) + "'," +
                                "DataAniversario: '" + cursorContatos.getString(cursorContatos.getColumnIndex("DATA")) + "'," +
                                "Logradouro: '" + cursorContatos.getString(cursorContatos.getColumnIndex("ENDERECO")) + "'," +
                                "complemento: '" + cursorContatos.getString(cursorContatos.getColumnIndex("COMPLEMENTO")) + "'," +
                                "cod_contat: '" + cursorContatos.getString(cursorContatos.getColumnIndex("CODCONTATO_EXT")) + "'," +
                                "uf: '" + cursorContatos.getString(cursorContatos.getColumnIndex("UF")) + "'," +
                                "observacao: '" + cursorContatos.getString(cursorContatos.getColumnIndex("OBS")) + "'," +
                                "cep: '" + CEP + "'," +
                                "setor: '" + cursorContatos.getString(cursorContatos.getColumnIndex("SETOR")) + "'," +
                                "Cidade: '" + cursorContatos.getString(cursorContatos.getColumnIndex("DESC_CIDADE")) + "'," +
                                "emails: [{email: '" + cursorContatos.getString(cursorContatos.getColumnIndex("EMAIL")) + "'}]," +
                                "telefones: [{numero: '" + TEL1 + "'," +
                                "numero: '" + TEL2 + "'}]," +
                                "dias_contato: [" + retornaJsonHorariosAgenda(cursorContatos.getString(cursorContatos.getColumnIndex("CODCONTATO_INT")), ctxEnvClie) + "]," +
                                "itens_contato: [" + retornaItens(cursorContatos.getString(cursorContatos.getColumnIndex("CODCONTATO_INT")), ctxEnvClie) + "]},";

                        //EnvioContato = "contatos: " + "[" + EnvioContato + "]";

                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);

                        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_CONTATOENVIO);
                        if (senha != null) {
                            soap.addProperty("aJson", EnvioContato);
                            soap.addProperty("aUsuario", usuario);
                            soap.addProperty("aSenha", senha);
                        } else {
                            soap.addProperty("aJson", EnvioContato);
                            soap.addProperty("aUsuario", user);
                            soap.addProperty("aSenha", pass);
                        }
                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        envelope.setOutputSoapObject(soap);
                        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);

                        int j = 0;
                        do {
                            try {
                                if (j > 0) {
                                    Thread.sleep(500);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
                                if (ConexOk) {
                                    if (j == 0) {
                                        Envio.call("", envelope);

                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                        RetClieEnvio = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                        Util.gravaContatoSincronizado(ctxEnvClie, Util.retornaCodContato(RetClieEnvio), codContInt);
                                        Util.atualizaCargoContato(CARGO, Util.verificaString(RetClieEnvio), "S", ctxEnvClie);

                                    } else {
                                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_CONTATOENVIO);
                                        if (senha != null) {
                                            newsoap.addProperty("aJson", EnvioContato);
                                            newsoap.addProperty("aUsuario", usuario);
                                            newsoap.addProperty("aSenha", senha);
                                        } else {
                                            newsoap.addProperty("aJson", EnvioContato);
                                            newsoap.addProperty("aUsuario", user);
                                            newsoap.addProperty("aSenha", pass);
                                        }
                                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                        newenvelope.setOutputSoapObject(newsoap);
                                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);

                                        newEnvio.call("", newenvelope);

                                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                        RetClieEnvio = (String) envelope.getResponse();
                                        Util.gravaContatoSincronizado(ctxEnvClie, Util.retornaCodContato(RetClieEnvio), codContInt);
                                        Util.atualizaCargoContato(CARGO, Util.verificaString(RetClieEnvio), "S", ctxEnvClie);
                                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                                    }

                                } else {
                                    sincclieenvstatic = ctxEnvClie.getString(R.string.no_connection);
                                    return sincclieenvstatic;
                                }
                            } catch (Exception e) {
                                e.toString();
                                sincclieenvstatic = ctxEnvClie.getString(R.string.failure_communicate);
                            }
                        } while (RetClieEnvio == null && j <= 20);
                        if (RetClieEnvio == null) {
                            sincclieenvstatic = ctxEnvClie.getString(R.string.failure_communicate);
                            return sincclieenvstatic;
                        }
                        try {

                        } catch (Exception e) {
                            sincclieenvstatic = ctxEnvClie.getString(R.string.failed_return);
                            return sincclieenvstatic;
                        }
                        j = +1;
                    } catch (Exception E) {
                        E.toString();
                    }
                } while (cursorContatos.moveToNext());
                cursorContatos.close();
            }
        } catch (Exception E) {
            sincclieenvstatic = ctxEnvClie.getString(R.string.failed_return);
            return sincclieenvstatic;
        }
        return sincclieenvstatic;
    }

    public static String sincronizaPedidosEnvio(String sUsuario, String sSenha, final Context ctxPedEnv, String NumPedido, final ProgressDialog dialog, final ProgressDialog DialogECB, Handler hd) {
        SharedPreferences prefsHost = ctxPedEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxPedEnv.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincpedenviostatic = "";
        if (NumPedido.equals("0")) {

            String JPedidos = null;
            String METHOD_NAMEENVIO = "CadastrarPedidos";
            DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
            Cursor CursorPedido;
            String RetClieEnvio = null;

            try {
                CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE FLAGINTEGRADO = '5' AND CODPERFIL = " + idPerfil, null);

                int jumpTime = 0;
                final int totalProgressTime = CursorPedido.getCount();
                CursorPedido.moveToFirst();
                if (dialog != null) {
                    dialog.setProgress(jumpTime);
                    dialog.setMax(totalProgressTime);
                }

                if (CursorPedido.getCount() > 0) {
                    CursorPedido.moveToFirst();
                    do {
                        for (int i = 0; i < CursorPedido.getCount(); i++) {

                            try {
                                jumpTime += 1;
                                if (dialog != null) {
                                    dialog.setProgress(jumpTime);
                                    hd.post(new Runnable() {
                                        public void run() {
                                            dialog.setMessage(ctxPedEnv.getString(R.string.updating_tables));
                                        }
                                    });
                                }

                                int CodClie_Int = CursorPedido.getInt(CursorPedido.getColumnIndex("CODCLIE"));

                                Cursor CursorClie = DB.rawQuery("SELECT CODCLIE_EXT,CODPERFIL,FLAGINTEGRADO FROM CLIENTES WHERE CODCLIE_INT = '" + CodClie_Int + "' AND CODPERFIL = " + idPerfil, null);
                                CursorClie.moveToFirst();
                                int CodClie_Ext = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_EXT"));
                                String FlagIntegrado = CursorClie.getString(CursorClie.getColumnIndex("FLAGINTEGRADO"));
                                CursorClie.close();

                                String vltotal = CursorPedido.getString(CursorPedido.getColumnIndex("VALORTOTAL")).replace(".", ",");
                                BigDecimal vendatotal = new BigDecimal(Double.parseDouble(vltotal.replace(',', '.')));
                                String vltotalvenda = vendatotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                vltotalvenda = vltotalvenda.replace('.', ',');


                                String ValorFrete = CursorPedido.getString(CursorPedido.getColumnIndex("VLFRETE"));
                                if (Util.isNullOrEmpty(ValorFrete)) {
                                    ValorFrete = "0";
                                }
                                String ValorSeguro = CursorPedido.getString(CursorPedido.getColumnIndex("VALORSEGURO"));
                                if (Util.isNullOrEmpty(ValorSeguro)) {
                                    ValorSeguro = "0";
                                }

                                String Observacao = CursorPedido.getString(CursorPedido.getColumnIndex("OBS"));
                                String line_separator = System.getProperty("line.separator");
                                String OBS = Observacao.replaceAll("\n|" + line_separator, "");
                                String vldesconto = CursorPedido.getString(CursorPedido.getColumnIndex("VLDESCONTO"));
                                if (vldesconto == null) {
                                    vldesconto = "0";
                                } else {
                                    vldesconto = vldesconto.replace(".", ",");
                                }
                                String dataEmUmFormato = CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS"));
                                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                                Date data = formato.parse(dataEmUmFormato);
                                formato.applyPattern("dd/MM/yyyy");
                                String sDataVenda = formato.format(data);


                                JPedidos = "{codclie_ext: '" + CodClie_Ext + "'," +
                                        "data_emissao: '" + sDataVenda + "'," +
                                        "data_geracao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAVENDA")) + "'," +
                                        "valor_mercad: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLMERCAD")).replace(".", ",") + "'," +
                                        "valor_frete: '" + ValorFrete + "'," +
                                        "valor_seguro: '" + ValorSeguro + "'," +
                                        "dataentregaprevista: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAPREVISTAENTREGA")) + "'," +
                                        "valor_desconto: '" + vldesconto + "'," +
                                        "obs_pedido: '" + OBS + "'," +
                                        "numpedido_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED")) + "'," +
                                        "chavePedido: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'," +
                                        "codempresa: '" + 1 + "'," +
                                        "cod_vendedor: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODVENDEDOR")) + "',";

                                String PedItems = "";
                                Cursor CursorItensEnv = DB.rawQuery(" SELECT * FROM PEDITENS WHERE CHAVEPEDIDO = '" +
                                        CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);

                                CursorItensEnv.moveToFirst();
                                do {
                                    PedItems = PedItems + "{codigoitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODIGOITEM")) + "'," +
                                            "descricao: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")) + "'," +
                                            "numeroitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("NUMEROITEM")) + "'," +
                                            "qtdmenorped: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")) + "'," +
                                            "vlunit: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ",") + "'," +
                                            "valortotal: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ",") + "'}";

                                    if (!CursorItensEnv.isLast()) {
                                        PedItems = PedItems + ",";
                                    }

                                } while (CursorItensEnv.moveToNext());
                                CursorItensEnv.close();

                                if (PedItems != "") {
                                    JPedidos = JPedidos + "produtos: " + "[" + PedItems + "]";
                                }
                                String PedParcelas = "";
                                Cursor CursorParcelasEnv = DB.rawQuery(" SELECT * FROM CONREC WHERE vendac_chave = '" +
                                        CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                                CursorParcelasEnv.moveToFirst();
                                do {
                                    PedParcelas = PedParcelas + "{chavePedido: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave")) + "'," +
                                            "numparcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela")) + "'," +
                                            "cod_formapg: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("conf_codformpgto_ext")) + "'," +
                                            "valor_parcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber")).replace(".", ",") + "'," +
                                            "dias_parcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("conf_dias_vencimento")) + "'}";

                                    if (!CursorParcelasEnv.isLast()) {
                                        PedParcelas = PedParcelas + ",";
                                    }
                                } while (CursorParcelasEnv.moveToNext());
                                CursorParcelasEnv.close();

                                if (PedParcelas != "") {
                                    JPedidos = JPedidos + ",formapgto: " + "[" + PedParcelas + "]";
                                }

                                JPedidos = JPedidos + '}';

                                String sitcliexvend = Sincronismo.sincronizaSitClieXPed(vltotalvenda, ctxPedEnv, usuario, senha, CodClie_Ext);
                                if (sitcliexvend.equals("OK")) {

                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                    StrictMode.setThreadPolicy(policy);

                                    SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                    soap.addProperty("aJson", JPedidos);
                                    soap.addProperty("aUsuario", sUsuario);
                                    soap.addProperty("aSenha", sSenha);
                                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                    envelope.setOutputSoapObject(soap);
                                    HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                                    int j = 0;
                                    do {
                                        try {
                                            if (j > 0) {
                                                Thread.sleep(1000);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            Boolean ConexOk = Util.checarConexaoCelular(ctxPedEnv);
                                            if (ConexOk) {
                                                if (j == 0) {
                                                    Envio.call("", envelope);

                                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                                    RetClieEnvio = (String) envelope.getResponse();
                                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                                } else {
                                                    SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                                    newsoap.addProperty("aJson", JPedidos);
                                                    newsoap.addProperty("aUsuario", sUsuario);
                                                    newsoap.addProperty("aSenha", sSenha);
                                                    SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                    newenvelope.setOutputSoapObject(newsoap);
                                                    HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                                                    newEnvio.call("", newenvelope);

                                                    SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                    RetClieEnvio = (String) envelope.getResponse();
                                                    System.out.println("Response :" + newresultsRequestSOAP.toString());
                                                }
                                            } else {
                                                sincpedenviostatic = ctxPedEnv.getString(R.string.no_connection);
                                                return sincpedenviostatic;
                                            }
                                        } catch (Exception e) {
                                            e.toString();
                                            sincpedenviostatic = ctxPedEnv.getString(R.string.failure_communicate);

                                        }
                                    } while (RetClieEnvio == null && j <= 20);
                                    try {
                                        DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
                                        Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "' AND CODPERFIL = " + idPerfil, null);
                                        CursPedAtu.moveToFirst();
                                        if (CursPedAtu.getCount() > 0) {
                                            DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '2', NUMPEDIDOERP = " + RetClieEnvio + " WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "' AND CODPERFIL = " + idPerfil);
                                        }
                                        sincpedenviostatic = "OK";
                                        CursPedAtu.close();
                                    } catch (Exception E) {
                                        E.toString();
                                    }
                                } else {
                                    sincpedenviostatic = sitcliexvend;
                                    return sincpedenviostatic;
                                }
                            } catch (Exception E) {
                                E.printStackTrace();
                            }
                        }
                        JPedidos = "";
                    }
                    while (CursorPedido.moveToNext());
                    CursorPedido.close();
                } else {
                    sincpedenviostatic = "Nenhum pedido a ser enviado.";
                    return sincpedenviostatic;
                }

            } catch (Exception E) {
                System.out.println("Error" + E);
            }
        } else {

            String JPedidos = null;
            String METHOD_NAMEENVIO = "CadastrarPedidos";
            DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
            Cursor CursorPedido;
            Cursor CursorCliente;
            int CodClie_Ext = 0;
            int CodClie_Int;

            try {
                CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + NumPedido + "' AND CODPERFIL = " + idPerfil, null);
                CursorPedido.moveToFirst();
                CodClie_Int = CursorPedido.getInt(CursorPedido.getColumnIndex("CODCLIE"));

                CursorCliente = DB.rawQuery("SELECT CODCLIE_EXT FROM CLIENTES WHERE CODCLIE_INT = " + CodClie_Int + " AND CODPERFIL =" + idPerfil, null);
                CursorCliente.moveToFirst();
                CodClie_Ext = CursorCliente.getInt(CursorCliente.getColumnIndex("CODCLIE_EXT"));
                CursorCliente.close();

                String RetClieEnvio = null;

                int jumpTime = 0;
                final int totalProgressTime = CursorPedido.getCount();
                if (dialog != null) {
                    dialog.setProgress(jumpTime);
                    dialog.setMax(totalProgressTime);
                }

                if (CursorPedido.getCount() > 0) {
                    CursorPedido.moveToFirst();
                    do {
                        for (int i = 0; i < CursorPedido.getCount(); i++) {
                            do try {
                                jumpTime += 1;
                                if (dialog != null) {
                                    dialog.setProgress(jumpTime);
                                    hd.post(new Runnable() {
                                        public void run() {
                                            dialog.setMessage(ctxPedEnv.getString(R.string.updating_tables));
                                        }
                                    });
                                }

                                String ValorFrete = CursorPedido.getString(CursorPedido.getColumnIndex("VLFRETE"));
                                if (Util.isNullOrEmpty(ValorFrete)) {
                                    ValorFrete = "0";
                                }
                                String ValorSeguro = CursorPedido.getString(CursorPedido.getColumnIndex("VALORSEGURO"));
                                if (Util.isNullOrEmpty(ValorSeguro)) {
                                    ValorSeguro = "0";
                                }
                                String OBS = CursorPedido.getString(CursorPedido.getColumnIndex("OBS"));
                                String line_separator = System.getProperty("line.separator");
                                String Observacao = OBS.replaceAll("\n|" + line_separator, " ");
                                String vldesconto = CursorPedido.getString(CursorPedido.getColumnIndex("VLDESCONTO"));
                                if (vldesconto == null) {
                                    vldesconto = "0";
                                } else {
                                    vldesconto = vldesconto.replace(".", ",");
                                }

                                String dataEmUmFormato = CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS"));
                                dataEmUmFormato = Util.FormataDataDDMMAAAA_ComHoras(dataEmUmFormato);

                                JPedidos = "{codclie_ext: '" + CodClie_Ext + "'," +
                                        "data_emissao: '" + dataEmUmFormato + "'," +
                                        "data_geracao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAVENDA")) + "'," +
                                        "valor_mercad: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLMERCAD")).replace(".", ",") + "'," +
                                        "valor_frete: '" + ValorFrete + "'," +
                                        "valor_seguro: '" + ValorSeguro + "'," +
                                        "dataentregaprevista: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAPREVISTAENTREGA")) + "'," +
                                        "valor_desconto: '" + vldesconto + "'," +
                                        "obs_pedido: '" + Observacao + "'," +
                                        "numpedido_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED")) + "'," +
                                        "chavePedido: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'," +
                                        "codempresa: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODEMPRESA")) + "'," +
                                        "cod_vendedor: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODVENDEDOR")) + "',";


                                String PedItems = "";
                                Cursor CursorItensEnv = DB.rawQuery(" SELECT * FROM PEDITENS WHERE CHAVEPEDIDO = '" +
                                        CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);

                                CursorItensEnv.moveToFirst();
                                do {
                                    PedItems = PedItems + "{codigoitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODIGOITEM")) + "'," +
                                            "descricao: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")) + "'," +
                                            "numeroitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("NUMEROITEM")) + "'," +
                                            "qtdmenorped: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")) + "'," +
                                            "vlunit: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ",") + "'," +
                                            "valortotal: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ",") + "'}";

                                    if (!CursorItensEnv.isLast()) {
                                        PedItems = PedItems + ",";
                                    }

                                } while (CursorItensEnv.moveToNext());
                                CursorItensEnv.close();

                                if (PedItems != "") {
                                    JPedidos = JPedidos + "produtos: " + "[" + PedItems + "]";
                                }
                                String PedParcelas = "";
                                Cursor CursorParcelasEnv = DB.rawQuery(" SELECT * FROM CONREC WHERE vendac_chave = '" +
                                        CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                                CursorParcelasEnv.moveToFirst();
                                /*String teste1 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave"));
                                String teste2 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela"));
                                String teste3 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("conf_codformpgto_ext"));
                                String teste4 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber"));
                                String teste5 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("conf_dias_vencimento"));*/
                                String teste3 = CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_codformpgto_ext"));
                                do {
                                    PedParcelas = PedParcelas + "{chavePedido: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave")) + "'," +
                                            "numparcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela")) + "'," +
                                            "cod_formapg: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_codformpgto_ext")) + "'," +
                                            "valor_parcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber")).replace(".", ",") + "'," +
                                            "dias_parcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_dias_vencimento")) + "'}";

                                    if (!CursorParcelasEnv.isLast()) {
                                        PedParcelas = PedParcelas + ",";
                                    }
                                } while (CursorParcelasEnv.moveToNext());
                                CursorParcelasEnv.close();

                                if (PedParcelas != "") {
                                    JPedidos = JPedidos + ",formapgto: " + "[" + PedParcelas + "]";
                                }

                                JPedidos = JPedidos + '}';

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                soap.addProperty("aJson", JPedidos);
                                soap.addProperty("aUsuario", usuario);
                                soap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                                int j = 0;
                                do {
                                    try {
                                        if (j > 0) {
                                            Thread.sleep(1000);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        Boolean ConexOk = Util.checarConexaoCelular(ctxPedEnv);
                                        if (ConexOk) {
                                            if (j == 0) {
                                                Envio.call("", envelope);

                                                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                                                RetClieEnvio = (String) envelope.getResponse();
                                                System.out.println("Response :" + resultsRequestSOAP.toString());
                                            } else {
                                                SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                                newsoap.addProperty("aJson", JPedidos);
                                                newsoap.addProperty("aUsuario", usuario);
                                                newsoap.addProperty("aSenha", senha);
                                                SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                                newenvelope.setOutputSoapObject(newsoap);
                                                HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                                                newEnvio.call("", newenvelope);

                                                SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                                                RetClieEnvio = (String) newenvelope.getResponse();
                                                System.out.println("Response :" + newresultsRequestSOAP.toString());
                                            }
                                        } else {
                                            sincpedenviostatic = ctxPedEnv.getString(R.string.no_connection);
                                            return sincpedenviostatic;
                                        }
                                    } catch (Exception e) {
                                        e.toString();
                                        sincpedenviostatic = ctxPedEnv.getString(R.string.failure_communicate);
                                        return sincpedenviostatic;
                                    }
                                } while (RetClieEnvio == null && j <= 20);

                            } catch (Exception E) {
                                System.out.println("Error montar envio pedido" + E);
                            }
                            while (jumpTime < totalProgressTime);
                        }
                        try {
                            DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
                            if (!RetClieEnvio.equals("0")) {
                                Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "' AND CODPERFIL =" + idPerfil, null);
                                CursPedAtu.moveToFirst();
                                if (CursPedAtu.getCount() > 0) {
                                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '2', NUMPEDIDOERP = " + RetClieEnvio + " WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "' AND CODPERFIL =" + idPerfil);
                                }
                                sincpedenviostatic = "OK";
                                CursPedAtu.close();
                            } else {
                                sincpedenviostatic = "Nenhum pedido a ser enviado.";
                                return sincpedenviostatic;
                            }
                        } catch (Exception E) {
                            Toast.makeText(ctxPedEnv, E.toString(), Toast.LENGTH_SHORT).show();
                            return sincpedenviostatic;
                        }
                        JPedidos = "";
                    } while (CursorPedido.moveToNext());
                    CursorPedido.close();
                } else {
                    sincpedenviostatic = "Nenhum pedido a ser enviado.";
                    return sincpedenviostatic;
                }
            } catch (Exception E) {
                System.out.println("Error" + E);
            }

        }
        return sincpedenviostatic;
    }

    private static String RetornaEstado(String NomeEstado, Context ctxEnv) {
        String Estado = null;
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        try {
            Cursor CursosEstado = DB.rawQuery(" SELECT UF FROM ESTADOS WHERE UF = '" + NomeEstado + "' AND CODPERFIL = " + idPerfil, null);
            if (CursosEstado.getCount() > 0) {
                CursosEstado.moveToFirst();
                Estado = CursosEstado.getString(CursosEstado.getColumnIndex("UF"));
            } else {
                DB.execSQL("INSERT INTO ESTADOS VALUES('" + NomeEstado + "','" + NomeEstado + "', " + idPerfil + ");");
                Cursor cursor1 = DB.rawQuery(" SELECT UF FROM ESTADOS WHERE UF = '" + NomeEstado + "' AND CODPERFIL = " + idPerfil, null);
                cursor1.moveToFirst();
                Estado = cursor1.getString(cursor1.getColumnIndex("UF"));
                cursor1.close();
            }
            CursosEstado.close();
            return Estado;
        } catch (Exception E) {
            E.toString();
            return Estado;
        }
    }

    private static int RetornaCidade(String NomeCidade, String NomeEstado, final Context ctxEnv) {
        int Cidade = 0;
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);

        try {
            Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
            if (CursorCidade.getCount() > 0) {
                CursorCidade.moveToFirst();
                Cidade = CursorCidade.getInt(CursorCidade.getColumnIndex("CODCIDADE"));
            } else {

                DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODPERFIL)" +
                        " VALUES('" + NomeCidade + "','" + NomeEstado + "', " + idPerfil + ");");
                Cursor CursorCidade2 = DB.rawQuery(" SELECT CODCIDADE FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
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

    private static int RetornaBairro(String NomeBairro, int CodCidade, Context ctxEnv) {
        int Bairro = 0;
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        try {
            Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
            if (CursorBairro.getCount() > 0) {
                CursorBairro.moveToFirst();
                Bairro = CursorBairro.getInt(CursorBairro.getColumnIndex("CODBAIRRO"));
            } else {
                DB.execSQL(" INSERT INTO BAIRROS (CODCIDADE, DESCRICAO, CODPERFIL )" +
                        " VALUES('" + CodCidade + "','" + NomeBairro + "', " + idPerfil + ");");
                Cursor CursorBairro2 = DB.rawQuery(" SELECT CODBAIRRO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "'AND CODPERFIL = " + idPerfil, null);
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

    public static String sincronizaEmpresas(String sUsuario, String sSenha, final Context ctxEnv) {
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        String sincempresastatic = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "RetornaEmpresas");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS, 10000);
        String RetEmpresa = null;

        int j = 0;
        do {
            try {
                if (j > 0) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
                if (ConexOk) {
                    if (j == 0) {

                        Envio.call("", envelope);

                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                        RetEmpresa = (String) envelope.getResponse();
                        System.out.println("Response :" + resultsRequestSOAP.toString());
                    } else {
                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "RetornaEmpresas");
                        newsoap.addProperty("aUsuario", sUsuario);
                        newsoap.addProperty("aSenha", sSenha);
                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        newenvelope.setOutputSoapObject(newsoap);
                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS, 60000);

                        newEnvio.call("", newenvelope);

                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                        RetEmpresa = (String) envelope.getResponse();
                        System.out.println("Response :" + newresultsRequestSOAP.toString());
                    }

                } else {
                    sincempresastatic = ctxEnv.getString(R.string.no_connection);
                }

            } catch (Exception e) {
                e.toString();
                sincempresastatic = ctxEnv.getString(R.string.failure_communicate);
            }
            j = j + 1;
        } while (RetEmpresa == null && j <= 6);

        try {
            JSONObject jsonObj = new JSONObject(RetEmpresa);
            JSONArray JEmpresas = jsonObj.getJSONArray("empresas");

            int jumpTime = 0;
            final int totalProgressTime = JEmpresas.length();
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JEmpresas.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JEmpresas.getJSONObject(jumpTime);
                        jumpTime += 1;
                        int CodEmpresa = c.getInt("codigo");
                        String NomeEmpresa = c.getString("nome_empresa");
                        String NomeAbreviado = c.getString("nome_abreviado");
                        String Cnpj = c.getString("cnpj");
                        String Tel1 = c.getString("tel1");
                        String Tel2 = c.getString("tel2");
                        String Email = c.getString("email");
                        String Ativo = c.getString("ativo");

                        /*byte[] imgRecebida = null;

                        try {
                            String LogoEmpresa = c.getString("logo");
                            imgRecebida = Base64.decode(LogoEmpresa, Base64.DEFAULT);
                            Bitmap imgLogo = BitmapFactory.decodeByteArray(imgRecebida, 0, imgRecebida.length);
                        } catch (Exception e) {
                            e.toString();
                        }*/

                        Cursor CursorEmpresa = DB.rawQuery(" SELECT CODEMPRESA, CODPERFIL, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa + " AND CODPERFIL = " + idPerfil + "", null);
                        if (CursorEmpresa.getCount() > 0) {
                            DB.execSQL(" UPDATE EMPRESAS SET CODEMPRESA = '" + CodEmpresa +
                                    "', NOMEEMPRE = '" + NomeEmpresa +
                                    "', NOMEABREV = '" + NomeAbreviado +
                                    "', CNPJ = '" + Cnpj +
                                    "', TEL1 = '" + Tel1 +
                                    "', TEL2 = '" + Tel2 +
                                    "', EMAIL = '" + Email +
                                    "', ATIVO = '" + Ativo +
                                    "', CODPERFIL = " + idPerfil +
                                    "   WHERE CODEMPRESA = " + CodEmpresa + " AND CODPERFIL = " + idPerfil);
                            /*Cursor cursor1 = DB.rawQuery(" SELECT CODEMPRESA, CODPERFIL, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
                            cursor1.moveToFirst();
                            CodBairroExt = cursor1.getInt(cursor1.getColumnIndex("CODBAIRRO_EXT"));
                            cursor1.close();*/
                        } else {
                            DB.execSQL(" INSERT INTO EMPRESAS (CODEMPRESA, CODPERFIL, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO)" +
                                    " VALUES(" + CodEmpresa +
                                    "," + idPerfil +
                                    ",'" + NomeEmpresa +
                                    "','" + NomeAbreviado +
                                    "','" + Cnpj +
                                    "','" + Tel1 +
                                    "','" + Tel2 +
                                    "','" + Email +
                                    "','" + Ativo + "' );");
                            //Cursor cursor1 = DB.rawQuery(" SELECT CODEMPRESA, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
                            sincempresastatic = "OK";
                            //cursor1.close();
                        }
                        sincempresastatic = "OK";
                        CursorEmpresa.close();

                    } catch (Exception E) {
                        E.toString();
                        return sincempresastatic;
                    }
                }
            }
        } catch (JSONException e) {
            e.toString();
            return sincempresastatic;
        }
        return sincempresastatic;
    }

    public static String sincronizaCidadeBairro(String UF, final Context ctxEnv, final ProgressDialog Dialog) {
        String sincatucidade = "0";
        if (UF.equals("")) {
            return sincatucidade;
        }
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        int CodCidadeExt = 0;
        int CodCidadeInt = 0;
        int CodCidade = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Cidades");
        soap.addProperty("aUF", UF);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
        String RetCidades = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk) {
                Envio.call("", envelope);

            } else {
                sincatucidade = ctxEnv.getString(R.string.no_connection);
                return sincatucidade;
            }
        } catch (Exception e) {
            e.toString();
            sincatucidade = ctxEnv.getString(R.string.failure_communicate);
            return sincatucidade;
        }
        try {
            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
            RetCidades = (String) envelope.getResponse();
            System.out.println("Response :" + resultsRequestSOAP.toString());
        } catch (Exception e) {
            e.toString();
            sincatucidade = ctxEnv.getString(R.string.failed_return);
            return sincatucidade;
        }
        try {
            JSONObject jsonObj = new JSONObject(RetCidades);
            JSONArray JCidades = jsonObj.getJSONArray("cidades");

            int jumpTime = 0;
            final int totalProgressTime = JCidades.length();
            if (Dialog != null) {
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();


            for (int i = 0; i < JCidades.length(); i++) {
                while (jumpTime < totalProgressTime) {

                    try {
                        JSONObject c = JCidades.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (Dialog != null) {
                            Dialog.setProgress(jumpTime);
                        }
                        String NomeCidade = c.getString("cidade");
                        CodCidadeExt = c.getInt("id_cidade");
                        NomeCidade = NomeCidade.replaceAll("'", "");

                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade +
                                "' AND CODPERFIL = " + idPerfil, null);
                        if (!(CursorCidade.getCount() > 0)) {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODCIDADE_EXT,CODPERFIL)" +
                                    " VALUES('" + NomeCidade + "','" + UF + "', '" + CodCidadeExt + "'," + idPerfil + ");");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade +
                                    "' AND CODPERFIL =" + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidadeExt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE_EXT"));
                            CodCidadeInt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE"));
                            cursor1.close();
                            CursorCidade.close();
                            sincatucidade = "OK";

                            /*final int finalCodCidadeExt = CodCidadeExt;
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Sincronismo.sincronizaBairro(finalCodCidadeExt, ctxEnv,Dialog);
                                }
                            });*/

                            StrictMode.ThreadPolicy policyBairro = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policyBairro);

                            SoapObject soapBaiiro = new SoapObject(ConfigConex.NAMESPACE, "Bairros");
                            soapBaiiro.addProperty("aIdCidade", CodCidadeExt);
                            SoapSerializationEnvelope envelopeBairro = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelopeBairro.setOutputSoapObject(soapBaiiro);
                            HttpTransportSE EnvioBaiiro = new HttpTransportSE(ConfigConex.URLDADOSCEP);
                            String RetBairros = null;

                            try {
                                Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
                                if (ConexOk) {
                                    EnvioBaiiro.call("", envelopeBairro);
                                    SoapObject resultsRequestSOAPBairro = (SoapObject) envelopeBairro.bodyIn;
                                    RetBairros = (String) envelopeBairro.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAPBairro.toString());
                                }
                            } catch (Exception e) {
                                System.out.println("Error" + e);
                            }

                            try {
                                JSONObject jsonObjBaiiro = new JSONObject(RetBairros);
                                JSONArray JBairros = jsonObjBaiiro.getJSONArray("bairros");

                                int jumpTimeBairro = 0;
                                final int totalProgressTimeBaiiro = JBairros.length();
                                DB = new ConfigDB(ctxEnv).getReadableDatabase();

                                for (int k = 0; k < JBairros.length(); k++) {
                                    while (jumpTimeBairro < totalProgressTimeBaiiro) {
                                        try {
                                            JSONObject cb = JBairros.getJSONObject(jumpTimeBairro);
                                            jumpTimeBairro += 1;
                                            String NomeBairro = cb.getString("bairro");
                                            int CodBairroExt = cb.getInt("id_bairro");
                                            NomeBairro = NomeBairro.replaceAll("'", " ");


                                            Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODBAIRRO_EXT, CODCIDADE FROM BAIRROS WHERE CODCIDADE = '" + CodCidadeInt +
                                                    "' AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                                            if (CursorBairro.getCount() > 0) {
                                                DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = '" + CodCidadeInt + "', DESCRICAO = '" + NomeBairro + "', CODBAIRRO_EXT = '" + CodBairroExt + "'" +
                                                        " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + CodCidadeInt + "' AND CODPERFIL = " + idPerfil);
                                                Cursor cursor2 = DB.rawQuery(" SELECT DESCRICAO, CODCIDADE, CODBAIRRO_EXT FROM BAIRROS WHERE CODCIDADE = '" + CodCidadeInt +
                                                        "' AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                                                cursor2.moveToFirst();
                                                CodBairroExt = cursor2.getInt(cursor2.getColumnIndex("CODBAIRRO_EXT"));
                                                cursor2.close();
                                            } else {
                                                DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODBAIRRO_EXT, CODCIDADE, CODPERFIL)" +
                                                        " VALUES('" + NomeBairro + "','" + CodBairroExt + "', '" + CodCidadeInt +
                                                        "', " + idPerfil + ");");
                                                Cursor cursor2 = DB.rawQuery(" SELECT DESCRICAO, CODCIDADE, CODBAIRRO_EXT FROM BAIRROS WHERE CODCIDADE = '" + CodCidadeInt +
                                                        "' AND DESCRICAO =  '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                                                cursor2.moveToFirst();
                                                CodBairroExt = cursor2.getInt(cursor2.getColumnIndex("CODBAIRRO_EXT"));
                                                cursor2.close();
                                            }
                                            CursorBairro.close();

                                        } catch (Exception E) {
                                            E.toString();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.toString();
                            }


                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
        } catch (JSONException e) {
            e.toString();
        }
        return sincatucidade;
    }

    public static String sincronizaCidade(String UF, final Context ctxEnv, final ProgressDialog Dialog, Handler handler) {
        String sincatucidade = "0";
        if (UF.equals("")) {
            return sincatucidade;
        }
        SharedPreferences prefs = ctxEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);
        int CodCidadeExt = 0;
        int CodCidadeInt = 0;
        int CodCidade = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Cidades");
        soap.addProperty("aUF", UF);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
        String RetCidades = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk) {
                int i = 0;
                do {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                    try {
                        Envio.call("", envelope);
                    } catch (Exception e) {
                        e.toString();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctxEnv, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    try {
                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                        RetCidades = (String) envelope.getResponse();
                        System.out.println("Response :" + resultsRequestSOAP.toString());
                    } catch (Exception e) {
                        e.toString();
                        sincatucidade = ctxEnv.getString(R.string.failed_return);
                        return sincatucidade;
                    }
                    i = i + 1;
                } while (RetCidades == null && i <= 6);

            } else {
                sincatucidade = ctxEnv.getString(R.string.no_connection);
                return sincatucidade;
            }
        } catch (Exception e) {
            e.toString();
            sincatucidade = ctxEnv.getString(R.string.failure_communicate);
            return sincatucidade;
        }
        if (RetCidades == null) {
            sincatucidade = ctxEnv.getString(R.string.failure_communicate);
            return sincatucidade;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetCidades);
            JSONArray JCidades = jsonObj.getJSONArray("cidades");

            int jumpTime = 0;
            final int totalProgressTime = JCidades.length();
            if (Dialog != null) {
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();


            for (int i = 0; i < JCidades.length(); i++) {
                while (jumpTime < totalProgressTime) {

                    try {
                        JSONObject c = JCidades.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (Dialog != null) {
                            Dialog.setProgress(jumpTime);
                        }
                        String NomeCidade = c.getString("cidade");
                        CodCidadeExt = c.getInt("id_cidade");
                        NomeCidade = NomeCidade.replaceAll("'", "");

                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade +
                                "' AND CODPERFIL = " + idPerfil, null);
                        if (!(CursorCidade.getCount() > 0)) {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODCIDADE_EXT,CODPERFIL)" +
                                    " VALUES('" + NomeCidade + "','" + UF + "', '" + CodCidadeExt + "'," + idPerfil + ");");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade +
                                    "' AND CODPERFIL =" + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidadeExt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE_EXT"));
                            CodCidadeInt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE"));
                            cursor1.close();
                            CursorCidade.close();
                        }
                        sincatucidade = "OK";

                    } catch (Exception e) {
                        e.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.toString();

        }
        return sincatucidade;
    }

    public static String sincronizaBairro(int codCidadeExt, final Context ctxEnv, ProgressDialog Dialog, int codCidadeInt, Handler handler) {
        String atuBairro = "0";
        int CodBairro = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Bairros");
        soap.addProperty("aIdCidade", codCidadeExt);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
        String RetBairros = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk) {
                int i = 0;
                do {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                    try {
                        Envio.call("", envelope);
                    } catch (Exception e) {
                        e.toString();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctxEnv, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    try {
                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                        RetBairros = (String) envelope.getResponse();
                        System.out.println("Response :" + resultsRequestSOAP.toString());
                    } catch (Exception e) {
                        e.toString();
                        atuBairro = ctxEnv.getString(R.string.failed_return);
                        return atuBairro;
                    }
                    i = i + 1;
                } while (RetBairros == null && i <= 6);

            } else {
                atuBairro = ctxEnv.getString(R.string.no_connection);
                return atuBairro;
            }
        } catch (Exception e) {
            e.toString();
            atuBairro = ctxEnv.getString(R.string.failure_communicate);
            return atuBairro;
        }
        if (RetBairros == null) {
            atuBairro = ctxEnv.getString(R.string.failure_communicate);
            return atuBairro;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetBairros);
            JSONArray JBairros = jsonObj.getJSONArray("bairros");

            int jumpTime = 0;
            final int totalProgressTime = JBairros.length();
            if (Dialog != null) {
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
            }
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JBairros.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JBairros.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (Dialog != null) {
                            Dialog.setProgress(jumpTime);
                        }
                        String NomeBairro = c.getString("bairro");
                        int CodBairroExt = c.getInt("id_bairro");

                        NomeBairro = NomeBairro.replaceAll("'", " ");

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODBAIRRO_EXT, CODCIDADE FROM BAIRROS WHERE CODCIDADE = '" + codCidadeInt +
                                "' AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorBairro.getCount() > 0) {
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = '" + codCidadeInt + "', DESCRICAO = '" + NomeBairro + "', CODBAIRRO_EXT = '" + CodBairroExt + "'" +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + codCidadeInt + "' AND CODPERFIL = " + idPerfil);
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODBAIRRO_EXT, CODCIDADE, CODPERFIL)" +
                                    " VALUES('" + NomeBairro + "','" + CodBairroExt + "', '" + codCidadeInt + "', " + idPerfil + ");");
                        }
                        CursorBairro.close();

                    } catch (Exception E) {
                        E.toString();
                    }
                }
            }
            atuBairro = "ok";
        } catch (
                JSONException e)

        {
            e.toString();
        }
        return atuBairro;
    }

    public static String sincronizaAtualizaPedido(String numPedido, Context ctxAtu, String tipoAtu) {
        SharedPreferences prefsHost = ctxAtu.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxAtu.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincAtuPedido = null;
        DB = new ConfigDB(ctxAtu).getReadableDatabase();
        if (tipoAtu.equals("C")) {
            try {
                Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + numPedido + "' AND CODPERFIL = " + idPerfil, null);
                CursPedAtu.moveToFirst();
                if (CursPedAtu.getCount() > 0) {

                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '4' WHERE NUMPED = '" + numPedido + "' AND CODPERFIL = " + idPerfil);
                }
                sincAtuPedido = "ok";
                CursPedAtu.close();
            } catch (Exception E) {
                E.toString();
                return null;
            }
            return sincAtuPedido;

        } else if (tipoAtu.equals("A")) {
            try {
                Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + numPedido + "' AND CODPERFIL = " + idPerfil, null);
                CursPedAtu.moveToFirst();
                if (CursPedAtu.getCount() > 0) {
                    String dataVenda = Util.DataHojeComHorasBR();
                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '5', DATAVENDA = '" + dataVenda + "' WHERE NUMPED = '" + numPedido + "' AND CODPERFIL = " + idPerfil);
                }
                sincAtuPedido = "ok";
                CursPedAtu.close();
            } catch (Exception E) {
                E.toString();
                return null;
            }
            return sincAtuPedido;

        } else if (tipoAtu.equals("S")) {
            String JPedidos = null;
            ProgressDialog Dialog = null;
            String METHOD_NAMEENVIO = "RetornaStatusPedidos";
            DB = new ConfigDB(ctxAtu).getReadableDatabase();
            Cursor CursorPedido;
            String NumFiscal = "";

            try {
                CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPEDIDOERP = " + numPedido + " AND CODPERFIL = " + idPerfil, null);

                Dialog = new ProgressDialog(ctxAtu);
                Dialog.setTitle("Aguarde...");
                Dialog.setMessage("Atualizando Pedido Nº: " + numPedido);
                Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                Dialog.setProgress(0);
                Dialog.setIcon(R.drawable.icon_sync);
                Dialog.setMax(0);
                Dialog.show();

                String RetStatusPedido = null;

                int jumpTime = 0;
                final int totalProgressTime = CursorPedido.getCount();
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
                CursorPedido.moveToFirst();
                if (CursorPedido.getCount() > 0) {
                    CursorPedido.moveToFirst();
                    do {
                        for (int i = 0; i < CursorPedido.getCount(); i++) {
                            do try {
                                jumpTime += 1;
                                Dialog.setProgress(jumpTime);
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                soap.addProperty("aNumPedido", numPedido);
                                soap.addProperty("aUsuario", usuario);
                                soap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                                try {
                                    Boolean ConexOk = Util.checarConexaoCelular(ctxAtu);
                                    if (ConexOk) {
                                        Envio.call("", envelope);
                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                        RetStatusPedido = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error" + e);
                                }
                            } catch (Exception E) {
                                E.printStackTrace();
                            }
                            while (jumpTime < totalProgressTime);
                        }
                        try {
                            DB = new ConfigDB(ctxAtu).getReadableDatabase();
                            Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPEDIDOERP = " + numPedido + " AND CODPERFIL = " + idPerfil, null);
                            CursPedAtu.moveToFirst();
                            if (CursPedAtu.getCount() > 0) {
                                if (!RetStatusPedido.equals("Orçamento")) {
                                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '3', NUMFISCAL = " + RetStatusPedido + " WHERE NUMPEDIDOERP = '" + numPedido + "' AND CODPERFIL = " + idPerfil);
                                }
                            }
                            sincAtuPedido = RetStatusPedido;
                            CursPedAtu.close();
                        } catch (Exception E) {
                            Toast.makeText(ctxAtu, E.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    while (CursorPedido.moveToNext());
                    CursorPedido.close();
                }
                if (Dialog.isShowing())
                    Dialog.dismiss();
            } catch (Exception E) {
                System.out.println("Error" + E);
            }
        }
        return sincAtuPedido;
    }

    public static String sincronizaSitClieXPed(String vltotalped, Context ctxEnvClie, String user, String pass, int CodClie) {

        String situacao = null;

        String METHOD_NAME = "RetornaSituacaoCliexVend";
        String TAG_SITUACAOCLIENTE = "sitclie";
        String TAG_SITCLIENTE = "situacaoclie";
        String TAG_DESCBLOQUEIO = "descricaobloqueio";
        vltotalped = vltotalped.replace(",", ".");

        SharedPreferences prefs = ctxEnvClie.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        if (senha != null) {
            soap.addProperty("aUsuario", usuario);
            soap.addProperty("aSenha", senha);
            soap.addProperty("aCodClie", CodClie);
            soap.addProperty("aValorTotalPedido", vltotalped);
        } else {
            soap.addProperty("aUsuario", user);
            soap.addProperty("aSenha", pass);
            soap.addProperty("aCodClie", CodClie);
            soap.addProperty("aValorTotalPedido", vltotalped);
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES, 60000);
        String RetCliexVend = null;
        /*int i = 0;
        do {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
            if (ConexOk) {

                try {
                    Envio.call("", envelope);
                } catch (Exception e) {
                    e.toString();
                }
                try {
                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                    RetCliexVend = (String) envelope.getResponse();
                    System.out.println("Response :" + resultsRequestSOAP.toString());
                } catch (Exception e) {
                    e.toString();
                }
            } else {
                situacao = "Sem conexão com o servidor. Tente novamente!";
                return situacao;
            }
        } catch (Exception e) {
            System.out.println("Error na solicitação" + e);
        }
        //i = i + 1;
        //}while (RetCliexVend == null && i <= 20);

        try {

            JSONObject jsonObj = new JSONObject(RetCliexVend);
            JSONArray InfoClie = jsonObj.getJSONArray(TAG_SITUACAOCLIENTE);


            JSONObject c = InfoClie.getJSONObject(0);

            String situacaocliente = c.getString(TAG_SITCLIENTE);
            String descricaobloqueio = c.getString(TAG_DESCBLOQUEIO);

            if (situacaocliente.equals("OK")) {
                situacao = "OK";
                return situacao;
            } else {
                situacao = situacaocliente + " " + descricaobloqueio;
                return situacao;
            }


        } catch (Exception E) {
            E.toString();
        }


        return situacao;

    }

    //Começa daqui para baixo as funções que gerar arquivo de pedido em formato .PDF

    public static String GerarPdf(String NumPedido, Context ctxRetPed) {
        SharedPreferences prefs = ctxRetPed.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        int idPerfil = prefs.getInt("idperfil", 0);

        DB = new ConfigDB(ctxRetPed).getReadableDatabase();

        String Pedido = "";
        String ItensPedido = "";
        String Situacao = "";
        PdfWriter docWriter = null;

        try {
            DB = new ConfigDB(ctxRetPed).getReadableDatabase();
            Cursor CursPedido = DB.rawQuery(" SELECT PEDOPER.*, EMPRESAS.NOMEEMPRE, USUARIOS.USUARIO, EMPRESAS.LOGO FROM PEDOPER LEFT OUTER JOIN " +
                    " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL) LEFT OUTER JOIN " +
                    " USUARIOS ON (PEDOPER.CODVENDEDOR = USUARIOS.CODVEND) " +
                    " WHERE NUMPED = '" + NumPedido + "' AND PEDOPER.CODPERFIL = " + idPerfil, null);
            CursPedido.moveToFirst();

            String dataEmUmFormato = CursPedido.getString(CursPedido.getColumnIndex("DATAEMIS"));
            /*SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            Date data = formato.parse(dataEmUmFormato);
            formato.applyPattern("dd/MM/yyyy");
            String sDataVenda = formato.format(data);*/

            Double VlTotal = (CursPedido.getDouble(CursPedido.getColumnIndex("VALORTOTAL")) -
                    CursPedido.getDouble(CursPedido.getColumnIndex("VLPERCACRES")));

            String VlDesc = CursPedido.getString(CursPedido.getColumnIndex("VLDESCONTO"));
            VlDesc = VlDesc.replace('.', ',');
            Double VlSubTot = CursPedido.getDouble(CursPedido.getColumnIndex("VLMERCAD"));

            String STotal = String.valueOf(VlSubTot);
            java.math.BigDecimal Subvenda = new java.math.BigDecimal(Double.parseDouble(STotal.replace(',', '.')));
            String SubTotal = Subvenda.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            SubTotal = SubTotal.replace('.', ',');

            String valor = String.valueOf(VlTotal);
            java.math.BigDecimal venda = new java.math.BigDecimal(Double.parseDouble(valor.replace(',', '.')));
            String ValorTotal = venda.setScale(2, java.math.BigDecimal.ROUND_HALF_UP).toString();
            ValorTotal = ValorTotal.replace('.', ',');

            Document PedidoPdf = new Document(); // cria o PDF
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf"; // diretório no dispositivo que irá gravar o arquivo
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            Log.d("PDFCreator", "PDF Path: " + path);
            File file = new File(dir, NumPedido + ".pdf");
            FileOutputStream fOut = new FileOutputStream(file);
            docWriter = PdfWriter.getInstance(PedidoPdf, fOut);

            PedidoPdf.addAuthor("JD System");
            PedidoPdf.addCreationDate();
            PedidoPdf.addProducer();
            PedidoPdf.addCreator("jdsystem.com.br");
            PedidoPdf.addTitle("Força de Vendas");
            PedidoPdf.setPageSize(PageSize.A4);


            PedidoPdf.open();
            PdfContentByte cb = docWriter.getDirectContent();
            initializeFonts();

            /*byte[] byteArray = CursPedido.getBlob(CursPedido.getColumnIndex("LOGO"));

            String Caminho = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf/";

            File arquivo = new File(Caminho + byteArray + ".jpg");
            Uri localFotoUri = Uri.fromFile(arquivo);

            Blob b = CursPedido.getBlob(2);
            byte barr[] = new byte[(int)b.length()];
            barr = b.getBytes(1,(int)b.length());

            FileOutputStream fout = new FileOutputStream("D:\\sonoo.jpg");
            fout.write(barr);

            Image companyLogo = Image.getInstance(Caminho + byteArray + ".jpg");
            companyLogo.setAbsolutePosition(25, 700);
            companyLogo.scalePercent(25);
            PedidoPdf.add(companyLogo);*/

            generateLayout(PedidoPdf, cb);

            String SitPed = CursPedido.getString(CursPedido.getColumnIndex("FLAGINTEGRADO"));
            if (SitPed.equals("1")) {
                Situacao = "Orçamento";
            }
            if (SitPed.equals("2")) {
                Situacao = "Pedido Gerado: " + CursPedido.getString(CursPedido.getColumnIndex("NUMPEDIDOERP"));
            }
            if (SitPed.equals("3")) {
                Situacao = "NFe Gerada: " + CursPedido.getString(CursPedido.getColumnIndex("NUMFISCAL"));
            }
            if (SitPed.equals("4")) {
                Situacao = "Cancelado";
            }
            if (SitPed.equals("5")) {
                Situacao = "Gerar Venda";
            }

            createHeadings(cb, 200, 800, "Empresa: " + CursPedido.getString(CursPedido.getColumnIndex("NOMEEMPRE")));
            createHeadings(cb, 200, 785, "Cliente: " + CursPedido.getString(CursPedido.getColumnIndex("NOMECLIE")));
            createHeadings(cb, 200, 770, "Data Emissão: " + dataEmUmFormato);
            createHeadings(cb, 200, 755, "Vendedor: " + CursPedido.getString(CursPedido.getColumnIndex("USUARIO")));
            createHeadings(cb, 200, 740, "Situação: " + Situacao);

            Cursor CursorItensEnv = DB.rawQuery(" SELECT PEDITENS.CODITEMANUAL, PEDITENS.DESCRICAO, PEDITENS.QTDMENORPED, " +
                    " PEDITENS.UNIDADE, PEDITENS.VLUNIT, PEDITENS.VLTOTAL FROM PEDITENS " +
                    " WHERE PEDITENS.CHAVEPEDIDO = '" + CursPedido.getString(CursPedido.getColumnIndex("CHAVE_PEDIDO")) + "' AND PEDITENS.CODPERFIL = " + idPerfil, null);
            int item = 1;
            int y = 670;
            CursorItensEnv.moveToFirst();
            DecimalFormat dfunit = new DecimalFormat("0.0000");
            DecimalFormat dftotal = new DecimalFormat("0.00");
            //Totalitem = Totalitem.replace('.', ',');

            do {
                Double vltotal = CursorItensEnv.getDouble(CursorItensEnv.getColumnIndex("VLTOTAL"));
                String vlTotalitem = String.valueOf(vltotal);
                java.math.BigDecimal totvendaitem = new java.math.BigDecimal(Double.parseDouble(vlTotalitem.replace(',', '.'))).setScale(2, BigDecimal.ROUND_HALF_UP);
                String Totalitem = totvendaitem.toString().replace('.', ',');

                createContent(cb, 30, y, Util.AcrescentaZeros(String.valueOf(item), 3), PdfContentByte.ALIGN_CENTER);
                createContent(cb, 43, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODITEMANUAL")), PdfContentByte.ALIGN_LEFT);
                createContent(cb, 102, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")), PdfContentByte.ALIGN_LEFT);
                createContent(cb, 430, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")), PdfContentByte.ALIGN_RIGHT);
                createContent(cb, 460, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("UNIDADE")), PdfContentByte.ALIGN_LEFT);
                createContent(cb, 547, y, dfunit.format(CursorItensEnv.getDouble(CursorItensEnv.getColumnIndex("VLUNIT"))).replace(".", ","), PdfContentByte.ALIGN_RIGHT);
                createContent(cb, 588, y, Totalitem, PdfContentByte.ALIGN_RIGHT);
                y = y - 15;
                item++;
            } while (CursorItensEnv.moveToNext());

            CursPedido.close();
            CursorItensEnv.close();

            createTotal(cb, 430, 180, "Sub-Total:    R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 580, 180, SubTotal, PdfContentByte.ALIGN_RIGHT);

            createTotal(cb, 430, 150, "Vl. Desconto: R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 580, 150, VlDesc, PdfContentByte.ALIGN_RIGHT);

            createTotal(cb, 430, 120, "Valor Total:  R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 580, 120, ValorTotal, PdfContentByte.ALIGN_RIGHT);

            // printPageNumber(cb);

            InserirRodape(cb, "Desenvolvido por JD System - www.jdsystem.com.br");

            PedidoPdf.close();
            docWriter.close();

        } catch (Exception E) {
            E.toString();
            return "0";
        }
        return NumPedido + ".pdf";
    }

    private static void initializeFonts() {
        try {
            bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateLayout(Document doc, PdfContentByte cb) {
        try {

            cb.setLineWidth(1f);

            // Invoice Detail box listview_parcelas
            //cb.rectangle(20, 50, 550, 600);
            cb.rectangle(20, 200, 570, 500);

            cb.moveTo(20, 680);
            cb.lineTo(590, 680); //Linha de Baixo ____ (Laterais / Altura)

            //Linhas |
            cb.moveTo(41, 200);
            cb.lineTo(41, 700);
            cb.moveTo(100, 200);
            cb.lineTo(100, 700);
            cb.moveTo(400, 200);
            cb.lineTo(400, 700);
            cb.moveTo(450, 200);
            cb.lineTo(450, 700);
            cb.moveTo(500, 200);
            cb.lineTo(500, 700);
            cb.moveTo(550, 200);
            cb.lineTo(550, 700);
            cb.stroke();

            // Invoice Detail box Text Headings
            createHeadings(cb, 21, 683, "Item");
            createHeadings(cb, 43, 683, "Código");
            createHeadings(cb, 102, 683, "Descrição");
            createHeadings(cb, 402, 683, "Qtd");
            createHeadings(cb, 452, 683, "Und.");
            createHeadings(cb, 502, 683, "Vl. Unit.");
            createHeadings(cb, 552, 683, "Vl. Total.");

            //add the images
            Image companyLogo = Image.getInstance(".gif");
            companyLogo.setAbsolutePosition(25, 700);
            companyLogo.scalePercent(25);
            doc.add(companyLogo);

        } catch (DocumentException dex) {
            dex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void createHeadings(PdfContentByte cb, float x, float y, String text) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.setTextMatrix(x, y);
        cb.showText(text.trim());
        cb.endText();
    }

    private static void createContent(PdfContentByte cb, float x, float y, String text,
                                      int align) {

        cb.beginText();
        cb.setFontAndSize(bf, 8);
        cb.showTextAligned(align, text.trim(), x, y, 0);
        cb.endText();

    }

    private static void createTotal(PdfContentByte cb, float x, float y, String text,
                                    int align) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 14);
        cb.showTextAligned(align, text.trim(), x, y, 0);
        cb.endText();
    }

    private static void InserirRodape(PdfContentByte cb, String Texto) {
        BaseFont bfRodaPe = null;
        try {
            bfRodaPe = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cb.beginText();
        cb.setFontAndSize(bfRodaPe, 7);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, Texto, 280, 25, 0);
        cb.endText();
    }

    private static void printPageNumber(PdfContentByte cb) {

        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Pg. " + (pageNumber + 1), 570, 25, 0);
        cb.endText();

        pageNumber++;

    }

    //Termine aqui para cima as funções que são utilizadas para a geração de arquivo do pedido em .PDF

    public static String retornaJsonHorariosAgenda(String codContato, Context contextCont) {
        String retorno = "";
        SQLiteDatabase db = new ConfigDB(contextCont).getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from dias_contatos where codcontatoint = " +
                    codContato, null);

            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    int codDiaSemana = cursor.getInt(cursor.getColumnIndex("cod_dia_semana"));
                    int horaInicial = cursor.getInt(cursor.getColumnIndex("hora_inicio"));
                    int minutoInicial = cursor.getInt(cursor.getColumnIndex("minuto_inicio"));
                    int horaFinal = cursor.getInt(cursor.getColumnIndex("hora_final"));
                    int minutoFinal = cursor.getInt(cursor.getColumnIndex("minuto_final"));

                    String horarioInicial = CadastroContatos.converteZero(String.valueOf(horaInicial)) + ":" + CadastroContatos.converteZero(String.valueOf(minutoInicial));
                    String horarioFinal = CadastroContatos.converteZero(String.valueOf(horaFinal)) + ":" + CadastroContatos.converteZero(String.valueOf(minutoFinal));

                    retorno = retorno + "{cod_dia_semana:'" + codDiaSemana + "'," +
                            "hora_inicio:'" + horarioInicial + "'," +
                            "hora_final:'" + horarioFinal + "'},";
                } while (cursor.moveToNext());
                cursor.close();
                return retorno.trim().substring(0, (retorno.length() - 1));
            } else {
                retorno = retorno + "";
            }
        } catch (Exception E) {
            E.toString();
        }
        return retorno;
    }

    public static String retornaItens(String codContato, Context context) {
        String retorno = "";
        SQLiteDatabase db = new ConfigDB(context).getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from produtos_contatos where cod_interno_contato = " +
                    codContato, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    int codItem = cursor.getInt(cursor.getColumnIndex("cod_item"));
                    retorno = retorno + "{codigoitem :'" + codItem + "'},";
                } while (cursor.moveToNext());
                cursor.close();
                return retorno.trim().substring(0, (retorno.length() - 1));
            } else {
                cursor.close();
                retorno = retorno + "";
            }

        } catch (Exception E) {
            E.toString();
        }
        return retorno;
    }

    private void declaraObjetos() {
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.toString();
        }
        DB = new ConfigDB(this).getReadableDatabase();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //Button btnSinc = (Button) findViewById(R.id.btnSincronizar);
    }

    private void carregaPreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);


    }

    private void carregaUsuarioLogado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(Sincronismo.this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        String usuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        if (usuarioLogado != null) {
            usuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
            usuariologado.setText("Olá " + usuarioLogado + "!");
        } else {
            usuariologado.setText("Olá " + usuario + "!");
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {

            Intent intent = new Intent(Sincronismo.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(Sincronismo.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(Sincronismo.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(Sincronismo.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_agenda) {
            Intent i = new Intent(Sincronismo.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        }else if (id == R.id.nav_sincronismo) {

        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(Sincronismo.this, Login.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            System.exit(1);
        } else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(Sincronismo.this, InfoJDSystem.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static String SincronizarAgendaEnvio (final Context ctxAgEnv, String NumAgenda, final ProgressDialog dialog) {

        SharedPreferences prefsHost = ctxAgEnv.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);
        int idPerfil = prefsHost.getInt("idperfil", 0);

        SharedPreferences prefs = ctxAgEnv.getSharedPreferences(Login.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String sincagendaenviostatic = null;
            String JAgenda = null;
            DB = new ConfigDB(ctxAgEnv).getReadableDatabase();
            Cursor CursorAgenda;
            String RetClieEnvio = null;
            try {
                CursorAgenda = DB.rawQuery(" SELECT * FROM AGENDA WHERE CODIGO = " + NumAgenda + " AND CODPERFIL = " + idPerfil, null);
                int jumpTime = 0;
                final int totalProgressTime = CursorAgenda.getCount();
                CursorAgenda.moveToFirst();
                if (dialog != null) {
                    dialog.setProgress(jumpTime);
                    dialog.setMax(totalProgressTime);
                }

                if (CursorAgenda.getCount() > 0) {
                    CursorAgenda.moveToFirst();
                    do {
                        for (int i = 0; i < CursorAgenda.getCount(); i++) {

                            try {
                                jumpTime += 1;
                                if (dialog != null) {
                                    dialog.setProgress(jumpTime);
                                }

                                int CodContato = CursorAgenda.getInt(CursorAgenda.getColumnIndex("CODCONTATO"));

                                String Observacao = CursorAgenda.getString(CursorAgenda.getColumnIndex("DESCRICAO"));
                                String line_separator = System.getProperty("line.separator");
                                String OBS = Observacao.replaceAll("\n|" + line_separator, "");

                                JAgenda = "{codcont_ext: '" + CodContato + "'," +
                                        "nome_contato: '" + CursorAgenda.getString(CursorAgenda.getColumnIndex("NOMECONTATO")) + "'," +
                                        "data_geracao: '" + CursorAgenda.getString(CursorAgenda.getColumnIndex("DATAAGEND")) + "'," +
                                        "cod_agenda: '" + CursorAgenda.getString(CursorAgenda.getColumnIndex("CODIGO"))+ "'," +
                                        "obs_pedido: '" + OBS + "'," +
                                        "situacao: '" + CursorAgenda.getString(CursorAgenda.getColumnIndex("SITUACAO")) + "',";

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME_AGENDA);
                                soap.addProperty("aJson", JAgenda);
                                soap.addProperty("aUsuario", usuario);
                                soap.addProperty("aSenha", senha);
                                soap.addProperty("aNumAgenda", NumAgenda);
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal);
                                String RetEnvio = "0";

                                try {
                                    Boolean ConexOk = Util.checarConexaoCelular(ctxAgEnv);
                                    if (ConexOk) {
                                        Envio.call("", envelope);
                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                        RetEnvio = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    } else {
                                        sincagendaenviostatic = ctxAgEnv.getString(R.string.no_connection);
                                        return sincagendaenviostatic;
                                    }
                                } catch (Exception e) {
                                    e.toString();
                                    sincagendaenviostatic = ctxAgEnv.getString(R.string.failure_communicate);
                                    Toast.makeText(ctxAgEnv, "Não foi possível enviar a agenda! Verifique.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            } catch (Exception e) {
                                e.toString();
                            }
                        }
                    } while (CursorAgenda.moveToNext());
                    try {
                        DB = new ConfigDB(ctxAgEnv).getReadableDatabase();
                        Cursor CursAgAtu = DB.rawQuery(" SELECT * FROM AGENDA WHERE CODIGO = '" + CursorAgenda.getString(CursorAgenda.getColumnIndex("CODIGO")) + "' AND CODPERFIL = " + idPerfil, null);
                        CursAgAtu.moveToFirst();
                        if (CursAgAtu.getCount() > 0) {
                            DB.execSQL(" UPDATE AGENDA SET STATUS = 'S' WHERE CODIGO = '" + CursAgAtu.getString(CursAgAtu.getColumnIndex("CODIGO")) + "' AND CODPERFIL = " + idPerfil);
                        }
                        sincagendaenviostatic = "OK";
                        CursAgAtu.close();
                    } catch (Exception E) {
                        E.toString();
                    }
                    CursorAgenda.close();
                } else {
                    sincagendaenviostatic = "Nenhum agendamento a ser enviado.";
                    return sincagendaenviostatic;
                }
            } catch (Exception e) {
                e.toString();
            }
        return sincagendaenviostatic;
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Sincronismo Page")
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
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Sincronismo.this, ConsultaPedidos.class);
        Bundle params = new Bundle();
        params.getString("codvendedor", codVendedor);
        params.getString("usuario", usuario);
        params.getString("senha", senha);
        params.getString("urlPrincipal", URLPrincipal);
        i.putExtras(params);
        startActivity(i);
        finish();
        super.onBackPressed();
    }

}