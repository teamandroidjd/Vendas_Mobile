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
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.fragments.FragmentCliente;
import com.jdsystem.br.vendasmobile.fragments.FragmentProdutos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ConsultaClientes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    ProgressDialog pDialog;
    private Handler handler = new Handler();
    public ListAdapterClientes adapter;
    String sCodVend, URLPrincipal, sincclieenvio, sincclie;
    ListView edtCliente;
    SearchView sv;
    String UsuarioLogado;
    SQLiteDatabase DB;


    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public static final String CONSULTA_CLIENTE = "CONSULTA_CLIENTE";
    public static String PESQUISAR_CLIENTE_NOME = "Razão Social";
    public static String PESQUISAR_CLIENTE_FANTASIA = "Nome Fantasia";
    public static String PESQUISAR_CLIENTE_CIDADE = "Cidade";
    public static String PESQUISAR_CLIENTE_BAIRRO = "Bairro";
    private Spinner adm_sp_filtrarcliente;
    private SimpleCursorAdapter AdapterClientes;
    private List<String> array_spinner = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String selecao_spinner, CodVendedor, usuario, senha, telaInvocada, chavepedido, numPedido,codEmpresa, codClie;
    //private Cursor cursor;
    private EditText adm_txt_pesquisacliente;
    private ListView adm_listview_cliente;
    private ImageView imgStatus;
    FloatingActionButton cadclie;
    public ProgressDialog dialog;
    public Boolean ConsultaPedido;
    public int CadastroContato;
    Clientes lstclientes;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_listclientes);

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

       /* array_spinner.add(PESQUISAR_CLIENTE_NOME);
        array_spinner.add(PESQUISAR_CLIENTE_FANTASIA);
        array_spinner.add(PESQUISAR_CLIENTE_CIDADE);
        array_spinner.add(PESQUISAR_CLIENTE_BAIRRO);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_spinner);
        adm_sp_filtrarcliente.setAdapter(arrayAdapter);

        adm_sp_filtrarcliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int posicao, long id) {
                selecao_spinner = spinner.getItemAtPosition(posicao).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        pDialog = new ProgressDialog(ConsultaClientes.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.loading_products));
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(ConsultaClientes.this);
        thread.start();

        //mostrar_clientes_listview();
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
        adm_sp_filtrarcliente = (Spinner) findViewById(R.id.adm_sp_filtrarcliente);

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
        }
        return super.onOptionsItemSelected(item);
    }


    private void mostrar_clientes_listview() {

        SqliteClienteBean cliBean = new SqliteClienteBean();
        SqliteClienteDao cliDao = new SqliteClienteDao(this);

        final Cursor cursor = cliDao.buscar_todos_cliente(sCodVend);

        String[] colunas = new String[]{cliBean.C_CODIGO_CLIENTE_CURSOR, cliBean.C_NOME_DO_CLIENTE, cliBean.C_NOME_FANTASIA, cliBean.C_CIDADE_CLIENTE,
                cliBean.C_BAIRRO_CLIENTE, cliBean.C_UF_CLIENTE, cliBean.C_TELEFONE_CLIENTE, cliBean.C_CNPJCPF, cliBean.C_ENVIADO};
        final int[] para;

        if (cliBean.C_ENVIADO == "1") {
            para = new int[]{R.id.lblCodClie, R.id.lblNomerazao, R.id.lblNomeFanClie, R.id.lblCidade, R.id.lblBairro, R.id.lblEstado,
                    R.id.lblTel, R.id.lblCNPJ, R.id.bola_vermelha};

        } else {
            para = new int[]{R.id.lblCodClie, R.id.lblNomerazao, R.id.lblNomeFanClie, R.id.lblCidade, R.id.lblBairro, R.id.lblEstado,
                    R.id.lblTel, R.id.lblCNPJ, 0};
        }

        //AdapterClientes = new SimpleCursorAdapter(this, R.layout.lstclientes_card, cursor, colunas, para, 0);
        AdapterClientes = new SimpleCursorAdapter(this, R.layout.lstclientes_card, cursor, colunas, para, 0);
        adm_listview_cliente = (ListView) findViewById(R.id.adm_listview_cliente);
        adm_listview_cliente.setAdapter(AdapterClientes);

        if (cursor.getCount() <= 0) {
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

        adm_listview_cliente.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int posicao, long id) {

                if (CadastroContato == 1) {
                    //String TipoContato = "C";
                    Cursor cliente_cursor = (Cursor) listview.getItemAtPosition(posicao);
                    int CodCliente = cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT"));
                    Intent intent = new Intent(getBaseContext(), CadastroContatos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putInt(getString(R.string.intent_codcliente), CodCliente);
                    params.putString(getString(R.string.intent_nomerazao), cliente_cursor.getString(cursor.getColumnIndex("NOMERAZAO")));
                    //params.putString("C",TipoContato);
                    intent.putExtras(params);
                    startActivity(intent);

                } else if (ConsultaPedido.equals(false)) {
                    Cursor cliente_cursor = (Cursor) listview.getItemAtPosition(posicao);
                    Intent intent = new Intent(getBaseContext(), DadosCliente.class);
                    Bundle params = new Bundle();
                    params.putInt(getString(R.string.intent_codcliente), cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                    params.putString(getString(R.string.intent_nomerazao), cliente_cursor.getString(cursor.getColumnIndex("NOMERAZAO")));
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    intent.putExtras(params);
                    startActivity(intent);
                    //finish();

                } else {
                    Cursor cliente_cursor = (Cursor) listview.getItemAtPosition(posicao);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(getString(R.string.intent_codcliente), cliente_cursor.getString(cursor.getColumnIndex("CODCLIE_INT")));
                    setResult(2, returnIntent);
                    finish();
                }
            }
        });

        adm_txt_pesquisacliente = (EditText) findViewById(R.id.adm_txt_pesquisacliente);
        adm_txt_pesquisacliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence texto_digitado, int start, int before, int count) {
                AdapterClientes.getFilter().filter(texto_digitado);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        AdapterClientes.setFilterQueryProvider(new FilterQueryProvider() {

            private Cursor cursor;

            @Override
            public Cursor runQuery(CharSequence valor) {
                SqliteClienteDao cli = new SqliteClienteDao(getApplicationContext());

                if (selecao_spinner == PESQUISAR_CLIENTE_NOME) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_DO_CLIENTE, sCodVend);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_FANTASIA) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_FANTASIA, sCodVend);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_CIDADE) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_CIDADE, sCodVend);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_BAIRRO) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_BAIRRO, sCodVend);
                }

                if (cursor.getCount() <= 0) {
                    Toast.makeText(ConsultaClientes.this, "Nenhum cliente encontrado!", Toast.LENGTH_SHORT).show();
                }

                return cursor;
            }
        });
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


                        lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2,bloqueio,flagintegrado);
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


                        lstclientes = new Clientes(codClieExt, codClieInt, nomeRazao, nomeFantasia, documento, estado, cidade, bairro, Tel1, Tel2,bloqueio,flagintegrado);
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            Toast.makeText(this, "Falha no SQL. Tente novamente!", Toast.LENGTH_LONG).show();
        }
        if (pDialog.isShowing())
            pDialog.dismiss();

        return DadosLisClientes;
    }

    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
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

        } else if (flag == 0 && CadastroContato == 1){
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
                params.putInt(getString(R.string.intent_cad_contato),CadastroContato);
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
        } else if(flag == 2) {
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
                params.putString(getString(R.string.intent_codigoempresa),codEmpresa);
                params.putString("TELA_QUE_CHAMOU",telaInvocada);
                params.putString(getString(R.string.intent_urlprincipal),URLPrincipal);
                frag.setArguments(params);
                ft.commit();
            }
        }
        /*if (dialog.isShowing()) {
            dialog.dismiss();
        }*/
    }
}
