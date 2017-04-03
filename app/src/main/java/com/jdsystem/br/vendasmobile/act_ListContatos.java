package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.domain.FiltroContatos;
import com.jdsystem.br.vendasmobile.fragments.FragmentContatos;
import com.jdsystem.br.vendasmobile.fragments.FragmentFiltroContatos;

import java.util.ArrayList;
import java.util.List;


public class act_ListContatos extends ActionBarActivity implements NavigationView.OnNavigationItemSelectedListener, Runnable {
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    String sCodVend, URLPrincipal, usuario, senha, UsuarioLogado, editQuery;
    SQLiteDatabase DB;
    Contatos lstcontatos;
    FiltroContatos lstfiltrocontatos;
    //Contatos lstfiltrocontatos;
    ProgressDialog pDialog;
    private EditText pesquisacliente;
    Handler handler = new Handler();
    Toolbar toolbar;
    MenuItem searchItem;
    SearchView searchView;
    ProgressDialog Dialogo;
    int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__contatos);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }
        FloatingActionButton CadContatos = (FloatingActionButton) findViewById(R.id.cadcontato);
        CadContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(act_ListContatos.this, CadContatos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                i.putExtras(params);
                startActivity(i);

            }
        });
        /*pesquisacliente = (EditText) findViewById(R.id.pesqcontato);
        pesquisacliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FragmentFiltroContatos frag = (FragmentFiltroContatos) getSupportFragmentManager().findFragmentByTag("mainFragA");
                if (frag == null) {
                    frag = new FragmentFiltroContatos();
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("pesquisa", s);
                    frag.setArguments(bundle);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container2, frag, "mainFragA");
                    ft.commit();
                } else {
                    frag = new FragmentFiltroContatos();
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("pesquisa", s);
                    frag.setArguments(bundle);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container2, frag, "mainFragA");
                    ft.commit();
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(act_ListContatos.this);

        carregausuariologado();

        pDialog = new ProgressDialog(act_ListContatos.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.loadingcontacts));
        pDialog.setCancelable(false);
        pDialog.show();

        flag = 1;

        Thread thread = new Thread(act_ListContatos.this);
        thread.start();

    }

    private void carregausuariologado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(act_ListContatos.this);
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
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        Intent intent = new Intent(act_ListContatos.this, actListPedidos.class);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act__contatos, menu);
        searchItem = menu.findItem(R.id.action_searchable_activity);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Dialogo = new ProgressDialog(act_ListContatos.this);
                Dialogo.setIndeterminate(true);
                Dialogo.setTitle(getString(R.string.wait));
                Dialogo.setMessage(getString(R.string.searchingcontacts));
                Dialogo.setCancelable(false);
                Dialogo.setProgress(0);
                Dialogo.show();

                query.toString();
                editQuery = query;
                searchView.clearFocus();

                flag = 2;

                Thread thread = new Thread(act_ListContatos.this);
                thread.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(act_ListContatos.this, act_ListClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_produtos) {
            Intent iprod = new Intent(act_ListContatos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            iprod.putExtras(params);
            startActivity(iprod);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent iped = new Intent(act_ListContatos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            iped.putExtras(params);
            startActivity(iped);
            finish();

        } else if (id == R.id.nav_contatos) {

        } else if (id == R.id.nav_sincronismo) {
            Intent isinc = new Intent(act_ListContatos.this, actSincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            isinc.putExtras(params);
            startActivity(isinc);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<Contatos> carregarcontatos() {

        DB = new ConfigDB(this).getReadableDatabase();

        ArrayList<Contatos> DadosListContatos = new ArrayList<Contatos>();

        try {
            Cursor cursorContatos = DB.rawQuery("SELECT CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, " +
                    "CONTATO.TEL2, CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, " +
                    "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                    "CONTATO.CODVENDEDOR, CONTATO.CODBAIRRO, " +
                    "CLIENTES.NOMERAZAO, CONTATO.CODCIDADE, CONTATO.CODCLIENTE, CLIENTES.CODCLIE_EXT " +
                    "FROM CONTATO " +
                    "JOIN CLIENTES ON CONTATO.CODCLIE_EXT = CLIENTES.CODCLIE_EXT" +
                    " ORDER BY NOME", null);
            cursorContatos.moveToFirst();
            if (cursorContatos.getCount() > 0) {
                do {
                    String nome = cursorContatos.getString(cursorContatos.getColumnIndex("NOME"));
                    String cargo = cursorContatos.getString(cursorContatos.getColumnIndex("CARGO"));
                    String email = cursorContatos.getString(cursorContatos.getColumnIndex("EMAIL"));
                    String tel1 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL1"));
                    String tel2 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL2"));
                    String Doc = cursorContatos.getString(cursorContatos.getColumnIndex("DOCUMENTO"));
                    String Data = cursorContatos.getString(cursorContatos.getColumnIndex("DATA"));
                    String Cep = cursorContatos.getString(cursorContatos.getColumnIndex("CEP"));
                    String Endereco = cursorContatos.getString(cursorContatos.getColumnIndex("ENDERECO"));
                    String Num = cursorContatos.getString(cursorContatos.getColumnIndex("NUMERO"));
                    String Compl = cursorContatos.getString(cursorContatos.getColumnIndex("COMPLEMENTO"));
                    String uf = cursorContatos.getString(cursorContatos.getColumnIndex("UF"));
                    int codClieExt = cursorContatos.getInt(cursorContatos.getColumnIndex("CLIENTES.CODCLIE_EXT"));
                    String nomeCliente = cursorContatos.getString(cursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));


                    lstcontatos = new Contatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null,
                            0, 0, codClieExt, nomeCliente);
                    DadosListContatos.add(lstcontatos);
                } while (cursorContatos.moveToNext());
                cursorContatos.close();

            } else {
                Toast.makeText(this, R.string.no_contacts_found, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception E) {
            Toast.makeText(this, "Houve um problema ao acessar a base de dados. Favor entrar em contato com o suporte técnico JD System.", Toast.LENGTH_SHORT).show();
        }

        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        return DadosListContatos;

    }

    /*public List<FiltroContatos> pesquisarcontatos(CharSequence valor_campo) {

        pDialog = new ProgressDialog(act_ListContatos.this);
        pDialog.setTitle("Aguarde");
        pDialog.setMessage("Realizando filtro...");
        pDialog.setCancelable(false);
        pDialog.show();

        FragmentContatos frag1 = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFrag");
        if (frag1 != null) {
            frag1.getActivity().getSupportFragmentManager().popBackStack();
        }

        ArrayList<FiltroContatos> DadosListContatos = new ArrayList<FiltroContatos>();

        try {
            Cursor cursorContatos = DB.rawQuery("select CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, " +
                    "CONTATO.TEL2, CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, " +
                    "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                    "CONTATO.CODVENDEDOR, CONTATO.CODBAIRRO, " +
                    "CLIENTES.NOMERAZAO, CONTATO.CODCIDADE, CONTATO.CODCLIENTE, CLIENTES.CODCLIE_EXT " +
                    " from CONTATO " +
                    " join CLIENTES on CLIENTES.CODCLIE_EXT = CONTATO.CODCLIE_EXT " +
                    "WHERE CONTATO.NOME LIKE '%" + editQuery + "%' OR CLIENTES.NOMERAZAO" +
                    " LIKE '%" + editQuery + "%'" +

                    " order by CLIENTES.NOMERAZAO ", null);
            cursorContatos.moveToFirst();
            if (cursorContatos.getCount() > 0) {
                do {
                    String nome = cursorContatos.getString(cursorContatos.getColumnIndex("NOME"));
                    String cargo = cursorContatos.getString(cursorContatos.getColumnIndex("CARGO"));
                    String email = cursorContatos.getString(cursorContatos.getColumnIndex("EMAIL"));
                    String tel1 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL1"));
                    String tel2 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL2"));
                    String Doc = cursorContatos.getString(cursorContatos.getColumnIndex("DOCUMENTO"));
                    String Data = cursorContatos.getString(cursorContatos.getColumnIndex("DATA"));
                    String Cep = cursorContatos.getString(cursorContatos.getColumnIndex("CEP"));
                    String Endereco = cursorContatos.getString(cursorContatos.getColumnIndex("ENDERECO"));
                    String Num = cursorContatos.getString(cursorContatos.getColumnIndex("NUMERO"));
                    String Compl = cursorContatos.getString(cursorContatos.getColumnIndex("COMPLEMENTO"));
                    String uf = cursorContatos.getString(cursorContatos.getColumnIndex("UF"));
                    int codClieExt = cursorContatos.getInt(cursorContatos.getColumnIndex("CLIENTES.CODCLIE_EXT"));
                    String nomeCliente = cursorContatos.getString(cursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));


                    lstfiltrocontatos = new Contatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null,
                            0, 0, codClieExt, nomeCliente);
                    DadosListContatos.add(lstfiltrocontatos);
                } while (cursorContatos.moveToNext());
            cursorContatos.close();

        } else {
            Toast.makeText(this, "Nenhum contato encontrado!", Toast.LENGTH_SHORT).show();
        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        return DadosListContatos;
    }*/


    @Override
    public void run() {
        if (flag == 1) {
            flag = 0;
            try {
                FragmentContatos frag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFrag");
                if (frag == null) {
                    frag = new FragmentContatos();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container2, frag, "mainFrag");
                    ft.commit();
                }
            } catch (Exception E) {

            }
            if (pDialog.isShowing())
                pDialog.dismiss();

        } else if (flag == 2) {

            flag = 0;
            try {

                    FragmentFiltroContatos frag = new FragmentFiltroContatos();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.rl_fragment_container2, frag, "mainFrag");
                    ft.commit();
            } catch (Exception E) {

            }
        }

    }

    public List<FiltroContatos> listarContatos() {

        Cursor CursorContatos = DB.rawQuery("select CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, " +
                "CONTATO.TEL2, CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, " +
                "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                "CONTATO.CODVENDEDOR, CONTATO.CODBAIRRO, " +
                "CLIENTES.NOMERAZAO, CONTATO.CODCIDADE, CONTATO.CODCLIENTE, CLIENTES.CODCLIE_EXT " +
                " from CONTATO " +
                " join CLIENTES on CLIENTES.CODCLIE_EXT = CONTATO.CODCLIE_EXT " +
                "WHERE CONTATO.NOME LIKE '%" + editQuery + "%' OR CLIENTES.NOMERAZAO" +
                " LIKE '%" + editQuery + "%'" +

                " order by CLIENTES.NOMERAZAO ", null);

        CursorContatos.moveToFirst();
        ArrayList<FiltroContatos> DadosList = new ArrayList<FiltroContatos>();

        if (CursorContatos.getCount() > 0) {
            do {
                String nome = CursorContatos.getString(CursorContatos.getColumnIndex("NOME"));
                String cargo = CursorContatos.getString(CursorContatos.getColumnIndex("CARGO"));
                String email = CursorContatos.getString(CursorContatos.getColumnIndex("EMAIL"));
                String tel1 = CursorContatos.getString(CursorContatos.getColumnIndex("TEL1"));
                String tel2 = CursorContatos.getString(CursorContatos.getColumnIndex("TEL2"));
                String Doc = CursorContatos.getString(CursorContatos.getColumnIndex("DOCUMENTO"));
                String Data = CursorContatos.getString(CursorContatos.getColumnIndex("DATA"));
                String Cep = CursorContatos.getString(CursorContatos.getColumnIndex("CEP"));
                String Endereco = CursorContatos.getString(CursorContatos.getColumnIndex("ENDERECO"));
                String Num = CursorContatos.getString(CursorContatos.getColumnIndex("NUMERO"));
                String Compl = CursorContatos.getString(CursorContatos.getColumnIndex("COMPLEMENTO"));
                String uf = CursorContatos.getString(CursorContatos.getColumnIndex("UF"));
                int codClieExt = CursorContatos.getInt(CursorContatos.getColumnIndex("CLIENTES.CODCLIE_EXT"));
                String nomeCliente = CursorContatos.getString(CursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));


                lstfiltrocontatos = new FiltroContatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null,
                        0, 0, codClieExt, nomeCliente);
                DadosList.add(lstfiltrocontatos);
            } while (CursorContatos.moveToNext());
        } else {
            Toast.makeText(this, getString(R.string.no_contacts_found), Toast.LENGTH_SHORT).show();
        }
        CursorContatos.close();
        if (Dialogo.isShowing()) {
            Dialogo.dismiss();
        }

        flag = 0;

        return DadosList;
    }
}

