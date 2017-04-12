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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.domain.FiltroClientes;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.fragments.FragmentCliente;
import com.jdsystem.br.vendasmobile.fragments.FragmentFiltroClientes;
import com.jdsystem.br.vendasmobile.fragments.FragmentProdutos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ConsultaClientes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private Handler handler = new Handler();
    public ListAdapterClientes adapter;
    Clientes lstclientes;
    FiltroClientes lstFiltroClientes;
    String sCodVend, URLPrincipal, codClie, codEmpresa, sincclieenvio, usuario, senha, sincclie, editQuery, UsuarioLogado, telaInvocada, chavepedido, numPedido;
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
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                ConsultaPedido = params.getBoolean("consultapedido");
                codEmpresa = params.getString(getString(R.string.intent_codigoempresa));
                codClie = params.getString(getString(R.string.intent_codcliente));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CadastroContato = params.getInt(getString(R.string.intent_cad_contato), 0);
                telaInvocada = params.getString("TELA_QUE_CHAMOU");
                flag = params.getInt(getString(R.string.intent_flag));
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
        params.putString(getString(R.string.intent_codvendedor), sCodVend);
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
                searchView.clearFocus();

                flag = 3;

                Thread thread = new Thread(ConsultaClientes.this);
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
                        " WHERE ATIVO = 'S' AND CODVENDEDOR = " + sCodVend + " ORDER BY NOMEFAN, NOMERAZAO ", null);
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
                        " WHERE ((ATIVO = 'S') AND (CODVENDEDOR = " + sCodVend + ")) AND ((CLIENTES.NOMERAZAO LIKE '%" + editQuery + "%') OR (CLIENTES.NOMEFAN LIKE '%" + editQuery + "%') OR (CLIENTES.CNPJ_CPF LIKE '%" + editQuery + "%')) ORDER BY NOMEFAN, NOMERAZAO ", null);
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
        if (editQuery != null) {
            Intent intent = new Intent(ConsultaClientes.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else {

            Intent intent = new Intent(ConsultaClientes.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
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
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(ConsultaClientes.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(ConsultaClientes.this, ConsultaContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(ConsultaClientes.this, Sincronismo.class);
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

    @Override
    public void run() {
        if (flag == 0 && CadastroContato == 0) {
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragD");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragD");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                frag.setArguments(params);
                ft.commit();
            }

        } else if (flag == 0 && CadastroContato == 1) {
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragD");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragD");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putInt(getString(R.string.intent_cad_contato), CadastroContato);
                frag.setArguments(params);
                ft.commit();
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
                sincclie = Sincronismo.SincronizarClientesStatic(sCodVend, this, usuario, senha, 0);
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
            FragmentCliente frag = (FragmentCliente) getSupportFragmentManager().findFragmentByTag("mainFragD");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentCliente();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragD");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString("TELA_QUE_CHAMOU", telaInvocada);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            }
        } else if (flag == 3) {
            FragmentFiltroClientes frag = (FragmentFiltroClientes) getSupportFragmentManager().findFragmentByTag("mainFragE");
            Bundle params = new Bundle();
            if (frag == null) {
                frag = new FragmentFiltroClientes();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.rl_fragment_container, frag, "mainFragE");
                params.putInt("flag", flag);
                params.putString("numpedido", numPedido);
                params.putString("chave", chavepedido);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                params.putString("TELA_QUE_CHAMOU", telaInvocada);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            }

        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
