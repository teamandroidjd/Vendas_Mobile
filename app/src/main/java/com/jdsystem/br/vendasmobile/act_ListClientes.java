package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Controller.Lista_produtos;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;

import java.util.ArrayList;
import java.util.List;

public class act_ListClientes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    ProgressDialog pDialog;
    private Handler handler = new Handler();
    public ListAdapterClientes adapter;
    String sCodVend, URLPrincipal;
    ListView edtCliente;
    SearchView sv;
    Clientes lstclientes;
    String UsuarioLogado;
    boolean sincclieenvio, sincclie;


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
    private String selecao_spinner, CodVendedor, usuario, senha, TELA_QUE_CHAMOU;
    //private Cursor cursor;
    private EditText adm_txt_pesquisacliente;
    private ListView adm_listview_cliente;
    private Intent TELA_QUE_CHAMOU_INTENT;
    private ImageView imgStatus;
    public ProgressDialog dialog;
    public Boolean ConsultaPedido;
    public int CadastroContato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_listclientes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                ConsultaPedido = params.getBoolean("consultapedido");
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CadastroContato = params.getInt(getString(R.string.intent_cad_contato));
            }
        }

        declaraobjetos();
        carregausuariologado();

        FloatingActionButton cadclie = (FloatingActionButton) findViewById(R.id.cadclie);
        cadclie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(act_ListClientes.this, act_CadClientes.class);
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
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TELA_QUE_CHAMOU_INTENT = getIntent();
        TELA_QUE_CHAMOU = TELA_QUE_CHAMOU_INTENT.getStringExtra("TELA_QUE_CHAMOU");
        CodVendedor = TELA_QUE_CHAMOU_INTENT.getStringExtra(getString(R.string.intent_codvendedor));
        usuario = TELA_QUE_CHAMOU_INTENT.getStringExtra(getString(R.string.intent_usuario));
        senha = TELA_QUE_CHAMOU_INTENT.getStringExtra(getString(R.string.intent_senha));
        CadastroContato = TELA_QUE_CHAMOU_INTENT.getIntExtra(getString(R.string.intent_cad_contato), 0);

        array_spinner.add(PESQUISAR_CLIENTE_NOME);
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
        });

        mostrar_clientes_listview();

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
        imgStatus = (ImageView) findViewById(R.id.imgStatus);
        adm_sp_filtrarcliente = (Spinner) findViewById(R.id.adm_sp_filtrarcliente);
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
                Cursor cursorVerificaClie = DB.rawQuery("SELECT * FROM CLIENTES", null);
                if (cursorVerificaClie.getCount() == 0) {
                    dialog = new ProgressDialog(act_ListClientes.this);
                    dialog.setTitle(R.string.wait);
                    dialog.setMessage(getString(R.string.primeira_sync_clientes));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setIcon(R.drawable.icon_sync);
                    dialog.setCancelable(false);
                    dialog.show();

                    Thread thread = new Thread(this);
                    thread.start();

                } else {
                    dialog = new ProgressDialog(act_ListClientes.this);
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
                    R.id.lblTel, R.id.lblCNPJ, R.id.bola_laranja};

        } else {
            para = new int[]{R.id.lblCodClie, R.id.lblNomerazao, R.id.lblNomeFanClie, R.id.lblCidade, R.id.lblBairro, R.id.lblEstado,
                    R.id.lblTel, R.id.lblCNPJ, 0};
        }

        //AdapterClientes = new SimpleCursorAdapter(this, R.layout.lstclientes_card, cursor, colunas, para, 0);
        AdapterClientes = new SimpleCursorAdapter(this, R.layout.lstclientes_card, cursor, colunas, para, 0);
        adm_listview_cliente = (ListView) findViewById(R.id.adm_listview_cliente);
        adm_listview_cliente.setAdapter(AdapterClientes);

        if (cursor.getCount() <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(act_ListClientes.this);
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
                    Intent intent = new Intent(getBaseContext(), CadContatos.class);
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
                    Intent intent = new Intent(getBaseContext(), actDadosCliente.class);
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
                    Toast.makeText(act_ListClientes.this, "Nenhum cliente encontrado!", Toast.LENGTH_SHORT).show();
                }

                return cursor;
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
        Intent intent = new Intent(act_ListClientes.this, actListPedidos.class);
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
            Intent intent = new Intent(act_ListClientes.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(act_ListClientes.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_contatos) {
            Intent i = new Intent(act_ListClientes.this, act_ListContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(act_ListClientes.this, actSincronismo.class);
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
        try {
            //actSincronismo.run(this);
            sincclieenvio = actSincronismo.SincronizarClientesEnvioStatic("0", this, usuario, senha);
            if (sincclieenvio == false) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Nenhum cliente a ser enviado.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Novos clientes enviados com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            sincclie = actSincronismo.SincronizarClientesStatic(CodVendedor, this, usuario, senha, 0);
            if (sincclie == false) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Nenhum cliente sincronizado. Verifique!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Clientes sincronizados com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Intent intent = (act_ListClientes.this).getIntent();
            (act_ListClientes.this).finish();
            startActivity(intent);
        } catch (Exception e) {
            e.toString();

        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

    }
}
