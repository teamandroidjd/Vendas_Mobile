package com.jdsystem.br.vendasmobile;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.list;

public class act_ListClientes extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    ProgressDialog pDialog;
    private Handler handler = new Handler();
    public ListAdapter adapter;
    String sCodVend;

    SQLiteDatabase DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_listclientes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(intent!=null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
            }
        }

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

        FloatingActionButton cadclie = (FloatingActionButton) findViewById(R.id.cadclie);
        cadclie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(act_ListClientes.this, act_CadClientes.class);
                Bundle params = new Bundle();
                params.putString("codvendedor", sCodVend);
                intent.putExtras(params);
                startActivity(intent);
                startActivityForResult(intent, 1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pDialog = new ProgressDialog(act_ListClientes.this);
        pDialog.setTitle("Aguarde...");
        pDialog.setMessage("Carregando Clientes");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(act_ListClientes.this);
        thread.start();
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getApplicationContext(), actDadosCliente.class);
        //String Cnpj = (String) adapter.getItem(position);
        //Bundle params = new Bundle();
        //params.putString("cnpj", Cnpj);
        //intent.putExtras(params);
        startActivityForResult(intent, 1);
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

        } else if (id == R.id.nav_produtos) {

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(act_ListClientes.this, actListPedidos.class);
            startActivity(i);

        } else if (id == R.id.nav_config) {

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(act_ListClientes.this, actSincronismo.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void CarregarClientes() {
        Cursor CursorClie = DB.rawQuery(" SELECT CLIENTES.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                                        " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN" +
                                        " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO WHERE CODVENDEDOR = " + sCodVend,null);
        ArrayList<HashMap<String, String>> DadosList = new ArrayList<HashMap<String, String>>();

        if(CursorClie.getCount() > 0) {
            CursorClie.moveToFirst();
            while (CursorClie.moveToNext()) {
                String NomeFan      = CursorClie.getString(CursorClie.getColumnIndex("NOMEFAN"));
                String Cnpj         = CursorClie.getString(CursorClie.getColumnIndex("CNPJ_CPF"));
                String NomeRazao    = CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO"));
                String Cidade       = CursorClie.getString(CursorClie.getColumnIndex("CIDADE"));
                String Estado       = CursorClie.getString(CursorClie.getColumnIndex("UF"));
                String Bairro       = CursorClie.getString(CursorClie.getColumnIndex("BAIRRO"));
                String Telefone     = "Telefone Sem Cadastro";//cursor.getString(4);


                HashMap<String, String> DadosClientes = new HashMap<String, String>();

                DadosClientes.put("CNPJ_CPF", Cnpj);
                DadosClientes.put("NOMERAZAO", NomeRazao);
                DadosClientes.put("NOMEFAN", NomeFan);
                DadosClientes.put("CIDADE", Cidade);
                DadosClientes.put("ESTADO", Estado);
                DadosClientes.put("BAIRRO", Bairro);
                DadosClientes.put("TELEFONE", Telefone);


                DadosList.add(DadosClientes);
            }
            CursorClie.close();
            adapter = new ListAdapter(act_ListClientes.this, DadosList, R.layout.lstclientes_card,
                    new String[]{"NOMEFAN", "CNPJ_CPF", "NOMERAZAO", "CIDADE","ESTADO", "BAIRRO", "TELEFONE"},
                    new int[]{R.id.lblNomeFanClie, R.id.lblCNPJ, R.id.lblNomerazao, R.id.lblCidade, R.id.lblEstado, R.id.lblBairro, R.id.lblTel});

            setListAdapter(adapter);
        }
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
           @Override
            public void run() {
                try {
                     CarregarClientes();
                }catch (Exception E){

                }
                finally {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
            };

        });

    }

}
