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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.FiltroProdutos;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.fragments.FragmentFiltroProdutos;
import com.jdsystem.br.vendasmobile.fragments.FragmentProdutos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ConsultaProdutos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {


    String sCodVend, URLPrincipal, usuario, senha, dtUltAtu, UsuarioLogado, sincprod,NumPedido,chavepedido;
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private EditText prod_txt_pesquisaproduto;
    private ProgressDialog pDialog;
    SQLiteDatabase DB;
    int Flag = 0;
    Produtos lstprodutos;
    FiltroProdutos lstfiltprodutos;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                NumPedido = params.getString("numpedido");
                chavepedido = params.getString("chave");
                Flag = params.getInt("flag");
                //Pedido = params.getBoolean("pedido");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        carregausuariologado();

        prod_txt_pesquisaproduto = (EditText) findViewById(R.id.prod_txt_pesquisaproduto);
        prod_txt_pesquisaproduto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                FragmentFiltroProdutos frag = (FragmentFiltroProdutos) getSupportFragmentManager().findFragmentByTag("mainFragB");
                if (frag == null) {
                    frag = new FragmentFiltroProdutos();
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("pesquisa", s);
                    frag.setArguments(bundle);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container, frag, "mainFragB");
                    ft.commit();
                } else {
                    frag = new FragmentFiltroProdutos();
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("pesquisa", s);
                    frag.setArguments(bundle);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container, frag, "mainFragB");
                    ft.commit();
                }
            }
        });

        pDialog = new ProgressDialog(ConsultaProdutos.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.loading_products));
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(ConsultaProdutos.this);
        thread.start();
    }

    private void carregausuariologado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        UsuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        if (UsuarioLogado != null) {
            UsuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
            usuariologado.setText("Olá " + UsuarioLogado + "!");
        } else {
            usuariologado.setText("Olá " + usuario + "!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sincroniza_cliente, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sinc_cliente) {
            if (item.getItemId() == R.id.menu_sinc_cliente) {
                Boolean ConexOk = Util.checarConexaoCelular(ConsultaProdutos.this);
                if (ConexOk == true) {
                    Cursor cursorVerificaProd = DB.rawQuery("SELECT * FROM ITENS", null);
                    if (cursorVerificaProd.getCount() == 0) {
                        Flag = 1;
                        pDialog = new ProgressDialog(this);
                        pDialog.setCancelable(false);
                        pDialog.setMessage(getString(R.string.primeira_sync_itens));
                        pDialog.setTitle(getString(R.string.wait));
                        pDialog.show();

                        Thread thread = new Thread(this);
                        thread.start();

                    } else {
                        Flag = 1;
                        pDialog = new ProgressDialog(this);
                        pDialog.setCancelable(false);
                        pDialog.setMessage(getString(R.string.sync_products));
                        pDialog.setTitle(getString(R.string.wait));
                        pDialog.show();

                        Thread thread = new Thread(this);
                        thread.start();
                    }

                } else {
                    Toast.makeText(ConsultaProdutos.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void run() {
        if (Flag == 1) {
            try {
                //Sincronismo.run(ConsultaProdutos.this);
                sincprod = Sincronismo.SincronizarProdutosStatic(ConsultaProdutos.this, usuario, senha, 0);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), sincprod, Toast.LENGTH_LONG).show();
                    }
                });
                Intent intent = (ConsultaProdutos.this).getIntent();
                (ConsultaProdutos.this).finish();
                startActivity(intent);
            } catch (Exception e) {
                e.toString();
            }
        } else if(Flag == 2){
            try {
                FragmentProdutos frag = (FragmentProdutos) getSupportFragmentManager().findFragmentByTag("mainFragC");
                Bundle params = new Bundle();
                if (frag == null) {
                    frag = new FragmentProdutos();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container, frag, "mainFragC");
                    params.putInt("flag",Flag);
                    params.putString("numpedido",NumPedido);
                    params.putString("chave",chavepedido);
                    params.putString(getString(R.string.intent_usuario),usuario);
                    params.putString(getString(R.string.intent_senha),senha);
                    params.putString(getString(R.string.intent_codvendedor),sCodVend);
                    frag.setArguments(params);
                    ft.commit();
                }
            } catch (Exception e) {
                e.toString();
            }

        }else{
            try {
                FragmentProdutos frag = (FragmentProdutos) getSupportFragmentManager().findFragmentByTag("mainFragC");
                if (frag == null) {
                    frag = new FragmentProdutos();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container, frag, "mainFragC");
                    ft.commit();
                }
            } catch (Exception e) {
                e.toString();
            }
        }
        if (pDialog.isShowing())
            pDialog.dismiss();

    }

    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        Intent intent = new Intent(ConsultaProdutos.this, ConsultaPedidos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), sCodVend);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(ConsultaProdutos.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_produtos) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);


        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaProdutos.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(ConsultaProdutos.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<Produtos> carregarprodutos() {

        DB = new ConfigDB(this).getReadableDatabase();
        BigDecimal preco1 = null;
        BigDecimal preco2 = null;
        BigDecimal preco3 = null;
        BigDecimal preco4 = null;
        BigDecimal preco5 = null;
        BigDecimal precoP1 = null;
        BigDecimal precoP2 = null;

        ArrayList<Produtos> DadosLisProdutos = new ArrayList<Produtos>();
        try {
            Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
            CursorParametro.moveToFirst();
            String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
            String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
            String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
            String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
            String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
            String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
            String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
            String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));

            Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS WHERE ATIVO = 'S' ORDER BY DESCRICAO", null);
            cursorProdutos.moveToFirst();
            if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {
                do {
                    String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                    String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                    String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                    String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                    String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                    String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));

                    String taPadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("TABELAPADRAO"));
                  /*ppadrao = ppadrao.trim();
                    BigDecimal precopadrao = new BigDecimal(Double.parseDouble(ppadrao.replace(',', '.')));*/

                    if (!tabela1.equals("")) {
                        String p1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA1"));
                        p1 = p1.trim();
                        if (!p1.equals("")) {
                            preco1 = new BigDecimal(Double.parseDouble(p1.replace(',', '.')));
                        }
                    }else {
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
                    }else {
                        tabela3 = "";
                    }
                    if (!tabela4.equals("")) {
                        String p4 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA4"));
                        p4 = p4.trim();
                        if (!p4.equals("")) {
                            preco4 = new BigDecimal(Double.parseDouble(p4.replace(',', '.')));
                        }
                    }else {
                        tabela4 = "";
                    }
                    if (!tabela5.equals("")) {
                        String p5 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA5"));
                        p5 = p5.trim();
                        if (!p5.equals("")) {
                            preco5 = new BigDecimal(Double.parseDouble(p5.replace(',', '.')));
                        }
                    }else {
                        tabela5 = "";
                    }
                    if (!tabpromo1.equals("")) {
                        String pp1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP1"));
                        pp1 = pp1.trim();
                        if (!pp1.equals("")) {
                            precoP1 = new BigDecimal(Double.parseDouble(pp1.replace(',', '.')));
                        }
                    }else {
                        tabpromo1 = "";
                    }
                    if (!tabpromo2.equals("")) {
                        String pp2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP2"));
                        pp2 = pp2.trim();
                        if (!pp2.equals("")) {
                            precoP2 = new BigDecimal(Double.parseDouble(pp2.replace(',', '.')));
                        }
                    }else {
                        tabpromo2 = "";
                    }
                    lstprodutos = new Produtos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5, precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, taPadrao);
                    DadosLisProdutos.add(lstprodutos);
                } while (cursorProdutos.moveToNext());
                cursorProdutos.close();
                CursorParametro.close();


            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaProdutos.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage(R.string.alertsyncproducts)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                return;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (Exception e) {
            e.toString();
            Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
        }

        if (pDialog.isShowing())
            pDialog.dismiss();

        return DadosLisProdutos;

    }

    public List<FiltroProdutos> pesquisarprodutos(CharSequence valor_campo) {

        pDialog = new ProgressDialog(ConsultaProdutos.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.performing_filter));
        pDialog.setCancelable(false);
        pDialog.show();

        FragmentProdutos frag1 = (FragmentProdutos) getSupportFragmentManager().findFragmentByTag("mainFrag");
        if (frag1 != null) {
            frag1.getActivity().getSupportFragmentManager().popBackStack();
        }
        BigDecimal preco1 = null;
        BigDecimal preco2 = null;
        BigDecimal preco3 = null;
        BigDecimal preco4 = null;
        BigDecimal preco5 = null;
        BigDecimal precoP1 = null;
        BigDecimal precoP2 = null;

        ArrayList<FiltroProdutos> DadosLisProdutos = new ArrayList<FiltroProdutos>();

        try {

            Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
            CursorParametro.moveToFirst();
            String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
            String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
            String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
            String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
            String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
            String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
            String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));

            String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));

            Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS WHERE DESCRICAO LIKE '%" + valor_campo + "%' OR CODITEMANUAL LIKE '%" + valor_campo + "%' OR CLASSE LIKE '%" + valor_campo + "%'" +
                    " OR FABRICANTE LIKE '%" + valor_campo + "%' OR FORNECEDOR LIKE '%" + valor_campo + "%' OR MARCA LIKE '%" + valor_campo + "%' ORDER BY DESCRICAO", null);
            cursorProdutos.moveToFirst();
            if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {
                do {
                    String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                    String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                    String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                    String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                    String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                    String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));

                    String taPadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("TABELAPADRAO"));
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

                    lstfiltprodutos = new FiltroProdutos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5, precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, taPadrao);
                    DadosLisProdutos.add(lstfiltprodutos);
                } while (cursorProdutos.moveToNext());
                cursorProdutos.close();
                CursorParametro.close();

            } else {
                Toast.makeText(this, R.string.no_products_found, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.toString();

        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        return DadosLisProdutos;
    }

}
