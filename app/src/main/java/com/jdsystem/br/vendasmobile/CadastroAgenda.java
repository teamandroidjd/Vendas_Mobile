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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Gps;
import com.jdsystem.br.vendasmobile.Util.Util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.jdsystem.br.vendasmobile.CadastroContatos.converteZero;

public class CadastroAgenda extends AppCompatActivity {

    String codVendedor, URLPrincipal, usuario, senha, sit, date, nomecont,nomeclie, datahoje, codclie, NumAgenda, dt, hr, telaInvocada, sCodContato;
    SQLiteDatabase DB;
    ArrayList<String> diasContatos, produtosRelacionados, obsAnteriores;
    ArrayAdapter<String> arrayAdapter, arrayAdapterProdutos, arrayAdapterObservacao;
    ListView listView, lv_informa_produtos, lv_obs_agenda;
    int idPerfil;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    private Context ctx = this;
    String ultAgenda;
    Double latitude, longitude;
    TextView TAG_HORARIOS_CONTATOS, nomeContato, txv3, txv2, txv4;
    EditText data, obs;
    Button salvar, cancelar;
    LinearLayout layprodutos, layhorarios;
    private DatePickerDialog datePicker;
    private SimpleDateFormat dateFormatterUSA;
    private SimpleDateFormat dateFormatterBR;
    private Date datadb, datadigitada, hj;
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
                sCodContato = String.valueOf(params.getInt(getString(R.string.intent_codcontato)));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                codclie =  String.valueOf(params.getInt(getString(R.string.intent_codcliente)));
                NumAgenda = params.getString(getString(R.string.intent_numagenda));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
            }
        }

        DB = new ConfigDB(this).getReadableDatabase();

        declaraObjetos();
        carregarpreferencias();
        setDateTimeField();
        diasContatos = diasMarcadosContatos();
        carregardados(diasContatos);
        listaProdutosContato();
        arrayObsAnteriores();

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
                listView.setVisibility(View.GONE);
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }
        return diasMarcadosVisita;
    }

    private void arrayObsAnteriores() {
        ArrayList<String> obsAnteriores = listaObsAnteriores();
        arrayAdapterObservacao = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, obsAnteriores);
        lv_obs_agenda.setAdapter(arrayAdapterObservacao);
    }

    private ArrayList<String> listaObsAnteriores() {
        obsAnteriores = new ArrayList<String>();
        DB = new ConfigDB(ctx).getReadableDatabase();
        if (NumAgenda != null) {
            try {
                Cursor cursor = DB.rawQuery("SELECT DATA_HORA, TEXTO FROM AGETEXTOS " +
                        "WHERE CODAGENDA_INT = " + NumAgenda + " AND CODPERFIL = " + idPerfil, null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    do {
                        String data = cursor.getString(cursor.getColumnIndex("DATA_HORA"));
                        String texto = cursor.getString(cursor.getColumnIndex("TEXTO"));

                        String itemLista = data + " - " + texto;

                        obsAnteriores.add(itemLista);

                    } while (cursor.moveToNext());
                } else {
                    txv4.setText("Nenhuma observação anterior!");
                }
                cursor.close();
            } catch (Exception E) {
                E.toString();
            }
        } else {
            txv4.setText("Nenhuma observação anterior!");
        }
        return obsAnteriores;
    }

    private void listaProdutosContato() {
        ArrayList<String> produtosContatos = listaProdutosRelacionados();
        arrayAdapterProdutos = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, produtosContatos);
        lv_informa_produtos.setAdapter(arrayAdapterProdutos);
    }



    private ArrayList<String> listaProdutosRelacionados() {
        produtosRelacionados = new ArrayList<String>();
        DB = new ConfigDB(ctx).getReadableDatabase();
        try {
            Cursor cursor = DB.rawQuery("select produtos_contatos.cod_produto_manual, produtos_contatos.cod_item, produtos_contatos.cod_interno_contato, " +
                    "ITENS.DESCRICAO as desc " +
                    "from produtos_contatos " +
                    "left outer join ITENS on produtos_contatos.cod_item = ITENS.CODIGOITEM " +
                    "where produtos_contatos.cod_interno_contato = " + sCodContato, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String codProdutoCont = cursor.getString(cursor.getColumnIndex("produtos_contatos.cod_item"));
                    int codProd = cursor.getInt(cursor.getColumnIndex("produtos_contatos.cod_item"));
                    String descProdCont = cursor.getString(cursor.getColumnIndex("desc"));
                    String itemLista;
                    if (descProdCont.length() <= 26) {
                        itemLista = codProdutoCont + " - " + descProdCont;
                    } else {
                        itemLista = codProdutoCont + " - " + descProdCont.substring(0, 26);
                    }

                    produtosRelacionados.add(itemLista);

                } while (cursor.moveToNext());

            } else if(cursor.getCount()==0){
                try {
                    Cursor cursorDois = DB.rawQuery("select produtos_contatos.cod_produto_manual, produtos_contatos.cod_item, " +
                            "produtos_contatos.cod_interno_contato, produtos_contatos.cod_externo_contato, " +
                            "ITENS.DESCRICAO as desc " +
                            "from produtos_contatos " +
                            "left outer join ITENS on produtos_contatos.cod_item = ITENS.CODIGOITEM " +
                            "where produtos_contatos.cod_externo_contato = " + sCodContato, null);
                    cursorDois.moveToFirst();
                    if (cursorDois.getCount()>0) {
                        do {
                            String codProdutoCont = cursorDois.getString(cursorDois.getColumnIndex("cod_item"));
                            int codProd = cursorDois.getInt(cursorDois.getColumnIndex("produtos_contatos.cod_item"));
                            String descProdCont = cursorDois.getString(cursorDois.getColumnIndex("desc"));
                            String itemLista;
                            if (descProdCont.length() <= 26) {
                                itemLista = codProdutoCont + " - " + descProdCont;
                            } else {
                                itemLista = codProdutoCont + " - " + descProdCont.substring(0, 26);
                            }

                            produtosRelacionados.add(itemLista);

                        } while (cursorDois.moveToNext());
                    } else{
                        txv2.setText("Nenhum produto sugerido!");
                        lv_informa_produtos.setVisibility(View.GONE);
                        //return produtosRelacionados;
                    }
                    cursor.close();
                    cursorDois.close();
                }catch(Exception E){
                    E.toString();
                }
            }else{
                txv2.setText("Nenhum produto sugerido!");
                lv_informa_produtos.setVisibility(View.GONE);
                //return produtosRelacionados;
            }
            cursor.close();
        } catch (Exception E) {
            E.toString();
        }
        return produtosRelacionados;
    }

    public void salvaragenda(final String NumAgenda) {

        Calendar calendar = Calendar.getInstance();
        hj = calendar.getTime();
        SimpleDateFormat dfUSA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        datahoje = dfUSA.format(hj);

        Gps gps = new Gps(getApplicationContext());
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();

      /* String hoje = Util.DataHojeComHorasBR();
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
        }*/

        if (data.getText().length() == 0) {
            data.setError("Digite a data do agendamento!");
            Toast.makeText(ctx, "Digite a data do agendamento!", Toast.LENGTH_LONG).show();
            data.requestFocus();
            return;
        }

        if (telaInvocada.equals("Finalizar")) {
            if (obs.getText().length() == 0) {
                obs.setError("Digite a observação!");
                obs.requestFocus();
                return;
            }
        }

        gravaragendaDB(NumAgenda);
        /*returnLastId();
        if (NumAgenda == null) {
            NumAgenda = Integer.toString(ultAgenda);
        }*/

        if (telaInvocada.equals("CadastroAgenda")) {
            final AlertDialog.Builder builderAut = new AlertDialog.Builder(this);
            builderAut.setTitle("Finalizar");
            builderAut.setMessage("Deseja finalizar o agendamento?");
            builderAut.setCancelable(false);
            builderAut.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    DB.execSQL(" UPDATE AGENDAS SET " +
                            "   STATUS = 'N'" +
                            ", SITUACAO = 2" +
                            ", DATA_FINALIZADO = '" + datahoje + "' " +
                            ", LATITUDE = '" + latitude + "' " +
                            ", LONGITUDE = '" + longitude + "' " +
                            " WHERE CODAGENDA_INT = '" + ultAgenda + "' AND CODPERFIL = " + idPerfil);

                    Boolean ConexOk = Util.checarConexaoCelular(ctx);
                    if (ConexOk) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(CadastroAgenda.this);
                        builder.setTitle("Sincronizar");
                        builder.setMessage("Deseja sincronizar o agendamento n° " + ultAgenda + " agora?");
                        builder.setCancelable(false);
                        //final String finalNumAgenda = NumAgenda;
                        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                dialog = new ProgressDialog(CadastroAgenda.this);
                                dialog.setMessage("Sincronizando agendamento n° " + ultAgenda);
                                dialog.setCancelable(false);
                                dialog.setTitle("Aguarde");
                                dialog.show();

                                String agendaenviada = Sincronismo.SincronizarAgendaEnvio(ctx, NumAgenda, ultAgenda, dialog);

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
            });
            builderAut.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    alerta.dismiss();
                    Boolean ConexOk = Util.checarConexaoCelular(ctx);
                    if (ConexOk) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(CadastroAgenda.this);
                        builder.setTitle("Sincronizar");
                        builder.setMessage("Deseja sincronizar o agendamento n° " + ultAgenda + " agora?");
                        builder.setCancelable(false);
                        final String finalNumAgenda = NumAgenda;
                        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                dialog = new ProgressDialog(CadastroAgenda.this);
                                dialog.setMessage("Sincronizando agendamento n° " + ultAgenda);
                                dialog.setCancelable(false);
                                dialog.setTitle("Aguarde");
                                dialog.show();

                                String agendaenviada = Sincronismo.SincronizarAgendaEnvio(ctx, finalNumAgenda, ultAgenda, dialog);

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
            });
            alerta = builderAut.create();
            alerta.show();
        }

    }

    public void gravaragendaDB(String NumAgenda) {
        try {
        DB = new ConfigDB(this).getReadableDatabase();
        Cursor CursorAgenda = DB.rawQuery(" SELECT * FROM AGENDAS" +
                " WHERE CODAGENDA_INT = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil, null);

            if (CursorAgenda.getCount() > 0) {
                if (telaInvocada.equals("Remarcar")) {
                    if (!sCodContato.equals("0") && sCodContato != null) {
                        DB.execSQL(" INSERT INTO AGENDAS ( CODCONTATO, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND )" +
                                " VALUES('"  + sCodContato + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                " 1,'" + codVendedor + "' );");
                    }  else if (codclie != null && !codclie.equals("0")) {
                        DB.execSQL(" INSERT INTO AGENDAS ( CODCLIE, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND )" +
                                " VALUES('"  + codclie + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                " 1,'" + codVendedor + "' );");
                    }

                    returnLastId();
                    if (obs.getText().length() != 0) {
                        DB.execSQL(" INSERT INTO AGETEXTOS ( CODAGENDA_INT, STATUS, CODPERFIL, DATA_HORA, TEXTO )" +
                                " VALUES('" + ultAgenda + "', 'N', '" + idPerfil + "', '" + datahoje + "', " +
                                " '" + obs.getText().toString() + "' );");
                    }
                    DB.execSQL(" UPDATE AGENDAS SET " +
                            "   STATUS = 'N'" +
                            ", SITUACAO = 4" +
                            ", COD_REAGENDADO = '" + ultAgenda + "' " +
                            " WHERE CODAGENDA_INT = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);

                } else if (telaInvocada.equals("Finalizar")) {

                    String dia = CursorAgenda.getString(CursorAgenda.getColumnIndex("DATA_HORA"));
                    try {
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        datadb = (Date) formatter.parse(dia);
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

                    if (datadigitada.equals(datadb)){
                        DB.execSQL(" UPDATE AGENDAS SET " +
                                "   STATUS = 'N'" +
                                ", SITUACAO = 2" +
                                ", DATA_FINALIZADO = '" + datahoje + "' " +
                                ", LATITUDE = '" + latitude + "' " +
                                ", LONGITUDE = '" + longitude + "' " +
                                " WHERE CODAGENDA_INT = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);

                        if (obs.getText().length() != 0) {
                            DB.execSQL(" INSERT INTO AGETEXTOS ( CODAGENDA_INT, STATUS, CODPERFIL, DATA_HORA, TEXTO )" +
                                    " VALUES('"  + NumAgenda + "', 'N', '" + idPerfil + "', '" + datahoje + "', " +
                                    " '" + obs.getText().toString() + "' );");
                        }

                    } else {
                        if (!sCodContato.equals("0") && sCodContato != null) {
                            DB.execSQL(" INSERT INTO AGENDAS ( CODCONTATO, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND, "+
                                    " DATA_FINALIZADO, LATITUDE, LONGITUDE )" +
                                    " VALUES('"  + sCodContato + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                    " 2,'" + codVendedor + "', '" + datahoje + "', '" + latitude + "', '" + longitude + "');");
                        }  else if (codclie != null && !codclie.equals("0")) {
                            DB.execSQL(" INSERT INTO AGENDAS ( CODCONTATO, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND, "+
                                    " DATA_FINALIZADO, LATITUDE, LONGITUDE )" +
                                    " VALUES('"  + codclie + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                    " 2,'" + codVendedor + "', '" + datahoje + "', '" + latitude + "', '" + longitude + "');");
                        }

                        returnLastId();

                        if (obs.getText().length() != 0) {
                            DB.execSQL(" INSERT INTO AGETEXTOS ( CODAGENDA_INT, STATUS, CODPERFIL, DATA_HORA, TEXTO )" +
                                    " VALUES('"  + ultAgenda + "', 'N', '" + idPerfil + "', '" + datahoje + "', " +
                                    " '" + obs.getText().toString() + "' );");
                        }

                        DB.execSQL(" UPDATE AGENDAS SET " +
                                "   STATUS = 'N'" +
                                ", SITUACAO = 4" +
                                ", COD_REAGENDADO = '" + ultAgenda + "' " +
                                " WHERE CODAGENDA_INT = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);
                    }
                }
            } else {
                if (telaInvocada.equals("CadastroAgenda")) {
                    if (!sCodContato.equals("0") && sCodContato != null) {
                        DB.execSQL(" INSERT INTO AGENDAS ( CODCONTAT, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND )" +
                                " VALUES('"  + sCodContato + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                " 1,'" + codVendedor + "' );");
                    }  else if (codclie != null && !codclie.equals("0")) {
                        DB.execSQL(" INSERT INTO AGENDAS ( CODCLIE, STATUS, CODPERFIL, DATA_HORA, SITUACAO, CODVEND )" +
                                " VALUES('"  + codclie + "', 'N', '" + idPerfil + "', '" + date + "', " +
                                " 1,'" + codVendedor + "' );");
                    }

                    returnLastId();

                    if (obs.getText().length() != 0) {
                        DB.execSQL(" INSERT INTO AGETEXTOS ( CODAGENDA_INT, STATUS, CODPERFIL, DATA_HORA, TEXTO )" +
                                " VALUES('" + ultAgenda + "', 'N', '" + idPerfil + "', '" + datahoje + "', " +
                                " '" + obs.getText().toString() + "' );");
                    }
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
                ultAgenda = cursor.getString(cursor.getColumnIndex("last_insert_rowid()"));
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
        lv_obs_agenda = (ListView) findViewById(R.id.lv_obs_agenda);
        nomeContato = (TextView) findViewById(R.id.txv_contato);
        txv2 = (TextView) findViewById(R.id.txv2);
        txv3 = (TextView) findViewById(R.id.txv3);
        txv4 = (TextView) findViewById(R.id.txv4);
        data = (EditText) findViewById(R.id.data);
        obs = (EditText) findViewById(R.id.EdtOBS);
        salvar = (Button) findViewById(R.id.btnsalvar);
        cancelar = (Button) findViewById(R.id.btncancelar);
        lv_informa_produtos = (ListView) findViewById(R.id.lv_produtos_contatos);
        layprodutos = (LinearLayout) findViewById(R.id.layprodutos);
        layhorarios = (LinearLayout) findViewById(R.id.layhorarios);
        date = "";

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString(getString(R.string.intent_prefs_host), null);
        idPerfil = prefs.getInt(getString(R.string.intent_prefs_perfil), 0);
    }

    private void carregardados(ArrayList<String> diasContatos) {
        if (telaInvocada.equals("CadastroAgenda")) {
            txv3.setText("Cadastrando: ");
            if (!sCodContato.equals("0") && sCodContato != null) {
                Cursor CursorContatos = DB.rawQuery("SELECT NOME FROM CONTATO WHERE CODCONTATO_INT = "+ sCodContato +
                        " AND CODPERFIL = " + idPerfil, null);
                CursorContatos.moveToFirst();
                if (CursorContatos.getCount() > 0) {
                    nomecont = CursorContatos.getString(CursorContatos.getColumnIndex("NOME"));
                }
                CursorContatos.close();

                nomeContato.setText("Contato: " + sCodContato + " - " + nomecont);
                layhorarios.setVisibility(View.VISIBLE);
                layprodutos.setVisibility(View.VISIBLE);

                try {
                    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, diasContatos);
                    listView.setAdapter(arrayAdapter);
                } catch (Exception e) {
                    e.toString();
                }

            } else if (codclie != null && !codclie.equals("0")) {
                Cursor cursorClientes = DB.rawQuery(" SELECT NOMERAZAO FROM CLIENTES WHERE CODCLIE_INT = " + codclie +
                        " AND CODPERFIL = " + idPerfil, null);
                cursorClientes.moveToFirst();
                if (cursorClientes.getCount() > 0) {
                    nomeclie = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                }
                nomeContato.setText("Cliente: " + codclie + " - " + nomeclie);
                layhorarios.setVisibility(View.GONE);
                layprodutos.setVisibility(View.GONE);
            }

        } else if (telaInvocada.equals("Remarcar")) {
            txv3.setText("Remarcando: ");
            try {
                Cursor CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODCONTAT, AGENDAS.CODCLIE " +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODAGENDA_INT = " + NumAgenda + " AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
                if (CursorAgenda.getCount() > 0) {
                    CursorAgenda.moveToFirst();

                    sCodContato = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCONTAT"));
                    nomecont = CursorAgenda.getString(CursorAgenda.getColumnIndex("CONTATO.NOME"));
                    if (!sCodContato.equals("0") && sCodContato != null){
                        nomeContato.setText("Contato: " + sCodContato + " - " + nomecont);

                        layhorarios.setVisibility(View.VISIBLE);
                        layprodutos.setVisibility(View.VISIBLE);
                        if (diasContatos.size() == 0) {
                            diasContatos = diasMarcadosContatos();
                        }
                        arrayAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, diasContatos);
                        listView.setAdapter(arrayAdapter);
                    }

                    codclie = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCLIE"));
                    nomeclie = CursorAgenda.getString(CursorAgenda.getColumnIndex("CLIENTES.NOMERAZAO"));
                    if (codclie != null && !codclie.equals("0")){
                        nomeContato.setText("Cliente: "+ codclie + " - " +nomeclie);
                        layhorarios.setVisibility(View.GONE);
                        layprodutos.setVisibility(View.GONE);
                    }

                    data.setText("Escolha nova data");

                    CursorAgenda.close();

                }
            } catch (Exception e) {
                e.toString();
            }
        } else if (telaInvocada.equals("Finalizar")) {
            txv3.setText("Finalizando: ");
            try {
                Cursor CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODCONTAT, AGENDAS.CODCLIE, AGENDAS.DATA_HORA " +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODAGENDA_INT = " + NumAgenda + " AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
                if (CursorAgenda.getCount() > 0) {
                    CursorAgenda.moveToFirst();

                    sCodContato = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCONTAT"));
                    nomecont = CursorAgenda.getString(CursorAgenda.getColumnIndex("CONTATO.NOME"));
                    if (!sCodContato.equals("0") && sCodContato != null){
                        nomeContato.setText("Contato: " + sCodContato + " - " + nomecont);

                        layhorarios.setVisibility(View.VISIBLE);
                        layprodutos.setVisibility(View.VISIBLE);
                        if (diasContatos.size() == 0) {
                            diasContatos = diasMarcadosContatos();
                        }
                        arrayAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, diasContatos);
                        listView.setAdapter(arrayAdapter);
                    }

                    codclie = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCLIE"));
                    nomeclie = CursorAgenda.getString(CursorAgenda.getColumnIndex("CLIENTES.NOMERAZAO"));
                    if (codclie != null && !codclie.equals("0")){
                        nomeContato.setText("Cliente: "+ codclie + " - " +nomeclie);
                        layhorarios.setVisibility(View.GONE);
                        layprodutos.setVisibility(View.GONE);
                    }

                    String dt = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.DATA_HORA"));
                    data.setText(Util.FormataDataDDMMAAAA_ComHoras(dt));

                    CursorAgenda.close();

                }
            } catch (Exception e) {
                e.toString();
            }
        }
/*
        if (NumAgenda == null || NumAgenda.equals("0")) {


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
                        Integer SitAg = CursorAgenda.getInt(CursorAgenda.getColumnIndex("SITUACAO"));
                        if (SitAg == 1) {
                            sit = "A";
                        }
                        if (SitAg == 2) {
                            sit = "F";
                        }

                        String dt = CursorAgenda.getString(CursorAgenda.getColumnIndex("DATAAGEND"));
                        data.setText(Util.FormataDataDDMMAAAA_ComHoras(dt));

                        CursorAgenda.close();

                        if (codcont != null && !codcont.equals("0")) {
                            sCodContato = codcont;
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
            }*/
        }
    }

