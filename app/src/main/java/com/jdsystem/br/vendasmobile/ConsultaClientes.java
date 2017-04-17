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
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.domain.FiltroClientes;
import com.jdsystem.br.vendasmobile.fragments.FragmentCliente;

import java.util.ArrayList;
import java.util.List;

public class ConsultaClientes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private Handler handler = new Handler();
    public ListAdapterClientes adapter;
    Clientes lstclientes;
    FiltroClientes lstFiltroClientes;
    String codVendedor, URLPrincipal, codClie, codEmpresa, sincclieenvio, usuario, senha, sincclie, editQuery,
            UsuarioLogado, telaInvocada, chavepedido, numPedido;
    SQLiteDatabase DB;
    MenuItem searchItem;
    SearchView searchView;
    private ImageView imgStatus;
    FloatingActionButton cadclie;
    public ProgressDialog dialog;
    public Boolean ConsultaPedido;
    public int CadastroContato, flag;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_listclientes);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                ConsultaPedido = params.getBoolean("consultapedido");
                codEmpresa = params.getString(getString(R.string.intent_codigoempresa));
                codClie = params.getString(getString(R.string.intent_codcliente));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CadastroContato = params.getInt(getString(R.string.intent_cad_contato), 0);
                telaInvocada = params.getString("TELA_QUE_CHAMOU");
                flag = params.getInt(getString(R.string.intent_flag));
                numPedido = params.getString(getString(R.string.intent_numpedido));
            }
        }
        declaraobjetos();
        carregausuariologado();

        dialog = new ProgressDialog(ConsultaClientes.this);
        dialog.setTitle(getString(R.string.wait));
        dialog.setMessage(getString(R.string.loading_products));
        dialog.setCancelable(false);
        dialog.show();

        Thread thread = new Thread(ConsultaClientes.this);
        thread.start();

    }

    public void cadcliente(View view) {
        Intent intent = new Intent(ConsultaClientes.this, CadastroClientes.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putInt(getString(R.string.intent_listaclie), 1);
        intent.putExtras(params);
        startActivity(intent);
        finish();

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

    private void declaraobjetos() {
        DB = new ConfigDB(this).getReadableDatabase();
        cadclie = (FloatingActionButton) findViewById(R.id.cadclie);
        imgStatus = (ImageView) findViewById(R.id.imgStatus);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sincroniza_cliente, menu);
        searchItem = menu.findItem(R.id.action_searchable_activity);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                dialog = new ProgressDialog(ConsultaClientes.this);
                dialog.setIndeterminate(true);
                dialog.setTitle(getString(R.string.wait));
                dialog.setMessage(getString(R.string.searchingclients));
                dialog.setCancelable(false);
                dialog.setProgress(0);
                dialog.show();

                query.toString();
                editQuery = query;
                searchView.setQuery("",false);
               // searchView.clearFocus();

                //flag = 3;

                Thread thread = new Thread(ConsultaClientes.this);
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

                flag = 0;
                searchView.onActionViewCollapsed();
                Thread thread = new Thread(ConsultaClientes.this);
                thread.start();

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_sinc_cliente) {
            Boolean ConexOk = Util.checarConexaoCelular(this);
            SQLiteDatabase DB = new ConfigDB(this).getReadableDatabase();
            if (ConexOk == true) {
                flag = 1;
                Cursor cursorVerificaClie = DB.rawQuery("SELECT * FROM CLIENTES", null);
                if (cursorVerificaClie.getCount() == 0) {
                    dialog = new ProgressDialog(ConsultaClientes.this);
                    dialog.setTitle(R.string.wait);
                    dialog.setMessage(getString(R.string.primeira_sync_clientes));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setIcon(R.drawable.icon_sync);
                    dialog.setCancelable(false);
                    dialog.show();

                    Thread thread = new Thread(this);
                    thread.start();

                } else {
                    dialog = new ProgressDialog(ConsultaClientes.this);
                    dialog.setCancelable(false);
                    dialog.setTitle(getString(R.string.wait));
                    dialog.setMessage(getString(R.string.sync_clients));
                    dialog.show();

                    Thread thread = new Thread(this);
                    thread.start();
                }
            } else {
                Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.action_searchable_activity) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<Clientes> CarregarClientes() {
        ArrayList<Clientes> DadosLisClientes = new ArrayList<Clientes>();
        if (editQuery == null) {
            try {
                Cursor cursorparametro = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP", null);
                cursorparametro.moveToFirst();
                String cliexvend = cursorparametro.getString(cursorparametro.getColumnIndex("HABCRITSITCLIE"));
                cursorparametro.close();
                if (cliexvend.equals("S")) {
                    Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                            " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                            " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                            " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                            " WHERE ATIVO = 'S' AND CODVENDEDOR = " + codVendedor + " ORDER BY NOMEFAN, NOMERAZAO ", null);
                    cursorClientes.moveToFirst();
                    if (cursorClientes.getCount() > 0) {
                        do {
                            String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                            String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                            String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                            String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                            String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                            String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                            String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                            String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                            //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                            String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                            String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                            String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                            String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                            lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                            DadosLisClientes.add(lstclientes);
                        } while (cursorClientes.moveToNext());
                        cursorClientes.close();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaClientes.this);
                        builder.setTitle(R.string.app_namesair);
                        builder.setIcon(R.drawable.logo_ico);
                        builder.setMessage(R.string.alertsyncclients)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
                    Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                            " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                            " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                            " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                            " WHERE ATIVO = 'S' ORDER BY NOMEFAN, NOMERAZAO ", null);
                    cursorClientes.moveToFirst();
                    if (cursorClientes.getCount() > 0) {
                        do {
                            String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                            String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                            String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                            String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                            String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                            String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                            String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                            String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                            //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                            String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                            String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                            String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                            String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                            lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                            DadosLisClientes.add(lstclientes);
                        } while (cursorClientes.moveToNext());
                        cursorClientes.close();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaClientes.this);
                        builder.setTitle(R.string.app_namesair);
                        builder.setIcon(R.drawable.logo_ico);
                        builder.setMessage(R.string.alertsyncclients)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
            } catch (Exception e) {
                e.toString();
                if (dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                Cursor cursorparametro = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP", null);
                cursorparametro.moveToFirst();
                String cliexvend = cursorparametro.getString(cursorparametro.getColumnIndex("HABCRITSITCLIE"));
                cursorparametro.close();
                if (cliexvend.equals("S")) {
                    Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                            " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                            " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                            " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                            " WHERE ((ATIVO = 'S') AND (CODVENDEDOR = " + codVendedor + ")) AND ((CLIENTES.NOMERAZAO LIKE '%" + editQuery + "%') OR (CLIENTES.NOMEFAN LIKE '%" + editQuery + "%') OR (CLIENTES.CNPJ_CPF LIKE '%" + editQuery + "%')) ORDER BY NOMEFAN, NOMERAZAO ", null);
                    cursorClientes.moveToFirst();
                    if (cursorClientes.getCount() > 0) {
                        do {
                            String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                            String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                            String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                            String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                            String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                            String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                            String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                            String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                            //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                            String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                            String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                            String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                            String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                            lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                            DadosLisClientes.add(lstclientes);
                        } while (cursorClientes.moveToNext());
                        cursorClientes.close();
                    } else {
                        Toast.makeText(this, "Nenhum cliente encontrando. Verifique!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                            " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                            " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                            " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                            " WHERE ATIVO = 'S' OR CLIENTES.NOMERAZAO LIKE '%" + editQuery + "%' OR CLIENTES.NOMEFAN LIKE '%" + editQuery + "%' OR CLIENTES.CNPJ_CPF LIKE '%" + editQuery + "%'   ORDER BY NOMEFAN, NOMERAZAO ORDER BY NOMEFAN, NOMERAZAO ", null);
                    cursorClientes.moveToFirst();
                    if (cursorClientes.getCount() > 0) {
                        do {
                            String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                            String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                            String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                            String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                            String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                            String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                            String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                            String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                            //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                            String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                            String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                            String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                            String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                            lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                            DadosLisClientes.add(lstclientes);
                        } while (cursorClientes.moveToNext());
                        cursorClientes.close();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaClientes.this);
                        builder.setTitle(R.string.app_namesair);
                        builder.setIcon(R.drawable.logo_ico);
                        builder.setMessage(R.string.alertsyncclients)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
            } catch (Exception e) {
                e.toString();
                if (dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
            }

        }
        if (dialog.isShowing())
            dialog.dismiss();

        return DadosLisClientes;
    }

    public List<FiltroClientes> CarregarClientesFiltrados() {

        ArrayList<FiltroClientes> DadosLisClientes = new ArrayList<FiltroClientes>();
        try {
            Cursor cursorparametro = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP", null);
            cursorparametro.moveToFirst();
            String cliexvend = cursorparametro.getString(cursorparametro.getColumnIndex("HABCRITSITCLIE"));
            cursorparametro.close();
            if (cliexvend.equals("S")) {
                Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                        " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                        " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                        " WHERE ((ATIVO = 'S') AND (CODVENDEDOR = " + codVendedor + ")) AND ((CLIENTES.NOMERAZAO LIKE '%" + editQuery + "%') OR (CLIENTES.NOMEFAN LIKE '%" + editQuery + "%') OR (CLIENTES.CNPJ_CPF LIKE '%" + editQuery + "%')) ORDER BY NOMEFAN, NOMERAZAO ", null);
                cursorClientes.moveToFirst();
                if (cursorClientes.getCount() > 0) {
                    do {
                        String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                        String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                        String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                        String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                        String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                        String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                        String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                        String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                        //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                        String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                        String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                        String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                        String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                        lstFiltroClientes = new FiltroClientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                        DadosLisClientes.add(lstFiltroClientes);
                    } while (cursorClientes.moveToNext());
                    cursorClientes.close();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaClientes.this);
                    builder.setTitle(R.string.app_namesair);
                    builder.setIcon(R.drawable.logo_ico);
                    builder.setMessage(R.string.alertsyncclients)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else {
                Cursor cursorClientes = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, CODCLIE_INT, BLOQUEIO, FLAGINTEGRADO, EMAIL, REGIDENT, CNPJ_CPF, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                        " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                        " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                        " WHERE ATIVO = 'S' OR CLIENTES.NOMERAZAO LIKE '%" + editQuery + "%' OR CLIENTES.NOMEFAN LIKE '%" + editQuery + "%' OR CLIENTES.CNPJ_CPF LIKE '%" + editQuery + "%'   ORDER BY NOMEFAN, NOMERAZAO ORDER BY NOMEFAN, NOMERAZAO ", null);
                cursorClientes.moveToFirst();
                if (cursorClientes.getCount() > 0) {
                    do {
                        String codClieExt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_EXT"));
                        String codClieInt = cursorClientes.getString(cursorClientes.getColumnIndex("CODCLIE_INT"));
                        String nomeRazao = cursorClientes.getString(cursorClientes.getColumnIndex("NOMERAZAO"));
                        String nomeFantasia = cursorClientes.getString(cursorClientes.getColumnIndex("NOMEFAN"));
                        String documento = cursorClientes.getString(cursorClientes.getColumnIndex("CNPJ_CPF"));
                        String estado = cursorClientes.getString(cursorClientes.getColumnIndex("UF"));
                        String cidade = cursorClientes.getString(cursorClientes.getColumnIndex("CIDADE"));
                        String bairro = cursorClientes.getString(cursorClientes.getColumnIndex("BAIRRO"));
                        //String CEP = cursorClientes.getString(cursorClientes.getColumnIndex("CEP"));
                        String Tel1 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL1"));
                        String Tel2 = cursorClientes.getString(cursorClientes.getColumnIndex("TEL2"));
                        String bloqueio = cursorClientes.getString(cursorClientes.getColumnIndex("BLOQUEIO"));
                        String flagintegrado = cursorClientes.getString(cursorClientes.getColumnIndex("FLAGINTEGRADO"));


                        lstFiltroClientes = new FiltroClientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2, bloqueio, flagintegrado);
                        DadosLisClientes.add(lstFiltroClientes);
                    } while (cursorClientes.moveToNext());
                    cursorClientes.close();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConsultaClientes.this);
                    builder.setTitle(R.string.app_namesair);
                    builder.setIcon(R.drawable.logo_ico);
                    builder.setMessage(R.string.alertsyncclients)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        } catch (Exception e) {
            e.toString();
            if (dialog.isShowing())
                dialog.dismiss();
            Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
        }
        if (dialog.isShowing())
            dialog.dismiss();

        return DadosLisClientes;
    }

    @Override
    public void onBackPressed() {
        switch (flag) {
            case 0:
                Intent intent = new Intent(ConsultaClientes.this, ConsultaPedidos.class);
                Bundle params = new Bundle();
                params.putInt(getString(R.string.intent_flag), flag);
                params.putString(getString(R.string.intent_numpedido), numPedido);
                params.putString(getString(R.string.intent_chavepedido), chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                intent.putExtras(params);
                startActivity(intent);
                finish();
                break;
            case 1:
                Intent intent2 = new Intent(ConsultaClientes.this, ConsultaClientes.class);
                Bundle params2 = new Bundle();
                params2.putInt(getString(R.string.intent_flag), flag);
                params2.putString(getString(R.string.intent_numpedido), numPedido);
                params2.putString(getString(R.string.intent_chavepedido), chavepedido);
                params2.putString(getString(R.string.intent_usuario), usuario);
                params2.putString(getString(R.string.intent_senha), senha);
                params2.putString(getString(R.string.intent_codvendedor), codVendedor);
                params2.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params2.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                intent2.putExtras(params2);
                startActivity(intent2);
                finish();
                break;
            case 2:
                Intent intent3 = new Intent(ConsultaClientes.this, ConsultaClientes.class);
                Bundle params3 = new Bundle();
                params3.putInt(getString(R.string.intent_flag), flag);
                params3.putString(getString(R.string.intent_numpedido), numPedido);
                params3.putString(getString(R.string.intent_chavepedido), chavepedido);
                params3.putString(getString(R.string.intent_usuario), usuario);
                params3.putString(getString(R.string.intent_senha), senha);
                params3.putString(getString(R.string.intent_codvendedor), codVendedor);
                params3.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params3.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                params3.putString("TELA_QUE_CHAMOU", telaInvocada);
                intent3.putExtras(params3);
                startActivity(intent3);
                finish();
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            //
        } else if (id == R.id.nav_produtos) {
            Intent intent = new Intent(ConsultaClientes.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(ConsultaClientes.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaClientes.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(ConsultaClientes.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", codVendedor);
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

    @Override
    public void run() {
        if (flag == 0 && CadastroContato == 0) {
            //uttilizado para carregar todos os clientes.
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragA");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragA");
                params.putInt(getString(R.string.intent_flag), flag);
                params.putString(getString(R.string.intent_numpedido), numPedido);
                params.putString(getString(R.string.intent_chavepedido), chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putBoolean("consultapedido", true);
                frag.setArguments(params);
                ft.commit();
            } else {
                // utilizado para o filtro de clientes
                FragmentCliente newfrag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragB");
                Bundle newparams = new Bundle();
                if (newfrag == null) {
                    newfrag = new FragmentCliente();
                    FragmentTransaction newft = getSupportFragmentManager().beginTransaction();
                    newft.replace(R.id.rl_fragment_container, newfrag, "mainFragB");
                    newparams.putInt(getString(R.string.intent_flag), flag);
                    newparams.putString(getString(R.string.intent_numpedido), numPedido);
                    newparams.putString(getString(R.string.intent_chavepedido), chavepedido);
                    newparams.putString(getString(R.string.intent_usuario), usuario);
                    newparams.putString(getString(R.string.intent_senha), senha);
                    newparams.putString(getString(R.string.intent_codvendedor), codVendedor);
                    newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    newparams.putBoolean("consultapedido", true);
                    newfrag.setArguments(newparams);
                    newft.commit();
                }
            }

        } else if (flag == 0 && CadastroContato == 1) {
            // utilizado para carregar todos clientes para inclusão de contato
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragA");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragA");
                params.putInt(getString(R.string.intent_flag), flag);
                params.putString(getString(R.string.intent_numpedido), numPedido);
                params.putString(getString(R.string.intent_chavepedido), chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            } else {
                // utilizado para o filtro de clientes para inclusão de contato
                FragmentCliente newfrag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragB");
                Bundle newparams = new Bundle();
                if (newfrag == null) {
                    newfrag = new FragmentCliente();
                    FragmentTransaction newft = getSupportFragmentManager().beginTransaction();
                    newft.replace(R.id.rl_fragment_container, newfrag, "mainFragB");
                    newparams.putInt(getString(R.string.intent_flag), flag);
                    newparams.putString(getString(R.string.intent_numpedido), numPedido);
                    newparams.putString(getString(R.string.intent_chavepedido), chavepedido);
                    newparams.putString(getString(R.string.intent_usuario), usuario);
                    newparams.putString(getString(R.string.intent_senha), senha);
                    newparams.putString(getString(R.string.intent_codvendedor), codVendedor);
                    newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    newparams.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                    newfrag.setArguments(newparams);
                    newft.commit();
                }
            }

        } else if (flag == 1) {
            try {
                sincclieenvio = Sincronismo.SincronizarClientesEnvioStatic("0", this, usuario, senha);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), sincclieenvio, Toast.LENGTH_SHORT).show();
                    }
                });
                sincclie = Sincronismo.SincronizarClientesStatic(codVendedor, this, usuario, senha, 0);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), sincclie, Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = (ConsultaClientes.this).getIntent();
                (ConsultaClientes.this).finish();
                startActivity(intent);
            } catch (Exception e) {
                e.toString();

            }
        } else if (flag == 2) {
            //utilizada para carregar todos os clientes para inclusão no pedido.
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragA");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragA");
                params.putInt(getString(R.string.intent_flag), flag);
                params.putString(getString(R.string.intent_numpedido), numPedido);
                params.putString(getString(R.string.intent_chavepedido), chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString("TELA_QUE_CHAMOU", telaInvocada);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            } else {
                //utilizada para filtrar cliente para inclusão no pedido.
                FragmentCliente newfrag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragB");
                Bundle newparams = new Bundle();
                if (newfrag == null) {
                    newfrag = new FragmentCliente();
                    FragmentTransaction newft = getSupportFragmentManager().beginTransaction();
                    newft.replace(R.id.rl_fragment_container, newfrag, "mainFragB");
                    newparams.putInt(getString(R.string.intent_flag), flag);
                    newparams.putString(getString(R.string.intent_numpedido), numPedido);
                    newparams.putString(getString(R.string.intent_chavepedido), chavepedido);
                    newparams.putString(getString(R.string.intent_usuario), usuario);
                    newparams.putString(getString(R.string.intent_senha), senha);
                    newparams.putString(getString(R.string.intent_codvendedor), codVendedor);
                    newparams.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                    newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    newparams.putString("TELA_QUE_CHAMOU", telaInvocada);
                    newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    newfrag.setArguments(newparams);
                    newft.commit();
                }

            }
        } /*else if (flag == 3) {
            FragmentFiltroClientes frag = (FragmentFiltroClientes) getSupportFragmentManager().findFragmentByTag("mainFragF");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentFiltroClientes();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragF");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavePedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString("TELA_QUE_CHAMOU", telaInvocada);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            }
        } else if (flag == 3 && CadastroContato == 1) {
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragA");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragD");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavePedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                frag.setArguments(params);
                ft.commit();
            }
        }*/
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}