package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.v7.widget.SearchView;
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
import com.jdsystem.br.vendasmobile.Controller.Lista_clientes;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.fragments.FragmentPedido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import android.support.design.widget.FloatingActionButton;

public class actListPedidos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    ProgressDialog pDialog;
    private Handler handler = new Handler();
    public ListAdapterPedidos adapter;
    SearchView sv;
    Pedidos lstpedidos;
    String UsuarioLogado;
    LinearLayout lnenhum;

    String sCodVend, URLPrincipal;
    SQLiteDatabase DB;
    private Context ctx;
    private AlertDialog dlg;

    private GoogleApiClient client;
    private static String sCodEmpresa;

    Integer SitPed = 0;
    String CodClie = "0";
    String DtInicio = "0";
    String DtFinal = "0";


    FloatingActionMenu mmPrinc_Pedido, mmPrincNovoPed;
    FloatingActionButton mmSitPedido, mmEmissaoPedido, mmCliePedido, mmNovoPedido;


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
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                SitPed = params.getInt("SitPedido");
                CodClie = params.getString("CodCliente");
                DtInicio = params.getString("datainicial");
                DtFinal = params.getString("datafinal");
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
        try {
            mmPrinc_Pedido = (FloatingActionMenu) findViewById(R.id.mmPrinc_Pedido);
            mmSitPedido = (FloatingActionButton) findViewById(R.id.mmSitPedido);
            mmEmissaoPedido = (FloatingActionButton) findViewById(R.id.mmEmissaoPedido);
            mmCliePedido = (FloatingActionButton) findViewById(R.id.mmCliePedido);

            mmSitPedido.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View v) {
                                                   View viewSitPed = (LayoutInflater.from(actListPedidos.this)).inflate(R.layout.input_filtro_situacao_pedido, null);

                                                   AlertDialog.Builder alertBuilder = new AlertDialog.Builder(actListPedidos.this);
                                                   alertBuilder.setView(viewSitPed);
                                                   final Spinner spSituacaoPedido = (Spinner) viewSitPed.findViewById(R.id.spnSitPedido);

                                                   alertBuilder.setCancelable(true)
                                                           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                               @Override
                                                               public void onClick(DialogInterface dialog, int which) {
                                                                   String NomeSitPed = spSituacaoPedido.getSelectedItem().toString();

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
                                                                       Intent intent = new Intent(actListPedidos.this, actListPedidos.class);
                                                                       Bundle params = new Bundle();
                                                                       params.putString("codvendedor", sCodVend);
                                                                       params.putString("urlPrincipal", URLPrincipal);
                                                                       params.putInt("SitPedido", SitPed);
                                                                       params.putInt("codclie", 0);
                                                                       params.putString("datainicial", "0");
                                                                       params.putString("datafinal", "0");

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
                                           }


            );
            mmEmissaoPedido.setOnClickListener(new View.OnClickListener() {
                                                   public void onClick(View v) {
                                                       Intent intent = new Intent(actListPedidos.this, actFiltroPeriodoPedidos.class);
                                                       //finish();
                                                       startActivityForResult(intent, 3);
                                                   }
                                               }

            );
            mmCliePedido.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(actListPedidos.this, act_ListClientes.class);
                                                    Bundle params = new Bundle();
                                                    params.putString("codvendedor", sCodVend);
                                                    params.putString("urlPrincipal", URLPrincipal);
                                                    params.putBoolean("consultapedido", true);
                                                    intent.putExtras(params);
                                                    //finish();
                                                    startActivityForResult(intent, 2);
                                                }
                                            }

            );
        } catch (Exception e) {
            e.toString();
        }


        FragmentPedido frag = (FragmentPedido) getSupportFragmentManager().findFragmentByTag("mainFrag");
        if (frag == null) {
            frag = new FragmentPedido();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rl_fragment_container, frag, "mainFrag");
            ft.commit();
        }

        DB = new ConfigDB(this).getReadableDatabase();


        try {
            mmPrincNovoPed = (FloatingActionMenu) findViewById(R.id.mmPrincNovoPed);
            mmNovoPedido = (FloatingActionButton) findViewById(R.id.mmNovoPedido);

            mmNovoPedido.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sCodEmpresa = "0";
                    try {
                        Cursor CursEmpr = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE ATIVO = 'S' ", null);
                        CursEmpr.moveToFirst();
                        if (CursEmpr.getCount() > 1) {
                            List<String> DadosListEmpresa = new ArrayList<String>();
                            do {
                                DadosListEmpresa.add(CursEmpr.getString(CursEmpr.getColumnIndex("NOMEABREV")));
                            } while (CursEmpr.moveToNext());

                            View viewEmp = (LayoutInflater.from(actListPedidos.this)).inflate(R.layout.input_empresa_corrente_pedido, null);

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(actListPedidos.this);
                            alertBuilder.setView(viewEmp);
                            final Spinner spEmpresaInput = (Spinner) viewEmp.findViewById(R.id.spnEmpresa);

                            ArrayAdapter<String> arrayEmpresa = new ArrayAdapter<String>(actListPedidos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa);
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
                                                Intent intent = new Intent(actListPedidos.this, Lista_clientes.class);
                                                Bundle params = new Bundle();
                                                params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
                                                params.putString("CodVendedor", sCodVend);
                                                params.putString("codempresa", sCodEmpresa);
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
                            Intent intent = new Intent(actListPedidos.this, Lista_clientes.class);
                            Bundle params = new Bundle();
                            params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
                            params.putString("CodVendedor", sCodVend);
                            params.putString("codempresa", sCodEmpresa);
                            intent.putExtras(params);
                            startActivityForResult(intent, 1);
                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                }
            });

        } catch (Exception e) {
            e.toString();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        UsuarioLogado = prefs.getString("usuario", null);
        usuariologado.setText(UsuarioLogado);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        pDialog = new ProgressDialog(actListPedidos.this);
        pDialog.setTitle("Aguarde...");
        pDialog.setMessage("Carregando Pedidos");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(actListPedidos.this);
        thread.start();

        lnenhum = (LinearLayout) findViewById(R.id.lnenhum);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                    Intent intent = new Intent(actListPedidos.this, actListPedidos.class);
                    Bundle params = new Bundle();
                    params.putString("codvendedor", sCodVend);
                    params.putString("urlPrincipal", URLPrincipal);
                    params.putString("datainicial", DtInicio);
                    params.putString("datafinal", DtFinal);
                    params.putInt("SitPedido", SitPed);
                    params.putString("CodCliente", CodClie);

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
                    DtInicio = data.getExtras().getString("datainicial");
                    DtFinal = data.getExtras().getString("datafinal");
                    Intent intent = new Intent(actListPedidos.this, actListPedidos.class);
                    Bundle params = new Bundle();
                    params.putString("codvendedor", sCodVend);
                    params.putString("urlPrincipal", URLPrincipal);
                    params.putString("datainicial", DtInicio);
                    params.putString("datafinal", DtFinal);
                    params.putInt("SitPedido", SitPed);
                    params.putString("CodCliente", CodClie);

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
            Intent intent = new Intent(actListPedidos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putBoolean("fazpedido", false);
            intent.putExtras(params);
            startActivityForResult(intent, 1);

        } else if (id == R.id.nav_pedidos) {

        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(actListPedidos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            //finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent intent = new Intent(actListPedidos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
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
                    CarregarPedidos();
                } catch (Exception E) {

                } finally {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
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
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA" +
                        " WHERE PEDOPER.CODVENDEDOR = " + sCodVend + " AND PEDOPER.FLAGINTEGRADO = '" + SitPed + "'" +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            } else if (!CodClie.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA" +
                        " WHERE PEDOPER.CODVENDEDOR = " + sCodVend + " AND PEDOPER.CODCLIE = " + CodClie +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);
            }else if (!DtInicio.equals("0")) {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA" +
                        " WHERE PEDOPER.CODVENDEDOR = " + sCodVend + " AND (PEDOPER.DATAEMIS BETWEEN '" + DtInicio + "' AND '" + DtFinal + "')" +
                        " ORDER BY PEDOPER.DATAEMIS DESC ", null);

            } else {
                CursorPed = DB.rawQuery(" SELECT EMPRESAS.NOMEABREV, PEDOPER.NUMPED, PEDOPER.DATAEMIS, PEDOPER.NOMECLIE, PEDOPER.VALORTOTAL, PEDOPER.STATUS, " +
                        " PEDOPER.FLAGINTEGRADO, PEDOPER.NUMPEDIDOERP, PEDOPER.NUMFISCAL, PEDOPER.VLPERCACRES FROM PEDOPER LEFT OUTER JOIN" +
                        " EMPRESAS ON PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA" +
                        " WHERE PEDOPER.CODVENDEDOR = " + sCodVend +
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
                        String ValorTotal = venda.setScale(2, java.math.BigDecimal.ROUND_UP).toString();
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
                        Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();

                    }
                }
                while (CursorPed.moveToNext());
                CursorPed.close();

            } else {
                lnenhum.setVisibility(View.VISIBLE);
            }


        } catch (Exception E) {
            E.toString();
        }
        return DadosList;
    }


}
