package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
    public SharedPreferences prefs;
    public String ChaveAcesso, RetHost, host, perfil, chave;
    public int idPerfil, codperfil;
    ProgressDialog DialogECB;
    TextView txvlicenca1, txvlicenca2, txvlicenca3, txvempresa1, txvempresa2, txvempresa3, txvexcluir;
    View view0, view1, view2, view3, view4;
    RelativeLayout lyttabela;
    private EditText edtChave;
    private Button btnsalvhost, btnexcluir1, btnexcluir2, btnexcluir3;
    private SQLiteDatabase DB;
    private Handler hd = new Handler();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configweb);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }

        declaraobjetos();
        carregarpreferencias();


        if (ChaveAcesso != null) {
            edtChave.setText("");
        } else {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        carregarlicencas();

        btnexcluir1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigWeb.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.msg_delete_licença)
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                chave = txvlicenca1.getText().toString();
                                excluirhost(chave);
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
        });
        btnexcluir2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigWeb.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.msg_delete_licença)
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                chave = txvlicenca2.getText().toString();
                                excluirhost(chave);
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
        });

        btnexcluir3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigWeb.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.msg_delete_licença)
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                chave = txvlicenca3.getText().toString();
                                excluirhost(chave);
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
        });
    }

    private void excluirhost(String chave) {
        try {
            //pegar o codigo do perfil filtrando pela chave
            Cursor cursorexclicenca = DB.rawQuery("SELECT CODPERFIL FROM PERFIL WHERE LICENCA='" + chave + "'", null);
            cursorexclicenca.moveToFirst();
            if (cursorexclicenca.getCount() > 0) {
                codperfil = cursorexclicenca.getInt(cursorexclicenca.getColumnIndex("CODPERFIL"));
            }

            try {
                //apagando todos os clientes desse perfil
                Cursor dbclientes = DB.rawQuery("SELECT * FROM CLIENTES WHERE CODPERFIL=" + codperfil, null);
                dbclientes.moveToFirst();
                if (dbclientes.getCount() > 0) {
                    DB.execSQL("DELETE FROM CLIENTES WHERE CODPERFIL =" + codperfil);
                }
                dbclientes.close();
            } catch (Exception e) {
                e.toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Util.msg_toast_personal(ConfigWeb.this, "Falha ao tentar excluir os clientes desse perfil", Toast.LENGTH_SHORT);
                }else {
                    Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os clientes desse perfil", Toast.LENGTH_SHORT).show();
                }
            }

            try {
                //apagando todos os parametros desse perfil
                Cursor dbparamapp = DB.rawQuery("SELECT * FROM PARAMAPP WHERE CODPERFIL=" + codperfil, null);
                dbparamapp.moveToFirst();
                if (dbparamapp.getCount() > 0) {
                    DB.execSQL("DELETE FROM PARAMAPP WHERE CODPERFIL =" + codperfil);
                }
                dbparamapp.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os parametros desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando todos os contatos desse perfil
                Cursor dbcontatos = DB.rawQuery("SELECT * FROM CONTATO WHERE CODPERFIL=" + codperfil, null);
                dbcontatos.moveToFirst();
                if (dbcontatos.getCount() > 0) {
                    DB.execSQL("DELETE FROM CONTATO WHERE CODPERFIL =" + codperfil);
                }
                dbcontatos.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os contatos desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando todos os produtos desse perfil
                Cursor dbitens = DB.rawQuery("SELECT * FROM ITENS WHERE CODPERFIL=" + codperfil, null);
                dbitens.moveToFirst();
                if (dbitens.getCount() > 0) {
                    DB.execSQL("DELETE FROM ITENS WHERE CODPERFIL =" + codperfil);
                }
                dbitens.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os produtos desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando todos os itens dos pedidos desse perfil
                Cursor dbpeditens = DB.rawQuery("SELECT * FROM PEDITENS WHERE CODPERFIL=" + codperfil, null);
                dbpeditens.moveToFirst();
                if (dbpeditens.getCount() > 0) {
                    DB.execSQL("DELETE FROM PEDITENS WHERE CODPERFIL =" + codperfil);
                }
                dbpeditens.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os itens dos pedidos desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando todos os pedidos desse perfil
                Cursor dbpedoper = DB.rawQuery("SELECT * FROM PEDOPER WHERE CODPERFIL=" + codperfil, null);
                dbpedoper.moveToFirst();
                if (dbpedoper.getCount() > 0) {
                    DB.execSQL("DELETE FROM PEDOPER WHERE CODPERFIL =" + codperfil);
                }
                dbpedoper.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os pedidos desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando a txvempresa desse perfil
                Cursor dbempresa = DB.rawQuery("SELECT * FROM EMPRESAS WHERE CODPERFIL=" + codperfil, null);
                dbempresa.moveToFirst();
                if (dbempresa.getCount() > 0) {
                    DB.execSQL("DELETE FROM EMPRESAS WHERE CODPERFIL =" + codperfil);
                }
                dbempresa.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir a txvempresa desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando os usuarios desse perfil
                Cursor dbusuario = DB.rawQuery("SELECT * FROM USUARIOS WHERE CODPERFIL=" + codperfil, null);
                dbusuario.moveToFirst();
                if (dbusuario.getCount() > 0) {
                    DB.execSQL("DELETE FROM USUARIOS WHERE CODPERFIL =" + codperfil);
                }
                dbusuario.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os usuários desse perfil", Toast.LENGTH_SHORT).show();
            }

            try {
                //apagando os bloqueios desse perfil
                Cursor dbbloqueio = DB.rawQuery("SELECT * FROM BLOQCLIE WHERE CODPERFIL=" + codperfil, null);
                dbbloqueio.moveToFirst();
                if (dbbloqueio.getCount() > 0) {
                    DB.execSQL("DELETE FROM BLOQCLIE WHERE CODPERFIL =" + codperfil);
                }
                dbbloqueio.close();
            } catch (Exception e) {
                e.toString();
                Toast.makeText(ConfigWeb.this, "Falha ao tentar excluir os bloqueios desse perfil", Toast.LENGTH_SHORT).show();
            }

            //após apagar todas as tabelas que tem esse perfil será apagado esse id de perfil
            if (cursorexclicenca.getCount() > 0) {
                DB.execSQL("DELETE FROM PERFIL WHERE CODPERFIL =" + codperfil);
            }
            cursorexclicenca.close();
        } catch (Exception e) {
            e.toString();
            Toast.makeText(ConfigWeb.this, "Falha ao tentar deletar o perfil", Toast.LENGTH_SHORT).show();
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
        lyttabela = (RelativeLayout) findViewById(R.id.tabela);
        view0 = (View) findViewById(R.id.view0);
        view1 = (View) findViewById(R.id.view1);
        view2 = (View) findViewById(R.id.view2);
        view3 = (View) findViewById(R.id.view3);
        view4 = (View) findViewById(R.id.view4);
        btnexcluir1 = (Button) findViewById(R.id.excluir1);
        btnexcluir2 = (Button) findViewById(R.id.excluir2);
        btnexcluir3 = (Button) findViewById(R.id.excluir3);
        txvempresa1 = (TextView) findViewById(R.id.txtempresa1);
        txvempresa2 = (TextView) findViewById(R.id.txtempresa2);
        txvempresa3 = (TextView) findViewById(R.id.txtempresa3);
        txvlicenca1 = (TextView) findViewById(R.id.txtlicenca1);
        txvlicenca2 = (TextView) findViewById(R.id.txtlicenca2);
        txvlicenca3 = (TextView) findViewById(R.id.txtlicenca3);
        txvexcluir = (TextView) findViewById(R.id.txtexcluir);
        DB = new ConfigDB(this).getReadableDatabase();
        btnsalvhost = (Button) findViewById(R.id.btsalvhost);
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
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSHOST, 10000);
        RetHost = null;
        Boolean ConexOk = Util.checarConexaoCelular(ConfigWeb.this);
        if (ConexOk) {
            int j = 0;
            do {
                try {
                    if (j > 0) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (j == 0) {

                    try {
                        Envio.call("", envelope);
                    } catch (Exception e) {
                        e.toString();
                        DialogECB.dismiss();
                        hd.post(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    Util.msg_toast_personal(ConfigWeb.this, "Falha de comunicação com o servidor. Tente novamente!", Toast.LENGTH_SHORT);
                                }else {
                                    Toast.makeText(ConfigWeb.this, "Falha de comunicação com o servidor. Tente novamente!", Toast.LENGTH_SHORT).show();
                                }
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
                } else {
                    SoapObject newsoap = new SoapObject(ConfigConex.NAMESPACE, "CarregaHostCliente");
                    newsoap.addProperty("Chave", edtChave.getText().toString());
                    SoapSerializationEnvelope newenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    newenvelope.setOutputSoapObject(newsoap);
                    HttpTransportSE newEnvio = new HttpTransportSE(ConfigConex.URLDADOSHOST);

                    try {
                        newEnvio.call("", newenvelope);
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
                        SoapObject newresultsRequestSOAP = (SoapObject) newenvelope.bodyIn;
                        RetHost = (String) newenvelope.getResponse();
                        System.out.println("Response :" + newresultsRequestSOAP.toString());
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
                }

            } while (RetHost == null && j <= 6);

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
            perfil = c.getString("nomeempresa").toUpperCase();
            host = c.getString("host");

            if (!(gravarperfil(host, perfil, licenca))) {
                return;
            }
        } catch (Exception e) {
            e.toString();
        }
        DialogECB.dismiss();
        gravarpreferencias();
        hd.post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Util.msg_toast_personal(ConfigWeb.this, "Licença validada com sucesso!", Toast.LENGTH_SHORT);
                }else{
                    Toast.makeText(ConfigWeb.this, "Licença validada com sucesso!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    private void gravarpreferencias() {
        try {
            Cursor idlicenca = DB.rawQuery("SELECT LICENCA,CODPERFIL FROM PERFIL WHERE LICENCA = '" + edtChave.getText().toString() + "'", null);
            idlicenca.moveToFirst();
            if (idlicenca.getCount() > 0) {
                idPerfil = idlicenca.getInt(idlicenca.getColumnIndex("CODPERFIL"));
                idlicenca.close();
            }
        } catch (Exception e) {
            e.toString();
        }
        SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
        editorhost.putString("ChaveAcesso", edtChave.getText().toString());
        editorhost.putString("host", host);
        editorhost.putString("nome", perfil);
        editorhost.putInt("idperfil", idPerfil);
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
                cursorlicenca.close();
            } else {
                DB.execSQL("INSERT INTO PERFIL(LICENCA,HOST,NOMEPERFIL) VALUES('" + edtChave.getText().toString() + "','" + host + "','" + perfil + "');");
                cursorlicenca.close();
                return true;
            }

        } catch (Exception e) {
            e.toString();
        }
        return false;
    }

    private void carregarlicencas() {
        String nomeperfil1 = null;
        String nomeperfil2 = null;
        String nomeperfil3 = null;
        List<String> DadosListPerfil = new ArrayList<String>();

        String licperfil1 = null;
        String licperfil2 = null;
        String licperfil3 = null;
        List<String> DadosListLicPerfil = new ArrayList<String>();

        Cursor cursorPerfil = DB.rawQuery("SELECT * FROM PERFIL", null);
        cursorPerfil.moveToFirst();
        if (cursorPerfil.getCount() > 0) {
            lyttabela.setVisibility(View.VISIBLE);
            do {
                DadosListPerfil.add(cursorPerfil.getString(cursorPerfil.getColumnIndex("NOMEPERFIL")));
                DadosListLicPerfil.add(cursorPerfil.getString(cursorPerfil.getColumnIndex("LICENCA")));
            } while (cursorPerfil.moveToNext());
            int i = DadosListPerfil.size();
            switch (i) {
                case 1:
                    nomeperfil1 = DadosListPerfil.get(0);
                    //nomeperfil1 = nomeperfil1.toUpperCase();
                    licperfil1 = DadosListLicPerfil.get(0);
                    txvempresa1.setText(nomeperfil1);
                    txvlicenca1.setText(licperfil1);
                    view0.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    view2.setVisibility(View.VISIBLE);
                    view3.setVisibility(View.GONE);
                    view4.setVisibility(View.GONE);
                    txvempresa1.setVisibility(View.VISIBLE);
                    txvempresa2.setVisibility(View.GONE);
                    txvempresa3.setVisibility(View.GONE);
                    txvlicenca1.setVisibility(View.VISIBLE);
                    txvlicenca2.setVisibility(View.GONE);
                    txvlicenca3.setVisibility(View.GONE);
                    btnexcluir1.setVisibility(View.GONE);
                    btnexcluir2.setVisibility(View.GONE);
                    btnexcluir3.setVisibility(View.GONE);
                    txvexcluir.setVisibility(View.GONE);
                    break;
                case 2:
                    nomeperfil1 = DadosListPerfil.get(0);
                    //nomeperfil1 = nomeperfil1.toUpperCase();
                    licperfil1 = DadosListLicPerfil.get(0);
                    txvempresa1.setText(nomeperfil1);
                    txvlicenca1.setText(licperfil1);
                    nomeperfil2 = DadosListPerfil.get(1);
                    //nomeperfil2 = nomeperfil2.toUpperCase();
                    licperfil2 = DadosListLicPerfil.get(1);
                    txvempresa2.setText(nomeperfil2);
                    txvlicenca2.setText(licperfil2);
                    view0.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    view2.setVisibility(View.VISIBLE);
                    view3.setVisibility(View.VISIBLE);
                    view4.setVisibility(View.GONE);
                    txvempresa1.setVisibility(View.VISIBLE);
                    txvempresa2.setVisibility(View.VISIBLE);
                    txvempresa3.setVisibility(View.GONE);
                    txvlicenca1.setVisibility(View.VISIBLE);
                    txvlicenca2.setVisibility(View.VISIBLE);
                    txvlicenca3.setVisibility(View.GONE);
                    btnexcluir1.setVisibility(View.VISIBLE);
                    btnexcluir2.setVisibility(View.VISIBLE);
                    btnexcluir3.setVisibility(View.GONE);
                    txvexcluir.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    nomeperfil1 = DadosListPerfil.get(0);
                    //nomeperfil1 = nomeperfil1.toUpperCase();
                    licperfil1 = DadosListLicPerfil.get(0);
                    txvempresa1.setText(nomeperfil1);
                    txvlicenca1.setText(licperfil1);
                    nomeperfil2 = DadosListPerfil.get(1);
                    //nomeperfil2 = nomeperfil2.toUpperCase();
                    licperfil2 = DadosListLicPerfil.get(1);
                    txvempresa2.setText(nomeperfil2);
                    txvlicenca2.setText(licperfil2);
                    nomeperfil3 = DadosListPerfil.get(2);
                    //nomeperfil3 = nomeperfil3.toUpperCase();
                    licperfil3 = DadosListLicPerfil.get(2);
                    txvempresa3.setText(nomeperfil3);
                    txvlicenca3.setText(licperfil3);
                    view0.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    view2.setVisibility(View.VISIBLE);
                    view3.setVisibility(View.VISIBLE);
                    view4.setVisibility(View.VISIBLE);
                    txvempresa1.setVisibility(View.VISIBLE);
                    txvempresa2.setVisibility(View.VISIBLE);
                    txvempresa3.setVisibility(View.VISIBLE);
                    txvlicenca1.setVisibility(View.VISIBLE);
                    txvlicenca2.setVisibility(View.VISIBLE);
                    txvlicenca3.setVisibility(View.VISIBLE);
                    btnexcluir1.setVisibility(View.VISIBLE);
                    btnexcluir2.setVisibility(View.VISIBLE);
                    btnexcluir3.setVisibility(View.VISIBLE);
                    txvexcluir.setVisibility(View.VISIBLE);
            }
            cursorPerfil.close();
        } else {
            lyttabela.setVisibility(View.GONE);
            cursorPerfil.close();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ConfigWeb.this, Login.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}