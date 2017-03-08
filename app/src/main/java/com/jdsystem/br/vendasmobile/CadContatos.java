package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class CadContatos extends AppCompatActivity {
    String sCodVend, URLPrincipal, usuario, senha, sUF, sTipoContato, NomeBairro, NomeCidade, NomeCliente;
    Boolean PesqCEP;
    int CodCidade, CodBairro, CodCliente;
    EditText nome, setor, data, documento, endereco, numero, cep, tel1, tel2, email, OBS, Complemento;
    Spinner TipoContato, TipoCargoEspec, UF, SpnCidade, Bairro;
    LinearLayout linearcheck1, linearcheck2, lineartxtsemana, linearrazao;
    TextView razaosocial;
    private CheckBox domingo, segunda, terca, quarta, quinta, sexta;
    SQLiteDatabase DB;
    private static ProgressDialog DialogECB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_contatos);

        TipoContato = (Spinner) findViewById(R.id.spnTipoContato);
        TipoCargoEspec = (Spinner) findViewById(R.id.spnCargoEspec);
        UF = (Spinner) findViewById(R.id.spnUF);
        SpnCidade = (Spinner) findViewById(R.id.spnCidade);
        Bairro = (Spinner) findViewById(R.id.spnBairro);
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


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                usuario = params.getString("usuario");
                senha = params.getString("senha");
                CodCliente = params.getInt("codCliente");
                NomeCliente = params.getString("nomerazao");
                //sTipoContato = params.getString("C");


            }
        }
        ImageButton BtnPesqCep = (ImageButton) findViewById(R.id.btnBuscaCep);
        BtnPesqCep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");
                cadastraDadosCep(sCEP);
            }
        });
        final EditText etCEP = (EditText) findViewById(R.id.EdtCep);
        etCEP.addTextChangedListener(Mask.insert(Mask.CEP_MASK, etCEP));

        EditText etTelefone1 = (EditText) findViewById(R.id.EdtTel1);
        etTelefone1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone1));

        EditText etTelefone2 = (EditText) findViewById(R.id.EdtTel2);
        etTelefone2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone2));

        EditText dtnasc = (EditText) findViewById(R.id.EdtData);
        dtnasc.addTextChangedListener(Mask.insert(Mask.DATA_MASK, dtnasc));

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
                    linearcheck1.setVisibility(EditText.VISIBLE);
                    linearcheck2.setVisibility(EditText.VISIBLE);
                    lineartxtsemana.setVisibility(EditText.VISIBLE);


                } else if (sTipoContato == "C" && CodCliente == 0) {
                    linearcheck1.setVisibility(EditText.GONE);
                    linearcheck2.setVisibility(EditText.GONE);
                    lineartxtsemana.setVisibility(EditText.GONE);
                    Intent i = new Intent(CadContatos.this, act_ListClientes.class);
                    Bundle params = new Bundle();
                    params.putString("codvendedor", sCodVend);
                    params.putString("usuario", usuario);
                    params.putString("senha", senha);
                    params.putInt("cadcont", 1);
                    i.putExtras(params);
                    startActivity(i);

                } else if (CodCliente != 0) {
                    TipoContato.setSelection(1);
                    linearrazao.setVisibility(View.VISIBLE);
                    razaosocial.setText(NomeCliente);
                    linearcheck1.setVisibility(View.GONE);
                    linearcheck2.setVisibility(View.GONE);
                    lineartxtsemana.setVisibility(View.GONE);

                } else {
                    linearrazao.setVisibility(View.GONE);
                    linearcheck1.setVisibility(View.GONE);
                    linearcheck2.setVisibility(View.GONE);
                    lineartxtsemana.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        UF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                Boolean ConexOk = Util.checarConexaoCelular(CadContatos.this);
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

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                            ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                            SpnCidade.setAdapter(spinnerArrayAdapter);
                        }
                    } catch (Exception E) {
                        System.out.println("Error" + E);
                    }

                }


                /*Thread thread = new Thread(CadContatos.this);
                thread.start();*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpnCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Boolean ConexOk = Util.checarConexaoCelular(CadContatos.this);
                if (ConexOk == false) {

                    NomeCidade = SpnCidade.getSelectedItem().toString();
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

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                    ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    Bairro.setAdapter(spinnerArrayAdapter);

                } else {

                    NomeCidade = SpnCidade.getSelectedItem().toString();
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

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                    ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    Bairro.setAdapter(spinnerArrayAdapter);

                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Bairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NomeBairro = Bairro.getSelectedItem().toString();
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

    public void btnsalvarcontato(View view) {

        DB = new ConfigDB(this).getReadableDatabase();

        if (sTipoContato == "Selecione o tipo de contato") {
            Toast.makeText(this, "Informe o tipo de contato!", Toast.LENGTH_SHORT).show();
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
            DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, COMPLEMENTO,UF,CODVENDEDOR,CODBAIRRO,CODCIDADE, CODCLIENTE ) VALUES(" +
                    "'" + nome.getText().toString() + "','" + setor.getText().toString() + "','" + email.getText().toString() + "','" + tel1.getText().toString() + "','" + tel2.getText().toString() +
                    "','" + documento.getText().toString() + "','" + data.getText().toString() + "','" + cep.getText().toString() +
                    "','" + endereco.getText().toString() + "','" + numero.getText().toString() + "','" + Complemento.getText().toString() + "','" + sUF + "'," + sCodVend + "," + CodBairro + "," + CodCidade +
                    "," + CodCliente + ");");

            //}
            //CursorContatos.close();
        } catch (Exception E) {
            E.toString();
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadContatos.this);
        builder.setTitle("Novo Contato");
        builder.setIcon(R.drawable.logo_ico);
        builder.setMessage("Deseja cadastrar outro contato para este cliente?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getBaseContext(), CadContatos.class);
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
                        Intent intent = new Intent(getBaseContext(), actListPedidos.class);
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

    public void cadastraDadosCep(String cep) {

        if (cep.length() < 8) {
            Toast.makeText(this, "CEP incompleto. Verifique!", Toast.LENGTH_SHORT).show();
            return;
        }


        String Estado = null;
        String Cidade = null;
        Boolean AtualizaEst = true;
        PesqCEP = true;

        DialogECB = new ProgressDialog(CadContatos.this);
        DialogECB.setTitle("Aguarde...");
        DialogECB.setMessage("");
        DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DialogECB.setProgress(0);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.setMax(0);
        DialogECB.show();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = this.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

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
                Toast.makeText(this, "Sem conexão com a internet! Verifique.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
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

                    endereco.setText(c.getString("logradouro"));
                    numero.requestFocus();

                    //Estado
                    List<String> DadosListEstado = new ArrayList<String>();
                    DadosListEstado.add(Estado);
                    sUF = Estado;
                    ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEstado);
                    arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    UF.setAdapter(arrayAdapterUF);

                    //Cidade
                    List<String> DadosListCidade = new ArrayList<String>();
                    DadosListCidade.add(NomeCidade);
                    ArrayAdapter<String> arrayAdapterCidade = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListCidade);
                    ArrayAdapter<String> spinnerArrayAdapterCidade = arrayAdapterCidade;
                    spinnerArrayAdapterCidade.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    SpnCidade.setAdapter(spinnerArrayAdapterCidade);

                    //Bairro
                    List<String> DadosListBairroUnic = new ArrayList<String>();
                    DadosListBairroUnic.add(NomeBairro);
                    ArrayAdapter<String> arrayAdapterBairroUnic = new ArrayAdapter<String>(CadContatos.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairroUnic);
                    ArrayAdapter<String> spinnerArrayAdapterBairroUnic = arrayAdapterBairroUnic;
                    spinnerArrayAdapterBairroUnic.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    Bairro.setAdapter(spinnerArrayAdapterBairroUnic);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

    }
}