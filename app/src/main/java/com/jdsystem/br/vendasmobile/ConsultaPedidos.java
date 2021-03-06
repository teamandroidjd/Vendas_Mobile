package com.jdsystem.br.vendasmobile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.RateDialogManager;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.fragments.FragmentPedido;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsultaPedidos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public ListAdapterPedidos adapter;
    public SharedPreferences prefs;
    private Integer sitPed = 0;
    private String codClie = "0";
    private String DtInicio = "0";
    private String DtFinal = "0";
    private String codVendedor, URLPrincipal, usuario, senha, usuarioLogado, codEmpresa, nomeCliente, nomeSitPed;
    private ProgressDialog pDialog;
    SQLiteDatabase DB;
    FloatingActionMenu mmPrincPedido, mmPrincNovoPed;
    FloatingActionButton mmSitPedido, mmEmissaoPedido, mmCliePedido, mmNovoPedido;
    private int idPerfil;
    private Handler handler = new Handler();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_pedidos);
        declaraObjetos();
        setSupportActionBar(toolbar);
        if(Util.checarConexaoCelular(this)) {
            RateDialogManager.showRateDialog(this, savedInstanceState); // Avaliação do aplicativo.
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                sitPed = params.getInt(getString(R.string.intent_situacaopedido));
                codClie = params.getString(getString(R.string.intent_codcliente));
                DtInicio = params.getString(getString(R.string.intent_datainicial));
                DtFinal = params.getString(getString(R.string.intent_datafinal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                nomeCliente = params.getString(getString(R.string.intent_nomerazao));
                nomeSitPed = params.getString("tipositped");
            }
            if (DtInicio == null) {
                DtInicio = "0";
            }
            if (DtFinal == null) {
                DtFinal = "0";
            }
            if (codClie == null) {
                codClie = "0";
            }
        }
        carregaUsuarioLogado();
        carregaPreferencias();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
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

    public List<Pedidos> CarregarPedidos() {
        ArrayList<Pedidos> DadosList = new ArrayList<Pedidos>();
        Pedidos lstpedidos;
        LinearLayout lnenhum;
        lnenhum = (LinearLayout) findViewById(R.id.lnenhum);
        try {
            Cursor CursorPed = null;
            if (sitPed > 0) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.FLAGINTEGRADO = '" + sitPed + "' AND PEDOPER.CODPERFIL = " + idPerfil +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            } else if (!codClie.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.CODCLIE = " + codClie + " AND PEDOPER.CODPERFIL = " + idPerfil +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            } else if (!DtInicio.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.CODPERFIL, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + codVendedor + " AND PEDOPER.CODPERFIL = " + idPerfil + " AND (PEDOPER.DATAEMIS >= '" + DtInicio +
                        "' AND PEDOPER.DATAEMIS < '" + DtFinal + 1 + "')" +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);

            } else {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.CODPERFIL, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON (PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA) AND (PEDOPER.CODPERFIL = EMPRESAS.CODPERFIL)" +
                        " WHERE PEDOPER.CODVENDEDOR = " + Integer.parseInt(codVendedor) + " AND PEDOPER.CODPERFIL = " + idPerfil +
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

                        //String Vendedor = usuarioLogado;

                        String dataEmUmFormato = CursorPed.getString(CursorPed.getColumnIndex("DATAEMIS"));
                        dataEmUmFormato = Util.FormataDataDDMMAAAA_ComHoras(dataEmUmFormato);
                        /*SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                        Date data = formato.parse(dataEmUmFormato);
                        formato.applyPattern("dd/MM/yyyy");
                        String sDataVenda = formato.format(data);*/

                        lstpedidos = new Pedidos(Situacao, NomeCliente, ValorTotal, usuarioLogado, dataEmUmFormato, NumPedido, NumPedidoExt, NumFiscal, Empresa);
                        DadosList.add(lstpedidos);
                    } catch (Exception E) {
                        Toast.makeText(this, E.toString(), Toast.LENGTH_SHORT).show();

                    }
                }
                while (CursorPed.moveToNext());
                CursorPed.close();

            } else {
                if (sitPed > 0) {
                    Toast.makeText(this, "Nenhum pedido do tipo " + nomeSitPed + " encontrado!", Toast.LENGTH_LONG).show();
                    lnenhum.setVisibility(View.VISIBLE);
                } else if (!codClie.equals("0")) {
                    Toast.makeText(this, "Nenhum pedido do cliente " + nomeCliente + " encontrado!", Toast.LENGTH_LONG).show();
                    lnenhum.setVisibility(View.VISIBLE);
                } else if (!DtInicio.equals("0")) {
                    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                    Date dataInicio = formato.parse(DtInicio);
                    Date dataFim = formato.parse(DtFinal);
                    formato.applyPattern("dd/MM/yyyy");
                    String sDataInicio = formato.format(dataInicio);
                    String sDataFim = formato.format(dataFim);
                    DtInicio = sDataInicio;
                    DtFinal = sDataFim;
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

    public void novoPedido(View view) {

        codEmpresa = "0";
        try {
            Cursor CursEmpr = DB.rawQuery(" SELECT CODEMPRESA, CODPERFIL, NOMEABREV  FROM EMPRESAS WHERE ATIVO = 'S' AND CODPERFIL = " + idPerfil, null);
            CursEmpr.moveToFirst();
            if (CursEmpr.getCount() > 1) {
                List<String> DadosListEmpresa = new ArrayList<String>();
                do {
                    DadosListEmpresa.add(CursEmpr.getString(CursEmpr.getColumnIndex("NOMEABREV")));
                } while (CursEmpr.moveToNext());
                CursEmpr.close();

                @SuppressLint("InflateParams") View viewEmp = (LayoutInflater.from(ConsultaPedidos.this)).inflate(R.layout.input_empresa_corrente_pedido, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaPedidos.this);
                alertBuilder.setView(viewEmp);
                final Spinner spEmpresaInput = (Spinner) viewEmp.findViewById(R.id.spnEmpresa);

                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa).setDropDownViewResource(android.R.layout.simple_list_item_1);
                spEmpresaInput.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa));

                alertBuilder.setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String NomeEmpresa = spEmpresaInput.getSelectedItem().toString();
                                try {
                                    Cursor CursEmpr2 = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV, CODPERFIL FROM EMPRESAS WHERE NOMEABREV = '" + NomeEmpresa + "' AND CODPERFIL = " + idPerfil, null);
                                    CursEmpr2.moveToFirst();
                                    if (CursEmpr2.getCount() > 0) {
                                        codEmpresa = CursEmpr2.getString(CursEmpr2.getColumnIndex("CODEMPRESA"));
                                    }
                                    CursEmpr2.close();
                                    Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_telainvocada), "CadastroPedidos");
                                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                    params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putInt(getString(R.string.intent_flag), 2);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    ConsultaPedidos.this.finish();
                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }
                            }
                        });
                Dialog dialog = alertBuilder.create();

                Configuration configuration = getResources().getConfiguration();

                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                dialog.show();

            } else {
                codEmpresa = CursEmpr.getString(CursEmpr.getColumnIndex("CODEMPRESA"));
                Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_telainvocada), "CadastroPedidos");
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                intent.putExtras(params);
                startActivity(intent);
                ConsultaPedidos.this.finish();
            }
        } catch (Exception E) {
            E.toString();
        }

    }

    public void filtroSitPed(View view) {

        @SuppressLint("InflateParams") View viewSitPed = (LayoutInflater.from(ConsultaPedidos.this)).inflate(R.layout.input_filtro_situacao_pedido, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsultaPedidos.this);
        alertBuilder.setView(viewSitPed);
        final Spinner spSituacaoPedido = (Spinner) viewSitPed.findViewById(R.id.spnSitPedido);

        alertBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nomeSitPed = spSituacaoPedido.getSelectedItem().toString();

                        if (nomeSitPed.equals("Orçamento")) {
                            sitPed = 1;
                        } else if (nomeSitPed.equals("Cancelado")) {
                            sitPed = 4;
                        } else if (nomeSitPed.equals("Faturado")) {
                            sitPed = 3;
                        } else if (nomeSitPed.equals("Sincronizado")) {
                            sitPed = 2;
                        } else if (nomeSitPed.equals("Gerar Venda")) {
                            sitPed = 5;
                        } else if (nomeSitPed.equals("Todos")) {
                            sitPed = 0;
                        }
                        try {
                            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putInt(getString(R.string.intent_situacaopedido), sitPed);
                            params.putInt(getString(R.string.intent_codcliente), 0);
                            params.putString("tipositped", nomeSitPed);
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

    public void filtroEmissaoPed(View view) {
        Intent intent = new Intent(ConsultaPedidos.this, FiltroPeriodoPedidos.class);
        //finish();
        startActivityForResult(intent, 3);
    }

    public void filtroCliPed(View view) {
        Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString(getString(R.string.intent_telainvocada), "ConsultaPedidos");
        params.putBoolean(getString(R.string.intent_consultapedido), true);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (sitPed > 0) {
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_usuario), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (!codClie.equals("0")) {
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
                    if (Resultado) {
                        sitPed = 0;
                        codClie = "0";
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
                    codClie = data.getExtras().getString("CodCliente");
                    sitPed = 0;
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
                    params.putInt(getString(R.string.intent_situacaopedido), sitPed);
                    params.putString(getString(R.string.intent_codcliente), codClie);

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
                    codClie = "0";
                    sitPed = 0;
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
                    params.putInt(getString(R.string.intent_situacaopedido), sitPed);
                    params.putString(getString(R.string.intent_codcliente), codClie);

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
            Intent intent = new Intent(ConsultaPedidos.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putInt(getString(R.string.intent_flag),0);
            //params.putString(getString(R.string.intent_telainvocada),"ConsultaPedidos");
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

        } else if (id == R.id.nav_agenda) {
            Intent i = new Intent(ConsultaPedidos.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        }else if (id == R.id.nav_sincronismo) {
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
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.exit(1);
            finish();
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

        });
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    private void carregaPreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString(getString(R.string.intent_prefs_host), null);
        idPerfil = prefs.getInt(getString(R.string.intent_prefs_perfil), 0);
    }

    private void carregaUsuarioLogado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        usuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        usuariologado.setText("Olá " + usuarioLogado + "!");
    }

    private void declaraObjetos() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        GoogleApiClient client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        DB = new ConfigDB(this).getReadableDatabase();
        mmPrincNovoPed = (FloatingActionMenu) findViewById(R.id.mmPrincNovoPed);
        mmNovoPedido = (FloatingActionButton) findViewById(R.id.mmNovoPedido);
        mmPrincPedido = (FloatingActionMenu) findViewById(R.id.mmPrinc_Pedido);
        mmSitPedido = (FloatingActionButton) findViewById(R.id.mmSitPedido);
        mmEmissaoPedido = (FloatingActionButton) findViewById(R.id.mmEmissaoPedido);
        mmCliePedido = (FloatingActionButton) findViewById(R.id.mmCliePedido);

    }
}
