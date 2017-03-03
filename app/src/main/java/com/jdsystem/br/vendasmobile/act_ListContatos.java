package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.fragments.FragmentContatos;

import java.util.ArrayList;
import java.util.List;


public class act_ListContatos extends AppCompatActivity implements Runnable  {
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    String sCodVend, URLPrincipal, usuario, senha, UsuarioLogado;
    SQLiteDatabase DB;
    Contatos lstcontatos;
    ProgressDialog pDialog;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__contatos);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                usuario = params.getString("usuario");
                senha = params.getString("senha");
            }

            FloatingActionButton CadContatos = (FloatingActionButton) findViewById(R.id.cadcontato);
            CadContatos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(act_ListContatos.this, CadContatos.class);
                    Bundle params = new Bundle();
                    params.putString("codvendedor", sCodVend);
                    params.putString("usuario", usuario);
                    params.putString("senha", senha);
                    params.putString("urlPrincipal",URLPrincipal);
                    i.putExtras(params);
                    startActivity(i);
                    finish();

                }
            });

            /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            if(UsuarioLogado != null) {
                UsuarioLogado = prefs.getString("usuario", null);
                usuariologado.setText("Olá " + UsuarioLogado + "!");
            }else {
                usuariologado.setText("Olá " + usuario + "!");
            }*/
        }
        pDialog = new ProgressDialog(act_ListContatos.this);
        pDialog.setTitle("Aguarde...");
        pDialog.setMessage("Carregando Clientes");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(act_ListContatos.this);
        thread.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, actListPedidos.class);
        Bundle params = new Bundle();
        params.putString("codvendedor", sCodVend);
        params.putString("usuario", usuario);
        params.putString("senha", senha);
        params.putString("urlPrincipal",URLPrincipal);
        intent.putExtras(params);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act__contatos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(act_ListContatos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivity(intent);
        } else if (id == R.id.nav_produtos) {
            Intent iprod = new Intent(act_ListContatos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            iprod.putExtras(params);
            startActivity(iprod);

        } else if (id == R.id.nav_pedidos) {
            Intent iped = new Intent(act_ListContatos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            iped.putExtras(params);
            startActivity(iped);

        } else if(id == R.id.nav_contatos){

        }else if (id == R.id.nav_sincronismo) {
            Intent isinc = new Intent(act_ListContatos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            isinc.putExtras(params);
            startActivity(isinc);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/

    public List<Contatos> carregarcontatos(){

        DB = new ConfigDB(this).getReadableDatabase();

        ArrayList<Contatos> DadosListContatos = new ArrayList<Contatos>();

        Cursor cursorContatos = DB.rawQuery("SELECT NOME, CARGO, EMAIL, TEL1, TEL2, DOCUMENTO, DATA, CEP, ENDERECO, NUMERO, COMPLEMENTO, UF, CODVENDEDOR, CODBAIRRO, CODCIDADE, CODCLIENTE FROM CONTATO",null);
        cursorContatos.moveToFirst();
        if(cursorContatos.getCount() > 0){
            do{
                String nome = cursorContatos.getString(cursorContatos.getColumnIndex("NOME"));
                String cargo = cursorContatos.getString(cursorContatos.getColumnIndex("CARGO"));
                String email = cursorContatos.getString(cursorContatos.getColumnIndex("EMAIL"));
                String tel1 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL1"));
                String tel2 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL2"));


                lstcontatos = new Contatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null, 0, 0);
                DadosListContatos.add(lstcontatos);
            }while (cursorContatos.moveToNext());
            cursorContatos.close();

        }else {
            Toast.makeText(this, "Nenhum contato encontrado!", Toast.LENGTH_SHORT).show();
        }
        return DadosListContatos;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FragmentContatos frag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFrag");
                    if (frag == null) {
                        frag = new FragmentContatos();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.rl_fragment_container2, frag, "mainFrag");
                        ft.commit();
                    }
                } catch (Exception E) {

                } finally {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
            }

            ;

        });

    }
}

