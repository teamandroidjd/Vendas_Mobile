package com.jdsystem.br.vendasmobile;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Controller.Lista_clientes;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.jdsystem.br.vendasmobile.R.id.url;


public class act_CadClientes extends AppCompatActivity implements Runnable, View.OnFocusChangeListener {

    String sTipoPessoa, sUF, sCodVend, NomeBairro, NomeCidade, usuario, senha, URLPrincipal, nomeRazao;
    private Handler handler = new Handler();
    Spinner spCidade, spTipoPessoa, spBairro, spUF;
    int CodCidade, CodBairro, telaInvocada, codClieExt;
    Boolean PesqCEP;
    ImageButton BtnPesqCep, BtnconsultaCNPJ;
    private static ProgressDialog DialogECB;
    EditText nomerazao, nomefan, nomecompleto, cnpjcpf, Edtcpf, EdtRG, ie, endereco, numero, cep, tel1, tel2, email, edtOBS, Complemento;
    SQLiteDatabase DB;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_clientes);

        declaraobjetos();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                usuario = params.getString("usuario");
                senha = params.getString("senha");
                URLPrincipal = params.getString("urlPrincipal");
                telaInvocada = params.getInt("listaclie");
            }
        }

        PesqCEP = false;
        NomeBairro = null;
        NomeCidade = null;

        spTipoPessoa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sTipoPessoa = "F";
                        break;
                    case 1:
                        sTipoPessoa = "J";
                        break;
                }
                if (sTipoPessoa == "F") {
                    nomerazao.setVisibility(EditText.GONE);
                    nomefan.setVisibility(EditText.GONE);
                    cnpjcpf.setVisibility(EditText.GONE);
                    ie.setVisibility(EditText.GONE);
                    Edtcpf.setVisibility(EditText.VISIBLE);
                    nomecompleto.setVisibility(EditText.VISIBLE);
                    EdtRG.setVisibility(EditText.VISIBLE);
                } else {
                    nomerazao.setVisibility(EditText.VISIBLE);
                    nomefan.setVisibility(EditText.VISIBLE);
                    cnpjcpf.setVisibility(EditText.VISIBLE);
                    ie.setVisibility(EditText.VISIBLE);
                    Edtcpf.setVisibility(EditText.GONE);
                    nomecompleto.setVisibility(EditText.GONE);
                    EdtRG.setVisibility(EditText.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spUF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        sUF = "AC"; //Acre
                        break;
                    case 2:
                        sUF = "AL"; // Alagoas
                        break;
                    case 3:
                        sUF = "AP"; //Amapá
                        break;
                    case 4:
                        sUF = "AM";//Amazonas
                        break;
                    case 5:
                        sUF = "BA";//Bahia
                        break;
                    case 6:
                        sUF = "CE";//Ceará
                        break;
                    case 7:
                        sUF = "DF";//Distrito Federal
                        break;
                    case 8:
                        sUF = "ES";//Espírito Santo
                        break;
                    case 9:
                        sUF = "GO";//Goiás
                        break;
                    case 10:
                        sUF = "MA";//Maranhão
                        break;
                    case 11:
                        sUF = "MT";//Mato Grosso
                        break;
                    case 12:
                        sUF = "MS";//Mato Grosso do Sul
                        break;
                    case 13:
                        sUF = "MG";//Minas Gerais
                        break;
                    case 14:
                        sUF = "PA";//Pará
                        break;
                    case 15:
                        sUF = "PB";//Paraíba
                        break;
                    case 16:
                        sUF = "PR";//Paraná
                        break;
                    case 17:
                        sUF = "PE";//Pernambuco
                        break;
                    case 18:
                        sUF = "PI";//Piauí
                        break;
                    case 19:
                        sUF = "RJ";//Rio de Janeiro
                        break;
                    case 20:
                        sUF = "RN"; //Rio Grande do Norte
                        break;
                    case 21:
                        sUF = "RS";//Rio Grande do Sul
                        break;
                    case 22:
                        sUF = "RO"; //Rondônia
                        break;
                    case 23:
                        sUF = "RR"; //Roraima
                        break;
                    case 24:
                        sUF = "SC";//Santa Catarina
                        break;
                    case 25:
                        sUF = "SP";//São Paulo
                        break;
                    case 26:
                        sUF = "SE";//Sergipe
                        break;
                    case 27:
                        sUF = "TO";//Tocantins
                        break;
                }
                Boolean ConexOk = VerificaConexao();
                if (ConexOk == false) {
                    int CodCidade = 0;
                    try {
                        Cursor cursor = DB.rawQuery(" SELECT CODCIDADE_EXT, DESCRICAO FROM CIDADES WHERE UF = '" + sUF + "'", null);
                        List<String> DadosList = new ArrayList<String>();
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                String Cidade = cursor.getString(cursor.getColumnIndex("DESCRICAO"));
                                CodCidade = cursor.getInt(cursor.getColumnIndex("CODCIDADE_EXT"));
                                DadosList.add(Cidade);
                            } while (cursor.moveToNext());
                            cursor.close();

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                            ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                            spCidade.setAdapter(spinnerArrayAdapter);
                        }
                    } catch (Exception E) {
                        System.out.println("Error" + E);
                    }
                }
                Thread thread = new Thread(act_CadClientes.this);
                thread.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Boolean ConexOk = VerificaConexao();
                if (ConexOk == false) {
                    NomeCidade = spCidade.getSelectedItem().toString();
                    Cursor CurCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "'", null);
                    if (CurCidade.getCount() > 0) {
                        CurCidade.moveToFirst();
                        CodCidade = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE_EXT"));
                    }
                    CurCidade.close();
                    Cursor CurBairro = null;
                    try {
                        if (PesqCEP.equals(false)) {
                            CurBairro = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidade, null);
                        } else {
                            CurBairro = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE DESCRICAO = '" + NomeBairro + "'", null);
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                    List<String> DadosListBairro = new ArrayList<String>();
                    if (CurBairro.getCount() > 0) {
                        CurBairro.moveToFirst();
                        do {
                            String Bairro = CurBairro.getString(CurBairro.getColumnIndex("DESCRICAO"));
                            DadosListBairro.add(Bairro);
                        } while (CurBairro.moveToNext());
                    }
                    CurBairro.close();

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                    ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spBairro.setAdapter(spinnerArrayAdapter);

                } else {
                    NomeCidade = spCidade.getSelectedItem().toString();
                    Cursor CurCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "'", null);
                    if (CurCidade.getCount() > 0) {
                        CurCidade.moveToFirst();
                        CodCidade = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE"));
                    }
                    CurCidade.close();
                    Cursor CurBairro = null;
                    try {
                        if (PesqCEP.equals(false)) {
                            CurBairro = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidade, null);
                        } else {
                            CurBairro = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE DESCRICAO = '" + NomeBairro + "'", null);
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                    List<String> DadosListBairro = new ArrayList<String>();
                    if (CurBairro.getCount() > 0) {
                        CurBairro.moveToFirst();
                        do {
                            String Bairro = CurBairro.getString(CurBairro.getColumnIndex("DESCRICAO"));
                            DadosListBairro.add(Bairro);
                        } while (CurBairro.moveToNext());
                    }
                    CurBairro.close();

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                    ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spBairro.setAdapter(spinnerArrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spBairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NomeBairro = spBairro.getSelectedItem().toString();
                try {
                    Cursor CurBai = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE DESCRICAO = '" + NomeBairro + "'", null);
                    if (CurBai.getCount() > 0) {
                        CurBai.moveToFirst();
                        CodBairro = CurBai.getInt(CurBai.getColumnIndex("CODBAIRRO"));
                    }
                    CurBai.close();
                } catch (Exception E) {
                    System.out.println("Error" + E);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void declaraobjetos() {

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        DB = new ConfigDB(this).getReadableDatabase();
        BtnPesqCep = (ImageButton) findViewById(R.id.btnBuscaCep);
        spTipoPessoa = (Spinner) findViewById(R.id.spnTipoPessoa);
        spUF = (Spinner) findViewById(R.id.spnUF);
        spCidade = (Spinner) findViewById(R.id.spnCidade);
        spBairro = (Spinner) findViewById(R.id.spnBairro);
        nomerazao = (EditText) findViewById(R.id.EdtNomeRazao);
        nomefan = (EditText) findViewById(R.id.EdtNomeFan);
        cnpjcpf = (EditText) findViewById(R.id.EdtCnpjCpf);
        ie = (EditText) findViewById(R.id.EdtIE);
        endereco = (EditText) findViewById(R.id.EdtEndereco);
        numero = (EditText) findViewById(R.id.EdtNumero);
        Complemento = (EditText) findViewById(R.id.EdtComple);
        email = (EditText) findViewById(R.id.EdtEmail);
        cep = (EditText) findViewById(R.id.EdtCep);
        tel1 = (EditText) findViewById(R.id.EdtTel1);
        tel2 = (EditText) findViewById(R.id.EdtTel2);
        Edtcpf = (EditText) findViewById(R.id.Edtcpf);
        nomecompleto = (EditText) findViewById(R.id.EdtNomeCompleto);
        EdtRG = (EditText) findViewById(R.id.EdtRG);
        edtOBS = (EditText) findViewById(R.id.EdtOBS);

        final EditText etCNPJ = (EditText) findViewById(R.id.EdtCnpjCpf);
        etCNPJ.addTextChangedListener(Mask.insert(Mask.CNPJ_MASK, etCNPJ));

        final EditText etCPF = (EditText) findViewById(R.id.Edtcpf);
        etCPF.addTextChangedListener(Mask.insert(Mask.CPF_MASK, etCPF));

        final EditText etCEP = (EditText) findViewById(R.id.EdtCep);
        etCEP.addTextChangedListener(Mask.insert(Mask.CEP_MASK, etCEP));

        EditText etTelefone1 = (EditText) findViewById(R.id.EdtTel1);
        etTelefone1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone1));

        EditText etTelefone2 = (EditText) findViewById(R.id.EdtTel2);
        etTelefone2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone2));

        Edtcpf.setOnFocusChangeListener(this);
        cnpjcpf.setOnFocusChangeListener(this);
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

    public void buscacepclie(View view) {
        String sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");
        if (sCEP.length() < 8) {
            cep.setError("CEP incompleto. Verifique!");
            cep.requestFocus();
            return;
        }
        DialogECB = new ProgressDialog(act_CadClientes.this);
        DialogECB.setTitle("Aguarde.");
        DialogECB.setMessage("Pesquisando o CEP informado...");
        DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.show();

        cadastraDadosCep(sCEP);
    }

    public void cadastraDadosCep(String cep) {
        String Estado = null;
        String Cidade = null;
        Boolean AtualizaEst = true;
        PesqCEP = true;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = this.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        String URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "PesquisaCEP");
        soap.addProperty("aCEP", cep);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
        String RetDadosEndereco = null;

        try {
            Boolean ConexOK = Util.checarConexaoCelular(this);
            if (ConexOK == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                RetDadosEndereco = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            } else {
                DialogECB.dismiss();
                Toast.makeText(act_CadClientes.this, "Sem conexão com a internet! Verifique e tente novamente.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
        if(RetDadosEndereco.equals("CEP não Encontrado")){
            DialogECB.dismiss();
            Toast.makeText(act_CadClientes.this, "CEP não encontrado na base de dados. Verifique se está correto e tente novamente.", Toast.LENGTH_LONG).show();
            endereco.setText("");
            return;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetDadosEndereco);
            JSONArray JEndereco = jsonObj.getJSONArray("cep");
            int jumpTime = 0;
            final int totalProgressTime = JEndereco.length();
            DB = new ConfigDB(this).getReadableDatabase();

            for (int i = 0; i < JEndereco.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    JSONObject c = JEndereco.getJSONObject(jumpTime);
                    try {
                        jumpTime += 1;
                        String SiglaEstado = c.getString("uf");

                        Cursor CursosEstado = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);

                        if (CursosEstado.getCount() > 0) {
                            DB.execSQL(" UPDATE ESTADOS SET UF = '" + SiglaEstado + "', DESCRICAO = '" + SiglaEstado + "'" +
                                    " WHERE UF = '" + SiglaEstado + "'");
                            Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);
                            cursor1.moveToFirst();
                            Estado = cursor1.getString(CursosEstado.getColumnIndex("UF"));
                            cursor1.close();
                        } else {
                            DB.execSQL("INSERT INTO ESTADOS VALUES('" + SiglaEstado + "','" + SiglaEstado + "');");
                            Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);
                            cursor1.moveToFirst();
                            Estado = cursor1.getString(cursor1.getColumnIndex("UF"));
                            cursor1.close();
                        }
                        CursosEstado.close();
                    } catch (Exception E) {
                    }

                    //Cadastrar Cidades
                    try {
                        NomeCidade = c.getString("cidade");
                        //int CodCidadeExt = c.getInt("id_cidade");
                        NomeCidade = NomeCidade.replaceAll("'", "");

                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF FROM CIDADES WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                        if (CursorCidade.getCount() > 0) {
                            DB.execSQL(" UPDATE CIDADES SET UF = '" + Estado + "', DESCRICAO = '" + NomeCidade + "' " +//, CODCIDADE_EXT = '" + CodCidadeExt + "'" +
                                    " WHERE DESCRICAO = '" + NomeCidade + "'");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidade = Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE_EXT")));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF)" +
                                    " VALUES('" + NomeCidade + "','" + Estado + "');");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidade = Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE_EXT")));
                            cursor1.close();
                        }
                        CursorCidade.close();
                    } catch (Exception E) {
                        E.printStackTrace();
                    }

                    //Cadastrar Bairros
                    try {
                        NomeBairro = c.getString("bairro");
                        int CodBairroExt = c.getInt("id_bairro");
                        NomeBairro = NomeBairro.replaceAll("'", "");

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODCIDADE FROM BAIRROS WHERE CODCIDADE = " + CodCidade + " AND DESCRICAO = '" + NomeBairro + "'", null);
                        if (CursorBairro.getCount() > 0) {
                            CursorBairro.moveToFirst();
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = " + CodCidade + ", DESCRICAO = '" + NomeBairro + "'" +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + CodCidade + "'");
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODCIDADE)" +
                                    " VALUES('" + NomeBairro + "'," + CodCidade + ");");
                        }
                        CursorBairro.close();
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                    String end = c.getString("logradouro");
                    String tipoend = c.getString("tipo_logradouro");
                    endereco.setText(tipoend + " " + end);
                    numero.requestFocus();

                    //Estado
                    List<String> DadosListEstado = new ArrayList<String>();
                    DadosListEstado.add(Estado);
                    sUF = Estado;
                    ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListEstado);
                    arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);

                    spUF.setAdapter(arrayAdapterUF);

                    //Cidade
                    List<String> DadosListCidade = new ArrayList<String>();
                    DadosListCidade.add(NomeCidade);
                    ArrayAdapter<String> arrayAdapterCidade = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListCidade);
                    ArrayAdapter<String> spinnerArrayAdapterCidade = arrayAdapterCidade;
                    spinnerArrayAdapterCidade.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spCidade.setAdapter(spinnerArrayAdapterCidade);

                    //Bairro
                    List<String> DadosListBairroUnic = new ArrayList<String>();
                    DadosListBairroUnic.add(NomeBairro);
                    ArrayAdapter<String> arrayAdapterBairroUnic = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairroUnic);
                    ArrayAdapter<String> spinnerArrayAdapterBairroUnic = arrayAdapterBairroUnic;
                    spinnerArrayAdapterBairroUnic.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spBairro.setAdapter(spinnerArrayAdapterBairroUnic);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

    }

    @Override
    public void onBackPressed() {
        if (nomecompleto.getText().length() != 0 || Edtcpf.getText().length() != 0 || EdtRG.getText().length() != 0 || nomerazao.getText().length() != 0 ||
                nomefan.getText().length() != 0 || cnpjcpf.getText().length() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(act_CadClientes.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Deseja realmente cancelar o cadasto do cliente?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(act_CadClientes.this, actListPedidos.class);
                            Bundle params = new Bundle();
                            params.putString("codvendedor", sCodVend);
                            intent.putExtras(params);
                            startActivity(intent);
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
            return;

        } else {
            Intent i = new Intent(act_CadClientes.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            finish();
        }
        super.onBackPressed();
    }

    public void btnsalvar(View view) {

        String NomePessoa = null;
        String NomeFantasia = null;
        String CpfCnpj = null;
        String CEP;
        boolean validacliente;
        validacliente = validarclientes();

        if (sTipoPessoa == "J") {
            if (nomerazao.getText().length() == 0) {
                nomerazao.setError("Digite a Razão Social!");
                nomerazao.requestFocus();
                return;
            } else if (nomefan.getText().length() == 0) {
                nomefan.setError("Digite o nome Fantasia!");
                nomefan.requestFocus();
                return;
            } else if (cnpjcpf.getText().length() == 0) {
                cnpjcpf.setError("Digite o CNPJ!");
                cnpjcpf.requestFocus();
                return;
            }
            NomePessoa = nomerazao.getText().toString();
            NomeFantasia = nomefan.getText().toString();
            CpfCnpj = cnpjcpf.getText().toString().replaceAll("[^0123456789]", "");
        } else if (sTipoPessoa == "F") {
            if (nomecompleto.getText().length() == 0) {
                nomecompleto.setError("Digite o Nome Completo!");
                nomecompleto.requestFocus();
                return;
            } else if (Edtcpf.getText().length() == 0) {
                Edtcpf.setError("Digite a Identidade!");
                Edtcpf.requestFocus();
                return;
            }
            NomePessoa = nomecompleto.getText().toString().trim();
            NomeFantasia = NomePessoa;
            CpfCnpj = Edtcpf.getText().toString().replaceAll("[^0123456789]", "");
        }
        if (endereco.getText().length() == 0) {
            endereco.setError("Digite o Logradouro!");
            endereco.requestFocus();
            return;
        } else if (numero.getText().length() == 0) {
            numero.setError("Digite o número da rua!");
            numero.requestFocus();
            return;
        } else if (cep.getText().length() == 0) {
            cep.setError("Digite o CEP!");
            cep.requestFocus();
            return;
        } /*else if (Util.validaEmail(email.getText().toString()) == false) {
            email.setError("E-mail inválido! Verifique.");
            email.requestFocus();
            return;
        }*/
        CEP = cep.getText().toString().replaceAll("[.-]", "");

        Cursor CursorClieCons = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                " CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + CpfCnpj + "'", null);
        try {
            if (CursorClieCons.getCount() > 0) {
                CursorClieCons.moveToFirst();
                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + NomePessoa +
                        "', NOMEFAN = '" + NomeFantasia +
                        "', INSCREST = '" + ie.getText().toString() + "', EMAIL = '" + email.getText().toString() +
                        "', TEL1 = '" + tel1.getText().toString() + "', TEL2 = '" + tel2.getText().toString() + "', ENDERECO = '" + endereco.getText().toString() +
                        "', NUMERO = '" + numero.getText().toString() + "', COMPLEMENT = '" + Complemento.getText().toString() +
                        "', CODBAIRRO = '" + CodBairro + "', OBS = '" + edtOBS.getText().toString() + "', CODCIDADE = '" + CodCidade + "', UF = '" + sUF +
                        "', CEP = '" + CEP + "', " +
                        " TIPOPESSOA = '" + sTipoPessoa + "', REGIDENT = '" + EdtRG.getText().toString() + "', ATIVO = 'S'" +
                        ", CODVENDEDOR = " + sCodVend +
                        " WHERE CNPJ_CPF = '" + CpfCnpj + "'");
            } else {
                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                        "CEP, CODVENDEDOR, TIPOPESSOA, ATIVO, BLOQUEIO, REGIDENT, FLAGINTEGRADO) VALUES(" +
                        "'" + CpfCnpj + "', '" + NomePessoa +
                        "','" + NomeFantasia + "', '" + ie.getText().toString() + "', '" + email.getText().toString() +
                        "','" + tel1.getText().toString() + "', '" + tel2.getText().toString() + "', '" + endereco.getText().toString() +
                        "','" + numero.getText().toString() + "', '" + Complemento.getText().toString() +
                        "'," + CodBairro + ", '" + edtOBS.getText().toString() + "', " + CodCidade + ", '" + sUF +
                        "','" + CEP + "', " + sCodVend + ", '" + sTipoPessoa + "', '" + "S" + "', '" + "01" + "', '" + EdtRG.getText().toString() + "','"
                        + "1" + "');");
            }
            CursorClieCons.close();

            Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + CpfCnpj + "'", null);
            cursor1.moveToFirst();
            final String CodCliente = cursor1.getString(cursor1.getColumnIndex("CODCLIE_INT"));

            Toast.makeText(this, "Cliente salvo com sucesso!", Toast.LENGTH_SHORT).show();

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(act_CadClientes.this);
            builder.setTitle("Sincronização");
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Deseja sincronizar o cliente cadastrado?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            boolean sitclieenvio;
                            if (telaInvocada == 0) {
                                sitclieenvio = actSincronismo.SincronizarClientesEnvioStatic(CodCliente, act_CadClientes.this, usuario, senha);
                                if (sitclieenvio == true) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act_CadClientes.this, "Cliente sincronizado com sucesso!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), Lista_clientes.class);
                                    Bundle params = new Bundle();
                                    params.putString("CodVendedor", sCodVend);
                                    params.putString("usuario", usuario);
                                    params.putString("senha", senha);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act_CadClientes.this, "Não foi possivel sincronizar o cliente. Tente novamente!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), Lista_clientes.class);
                                    Bundle params = new Bundle();
                                    params.putString("CodVendedor", sCodVend);
                                    params.putString("usuario", usuario);
                                    params.putString("senha", senha);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                sitclieenvio = actSincronismo.SincronizarClientesEnvioStatic(CodCliente, act_CadClientes.this, usuario, senha);
                                if (sitclieenvio == true) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act_CadClientes.this, "Cliente sincronizado com sucesso!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), act_ListClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString("codvendedor", sCodVend);
                                    params.putString("usuario", usuario);
                                    params.putString("senha", senha);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act_CadClientes.this, "Não foi possivel sincronizar o cliente. Tente novamente!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), act_ListClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString("codvendedor", sCodVend);
                                    params.putString("usuario", usuario);
                                    params.putString("senha", senha);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (telaInvocada == 0) {
                                Intent intent = new Intent(getBaseContext(), Lista_clientes.class);
                                Bundle params = new Bundle();
                                params.putString("CodVendedor", sCodVend);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();

                            } else {
                                Intent intent = new Intent(getBaseContext(), act_ListClientes.class);
                                Bundle params = new Bundle();
                                params.putString("codvendedor", sCodVend);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();

                            }
                        }
                    });
            android.app.AlertDialog alert = builder.create();
            alert.show();

        } catch (Exception E) {
            //Toast.makeText(this, "Não foi possivel salvar o CLiente!", Toast.LENGTH_SHORT).show();
            System.out.println("Error" + E);
        }
    }

    public void consultacnpj(View view) {

        String CNPJ = cnpjcpf.getText().toString().replaceAll("[^0123456789]", "");
        cadastraDadosCNPJ(CNPJ);
    }

    private void cadastraDadosCNPJ(String cnpj) {
        /*HttpURLConnection  urlConnection = null;

        try {
            URL url = new URL("https://www.receitaws.com.br/v1/cnpj/" + cnpj);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15001);
            urlConnection.setConnectTimeout(15001);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }*/




        /*try {
            //JSONObject jsonObj = new JSONObject(in);
            //JSONArray JCNPJ = jsonObj.getJSONArray("" +
                   // "");

            DB = new ConfigDB(this).getReadableDatabase();

            //JSONObject c = JCNPJ.getJSONObject(0);
            String nome = c.getString("nome");
            String uf = c.getString("uf");
            String tel1 = c.getString("telefone");
            String bairro = c.getString("bairro");
            String endereco = c.getString("logradouro");
            String numero = c.getString("numero");
            String cep = c.getString("cep");
            String cidade = c.getString("municipio");
            String nomefan = c.getString("fantasia");
            String complemento = c.getString("complemento");


        } catch (Exception E) {
            E.printStackTrace();
        }*/

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                /*int CodCidade = 0;
                try {

                    Cursor cursor = DB.rawQuery(" SELECT CODCIDADE_EXT, DESCRICAO FROM CIDADES WHERE UF = '" + sUF + "'", null);
                    List<String> DadosList = new ArrayList<String>();
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            String Cidade = cursor.getString(cursor.getColumnIndex("DESCRICAO"));
                            CodCidade = cursor.getInt(cursor.getColumnIndex("CODCIDADE_EXT"));
                            DadosList.add(Cidade);
                        } while (cursor.moveToNext());
                        cursor.close();

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                        ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        spCidade.setAdapter(spinnerArrayAdapter);
                    }
                } catch (Exception E) {
                    System.out.println("Error" + E);
                }*/

            }
        });

    }


    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("act_CadClientes Page")
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

    private boolean validarclientes() {
        boolean clientecadastrado = false;
        if (sTipoPessoa == "J") {
            String CNPJ = cnpjcpf.getText().toString().replaceAll("[^0123456789]", "");
            Cursor CursorClie = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                    " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                    " CEP, CODCLIE_EXT, CODCLIE_INT, CODVENDEDOR, TIPOPESSOA, ATIVO, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + cnpjcpf.getText().toString().replaceAll("[^0123456789]", "") + "'", null);
            CursorClie.moveToFirst();
            try {
                if (CursorClie.getCount() > 0) {
                    nomeRazao = CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO"));
                    int codClieInt = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_INT"));
                    codClieExt = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_EXT"));
                    clientecadastrado = true;
                } else {
                }
                CursorClie.close();
            } catch (Exception e) {
                e.toString();
            }
        } else {
            String CPF = Edtcpf.getText().toString().replaceAll("[^0123456789]", "");
            Cursor CursorClie = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                    " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                    " CEP, CODCLIE_EXT, CODCLIE_INT, CODVENDEDOR, TIPOPESSOA, ATIVO, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + Edtcpf.getText().toString().replaceAll("[^0123456789]", "") + "'", null);
            CursorClie.moveToFirst();
            try {
                if (CursorClie.getCount() > 0) {
                    nomeRazao = CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO"));
                    int codClieInt = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_INT"));
                    codClieExt = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_EXT"));
                    clientecadastrado = true;
                } else {
                }
                CursorClie.close();
            } catch (Exception e) {
                e.toString();
            }
        }
        return clientecadastrado;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (sTipoPessoa.equals("F") && hasFocus == false && Edtcpf.getText().toString().length() > 0) {
            if (Util.validaCPF(Edtcpf.getText().toString().replaceAll("[^0123456789]", "")) == false) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage("CPF inválido. Verifique!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Edtcpf.requestFocus();
                                Edtcpf.selectAll();
                            }
                        });

                AlertDialog alert = alerta.create();
                alert.show();
                return;
            }
            if (validarclientes()) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage("CPF já cadastrado para o cliente " + nomeRazao + " código " + codClieExt + ". Verifique!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Edtcpf.requestFocus();
                                Edtcpf.selectAll();
                            }
                        });

                AlertDialog alert = alerta.create();
                alert.show();
                return;

            }

        } else if (sTipoPessoa.equals("J") && hasFocus == false && cnpjcpf.getText().toString().length() > 0) {
            if (Util.validaCNPJ(cnpjcpf.getText().toString().replaceAll("[^0123456789]", "")) == false) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage("CNPJ inválido. Verifique!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cnpjcpf.requestFocus();
                                cnpjcpf.selectAll();
                            }
                        });

                AlertDialog alert = alerta.create();
                alert.show();
                return;
            }
            if (validarclientes()) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage("CNPJ já cadastrado para o cliente " + nomeRazao + " código " + codClieExt + ". Verifique!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cnpjcpf.requestFocus();
                                cnpjcpf.selectAll();
                            }
                        });

                AlertDialog alert = alerta.create();
                alert.show();
                return;

            }
        }
    }
}
