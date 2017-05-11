package com.jdsystem.br.vendasmobile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity implements Runnable {
    public static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public static final String COD_EMPRESA = "CODIGO_EMPRESA";
    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String METHOD_NAME = "Login";
    private static final int REQUEST_READ_PHONE_STATE = 0;
    private SQLiteDatabase DB;
    private GoogleApiClient client;
    private EditText edtUsuario, edtSenha;
    private Button btnEntrar;
    private CheckBox cbGravSenha;
    private ProgressDialog Dialogo;
    private Handler handler = new Handler();
    public String Retorno = "0";
    public SharedPreferences prefs;
    public String usuario, senha, URLPrincipal, sCodVend, UFVendedor, qtdperfil;
    private String codVendedor = "0";
    public TextView copyright, versao, empresa;
    Spinner spPerfilInput;
    Boolean ConexOk;
    int idPerfil;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGB_565);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_login);

        declaraobjetos();
        carregarpreferencias();
        carregarperfil();


        if (URLPrincipal == null && qtdperfil.equals("N")) {
            Intent intent = new Intent(getApplicationContext(), ConfigWeb.class);
            startActivity(intent);
            finish();
        }
        if (usuario != null) {
            edtUsuario.setText(usuario);
        }
        if (senha != null) {
            edtSenha.setText(senha);
            cbGravSenha.setChecked(true);
        }
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        versao.setText("Versão " + version);

        copyright.setText("Copyright © " + Util.AnoAtual() + " - JD System Tecnologia em Informática");
    }

    private String carregarperfil() {
        qtdperfil = null;
        try {
            Cursor cursorPerfil = DB.rawQuery("SELECT * FROM PERFIL", null);
            cursorPerfil.moveToFirst();
            if (cursorPerfil.getCount() > 1) {
                List<String> DadosListPerfil = new ArrayList<String>();
                do {
                    DadosListPerfil.add(cursorPerfil.getString(cursorPerfil.getColumnIndex("NOMEPERFIL")).toUpperCase());
                } while (cursorPerfil.moveToNext());

                View viewEmp = (LayoutInflater.from(Login.this)).inflate(R.layout.input_perfil, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Login.this);
                alertBuilder.setView(viewEmp);
                spPerfilInput = (Spinner) viewEmp.findViewById(R.id.spnperfil);

                ArrayAdapter<String> arrayEmpresa = new ArrayAdapter<String>(Login.this, android.R.layout.simple_spinner_dropdown_item, DadosListPerfil);
                ArrayAdapter<String> spArrayEmpresa = arrayEmpresa;
                spArrayEmpresa.setDropDownViewResource(android.R.layout.simple_list_item_1);
                spPerfilInput.setAdapter(spArrayEmpresa);

                alertBuilder.setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String NomePerfil = spPerfilInput.getSelectedItem().toString();
                                try {
                                    Cursor cursorperfil1 = DB.rawQuery(" SELECT LICENCA,CODPERFIL,HOST,NOMEPERFIL FROM PERFIL WHERE NOMEPERFIL = '" + NomePerfil + "'", null);
                                    cursorperfil1.moveToFirst();
                                    int idPerfil = 0;
                                    String host = null;
                                    String chave = null;
                                    String nomePerfil = null;
                                    if (cursorperfil1.getCount() > 0) {
                                        idPerfil = cursorperfil1.getInt(cursorperfil1.getColumnIndex("CODPERFIL"));
                                        host = cursorperfil1.getString(cursorperfil1.getColumnIndex("HOST"));
                                        chave = cursorperfil1.getString(cursorperfil1.getColumnIndex("LICENCA"));
                                        nomePerfil = cursorperfil1.getString(cursorperfil1.getColumnIndex("NOMEPERFIL"));
                                    }
                                    cursorperfil1.close();

                                    SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
                                    editorhost.putString("ChaveAcesso", chave);
                                    editorhost.putString("host", host);
                                    editorhost.putInt("idperfil", idPerfil);
                                    editorhost.apply();

                                    URLPrincipal = host;
                                    empresa.setText(nomePerfil);


                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }
                            }
                        });
                Dialog dialog = alertBuilder.create();
                dialog.show();
                qtdperfil = "S";
                return qtdperfil;

            } else {
                empresa.setVisibility(View.GONE);
                qtdperfil = "N";
                return qtdperfil;
            }
        } catch (Exception E) {
            E.toString();
        }
        return qtdperfil;
    }

    public void logar(View view) {
        carregarpreferencias();
        final String user = edtUsuario.getText().toString();
        final String pass = edtSenha.getText().toString();
        ConexOk = Util.checarConexaoCelular(Login.this);
        if (ConexOk == false) {
            sCodVend = ValidarLogin(user, pass); // verifica se o usuário e senha  existe na base local do dispositivo
            if (sCodVend != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.login_no_connection)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Login.this, ConsultaPedidos.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                                params.putString(getString(R.string.intent_usuario), user);
                                params.putString(getString(R.string.intent_senha), pass);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();
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
                return;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Atenção! O Usuário ou a senha informada são inválidos ou não existe esse usuário " + edtUsuario.getText().toString() + " cadastrado neste aparelho. Caso seja a primeira utilização, é necessário se conectar online" +
                        " para que o aparelho cadastre esse usuário e posteriormente seja possível se conectar offline.Verifique essas informações e tente novamente.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        } else {
            if (edtUsuario.getText().length() == 0) {
                edtUsuario.setError(getString(R.string.enter_username));
                edtUsuario.requestFocus();
                return;

            } else if (edtSenha.getText().length() == 0) {
                edtSenha.setError(getString(R.string.enter_password));
                edtSenha.requestFocus();
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE).edit();
            editor.putString(getString(R.string.intent_usuario), edtUsuario.getText().toString());
            if (cbGravSenha.isChecked()) {
                editor.putString("senha", edtSenha.getText().toString());
            } else {
                editor.putString("senha", null);
            }
            editor.commit();

            Dialogo = new ProgressDialog(Login.this);
            Dialogo.setIndeterminate(true);
            Dialogo.setCancelable(false);
            Dialogo.setMessage(getString(R.string.checking_user_password));
            Dialogo.setTitle(R.string.wait);
            Dialogo.show();

        }
        Thread thread = new Thread(Login.this);
        thread.start();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    private void declaraobjetos() {

        empresa = (TextView) findViewById(R.id.txtempresalogin);
        versao = (TextView) findViewById(R.id.txtversaologin);
        DB = new ConfigDB(this).getReadableDatabase();
        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        edtUsuario = (EditText) findViewById(R.id.edtUsuario);
        edtSenha = (EditText) findViewById(R.id.edtSenha);
        cbGravSenha = (CheckBox) findViewById(R.id.cbGravSenha);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        copyright = (TextView) findViewById(R.id.textView2);
    }

    public boolean VerificaConexaoWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
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
        Configuration configuration = getResources().getConfiguration();

        if (Dialogo.isShowing() && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(Login.this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            Dialogo.dismiss();
            Thread.interrupted();
            return;
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aUsuario", edtUsuario.getText().toString());
        soap.addProperty("aSenha", edtSenha.getText().toString());
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS, 5000);


        String CodEmpresa = null;
        final String usuario = edtUsuario.getText().toString();
        final String pass = edtSenha.getText().toString();
        String sUsuario = null;
        Boolean ConexOk = Util.checarConexaoCelular(Login.this);
        if (ConexOk == true) {
            int i = 0;
            do {

                try {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                    if (i == 3) {
                        handler.post(new Runnable() {
                            public void run() {
                                Dialogo.setMessage("Por favor, aguarde mais alguns instantes, estamos tentando comunicação com o servidor... " + getString(R.string.checking_user_password));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (i == 0) {
                        Envio.call("", envelope);

                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                        sUsuario = (String) envelope.getResponse();
                        System.out.println("Response::" + resultsRequestSOAP.toString());
                    } else {
                        SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
                        newsoap.addProperty("aUsuario", edtUsuario.getText().toString());
                        newsoap.addProperty("aSenha", edtSenha.getText().toString());
                        SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        newenvelope.setOutputSoapObject(newsoap);
                        HttpTransportSE newEnvio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                        newEnvio.call("", newenvelope);

                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                        sUsuario = (String) newenvelope.getResponse();
                        System.out.println("Response::" + newresultsRequestSOAP.toString());
                    }
                } catch (Exception e) {
                    e.toString();
                }
                i = i + 1;
            } while (sUsuario == null && i <= 6);
            if (sUsuario == null) {
                Dialogo.dismiss();
                Thread.interrupted();
                sCodVend = ValidarLogin(edtUsuario.getText().toString(), edtSenha.getText().toString()); // verifica se o usuário e senha  existe na base local do dispositivo
                if (sCodVend != null) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle(R.string.app_namesair);
                    builder.setIcon(R.drawable.logo_ico);
                    builder.setMessage("Atenção! Não foi possível obter resposta do servidor referente a validação das informações de atualização, neste caso o usuário será conectado porém até que seja restabelecida " +
                            "a comunicação com o servidor, não será possivel realizar nenhuma atualização ou transmissão de informações.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Login.this, ConsultaPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                                    params.putString(getString(R.string.intent_usuario), edtUsuario.getText().toString());
                                    params.putString(getString(R.string.intent_senha), edtSenha.getText().toString());
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle(R.string.app_namesair);
                    builder.setIcon(R.drawable.logo_ico);
                    builder.setMessage("Atenção! O Usuário ou a senha informada são inválidos ou não existe esse usuário " + edtUsuario.getText().toString() + " cadastrado neste aparelho. Caso seja a primeira utilização, é necessário se conectar online" +
                            " para que o aparelho cadastre esse usuário e posteriormente seja possível se conectar offline.Verifique essas informações e tente novamente.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(sUsuario);
                JSONArray JUsuario = jsonObj.getJSONArray("usuario");
                JSONObject user = JUsuario.getJSONObject(0);

                codVendedor = user.getString("codvend");
                CodEmpresa = user.getString("codempresa");
                UFVendedor = user.getString("uf");
            } catch (Exception e) {
                e.toString();
            }

            if (codVendedor.equals("0")) {
                Dialogo.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder validuser = new AlertDialog.Builder(Login.this);
                        validuser.setTitle(R.string.app_namesair);
                        validuser.setIcon(R.drawable.logo_ico);
                        validuser.setMessage(R.string.invalid_user_password)
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
            } else {
                SharedPreferences.Editor edtEmp = getSharedPreferences(COD_EMPRESA, MODE_PRIVATE).edit();
                edtEmp.putString("codempresa", CodEmpresa);
                edtEmp.commit();

                DB = new ConfigDB(Login.this).getReadableDatabase();
                Cursor CursorUser = DB.rawQuery(" SELECT * FROM USUARIOS WHERE CODVEND = " + codVendedor + " AND CODEMPRESA = " + CodEmpresa, null);
                if (!(CursorUser.getCount() > 0)) {
                    DB.execSQL(" UPDATE USUARIOS SET CODVEND = " + codVendedor + ", CODEMPRESA = " + CodEmpresa +
                            " WHERE CODVEND = " + codVendedor);
                    CursorUser.close();
                }
                CadastrarLogin(usuario, pass, codVendedor, CodEmpresa); // Cadastra usuário, senha e código do vendedor

                String IMEI = "";
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                IMEI = telephonyManager.getDeviceId();

                SoapObject soapValida = new SoapObject(ConfigConex.NAMESPACE, "VerificaUsuario");
                soapValida.addProperty("aUsuario", edtUsuario.getText().toString());
                soapValida.addProperty("aEndMac", IMEI);

                SoapSerializationEnvelope envelopeValida = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelopeValida.setOutputSoapObject(soapValida);
                HttpTransportSE Envio2 = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
                String HabUsuarioApp = "";
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
                        if (j == 0) {

                            Envio2.call("", envelopeValida);

                            SoapObject resultsRequestSOAP2 = (SoapObject) envelopeValida.bodyIn;
                            HabUsuarioApp = (String) envelopeValida.getResponse();
                            System.out.println("Response::" + resultsRequestSOAP2.toString());
                        } else {
                            SoapObject newsoapValida = new SoapObject(ConfigConex.NAMESPACE, "VerificaUsuario");
                            newsoapValida.addProperty("aUsuario", edtUsuario.getText().toString());
                            newsoapValida.addProperty("aEndMac", IMEI);

                            SoapSerializationEnvelope newenvelopeValida = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            newenvelopeValida.setOutputSoapObject(newsoapValida);
                            HttpTransportSE newEnvio2 = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);

                            newEnvio2.call("", newenvelopeValida);

                            SoapObject newresultsRequestSOAP2 = (SoapObject) newenvelopeValida.bodyIn;
                            HabUsuarioApp = (String) newenvelopeValida.getResponse();
                            System.out.println("Response::" + newresultsRequestSOAP2.toString());
                        }
                    } catch (Exception e) {
                        e.toString();
                        Dialogo.dismiss();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                        return;
                    }
                } while (HabUsuarioApp == null && j <= 20);

                Boolean ConexOkWifi = VerificaConexaoWifi();
                if (HabUsuarioApp.equals("True") && ConexOkWifi == true) {
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.sync_companies));
                        }
                    });
                    Sincronismo.SincEmpresas(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_parameters));
                        }
                    });
                    Sincronismo.SincParametrosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_tables));
                        }
                    });
                    Sincronismo.SincDescricaoTabelasStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_locks));

                        }
                    });
                    Sincronismo.SincBloqueiosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_customer_registration));
                        }
                    });
                    Sincronismo.SincronizarClientesEnvioStatic("0", Login.this, edtUsuario.getText().toString(), edtSenha.getText().toString(),null,null,null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage(getString(R.string.sending_orders));
                        }
                    });
                    Sincronismo.SincronizarPedidosEnvioStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this, "0",null,null,null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!UFVendedor.equals("")) {
                                Dialogo.setMessage(getString(R.string.updating_city));
                            }
                        }
                    });
                    Sincronismo.SincAtualizaCidade(UFVendedor, Login.this);
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent IntVend = new Intent(getApplicationContext(), ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), pass);
                            IntVend.putExtras(params);
                            startActivity(IntVend);
                            finish();
                        }
                    });
                } else if (HabUsuarioApp.equals("True")) {
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.sync_companies));
                        }
                    });
                    Sincronismo.SincEmpresas(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_parameters));
                        }
                    });
                    Sincronismo.SincParametrosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_tables));
                        }
                    });
                    Sincronismo.SincDescricaoTabelasStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_locks));

                        }
                    });
                    Sincronismo.SincBloqueiosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this,null,null,null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage(getString(R.string.updating_customer_registration));
                        }
                    });
                    Sincronismo.SincronizarClientesEnvioStatic("0", Login.this, edtUsuario.getText().toString(), edtSenha.getText().toString(),null,null,null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage(getString(R.string.sending_orders));
                        }
                    });
                    Sincronismo.SincronizarPedidosEnvioStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), Login.this, "0",null,null,null);
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent IntVend = new Intent(getApplicationContext(), ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), pass);
                            IntVend.putExtras(params);
                            startActivity(IntVend);
                            finish();
                        }
                    });
                } else {
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                            builder.setTitle(R.string.app_namesair);
                            builder.setIcon(R.drawable.logo_ico);
                            builder.setMessage(R.string.limit_users)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }

            }
        } else

        {
            Dialogo.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage(R.string.no_connection)
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

    }

    private int CadastrarLogin(String NomeUsuario, String Senha, String CodVendedor, String CodEmpresa) {
        int CodVend;
        try {
            Cursor CursorLogin = DB.rawQuery(" SELECT * FROM USUARIOS WHERE USUARIO = '" + NomeUsuario + "' AND SENHA = '" + Senha + "' AND CODEMPRESA = " + CodEmpresa + " AND CODPERFIL = " + idPerfil, null);
            if (CursorLogin.getCount() > 0) {
                CursorLogin.moveToFirst();
                CodVend = CursorLogin.getInt(CursorLogin.getColumnIndex("CODVEND"));
            } else {
                DB.execSQL("INSERT INTO USUARIOS VALUES(" + CodVendedor + ",'" + NomeUsuario + "','" + Senha + "'," + CodEmpresa + "," + idPerfil + ");");
                Cursor cursor1 = DB.rawQuery(" SELECT * FROM USUARIOS WHERE USUARIO = '" + NomeUsuario + "' AND SENHA = '" + Senha + "' AND CODVEND = " + CodVendedor + " AND CODEMPRESA = " + CodEmpresa, null);
                cursor1.moveToFirst();
                CodVend = cursor1.getInt(cursor1.getColumnIndex("CODVEND"));
                cursor1.close();
            }
            CursorLogin.close();

            return CodVend;
        } catch (Exception E) {
            System.out.println("Login, falha no SQL da função CadastrarLogin.Tente novamente.");
            return 0;
        }
    }

    private String ValidarLogin(String NomeUsuario, String Senha) {

        Cursor CursorLogin = DB.rawQuery(" SELECT * FROM USUARIOS WHERE USUARIO = '" + NomeUsuario + "' AND SENHA = '" + Senha + "' AND CODPERFIL = " + idPerfil + "", null);
        if (CursorLogin.getCount() > 0) {
            CursorLogin.moveToFirst();
            String sCodVend = CursorLogin.getString(CursorLogin.getColumnIndex("CODVEND"));
            CursorLogin.close();
            return sCodVend;
        } else {
            //Toast.makeText(Login.this, R.string.invalid_user_password, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_act_principal, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem alteraperfil = menu.findItem(R.id.alteraperfil);
        if (qtdperfil.equals("S")) {
            alteraperfil.setVisible(true);
        } else {
            alteraperfil.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.act_configweb) {
            Intent intent = new Intent(getApplicationContext(), ConfigWeb.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.alteraperfil) {
            carregarperfil();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
