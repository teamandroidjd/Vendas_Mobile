package com.jdsystem.br.vendasmobile;


import android.app.AlertDialog;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.fragments.FragmentProdutos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ConsultaProdutos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {


    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public SharedPreferences prefs;
    private String codVendedor, URLPrincipal, usuario, senha, usuarioLogado, sincProd, numPedido, chavePedido,
            nomeCliente, editQuery, telaInvocada, nomeEmpresa;
    private SQLiteDatabase DB;
    private int flag = 0, codCliente, codContato, idPerfil, CadastroContato;
    Produtos lstprodutos;
    Handler handler = new Handler();
    MenuItem searchItem;
    SearchView searchView;
    private TextView txvqtdregprod;
    private ProgressDialog dialog, dialogECB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                numPedido = params.getString(getString(R.string.intent_numpedido));
                chavePedido = params.getString(getString(R.string.intent_chavepedido));
                flag = params.getInt(getString(R.string.intent_flag));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
                CadastroContato = params.getInt(getString(R.string.intent_cad_contato), 0);
                codCliente = params.getInt(getString(R.string.intent_codcliente));
                codContato = params.getInt(getString(R.string.intent_codcontato));
                nomeCliente = params.getString(getString(R.string.intent_nomerazao));
                //Pedido = params.getBoolean("pedido");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        carregaUsuarioLogado();
        carregaPreferencias();
        declaraObjetos();

        dialog = new ProgressDialog(ConsultaProdutos.this);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.wait));
        dialog.setMessage(getString(R.string.loading_products));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        Thread thread = new Thread(ConsultaProdutos.this);
        thread.start();
    }

    public List<Produtos> carregarprodutos() {
        ArrayList<Produtos> DadosLisProdutos = new ArrayList<Produtos>();
        if (editQuery == null) {
            BigDecimal preco1 = null;
            BigDecimal preco2 = null;
            BigDecimal preco3 = null;
            BigDecimal preco4 = null;
            BigDecimal preco5 = null;
            BigDecimal precoP1 = null;
            BigDecimal precoP2 = null;

            try {
                Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABCONTROLQTDMINVEND, CODPERFIL, HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP WHERE CODPERFIL =" + idPerfil, null);
                CursorParametro.moveToFirst();
                String habcontrolqtdmin = CursorParametro.getString(CursorParametro.getColumnIndex("HABCONTROLQTDMINVEND"));
                String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));
                if (tabela1.equals("") && tabela1.equals("") && tabela1.equals("") && tabela1.equals("") && tabela1.equals("") &&
                        tabela1.equals("") && tabela1.equals("") && tipoEstoque == null) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setCancelable(false);
                    alert.setIcon(R.drawable.logo_ico);
                    alert.setTitle("Atenção!");
                    alert.setMessage("Não foi possível realizar a consulta de produtos, pois não foram configurado os parâmetros. Entre em contato com a " + nomeEmpresa + ".");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    alert.show();
                    return DadosLisProdutos;
                }

                Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS WHERE ((ATIVO = 'S') AND (CODPERFIL = " + idPerfil + ")) ORDER BY DESCRICAO", null);
                cursorProdutos.moveToFirst();
                if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {
                    txvqtdregprod.setText("Quantidade de registro: " + cursorProdutos.getCount());
                    do {
                        int codigoexterno = cursorProdutos.getInt(cursorProdutos.getColumnIndex("CODIGOITEM"));
                        String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                        String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                        String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                        String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                        String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                        String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));
                        int codInterno = cursorProdutos.getInt(cursorProdutos.getColumnIndex("CODIGOITEM"));
                        String taPadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("TABELAPADRAO"));
                        String qtdminvend = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDMINVEND"));
                  /*ppadrao = ppadrao.trim();
                    BigDecimal precopadrao = new BigDecimal(Double.parseDouble(ppadrao.replace(',', '.')));*/

                        if (!tabela1.equals("")) {
                            String p1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA1"));
                            p1 = p1.trim();
                            if (!p1.equals("")) {
                                preco1 = new BigDecimal(Double.parseDouble(p1.replace(',', '.')));
                            }
                        } else {
                            tabela1 = "";
                        }
                        if (!tabela2.equals("")) {
                            String p2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA2"));
                            p2 = p2.trim();
                            if (!p2.equals("")) {
                                preco2 = new BigDecimal(Double.parseDouble(p2.replace(',', '.')));
                            }
                        } else {
                            tabela2 = "";
                        }
                        if (!tabela3.equals("")) {
                            String p3 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA3"));
                            p3 = p3.trim();
                            if (!p3.equals("")) {
                                preco3 = new BigDecimal(Double.parseDouble(p3.replace(',', '.')));
                            }
                        } else {
                            tabela3 = "";
                        }
                        if (!tabela4.equals("")) {
                            String p4 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA4"));
                            p4 = p4.trim();
                            if (!p4.equals("")) {
                                preco4 = new BigDecimal(Double.parseDouble(p4.replace(',', '.')));
                            }
                        } else {
                            tabela4 = "";
                        }
                        if (!tabela5.equals("")) {
                            String p5 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA5"));
                            p5 = p5.trim();
                            if (!p5.equals("")) {
                                preco5 = new BigDecimal(Double.parseDouble(p5.replace(',', '.')));
                            }
                        } else {
                            tabela5 = "";
                        }
                        if (!tabpromo1.equals("")) {
                            String pp1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP1"));
                            pp1 = pp1.trim();
                            if (!pp1.equals("")) {
                                precoP1 = new BigDecimal(Double.parseDouble(pp1.replace(',', '.')));
                            }
                        } else {
                            tabpromo1 = "";
                        }
                        if (!tabpromo2.equals("")) {
                            String pp2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP2"));
                            pp2 = pp2.trim();
                            if (!pp2.equals("")) {
                                precoP2 = new BigDecimal(Double.parseDouble(pp2.replace(',', '.')));
                            }
                        } else {
                            tabpromo2 = "";
                        }
                        lstprodutos = new Produtos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5,
                                precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, taPadrao,
                                codigoexterno, habcontrolqtdmin, qtdminvend);
                        DadosLisProdutos.add(lstprodutos);
                    } while (cursorProdutos.moveToNext());
                    cursorProdutos.close();
                    CursorParametro.close();

                } else {
                    txvqtdregprod.setText("Quantidade de Registro: 0");
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaProdutos.this);
                    builder.setTitle(R.string.app_namesair);
                    builder.setIcon(R.drawable.logo_ico);
                    builder.setMessage(R.string.alertsyncproducts)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                e.toString();
                Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
            }
        } else {
            BigDecimal preco1 = null;
            BigDecimal preco2 = null;
            BigDecimal preco3 = null;
            BigDecimal preco4 = null;
            BigDecimal preco5 = null;
            BigDecimal precoP1 = null;
            BigDecimal precoP2 = null;

            try {

                Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABCONTROLQTDMINVEND, CODPERFIL, HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP WHERE CODPERFIL =" + idPerfil, null);
                CursorParametro.moveToFirst();
                String habcontrolqtdmin = CursorParametro.getString(CursorParametro.getColumnIndex("HABCONTROLQTDMINVEND"));
                String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));

                Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS WHERE ((ATIVO = 'S') AND (CODPERFIL = " + idPerfil + ")) AND ((DESCRICAO LIKE '%" + editQuery + "%') OR (CODITEMANUAL LIKE '%" + editQuery + "%') OR (CLASSE LIKE '%" + editQuery + "%')" +
                        " OR (FABRICANTE LIKE '%" + editQuery + "%') OR (FORNECEDOR LIKE '%" + editQuery + "%') OR (MARCA LIKE '%" + editQuery + "%')) ORDER BY DESCRICAO", null);
                cursorProdutos.moveToFirst();
                if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {
                    txvqtdregprod.setText("Quantidade de registro: " + cursorProdutos.getCount());
                    do {
                        int codigoexterno = cursorProdutos.getInt(cursorProdutos.getColumnIndex("CODIGOITEM"));
                        String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                        String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                        String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                        String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                        String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                        String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));
                        int codInterno = cursorProdutos.getInt(cursorProdutos.getColumnIndex("CODIGOITEM"));
                        String taPadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("TABELAPADRAO"));
                        String qtdminvend = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDMINVEND"));
                  /*ppadrao = ppadrao.trim();
                    BigDecimal precopadrao = new BigDecimal(Double.parseDouble(ppadrao.replace(',', '.')));*/

                        if (!tabela1.equals("")) {
                            String p1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA1"));
                            p1 = p1.trim();
                            if (!p1.equals("")) {
                                preco1 = new BigDecimal(Double.parseDouble(p1.replace(',', '.')));
                            }
                        }
                        if (!tabela2.equals("")) {
                            String p2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA2"));
                            p2 = p2.trim();
                            if (!p2.equals("")) {
                                preco2 = new BigDecimal(Double.parseDouble(p2.replace(',', '.')));
                            }
                        }
                        if (!tabela3.equals("")) {
                            String p3 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA3"));
                            p3 = p3.trim();
                            if (!p3.equals("")) {
                                preco3 = new BigDecimal(Double.parseDouble(p3.replace(',', '.')));
                            }
                        }
                        if (!tabela4.equals("")) {
                            String p4 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA4"));
                            p4 = p4.trim();
                            if (!p4.equals("")) {
                                preco4 = new BigDecimal(Double.parseDouble(p4.replace(',', '.')));
                            }
                        }
                        if (!tabela5.equals("")) {
                            String p5 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA5"));
                            p5 = p5.trim();
                            if (!p5.equals("")) {
                                preco5 = new BigDecimal(Double.parseDouble(p5.replace(',', '.')));
                            }
                        }
                        if (!tabpromo1.equals("")) {
                            String pp1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP1"));
                            pp1 = pp1.trim();
                            if (!pp1.equals("")) {
                                precoP1 = new BigDecimal(Double.parseDouble(pp1.replace(',', '.')));
                            }
                        }
                        if (!tabpromo2.equals("")) {
                            String pp2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP2"));
                            pp2 = pp2.trim();
                            if (!pp2.equals("")) {
                                precoP2 = new BigDecimal(Double.parseDouble(pp2.replace(',', '.')));
                            }
                        }

                        lstprodutos = new Produtos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5,
                                precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, taPadrao,
                                codigoexterno, habcontrolqtdmin, qtdminvend);
                        DadosLisProdutos.add(lstprodutos);
                    } while (cursorProdutos.moveToNext());
                    cursorProdutos.close();
                    CursorParametro.close();

                } else {
                    txvqtdregprod.setText("Quantidade de Registro: 0");
                    Toast.makeText(this, R.string.no_products_found, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.toString();

            }
        }
        if (dialog.isShowing())
            dialog.dismiss();

        return DadosLisProdutos;
    }

    private void declaraObjetos() {
        DB = new ConfigDB(this).getReadableDatabase();
        txvqtdregprod = (TextView) findViewById(R.id.txvqtdregistroprod);
    }

    private void carregaPreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        nomeEmpresa = prefs.getString("nome", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    private void carregaUsuarioLogado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        usuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        if (usuarioLogado != null) {
            usuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
            usuariologado.setText("Olá " + usuarioLogado + "!");
        } else {
            usuariologado.setText("Olá " + usuario + "!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_produto, menu);
        searchItem = menu.findItem(R.id.action_searchable_activity);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                dialog = new ProgressDialog(ConsultaProdutos.this);
                dialog.setIndeterminate(true);
                dialog.setTitle(getString(R.string.wait));
                dialog.setMessage(getString(R.string.searchingproduct));
                dialog.setCancelable(false);
                dialog.setProgress(0);
                dialog.show();

                query.toString();
                editQuery = query;
                searchView.setQuery("", false);
                //searchView.clearFocus();
                if (flag != 0)
                    flag = 2;

                Thread thread = new Thread(ConsultaProdutos.this);
                thread.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                //flag = 2;
                ConsultaProdutos.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                editQuery = null;
                searchView.onActionViewCollapsed();
                dialog = new ProgressDialog(ConsultaProdutos.this);
                dialog.setCancelable(false);
                dialog.setTitle(getString(R.string.wait));
                dialog.setMessage(getString(R.string.loading_products));
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();

                Thread thread = new Thread(ConsultaProdutos.this);
                thread.start();

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sinc_produto) {
            Boolean ConexOk = Util.checarConexaoCelular(ConsultaProdutos.this);
            if (ConexOk) {
                try {
                    Cursor cursorVerificaProd = DB.rawQuery("SELECT * FROM ITENS WHERE CODPERFIL = " + idPerfil, null);
                    if (cursorVerificaProd.getCount() == 0) {
                        cursorVerificaProd.close();
                        flag = 1;
                        dialog = new ProgressDialog(ConsultaProdutos.this);
                        dialog.setCancelable(false);
                        dialog.setMessage(getString(R.string.primeira_sync_itens));
                        dialog.setTitle(getString(R.string.wait));
                        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        dialog.setProgress(0);
                        dialog.setMax(0);
                        dialog.setIcon(R.drawable.icon_sync);
                        dialog.show();

                        dialogECB = new ProgressDialog(ConsultaProdutos.this);

                        new Thread(ConsultaProdutos.this).start();

                    } else {
                        cursorVerificaProd.close();
                        flag = 1;
                        dialog = new ProgressDialog(ConsultaProdutos.this);
                        dialog.setCancelable(false);
                        dialog.setMessage(getString(R.string.sync_products));
                        dialog.setTitle(getString(R.string.wait));
                        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        dialog.setProgress(0);
                        dialog.setMax(0);
                        dialog.setIcon(R.drawable.icon_sync);
                        dialog.show();

                        Thread thread = new Thread(this);
                        thread.start();
                    }
                } catch (Exception e) {
                    e.toString();
                }

            } else {
                Toast.makeText(ConsultaProdutos.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void run() {
        if (flag == 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentProdutos frag = (FragmentProdutos) getSupportFragmentManager().findFragmentByTag("mainFragA");
                        Bundle params = new Bundle();
                        if (frag != null) {
                            frag = null;
                        }
                        frag = new FragmentProdutos();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.rl_fragment_container, frag, "mainFragA");
                        params.putInt(getString(R.string.intent_flag), flag);
                        params.putString(getString(R.string.intent_numpedido), numPedido);
                        params.putString(getString(R.string.intent_chavepedido), chavePedido);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                        params.putInt(getString(R.string.intent_codcliente), codCliente);
                        params.putInt(getString(R.string.intent_codcontato), codContato);
                        params.putString(getString(R.string.intent_nomerazao), nomeCliente);
                        params.putInt(getString(R.string.intent_codcontato), codContato);
                        frag.setArguments(params);
                        ft.commit();

                    } catch (Exception e) {
                        e.toString();
                    }
                }
            });
        } else if (flag == 1) {
            try {
                sincProd = Sincronismo.sincronizaProdutos(ConsultaProdutos.this, usuario, senha, 0, dialog, dialogECB, handler);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), sincProd, Toast.LENGTH_LONG).show();
                    }
                });
                if (numPedido == null) {
                    Intent intent = (ConsultaProdutos.this).getIntent();
                    (ConsultaProdutos.this).finish();
                    startActivity(intent);
                } else {
                    Intent intent = (ConsultaProdutos.this).getIntent();
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_numpedido), numPedido);
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_chavepedido), chavePedido);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putInt(getString(R.string.intent_flag), 0);
                    intent.putExtras(params);
                    (ConsultaProdutos.this).finish();
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.toString();
            }
            if (dialog.isShowing())
                dialog.dismiss();
        } else if (flag == 2) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentProdutos frag = (FragmentProdutos) getSupportFragmentManager().findFragmentByTag("mainFragA");
                        Bundle params = new Bundle();
                        if (frag != null) {
                            frag = null;
                        }
                        frag = new FragmentProdutos();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.rl_fragment_container, frag, "mainFragA");
                        params.putInt(getString(R.string.intent_flag), flag);
                        params.putString(getString(R.string.intent_numpedido), numPedido);
                        params.putString(getString(R.string.intent_chavepedido), chavePedido);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                        frag.setArguments(params);
                        ft.commit();

                    } catch (Exception e) {
                        e.toString();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (telaInvocada != null) {
            if (telaInvocada.equals("CadastroPedidos")) {
                finish();
                return;
            } else if (telaInvocada.equals("CADASTRO_CONTATOS")) {
                Intent intentp = new Intent(ConsultaProdutos.this, CadastroContatos.class);
                Bundle params = new Bundle();
                //params.putString(getString(R.string.intent_codproduto), CodProd);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                //params.putString(getString(R.string.intent_cad_contato), CodProd);
                params.putInt(getString(R.string.intent_codcliente), codCliente);
                params.putString(getString(R.string.intent_nomerazao), nomeCliente);
                intentp.putExtras(params);
                startActivity(intentp);
                ConsultaProdutos.this.finish();
            } else if (telaInvocada.equals("TAB_PRODUTOS_CONTATOS")) {
                Intent intentp = new Intent(ConsultaProdutos.this, DadosContato.class);
                Bundle params = new Bundle();
                //params.putString(getString(R.string.intent_codproduto), CodProd);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                // params.putString(getString(R.string.intent_cad_contato), CodProd);
                params.putInt(getString(R.string.intent_codcliente), codCliente);
                params.putString(getString(R.string.intent_nomerazao), nomeCliente);
                params.putInt(getString(R.string.intent_codcontato), codContato);
                intentp.putExtras(params);
                startActivity(intentp);
                ConsultaProdutos.this.finish();
            }

            switch (flag) {
                case 0:
                    if (!telaInvocada.equals("CADASTRO_CONTATOS")) {
                        if (!telaInvocada.equals("TAB_PRODUTOS_CONTATOS")) {
                            Intent intent = new Intent(ConsultaProdutos.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            intent.putExtras(params);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                case 1:
                    if (!telaInvocada.equals("CADASTRO_CONTATOS")) {
                        if (!telaInvocada.equals("TAB_PRODUTOS_CONTATOS")) {
                            Intent intent1 = new Intent(ConsultaProdutos.this, ConsultaProdutos.class);
                            Bundle params1 = new Bundle();
                            params1.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params1.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params1.putString(getString(R.string.intent_usuario), usuario);
                            params1.putString(getString(R.string.intent_senha), senha);
                            intent1.putExtras(params1);
                            startActivity(intent1);
                            finish();
                            break;
                        }
                    }
                case 2:
                    if (!telaInvocada.equals("CADASTRO_CONTATOS")) {
                        if (!telaInvocada.equals("TAB_PRODUTOS_CONTATOS")) {
                            Intent intent2 = new Intent(ConsultaProdutos.this, ConsultaProdutos.class);
                            Bundle params2 = new Bundle();
                            params2.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params2.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params2.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params2.putString(getString(R.string.intent_usuario), usuario);
                            params2.putString(getString(R.string.intent_senha), senha);
                            params2.putString(getString(R.string.intent_numpedido), numPedido);
                            params2.putInt(getString(R.string.intent_flag), flag);
                            intent2.putExtras(params2);
                            startActivity(intent2);
                            finish();
                            break;
                        }
                    }
            }
            if (numPedido != null) {
                if (numPedido.equals("0")) {
                    finish();
                }
            }
        } else {
            Intent intent = new Intent(ConsultaProdutos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(ConsultaProdutos.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_produtos) {

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_agenda) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(ConsultaProdutos.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(ConsultaProdutos.this, Login.class);
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
            Intent intent = new Intent(ConsultaProdutos.this, InfoJDSystem.class);
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

}
