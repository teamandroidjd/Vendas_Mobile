package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.domain.Contatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class CadastroContatos extends AppCompatActivity implements Runnable {
    String sCodVend, URLPrincipal, usuario, senha, sUF, sTipoContato, NomeBairro, NomeCidade, NomeCliente, descBairro;
    Boolean PesqCEP;
    CheckBox cbSegunda, cbTerca, cbQuarta, cbQuinta, cbSexta, cbSabado, cbDomingo;
    TimePicker timePicker;
    ImageButton BtnPesqCep;
    int CodCidade, CodBairro, CodCliente, hour, minute;
    EditText nome, setor, data, documento, endereco, numero, cep, tel1, tel2, email, OBS, Complemento;
    Spinner TipoContato, TipoCargoEspec, spCidade, spBairro, spUF;
    Context ctx;
    LinearLayout linearcheck1, linearcheck2, lineartxtsemana, linearrazao;
    TextView razaosocial;
    //private CheckBox domingo, segunda, terca, quarta, quinta, sexta;
    private Handler handler = new Handler();
    SQLiteDatabase DB;
    private static ProgressDialog DialogECB;
    private GoogleApiClient client;
    private TimePicker timerPicker1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_contatos);

        declaraobjetos();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
                //sTipoContato = params.getString("C");
                //            }
            }
        }

        PesqCEP = false;
        NomeBairro = null;
        NomeCidade = null;

        TipoContato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sTipoContato = "Selecione o tipo de contato";
                        break;
                    case 1:
                        sTipoContato = "C";
                        break;
                    case 2:
                        sTipoContato = "O";
                        break;
                }
                if (sTipoContato == "O") {
                    CodCliente = 0;
                    linearrazao.setVisibility(View.GONE);
                    linearcheck1.setVisibility(EditText.VISIBLE);
                    linearcheck2.setVisibility(EditText.VISIBLE);
                    lineartxtsemana.setVisibility(EditText.VISIBLE);
                } else if (sTipoContato == "C" && CodCliente == 0) {
                    linearcheck1.setVisibility(EditText.VISIBLE);
                    linearcheck2.setVisibility(EditText.VISIBLE);
                    lineartxtsemana.setVisibility(EditText.GONE);
                    Intent i = new Intent(CadastroContatos.this, ConsultaClientes.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putInt(getString(R.string.intent_cad_contato), 1);
                    i.putExtras(params);
                    startActivity(i);
                } else if (CodCliente != 0) {
                    TipoContato.setSelection(1);
                    linearrazao.setVisibility(View.VISIBLE);
                    razaosocial.setText(NomeCliente);
                    linearcheck1.setVisibility(View.VISIBLE);
                    linearcheck2.setVisibility(View.VISIBLE);
                    lineartxtsemana.setVisibility(View.GONE);
                } else {
                    linearrazao.setVisibility(View.GONE);
                    linearcheck1.setVisibility(View.VISIBLE);
                    linearcheck2.setVisibility(View.VISIBLE);
                    lineartxtsemana.setVisibility(View.GONE);
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
                Boolean ConexOk = Util.checarConexaoCelular(CadastroContatos.this);
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

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                            ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                            spCidade.setAdapter(spinnerArrayAdapter);
                        }
                    } catch (Exception E) {
                        System.out.println("Error" + E);
                    }
                }
                Thread thread = new Thread(CadastroContatos.this);
                thread.start();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Boolean ConexOk = Util.checarConexaoCelular(CadastroContatos.this);
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

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
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

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
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
                        descBairro = CurBai.getString(CurBai.getColumnIndex("DESCRICAO"));

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
        DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        BtnPesqCep = (ImageButton) findViewById(R.id.btnBuscaCep);
        TipoContato = (Spinner) findViewById(R.id.spnTipoContato);
        TipoCargoEspec = (Spinner) findViewById(R.id.spnCargoEspec);
        spUF = (Spinner) findViewById(R.id.spnUF);
        spCidade = (Spinner) findViewById(R.id.spnCidade);
        spBairro = (Spinner) findViewById(R.id.spnBairro);
        linearcheck1 = (LinearLayout) findViewById(R.id.lnrcheckbox);
        linearcheck2 = (LinearLayout) findViewById(R.id.lnrcheckbox2);
        lineartxtsemana = (LinearLayout) findViewById(R.id.lnrtxtdiasemana);
        linearrazao = (LinearLayout) findViewById(R.id.linearrazaosocial);
        nome = (EditText) findViewById(R.id.EdtNomeCompleto);
        documento = (EditText) findViewById(R.id.EdtDocumento);
        setor = (EditText) findViewById(R.id.EdtSetor);
        email = (EditText) findViewById(R.id.EdtEmail);
        cep = (EditText) findViewById(R.id.EdtCep);
        endereco = (EditText) findViewById(R.id.EdtEndereco);
        numero = (EditText) findViewById(R.id.EdtNumero);
        Complemento = (EditText) findViewById(R.id.EdtComple);
        tel1 = (EditText) findViewById(R.id.EdtTel1);
        tel2 = (EditText) findViewById(R.id.EdtTel2);
        OBS = (EditText) findViewById(R.id.EdtOBS);
        data = (EditText) findViewById(R.id.EdtData);
        razaosocial = (TextView) findViewById(R.id.txtrazaocontato);

        final EditText etCEP = (EditText) findViewById(R.id.EdtCep);
        etCEP.addTextChangedListener(Mask.insert(Mask.CEP_MASK, etCEP));

        EditText etTelefone1 = (EditText) findViewById(R.id.EdtTel1);
        etTelefone1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone1));

        EditText etTelefone2 = (EditText) findViewById(R.id.EdtTel2);
        etTelefone2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone2));

        EditText dtnasc = (EditText) findViewById(R.id.EdtData);
        dtnasc.addTextChangedListener(Mask.insert(Mask.DATA_MASK, dtnasc));
    }

    public void btnsalvarcontato(View view) {

        DB = new ConfigDB(this).getReadableDatabase();

        if (sTipoContato == "Selecione o tipo de contato") {
            Toast.makeText(this, "Informe o tipo de contato!", Toast.LENGTH_SHORT).show();
            return;
        } else if (sTipoContato == "C" && CodCliente == 0) {
            Toast.makeText(this, "Informe o cliente para cadastrar esse contato!", Toast.LENGTH_SHORT).show();
            return;
        } else if (nome.getText().length() == 0) {
            nome.setError("Digite o nome do contato!");
            nome.requestFocus();
            return;
        } else if (data.getText().length() > 0 && data.getText().length() < 10) {
            data.setError("Data inválida. Verifique!");
            data.requestFocus();
            return;

        } else if (cep.getText().length() > 0 && cep.getText().length() < 9) {
            cep.setError("CEP incompleto. Verifique!");
            cep.requestFocus();
            return;

        } else if (tel1.getText().length() > 0 && tel1.getText().length() < 14) {
            tel1.setError("Telefone inválido.Verifique!");
            tel1.requestFocus();
            return;
        } else if (tel2.getText().length() > 0 && tel2.getText().length() < 14) {
            tel2.setError("Telefone inválido.Verifique!");
            tel2.requestFocus();
            return;
        }

        /*Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CODCLIE_INT = '" + CodCliente + "'", null);
        cursor1.moveToFirst();
        cursor1.close();*/


        try {

            //Cursor CursorContatos = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " + CodCliente + " AND NOME = '" + nome.getText().toString() + "'", null);

            //if (!(CursorContatos.getCount() > 0)) {
            DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, " +
                    "COMPLEMENTO, UF, CODVENDEDOR, BAIRRO, DESC_CIDADE, CODCLIENTE, TIPO, OBS) VALUES(" +
                    "'" + nome.getText().toString() + "', '" + setor.getText().toString() + "', '" +
                    email.getText().toString() + "', '" + tel1.getText().toString() + "', '" + tel2.getText().toString() +
                    "', '" + documento.getText().toString() + "', '" + data.getText().toString() + "','" +
                    cep.getText().toString() +
                    "', '" + endereco.getText().toString() + "', '" + numero.getText().toString() + "', '" +
                    Complemento.getText().toString() + "', '" + sUF + "', " + sCodVend + ", '" + descBairro + "', '" +
                    NomeCidade + "', " + CodCliente +", '" + sTipoContato + "', '" + OBS.getText().toString() + "');");

                    //Estado = cursor1.getString(CursosEstado.getColumnIndex("UF"));
            //}
            //CursorContatos.close();
        } catch (Exception E) {
            E.toString();
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadastroContatos.this);
        builder.setTitle("Novo Contato");
        builder.setIcon(R.drawable.logo_ico);
        builder.setMessage("Deseja cadastrar outro contato para este cliente?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getBaseContext(), CadastroContatos.class);
                        Bundle params = new Bundle();
                        params.putString("codvendedor", sCodVend);
                        params.putString("usuario", usuario);
                        params.putString("senha", senha);
                        params.putInt("codCliente", CodCliente);
                        params.putString("nomerazao", NomeCliente);
                        params.putString("urlPrincipal", URLPrincipal);
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getBaseContext(), ConsultaPedidos.class);
                        Bundle params = new Bundle();
                        params.putString("codvendedor", sCodVend);
                        params.putString("usuario", usuario);
                        params.putString("senha", senha);
                        params.putString("urlPrincipal", URLPrincipal);
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();


        Toast.makeText(this, "Contato Salvo com sucesso!", Toast.LENGTH_SHORT).show();
    }

    public boolean cadastraDadosCep(String cep) {
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
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetDadosEndereco = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            } else {
                DialogECB.cancel();
                Toast.makeText(this, "Sem conexão com a internet! Verifique.", Toast.LENGTH_LONG).show();
                PesqCEP = false;
                return PesqCEP;
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        if (RetDadosEndereco.equals("CEP não Encontrado")) {
            DialogECB.dismiss();
            Toast.makeText(CadastroContatos.this, "CEP não encontrado na base de dados. Verifique se está correto e tente novamente.", Toast.LENGTH_LONG).show();
            endereco.setText("");
            return PesqCEP;
        }

        try {
            JSONObject jsonObj = new JSONObject(RetDadosEndereco);
            JSONArray JEndereco = jsonObj.getJSONArray("cep");

            int jumpTime = 0;
            DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JEndereco.length();
            DialogECB.setMax(totalProgressTime);
            DB = new ConfigDB(this).getReadableDatabase();

            for (int i = 0; i < JEndereco.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    JSONObject c = JEndereco.getJSONObject(jumpTime);
                    try {
                        jumpTime += 1;
                        DialogECB.setProgress(jumpTime);
                        DialogECB.setMessage("Sincronizando Tabelas - Estados");
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
                        // TODO Auto-generated catch block
                        E.printStackTrace();
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
                        // TODO Auto-generated catch block
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
                        // TODO Auto-generated catch block
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
                    ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEstado);
                    arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spUF.setAdapter(arrayAdapterUF);

                    //Cidade
                    List<String> DadosListCidade = new ArrayList<String>();
                    DadosListCidade.add(NomeCidade);
                    ArrayAdapter<String> arrayAdapterCidade = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListCidade);
                    ArrayAdapter<String> spinnerArrayAdapterCidade = arrayAdapterCidade;
                    spinnerArrayAdapterCidade.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spCidade.setAdapter(spinnerArrayAdapterCidade);

                    //Bairro
                    List<String> DadosListBairroUnic = new ArrayList<String>();
                    DadosListBairroUnic.add(NomeBairro);
                    ArrayAdapter<String> arrayAdapterBairroUnic = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairroUnic);
                    ArrayAdapter<String> spinnerArrayAdapterBairroUnic = arrayAdapterBairroUnic;
                    spinnerArrayAdapterBairroUnic.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    spBairro.setAdapter(spinnerArrayAdapterBairroUnic);

                }
            }
            PesqCEP = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

        return PesqCEP;
    }

    public void buscacep(View view) {
        String sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");

        if (sCEP.length() < 8) {
            cep.setError("CEP incompleto. Verifique!");
            cep.requestFocus();
            return;
        }
        DialogECB = new ProgressDialog(CadastroContatos.this);
        DialogECB.setTitle("Aguarde.");
        DialogECB.setMessage("Pesquisando o CEP informado...");
        DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.show();

        cadastraDadosCep(sCEP);
    }

    @Override
    public void onBackPressed() {
        Intent cadcont = new Intent(CadastroContatos.this, ConsultaContatos.class);
        Bundle params = new Bundle();
        params.putString("codvendedor", sCodVend);
        params.putString("urlPrincipal", URLPrincipal);
        params.putString("usuario", usuario);
        params.putString("senha", senha);
        cadcont.putExtras(params);
        startActivity(cadcont);
        finish();
        super.onBackPressed();
    }

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

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadastroClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
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

    public void onCheckedboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.cb_domingo:
                if (checked) {
                    ImageButton imageButton = (ImageButton) findViewById(R.id.add_horario_domingo);
                    imageButton.setVisibility(View.VISIBLE);
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.editTextGroupLayout);

                            EditText editTextView = new EditText(CadastroContatos.this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                            editTextView.setLayoutParams(params);

                            linearLayout.addView(editTextView);
                            editTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new TimePickerDialog(CadastroContatos.this, timePickerListener, hour, minute, false);

                                    /*EditText pickerTimeView = new EditText(CadContatos.this);

                                    TimePicker.LayoutParams params = new TimePicker.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                            TimePicker.LayoutParams.WRAP_CONTENT, 1);

                                    pickerTimeView.setLayoutParams(params);

                                    linearLayout.addView(pickerTimeView);*/


                                }
                            });
                            /*TimePicker pickerTimeView = new TimePicker(CadContatos.this);

                           TimePicker.LayoutParams params = new TimePicker.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                   TimePicker.LayoutParams.WRAP_CONTENT, 1);

                            pickerTimeView.setLayoutParams(params);

                            linearLayout.addView(pickerTimeView);*/

                        }
                    });
                    ImageView imgView = (ImageView) findViewById(R.id.imageView2);
                    imgView.setVisibility(View.VISIBLE);
                } else {
                    ImageButton imageButton = (ImageButton) findViewById(R.id.add_horario_domingo);
                    imageButton.setVisibility(View.GONE);
                    ImageView imgView = (ImageView) findViewById(R.id.imageView2);
                    imgView.setVisibility(View.GONE);
                }
                break;

        }
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            hour = hourOfDay;
            minute = minuteOfHour;
        }
    };

}
