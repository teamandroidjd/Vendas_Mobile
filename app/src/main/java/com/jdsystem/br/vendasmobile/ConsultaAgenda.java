package com.jdsystem.br.vendasmobile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterAgenda;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Agenda;
import com.jdsystem.br.vendasmobile.fragments.FragmentAgenda;

import java.util.ArrayList;
import java.util.List;

public class ConsultaAgenda extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public ListAdapterAgenda adapter;
    public SharedPreferences prefs;
    String SitAgenda = "0";
    String CodContato = "0";
    String DtInicio = "0";
    String DtFinal = "0";
    String codVendedor, URLPrincipal, usuario, senha, UsuarioLogado, nomeContato, NomeSitAgenda;
    ProgressDialog pDialog;
    Agenda lstagenda;
    SQLiteDatabase DB;
    FloatingActionMenu mmPrinc_Agenda;
    Toolbar toolbar;
    FloatingActionButton mmSitAgenda, mmDataAgenda, mmContAgenda, mmNovoAgenda;
    int idPerfil;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_consulta_agenda);
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
                SitAgenda = params.getString(getString(R.string.intent_situacaoagenda));
                CodContato = String.valueOf(params.getInt(getString(R.string.intent_codcontato)));
                DtInicio = params.getString(getString(R.string.intent_datainicial));
                DtFinal = params.getString(getString(R.string.intent_datafinal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                nomeContato = params.getString(getString(R.string.intent_nomecontato));
                NomeSitAgenda= params.getString(getString(R.string.intent_nomesitagenda));
            }
            if (SitAgenda == null) {
                SitAgenda = "0";
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

    }

    public List<Agenda> CarregarAgenda() {
        ArrayList<Agenda> DadosList = new ArrayList<Agenda>();
        try {
            Cursor CursorAgenda = null;
            if (!SitAgenda.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO FROM AGENDA " +
                        " WHERE SITUACAO = '" + SitAgenda + "' AND CODPERFIL = " + idPerfil +
                        " ORDER BY DATAAGEND DESC ", null);
            } else if (!CodContato.equals("0")) {
                CursorAgenda = DB.rawQuery("  SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO FROM AGENDA " +
                        " WHERE CODCONTATO = " + CodContato + " AND CODPERFIL = " + idPerfil +
                        " ORDER BY DATAAGEND DESC ", null);
            } else if (!DtInicio.equals("0")) {
                CursorAgenda = DB.rawQuery(" SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO FROM AGENDA " +
                        " WHERE CODPERFIL = " + idPerfil + " AND (DATAAGEND >= '" + DtInicio + "' AND DATAAGEND < '" + DtFinal + 1 + "')" +
                        " ORDER BY DATAAGEND DESC ", null);
            } else {
                CursorAgenda = DB.rawQuery(" SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO, SITUACAO FROM AGENDA " +
                        " WHERE CODPERFIL = " + idPerfil +
                        " ORDER BY DATAAGEND DESC ", null);
            }

            if (CursorAgenda.getCount() > 0) {
                CursorAgenda.moveToFirst();
                do {
                    try {
                        String Situacao = CursorAgenda.getString(CursorAgenda.getColumnIndex("SITUACAO"));
                        String NomeContato = CursorAgenda.getString(CursorAgenda.getColumnIndex("NOMECONTATO"));
                        String DATAAGEND = CursorAgenda.getString(CursorAgenda.getColumnIndex("DATAAGEND"));
                        String NumAgenda = CursorAgenda.getString(CursorAgenda.getColumnIndex("CODIGO"));
                        String Status = CursorAgenda.getString(CursorAgenda.getColumnIndex("STATUS"));

                        lstagenda = new Agenda(Situacao, NomeContato, DATAAGEND, NumAgenda, Status);
                        DadosList.add(lstagenda);
                    } catch (Exception E) {
                        Toast.makeText(this, E.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                while (CursorAgenda.moveToNext());
                CursorAgenda.close();

            } else {
                if (!SitAgenda.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento do tipo " + NomeSitAgenda + " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!CodContato.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento do contato " + nomeContato + " encontrado!", Toast.LENGTH_LONG).show();
                } else if (!DtInicio.equals("0")) {
                    Toast.makeText(this, "Nenhum agendamento encontrado com o período de " + DtInicio + " até " + DtFinal + ".", Toast.LENGTH_LONG).show();
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
                            SitAgenda = "A";
                        } else if (NomeSitAgenda.equals("Finalizado")) {
                            SitAgenda = "F";
                        } else if (NomeSitAgenda.equals("Cancelado")) {
                            SitAgenda = "C";
                        }  else if (NomeSitAgenda.equals("Todos")) {
                            SitAgenda = "0";
                        }

                        try {
                            Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putInt(getString(R.string.intent_codcontato), 0);
                            params.putString(getString(R.string.intent_situacaoagenda), SitAgenda);
                            params.putString(getString(R.string.intent_nomesitagenda), NomeSitAgenda);
                            params.putString(getString(R.string.intent_datainicial), "0");
                            params.putString(getString(R.string.intent_datafinal), "0");

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
    }

    @Override
    public void onBackPressed() {
        if (!SitAgenda.equals("0")) {
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
                        SitAgenda = "0";
                        CodContato = "0";
                        DtInicio = "0";
                        DtFinal = "0";
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
                    SitAgenda = "0";
                    DtInicio = "0";
                    DtFinal = "0";
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putString(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);

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
                    SitAgenda = "0";
                    DtInicio = data.getExtras().getString(getString(R.string.intent_datainicial));
                    DtFinal = data.getExtras().getString(getString(R.string.intent_datafinal));
                    Intent intent = new Intent(ConsultaAgenda.this, ConsultaAgenda.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putString(getString(R.string.intent_situacaoagenda), SitAgenda);
                    params.putString(getString(R.string.intent_codcontato), CodContato);

                    intent.putExtras(params);
                    finish();
                    startActivity(intent);

                } catch (Exception E) {
                    //
                }
            }
            break;
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
        } catch (Exception e){
            e.toString();
        }

    }
}
