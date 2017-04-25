package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

import java.util.ArrayList;
import java.util.Calendar;

import static com.jdsystem.br.vendasmobile.CadastroContatos.converteZero;
import static java.lang.Integer.parseInt;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class act_TH_horarios_contatos extends Fragment {
    int sCodContato;
    String sCodVend, URLPrincipal, usuario, senha, DOMINGO = "Domingo",
            SEGUNDA = "Segunda-feira",
            TERCA = "Terça-feira",
            QUARTA = "Quarta-feira",
            QUINTA = "Quinta-feira",
            SEXTA = "Sexta-feira",
            SABADO = "Sábado",
            sDiaSemana, horarioInicial, horarioFinal, agendaContato;
    int hour, minute, hora1, minute1, hora2, minute2, codInternoUlt, codDiaSemana;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    TextView TAG_HORARIOS_CONTATOS;
    TimePickerDialog timePickerDialog;
    EditText idEditText, horaFinal, horaInicial;
    ArrayList<String> diasContatos;
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    public SharedPreferences prefs;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    int idPerfil;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_horarios_contato, container, false);
        ctx = getContext();

        prefs = ctx.getSharedPreferences(CONFIG_HOST, ctx.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        TAG_HORARIOS_CONTATOS = (TextView) v.findViewById(R.id.txt_horarios_contatos);
        listView = (ListView) v.findViewById(R.id.lv_horarios_contatos);
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
            public void onItemClick(AdapterView<?> parent, View itemClickView, final int position, long id) {
                AlertDialog.Builder confirmRemove = new AlertDialog.Builder(ctx);
                confirmRemove.setTitle(R.string.remove_hour);
                confirmRemove.setMessage(R.string.remove_schedule)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String itemLista = listView.getItemAtPosition(position).toString();
                                arrayAdapter.remove(diasContatos.get(position));
                                arrayAdapter.notifyDataSetChanged();
                                try {
                                    excluiContatoAgendado(itemLista, sCodContato, ctx);
                                } catch (Exception E) {
                                    E.toString();
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

        DB = new ConfigDB(ctx).getReadableDatabase();
        Intent intent = ((DadosContato) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodContato = params.getInt("codContato");
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }
        CadastroContatos.excluiBaseTempContatos(ctx);
        diasContatos = diasMarcadosContatos();
        arrayAdapter = new ArrayAdapter<String>(ctx,
                android.R.layout.simple_list_item_1, diasContatos);
        listView.setAdapter(arrayAdapter);

        FloatingActionButton horariosContatos = (FloatingActionButton) v.findViewById(R.id.cad_contato_horario);
        horariosContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {

                View fragView = (LayoutInflater.from(ctx)).inflate(R.layout.input_horario_contato, null);

                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);
                alertBuilder.setView(fragView);
                final Spinner horario_contato = (Spinner) fragView.findViewById(R.id.spn_horario_contato);
                timePickerDialog = new TimePickerDialog(ctx, timePickerListener, hour, minute, true);

                horaInicial = (EditText) fragView.findViewById(R.id.horario_inicial);
                horaInicial.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //v.onTouchEvent(event);   // handle the event first
                        idEditText = horaInicial;

                        if (timePickerDialog.isShowing()) {
                            timePickerDialog.dismiss();
                            timePickerDialog = new TimePickerDialog(ctx, timePickerListener, hour, minute, true);
                        }
                        timePickerDialog.show();
                        return true;
                    }
                });

                horaFinal = (EditText) fragView.findViewById(R.id.horario_final);
                horaFinal.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        idEditText = horaFinal;
                        if (timePickerDialog.isShowing()) {
                            timePickerDialog.dismiss();
                            timePickerDialog = new TimePickerDialog(ctx, timePickerListener, hour, minute, true);
                        }
                        timePickerDialog.show();
                        return true;
                    }
                });
                alertBuilder.setView(fragView);
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", null)
                        .setView(fragView);

                final AlertDialog mAlertDialog = alertBuilder.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String diaSemana = horario_contato.getSelectedItem().toString();
                                codDiaSemana = 0;
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
                                        Util.msg_toast_personal(ctx, "Horário inicial de visita não informado!", Toast.LENGTH_SHORT);
                                    } else if ((horaFinal.getText().toString().equals("")) || (horaFinal.getText().toString().equals(null))) {
                                        Util.msg_toast_personal(ctx, "Horário final de visita não informado!", Toast.LENGTH_SHORT);
                                    }
                                } else {
                                    horarioInicial = horaInicial.getText().toString();
                                    horarioFinal = horaFinal.getText().toString();

                                    int a = parseInt(horarioFinal.substring(0, 2));
                                    int b = parseInt(horarioInicial.substring(0, 2));

                                    if (a < b) {
                                        Util.msg_toast_personal(ctx, "Horário final " +
                                                "de visita maior do que o horário inicial de visita", Toast.LENGTH_SHORT);
                                    } else {
                                        salvarAgenda();
                                        /*agendaContato = sDiaSemana + ", de " + converteZero(Integer.toString(hora1)) +
                                                ":" + converteZero(Integer.toString(minute1)) + " às " + converteZero(Integer.toString(hora2)) + ":" +
                                                converteZero(Integer.toString(minute2));*/
                                        //if (salvarAgenda()) {
                                        diasContatos = diasMarcadosContatos();

                                        arrayAdapter = new ArrayAdapter<String>(ctx,
                                                android.R.layout.simple_list_item_1, diasContatos);
                                        listView.setAdapter(arrayAdapter);
                                        mAlertDialog.dismiss();
                                        //}
                                    }
                                }
                            }
                        });
                    }
                });
                mAlertDialog.show();
            }

                /*Intent i = new Intent(ctx, CadastroContatos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                i.putExtras(params);
                startActivity(i);*/

        });
        return v;
    }

    private ArrayList<String> salvarAgenda() {
        ArrayList<String> diasContatos = new ArrayList<String>();
        try {
            DB.execSQL("insert into dias_contatos (cod_dia_semana, codcontatoint, hora_inicio, minuto_inicio, " +
                    "hora_final, minuto_final) values (" + codDiaSemana + ", " + sCodContato + ", " +
                    hora1 + ", " + minute1 + ", " + hora2 + ", " + minute2 + ");");
        } catch (Exception E) {
            E.toString();
        }
        return diasContatos;
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
                TAG_HORARIOS_CONTATOS.setText("Nenhum contato agendado!");
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
        return diasMarcadosVisita;
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

    public static void excluiContatoAgendado(String busca, int codContato, Context context) {
        SQLiteDatabase db = new ConfigDB(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("select coddiacontato, cod_dia_semana, codcontatoint, hora_inicio, minuto_inicio, hora_final, minuto_final " +
                "from dias_contatos " +
                "where codcontatoint = " + codContato, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                int codSemana = cursor.getInt(cursor.getColumnIndex("dias_contatos.cod_dia_semana"));
                String mDiaSemana = Util.diaSemana(codSemana);
                String horaInicio = converteZero(Integer.toString(cursor.getInt(cursor.getColumnIndex("hora_inicio"))));
                String minutoInicio = converteZero(Integer.toString(cursor.getInt(cursor.getColumnIndex("minuto_inicio"))));
                String horaFinal = converteZero(Integer.toString(cursor.getInt(cursor.getColumnIndex("hora_final"))));
                String minutoFinal = converteZero(Integer.toString(cursor.getInt(cursor.getColumnIndex("minuto_final"))));
                String indiceAgenda = Integer.toString(cursor.getInt(cursor.getColumnIndex("coddiacontato")));
                String diaVisita = mDiaSemana + ", de " + horaInicio + ":" + minutoInicio + " às " + horaFinal + ":" + minutoFinal;

                if (diaVisita.equals(busca)) {
                    db.execSQL("delete from dias_contatos " +
                            "where coddiacontato = " + indiceAgenda);
                    cursor.moveToLast();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}



