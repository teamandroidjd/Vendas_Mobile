package com.jdsystem.br.vendasmobile;


import android.app.ProgressDialog;
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

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.FiltroProdutos;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.fragments.FragmentFiltroProdutos;
import com.jdsystem.br.vendasmobile.fragments.ProdutosFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class act_ListProdutos extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {


    String sCodVend, URLPrincipal, usuario, senha, dtUltAtu;
    String UsuarioLogado;

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private EditText prod_txt_pesquisaproduto;
    private ProgressDialog dialog, pDialog;
    SQLiteDatabase DB;
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
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                usuario = params.getString("usuario");
                senha = params.getString("senha");
                //Pedido = params.getBoolean("pedido");
            }
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
        if (UsuarioLogado != null) {
            UsuarioLogado = prefs.getString("usuario", null);
            usuariologado.setText("Olá " + UsuarioLogado + "!");
        } else {
            usuariologado.setText("Olá " + usuario + "!");
        }


        prod_txt_pesquisaproduto = (EditText) findViewById(R.id.prod_txt_pesquisaproduto);
        prod_txt_pesquisaproduto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

            public void afterTextChanged(Editable s) {
            }
        });

        pDialog = new ProgressDialog(act_ListProdutos.this);
        pDialog.setTitle("Aguarde");
        pDialog.setMessage("Carregando Produtos...");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(act_ListProdutos.this);
        thread.start();


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
            Boolean ConexOk = Util.checarConexaoCelular(act_ListProdutos.this);
            if (ConexOk == true) {
                dialog = new ProgressDialog(this);
                dialog.setCancelable(false);
                dialog.setMessage("Sincronizando Produtos");
                dialog.setTitle("Aguarde");
                dialog.show();

                try {
                    actSincronismo.run(act_ListProdutos.this);
                    actSincronismo.SincronizarProdutosStatic(dtUltAtu, act_ListProdutos.this, true);

                    Intent intent = (act_ListProdutos.this).getIntent();
                    (act_ListProdutos.this).finish();
                    startActivity(intent);
                } catch (Exception e) {
                    e.toString();
                }
                if (dialog.isShowing())
                    dialog.dismiss();

            } else {
                Toast.makeText(act_ListProdutos.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ProdutosFragment frag = (ProdutosFragment) getSupportFragmentManager().findFragmentByTag("mainFragC");
                    if (frag == null) {
                        frag = new ProdutosFragment();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.rl_fragment_container, frag, "mainFragC");
                        ft.commit();
                    }
                } catch (Exception E) {
                    System.out.println("Error" + E);
                }
                if (pDialog.isShowing())
                    pDialog.dismiss();

            }

        });

    }


    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        Intent intent = new Intent(act_ListProdutos.this, actListPedidos.class);
        Bundle params = new Bundle();
        params.putString("codvendedor", sCodVend);
        params.putString("urlPrincipal", URLPrincipal);
        params.putString("usuario", usuario);
        params.putString("senha", senha);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(act_ListProdutos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_produtos) {
            Intent i = new Intent(act_ListProdutos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);


        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(act_ListProdutos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(act_ListProdutos.this, act_ListContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(act_ListProdutos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
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

        ArrayList<Produtos> DadosLisProdutos = new ArrayList<Produtos>();

        Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
        CursorParametro.moveToFirst();

        Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS ORDER BY DESCRICAO", null);
        cursorProdutos.moveToFirst();
        if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {
            do {
                String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));

                String ppadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("VENDAPADRAO"));
                ppadrao = ppadrao.trim();
                BigDecimal precopadrao = new BigDecimal(Double.parseDouble(ppadrao.replace(',', '.')));

                String p1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA1"));
                p1 = p1.trim();
                BigDecimal preco1 = new BigDecimal(Double.parseDouble(p1.replace(',', '.')));

                String p2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA2"));
                p2 = p2.trim();
                BigDecimal preco2 = new BigDecimal(Double.parseDouble(p2.replace(',', '.')));

                String p3 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA3"));
                p3 = p3.trim();
                BigDecimal preco3 = new BigDecimal(Double.parseDouble(p3.replace(',', '.')));

                String p4 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA4"));
                p4 = p4.trim();
                BigDecimal preco4 = new BigDecimal(Double.parseDouble(p4.replace(',', '.')));

                String p5 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA5"));
                p5 = p5.trim();
                BigDecimal preco5 = new BigDecimal(Double.parseDouble(p5.replace(',', '.')));

                String pp1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP1"));
                pp1 = pp1.trim();
                BigDecimal precoP1 = new BigDecimal(Double.parseDouble(pp1.replace(',', '.')));

                String pp2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP2"));
                pp2 = pp2.trim();
                BigDecimal precoP2 = new BigDecimal(Double.parseDouble(pp2.replace(',', '.')));

                String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));

                String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));

                lstprodutos = new Produtos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5, precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, precopadrao);
                DadosLisProdutos.add(lstprodutos);
            } while (cursorProdutos.moveToNext());
            cursorProdutos.close();
            CursorParametro.close();


        } else {
            Toast.makeText(this, "Nenhum produto encontrado!", Toast.LENGTH_SHORT).show();
        }

        if (pDialog.isShowing())
            pDialog.dismiss();

        return DadosLisProdutos;

    }

    public List<FiltroProdutos> pesquisarprodutos(CharSequence valor_campo) {

        pDialog = new ProgressDialog(act_ListProdutos.this);
        pDialog.setTitle("Aguarde");
        pDialog.setMessage("Realizando filtro...");
        pDialog.setCancelable(false);
        pDialog.show();

        ProdutosFragment frag1 = (ProdutosFragment) getSupportFragmentManager().findFragmentByTag("mainFrag");
        if (frag1 != null) {
            frag1.getActivity().getSupportFragmentManager().popBackStack();
        }

        ArrayList<FiltroProdutos> DadosLisProdutos = new ArrayList<FiltroProdutos>();

        Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
        CursorParametro.moveToFirst();

        Cursor cursorProdutos = DB.rawQuery("SELECT * FROM ITENS WHERE DESCRICAO LIKE '%" + valor_campo + "%' ORDER BY DESCRICAO", null);
        cursorProdutos.moveToFirst();
        if (cursorProdutos.getCount() > 0 && CursorParametro.getCount() > 0) {

            do {
                String descricao = cursorProdutos.getString(cursorProdutos.getColumnIndex("DESCRICAO"));
                String codigoManual = cursorProdutos.getString(cursorProdutos.getColumnIndex("CODITEMANUAL"));
                String status = cursorProdutos.getString(cursorProdutos.getColumnIndex("ATIVO"));
                String unidVenda = cursorProdutos.getString(cursorProdutos.getColumnIndex("UNIVENDA"));
                String apresentacao = cursorProdutos.getString(cursorProdutos.getColumnIndex("APRESENTACAO"));
                String quantidade = cursorProdutos.getString(cursorProdutos.getColumnIndex("QTDESTPROD"));

                String ppadrao = cursorProdutos.getString(cursorProdutos.getColumnIndex("VENDAPADRAO"));
                ppadrao = ppadrao.trim();
                BigDecimal precopadrao = new BigDecimal(Double.parseDouble(ppadrao.replace(',', '.')));

                String p1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA1"));
                p1 = p1.trim();
                BigDecimal preco1 = new BigDecimal(Double.parseDouble(p1.replace(',', '.')));

                String p2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA2"));
                p2 = p2.trim();
                BigDecimal preco2 = new BigDecimal(Double.parseDouble(p2.replace(',', '.')));

                String p3 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA3"));
                p3 = p3.trim();
                BigDecimal preco3 = new BigDecimal(Double.parseDouble(p3.replace(',', '.')));

                String p4 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA4"));
                p4 = p4.trim();
                BigDecimal preco4 = new BigDecimal(Double.parseDouble(p4.replace(',', '.')));

                String p5 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDA5"));
                p5 = p5.trim();
                BigDecimal preco5 = new BigDecimal(Double.parseDouble(p5.replace(',', '.')));

                String pp1 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP1"));
                pp1 = pp1.trim();
                BigDecimal precoP1 = new BigDecimal(Double.parseDouble(pp1.replace(',', '.')));

                String pp2 = cursorProdutos.getString(cursorProdutos.getColumnIndex("VLVENDAP2"));
                pp2 = pp2.trim();
                BigDecimal precoP2 = new BigDecimal(Double.parseDouble(pp2.replace(',', '.')));

                String tabela1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                String tabela2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                String tabela3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                String tabela4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                String tabela5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                String tabpromo1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                String tabpromo2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));

                String tipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));


                lstfiltprodutos = new FiltroProdutos(descricao, codigoManual, status, unidVenda, apresentacao, preco1, preco2, preco3, preco4, preco5, precoP1, precoP2, quantidade, tabela1, tabela2, tabela3, tabela4, tabela5, tabpromo1, tabpromo2, tipoEstoque, precopadrao);
                DadosLisProdutos.add(lstfiltprodutos);
            } while (cursorProdutos.moveToNext());
            cursorProdutos.close();
            CursorParametro.close();

        } else {
            Toast.makeText(this, "Nenhum contato encontrado!", Toast.LENGTH_SHORT).show();
        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        return DadosLisProdutos;
    }


}
