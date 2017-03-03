package com.jdsystem.br.vendasmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoDao;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class act_ListProdutos extends ActionBarActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {


    String sCodVend, URLPrincipal;
    public static String DESCRICAO_PRODUTO = "Descrição";
    public static String CODIGO_PRODUTO = "Código";
    public static String CATEGORIA_PRODUTO = "Classes";
    String UsuarioLogado;

    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    private Spinner prod_sp_produtos;
    private SimpleCursorAdapter adapter;
    private List<String> array_spinner = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String selecao_spinner;
    private String dtUltAtu, usuario, senha;
    private Cursor cursor;
    private EditText prod_txt_pesquisaproduto;
    private ListView prod_listview_produtotemp;
    private ListView prod_listview_itenstemp;
    private AlertDialog.Builder alerta;
    private AlertDialog dlg;
    private ProgressDialog dialog;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
                usuario = params.getString("usuario");
                senha = params.getString("senha");
                //Pedido = params.getBoolean("pedido");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        usuariologado.setText("Olá " +UsuarioLogado+"!");

        // atualiza_listview_com_os_itens_da_venda();

        array_spinner.add(DESCRICAO_PRODUTO);
        array_spinner.add(CODIGO_PRODUTO);
        array_spinner.add(CATEGORIA_PRODUTO);


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_spinner);
        prod_sp_produtos = (Spinner) findViewById(R.id.prod_sp_produtos);
        prod_sp_produtos.setAdapter(arrayAdapter);

        prod_sp_produtos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int posicao, long id) {
                selecao_spinner = spinner.getItemAtPosition(posicao).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        carrega_produto_para_venda();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


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
            Boolean ConexOk = Util.checarConexaoCelular(act_ListProdutos.this);
            if (ConexOk == true) {
                dialog = new ProgressDialog(this);
                dialog.setCancelable(false);
                dialog.setMessage("Sincronizando Produtos");
                dialog.setTitle("Aguarde");
                dialog.show();

                Thread thread = new Thread(this);
                thread.start();

            } else {
                Toast.makeText(act_ListProdutos.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void run() {
        try {
            actSincronismo.run(act_ListProdutos.this);
            actSincronismo.SincronizarProdutosStatic(dtUltAtu, act_ListProdutos.this, true);

            Intent intent = (act_ListProdutos.this).getIntent();
            (act_ListProdutos.this).finish();
            startActivity(intent);
        } catch (Exception e){
            e.toString();
        }
        if (dialog.isShowing())
            dialog.dismiss();

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
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            intent.putExtras(params);
            startActivity(intent);

        } else if (id == R.id.nav_produtos) {
            Intent i = new Intent(act_ListProdutos.this, act_ListProdutos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            //finish();

        } else if (id == R.id.nav_pedidos) {
            Intent i = new Intent(act_ListProdutos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);
            //finish();

        } else if(id == R.id.nav_contatos){
            Intent i = new Intent(act_ListProdutos.this, act_ListContatos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            params.putString("usuario", usuario);
            params.putString("senha", senha);
            i.putExtras(params);
            startActivity(i);

        }else if (id == R.id.nav_sincronismo) {
            Intent i = new Intent(act_ListProdutos.this, actSincronismo.class);
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

    private void carrega_produto_para_venda() {

        SqliteProdutoBean prdBean = new SqliteProdutoBean();
        SqliteProdutoDao prdDao = new SqliteProdutoDao(getApplicationContext());

        final Cursor cursor = prdDao.buscar_produtos(1);


        String[] colunas = new String[]{
                prdBean.P_PRODUTO_SIMPLECURSOR,
                //prdBean.P_CODIGO_PRODUTO,
                //prdBean.P_CODIGO_BARRAS,
                prdBean.P_DESCRICAO_PRODUTO,
                prdBean.P_UNIDADE_MEDIDA,
                prdBean.P_QUANTIDADE_PRODUTO,
                //prdBean.P_CATEGORIA_PRODUTO,
                prdBean.P_STATUS_PRODUTO,
                prdBean.P_APRESENTACAO_PRODUTO,
                prdBean.P_DESCRICAO_TAB1,
                prdBean.P_DESCRICAO_TAB2,
                prdBean.P_DESCRICAO_TAB3,
                prdBean.P_DESCRICAO_TAB4,
                prdBean.P_DESCRICAO_TAB5,
                prdBean.P_DESCRICAO_TAB6,
                prdBean.P_DESCRICAO_TAB7,
                prdBean.P_PRECO_PROD_PADRAO,   //VLVENDA1
                prdBean.P_PRECO_PROD_VLVENDA2, //VLVENDA2
                prdBean.P_PRECO_PROD_VLVENDA3, //VLVENDA3
                prdBean.P_PRECO_PROD_VLVENDA4, //VLVENDA4
                prdBean.P_PRECO_PROD_VLVENDA5, //VLVENDA5
                prdBean.P_PRECO_PROD_VLVENDAP1,//PROMOCAO_A
                prdBean.P_PRECO_PROD_VLVENDAP2 //PROMOCAO_B
        };

        int[] to = new int[]{
                R.id.txt_codprod,
                //R.id.prod_txv_prd_codigobarras,
                R.id.txt_descricao,
                R.id.txtunvenda,
                R.id.txt_qtdestoque,
                //R.id.prod_txv_prd_quantidade,
                //R.id.prod_txv_prd_categoria,
                R.id.txtStatus,
                R.id.txtapres,
                R.id.lbl7,
                R.id.lbl8,
                R.id.lbl9,
                R.id.lbl10,
                R.id.lbl11,
                R.id.lbl12,
                R.id.lbl13,
                R.id.txtpreco,
                R.id.txtprecoauxiliara,
                R.id.txtprecoauxiliarb,
                R.id.txtprecoauxiliarc,
                R.id.txtprecoauxiliard,
                R.id.txtprecopromocaoa,
                R.id.txtprecopromocaob
        };

        try {
            adapter = new SimpleCursorAdapter(this, R.layout.lstprodutos_card, cursor, colunas, to, 0);
            prod_listview_produtotemp = (ListView) findViewById(R.id.prod_listview_produtotemp);
            prod_listview_produtotemp.setAdapter(adapter);

            prod_listview_produtotemp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> listView, View view, int posicao, long l) {
                    Cursor produto = (Cursor) listView.getItemAtPosition(posicao);

                    Intent intent = new Intent(getBaseContext(), actDadosProdutos.class);
                    Bundle params = new Bundle();
                    params.putString("codproduto", produto.getString(produto.getColumnIndex("CODITEMANUAL")));
                    intent.putExtras(params);
                    startActivity(intent);
                }
            });
        } catch (Exception E) {
            Toast.makeText(this, E.getMessage().toString(), Toast.LENGTH_SHORT).show();

        }
        prod_txt_pesquisaproduto = (EditText) findViewById(R.id.prod_txt_pesquisaproduto);
        prod_txt_pesquisaproduto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence texto_digitado, int start, int before, int count) {
                adapter.getFilter().filter(texto_digitado);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            private Cursor cursor;

            @Override
            public Cursor runQuery(CharSequence valor) {
                try {
                    SqliteProdutoDao prdDao = new SqliteProdutoDao(getApplicationContext());
                    if (selecao_spinner == DESCRICAO_PRODUTO) {
                        this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.DESCRICAO_PRODUTO, 1);
                    }
                    if (selecao_spinner == CATEGORIA_PRODUTO) {
                        this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.CATEGORIA_PRODUTO, 1);
                    }
                    if (selecao_spinner == CODIGO_PRODUTO) {
                        this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.CODIGO_PRODUTO, 1);
                    }
                }catch (Exception e){
                    e.toString();
                }
                return cursor;
            }
        });
    }


}
