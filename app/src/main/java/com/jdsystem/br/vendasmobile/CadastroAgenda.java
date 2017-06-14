package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.jdsystem.br.vendasmobile.Util.Util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.jdsystem.br.vendasmobile.CadastroContatos.converteZero;

public class CadastroAgenda extends AppCompatActivity {

    String codVendedor, URLPrincipal, usuario, senha, sit, date, nomecont, codcont, codclie, NumAgenda, dt, hr;
    SQLiteDatabase DB;
    ArrayList<String> diasContatos;
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    int idPerfil;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    private Context ctx = this;
    int sCodContato, ultAgenda;
    TextView TAG_HORARIOS_CONTATOS, nomeContato;
    RadioButton agendando, finalizando;
    RadioGroup rgsituacao;
    EditText data, obs;
    Button salvar, cancelar;
    private DatePickerDialog datePicker;
    private SimpleDateFormat dateFormatterUSA;
    private SimpleDateFormat dateFormatterBR;
    private Date datahoje, datadigitada;
    public ProgressDialog dialog;
    public AlertDialog alerta;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_agenda);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodContato = params.getInt(getString(R.string.intent_codcontato));
                nomecont = params.getString(getString(R.string.intent_nomecontato));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                codclie = params.getString(getString(R.string.intent_codcliente));
                NumAgenda = params.getString(getString(R.string.intent_numagenda));
            }
        }

        DB = new ConfigDB(this).getReadableDatabase();

        declaraObjetos();
        carregarpreferencias();
        setDateTimeField();
        diasContatos = diasMarcadosContatos();
        carregardados(diasContatos);
        //funcaorg();

        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvaragenda(NumAgenda);
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelaragenda();
            }
        });

        rgsituacao.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                funcaorg();
            }
        });

        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setTitle("Escolha a data: ");
                datePicker.show();

            }
        });

    }

    private ArrayList<String> diasMarcadosContatos() {
        ArrayList<String> diasMarcadosVisita = new ArrayList<String>();
        try {
            Cursor CursorCont = DB.rawQuery(" SELECT dias_contatos.cod_dia_semana, dias_contatos.codcontatoint, " +
                    "dias_contatos.hora_inicio, dias_contatos.minuto_inicio, dias_contatos.hora_final, " +
                    "dias_contatos.minuto_final " +
                    "FROM dias_contatos " +
                    "LEFT OUTER JOIN contato ON CONTATO.CODCONTATO_INT = dias_contatos.codcontatoint " +
                    "WHERE codcontatoint = '" + sCodContato + "' " +
                    "order by cod_dia_semana", null);

            if (CursorCont.getCount() > 0) {
                CursorCont.moveToFirst();
                do {
                    int codSemana = CursorCont.getInt(CursorCont.getColumnIndex("dias_contatos.cod_dia_semana"));
                    String mDiaSemana = Util.diaSemana(codSemana);
                    String horaInicio = converteZero(Integer.toString(CursorCont.getInt(CursorCont.getColumnIndex("dias_contatos.hora_inicio"))));
                    String minutoInicio = converteZero(Integer.toString(CursorCont.getInt(CursorCont.getColumnIndex("dias_contatos.minuto_inicio"))));
                    String horaFinal = converteZero(Integer.toString(CursorCont.getInt(CursorCont.getColumnIndex("dias_contatos.hora_final"))));
                    String minutoFinal = converteZero(Integer.toString(CursorCont.getInt(CursorCont.getColumnIndex("dias_contatos.minuto_final"))));
                    String diaVisita = mDiaSemana + ", de " + horaInicio + ":" + minutoInicio + " às " + horaFinal + ":" + minutoFinal;


                    diasMarcadosVisita.add(diaVisita);
                } while (CursorCont.moveToNext());

                CursorCont.close();
            } else {
                TAG_HORARIOS_CONTATOS.setText("Nenhuma data sugerida!");
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
        return diasMarcadosVisita;
    }

    public void salvaragenda(String NumAgenda) {
        String hoje = Util.DataHojeComHorasBR();
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            datahoje = (Date) formatter.parse(hoje);
        } catch (Exception e) {
            e.toString();
        }

        String dat = data.getText().toString();
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            datadigitada = (Date) formatter.parse(dat);
        } catch (Exception e) {
            e.toString();
        }

        if (agendando.isChecked()) {
            if (data.getText().length() == 0) {
                data.setError("Digite a data do agendamento!");
                Toast.makeText(ctx, "Digite a data do agendamento!", Toast.LENGTH_LONG).show();
                data.requestFocus();
                rgsituacao.check(R.id.rbagendando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.GONE);
                return;
            } else if (datadigitada.before(datahoje)) {
                data.setError("Data e hora do agendamento menor que a data e hora de atual!");
                Toast.makeText(ctx, "Data e hora do agendamento menor que a data e hora de atual!", Toast.LENGTH_LONG).show();
                data.requestFocus();
                rgsituacao.check(R.id.rbagendando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.GONE);
                return;
            }

        } else if (finalizando.isChecked()) {
            if (data.getText().length() == 0) {
                data.setError("Digite a data!");
                Toast.makeText(ctx, "Digite a data!", Toast.LENGTH_LONG).show();
                data.requestFocus();
                rgsituacao.check(R.id.rbfinalizando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.VISIBLE);
                return;
            } else if (datadigitada.after(datahoje)) {
                data.setError("Data e hora da finalização maior que a data e hora de atual!");
                Toast.makeText(ctx, "Data e hora da finalização maior que a data e hora de atual!", Toast.LENGTH_LONG).show();
                data.requestFocus();
                rgsituacao.check(R.id.rbfinalizando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.VISIBLE);
                return;
            } else if (obs.getText().length() == 0) {
                obs.setError("Digite a observação!");
                obs.requestFocus();
                rgsituacao.check(R.id.rbfinalizando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.VISIBLE);
                return;
            }
        }

        gravaragendaDB(NumAgenda);
        returnLastId();
        if (NumAgenda == null) {
            NumAgenda = Integer.toString(ultAgenda);
        }

        Boolean ConexOk = Util.checarConexaoCelular(this);
        if (ConexOk) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(CadastroAgenda.this);
            builder.setTitle("Sincronizar");
            builder.setMessage("Deseja sincronizar o agendamento n° " + NumAgenda + " agora?");
            builder.setCancelable(false);
            final String finalNumAgenda = NumAgenda;
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {

                    dialog = new ProgressDialog(CadastroAgenda.this);
                    dialog.setMessage("Sincronizando agendamento n° " + finalNumAgenda);
                    dialog.setCancelable(false);
                    dialog.setTitle("Aguarde");
                    dialog.show();

                    String agendaenviada = Sincronismo.SincronizarAgendaEnvio(ctx, finalNumAgenda, dialog);

                    if (agendaenviada.equals("OK")) {
                        dialog.dismiss();
                        Intent intent = new Intent(CadastroAgenda.this, ConsultaAgenda.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        intent.putExtras(params);
                        startActivityForResult(intent, 1);
                        finish();
                    }
                }
            });

            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    Toast.makeText(CadastroAgenda.this, "Agendamento não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(CadastroAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    i.putExtras(params);
                    startActivity(i);
                    finish();

                }
            });
            alerta = builder.create();
            alerta.show();
        } else {
            Toast.makeText(CadastroAgenda.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(CadastroAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            finish();
        }
    }

    public void gravaragendaDB(String NumAgenda) {
        try {
        DB = new ConfigDB(this).getReadableDatabase();
        Cursor CursorAgenda = DB.rawQuery(" SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO FROM AGENDA" +
                " WHERE CODIGO = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil, null);

            if (CursorAgenda.getCount() > 0) {
                if (agendando.isChecked()) {
                    DB.execSQL(" UPDATE AGENDA SET " +
                            "   STATUS = 'N'" +
                            ", SITUACAO = 'A'" +
                            ", DATAAGEND = '" + date + "' " +
                            " WHERE CODIGO = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);

                } else if (finalizando.isChecked()) {
                    DB.execSQL(" UPDATE AGENDA SET " +
                            "   STATUS = 'N'" +
                            ", SITUACAO = 'F'" +
                            ", DESCRICAO = '" + obs.getText().toString() + "' " +
                            ", DATAAGEND = '" + date + "' " +
                            " WHERE CODIGO = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);
                }
            } else {
                if (agendando.isChecked()) {
                    DB.execSQL(" INSERT INTO AGENDA ( NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO )" +
                            " VALUES('" + nomecont + "', '" + sCodContato + "', 'N', '" + idPerfil + "', '" + date + "', " +
                            " '', 'A');");
                } else if (finalizando.isChecked()) {
                    DB.execSQL(" INSERT INTO AGENDA ( NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO )" +
                            " VALUES('" + nomecont + "', '" + sCodContato + "', 'N', '" + idPerfil + "', '" + date + "', " +
                            " '" + obs.getText().toString() + "', 'F');");
                }
            }
            CursorAgenda.close();
        } catch (Exception E) {
            E.toString();
            Toast.makeText(this, "Não foi possível salvar o agendamento. Verifique!", Toast.LENGTH_SHORT).show();
        }
    }

    private void returnLastId() {
        Cursor cursor = DB.rawQuery("select last_insert_rowid()", null);
        try {
            if (cursor.moveToFirst()) {
                ultAgenda = cursor.getInt(cursor.getColumnIndex("last_insert_rowid()"));
                cursor.close();
            }
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    public void cancelaragenda() {
        Intent intent = new Intent(CadastroAgenda.this, ConsultaAgenda.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString(getString(R.string.intent_datainicial), null);
        params.putString(getString(R.string.intent_datafinal), null);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    public void funcaorg() {
        switch (rgsituacao.getCheckedRadioButtonId()) {
            case R.id.rbagendando:
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.GONE);
                sit = "A";
                break;
            case R.id.rbfinalizando:
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.VISIBLE);
                sit = "F";
                break;
        }
    }

    private void setDateTimeField() {
        dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormatterUSA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        mYear = newCalendar.get(Calendar.YEAR);
        mMonth = newCalendar.get(Calendar.MONTH);
        mDay = newCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = newCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = newCalendar.get(Calendar.MINUTE);
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    dt = dateFormatterBR.format(newDate.getTime());

                    new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            hr = Util.AcrescentaZeros(String.valueOf(hourOfDay),2) + ":" + Util.AcrescentaZeros(String.valueOf(minute),2);
                            data.setText(dt+ " " +hr);
                            date = Util.FormataDataAAAAMMDD_ComHoras(data.getText().toString() +":00");
                        }
                    }, mHour, mMinute, false).show();
                }catch (Exception e) {
                    e.toString();
                }
            }
        }, mYear, mMonth, mDay);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CadastroAgenda.this, ConsultaAgenda.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString(getString(R.string.intent_datainicial), null);
        params.putString(getString(R.string.intent_datafinal), null);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    public void declaraObjetos() {
        TAG_HORARIOS_CONTATOS = (TextView) findViewById(R.id.txv1);
        listView = (ListView) findViewById(R.id.lv_horarios_contatos);
        nomeContato = (TextView) findViewById(R.id.txv_contato);
        rgsituacao = (RadioGroup) findViewById(R.id.rgsituacao);
        data = (EditText) findViewById(R.id.data);
        obs = (EditText) findViewById(R.id.EdtOBS);
        agendando = (RadioButton) findViewById(R.id.rbagendando);
        finalizando = (RadioButton) findViewById(R.id.rbfinalizando);
        salvar = (Button) findViewById(R.id.btnsalvar);
        cancelar = (Button) findViewById(R.id.btncancelar);
        date = "";
        rgsituacao.check(R.id.rbagendando);

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString(getString(R.string.intent_prefs_host), null);
        idPerfil = prefs.getInt(getString(R.string.intent_prefs_perfil), 0);
    }

    private void carregardados(ArrayList<String> diasContatos) {
        if (NumAgenda == null || NumAgenda.equals("0")) {

            nomeContato.setText("Contato: " + sCodContato + " - " + nomecont);
            try {
                arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, diasContatos);
                listView.setAdapter(arrayAdapter);
            } catch (Exception e) {
                e.toString();
            }
                rgsituacao.check(R.id.rbagendando);
                data.setVisibility(View.VISIBLE);
                obs.setVisibility(View.GONE);
                sit = "A";
            } else{
                try {
                    Cursor CursorAgenda = DB.rawQuery(" SELECT CODIGO, SITUACAO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO FROM AGENDA " +
                            " WHERE CODIGO = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil, null);

                    if (CursorAgenda.getCount() > 0) {
                        CursorAgenda.moveToFirst();
                        codcont = CursorAgenda.getString(CursorAgenda.getColumnIndex("CODCONTATO"));
                        nomecont = CursorAgenda.getString(CursorAgenda.getColumnIndex("NOMECONTATO"));
                        nomeContato.setText("Contato: " + codcont + " - " + nomecont);
                        String SitAg = CursorAgenda.getString(CursorAgenda.getColumnIndex("SITUACAO"));
                        if (SitAg.equals("A")) {
                            rgsituacao.check(R.id.rbagendando);
                            data.setVisibility(View.VISIBLE);
                            obs.setVisibility(View.GONE);
                            sit = "A";
                        }
                        if (SitAg.equals("F")) {
                            rgsituacao.check(R.id.rbfinalizando);
                            data.setVisibility(View.VISIBLE);
                            obs.setVisibility(View.VISIBLE);
                            obs.setText(CursorAgenda.getString(CursorAgenda.getColumnIndex("DESCRICAO")));
                            sit = "F";
                        }

                        String dt = CursorAgenda.getString(CursorAgenda.getColumnIndex("DATAAGEND"));
                        data.setText(Util.FormataDataDDMMAAAA_ComHoras(dt));

                        CursorAgenda.close();

                        if (codcont != null && !codcont.equals("0")) {
                            sCodContato = Integer.parseInt(codcont);
                        }
                        if (diasContatos.size() == 0) {
                            diasContatos = diasMarcadosContatos();
                        }
                        arrayAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, diasContatos);
                        listView.setAdapter(arrayAdapter);
                    }
                } catch (Exception e) {
                    e.toString();
                }
            }
        }
    }

