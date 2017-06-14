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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import static com.jdsystem.br.vendasmobile.Login.NOME_USUARIO;


public class CadastroClientes extends AppCompatActivity implements Runnable, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static ProgressDialog DialogECB;
    public SharedPreferences prefs;
    String sTipoPessoa, sUF, codVendedor, NomeBairro, NomeCidade, usuario, senha, URLPrincipal, nomeRazao, TelaChamada, codEmpresa,
            chavepedido, numPedido, atuok, sCEP;
    Spinner spCidade, spTipoPessoa, spBairro, spUF;
    int CodCidade, CodCidadeInt, CodBairro, telaInvocada, idPerfil, flag, posicao, codClieExt, codClieInt;
    Boolean PesqCEP, atuBairro, atuCidade;
    ImageButton BtnPesqCep;
    ImageView imgdowncidade, imgdownbairro;
    EditText nomerazao, nomefan, nomecompleto, cnpjcpf, Edtcpf, EdtRG, ie, endereco, numero, cep, tel1, tel2, email, edtOBS, Complemento;
    SQLiteDatabase DB;
    private Handler handler = new Handler();
    private GoogleApiClient client;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_clientes);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }

        declaraobjetos();
        carregarpreferencias();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                TelaChamada = params.getString(getString(R.string.intent_telainvocada));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                telaInvocada = params.getInt(getString(R.string.intent_listaclie));
                codEmpresa = params.getString(getString(R.string.intent_codigoempresa));
                chavepedido = params.getString(getString(R.string.intent_chavepedido));
                numPedido = params.getString(getString(R.string.intent_numpedido));
                codClieInt = params.getInt(getString(R.string.intent_codcliente));

            }
        }
        if (savedInstanceState == null) {
            spUF.setOnItemSelectedListener(CadastroClientes.this);
            spCidade.setOnItemSelectedListener(CadastroClientes.this);
            spBairro.setOnItemSelectedListener(CadastroClientes.this);
        }

        PesqCEP = false;
        atuCidade = false;
        atuBairro = false;
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

        if (codClieInt != 0) {
            carregardadoscliente();
        }
    }

    private void carregardadoscliente() {
        try {
            Cursor cursoclie = DB.rawQuery("SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                    " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                    " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                    " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                    " WHERE CODCLIE_INT = " + codClieInt, null);
            cursoclie.moveToFirst();
            if (cursoclie.getCount() > 0) {
                String tipopessoa = cursoclie.getString(cursoclie.getColumnIndex("TIPOPESSOA"));
                if (tipopessoa.equals("F")) {
                    spTipoPessoa.setSelection(0);
                    nomerazao.setVisibility(EditText.GONE);
                    nomefan.setVisibility(EditText.GONE);
                    cnpjcpf.setVisibility(EditText.GONE);
                    ie.setVisibility(EditText.GONE);
                    Edtcpf.setVisibility(EditText.VISIBLE);
                    nomecompleto.setVisibility(EditText.VISIBLE);
                    nomecompleto.setText(cursoclie.getString(cursoclie.getColumnIndex("NOMERAZAO")));
                    EdtRG.setVisibility(EditText.VISIBLE);
                    EdtRG.setText(cursoclie.getString(cursoclie.getColumnIndex("REGIDENT")));
                    Edtcpf.setText(cursoclie.getString(cursoclie.getColumnIndex("CNPJ_CPF")));
                    email.setText(cursoclie.getString(cursoclie.getColumnIndex("EMAIL")));
                    cep.setText(cursoclie.getString(cursoclie.getColumnIndex("CEP")));
                    endereco.setText(cursoclie.getString(cursoclie.getColumnIndex("ENDERECO")));
                    numero.setText(cursoclie.getString(cursoclie.getColumnIndex("NUMERO")));
                    Complemento.setText(cursoclie.getString(cursoclie.getColumnIndex("COMPLEMENT")));
                    sUF = cursoclie.getString(cursoclie.getColumnIndex("UF"));
                    NomeCidade = cursoclie.getString(cursoclie.getColumnIndex("CIDADE"));
                    NomeBairro = cursoclie.getString(cursoclie.getColumnIndex("BAIRRO"));
                    carregaruf(sUF);
                    tel1.setText(cursoclie.getString(cursoclie.getColumnIndex("TEL1")));
                    tel2.setText(cursoclie.getString(cursoclie.getColumnIndex("TEL2")));
                    edtOBS.setText(cursoclie.getString(cursoclie.getColumnIndex("OBS")));
                } else {
                    spTipoPessoa.setSelection(1);
                    nomerazao.setVisibility(EditText.VISIBLE);
                    nomefan.setVisibility(EditText.VISIBLE);
                    cnpjcpf.setVisibility(EditText.VISIBLE);
                    ie.setVisibility(EditText.VISIBLE);
                    Edtcpf.setVisibility(EditText.GONE);
                    nomecompleto.setVisibility(EditText.GONE);
                    EdtRG.setVisibility(EditText.GONE);
                    nomerazao.setText(cursoclie.getString(cursoclie.getColumnIndex("NOMERAZAO")));
                    nomefan.setText(cursoclie.getString(cursoclie.getColumnIndex("NOMEFAN")));
                    ie.setText(cursoclie.getString(cursoclie.getColumnIndex("INSCREST")));
                    cnpjcpf.setText(cursoclie.getString(cursoclie.getColumnIndex("CNPJ_CPF")));
                    email.setText(cursoclie.getString(cursoclie.getColumnIndex("EMAIL")));
                    cep.setText(cursoclie.getString(cursoclie.getColumnIndex("CEP")));
                    endereco.setText(cursoclie.getString(cursoclie.getColumnIndex("ENDERECO")));
                    numero.setText(cursoclie.getString(cursoclie.getColumnIndex("NUMERO")));
                    Complemento.setText(cursoclie.getString(cursoclie.getColumnIndex("COMPLEMENT")));
                    sUF = cursoclie.getString(cursoclie.getColumnIndex("UF"));
                    NomeCidade = cursoclie.getString(cursoclie.getColumnIndex("CIDADE"));
                    NomeBairro = cursoclie.getString(cursoclie.getColumnIndex("BAIRRO"));
                    carregaruf(sUF);
                    tel1.setText(cursoclie.getString(cursoclie.getColumnIndex("TEL1")));
                    tel2.setText(cursoclie.getString(cursoclie.getColumnIndex("TEL2")));
                    edtOBS.setText(cursoclie.getString(cursoclie.getColumnIndex("OBS")));
                }
            } else {
                Toast.makeText(this, "Não foi possível localizar o cliente. Verifique e tente novamente", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.toString();

        }
    }

    private void carregaruf(String uf) {
        switch (uf) {
            case "0":
                sUF = "0";
                onItemSelected(spUF, null, 0, 0);
                break;
            case "AC":
                sUF = "AC"; //Acre
                onItemSelected(spUF, null, 1, 0);
                break;
            case "AL":
                sUF = "AL"; // Alagoas
                onItemSelected(spUF, null, 2, 0);
                break;
            case "AP":
                sUF = "AP"; //Amapá
                onItemSelected(spUF, null, 3, 0);
                break;
            case "AM":
                sUF = "AM";//Amazonas
                onItemSelected(spUF, null, 4, 0);
                break;
            case "BA":
                sUF = "BA";//Bahia
                onItemSelected(spUF, null, 5, 0);
                break;
            case "CE":
                sUF = "CE";//Ceará
                onItemSelected(spUF, null, 6, 0);
                break;
            case "DF":
                sUF = "DF";//Distrito Federal
                onItemSelected(spUF, null, 7, 0);
                break;
            case "ES":
                sUF = "ES";//Espírito Santo
                onItemSelected(spUF, null, 8, 0);
                break;
            case "GO":
                sUF = "GO";//Goiás
                onItemSelected(spUF, null, 9, 0);
                break;
            case "MA":
                sUF = "MA";//Maranhão
                onItemSelected(spUF, null, 10, 0);
                break;
            case "MT":
                sUF = "MT";//Mato Grosso
                onItemSelected(spUF, null, 11, 0);
                break;
            case "MS":
                sUF = "MS";//Mato Grosso do Sul
                onItemSelected(spUF, null, 12, 0);
                break;
            case "MG":
                sUF = "MG";//Minas Gerais
                onItemSelected(spUF, null, 13, 0);
                break;
            case "PA":
                sUF = "PA";//Pará
                onItemSelected(spUF, null, 14, 0);
                break;
            case "PB":
                sUF = "PB";//Paraíba
                onItemSelected(spUF, null, 15, 0);
                break;
            case "PR":
                sUF = "PR";//Paraná
                onItemSelected(spUF, null, 16, 0);
                break;
            case "PE":
                sUF = "PE";//Pernambuco
                onItemSelected(spUF, null, 17, 0);
                break;
            case "PI":
                sUF = "PI";//Piauí
                onItemSelected(spUF, null, 18, 0);
                break;
            case "RJ":
                sUF = "RJ";//Rio de Janeiro
                onItemSelected(spUF, null, 19, 0);
                break;
            case "RN":
                sUF = "RN"; //Rio Grande do Norte
                onItemSelected(spUF, null, 20, 0);
                break;
            case "RS":
                sUF = "RS";//Rio Grande do Sul
                onItemSelected(spUF, null, 21, 0);
                break;
            case "RO":
                sUF = "RO"; //Rondônia
                onItemSelected(spUF, null, 22, 0);
                break;
            case "RR":
                sUF = "RR"; //Roraima
                onItemSelected(spUF, null, 23, 0);
                break;
            case "SC":
                sUF = "SC";//Santa Catarina
                onItemSelected(spUF, null, 24, 0);
                break;
            case "SP":
                sUF = "SP";//São Paulo
                onItemSelected(spUF, null, 25, 0);
                break;
            case "SE":
                sUF = "SE";//Sergipe
                onItemSelected(spUF, null, 26, 0);
                break;
            case "TO":
                sUF = "TO";//Tocantins
                onItemSelected(spUF, null, 27, 0);
                break;
        }
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

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        DB = new ConfigDB(this).getReadableDatabase();
        imgdowncidade = (ImageView) findViewById(R.id.imgdowncidade);
        imgdownbairro = (ImageView) findViewById(R.id.imgdownbairro);
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

        cnpjcpf.addTextChangedListener(Mask.insert(Mask.CNPJ_MASK, cnpjcpf));

        Edtcpf.addTextChangedListener(Mask.insert(Mask.CPF_MASK, Edtcpf));

        cep.addTextChangedListener(Mask.insert(Mask.CEP_MASK, cep));

        tel1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, tel1));

        tel2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, tel2));

        Edtcpf.setOnFocusChangeListener(this);
        cnpjcpf.setOnFocusChangeListener(this);
        cep.setOnFocusChangeListener(this);
    }

    public boolean VerificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    public void buscacepclie(View view) {
        sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");
        if (sCEP.length() < 8) {
            cep.setError(getString(R.string.CEP_incomplete));
            cep.requestFocus();
            return;
        }
        DialogECB = new ProgressDialog(CadastroClientes.this);
        DialogECB.setTitle(getString(R.string.wait));
        DialogECB.setMessage(getString(R.string.searching_the_CEP_informed));
        DialogECB.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.show();
        flag = 3;

        Thread thread = new Thread(CadastroClientes.this);
        thread.start();

        //cadastraDadosCep(sCEP);
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
            if (ConexOK) {
                int i = 0;
                do {
                    if (i > 0) {
                        Thread.sleep(500);
                    }
                    try {
                        Envio.call("", envelope);
                    } catch (Exception e) {
                        DialogECB.dismiss();
                        e.toString();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CadastroClientes.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    try {
                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                        RetDadosEndereco = (String) envelope.getResponse();
                        System.out.println("Response :" + resultsRequestSOAP.toString());
                    } catch (Exception e) {
                        e.toString();

                    }
                    i = i + 1;
                } while (RetDadosEndereco == null && i <= 6);

            } else {
                DialogECB.dismiss();
                Toast.makeText(CadastroClientes.this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
        if (RetDadosEndereco == null) {
            DialogECB.dismiss();
            Toast.makeText(CadastroClientes.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
            endereco.setText("");
            return;
        } else if (RetDadosEndereco.equals(getString(R.string.zip_code_not_found))) {
            DialogECB.dismiss();
            Toast.makeText(CadastroClientes.this, R.string.CEP_not_found_database, Toast.LENGTH_LONG).show();
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
                    jumpTime += 1;


                    try {
                        sUF = c.getString("uf");
                        NomeCidade = c.getString("cidade");
                        CodCidade = c.getInt("id_cidade");
                        //if (codClieInt != 0) {
                        carregaruf(sUF);
                        //}

                        NomeCidade = NomeCidade.replaceAll("'", "");

                        //Carregar endereço
                        String end = c.getString("logradouro");
                        String tipoend = c.getString("tipo_logradouro");
                        endereco.setText(tipoend + " " + end);
                        numero.requestFocus();

                        //Cadastrar Cidades
                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorCidade.getCount() > 0) {
                            DB.execSQL(" UPDATE CIDADES SET UF = '" + sUF + "', DESCRICAO = '" + NomeCidade + "', CODCIDADE_EXT = " + CodCidade + "" +
                                    " WHERE DESCRICAO = '" + NomeCidade + "' AND UF = '" + sUF + "' AND CODPERFIL = " + idPerfil);
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidade = (cursor1.getInt(cursor1.getColumnIndex("CODCIDADE")));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODCIDADE_EXT, CODPERFIL)" +
                                    " VALUES('" + NomeCidade + "','" + sUF + "'," + CodCidade + ", " + idPerfil + ");");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidade = (cursor1.getInt(cursor1.getColumnIndex("CODCIDADE")));
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

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODCIDADE FROM BAIRROS WHERE CODCIDADE = " + CodCidade + " AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorBairro.getCount() > 0) {
                            CursorBairro.moveToFirst();
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = " + CodCidade + ", DESCRICAO = '" + NomeBairro + "'" +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + CodCidade + "' AND CODPERFIL = " + idPerfil);
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODCIDADE, CODBAIRRO_EXT, CODPERFIL)" +
                                    " VALUES('" + NomeBairro + "'," + CodCidade + "," + CodBairroExt + ", " + idPerfil + ");");
                        }
                        CursorBairro.close();

                        // Carrega o Estado
                        String ufconvert = Util.converteUf(sUF);
                        ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.uf));
                        arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        int pos = arrayAdapterUF.getPosition(ufconvert);
                        spUF.setSelection(pos);


                    } catch (Exception E) {
                        E.printStackTrace();
                    }


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
        if (codClieInt != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CadastroClientes.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Deseja Realmente cancelar as alterações no cadastro deste cliente?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(CadastroClientes.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
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

        }
        if (nomecompleto.getText().length() != 0 || Edtcpf.getText().length() != 0 || EdtRG.getText().length() != 0 || nomerazao.getText().length() != 0 ||
                nomefan.getText().length() != 0 || cnpjcpf.getText().length() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CadastroClientes.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage(R.string.cancel_customer_registration)
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(CadastroClientes.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
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
            Intent i = new Intent(CadastroClientes.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
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
                nomerazao.setError(getString(R.string.enter_corporate_name));
                nomerazao.requestFocus();
                return;
            } else if (nomefan.getText().length() == 0) {
                nomefan.setError(getString(R.string.enter_name_fantasia));
                nomefan.requestFocus();
                return;
            } else if (cnpjcpf.getText().length() == 0) {
                cnpjcpf.setError(getString(R.string.enter_CNPJ));
                cnpjcpf.requestFocus();
                return;
            }
            NomePessoa = nomerazao.getText().toString();
            NomeFantasia = nomefan.getText().toString();
            CpfCnpj = cnpjcpf.getText().toString().replaceAll("[^0123456789]", "");
        } else if (sTipoPessoa == "F") {
            if (nomecompleto.getText().length() == 0) {
                nomecompleto.setError(getString(R.string.enter_full_name));
                nomecompleto.requestFocus();
                return;
            } else if (Edtcpf.getText().length() == 0) {
                Edtcpf.setError(getString(R.string.enter_identity));
                Edtcpf.requestFocus();
                return;
            }
            NomePessoa = nomecompleto.getText().toString().trim();
            NomeFantasia = NomePessoa;
            CpfCnpj = Edtcpf.getText().toString().replaceAll("[^0123456789]", "");
        }
        if (endereco.getText().length() == 0) {
            endereco.setError(getString(R.string.enter_backstreet));
            endereco.requestFocus();
            return;
        } else if (numero.getText().length() == 0) {
            numero.setError(getString(R.string.enter_street_number));
            numero.requestFocus();
            return;
        } else if (cep.getText().length() == 0) {
            cep.setError(getString(R.string.enter_CEP));
            cep.requestFocus();
            return;
        }
        if (sUF.equals("Selecione um estado") || sUF.equals("0")) {

            Toast.makeText(this, "Informe o estado! Campo obrigatória", Toast.LENGTH_LONG).show();
            return;

        }
        CEP = cep.getText().toString().replaceAll("[.-]", "");

        Cursor CursorClieCons = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                " CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO, CODPERFIL, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + CpfCnpj + "' AND CODPERFIL = " + idPerfil + "", null);
        try {
            if (CursorClieCons.getCount() > 0) {
                CursorClieCons.moveToFirst();
                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + NomePessoa +
                        "', NOMEFAN = '" + NomeFantasia +
                        "', INSCREST = '" + ie.getText().toString() +
                        "', EMAIL = '" + email.getText().toString() +
                        "', TEL1 = '" + tel1.getText().toString() +
                        "', TEL2 = '" + tel2.getText().toString() +
                        "', ENDERECO = '" + endereco.getText().toString() +
                        "', NUMERO = '" + numero.getText().toString() +
                        "', COMPLEMENT = '" + Complemento.getText().toString() +
                        "', CODBAIRRO = '" + CodBairro +
                        "', OBS = '" + edtOBS.getText().toString() +
                        "', CODCIDADE = '" + CodCidadeInt +
                        "', UF = '" + sUF +
                        "', CEP = '" + CEP + "', " +
                        " TIPOPESSOA = '" + sTipoPessoa +
                        "', REGIDENT = '" + EdtRG.getText().toString() +
                        "', ATIVO = 'S'" +
                        ", CODVENDEDOR = " + codVendedor +
                        " WHERE CNPJ_CPF = '" + CpfCnpj + "'" +
                        " AND CODPERFIL = " + idPerfil + "");
            } else {
                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                        "CEP, CODVENDEDOR, CODPERFIL, TIPOPESSOA, ATIVO, BLOQUEIO, REGIDENT, FLAGINTEGRADO) VALUES(" +
                        "'" + CpfCnpj +
                        "', '" + NomePessoa +
                        "','" + NomeFantasia +
                        "', '" + ie.getText().toString() +
                        "', '" + email.getText().toString() +
                        "','" + tel1.getText().toString() +
                        "', '" + tel2.getText().toString() +
                        "', '" + endereco.getText().toString() +
                        "','" + numero.getText().toString() +
                        "', '" + Complemento.getText().toString() +
                        "'," + CodBairro +
                        ", '" + edtOBS.getText().toString() +
                        "', " + CodCidadeInt +
                        ", '" + sUF +
                        "','" + CEP +
                        "', " + codVendedor +
                        ",  " + idPerfil +
                        ", '" + sTipoPessoa +
                        "', '" + "S" +
                        "', '" + "01" +
                        "', '" + EdtRG.getText().toString() + "','"
                        + "1" + "');");
            }
            CursorClieCons.close();

            Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + CpfCnpj + "' AND CODPERFIL = " + idPerfil + "", null);
            cursor1.moveToFirst();
            final String CodCliente = cursor1.getString(cursor1.getColumnIndex("CODCLIE_INT"));
            cursor1.close();

            Toast.makeText(this, "Cliente salvo com sucesso!", Toast.LENGTH_SHORT).show();

            if (Util.checarConexaoCelular(this)) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadastroClientes.this);
                builder.setTitle(R.string.synchronization);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.synchronize_registered_customer)
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String sitclieenvio;
                                sitclieenvio = Sincronismo.sincronizaClientesEnvio(CodCliente, CadastroClientes.this, usuario, senha, null, null, null);
                                if (sitclieenvio.equals(getString(R.string.newcustomers_successfully))) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CadastroClientes.this, getString(R.string.syn_clients_successfully), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CadastroClientes.this, getString(R.string.customer_not_sent), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (String.valueOf(TelaChamada).equals("null")) {
                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                } else if (TelaChamada.equals("CadastroPedidos")) {

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadastroClientes.this);
                                    builder.setTitle(R.string.synchronization);
                                    builder.setIcon(R.drawable.logo_ico);
                                    builder.setMessage("Incluir este cliente no pedido?")
                                            .setCancelable(false)
                                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                                    Bundle params = new Bundle();
                                                    params.putString(getString(R.string.intent_usuario), usuario);
                                                    params.putString(getString(R.string.intent_senha), senha);
                                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                                    intent.putExtras(params);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                                    Bundle params = new Bundle();
                                                    params.putString(getString(R.string.intent_usuario), usuario);
                                                    params.putString(getString(R.string.intent_senha), senha);
                                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                                    intent.putExtras(params);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            });
                                    android.app.AlertDialog alert = builder.create();
                                    alert.show();

                                } else {
                                    Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                    params.putString(getString(R.string.intent_chavepedido), chavepedido);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                android.app.AlertDialog alert = builder.create();
                alert.show();
            } else {
                Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_telainvocada), TelaChamada);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString(getString(R.string.intent_chavepedido), chavepedido);
                params.putString(getString(R.string.intent_numpedido), numPedido);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                intent.putExtras(params);
                startActivity(intent);
                finish();
            }

        } catch (Exception E) {
            System.out.println("Error" + E);
        }
    }

    @Override
    public void run() {
        if (flag == 1) {
            try {
                Sincronismo.sincronizaCidade(sUF, CadastroClientes.this, DialogECB, handler);
            } catch (Exception e) {
                e.toString();
            }
            if (DialogECB != null && flag != 0) {
                DialogECB.dismiss();
                flag = 0;
                onItemSelected(spUF, null, posicao, 0);
            }
        } else if (flag == 2) {
            try {
                Sincronismo.sincronizaBairro(CodCidade, this, DialogECB, CodCidadeInt, handler);
                atuBairro = true;
            } catch (Exception e) {
                e.toString();
            }

            if (DialogECB != null && flag != 0) {
                DialogECB.dismiss();
                flag = 0;
                onItemSelected(spCidade, null, posicao, 0);
            }
        } else if (flag == 3) {
            String end = null;
            String tipoend = null;
            PesqCEP = true;

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "PesquisaCEP");
            soap.addProperty("aCEP", sCEP);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(soap);
            HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
            String RetDadosEndereco = null;


            try {
                Boolean ConexOK = Util.checarConexaoCelular(this);
                if (ConexOK) {
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
                                    Toast.makeText(CadastroClientes.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        try {
                            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                            RetDadosEndereco = (String) envelope.getResponse();
                            System.out.println("Response :" + resultsRequestSOAP.toString());
                        } catch (Exception e) {
                            e.toString();

                        }
                        i = i + 1;
                    } while (RetDadosEndereco == null && i <= 6);

                } else {
                    DialogECB.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CadastroClientes.this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
            if (RetDadosEndereco == null) {
                DialogECB.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CadastroClientes.this, R.string.failure_communicate, Toast.LENGTH_SHORT).show();
                    }
                });
                endereco.setText("");
                return;
            } else if (RetDadosEndereco.equals(getString(R.string.zip_code_not_found))) {
                DialogECB.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CadastroClientes.this, R.string.CEP_not_found_database, Toast.LENGTH_LONG).show();
                    }
                });
                endereco.setText("");
                return;
            }
            try {
                JSONObject jsonObj = new JSONObject(RetDadosEndereco);
                JSONArray JEndereco = jsonObj.getJSONArray("cep");
                int jumpTime = 0;
                final int totalProgressTime = JEndereco.length();
                DB = new ConfigDB(this).getReadableDatabase();

                while (jumpTime < totalProgressTime) {
                    JSONObject c = JEndereco.getJSONObject(jumpTime);
                    jumpTime += 1;
                    try {
                        sUF = c.getString("uf");
                        NomeCidade = c.getString("cidade");
                        NomeCidade = NomeCidade.replaceAll("'", "");
                        CodCidade = c.getInt("id_cidade");

                        //Carregar endereço
                        end = c.getString("logradouro");
                        tipoend = c.getString("tipo_logradouro");
                        final String finalTipoend = tipoend;
                        final String finalEnd = end;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                endereco.setText(finalTipoend + " " + finalEnd);
                                numero.requestFocus();
                            }
                        });


                        //Cadastrar Cidades
                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorCidade.getCount() > 0) {
                            DB.execSQL(" UPDATE CIDADES SET UF = '" + sUF + "', DESCRICAO = '" + NomeCidade + "', CODCIDADE_EXT = " + CodCidade + "" +
                                    " WHERE DESCRICAO = '" + NomeCidade + "' AND UF = '" + sUF + "' AND CODPERFIL = " + idPerfil);
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidade = (cursor1.getInt(cursor1.getColumnIndex("CODCIDADE")));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODCIDADE_EXT, CODPERFIL)" +
                                    " VALUES('" + NomeCidade + "','" + sUF + "'," + CodCidade + ", " + idPerfil + ");");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + sUF + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidade = (cursor1.getInt(cursor1.getColumnIndex("CODCIDADE")));
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

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODCIDADE FROM BAIRROS WHERE CODCIDADE = " + CodCidade + " AND DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorBairro.getCount() > 0) {
                            CursorBairro.moveToFirst();
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = " + CodCidade + ", DESCRICAO = '" + NomeBairro + "'" +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + CodCidade + "' AND CODPERFIL = " + idPerfil);
                        } else if (NomeBairro.equals("")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CadastroClientes.this, "Bairro não encontrado na base de dados. Verifique!", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODCIDADE, CODBAIRRO_EXT, CODPERFIL)" +
                                    " VALUES('" + NomeBairro + "'," + CodCidade + "," + CodBairroExt + ", " + idPerfil + ");");
                        }
                        CursorBairro.close();

                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                }
                // Carrega o Estado
                final String[] ufconvert = {null};
                final String estado = sUF;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ufconvert[0] = Util.converteUf(estado);
                        ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.uf));
                        arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        int pos = arrayAdapterUF.getPosition(ufconvert[0]);
                        spUF.setSelection(pos);
                        onItemSelected(spUF, null, pos, 0);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (DialogECB != null && flag != 0) {
                DialogECB.dismiss();
                flag = 0;
            }
        }
    }

    private boolean validarclientes() {
        boolean clientecadastrado = false;
        if (sTipoPessoa == "J") {
            String CNPJ = cnpjcpf.getText().toString().replaceAll("[^0123456789]", "");
            Cursor CursorClie = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                    " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                    " CEP, CODCLIE_EXT, CODCLIE_INT, CODVENDEDOR, TIPOPESSOA, ATIVO, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + cnpjcpf.getText().toString().replaceAll("[^0123456789]", "") + "'AND CODPERFIL = " + idPerfil + "", null);
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
                    " CEP, CODCLIE_EXT, CODCLIE_INT, CODVENDEDOR, TIPOPESSOA, ATIVO, REGIDENT FROM CLIENTES WHERE CNPJ_CPF = '" + Edtcpf.getText().toString().replaceAll("[^0123456789]", "") + "'AND CODPERFIL = " + idPerfil + "", null);
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
        if (sTipoPessoa.equals("F") && !hasFocus && Edtcpf.getText().toString().length() > 0 && v == Edtcpf) {
            if (!Util.validaCPF(Edtcpf.getText().toString().replaceAll("[^0123456789]", ""))) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage(R.string.invalid_CPF)
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

            }

        } else if (sTipoPessoa.equals("J") && !hasFocus && cnpjcpf.getText().toString().length() > 0 && v == cnpjcpf) {
            if (!Util.validaCNPJ(cnpjcpf.getText().toString().replaceAll("[^0123456789]", ""))) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.app_namesair);
                alerta.setIcon(R.drawable.logo_ico);
                alerta.setMessage(R.string.invalid_CNPJ)
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

            }
        } else if (cep.getText().length() == 0 && !hasFocus && v == cep) {
            atualizaspinner();
            //spCidade.setAdapter(null);
            //spBairro.setAdapter(null);
            onItemSelected(spUF, null, 0, 0);
            PesqCEP = false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spnUF) {

            switch (position) {
                case 0:
                    sUF = "0";
                    break;
                case 1:
                    sUF = "AC"; //Acre
                    posicao = position;
                    break;
                case 2:
                    sUF = "AL"; // Alagoas
                    posicao = position;
                    break;
                case 3:
                    sUF = "AP"; //Amapá
                    posicao = position;
                    break;
                case 4:
                    sUF = "AM";//Amazonas
                    posicao = position;
                    break;
                case 5:
                    sUF = "BA";//Bahia
                    posicao = position;
                    break;
                case 6:
                    sUF = "CE";//Ceará
                    posicao = position;
                    break;
                case 7:
                    sUF = "DF";//Distrito Federal
                    posicao = position;
                    break;
                case 8:
                    sUF = "ES";//Espírito Santo
                    posicao = position;
                    break;
                case 9:
                    sUF = "GO";//Goiás
                    posicao = position;
                    break;
                case 10:
                    sUF = "MA";//Maranhão
                    posicao = position;
                    break;
                case 11:
                    sUF = "MT";//Mato Grosso
                    posicao = position;
                    break;
                case 12:
                    sUF = "MS";//Mato Grosso do Sul
                    posicao = position;
                    break;
                case 13:
                    sUF = "MG";//Minas Gerais
                    posicao = position;
                    break;
                case 14:
                    sUF = "PA";//Pará
                    posicao = position;
                    break;
                case 15:
                    sUF = "PB";//Paraíba
                    posicao = position;
                    break;
                case 16:
                    sUF = "PR";//Paraná
                    posicao = position;
                    break;
                case 17:
                    sUF = "PE";//Pernambuco
                    posicao = position;
                    break;
                case 18:
                    sUF = "PI";//Piauí
                    posicao = position;
                    break;
                case 19:
                    sUF = "RJ";//Rio de Janeiro
                    posicao = position;
                    break;
                case 20:
                    sUF = "RN"; //Rio Grande do Norte
                    posicao = position;
                    break;
                case 21:
                    sUF = "RS";//Rio Grande do Sul
                    posicao = position;
                    break;
                case 22:
                    sUF = "RO"; //Rondônia
                    posicao = position;
                    break;
                case 23:
                    sUF = "RR"; //Roraima
                    posicao = position;
                    break;
                case 24:
                    sUF = "SC";//Santa Catarina
                    posicao = position;
                    break;
                case 25:
                    sUF = "SP";//São Paulo
                    posicao = position;
                    break;
                case 26:
                    sUF = "SE";//Sergipe
                    posicao = position;
                    break;
                case 27:
                    sUF = "TO";//Tocantins
                    posicao = position;
                    break;
            }

            int CodCidade = 0;
            Cursor cursor = null;
            try {
                if (codClieInt == 0) {
                    cursor = DB.rawQuery(" SELECT CODCIDADE_EXT, DESCRICAO FROM CIDADES WHERE UF = '" + sUF + "' AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                } else {
                    cursor = DB.rawQuery(" SELECT CODCIDADE_EXT, DESCRICAO FROM CIDADES WHERE UF = '" + sUF + "' AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                }
                final List<String> DadosList = new ArrayList<String>();
                DadosList.clear();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String Cidade = cursor.getString(cursor.getColumnIndex("DESCRICAO"));
                        DadosList.add(Cidade);
                    } while (cursor.moveToNext());
                    cursor.close();

                    final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    if (codClieInt != 0) {
                        spUF.setSelection(position);
                    }

                    if (codClieInt != 0 || PesqCEP.equals(true)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spCidade.setAdapter(spinnerArrayAdapter);
                                int pos = spinnerArrayAdapter.getPosition(NomeCidade);
                                spCidade.setSelection(pos);
                            }
                        });
                        return;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spCidade.setAdapter(spinnerArrayAdapter);
                            }
                        });
                        return;
                    }
                } else {
                    CodCidadeInt = 0;
                    DadosList.clear();
                    DadosList.add("Selecione a Cidade");
                    final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spCidade.setAdapter(spinnerArrayAdapter);
                }
                if (position == 0) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CodCidadeInt = 0;
                                DadosList.clear();
                                DadosList.add("Selecione a Cidade");
                                final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                                spCidade.setAdapter(spinnerArrayAdapter);
                                //spCidade.setAdapter(null);
                                spBairro.setAdapter(null);
                            }
                        });

                    } catch (Exception e) {
                        e.toString();

                    }
                } /*else {
                    spCidade.setAdapter(null);
                    spBairro.setAdapter(null);
                }*/
            } catch (Exception E) {
                System.out.println("Error" + E);
            }
            Thread thread = new Thread(CadastroClientes.this);
            thread.start();

        } else if (parent.getId() == R.id.spnCidade) {
            if (atuBairro) {
                try {
                    Cursor CurCidade = DB.rawQuery(" SELECT CODCIDADE_EXT, CODCIDADE FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "' AND UF = '" + sUF + "' AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                    if (CurCidade.getCount() > 0) {
                        CurCidade.moveToFirst();
                        CodCidade = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE_EXT"));
                        CodCidadeInt = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE"));
                    }

                    CurCidade.close();
                } catch (Exception e) {
                    e.toString();
                }
                Cursor CurBairro = null;
                try {
                    CurBairro = DB.rawQuery(" SELECT DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidadeInt + " AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                } catch (Exception e) {
                    e.toString();
                }
                final List<String> DadosListBairro = new ArrayList<String>();
                DadosListBairro.clear();
                if (CurBairro.getCount() > 0) {
                    CurBairro.moveToFirst();
                    do {
                        String Bairro = CurBairro.getString(CurBairro.getColumnIndex("DESCRICAO"));
                        DadosListBairro.add(Bairro);
                    } while (CurBairro.moveToNext());
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DadosListBairro.clear();
                            DadosListBairro.add("Selecione o Bairro");
                            new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                            spBairro.setAdapter(new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro));
                        }
                    });
                }
                CurBairro.close();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        spBairro.setAdapter(new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro));
                    }
                });

                if (PesqCEP) {
                    int pos = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).getPosition(NomeBairro);
                    spBairro.setSelection(pos);
                }
                atuBairro = false;
            } else {

                if (!PesqCEP) {
                    NomeCidade = spCidade.getSelectedItem().toString();
                }

                try {
                    Cursor CurCidade = DB.rawQuery(" SELECT CODCIDADE_EXT, CODCIDADE FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "' AND UF = '" + sUF + "' AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                    if (CurCidade.getCount() > 0) {
                        CurCidade.moveToFirst();
                        CodCidade = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE_EXT"));
                        CodCidadeInt = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE"));
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                List<String> DadosListBairro = new ArrayList<String>();
                                DadosListBairro.clear();
                                DadosListBairro.add("Selecione o Bairro");
                                new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                                spBairro.setAdapter(new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro));
                            }
                        });
                    }

                    CurCidade.close();
                } catch (Exception e) {
                    e.toString();
                }
                Cursor CurBairro = null;
                try {
                    CurBairro = DB.rawQuery(" SELECT DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidadeInt + " AND CODPERFIL = " + idPerfil + " ORDER BY DESCRICAO", null);
                } catch (Exception e) {
                    e.toString();
                }
                final List<String> DadosListBairro = new ArrayList<String>();
                DadosListBairro.clear();
                if (CurBairro.getCount() > 0) {
                    CurBairro.moveToFirst();
                    do {
                        String Bairro = CurBairro.getString(CurBairro.getColumnIndex("DESCRICAO"));
                        DadosListBairro.add(Bairro);
                    } while (CurBairro.moveToNext());
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DadosListBairro.clear();
                            DadosListBairro.add("Selecione o Bairro");
                            new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                            spBairro.setAdapter(new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro));
                        }
                    });
                }
                CurBairro.close();

                new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                spBairro.setAdapter(new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro));
                if (PesqCEP) {
                    int pos = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro).getPosition(NomeBairro);
                    spBairro.setSelection(pos);
                }
            }

        } else if (parent.getId() == R.id.spnBairro) {
            if (!PesqCEP) {
                NomeBairro = spBairro.getSelectedItem().toString();
            }

            try {
                Cursor CurBai = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE DESCRICAO = '" + NomeBairro + "' AND CODPERFIL = " + idPerfil, null);
                if (CurBai.getCount() > 0) {
                    CurBai.moveToFirst();
                    CodBairro = CurBai.getInt(CurBai.getColumnIndex("CODBAIRRO"));
                }
                CurBai.close();
                PesqCEP = false;
            } catch (Exception E) {
                System.out.println("Error" + E);
            }
        }
    }

    public void atualizacidades(View v) {
        boolean conexOK = Util.checarConexaoCelular(CadastroClientes.this);
        String UF = Util.converteUf(sUF);
        if (conexOK) {
            DialogECB = new ProgressDialog(CadastroClientes.this);
            DialogECB.setCancelable(false);
            DialogECB.setProgress(0);
            DialogECB.setMax(0);
            DialogECB.setIcon(R.drawable.icon_sync);
            DialogECB.setTitle(getString(R.string.wait));
            DialogECB.setMessage("Atualizando cidades de " + UF + "... Esse processo pode demorar alguns instantes caso seja a primeira consulta" +
                    " a esse estado.");
            DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            DialogECB.show();
            flag = 1;

        } else {
            Toast.makeText(this, "Sem Conexão com a internet. Verifique!", Toast.LENGTH_SHORT).show();
            return;
        }
        Thread thread = new Thread(CadastroClientes.this);
        thread.start();
    }

    public void atualizabairros(View v) {
        boolean conexOK = Util.checarConexaoCelular(CadastroClientes.this);
        if (conexOK) {
            DialogECB = new ProgressDialog(CadastroClientes.this);
            DialogECB.setCancelable(false);
            DialogECB.setProgress(0);
            DialogECB.setMax(0);
            DialogECB.setIcon(R.drawable.icon_sync);
            DialogECB.setTitle(getString(R.string.wait));
            DialogECB.setMessage("Atualizando bairros da cidade de " + NomeCidade + "... Esse processo pode demorar alguns instantes caso seja a primeira consulta" +
                    " a esse estado.");
            DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            DialogECB.show();
            flag = 2;

        } else {
            Toast.makeText(this, "Sem Conexão com a internet. Verifique!", Toast.LENGTH_SHORT).show();
            return;
        }
        Thread thread = new Thread(CadastroClientes.this);
        thread.start();

    }

    private String atualizaspinner() {
        atuok = "S";
        try {
            ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.uf));
            arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
            spUF.setAdapter(arrayAdapterUF);
        } catch (Exception e) {
            e.toString();
            atuok = "N";
            return atuok;
        }
        return atuok;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("CadastroClientes Page")
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("tipocliente", spTipoPessoa.getSelectedItemPosition());
        outState.putInt("estadoUF", spUF.getSelectedItemPosition());
        outState.putString("cidade", spCidade.getSelectedItem().toString());
        //outState.putInt("cidade",spCidade.getSelectedItemPosition());
        outState.putString("bairro", spBairro.getSelectedItem().toString());
        //outState.putInt("bairro",spBairro.getSelectedItemPosition());
        outState.putString("razasocial", nomerazao.getText().toString());
        outState.putString("nomefan", nomefan.getText().toString());
        outState.putString("nomecompleto", nomecompleto.getText().toString());
        outState.putString("cnpj", cnpjcpf.getText().toString());
        outState.putString("cpf", Edtcpf.getText().toString());
        outState.putString("email", email.getText().toString());
        outState.putString("ie", ie.getText().toString());
        outState.putString("cep", cep.getText().toString());
        outState.putString("endereco", endereco.getText().toString());
        outState.putString("numero", numero.getText().toString());
        outState.putString("complemento", Complemento.getText().toString());
        outState.putString("tel1", tel1.getText().toString());
        outState.putString("tel2", tel2.getText().toString());
        outState.putString("rg", EdtRG.getText().toString());
        outState.putString("obs", edtOBS.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            spTipoPessoa.setSelection(savedInstanceState.getInt("tipocliente"));
            PesqCEP = true;
            NomeCidade = savedInstanceState.getString("cidade");
            NomeBairro = savedInstanceState.getString("bairro");
            spUF.setSelection(savedInstanceState.getInt("estadoUF"));
            onItemSelected(spUF,null,savedInstanceState.getInt("estadoUF"),0);
            onItemSelected(spCidade, null, 0, 0);
            nomerazao.setText(savedInstanceState.getString("razasocial"));
            nomefan.setText(savedInstanceState.getString("nomefan"));
            nomecompleto.setText(savedInstanceState.getString("nomecompleto"));
            cnpjcpf.setText(savedInstanceState.getString("cnpj"));
            Edtcpf.setText(savedInstanceState.getString("cpf"));
            email.setText(savedInstanceState.getString("email"));
            ie.setText(savedInstanceState.getString("ie"));
            cep.setText(savedInstanceState.getString("cep"));
            endereco.setText(savedInstanceState.getString("endereco"));
            numero.setText(savedInstanceState.getString("numero"));
            Complemento.setText(savedInstanceState.getString("complemento"));
            try {
                tel1.setText(savedInstanceState.getString("tel1"));
            } catch (Exception e) {
                e.toString();
            }
            try {
                tel2.setText(savedInstanceState.getString("tel2"));
            } catch (Exception e) {
                e.toString();
            }
            edtOBS.setText(savedInstanceState.getString("obs"));
            EdtRG.setText(savedInstanceState.getString("rg"));

        }
        //super.onRestoreInstanceState(savedInstanceState);
    }
}
