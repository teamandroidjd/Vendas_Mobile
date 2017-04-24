package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.TimeZoneFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Integer.parseInt;

public class CadastroContatos extends AppCompatActivity implements Runnable {
    String sDiaSemana, horarioInicial, horarioFinal,
            agendaContato;
    String DOMINGO = "Domingo",
            SEGUNDA = "Segunda-feira",
            TERCA = "Terça-feira",
            QUARTA = "Quarta-feira",
            QUINTA = "Quinta-feira",
            SEXTA = "Sexta-feira",
            SABADO = "Sábado";

    String codVendedor, URLPrincipal, usuario, senha, sUF, sTipoContato, NomeBairro, NomeCidade, NomeCliente, descBairro,telaInvocada;
    Boolean PesqCEP;
    TimePicker timePicker;
    ImageButton BtnPesqCep, btnInformaDiasVisita;
    int CodCidade, CodBairro, CodCliente, hour, minute, codInternoUlt;
    EditText nome, setor, data, documento, endereco, numero, cep, tel1, tel2, email, OBS, Complemento, horaFinal, horaInicial, idEditText;
    Spinner TipoContato, TipoCargoEspec, spCidade, spBairro, spUF, horarioContato;
    Context ctx;
    LinearLayout linearcheck1, linearcheck2, lineartxtsemana, linearrazao;
    TextView razaosocial;
    private Handler handler = new Handler();
    SQLiteDatabase DB;
    private static ProgressDialog DialogECB;
    private GoogleApiClient client;
    private TimePicker timerPicker1;
    ListView listView;
    ArrayList<String> diasContatos;
    ArrayAdapter<String> arrayAdapter;
    int hora1, minute1, hora2, minute2;
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_contatos);

        declaraobjetos();
        excluiBaseTempContatos(CadastroContatos.this);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
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
                    lineartxtsemana.setVisibility(EditText.VISIBLE);
                } else if (sTipoContato == "C" && CodCliente == 0) {
                    lineartxtsemana.setVisibility(EditText.VISIBLE);
                    Intent i = new Intent(CadastroContatos.this, ConsultaClientes.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putInt(getString(R.string.intent_cad_contato), 1);
                    i.putExtras(params);
                    startActivity(i);
                } else if (CodCliente != 0) {
                    TipoContato.setSelection(1);
                    linearrazao.setVisibility(View.VISIBLE);
                    razaosocial.setText(NomeCliente);
                    lineartxtsemana.setVisibility(View.VISIBLE);
                } else {
                    linearrazao.setVisibility(View.GONE);
                    lineartxtsemana.setVisibility(View.VISIBLE);
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

        listView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder confirmRemove = new AlertDialog.Builder(CadastroContatos.this);
                confirmRemove.setTitle(R.string.remove_hour);
                confirmRemove.setMessage(R.string.remove_schedule)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String itemLista = listView.getItemAtPosition(position).toString();
                                arrayAdapter.remove(diasContatos.get(position));
                                arrayAdapter.notifyDataSetChanged();
                                try {
                                    DB.execSQL("delete from diascontatotemporario " +
                                            "where dia_visita = '" + itemLista + "'");
                                }catch (Exception E){
                                    System.out.println(E);
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = confirmRemove.create();
                alert.show();
            }
        });

        btnInformaDiasVisita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                criaAgendaVisitas();
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
        btnInformaDiasVisita = (ImageButton) findViewById(R.id.btn_add_dias_contato);
        listView = (ListView) findViewById(R.id.list_view_agenda_contato);


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
                    Complemento.getText().toString() + "', '" + sUF + "', " + codVendedor + ", '" + descBairro + "', '" +
                    NomeCidade + "', " + CodCliente + ", '" + sTipoContato + "', '" + OBS.getText().toString() + "');");

            returnLastId();
            salvarAgenda();

            //DB.execSQL("select diascontatotemporario.dia_visita, diascontatotemporario.cod_dia_semana");

            //Estado = cursor1.getString(CursosEstado.getColumnIndex("UF"));
            //}
            //CursorContatos.close();
        } catch (Exception E) {
            E.toString();
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadastroContatos.this);
        builder.setTitle(R.string.title_novocontato);
        builder.setIcon(R.drawable.logo_ico);
        builder.setMessage(R.string.question_newcontact)
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getBaseContext(), CadastroContatos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        params.putInt(getString(R.string.intent_codcliente), CodCliente);
                        params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getBaseContext(), ConsultaPedidos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
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
                            CodCidade = parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE_EXT")));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF)" +
                                    " VALUES('" + NomeCidade + "','" + Estado + "');");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidade = parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE_EXT")));
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
        if (telaInvocada != null) {
            if (telaInvocada.equals("act_TH_contclie")) {

                Intent cadcont = new Intent(CadastroContatos.this, DadosCliente.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codcliente),String.valueOf(CodCliente));
                params.putString(getString(R.string.intent_nomerazao),NomeCliente);
                cadcont.putExtras(params);
                startActivity(cadcont);
                finish();
            }
        } else {
            Intent cadcont = new Intent(CadastroContatos.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            cadcont.putExtras(params);
            startActivity(cadcont);
            finish();
            super.onBackPressed();
        }
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

    private void criaAgendaVisitas() {
        View view = (LayoutInflater.from(CadastroContatos.this)).inflate(R.layout.input_horario_contato, null);

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CadastroContatos.this);
        alertBuilder.setView(view);
        final Spinner horario_contato = (Spinner) view.findViewById(R.id.spn_horario_contato);
        timePickerDialog = new TimePickerDialog(CadastroContatos.this, timePickerListener, hour, minute, true);


        horaInicial = (EditText) view.findViewById(R.id.horario_inicial);
        horaInicial.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //v.onTouchEvent(event);   // handle the event first
                idEditText = horaInicial;

                if (timePickerDialog.isShowing()) {
                    timePickerDialog.dismiss();
                    timePickerDialog = new TimePickerDialog(CadastroContatos.this, timePickerListener, hour, minute, true);
                }
                timePickerDialog.show();
                return true;
            }
        });

        horaFinal = (EditText) view.findViewById(R.id.horario_final);
        horaFinal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                idEditText = horaFinal;
                if (timePickerDialog.isShowing()) {
                    timePickerDialog.dismiss();
                    timePickerDialog = new TimePickerDialog(CadastroContatos.this, timePickerListener, hour, minute, true);
                }
                timePickerDialog.show();
                return true;
            }
        });
        alertBuilder.setView(view);
        alertBuilder.setCancelable(true)
                .setPositiveButton("Ok", null)
                .setView(view);

        final AlertDialog mAlertDialog = alertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String diaSemana = horario_contato.getSelectedItem().toString();
                        int codDiaSemana = 0;
                        if (diaSemana.equals(DOMINGO)) {
                            sDiaSemana = DOMINGO;
                            codDiaSemana = 0;
                        } else if (diaSemana.equals(SEGUNDA)) {
                            sDiaSemana = SEGUNDA;
                            codDiaSemana = 1;
                        } else if (diaSemana.equals(TERCA)) {
                            sDiaSemana = TERCA;
                            codDiaSemana = 2;
                        } else if (diaSemana.equals(QUARTA)) {
                            sDiaSemana = QUARTA;
                            codDiaSemana = 3;
                        } else if (diaSemana.equals(QUINTA)) {
                            sDiaSemana = QUINTA;
                            codDiaSemana = 4;
                        } else if (diaSemana.equals(SEXTA)) {
                            sDiaSemana = SEXTA;
                            codDiaSemana = 5;
                        } else if (diaSemana.equals(SABADO)) {
                            sDiaSemana = SABADO;
                            codDiaSemana = 6;
                        }

                        if ((horaFinal.getText().toString().equals("")) || (horaInicial.getText().toString().equals(""))) {
                            if ((horaInicial.getText().toString().equals(""))|| (horaInicial.getText().toString().equals(null))) {
                                Util.msg_toast_personal(CadastroContatos.this, "Horário inicial de visita não informado!", Toast.LENGTH_SHORT);
                            } else if ((horaFinal.getText().toString().equals("")) || (horaFinal.getText().toString().equals(null))) {
                                Util.msg_toast_personal(CadastroContatos.this, "Horário final de visita não informado!", Toast.LENGTH_SHORT);
                            }
                        } else {
                            horarioInicial = horaInicial.getText().toString();
                            horarioFinal = horaFinal.getText().toString();

                            int a = parseInt(horarioFinal.substring(0, 2));
                            int b = parseInt(horarioInicial.substring(0, 2));

                            if (a < b) {
                                Util.msg_toast_personal(CadastroContatos.this, "Horário final " +
                                        "de visita maior do que o horário inicial de visita", Toast.LENGTH_SHORT);
                            } else {
                                agendaContato = sDiaSemana + ", de " + converteZero(Integer.toString(hora1)) +
                                        ":" + converteZero(Integer.toString(minute1)) + " às " + converteZero(Integer.toString(hora2)) + ":" +
                                        converteZero(Integer.toString(minute2));
                                if(insereContatos(agendaContato, codDiaSemana)){
                                    diasContatos = listaContatos();

                                    arrayAdapter = new ArrayAdapter<String>(CadastroContatos.this,
                                            android.R.layout.simple_list_item_1, diasContatos);
                                    listView.setAdapter(arrayAdapter);
                                    mAlertDialog.dismiss();
                                };
                                //listView

                            }
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private boolean insereContatos(String diaVisita, int nCodDiaSemana) {
        try {
            Cursor cursor = DB.rawQuery("select diascontatotemporario.dia_visita " +
                    "from diascontatotemporario " +
                    "where dia_visita = '" + diaVisita + "'", null);

            if (cursor.getCount() > 0) {
                Util.msg_toast_personal(CadastroContatos.this, getString(R.string.sched_already_exist), Toast.LENGTH_SHORT);

                return false;
            } else {
                DB.execSQL("insert into diascontatotemporario (dia_visita, cod_dia_semana, hora_inicio, minuto_inicio, hora_final, " +
                        "minuto_final) values ('" + diaVisita + "', " +
                        nCodDiaSemana + ", " + hora1 + ", " + minute1 + ", " + hora2 + ", " + minute2 + " );");
            }
        } catch (Exception E) {
            E.toString();

        }
        return true;
    }

    private ArrayList<String> listaContatos() {
        ArrayList<String> diasMarcados = new ArrayList<String>();
        try {
            Cursor cursor = DB.rawQuery("select dia_visita, cod_dia_semana from diascontatotemporario " +
                    "order by cod_dia_semana", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String dataContato = cursor.getString(cursor.getColumnIndex("dia_visita"));

                    diasMarcados.add(dataContato);

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
        return diasMarcados;

    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            hour = hourOfDay;
            minute = minuteOfHour;

            if (idEditText.equals(horaFinal)) {
                hora2 = hour;
                minute2 = minute;
                //horarioFinal = converteZero(Integer.toString(hour)) + converteZero(Integer.toString(minute));
                horaFinal.setText(converteZero(Integer.toString(hour)) + ":" + converteZero(Integer.toString(minute)));
                idEditText = null;
            } else if (idEditText.equals(horaInicial)) {
                hora1 = hour;
                minute1 = minute;
                //horarioInicial = converteZero(Integer.toString(hour)) + converteZero(Integer.toString(minute));
                horaInicial.setText(converteZero(Integer.toString(hour)) + ":" + converteZero(Integer.toString(minute)));
                idEditText = null;
            }

            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, hour);
            time.set(Calendar.MINUTE, minute);

        }
    };

    public static String converteZero(String valor) {
        valor = valor.format("%2s", valor);
        valor = valor.replace(' ', '0');
        return valor;
    }

    public static void excluiBaseTempContatos(Context ctx) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        db.execSQL("delete from diascontatotemporario");
    }

    private void returnLastId() {
        Cursor cursor = DB.rawQuery("select last_insert_rowid()", null);
        try {
            if (cursor.moveToFirst()) {
                codInternoUlt = cursor.getInt(cursor.getColumnIndex("last_insert_rowid()"));
            }
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    private ArrayList<String> salvarAgenda() {
        ArrayList<String> diasContatos = new ArrayList<String>();
        try {
            Cursor cursor = DB.rawQuery("select cod_dia_semana, hora_inicio, minuto_inicio, " +
                    "hora_final, minuto_final " +
                    "from diascontatotemporario " +
                    "order by cod_dia_semana", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    //String dataContato = cursor.getString(cursor.getColumnIndex("dia_visita"));
                    int codDiaSemana = cursor.getInt(cursor.getColumnIndex("cod_dia_semana"));
                    int horaInicio = cursor.getInt(cursor.getColumnIndex("hora_inicio"));
                    int minutoInicio = cursor.getInt(cursor.getColumnIndex("minuto_inicio"));
                    int horaFinal = cursor.getInt(cursor.getColumnIndex("hora_final"));
                    int minutoFinal = cursor.getInt(cursor.getColumnIndex("minuto_final"));

                    DB.execSQL("insert into dias_contatos (cod_dia_semana, codcontatoint, hora_inicio, minuto_inicio, " +
                            "hora_final, minuto_final) values (" + codDiaSemana + ", " + codInternoUlt + ", " +
                            horaInicio + ", " + minutoInicio + ", " + horaFinal + ", " + minutoFinal + ");");
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
        return diasContatos;
    }

}
