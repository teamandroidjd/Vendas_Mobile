package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.fragments.FragmentPedido;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import android.support.design.widget.FloatingActionButton;

public class ConsultaPedidos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private Handler handler = new Handler();
    Integer SitPed = 0;
    String CodClie = "0";
    String DtInicio = "0";
    String DtFinal = "0";
    String codVendedor, URLPrincipal, usuario, senha, UsuarioLogado, sCodEmpresa, nomeCliente, NomeSitPed;
    public ListAdapterPedidos adapter;
    ProgressDialog pDialog;
    //SearchView sv;
    Pedidos lstpedidos;
    LinearLayout lnenhum;
    SQLiteDatabase DB;
    //private Context ctx;
    //private AlertDialog dlg;
    private GoogleApiClient client;
    FloatingActionMenu mmPrinc_Pedido, mmPrincNovoPed;
    FloatingActionButton mmSitPedido, mmEmissaoPedido, mmCliePedido, mmNovoPedido;
    public SharedPreferences prefs;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    int idPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_listpedidos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                SitPed = params.getInt(getString(R.string.intent_situacaopedido));
                CodClie = params.getString(getString(R.string.intent_codcliente));
                DtInicio = params.getString(getString(R.string.intent_datainicial));
                DtFinal = params.getString(getString(R.string.intent_datafinal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                nomeCliente = params.getString(getString(R.string.intent_nomerazao));
                NomeSitPed = params.getString("tipositped");
            }
            if (DtInicio == null) {
                DtInicio = "0";
            }
            if (DtFinal == null) {
                DtFinal = "0";
            }
            if (CodClie == null) {
                CodClie = "0";
            }
        }
        declaraobjetos();
        carregausuariologado();
        carregarpreferencias();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pDialog = new ProgressDialog(ConsultaPedidos.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.loading_orders));
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(ConsultaPedidos.this);
        thread.start();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
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
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        lnenhum = (LinearLayout) findViewById(R.id.lnenhum);
        DB = new ConfigDB(this).getReadableDatabase();
        mmPrincNovoPed = (FloatingActionMenu) findViewById(R.id.mmPrincNovoPed);
        mmNovoPedido = (FloatingActionButton) findViewById(R.id.mmNovoPedido);
        mmPrinc_Pedido = (FloatingActionMenu) findViewById(R.id.mmPrinc_Pedido);
        mmSitPedido = (FloatingActionButton) findViewById(R.id.mmSitPedido);
        mmEmissaoPedido = (FloatingActionButton) findViewById(R.id.mmEmissaoPedido);
        mmCliePedido = (FloatingActionButton) findViewById(R.id.mmCliePedido);
    }

    public void novopedido(View view) {
        Configuration configuration = getResources().getConfiguration();

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        sCodEmpresa = "0";
        try {
            Cursor CursEmpr = DB.rawQuery(" SELECT CODEMPRESA, CODPERFIL, NOMEABREV  FROM EMPRESAS WHERE ATIVO = 'S' AND CODPERFIL = "+idPerfil, null);
            CursEmpr.moveToFirst();
            if (CursEmpr.getCount() > 1) {
                List<String> DadosListEmpresa = new ArrayList<String>();
                do {
                    DadosListEmpresa.add(CursEmpr.getString(CursEmpr.getColumnIndex("NOMEABREV")));
                } while (CursEmpr.moveToNext());

                View viewEmp = (LayoutInflater.from(ConsultaPedidos.this)).inflate(R.layout.input_empresa_corrente_pedido, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaPedidos.this);
                alertBuilder.setView(viewEmp);
                final Spinner spEmpresaInput = (Spinner) viewEmp.findViewById(R.id.spnEmpresa);

                ArrayAdapter<String> arrayEmpresa = new ArrayAdapter<String>(ConsultaPedidos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa);
                ArrayAdapter<String> spArrayEmpresa = arrayEmpresa;
                spArrayEmpresa.setDropDownViewResource(android.R.layout.simple_list_item_1);
                spEmpresaInput.setAdapter(spArrayEmpresa);

                alertBuilder.setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String NomeEmpresa = spEmpresaInput.getSelectedItem().toString();
                                try {
                                    Cursor CursEmpr2 = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE NOMEABREV = '" + NomeEmpresa + "'", null);
                                    CursEmpr2.moveToFirst();
                                    if (CursEmpr2.getCount() > 0) {
                                        sCodEmpresa = CursEmpr2.getString(CursEmpr2.getColumnIndex("CODEMPRESA"));
                                    }
                                    CursEmpr2.close();
                                    Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    params.putString(getString(R.string.intent_codigoempresa), sCodEmpresa);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putInt(getString(R.string.intent_flag), 2);
                                    intent.putExtras(params);
                                    startActivityForResult(intent, 1);
                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }
                            }
                        });
                Dialog dialog = alertBuilder.create();
                dialog.show();

            } else {
                sCodEmpresa = CursEmpr.getString(CursEmpr.getColumnIndex("CODEMPRESA"));
                Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
                Bundle params = new Bundle();
                params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_codigoempresa), sCodEmpresa);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                intent.putExtras(params);
                startActivityForResult(intent, 1);
            }
        } catch (Exception E) {
            E.toString();
        }

    }

    public void filtrositped(View view) {

        View viewSitPed = (LayoutInflater.from(ConsultaPedidos.this)).inflate(R.layout.input_filtro_situacao_pedido, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaPedidos.this);
        alertBuilder.setView(viewSitPed);
        final Spinner spSituacaoPedido = (Spinner) viewSitPed.findViewById(R.id.spnSitPedido);

        alertBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NomeSitPed = spSituacaoPedido.getSelectedItem().toString();

                        if (NomeSitPed.equals("Orçamento")) {
                            SitPed = 1;
                        } else if (NomeSitPed.equals("Cancelado")) {
                            SitPed = 4;
                        } else if (NomeSitPed.equals("Faturado")) {
                            SitPed = 3;
                        } else if (NomeSitPed.equals("Sincronizado")) {
                            SitPed = 2;
                        } else if (NomeSitPed.equals("Gerar Venda")) {
                            SitPed = 5;
                        } else if (NomeSitPed.equals("Todos")) {
                            SitPed = 0;
                        }
                        try {
                            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putInt(getString(R.string.intent_situacaopedido), SitPed);
                            params.putInt(getString(R.string.intent_codcliente), 0);
                            params.putString("tipositped",NomeSitPed);
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

    public void filtroemissaoped(View view) {
        Intent intent = new Intent(ConsultaPedidos.this, actFiltroPeriodoPedidos.class);
        //finish();
        startActivityForResult(intent, 3);
    }

    public void filtrocliped(View view) {
        Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString("TELA_QUE_CHAMOU", "ConsultaPedidos");
        params.putBoolean(getString(R.string.intent_consultapedido), true);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (SitPed > 0) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_usuario), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!CodClie.equals("0")) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!DtInicio.equals("0")) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
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
                    if (Resultado == true) {
                        SitPed = 0;
                        CodClie = "0";
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
                    CodClie = data.getExtras().getString("CodCliente");
                    SitPed = 0;
                    DtInicio = "0";
                    DtFinal = "0";
                    Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaopedido), SitPed);
                    params.putString(getString(R.string.intent_codcliente), CodClie);

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
                    CodClie = "0";
                    SitPed = 0;
                    DtInicio = data.getExtras().getString(getString(R.string.intent_datainicial));
                    DtFinal = data.getExtras().getString(getString(R.string.intent_datafinal));
                    Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_datainicial), DtInicio);
                    params.putString(getString(R.string.intent_datafinal), DtFinal);
                    params.putInt(getString(R.string.intent_situacaopedido), SitPed);
                    params.putString(getString(R.string.intent_codcliente), CodClie);

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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            //params.putBoolean("fazpedido", false);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_pedidos) {

        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaPedidos.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent intent = new Intent(ConsultaPedidos.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(ConsultaPedidos.this, Login.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            System.exit(1);
        } else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(ConsultaPedidos.this, InfoJDSystem.class);
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
                    FragmentPedido frag = (FragmentPedido) getSupportFragmentManager().findFragmentByTag("mainFrag");
                    if (frag == null) {
                        frag = new FragmentPedido();
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

            ;

        });
    }

    public List<Pedidos> CarregarPedidos() {
        ArrayList<Pedidos> DadosList = new ArrayList<Pedidos>();
        try {
            Cursor CursorPed = null;
            if (SitPed > 0) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.FLAGINTEGRADO = '" + SitPed + "' AND PEDOPER.CODPERFIL = " +idPerfil+
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            } else if (!CodClie.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.CODCLIE = " + CodClie + " AND PEDOPER.CODPERFIL = " +idPerfil+
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            } else if (!DtInicio.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.CODPERFIL = "+idPerfil+" AND (PEDOPER.DATAEMIS >= '" + DtInicio + "' AND DATAEMIS < '" + DtFinal + 1 + "')" +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);

            } else {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.CODPERFIL, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + Integer.parseInt(codVendedor) +" AND PEDOPER.CODPERFIL = " +idPerfil+
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            }

            String Situacao = null;
            if (CursorPed.getCount() > 0) {
                lnenhum.setVisibility(View.GONE);
                CursorPed.moveToFirst();
                do {
                    try {
                        String SitPed = CursorPed.getString(CursorPed.getColumnIndex("FLAGINTEGRADO"));
                        if (SitPed.equals("1")) {
                            Situacao = "Orçamento";
                        }
                        if (SitPed.equals("2")) {
                            Situacao = "#";
                        }
                        if (SitPed.equals("3")) {
                            Situacao = "Faturado";
                        }
                        if (SitPed.equals("4")) {
                            Situacao = "Cancelado";
                        }
                        if (SitPed.equals("5")) {
                            Situacao = "Gerar Venda";
                        }
                        String NomeCliente = CursorPed.getString(CursorPed.getColumnIndex("NOMECLIE"));

                        Double VlTotal = (CursorPed.getDouble(CursorPed.getColumnIndex("VALORTOTAL")) -
                                CursorPed.getDouble(CursorPed.getColumnIndex("VLPERCACRES")));

                        String valor = String.valueOf(VlTotal);
                        java.math.BigDecimal venda = new java.math.BigDecimal(Double.parseDouble(valor.replace(',', '.')));
                        String ValorTotal = venda.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                        ValorTotal = ValorTotal.replace('.', ',');

                        String NumPedido = CursorPed.getString(CursorPed.getColumnIndex("NUMPED"));
                        String NumPedidoExt = CursorPed.getString(CursorPed.getColumnIndex("NUMPEDIDOERP"));
                        String NumFiscal = CursorPed.getString(CursorPed.getColumnIndex("NUMFISCAL"));

                        String Empresa = CursorPed.getString(CursorPed.getColumnIndex("NOMEABREV"));

                        String Vendedor = UsuarioLogado;

                        String dataEmUmFormato = CursorPed.getString(CursorPed.getColumnIndex("DATAEMIS"));
                        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                        Date data = formato.parse(dataEmUmFormato);
                        formato.applyPattern("dd/MM/yyyy");
                        String sDataVenda = formato.format(data);

                        lstpedidos = new Pedidos(Situacao, NomeCliente, ValorTotal, Vendedor, sDataVenda, NumPedido, NumPedidoExt, NumFiscal, Empresa);
                        DadosList.add(lstpedidos);
                    } catch (Exception E) {
                        Toast.makeText(this, E.toString(), Toast.LENGTH_SHORT).show();

                    }
                }
                while (CursorPed.moveToNext());
                CursorPed.close();

            } else {
                if (SitPed > 0) {
                    Toast.makeText(this, "Nenhum pedido do tipo " + NomeSitPed + " encontrado!", Toast.LENGTH_LONG).show();
                    lnenhum.setVisibility(View.VISIBLE);
                } else if (!CodClie.equals("0")) {
                    Toast.makeText(this, "Nenhum pedido do cliente " + nomeCliente + " encontrado!", Toast.LENGTH_LONG).show();
                    lnenhum.setVisibility(View.VISIBLE);
                } else if (!DtInicio.equals("0")) {
                    Toast.makeText(this, "Nenhum pedido encontrado com o período de " + DtInicio + " até " + DtFinal + ".", Toast.LENGTH_LONG).show();
                    lnenhum.setVisibility(View.VISIBLE);
                } else {
                    lnenhum.setVisibility(View.VISIBLE);
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
}
