package com.jdsystem.br.vendasmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.jdsystem.br.vendasmobile.Util.PesquisaCep;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.Util.Localizacao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.jdsystem.br.vendasmobile.Login.NOME_USUARIO;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;

public class CadastroContatos extends AppCompatActivity implements Runnable/*, AdapterView.OnItemSelectedListener*/ {
    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static ProgressDialog DialogECB;
    public SharedPreferences prefs;
    String DOMINGO = "Domingo",
            SEGUNDA = "Segunda-feira",
            TERCA = "Terça-feira",
            QUARTA = "Quarta-feira",
            QUINTA = "Quinta-feira",
            SEXTA = "Sexta-feira",
            SABADO = "Sábado",
            codVendedor, URLPrincipal, usuario, senha, sUF = "", sTipoContato, NomeBairro, NomeCidade = "",
            NomeCliente, descBairro, telaInvocada, sDiaSemana, horarioInicial, horarioFinal,
            agendaContato, codProdManual, atuok, codClieExt;
    Boolean PesqCEP;
    TimePicker timePicker;
    ImageButton BtnPesqCep, btnInformaDiasVisita, btnInformaprodutos, btnInformaCargo;
    int CodCidade, CodBairro, CodCliente, hour, minute, codInternoUlt, tipoContatoPos, ufPosition, cidadePos, bairroPos,
            idPerfil, hora1, minute1, hora2, minute2, codProd, CodContato, codCargo, posCargo, flag, posicao, codCidadeInt;
    int posCidade = 0, posBairro = 0;
    EditText nome, setor, data, documento, endereco, numero, cep, tel1, tel2, email, OBS, Complemento, horaFinal, horaInicial,
            idEditText, edtCidade, edtBairro;
    Spinner TipoContato, TipoCargoEspec,spUF;
    Context ctx;
    LinearLayout lineartxtsemana, linearrazao;
    TextView razaosocial;
    SQLiteDatabase DB;
    ListView listView, lv_informa_produtos;
    ArrayList<String> diasContatos;
    ArrayAdapter<String> arrayAdapter, arrayAdapterProdutos;
    TimePickerDialog timePickerDialog;
    ImageView bolaVermelha;
    Localizacao localizacao = new Localizacao();
    Toolbar toolbar;

    private Handler handler = new Handler();
    private GoogleApiClient client;
    private TimePicker timerPicker1;
    private String descCargo;
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
        valor = String.format("%2s", valor);
        valor = valor.replace(' ', '0');
        return valor;
    }

    public static void excluiBaseTempContatos(Context ctx) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            db.execSQL("delete from diascontatotemporario");
        } catch (Exception E) {
            E.toString();
        }
    }

    public static void excluiBaseTempCadastroContatos(Context ctx) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            db.execSQL("delete from contato_temporario");
        } catch (Exception E) {
            E.toString();
        }
    }

    private static void insereProdutoListaTemp(int CodContato, String codProdManual, int codProd, Context ctx) {
        SQLiteDatabase DB = new ConfigDB(ctx).getReadableDatabase();
        try {
            DB.execSQL("insert into produtos_contatos_temp (cod_interno_contato, cod_produto_manual, cod_item) values (" + CodContato + ", '" + codProdManual + "', " +
                    codProd + ");");
        } catch (Exception E) {
            E.toString();
        }
    }

    public static void excluiProdutosContatosTemp(Context ctx) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            db.execSQL("delete from produtos_contatos_temp");
        } catch (Exception E) {
            E.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_contatos);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        declaraobjetos();
        carregarpreferencias();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                codClieExt = params.getString(getString(R.string.intent_codcliente_ext));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
                codProdManual = params.getString(getString(R.string.intent_codproduto));
                codProd = params.getInt("codProdutoInt");
                CodContato = params.getInt(getString(R.string.intent_codcontato));
                //sTipoContato = params.getString("C");
                //            }
            }
        }

        exibeListaAgenda();

        if (codProdManual != null) {
            if (!codProdManual.isEmpty()) {
                insereProdutoListaTemp(CodContato, codProdManual, codProd, CadastroContatos.this);
            }
        }

        listaProdutosContato();

        PesqCEP = false;
        NomeBairro = null;
        NomeCidade = null;

        if (telaInvocada != null) {
            if (telaInvocada.equals("backPressed")) {
                TipoContato.setSelection(0);
            }
        }

        TipoContato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sTipoContato = "Selecione o tipo de contato";
                        tipoContatoPos = position;
                        break;
                    case 1:
                        sTipoContato = "C";
                        tipoContatoPos = position;
                        break;
                    case 2:
                        sTipoContato = "O";
                        tipoContatoPos = position;
                        break;
                }
                if (sTipoContato == "O") {
                    CodCliente = 0;
                    linearrazao.setVisibility(View.GONE);
                    lineartxtsemana.setVisibility(EditText.VISIBLE);
                } else if (sTipoContato == "C" && CodCliente == 0) {
                    salvaDadosContatosTemporario();
                    lineartxtsemana.setVisibility(EditText.VISIBLE);
                    Intent i = new Intent(CadastroContatos.this, ConsultaClientes.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_codcliente_ext), codClieExt);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putInt(getString(R.string.intent_cad_contato), 1);
                    params.putInt(getString(R.string.intent_codcontato), CodContato);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_telainvocada), telaInvocada);
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
        if ((telaInvocada != null)) {
            if ((telaInvocada.equals("FragmentContatos")) && (CodContato != 0)) {
                alterarDadosContatos();
                //carregaruf(sUF);
            }
        } else {
            carregaDadosContatoTemporario();
            //carregaruf(sUF);
        }
        carregaDadosContatoTemporario();
        TipoContato.setSelection(tipoContatoPos);

        spUF.setSelection(ufPosition);
        spUF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        sUF = "AC"; //Acre
                        ufPosition = position;
                        break;
                    case 2:
                        sUF = "AL"; // Alagoas
                        ufPosition = position;
                        break;
                    case 3:
                        sUF = "AP"; //Amapá
                        ufPosition = position;
                        break;
                    case 4:
                        sUF = "AM";//Amazonas
                        ufPosition = position;
                        break;
                    case 5:
                        sUF = "BA";//Bahia
                        ufPosition = position;
                        break;
                    case 6:
                        sUF = "CE";//Ceará
                        ufPosition = position;
                        break;
                    case 7:
                        sUF = "DF";//Distrito Federal
                        ufPosition = position;
                        break;
                    case 8:
                        sUF = "ES";//Espírito Santo
                        ufPosition = position;
                        break;
                    case 9:
                        sUF = "GO";//Goiás
                        ufPosition = position;
                        break;
                    case 10:
                        sUF = "MA";//Maranhão
                        ufPosition = position;
                        break;
                    case 11:
                        sUF = "MT";//Mato Grosso
                        ufPosition = position;
                        break;
                    case 12:
                        sUF = "MS";//Mato Grosso do Sul
                        ufPosition = position;
                        break;
                    case 13:
                        sUF = "MG";//Minas Gerais
                        ufPosition = position;
                        break;
                    case 14:
                        sUF = "PA";//Pará
                        ufPosition = position;
                        break;
                    case 15:
                        sUF = "PB";//Paraíba
                        ufPosition = position;
                        break;
                    case 16:
                        sUF = "PR";//Paraná
                        ufPosition = position;
                        break;
                    case 17:
                        sUF = "PE";//Pernambuco
                        ufPosition = position;
                        break;
                    case 18:
                        sUF = "PI";//Piauí
                        ufPosition = position;
                        break;
                    case 19:
                        sUF = "RJ";//Rio de Janeiro
                        ufPosition = position;
                        break;
                    case 20:
                        sUF = "RN"; //Rio Grande do Norte
                        ufPosition = position;
                        break;
                    case 21:
                        sUF = "RS";//Rio Grande do Sul
                        ufPosition = position;
                        break;
                    case 22:
                        sUF = "RO"; //Rondônia
                        ufPosition = position;
                        break;
                    case 23:
                        sUF = "RR"; //Roraima
                        ufPosition = position;
                        break;
                    case 24:
                        sUF = "SC";//Santa Catarina
                        ufPosition = position;
                        break;
                    case 25:
                        sUF = "SP";//São Paulo
                        ufPosition = position;
                        break;
                    case 26:
                        sUF = "SE";//Sergipe
                        ufPosition = position;
                        break;
                    case 27:
                        sUF = "TO";//Tocantins
                        ufPosition = position;
                        break;
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
                                } catch (Exception E) {
                                    System.out.println(E);
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
        btnInformaprodutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declaraProdutosContatos();
            }
        });
        lv_informa_produtos.setOnTouchListener(new ListView.OnTouchListener() {
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

        btnInformaCargo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvaNovoCargo();
            }
        });

        selecionaCargoContato();

        TipoCargoEspec.setSelection(posCargo);
        TipoCargoEspec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                posCargo = position;
                try {
                    ArrayList<String> listCargo = new ArrayList<String>();
                    DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
                    Cursor cursorCargo = DB.rawQuery(" SELECT CODCARGO_EXT, CODCARGO, DES_CARGO FROM CARGOS WHERE DES_CARGO = '" + descCargo + "'", null);
                    cursorCargo.moveToFirst();
                    if (cursorCargo.getCount() > 0) {
                        codCargo = cursorCargo.getInt(cursorCargo.getColumnIndex("CODCARGO"));
                        //descCargo = cursorCargo.getString(cursorCargo.getColumnIndex("DES_CARGO"));
                        cursorCargo.close();
                    }

                } catch (Exception E) {
                    System.out.println("Error" + E);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        TipoContato = (Spinner) findViewById(R.id.spnTipoContato);
        TipoCargoEspec = (Spinner) findViewById(R.id.spnCargoEspec);
        spUF = (Spinner) findViewById(R.id.spnUF);
        edtCidade = (EditText) findViewById(R.id.edt_cidade);
        edtBairro = (EditText) findViewById(R.id.edt_bairro);
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
        btnInformaprodutos = (ImageButton) findViewById(R.id.btn_add_produtos_contato);
        lv_informa_produtos = (ListView) findViewById(R.id.list_view_produtos_contato);
        btnInformaCargo = (ImageButton) findViewById(R.id.btn_add_novo_cargo);

        //=========================MÁSCARAS=======================

        cep.addTextChangedListener(Mask.insert(Mask.CEP_MASK, cep));
        tel1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, tel1));
        tel2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, tel2));
        data.addTextChangedListener(Mask.insert(Mask.DATA_MASK, data));
    }

    public void btnsalvarcontato(View view) {

        DB = new ConfigDB(this).getReadableDatabase();

        salvaDadosContatosTemporario();

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

        try {
            //SALVA DADOS NA TABELA DE CONTATOS FINAL

            if (sUF.equals("0")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Util.msg_toast_personal(CadastroContatos.this, "Informe o Estado e a Cidade deste contato.", Toast.LENGTH_SHORT);
                    spUF.requestFocus();
                    return;
                } else {
                    Toast.makeText(CadastroContatos.this, "Informe o Estado e a Cidade deste contato.", Toast.LENGTH_SHORT).show();
                    spUF.requestFocus();
                    return;
                }
            }
            if ((descCargo == null) || (descCargo.equals("null"))) {
                descCargo = "";
            }

            Cursor cursorCont = DB.rawQuery("SELECT CONTATO.CODPERFIL, CODCONTATO_INT " +
                    "FROM CONTATO " +
                    "WHERE CODCONTATO_INT = " + CodContato, null);

            if (cursorCont.getCount() > 0) {
                DB.execSQL("UPDATE CONTATO SET " +
                        "NOME   = '" + nome.getText().toString() + "', " +
                        "CARGO  = '" + descCargo + "', " +
                        "EMAIL  = '" + email.getText().toString() + "', " +
                        "TEL1   = '" + tel1.getText().toString() + "', " +
                        "TEL2   = '" + tel2.getText().toString() + "', " +
                        "DOCUMENTO = '" + documento.getText().toString() + "', " +
                        "DATA   = '" + data.getText().toString() + "', " +
                        "CEP    = '" + cep.getText().toString() + "', " +
                        "ENDERECO = '" + endereco.getText().toString() + "', " +
                        "NUMERO = '" + numero.getText().toString() + "', " +
                        "COMPLEMENTO = '" + Complemento.getText().toString() + "', " +
                        "UF     = '" + sUF + "', " +
                        "CODVENDEDOR = " + codVendedor + ", " +
                        "CODPERFIL = " + idPerfil + ", " +
                        "BAIRRO =   '" + NomeBairro + "', " +
                        "DESC_CIDADE    = '" + NomeCidade + "', " +
                        "CODCLIE_EXT    = " + CodCliente + ", " +
                        "TIPO       = '" + sTipoContato + "', " +
                        "OBS        = '" + OBS.getText().toString() + "', " +
                        "FLAGINTEGRADO = 'N', " +
                        "SETOR  =   '" + setor.getText().toString() + "', " +
                        "CODCARGO   =   " + codCargo + " " +
                        "WHERE CODCONTATO_INT = " + CodContato);
            } else {
                DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, " +
                        "COMPLEMENTO, UF, CODVENDEDOR, CODPERFIL, BAIRRO, DESC_CIDADE, CODCLIE_EXT, TIPO, OBS, " +
                        "FLAGINTEGRADO, SETOR, CODCARGO) VALUES(" +
                        "'" + nome.getText().toString() +
                        "', '" + descCargo +
                        "', '" + email.getText().toString() +
                        "', '" + tel1.getText().toString() +
                        "', '" + tel2.getText().toString() +
                        "', '" + documento.getText().toString() +
                        "', '" + data.getText().toString() +
                        "', '" + cep.getText().toString() +
                        "', '" + endereco.getText().toString() +
                        "', '" + numero.getText().toString() +
                        "', '" + Complemento.getText().toString() +
                        "', '" + sUF +
                        "', " + codVendedor +
                        ", " + idPerfil +
                        ", '" + NomeBairro +
                        "', '" + NomeCidade +
                        "', " + CodCliente +
                        ", '" + sTipoContato +
                        "', '" + OBS.getText().toString() +
                        "', 'N', '" +
                        setor.getText().toString() + "', " +
                        codCargo + ");");
            }

            returnLastId(); //APÓS SALVAR, A FUNÇÃO ABAIXO PEGA A ÚLTIMA ID DO CONTATO SALVO PARA PREENCHER A TABELA DE AGENDA;
            salvarAgenda(); //ESTA FUNÇÃO SALVA AS INFORMAÇÕES DA TABELA TEMPORÁRIA DA AGENDA NA TABELA FINAL DE AGENDA
            salvaProdutosContatos(); //SALVA NA TABELA DE PRODUTOS DEFINITIVA TODOS OS PRODUTOS OFERECIDOS/RELACIONADOS AO CLIENTE.
            excluiBaseTempCadastroContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE CADASTRO DE CONTATOS
            excluiBaseTempContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE HORARIOS DOS CONTATOS
            excluiProdutosContatosTemp(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE PRODUTOS DOS CONTATOS
        } catch (Exception E) {
            E.toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroContatos.this);
        builder.setTitle(R.string.title_novocontato);
        builder.setIcon(R.drawable.logo_ico);
        if (sTipoContato.equals("O")) {
            builder.setMessage(R.string.question_newcontact_contact);
        } else {
            builder.setMessage(R.string.question_newcontact);
        }
        builder.setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        excluiBaseTempCadastroContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE CADASTRO DE CONTATOS
                        excluiBaseTempContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE HORARIOS DOS CONTATOS
                        excluiProdutosContatosTemp(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE PRODUTOS DOS CONTATOS

                        Intent intent = new Intent(getBaseContext(), CadastroContatos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        params.putInt(getString(R.string.intent_codcliente), CodCliente);
                        params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                        params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        excluiBaseTempCadastroContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE CADASTRO DE CONTATOS
                        excluiBaseTempContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE HORARIOS DOS CONTATOS
                        excluiProdutosContatosTemp(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE PRODUTOS DOS CONTATOS

                        Intent intent = new Intent(getBaseContext(), ConsultaPedidos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Toast.makeText(this, "Contato Salvo com sucesso!", Toast.LENGTH_SHORT).show();
    }

    public boolean cadastraDadosCep(String cep) {
        String Estado = null;
        String Cidade = null;
        Boolean AtualizaEst = true;
        String respostaCep = "";
        PesqCEP = true;

        PesquisaCep pesquisaCep = new PesquisaCep();
        respostaCep = pesquisaCep.buscarDadosConsultaCep(cep, CadastroContatos.this);

        if (respostaCep.equals("CEP não Encontrado")) {
            DialogECB.dismiss();
            Toast.makeText(CadastroContatos.this, R.string.CEP_not_found_database, Toast.LENGTH_LONG).show();
            endereco.setText("");
        }

        try {
            JSONObject jsonObj = new JSONObject(respostaCep);
            JSONArray JEndereco = jsonObj.getJSONArray("cep");

            int jumpTime = 0;
            DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JEndereco.length();
            DialogECB.setMax(totalProgressTime);

            for (int i = 0; i < JEndereco.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    JSONObject c = JEndereco.getJSONObject(jumpTime);
                    try {
                        jumpTime += 1;
                        DialogECB.setProgress(jumpTime);
                        DialogECB.setMessage("Sincronizando Tabelas - Estados");
                        String SiglaEstado = c.getString("uf");
                        Estado = SiglaEstado;
                    } catch (Exception E) {
                        E.printStackTrace();
                    }

                    //Cadastrar Cidades
                    try {
                        NomeCidade = c.getString("cidade");
                        int CodCidadeExt = c.getInt("id_cidade");
                        NomeCidade = NomeCidade.replaceAll("'", "");

                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT, UF, CODPERFIL " +
                                "FROM CIDADES " +
                                "WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                        if (CursorCidade.getCount() > 0) {
                            DB.execSQL("UPDATE CIDADES SET UF = '" + Estado + "', DESCRICAO = '" + NomeCidade + "', " +
                                    "CODCIDADE_EXT = '" + CodCidadeExt + "'" +
                                    " WHERE DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil);
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT " +
                                    "FROM CIDADES " +
                                    "WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "' AND CODPERFIL = " + idPerfil, null);
                            cursor1.moveToFirst();
                            CodCidade = parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE")));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO,UF,CODCIDADE_EXT,CODPERFIL)" +
                                    " VALUES('" + NomeCidade + "','" + Estado + "'," + CodCidadeExt + ", " + idPerfil +
                                    ");");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + Estado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidade = parseInt(cursor1.getString(cursor1.getColumnIndex("CODCIDADE")));
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

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODCIDADE, CODPERFIL, CODBAIRRO_EXT " +
                                "FROM BAIRROS " +
                                "WHERE CODCIDADE = " + CodCidade + " AND DESCRICAO = '" + NomeBairro + "' " +
                                "AND CODPERFIL = " + idPerfil, null);
                        if (CursorBairro.getCount() > 0) {
                            CursorBairro.moveToFirst();
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = " + CodCidade + ", DESCRICAO = '" + NomeBairro + "', CODPERFIL = " +
                                    idPerfil + "CODBAIRRO_EXT = " + CodBairroExt +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + CodCidade + "'");
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODCIDADE, CODBAIRRO_EXT, CODPERFIL)" +
                                    " VALUES('" + NomeBairro + "'," + CodCidade + "," + CodBairroExt + "," + idPerfil +
                                    ");");
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
                    sUF = c.getString("uf");
                    String ufconvert = Util.converteUf(sUF);
                    ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.uf));
                    arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                    int pos = arrayAdapterUF.getPosition(ufconvert);
                    spUF.setSelection(pos);

                    //Cidade
                    //TODO Insert das cidades, bairros e estados

                    //TODO preencher o campo texto da cidade;

                    /*final ArrayAdapter<String> spinnerArrayAdapter = localizacao.Cidades(CadastroContatos.this, sUF, spCidade, DialogECB);
                    spCidade.setAdapter(spinnerArrayAdapter);
                    posCidade = spinnerArrayAdapter.getPosition(NomeCidade);
                    spCidade.setSelection(posCidade);*/

                    //BAIRRO

                    //TODO preencher o campo texto do bairro

                    /*final ArrayAdapter<String> spinnerArrayAdapterBairros = localizacao.Bairros(CadastroContatos.this, NomeCidade, spBairro, DialogECB);
                    spBairro.setAdapter(spinnerArrayAdapterBairros);
                    bairroPos = spinnerArrayAdapterBairros.getPosition(NomeBairro);
                    spBairro.setSelection(bairroPos);*/
                }
            }
            PesqCEP = true;

        } catch (JSONException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(CadastroContatos.this, respostaCep, Toast.LENGTH_SHORT);
            } else
                Toast.makeText(this, respostaCep, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

        return PesqCEP;
    }

    public void buscacep(View view) {
        final String sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");

        if (sCEP.length() == 0) {
            endereco.getText().clear();

        } else if (sCEP.length() < 8) {
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

        flag = 2;

        new Thread(CadastroContatos.this).start();

    }

    @Override
    public void onBackPressed() {
        excluiBaseTempCadastroContatos(this);
        if (telaInvocada != null) {
            if (telaInvocada.equals("TH_ContClie")) {

                Intent cadcont = new Intent(CadastroContatos.this, DadosCliente.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codcliente), String.valueOf(CodCliente));
                params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                cadcont.putExtras(params);
                startActivity(cadcont);
                finish();
            } else if (telaInvocada.equals("FragmentContatos")) {
                Intent cadcont = new Intent(CadastroContatos.this, ConsultaPedidos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codcliente), String.valueOf(CodCliente));
                params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                cadcont.putExtras(params);
                startActivity(cadcont);
                finish();
            }
        } else {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CadastroContatos.this);
            builder.setMessage(R.string.cancel_add_contact)
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent cadcont = new Intent(CadastroContatos.this, ConsultaContatos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            cadcont.putExtras(params);
                            excluiBaseTempCadastroContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE CADASTRO DE CONTATOS
                            excluiBaseTempContatos(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE HORARIOS DOS CONTATOS
                            excluiProdutosContatosTemp(CadastroContatos.this); //EXCLUI A TABELA TEMPORARIA DE PRODUTOS DOS CONTATOS
                            startActivity(cadcont);
                            finish();
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            android.app.AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (flag == 1) {
                    Sincronismo.sincronizaCidade(sUF, CadastroContatos.this, DialogECB, handler);
                }
                if (DialogECB != null && flag == 1) {
                    flag = 0;
                    DialogECB.dismiss();
                    //onItemSelected(null, null, posicao, 0);
                } else if (flag == 2) {
                    //====================ACIONADO QUANDO APERTADO O BOTÃO DE PESQUISA DE CEP=======================||
                    new Activity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String sCEP = cep.getText().toString().replaceAll("[^0123456789]", "");
                            cadastraDadosCep(sCEP);
                        }
                    });
                } /*else if (flag == 3) {
                    flag = 0;
                    if (spUF.getSelectedItemPosition() != 0) {
                        if (VerificaConexao()) {
                            if (sUF != null) {
                                if (!PesqCEP) {
                                    Sincronismo.sincronizaCidade(sUF, CadastroContatos.this, DialogECB, handler);
                                    spBairro.setAdapter(null);
                                }
                            }
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = (localizacao.Cidades(CadastroContatos.this, sUF, spCidade, DialogECB));
                        if (spinnerArrayAdapter != null) {
                            try {

                                spCidade.setAdapter(null);
                                spCidade.setAdapter(spinnerArrayAdapter);
                                spCidade.setSelection(posCidade);
                                return;
                            } catch (Exception e) {
                                e.toString();
                            }

                        } else
                            return;
                    }
                    if (DialogECB.isShowing())
                        DialogECB.dismiss();
                } else if (flag == 4) {
                    try {
                        flag = 0;
                        if (spCidade.getSelectedItemPosition() != 0) {
                            //Preenche o spinner de Bairros
                            if (VerificaConexao()) {
                                if (NomeBairro != null) {
                                    if (!PesqCEP) //TODO Está caindo aqui e sincronizando Bairros
                                        Sincronismo.sincronizaBairro(localizacao.retornaCodContatoExt(CadastroContatos.this, NomeCidade, sUF),
                                                CadastroContatos.this, DialogECB, codCidadeInt, handler);
                                }
                            }
                            ArrayAdapter<String> spinnerArrayAdapter = localizacao.Bairros(CadastroContatos.this, NomeCidade, spBairro, DialogECB);
                            if (spinnerArrayAdapter != null) {
                                try {
                                    spBairro.setAdapter(null);
                                    spBairro.setAdapter(spinnerArrayAdapter);
                                    spBairro.setSelection(bairroPos);
                                    return;
                                } catch (Exception e) {
                                    e.toString();
                                }

                            } else {
                                return;
                            }
                        }
                        if (DialogECB.isShowing())
                            DialogECB.dismiss();
                    } catch (Exception e) {
                        e.toString();
                    }
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
        @SuppressLint("InflateParams") View view = (LayoutInflater.from(CadastroContatos.this)).inflate(R.layout.input_horario_contato, null);

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
                .setNegativeButton("Cancelar", null)
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
                            if ((horaInicial.getText().toString().equals("")) || (horaInicial.getText().toString().equals(null))) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    Util.msg_toast_personal(CadastroContatos.this, "Horário inicial de visita não informado!", Toast.LENGTH_SHORT);
                                }else {
                                    Toast.makeText(ctx, "Horário inicial de visita não informado!", Toast.LENGTH_SHORT).show();
                                }
                            } else if ((horaFinal.getText().toString().equals("")) || (horaFinal.getText().toString().equals(null))) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    Util.msg_toast_personal(CadastroContatos.this, "Horário final de visita não informado!", Toast.LENGTH_SHORT);
                                }else {
                                    Toast.makeText(ctx, "Horário final de visita não informado!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            horarioInicial = horaInicial.getText().toString();
                            horarioFinal = horaFinal.getText().toString();

                            int a = parseInt(horarioFinal.substring(0, 2));
                            int b = parseInt(horarioInicial.substring(0, 2));

                            if (a < b) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    Util.msg_toast_personal(CadastroContatos.this, "Horário final " +
                                            "de visita maior do que o horário inicial de visita", Toast.LENGTH_SHORT);
                                }else {
                                    Toast.makeText(ctx, "Horário final " +
                                            "de visita maior do que o horário inicial de visita", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                agendaContato = sDiaSemana + ", de " + converteZero(Integer.toString(hora1)) +
                                        ":" + converteZero(Integer.toString(minute1)) + " às " + converteZero(Integer.toString(hora2)) + ":" +
                                        converteZero(Integer.toString(minute2));
                                if (insereContatos(agendaContato, codDiaSemana)) {
                                    exibeListaAgenda();
                                    mAlertDialog.dismiss();
                                }
                                //listView

                            }
                        }
                    }
                });
                Button cancelBtn = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.cancel();
                    }
                });

            }
        });
        mAlertDialog.show();
    }

    private void exibeListaAgenda() {
        diasContatos = listaContatos();

        arrayAdapter = new ArrayAdapter<String>(CadastroContatos.this,
                android.R.layout.simple_list_item_1, diasContatos);
        listView.setAdapter(arrayAdapter);
    }

    private boolean insereContatos(String diaVisita, int nCodDiaSemana) {
        try {
            Cursor cursor = DB.rawQuery("select diascontatotemporario.dia_visita " +
                    "from diascontatotemporario " +
                    "where dia_visita = '" + diaVisita + "'", null);

            if (cursor.getCount() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Util.msg_toast_personal(CadastroContatos.this, getString(R.string.sched_already_exist), Toast.LENGTH_SHORT);
                }else {
                    Toast.makeText(ctx, getString(R.string.sched_already_exist), Toast.LENGTH_SHORT).show();
                }
                cursor.close();
                return false;
            } else {
                DB.execSQL("insert into diascontatotemporario (dia_visita, cod_dia_semana, hora_inicio, minuto_inicio, hora_final, " +
                        "minuto_final) values ('" + diaVisita + "', " +
                        nCodDiaSemana + ", " + hora1 + ", " + minute1 + ", " + hora2 + ", " + minute2 + " );");
                cursor.close();
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

    private void returnLastId() {
        Cursor cursor = DB.rawQuery("select last_insert_rowid()", null);
        try {
            if (cursor.moveToFirst()) {
                codInternoUlt = cursor.getInt(cursor.getColumnIndex("last_insert_rowid()"));
                cursor.close();
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

    private void salvaProdutosContatos() {
        //SALVA PRODUTOS APÓS CONFIRMAR O SALVAMENTO DO CADASTRO DO CONTATO.
        DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
        try {
            Cursor cursor = DB.rawQuery("select cod_produto_manual, cod_interno_contato, cod_item from produtos_contatos_temp", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                do {
                    String codProdManual = cursor.getString(cursor.getColumnIndex("cod_produto_manual"));
                    int codProd = cursor.getInt(cursor.getColumnIndex("cod_item"));
                    int codInternoContato = codInternoUlt;

                    DB.execSQL("insert into produtos_contatos (cod_produto_manual, cod_interno_contato, cod_item) values " +
                            "('" + codProdManual + "', " + codInternoContato + ", " + codProd + ");");
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
    }

    public void declaraProdutosContatos() {
        salvaDadosContatosTemporario();

        Intent i = new Intent(CadastroContatos.this, ConsultaProdutos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putInt(getString(R.string.intent_cad_contato), 1);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putInt(getString(R.string.intent_codcliente), CodCliente);
        params.putString(getString(R.string.intent_codcliente_ext), codClieExt);
        params.putString(getString(R.string.intent_nomerazao), NomeCliente);
        params.putString(getString(R.string.intent_telainvocada), "CADASTRO_CONTATOS");
        i.putExtras(params);
        startActivity(i);
    }

    private void salvaDadosContatosTemporario() {
        try {
            String nomeContato = nome.getText().toString();
            String setorContato = setor.getText().toString();
            String emailContato = email.getText().toString();
            String tel1Contato = tel1.getText().toString();
            String tel2Contato = tel2.getText().toString();
            String docContato = documento.getText().toString();
            String dataContato = data.getText().toString();
            String cepContato = cep.getText().toString();
            String endContato = endereco.getText().toString();
            String numEndContato = numero.getText().toString();
            String complContato = Complemento.getText().toString();
            String obsContato = OBS.getText().toString();

            Cursor cursor = DB.rawQuery("select * from contato_temporario", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                //returnLastId();
                DB.execSQL("update contato_temporario set NOME = '" + nomeContato + "', " +
                                "CARGO = '" + descCargo + "', EMAIL = '" + emailContato + "', " +
                                "TEL1 = '" + tel1Contato + "', TEL2 = '" + tel2Contato + "', " +
                                "DOCUMENTO = '" + docContato + "', DATA = '" + dataContato + "', " +
                                "CEP = '" + cepContato + "', ENDERECO = '" + endContato + "', " +
                                "NUMERO = '" + numEndContato + "', " +
                                "COMPLEMENTO = '" + complContato + "', " +
                                "UF = '" + sUF + "', CODVENDEDOR = " + codVendedor + ", BAIRRO = '" + NomeBairro + "', " +
                                "DESC_CIDADE = '" + NomeCidade + "', CODCLIE_EXT = " + CodCliente + ", TIPO = '" + sTipoContato + "', " +
                                "OBS = '" + obsContato + "', TIPO_POS = " + tipoContatoPos + ", CODBAIRRO = " + bairroPos + ", " +
                                "CODCIDADE = " + cidadePos + ", UFPOSITION = " + ufPosition + ", SETOR = '" + setorContato + "', CARGO_POS = " +
                                posCargo + ", CODCARGO = " + codCargo
                        /*"where CODCONTATO_INT = " + codInternoUlt*/);
            } else {
                DB.execSQL("INSERT INTO CONTATO_TEMPORARIO (NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, " +
                        "COMPLEMENTO, UF, CODVENDEDOR, BAIRRO, DESC_CIDADE, CODCLIENTE_EXT, TIPO, OBS, TIPO_POS, CODBAIRRO, CODCIDADE, UFPOSITION, " +
                        "SETOR, CARGO_POS, CODCARGO) VALUES(" +
                        "'" + nomeContato + "', '" + descCargo + "', '" +
                        emailContato + "', '" + tel1Contato + "', '" + tel2Contato +
                        "', '" + docContato + "', '" + dataContato + "','" +
                        cepContato +
                        "', '" + endContato + "', '" + numEndContato + "', '" +
                        complContato + "', '" + sUF + "', " + codVendedor + ", '" + NomeBairro + "', '" +
                        NomeCidade + "', " + CodCliente + ", '" + sTipoContato + "', '" + obsContato + "', " +
                        tipoContatoPos + ", " + bairroPos + ", " + cidadePos + ", " + ufPosition + ", '" + setorContato + "', " +
                        posCargo + ", " + codCargo + ");");
            }
            cursor.close();
        } catch (Exception E) {
            System.out.println();
        }
    }

    private void carregaDadosContatoTemporario() {
        try {
            Cursor cursor = DB.rawQuery("select NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, " +
                    "COMPLEMENTO, UF, CODVENDEDOR, BAIRRO, DESC_CIDADE, CODCLIE_EXT, TIPO, OBS, codcontato_int, TIPO_POS, " +
                    "CODBAIRRO, CODCIDADE, UFPOSITION, SETOR, CARGO_POS, CODCARGO " +
                    "from contato_temporario ", null);
            //returnLastId();
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                String nomeRazao = cursor.getString(cursor.getColumnIndex("NOME"));
                nome.setText(nomeRazao);
                descCargo = cursor.getString(cursor.getColumnIndex("CARGO"));
                posCargo = cursor.getInt(cursor.getColumnIndex("CARGO_POS"));
                codCargo = cursor.getInt(cursor.getColumnIndex("CODCARGO"));
                String setorContato = cursor.getString(cursor.getColumnIndex("SETOR"));
                setor.setText(setorContato);
                String emailContato = cursor.getString(cursor.getColumnIndex("EMAIL"));
                email.setText(emailContato);
                String telefone1 = cursor.getString(cursor.getColumnIndex("TEL1"));
                if (!telefone1.equals("")) {
                    tel1.setText(telefone1);
                }
                String telefone2 = cursor.getString(cursor.getColumnIndex("TEL2"));
                if (!telefone2.equals("")) {
                    tel2.setText(telefone2);
                }
                String docContato = cursor.getString(cursor.getColumnIndex("DOCUMENTO"));
                documento.setText(docContato);
                String dataContato = cursor.getString(cursor.getColumnIndex("DATA"));
                data.setText(dataContato);
                String cepContato = cursor.getString(cursor.getColumnIndex("CEP"));
                cep.setText(cepContato);
                String endContato = cursor.getString(cursor.getColumnIndex("ENDERECO"));
                endereco.setText(endContato);
                String numContato = cursor.getString(cursor.getColumnIndex("NUMERO"));
                numero.setText(numContato);
                String complContato = cursor.getString(cursor.getColumnIndex("COMPLEMENTO"));
                Complemento.setText(complContato);
                sUF = cursor.getString(cursor.getColumnIndex("UF"));
                codVendedor = cursor.getString(cursor.getColumnIndex("CODVENDEDOR"));
                NomeBairro = cursor.getString(cursor.getColumnIndex("BAIRRO"));
                //NomeBairro = descBairro;
                NomeCidade = cursor.getString(cursor.getColumnIndex("DESC_CIDADE"));
                sTipoContato = cursor.getString(cursor.getColumnIndex("TIPO"));
                tipoContatoPos = cursor.getInt(cursor.getColumnIndex("TIPO_POS"));
                String obs = cursor.getString(cursor.getColumnIndex("OBS"));
                OBS.setText(obs);
                bairroPos = cursor.getInt(cursor.getColumnIndex("CODBAIRRO"));
                cidadePos = cursor.getInt(cursor.getColumnIndex("CODCIDADE"));
                ufPosition = cursor.getInt(cursor.getColumnIndex("UFPOSITION"));
                cursor.close();
            }
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    private void listaProdutosContato() {
        ArrayList<String> produtosContatos = listaProdutosRelacionados();
        arrayAdapterProdutos = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_list_item_1, produtosContatos);
        lv_informa_produtos.setAdapter(arrayAdapterProdutos);
    }

    private ArrayList<String> listaProdutosRelacionados() {
        ArrayList<String> produtosRelacionados = new ArrayList<String>();
        String itemLista = "";
        DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
        try {
            Cursor cursor = DB.rawQuery("select produtos_contatos_temp.cod_produto_manual, itens.descricao as desc, " +
                    "produtos_contatos_temp.cod_item " +
                    "from produtos_contatos_temp " +
                    "left outer join itens on produtos_contatos_temp.cod_item = itens.CODIGOITEM ", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String codProdutoCont = cursor.getString(cursor.getColumnIndex("produtos_contatos_temp.cod_produto_manual"));
                    int codProduto = cursor.getInt(cursor.getColumnIndex("produtos_contatos_temp.cod_item"));
                    String descProdCont = cursor.getString(cursor.getColumnIndex("desc"));
                    if (descProdCont.length() <= 28) {
                        itemLista = codProdutoCont + " - " + descProdCont;
                    } else {
                        itemLista = codProdutoCont + " - " + descProdCont.substring(0, 28);
                    }

                    produtosRelacionados.add(itemLista);

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
        return produtosRelacionados;
    }

    public void salvaNovoCargo() {
        @SuppressLint("InflateParams") View view = (LayoutInflater.from(CadastroContatos.this)).inflate(R.layout.input_cargo_contato, null);

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CadastroContatos.this);
        alertBuilder.setView(view);
        final EditText edtCargoContato = (EditText) view.findViewById(R.id.input_cargo_contato);

        alertBuilder.setView(view);
        alertBuilder.setCancelable(true)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancelar", null)
                .setView(view);
        final AlertDialog mAlertDialog = alertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String nCargo = edtCargoContato.getText().toString();
                        inserirNovoCargoTabela(nCargo);
                        mAlertDialog.dismiss();
                    }
                });
                Button cancelBtn = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.cancel();
                    }
                });

            }
        });
        mAlertDialog.show();
    }

    public void selecionaCargoContato() {
        try {
            ArrayList<String> listCargo = new ArrayList<String>();
            listCargo.add("Selecione o cargo");
            DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
            Cursor cursorCargo = DB.rawQuery(" SELECT CODCARGO_EXT, CODCARGO, DES_CARGO FROM CARGOS ", null);
            cursorCargo.moveToFirst();
            if (cursorCargo.getCount() > 0) {
                do {
                    codCargo = cursorCargo.getInt(cursorCargo.getColumnIndex("CODCARGO"));
                    descCargo = cursorCargo.getString(cursorCargo.getColumnIndex("DES_CARGO"));

                    listCargo.add(descCargo);
                } while (cursorCargo.moveToNext());
                cursorCargo.close();

                new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item, listCargo).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                TipoCargoEspec.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item, listCargo));
            }

        } catch (Exception E) {
            System.out.println("Error" + E);
        }

    }

    public void inserirNovoCargoTabela(String descCargo) {
        Cursor cursor = null;
        try {
            DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
            cursor = DB.rawQuery("select DES_CARGO from CARGOS " +
                    "where DES_CARGO = '" + descCargo.toUpperCase() + "'", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Util.msg_toast_personal(CadastroContatos.this, "Este cargo já existe cadastrado. Verifique!", Toast.LENGTH_SHORT);
                }else {
                    Toast.makeText(ctx, "Este cargo já existe cadastrado. Verifique!", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                DB.execSQL("insert into CARGOS(DES_CARGO) values ('" + descCargo.toUpperCase() + "');");
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
        selecionaCargoContato();
    }

    private String atualizaspinner() {
        atuok = "S";
        try {
            ArrayAdapter<String> arrayAdapterUF = new ArrayAdapter<String>(CadastroContatos.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.uf));
            arrayAdapterUF.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
            spUF.setAdapter(arrayAdapterUF);
        } catch (Exception e) {
            e.toString();
            atuok = "N";
            return atuok;
        }
        return atuok;
    }

    public boolean VerificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    private void alterarDadosContatos() {
        DB = new ConfigDB(CadastroContatos.this).getReadableDatabase();
        try {
            Cursor cursor = DB.rawQuery("select CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, CONTATO.TEL2, " +
                    "CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, CONTATO.ENDERECO, CONTATO.NUMERO, " +
                    "CONTATO.COMPLEMENTO, CONTATO.UF, CONTATO.CODVENDEDOR, CONTATO.BAIRRO, CONTATO.DESC_CIDADE, " +
                    "CONTATO.CODCLIE_EXT, CONTATO.TIPO, CONTATO.OBS, CONTATO.codcontato_int, " +
                    "CONTATO.CODBAIRRO, CONTATO.CODCIDADE, CONTATO.SETOR, CLIENTES.NOMERAZAO " +
                    "from CONTATO " +
                    "LEFT OUTER JOIN CLIENTES ON CLIENTES.CODCLIE_EXT = CONTATO.CODCLIE_EXT " +
                    "WHERE CODCONTATO_INT = " + CodContato, null);
            //returnLastId();
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                String nomeRazao = cursor.getString(cursor.getColumnIndex("NOME"));
                nome.setText(nomeRazao);
                descCargo = cursor.getString(cursor.getColumnIndex("CARGO"));
                //posCargo = cursor.getInt(cursor.getColumnIndex("CARGO_POS"));
                //codCargo = cursor.getInt(cursor.getColumnIndex("CODCARGO"));
                String setorContato = cursor.getString(cursor.getColumnIndex("SETOR"));
                setor.setText(setorContato);
                String emailContato = cursor.getString(cursor.getColumnIndex("EMAIL"));
                email.setText(emailContato);
                String telefone1 = cursor.getString(cursor.getColumnIndex("TEL1"));
                if (!telefone1.equals("")) {
                    tel1.setText(telefone1);
                }
                String telefone2 = cursor.getString(cursor.getColumnIndex("TEL2"));
                if (!telefone2.equals("")) {
                    tel2.setText(telefone2);
                }
                String docContato = cursor.getString(cursor.getColumnIndex("DOCUMENTO"));
                documento.setText(docContato);
                String dataContato = cursor.getString(cursor.getColumnIndex("DATA"));
                data.setText(dataContato);
                String cepContato = cursor.getString(cursor.getColumnIndex("CEP"));
                cep.setText(cepContato);
                String endContato = cursor.getString(cursor.getColumnIndex("ENDERECO"));
                endereco.setText(endContato);
                String numContato = cursor.getString(cursor.getColumnIndex("NUMERO"));
                numero.setText(numContato);
                String complContato = cursor.getString(cursor.getColumnIndex("COMPLEMENTO"));
                Complemento.setText(complContato);
                sUF = cursor.getString(cursor.getColumnIndex("UF"));
                codVendedor = cursor.getString(cursor.getColumnIndex("CODVENDEDOR"));
                NomeBairro = cursor.getString(cursor.getColumnIndex("BAIRRO"));
                //NomeBairro = descBairro;
                NomeCidade = cursor.getString(cursor.getColumnIndex("DESC_CIDADE"));
                sTipoContato = cursor.getString(cursor.getColumnIndex("TIPO"));
                if (sTipoContato.equals("C")) {
                    TipoContato.setSelection(1);
                    tipoContatoPos = 1;
                } else if (sTipoContato.equals("O")) {
                    TipoContato.setSelection(2);
                    tipoContatoPos = 2;
                }
                //tipoContatoPos = cursor.getInt(cursor.getColumnIndex("TIPO_POS"));
                String obs = cursor.getString(cursor.getColumnIndex("OBS"));
                OBS.setText(obs);
                CodCliente = cursor.getInt(cursor.getColumnIndex("CONTATO.CODCLIE_EXT"));
                NomeCliente = cursor.getString(cursor.getColumnIndex("CLIENTES.NOMERAZAO"));
                /*bairroPos = cursor.getInt(cursor.getColumnIndex("CODBAIRRO"));
                cidadePos = cursor.getInt(cursor.getColumnIndex("CODCIDADE"));
                ufPosition = cursor.getInt(cursor.getColumnIndex("UFPOSITION"));*/
                cursor.close();
            }
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(getString(R.string.bundle_nome_contato), nome.getText().toString());
        outState.putInt(getString(R.string.bundle_pos_tipo_cargo), TipoCargoEspec.getSelectedItemPosition());
        outState.putInt(getString(R.string.bundle_pos_tipo_contato), TipoContato.getSelectedItemPosition());
        outState.putInt(getString(R.string.bundle_posicao_uf), spUF.getSelectedItemPosition());
        outState.putString(getString(R.string.bundle_posicao_cidade), edtCidade.getText().toString());
        outState.putString(getString(R.string.bundle_posicao_bairro), edtBairro.getText().toString());
        outState.putString(getString(R.string.bundle_documento), documento.getText().toString());
        outState.putString(getString(R.string.bundle_setor), setor.getText().toString());
        outState.putString(getString(R.string.bundle_email), email.getText().toString());
        outState.putString(getString(R.string.bundle_cep), cep.getText().toString());
        outState.putString(getString(R.string.bundle_endereco), endereco.getText().toString());
        outState.putString(getString(R.string.bundle_numero), numero.getText().toString());
        outState.putString(getString(R.string.bundle_complemento), Complemento.getText().toString());
        outState.putString(getString(R.string.bundle_telefone1), tel1.getText().toString());
        outState.putString(getString(R.string.bundle_telefone2), tel2.getText().toString());
        outState.putString(getString(R.string.bundle_observacao), OBS.getText().toString());
        outState.putString(getString(R.string.bundle_data), data.getText().toString());
        outState.putString(getString(R.string.bundle_razao_social), razaosocial.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            nome.setText(savedInstanceState.getString(getString(R.string.bundle_nome_contato)));
            TipoCargoEspec.setSelection(savedInstanceState.getInt(getString(R.string.bundle_pos_tipo_cargo)));
            TipoContato.setSelection(savedInstanceState.getInt(getString(R.string.bundle_pos_tipo_contato)));
            spUF.setSelection(savedInstanceState.getInt(getString(R.string.bundle_posicao_uf)));
            edtCidade.setText(savedInstanceState.getString(getString(R.string.bundle_posicao_cidade)));
            edtBairro.setText(savedInstanceState.getString(getString(R.string.bundle_posicao_bairro)));
            documento.setText(savedInstanceState.getString(getString(R.string.bundle_documento)));
            setor.setText(savedInstanceState.getString(getString(R.string.bundle_setor)));
            email.setText(savedInstanceState.getString(getString(R.string.bundle_email)));
            cep.setText(savedInstanceState.getString(getString(R.string.bundle_cep)));
            endereco.setText(savedInstanceState.getString(getString(R.string.bundle_endereco)));
            numero.setText(savedInstanceState.getString(getString(R.string.bundle_numero)));
            Complemento.setText(savedInstanceState.getString(getString(R.string.bundle_complemento)));
            tel1.setText(savedInstanceState.getString(getString(R.string.bundle_telefone1)));
            tel2.setText(savedInstanceState.getString(getString(R.string.bundle_telefone2)));
            OBS.setText(savedInstanceState.getString(getString(R.string.bundle_observacao)));
            data.setText(savedInstanceState.getString(getString(R.string.bundle_data)));
            razaosocial.setText(savedInstanceState.getString(getString(R.string.bundle_razao_social)));
        }
    }
}
