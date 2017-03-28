package com.jdsystem.br.vendasmobile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.PDF;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import static android.content.Context.MODE_PRIVATE;

public class actLogin extends AppCompatActivity implements Runnable {
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
    public String usuario, senha, URLPrincipal, sCodVend, UFVendedor;
    private String CodVendedor = "0";
    public TextView copyright;

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

        if (URLPrincipal == null) {
            Intent intent = new Intent(getApplicationContext(), ConfigWeb.class);
            startActivity(intent);
        }
        if (usuario != null) {
            edtUsuario.setText(usuario);
        }
        if (senha != null) {
            edtSenha.setText(senha);
            cbGravSenha.setChecked(true);
        }
        copyright.setText("Copyright © " + Util.AnoAtual() + " - JD System Tecnologia em Informática");
    }

    public void logar(View view) {
        final String user = edtUsuario.getText().toString();
        final String pass = edtSenha.getText().toString();
        Boolean ConexOk = VerificaConexao();
        if (ConexOk == false) {
            sCodVend = ValidarLogin(user, pass); // verifica se o usuário e senha  existe na base local do dispositivo
            if (sCodVend != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Sem conexão com a Internet! O Usuário será autenticado localmente. Não havendo possibilidade de" +
                        " sincronização de " +
                        "informação com o servidor até que a conexão com a internet seja restabelecida em seu dispositivo.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(actLogin.this, actListPedidos.class);
                                Bundle params = new Bundle();
                                params.putString("codvendedor", sCodVend);
                                params.putString("usuario", user);
                                params.putString("senha", pass);
                                //params.putString("urlPrincipal", URLPrincipal);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Atenção! O Usuário ou a senha informada são inválidos; ou não existe esse usuário " + edtUsuario.getText().toString() + " cadastrado neste aparelho. Caso seja a primeira utilização, é necessário se conectar online" +
                        " para que o aparelho cadastre esse usuário e posteriormente seja possível se conectar offline.Verifique essas informações e tente novamente.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        } else {
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
            Dialogo.setMessage("Verificando e atualizando informações...");
            Dialogo.setTitle("Aguarde");
            Dialogo.show();

        }
        Thread thread = new Thread(actLogin.this);
        thread.start();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
    }

    private void declaraobjetos() {
        DB = new ConfigDB(this).getReadableDatabase();
        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        edtUsuario = (EditText) findViewById(R.id.edtUsuario);
        edtSenha = (EditText) findViewById(R.id.edtSenha);
        cbGravSenha = (CheckBox) findViewById(R.id.cbGravSenha);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        copyright = (TextView) findViewById(R.id.textView2);
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

    public boolean VerificaConexaoWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
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


        int permissionCheck = ContextCompat.checkSelfPermission(actLogin.this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(actLogin.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            Dialogo.dismiss();
            return;
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aUsuario", edtUsuario.getText().toString());
        soap.addProperty("aSenha", edtSenha.getText().toString());
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);


        String CodEmpresa = null;
        final String usuario = edtUsuario.getText().toString();
        final String pass = edtSenha.getText().toString();
        Boolean ConexOk = Util.checarConexaoCelular(actLogin.this);
        if (ConexOk == true) {
            try {
                Envio.call("", envelope);
            } catch (Exception e) {
                e.toString();
                Dialogo.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(actLogin.this, "Falha de comunicação com o servidor, tente novamente!", Toast.LENGTH_LONG).show();
                        return;
                    }
                });
                return;
            }
            try {
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                String sUsuario = (String) envelope.getResponse();
                System.out.println("Response::" + resultsRequestSOAP.toString());

                JSONObject jsonObj = new JSONObject(sUsuario);
                JSONArray JUsuario = jsonObj.getJSONArray("usuario");
                JSONObject user = JUsuario.getJSONObject(0);

                CodVendedor = user.getString("codvend");
                CodEmpresa = user.getString("codempresa");
                UFVendedor = user.getString("uf");
            } catch (Exception e) {
                e.toString();
                Dialogo.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(actLogin.this, "Falha no retorno da informações do servidor, tente novamente!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                return;
            }
            if (CodVendedor.equals("0")) {
                Dialogo.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder validuser = new AlertDialog.Builder(actLogin.this);
                        validuser.setTitle(R.string.app_namesair);
                        validuser.setIcon(R.drawable.logo_ico);
                        validuser.setMessage("Usuário ou senha inválido. Verifique!")
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

                DB = new ConfigDB(actLogin.this).getReadableDatabase();
                Cursor CursorUser = DB.rawQuery(" SELECT * FROM USUARIOS WHERE CODVEND = " + CodVendedor + " AND CODEMPRESA = " + CodEmpresa, null);
                if (!(CursorUser.getCount() > 0)) {
                    DB.execSQL(" UPDATE USUARIOS SET CODVEND = " + CodVendedor + ", CODEMPRESA = " + CodEmpresa +
                            " WHERE CODVEND = " + CodVendedor);
                    CursorUser.close();
                }
                CadastrarLogin(usuario, pass, CodVendedor, CodEmpresa); // Cadastra usuário, senha e código do vendedor

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
                try {
                    Envio2.call("", envelopeValida);
                } catch (Exception e) {
                    e.toString();
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(actLogin.this, "Falha de comunicação com o servidor, tente novamente!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    return;
                }
                try {
                    SoapObject resultsRequestSOAP2 = (SoapObject) envelopeValida.bodyIn;
                    HabUsuarioApp = (String) envelopeValida.getResponse();
                    System.out.println("Response::" + resultsRequestSOAP2.toString());
                } catch (Exception e) {
                    e.toString();
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(actLogin.this, "Falha no retorno da informações do servidor, tente novamente!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    return;

                }
                Boolean ConexOkWifi = VerificaConexaoWifi();
                if (HabUsuarioApp.equals("True") && ConexOkWifi == true) {
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Sincronizando empresas");
                        }
                    });
                    actSincronismo.SincEmpresas(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando parâmetros");
                        }
                    });
                    actSincronismo.SincParametrosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando tabelas");
                        }
                    });
                    actSincronismo.SincDescricaoTabelasStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando bloqueios");

                        }
                    });
                    actSincronismo.SincBloqueiosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage("Atualizando cadastro de clientes");
                        }
                    });
                    actSincronismo.SincronizarClientesEnvioStatic("0", actLogin.this, true, edtUsuario.getText().toString(), edtSenha.getText().toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage("Enviando pedidos...");
                        }
                    });
                    actSincronismo.SincronizarPedidosEnvioStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!UFVendedor.equals("")) {
                                Dialogo.setMessage("Atualizando cadastro de cidades/bairros...");
                            }
                        }
                    });
                    actSincronismo.SincAtualizaCidade(UFVendedor, actLogin.this, true);
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent IntVend = new Intent(getApplicationContext(), actListPedidos.class);
                            Bundle params = new Bundle();
                            params.putString("codvendedor", CodVendedor);
                            params.putString("urlPrincipal", URLPrincipal);
                            params.putString("usuario", usuario);
                            params.putString("senha", pass);
                            IntVend.putExtras(params);
                            startActivity(IntVend);
                        }
                    });
                } else if (HabUsuarioApp.equals("True")) {
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Sincronizando empresas");
                        }
                    });
                    actSincronismo.SincEmpresas(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando parâmetros");
                        }
                    });
                    actSincronismo.SincParametrosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando tabelas");
                        }
                    });
                    actSincronismo.SincDescricaoTabelasStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        public void run() {
                            Dialogo.setMessage("Atualizando bloqueios");

                        }
                    });
                    actSincronismo.SincBloqueiosStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage("Atualizando cadastro de clientes");
                        }
                    });
                    actSincronismo.SincronizarClientesEnvioStatic("0", actLogin.this, true, edtUsuario.getText().toString(), edtSenha.getText().toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Dialogo.setMessage("Enviando pedidos...");
                        }
                    });
                    actSincronismo.SincronizarPedidosEnvioStatic(edtUsuario.getText().toString(), edtSenha.getText().toString(), actLogin.this, true);
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent IntVend = new Intent(getApplicationContext(), actListPedidos.class);
                            Bundle params = new Bundle();
                            params.putString("codvendedor", CodVendedor);
                            params.putString("urlPrincipal", URLPrincipal);
                            params.putString("usuario", usuario);
                            params.putString("senha", pass);
                            IntVend.putExtras(params);
                            startActivity(IntVend);
                        }
                    });
                } else {
                    Dialogo.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
                            builder.setTitle(R.string.app_namesair);
                            builder.setIcon(R.drawable.logo_ico);
                            builder.setMessage("Limite de usuários atingido ou não habilitado. Entre em contato com a empresa.")
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
        } else {
            Dialogo.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(actLogin.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Sem conexão com a internet, Verifique!")
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
            Cursor CursorLogin = DB.rawQuery(" SELECT * FROM USUARIOS WHERE USUARIO = '" + NomeUsuario + "' AND SENHA = '" + Senha + "' AND CODEMPRESA = " + CodEmpresa, null);
            if (CursorLogin.getCount() > 0) {
                CursorLogin.moveToFirst();
                CodVend = CursorLogin.getInt(CursorLogin.getColumnIndex("CODVEND"));
            } else {
                DB.execSQL("INSERT INTO USUARIOS VALUES(" + CodVendedor + ",'" + NomeUsuario + "','" + Senha + "'," + CodEmpresa + ");");
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

        Cursor CursorLogin = DB.rawQuery(" SELECT * FROM USUARIOS WHERE USUARIO = '" + NomeUsuario + "' AND SENHA = '" + Senha + "'", null);
        if (CursorLogin.getCount() > 0) {
            CursorLogin.moveToFirst();
            String sCodVend = CursorLogin.getString(CursorLogin.getColumnIndex("CODVEND"));
            CursorLogin.close();
            return sCodVend;
        } else {
            Toast.makeText(actLogin.this, "Usuário ou Senha inválidos!", Toast.LENGTH_LONG).show();
            return null;
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        carregarpreferencias();
    }
}
