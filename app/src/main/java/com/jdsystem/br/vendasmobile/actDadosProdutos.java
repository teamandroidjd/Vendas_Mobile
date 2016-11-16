package com.jdsystem.br.vendasmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class actDadosProdutos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String CodManual;
    String sCodVend;
    String URLPrincipal;

    /*SQLiteDatabase DB;

    String TAG_CODMANUAL = (TextView) findViewById(R.id.txt_codprod);
    TextView TAG_DESCRICAO = (TextView) findViewById(R.id.txt_descricao);
    TextView TAG_UNIVENDA = (TextView) findViewById(R.id.txtunvenda);
    TextView TAG_VLVENDA1 = (TextView) findViewById(R.id.txtpreco);
    TextView TAG_VLVENDA2 = (TextView) findViewById(R.id.txtpreco2);
    TextView TAG_VLVENDA3 = (TextView) findViewById(R.id.txtpreco3);
    TextView TAG_VLVENDA4 = (TextView) findViewById(R.id.txtpreco4);
    TextView TAG_VLVENDA5 = (TextView) findViewById(R.id.txtpreco5);
    TextView TAG_VLVENDAP1 = (TextView) findViewById(R.id.txtprecop1);
    TextView TAG_VLVENDAP2 = (TextView) findViewById(R.id.txtprecop2);
    TextView TAG_MARCA = (TextView) findViewById(R.id.txtmarca);
    TextView TAG_CLASSE = (TextView) findViewById(R.id.txtclasse);
    TextView TAG_FABRICANTE = (TextView) findViewById(R.id.txtfabricante);
    TextView TAG_FORNECEDOR = (TextView) findViewById(R.id.txtforncedor);
    TextView TAG_APRESENTACAO = (TextView) findViewById(R.id.txtapres);
    TextView TAG_ATIVO = (TextView) findViewById(R.id.txtStatus);*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                CodManual = params.getString("coditemanual");
            }

            /*Cursor CursorProd = DB.rawQuery(" SELECT CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA," +
                    "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2," +
                    "ATIVO, APRESENTACAO) FROM ITENS WHERE CODITEMANUAL = " + (TAG_CODMANUAL), null);

            if (CursorProd.getCount() > 0) {
                CursorProd.moveToFirst();

                TAG_CODMANUAL = CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL"));
                TAG_DESCRICAO = CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO"));
                TAG_UNIVENDA = CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA"));
                TAG_APRESENTACAO = CursorProd.getString(CursorProd.getColumnIndex("APRESENTACAO"));
                TAG_ATIVO = CursorProd.getString(CursorProd.getColumnIndex("ATIVO"));
                TAG_VLVENDA1 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));
                TAG_VLVENDA2 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA2"));
                TAG_VLVENDA3 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA3"));
                TAG_VLVENDA4 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA4"));
                TAG_VLVENDA5 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA5"));
                TAG_VLVENDAP1 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP1"));
                TAG_VLVENDAP2 = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP2"));
                TAG_FABRICANTE = CursorProd.getString(CursorProd.getColumnIndex("FABRICANTE"));
                TAG_FORNECEDOR = CursorProd.getString(CursorProd.getColumnIndex("FORNECEDOR"));
                TAG_CLASSE = CursorProd.getString(CursorProd.getColumnIndex("CLASSE"));
                TAG_MARCA = CursorProd.getString(CursorProd.getColumnIndex("MARCA"));

                HashMap<String, String> DadosProdutos = new HashMap<String, String>();

                DadosProdutos.put("CODITEMANUAL", TAG_CODMANUAL);
                DadosProdutos.put("DESCRICAO",TAG_DESCRICAO);
                DadosProdutos.put("UNIVENDA", TAG_UNIVENDA);
                DadosProdutos.put("APRESENTACAO", Apresentacao);
                DadosProdutos.put("ATIVO", SituProd);
                DadosProdutos.put("VLVENDA1", Preco);

                DadosList.add(DadosProdutos);
            }

            CursorProd.close();*/

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
            Intent intent = new Intent(actDadosProdutos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(actDadosProdutos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_produtos) {
            Intent i = new Intent(actDadosProdutos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent intent = new Intent(actDadosProdutos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

