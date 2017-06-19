package com.jdsystem.br.vendasmobile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterAgenda;
import com.jdsystem.br.vendasmobile.domain.Agenda;
import com.jdsystem.br.vendasmobile.fragments.FragmentAgenda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsultaAgenda extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public ListAdapterAgenda adapter;
    public SharedPreferences prefs;
    Integer SitAgenda = 0;
    String CodContato = "0";
    String DtInicio = "0";
    String DtFinal = "0";
    String NovaAgenda = "0";
    String sData = "0";
    String CodCliente = "0";
    String codVendedor, URLPrincipal, usuario, senha, UsuarioLogado, nomeContato, nomeCliente, NomeSitAgenda, TipoAgenda, OutraData, hoje, semana, mes, dthoje, dtsemana, dtmes;
    ProgressDialog pDialog;
    Agenda lstagenda;
    SQLiteDatabase DB;
    FloatingActionMenu mmPrinc_Agenda;
    private DatePickerDialog datePicker;
    private SimpleDateFormat dateFormatterUSA;
    Date hj, smn, ms;
    Toolbar toolbar;
    TextView txtdata;
    Spinner spndata;
    Button btnok;
    FloatingActionButton mmSitAgenda, mmDataAgenda, mmContAgenda, mmNovoAgenda;
    int idPerfil;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_agenda);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                SitAgenda = params.getInt(getString(R.string.intent_situacaoagenda));
                CodContato = String.valueOf(params.getInt(getString(R.string.intent_codcontato)));
                CodCliente = params.getString(getString(R.string.intent_codcliente));
                DtInicio = params.getString(getString(R.string.intent_datainicial));
                DtFinal = params.getString(getString(R.string.intent_datafinal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                NovaAgenda= params.getString(getString(R.string.intent_novaagenda));
                sData= params.getString(getString(R.string.intent_agendadata));
                OutraData= params.getString(getString(R.string.intent_outradata));
            }
        }

        declaraobjetos();
        carregausuariologado();
        carregarpreferencias();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pDialog = new ProgressDialog(ConsultaAgenda.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.carregando_agendas));
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(ConsultaAgenda.this);
        thread.start();

        spndata.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sData = "0";
                        txtdata.setVisibility(View.GONE);
                        break;
                    case 1:
                        sData = "Hoje";
                        txtdata.setVisibility(View.VISIBLE);
                        txtdata.setText(hoje);
                        break;
                    case 2:
                        sData = "Semana";
                        txtdata.setVisibility(View.VISIBLE);
                        txtdata.setText(hoje +" até "+semana);
                        break;
                    case 3:
                        sData = "Mes";
                        txtdata.setVisibility(View.VISIBLE);
                        txtdata.setText(hoje+" até "+mes);
                        break;
                    case 4:
                        sData = "Outro";
                        if (OutraData == null) {
                            datePicker.setTitle("Escolha a data");
                            datePicker.show();
                        } else {
                            txtdata.setText(Util.FormataDataDDMMAAAA(OutraData));
                        }
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        txtdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setTitle("Escolha a data");
                datePicker.show();
            }
        });

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funcaoSpnData(sData);
            }
        });
    }

    public Date somaDias(Date data, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }

    private void funcaoSpnData(String sData) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_agendadata), sData);
            params.putString(getString(R.string.intent_outradata), OutraData);

            intent.putExtras(params);
            startActivityForResult(intent, 4);
    }

    private void setDateTimeField() {
        dateFormatterUSA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                OutraData = dateFormatterUSA.format(newDate.getTime());
                txtdata.setVisibility(View.VISIBLE);
                txtdata.setText(Util.FormataDataDDMMAAAA(OutraData));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public List<Agenda> CarregarAgenda() {
        ArrayList<Agenda> DadosList = new ArrayList<Agenda>();
        try {
            Cursor CursorAgenda = null;
            if (SitAgenda != 0) {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.SITUACAO = '" + SitAgenda + "' AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);

            } else if (!CodContato.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODCONTAT = " + CodContato + " AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
            } else if (!CodCliente.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODCLIE = " + CodCliente + " AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
            } else if (!DtInicio.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODPERFIL = " + idPerfil + " AND (AGENDAS.DATA_HORA >= '" + DtInicio + "' AND AGENDAS.DATA_HORA < '" + DtFinal + 1 + "')" +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
            } else if (!NovaAgenda.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODAGENDA_INT = " + NovaAgenda + " AND AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
            } else if (!sData.equals("0")) {
                if (sData.equals("Hoje")) {
                    String hojeI = dthoje + " 00:00:00";
                    String hojeF = dthoje + " 23:59:59";
                    CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                            "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                            " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                            " FROM AGENDAS LEFT OUTER JOIN" +
                            " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                            " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                            " WHERE AGENDAS.CODPERFIL = " + idPerfil + " AND (AGENDAS.DATA_HORA >= '" + hojeI + "' AND AGENDAS.DATA_HORA < '" + hojeF + "')" +
                            " ORDER BY AGENDAS.DATA_HORA DESC ", null);

                } else if (sData.equals("Semana")) {
                     CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                             "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                             " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                             " FROM AGENDAS LEFT OUTER JOIN" +
                             " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                             " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                             " WHERE AGENDAS.CODPERFIL = " + idPerfil + " AND (AGENDAS.DATA_HORA >= '" + dthoje + "' AND AGENDAS.DATA_HORA < '" + dtsemana + 1 + "')" +
                             " ORDER BY AGENDAS.DATA_HORA DESC ", null);

                } else if (sData.equals("Mes")) {
                     CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                             "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                             " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                             " FROM AGENDAS LEFT OUTER JOIN" +
                             " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                             " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                             " WHERE AGENDAS.CODPERFIL = " + idPerfil + " AND (AGENDAS.DATA_HORA >= '" + dthoje + "' AND AGENDAS.DATA_HORA < '" + dtmes + 1 + "')" +
                             " ORDER BY AGENDAS.DATA_HORA DESC ", null);
                } else if (sData.equals("Outro")) {
                    String OutraDataI = OutraData + " 00:00:00";
                    String OutraDataF = OutraData + " 23:59:59";

                     CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                             "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                             " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                             " FROM AGENDAS LEFT OUTER JOIN" +
                             " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                             " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                             " WHERE AGENDAS.CODPERFIL = " + idPerfil + " AND (AGENDAS.DATA_HORA >= '" + OutraDataI + "' AND AGENDAS.DATA_HORA < '" + OutraDataF + "')" +
                             " ORDER BY AGENDAS.DATA_HORA DESC ", null);
                    OutraData = null;
                }
            } else {
                CursorAgenda = DB.rawQuery(" SELECT CONTATO.NOME, CLIENTES.NOMERAZAO, AGENDAS.CODAGENDA_INT, AGENDAS.CODCLIE, " +
                        "  AGENDAS.CODCONTAT, AGENDAS.STATUS, AGENDAS.SITUACAO, AGENDAS.DATA_HORA, AGENDAS.COD_REAGENDADO, " +
                        " (SELECT AGENDAS.DATA_HORA FROM AGENDAS AS DT WHERE DT.CODAGENDA_INT = AGENDAS.COD_REAGENDADO) AS DT_REAGENDADO" +
                        " FROM AGENDAS LEFT OUTER JOIN" +
                        " CONTATO ON (AGENDAS.CODCONTAT = CONTATO.CODCONTATO_INT) AND (AGENDAS.CODPERFIL = CONTATO.CODPERFIL) LEFT OUTER JOIN" +
                        " CLIENTES ON (AGENDAS.CODCLIE = CLIENTES.CODCLIE_INT) AND (AGENDAS.CODPERFIL = CLIENTES.CODPERFIL)" +
                        " WHERE AGENDAS.CODPERFIL = " + idPerfil +
                        " ORDER BY AGENDAS.DATA_HORA DESC ", null);
            }

            if (CursorAgenda.getCount() > 0) {
                CursorAgenda.moveToFirst();
                do {
                    try {
                        Integer Situacao = CursorAgenda.getInt(CursorAgenda.getColumnIndex("AGENDAS.SITUACAO"));

                        String Nome = "";
                        String O = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCONTAT"));
                        if (O != null){
                            nomeContato = CursorAgenda.getString(CursorAgenda.getColumnIndex("CONTATO.NOME"));
                            Nome = "Contato: " + nomeContato;
                        }
                        String E = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODCLIE"));
                        if (E != null){
                            nomeCliente  = CursorAgenda.getString(CursorAgenda.getColumnIndex("CLIENTES.NOMERAZAO"));
                            Nome = "Cliente: " + nomeCliente;
                        }

                        String DATAAGEND = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.DATA_HORA"));
                        String NumAgenda = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.CODAGENDA_INT"));
                        String Status = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.STATUS"));
                        String Novaagenda = CursorAgenda.getString(CursorAgenda.getColumnIndex("AGENDAS.COD_REAGENDADO"));
                        String Dtreagendado = CursorAgenda.getString(CursorAgenda.getColumnIndex("DT_REAGENDADO"));

                        lstagenda = new Agenda(Situacao, Nome, DATAAGEND, NumAgenda, Status, Novaagenda, Dtreagendado);
                        DadosList.add(lstagenda);
                    } catch (Exception E) {
                        Toast.makeText(this, E.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                while (CursorAgenda.moveToNext());
                CursorAgenda.close();

            } else {
                if (SitAgenda != 0) {
                    Toast.makeText(this, "Nenhum agendamento do tipo " + NomeSitAgenda + " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!CodContato.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento do contato " + nomeContato + " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!CodCliente.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento do cliente " + nomeCliente + " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!DtInicio.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento encontrado com o período de " + DtInicio + " até " + DtFinal + ".", Toast.LENGTH_LONG).show();
                } else if (!NovaAgenda.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento com esse código " + NovaAgenda +  " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!sData.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento encontrado com esse período!", Toast.LENGTH_LONG).show();
                    OutraData = null;
                }
            }
        } catch (Exception E) {
            E.toString();
        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
        return DadosList;
    }

    public void cadagenda(View view) {


        @SuppressLint("InflateParams") View viewTipoAgenda = (LayoutInflater.from(ConsultaAgenda.this)).inflate(R.layout.input_tipo_agenda, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaAgenda.this);
        alertBuilder.setView(viewTipoAgenda);
        final Spinner spntipo = (Spinner) viewTipoAgenda.findViewById(R.id.spnTipo);

        alertBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TipoAgenda = spntipo.getSelectedItem().toString();

                        if (TipoAgenda.equals("Contato")) {
                            try {
                                Intent intent = new Intent(ConsultaAgenda.this, ConsultaContatos.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_telainvocada), "CadastroAgenda");
                                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                params.putString(getString(R.string.intent_usuario), usuario);
                                params.putString(getString(R.string.intent_senha), senha);
                                params.putInt(getString(R.string.intent_flag), 2);
                                intent.putExtras(params);
                                startActivityForResult(intent, 1);

                            } catch (Exception E) {
                                E.toString();
                            }
                        } else if (TipoAgenda.equals("Cliente")) {
                            try {
                                Intent intent = new Intent(ConsultaAgenda.this, ConsultaClientes.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_telainvocada), "CadastroAgenda");
                                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                params.putString(getString(R.string.intent_usuario), usuario);
                                params.putString(getString(R.string.intent_senha), senha);
                                params.putInt(getString(R.string.intent_flag), 3);
                                intent.putExtras(params);
                                startActivityForResult(intent, 1);

                            } catch (Exception E) {
                                E.toString();
                            }
                        }
                    }
                });
        Dialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void filtrositagenda(View view) {

        @SuppressLint("InflateParams") View viewSitAgenda = (LayoutInflater.from(ConsultaAgenda.this)).inflate(R.layout.input_filtro_sit_agenda, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaAgenda.this);
        alertBuilder.setView(viewSitAgenda);
        final Spinner spSituacaoAgenda = (Spinner) viewSitAgenda.findViewById(R.id.spnSitAgenda);

        alertBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NomeSitAgenda = spSituacaoAgenda.getSelectedItem().toString();

                        if (NomeSitAgenda.equals("Agendado")) {
                            SitAgenda = 1;
                        } else if (NomeSitAgenda.equals("Finalizado")) {
                            SitAgenda = 2;
                        } else if (NomeSitAgenda.equals("Cancelado")) {
                            SitAgenda = 3;
                        }  else if (NomeSitAgenda.equals("Reagendado")) {
                            SitAgenda = 4;
                        } else if (NomeSitAgenda.equals("Todos")) {
                            SitAgenda = 0;
                        }

                        try {
                            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putInt(getString(R.string.intent_codcontato), 0);
                            params.putInt(getString(R.string.intent_situacaoagenda), SitAgenda);
                            params.putString(getString(R.string.intent_nomesitagenda), NomeSitAgenda);
                            params.putString(getString(R.string.intent_datainicial), "0");
                            params.putString(getString(R.string.intent_datafinal), "0");
                            params.putString(getString(R.string.intent_novaagenda), "0");
                            params.putString(getString(R.string.intent_agendadata), "0");

                            intent.putExtras(params);
                            finish();
                            startActivity(intent);

                        } catch (Exception E) {
                            E.toString();
                        }

                    }
                });
        Dialog dialog = alertBuilder.create();
        dialog.show();

    }

    public void filtrodataagenda(View view) {
        Intent intent = new Intent(ConsultaAgenda.this, FiltroPeriodoAgenda.class);
        //finish();
        startActivityForResult(intent, 3);
    }

    public void filtrocontagenda(View view) {

        @SuppressLint("InflateParams") View viewFiltroAgenda = (LayoutInflater.from(ConsultaAgenda.this)).inflate(R.layout.input_tipo_agenda, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaAgenda.this);
        alertBuilder.setView(viewFiltroAgenda);
        final Spinner spntipo = (Spinner) viewFiltroAgenda.findViewById(R.id.spnTipo);

        alertBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TipoAgenda = spntipo.getSelectedItem().toString();

                        if (TipoAgenda.equals("Contato")) {
                            try {
                                Intent intent = new Intent(ConsultaAgenda.this, ConsultaContatos.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                params.putString(getString(R.string.intent_usuario), usuario);
                                params.putString(getString(R.string.intent_senha), senha);
                                params.putString(getString(R.string.intent_telainvocada), "ConsultaAgenda");
                                params.putBoolean(getString(R.string.intent_consultaagenda), true);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();

                            } catch (Exception E) {
                                E.toString();
                            }
                        } else if (TipoAgenda.equals("Cliente")) {
                            try {
                                Intent intent = new Intent(ConsultaAgenda.this, ConsultaClientes.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_telainvocada), "ConsultaAgenda");
                                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                params.putString(getString(R.string.intent_usuario), usuario);
                                params.putString(getString(R.string.intent_senha), senha);
                                params.putInt(getString(R.string.intent_flag), 4);
                                intent.putExtras(params);
                                startActivityForResult(intent, 1);

                            } catch (Exception E) {
                                E.toString();
                            }
                        }
                    }
                });
        Dialog dialog = alertBuilder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (SitAgenda != 0) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_usuario), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!CodContato.equals("0")) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!DtInicio.equals("0")) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!NovaAgenda.equals("0")) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Boolean Resultado;
        switch (requestCode) {
            case 1: {
                try {
                    //Resultado = data.getExtras().getBoolean("atualizalista");
                    Resultado = true;
                    if (Resultado) {
                        SitAgenda = 0;
                        CodContato = "0";
                        DtInicio = "0";
                        DtFinal = "0";
                        NovaAgenda = "0";
                        sData = "0";
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

                    }
                } catch (Exception E) {
                    //
                }
            }
            break;
            case 2: {
                try {

                    CodContato = data.getExtras().getString("codContato");
                    SitAgenda = 0;
                    DtInicio = "0";
                    DtFinal = "0";
                    NovaAgenda = "0";
                    sData = "0";
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);
                    params.putString(getString(R.string.intent_agendadata), sData);

                    intent.putExtras(params);
                    finish();
                    startActivity(intent);

                } catch (Exception E) {
                    //
                }
            }
            break;
            case 3: {
                try {
                    CodContato = "0";
                    SitAgenda = 0;
                    DtInicio = data.getExtras().getString(getString(R.string.intent_datainicial));
                    DtFinal = data.getExtras().getString(getString(R.string.intent_datafinal));
                    NovaAgenda = "0";
                    sData = "0";
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);
                    params.putString(getString(R.string.intent_agendadata), sData);

                    intent.putExtras(params);
                    finish();
                    startActivity(intent);

                } catch (Exception E) {
                    //
                }
            }
            break;
            case 4: {
                try {
                    CodContato = "0";
                    SitAgenda = 0;
                    DtInicio = "0";
                    DtFinal = "0";
                    NovaAgenda = data.getExtras().getString("codContato");
                    sData = "0";
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_novaagenda), NovaAgenda);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);
                    params.putString(getString(R.string.intent_agendadata), sData);

                    intent.putExtras(params);
                    finish();
                    startActivity(intent);

                } catch (Exception E) {
                    //
                }
            }
            case 5: {
                try {
                    CodContato = "0";
                    SitAgenda = 0;
                    DtInicio = "0";
                    DtFinal = "0";
                    NovaAgenda = "0";
                    sData = data.getExtras().getString(getString(R.string.intent_agendadata));
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_novaagenda), NovaAgenda);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);
                    params.putString(getString(R.string.intent_agendadata), sData);

                    intent.putExtras(params);
                    finish();
                    startActivity(intent);

                } catch (Exception E) {
                    //
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(ConsultaAgenda.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaAgenda.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_agenda) {


        } else if (id == R.id.nav_sincronismo) {
            Intent intent = new Intent(ConsultaAgenda.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(ConsultaAgenda.this, Login.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.exit(1);
            finish();
        } else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(ConsultaAgenda.this, InfoJDSystem.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FragmentAgenda frag = (FragmentAgenda) getSupportFragmentManager().findFragmentByTag("mainFrag");
                    if (frag == null) {
                        frag = new FragmentAgenda();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putString(getString(R.string.intent_usuario), usuario);
                        bundle.putString(getString(R.string.intent_senha), senha);
                        bundle.putString(getString(R.string.intent_codvendedor), codVendedor);
                        bundle.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        frag.setArguments(bundle);
                        ft.replace(R.id.rl_fragment_container, frag, "mainFrag");
                        ft.commit();
                    }
                } catch (Exception E) {
                    E.toString();
                }
            }

        });
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString(getString(R.string.intent_prefs_host), null);
        idPerfil = prefs.getInt(getString(R.string.intent_prefs_perfil), 0);
    }

    private void carregausuariologado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        UsuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        usuariologado.setText("Olá " + UsuarioLogado + "!");
    }

    private void declaraobjetos() {
        try {
            GoogleApiClient client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
            DB = new ConfigDB(this).getReadableDatabase();
            mmNovoAgenda = (FloatingActionButton) findViewById(R.id.mmPrincNovoAgenda);
            mmPrinc_Agenda = (FloatingActionMenu) findViewById(R.id.mmPrinc_Agenda);
            mmSitAgenda = (FloatingActionButton) findViewById(R.id.mmSitAgenda);
            mmDataAgenda = (FloatingActionButton) findViewById(R.id.mmDataAgenda);
            mmContAgenda = (FloatingActionButton) findViewById(R.id.mmContAgenda);
            spndata = (Spinner) findViewById(R.id.spndata);
            txtdata= (TextView) findViewById(R.id.txtdata);
            btnok = (Button) findViewById(R.id.btnok);

            setDateTimeField();

            if (SitAgenda == null) {
                SitAgenda = 0;
            }
            if (DtInicio == null) {
                DtInicio = "0";
            }
            if (DtFinal == null) {
                DtFinal = "0";
            }
            if (CodContato == null) {
                CodContato = "0";
            }
            if (NovaAgenda == null) {
                NovaAgenda = "0";
            }
            if (sData == null) {
                sData = "0";
            }
            if (CodCliente == null) {
                CodCliente = "0";
            }

            Calendar calendar = Calendar.getInstance();
            hj = calendar.getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            SimpleDateFormat dfUSA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            hoje = df.format(hj);
            dthoje = dfUSA.format(hj);
            smn =  somaDias(hj, 7);
            semana = df.format(smn);
            dtsemana = dfUSA.format(smn);
            ms =  somaDias(hj, 30);
            mes = df.format(ms);
            dtmes = dfUSA.format(ms);

            if (sData.equals("0")) {
                spndata.setSelection(0);
            } else if (sData.equals("Hoje")) {
                spndata.setSelection(1);
            } else if (sData.equals("Semana")) {
                spndata.setSelection(2);
            } else if (sData.equals("Mes")) {
                spndata.setSelection(3);
            } else if (sData.equals("Outro")) {
                spndata.setSelection(4);
            }

        } catch (Exception e){
            e.toString();
        }

    }
}
