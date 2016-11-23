package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class act_ListProdutos extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    ProgressDialog prodDialog;
    private Handler handlerp = new Handler();
    public ListAdapter adapterp;
    String sCodVend, URLPrincipal;
    ListView edtProdutos;
    SearchView sv;

    Boolean Pedido;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        edtProdutos = (ListView) findViewById(android.R.id.list);
        sv = (SearchView) findViewById(R.id.EdtPesqItens);
        sv.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapterp.getFilter().filter(text);

                return false;
            }
        });


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                Pedido = params.getBoolean("pedido");
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

        prodDialog = new ProgressDialog(act_ListProdutos.this);
        prodDialog.setTitle("Aguarde...");
        prodDialog.setMessage("Carregando Produtos");
        prodDialog.setCancelable(false);
        prodDialog.show();

        Thread thread = new Thread(act_ListProdutos.this);
        thread.start();
    }


    @Override
    protected void onListItemClick(ListView lp, View vp, int position, long id) {
        if (Pedido == true) {
            AlertDialog.Builder mensagem = new AlertDialog.Builder(act_ListProdutos.this);
            mensagem.setTitle("Quantidade");
            mensagem.setMessage("Digite a quantidade do Item");
            // DECLARACAO DO EDITTEXT
            final EditText input = new EditText(this);
            mensagem.setView(input);
            mensagem.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), input.getText().toString().trim(),
                            Toast.LENGTH_SHORT).show();
                }

            });
            mensagem.show();
            // FORÇA O TECLADO APARECER AO ABRIR O ALERT
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        }else{
            Intent intentp = new Intent(getApplicationContext(), actDadosProdutos.class);
            Bundle extras = new Bundle();
            extras.putLong("position", id);
            intentp.putExtras(extras);
            startActivity(intentp);
        }
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
            Intent intent = new Intent(act_ListProdutos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);

        } else if (id == R.id.nav_produtos) {
            Intent i = new Intent(act_ListProdutos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            //finish();

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(act_ListProdutos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            //finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(act_ListProdutos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            i.putExtras(params);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void CarregarProdutos() {
        Cursor CursorProd = DB.rawQuery(" SELECT CODITEMANUAL, DESCRICAO, UNIVENDA, APRESENTACAO, ATIVO, VLVENDA1  FROM ITENS ", null);
        ArrayList<HashMap<String, String>> DadosList = new ArrayList<HashMap<String, String>>();

        if (CursorProd.getCount() > 0) {
            CursorProd.moveToFirst();

            while (CursorProd.moveToNext()) {
                String CodigoManual = CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL"));
                String Descricao = CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO"));
                String UnidVenda = CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA"));
                String Apresentacao = CursorProd.getString(CursorProd.getColumnIndex("APRESENTACAO"));
                String Status = CursorProd.getString(CursorProd.getColumnIndex("ATIVO"));
                String Preco = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));
                String SituProd = null;

                if (Status.equals("1")) {
                    SituProd = "Ativo";
                } else {
                    SituProd = "Inativo";
                }

                HashMap<String, String> DadosProdutos = new HashMap<String, String>();

                DadosProdutos.put("CODITEMANUAL", CodigoManual);
                DadosProdutos.put("DESCRICAO", Descricao);
                DadosProdutos.put("UNIVENDA", UnidVenda);
                DadosProdutos.put("APRESENTACAO", Apresentacao);
                DadosProdutos.put("ATIVO", SituProd);
                DadosProdutos.put("VLVENDA1", Preco);

                DadosList.add(DadosProdutos);
            }
            CursorProd.close();
            adapterp = new ListAdapter(act_ListProdutos.this, DadosList, R.layout.lstprodutos_card,
                    new String[]{"CODITEMANUAL", "DESCRICAO", "UNIVENDA", "APRESENTACAO", "ATIVO", "VLVENDA1"},
                    new int[]{R.id.txt_codprod, R.id.txt_descricao, R.id.txtunvenda, R.id.txtapres, R.id.txtStatus, R.id.txtpreco});

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
                } catch (Exception E) {

                } finally {
                    if (prodDialog.isShowing())
                        prodDialog.dismiss();
                }
            }

            ;

        });

    }
}
