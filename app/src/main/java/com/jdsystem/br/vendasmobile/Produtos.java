package com.jdsystem.br.vendasmobile;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;

public class Produtos extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    ProgressDialog prodDialog;
    private Handler handlerp = new Handler();
    public ListAdapter adapterp;
    String sCodVend, URLPrincipal;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
            }
        }

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prodDialog = new ProgressDialog(Produtos.this);
        prodDialog.setTitle("Aguarde...");
        prodDialog.setMessage("Carregando Produtos");
        prodDialog.setCancelable(false);
        prodDialog.show();

        Thread thread = new Thread(Produtos.this);
        thread.start();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(Produtos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(Produtos.this, actListPedidos.class);
            startActivity(i);

        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(Produtos.this, Produtos.class);
            startActivity(intent);

        } else if (id == R.id.nav_config) {

        } else if (id == R.id.nav_sincronismo) {
            Intent intent = new Intent(Produtos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void CarregarProdutos() {
        Cursor CursorProd = DB.rawQuery(" SELECT ITENS.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN" +
                " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO WHERE CODVENDEDOR = " + sCodVend+
                " ORDER BY NOMEFAN ",null);
        ArrayList<HashMap<String, String>> DadosList = new ArrayList<HashMap<String, String>>();

        if(CursorProd.getCount() > 0) {
            CursorProd.moveToFirst();
            while (CursorProd.moveToNext()) {
                String NomeFan      = CursorProd.getString(CursorProd.getColumnIndex("NOMEFAN"));
                String Cnpj         = CursorProd.getString(CursorProd.getColumnIndex("CNPJ_CPF"));
                String NomeRazao    = CursorProd.getString(CursorProd.getColumnIndex("NOMERAZAO"));
                String Cidade       = CursorProd.getString(CursorProd.getColumnIndex("CIDADE"));
                String Estado       = CursorProd.getString(CursorProd.getColumnIndex("UF"));
                String Bairro       = CursorProd.getString(CursorProd.getColumnIndex("BAIRRO"));
                String Telefone     = "Telefone Sem Cadastro";//cursor.getString(4);


                HashMap<String, String> DadosProdutos = new HashMap<String, String>();

                DadosProdutos.put("CNPJ_CPF", Cnpj);
                DadosProdutos.put("NOMERAZAO", NomeRazao);
                DadosProdutos.put("NOMEFAN", NomeFan);
                DadosProdutos.put("CIDADE", Cidade);
                DadosProdutos.put("ESTADO", Estado);
                DadosProdutos.put("BAIRRO", Bairro);
                DadosProdutos.put("TELEFONE", Telefone);

                DadosList.add(DadosProdutos);
            }
            CursorProd.close();
            adapterp = new ListAdapter(Produtos.this, DadosList, R.layout.lstprodutos_card,
                    new String[]{"NOMEFAN", "CNPJ_CPF", "NOMERAZAO", "CIDADE","ESTADO", "BAIRRO", "TELEFONE"},
                    new int[]{R.id.lblNomeFanClie, R.id.lblCNPJ, R.id.lblNomerazao, R.id.lblCidade, R.id.lblEstado, R.id.lblBairro, R.id.lblTel});

            setListAdapter(adapterp);
        }
    }

    @Override
    public void run() {
        handlerp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    CarregarProdutos();
                }catch (Exception E){

                }
                finally {
                    if (prodDialog.isShowing())
                        prodDialog.dismiss();
                }
            };

        });

    }

}
