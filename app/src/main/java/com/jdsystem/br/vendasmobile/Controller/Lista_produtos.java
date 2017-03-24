package com.jdsystem.br.vendasmobile.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.jdsystem.br.vendasmobile.ConfigConex;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConfigWeb;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoDao;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.Model.Sqlite_VENDADAO;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.actLogin;
import com.jdsystem.br.vendasmobile.actSincronismo;
import com.jdsystem.br.vendasmobile.act_ListProdutos;
import com.jdsystem.br.vendasmobile.adapter.ListaItensTemporariosAdapter;
import com.jdsystem.br.vendasmobile.adapter.ListaItensVendaAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Lista_produtos extends AppCompatActivity implements Runnable {


    public static String DESCRICAO_PRODUTO = "Descrição";
    public static String CODIGO_PRODUTO = "Código";
    public static String CATEGORIA_PRODUTO = "Classes";

    private Spinner prod_sp_produtos;
    private SimpleCursorAdapter adapter;
    private List<String> array_spinner = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String selecao_spinner;
    private Cursor cursor;
    private EditText prod_txt_pesquisaproduto;
    private ListView prod_listview_produtotemp;
    private ListView prod_listview_itenstemp;
    private ListView ListView_ItensVendidos;

    private Handler handler = new Handler();
    private Intent Codigo;
    private String NumPedido, spreco, tab1, tab2, tab3, tab4, tab5, tab6, tab7, sCodvend, chavepedido, usuario, senha;
    private int CodigoItem, sprecoprincipal, tabanterior;
    SQLiteDatabase DB;
    private Double qtdestoque;
    private boolean sincprod;

    private SharedPreferences prefs;
    private String PREFS_PRIVATE = "PREFS_PRIVATE";
    private Spinner spntabpreco;


    public String dtUltAtu;

    private Builder alerta;
    private AlertDialog dlg;
    private ProgressDialog dialog;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_produtos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        array_spinner.add(DESCRICAO_PRODUTO);
        array_spinner.add(CODIGO_PRODUTO);
        array_spinner.add(CATEGORIA_PRODUTO);
        Codigo = getIntent();

        NumPedido = Codigo.getStringExtra("numpedido");
        chavepedido = Codigo.getStringExtra("chave");
        sCodvend = Codigo.getStringExtra("CodVendedor");
        usuario = Codigo.getStringExtra("usuario");
        senha = Codigo.getStringExtra("senha");

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
            Boolean ConexOk = Util.checarConexaoCelular(Lista_produtos.this);
            if (ConexOk == true) {
                dialog = new ProgressDialog(this);
                dialog.setCancelable(false);
                dialog.setMessage("Sincronizando Produtos");
                dialog.setTitle("Aguarde");
                dialog.show();

                Thread thread = new Thread(this);
                thread.start();

            } else {
                Toast.makeText(Lista_produtos.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }


    private void carrega_produto_para_venda() {

        SqliteProdutoBean prdBean = new SqliteProdutoBean();
        SqliteProdutoDao prdDao = new SqliteProdutoDao(getApplicationContext());

        final Cursor cursor = prdDao.buscar_produtos(2);

        String[] colunas = new String[]{
                prdBean.P_PRODUTO_SIMPLECURSOR,
                //prdBean.P_CODIGOITEM,
                //prdBean.P_CODIGO_BARRAS,
                prdBean.P_DESCRICAO_PRODUTO,
                prdBean.P_QUANTIDADE_PRODUTO,
                prdBean.P_CATEGORIA_PRODUTO,
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
                prdBean.P_PRECO_PROD_VLVENDAP2,//PROMOCAO_B
        };

        int[] to = new int[]{
                R.id.prod_txv_prd_codigo,
                //R.id.prod_txv_prd_codigobarras,
                R.id.prod_txv_prd_descricaoproduto,
                R.id.txt_qtdestoque,
                R.id.prod_txv_prd_categoria,
                R.id.txtapres,
                R.id.txtprocobase,
                R.id.txtprocoauxA,
                R.id.txtprocoauxB,
                R.id.txtprocoauxC,
                R.id.txtprocoauxD,
                R.id.txtprocopromoA,
                R.id.txtprocopromoB,
                R.id.prod_txv_prd_preco,  //VLVENDA1
                R.id.prod_txv_prd_preco2, //VLVENDA2
                R.id.prod_txv_prd_preco3, //VLVENDA3
                R.id.prod_txv_prd_preco4, //VLVENDA4
                R.id.prod_txv_prd_preco5, //VLVENDA5
                R.id.prod_txv_prd_preco6, //PROMOCAO_A
                R.id.prod_txv_prd_preco7, //PROMOCAO_B
        };

        try {
            adapter = new SimpleCursorAdapter(this, R.layout.lista_produto_item, cursor, colunas, to, 0);
            prod_listview_produtotemp = (ListView) findViewById(R.id.prod_listview_produtotemp);
            prod_listview_produtotemp.setAdapter(adapter);

            prod_listview_produtotemp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> listView, View view, int posicao, long l) {
                    Cursor produto = (Cursor) listView.getItemAtPosition(posicao);
                    informa_produto_na_venda(produto);
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
                SqliteProdutoDao prdDao = new SqliteProdutoDao(getApplicationContext());
                if (selecao_spinner == DESCRICAO_PRODUTO) {
                    this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.DESCRICAO_PRODUTO, 2);
                }
                if (selecao_spinner == CATEGORIA_PRODUTO) {
                    this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.CATEGORIA_PRODUTO, 2);
                }
                if (selecao_spinner == CODIGO_PRODUTO) {
                    this.cursor = prdDao.buscar_produto_na_pesquisa_edittext(valor.toString(), prdDao.CODIGO_PRODUTO, 2);
                }
                return cursor;
            }
        });
    }

    private void informa_produto_na_venda(final Cursor produto_cursor) {
        if (NumPedido.equals("0")) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.info_produto_venda, null);
            alerta = new Builder(this);
            alerta.setCancelable(false);
            alerta.setView(view);

            DB = new ConfigDB(this).getReadableDatabase();

            Cursor Bloqueios = DB.rawQuery("SELECT HABITEMNEGATIVO FROM PARAMAPP", null);
            Bloqueios.moveToFirst();
            final String vendenegativo = Bloqueios.getString(Bloqueios.getColumnIndex("HABITEMNEGATIVO"));
            Bloqueios.close();
            CodigoItem = produto_cursor.getInt(produto_cursor.getColumnIndex("CODIGOITEM"));
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (vendenegativo.equals("N") && ConexOk == true) {
                atualizaEstoqueItem(CodigoItem);
            }

            Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodigoItem, null);
            CursItens.moveToFirst();
            qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
            CursItens.close();

            if (qtdestoque <= 0) {
                Util.msg_toast_personal(getBaseContext(), "Produto sem quantidade disponível.", Util.ALERTA);
                return;
            }


            final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
            final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
            final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
            final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
            final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);
        /*final Spinner*/
            spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);
            DB = new ConfigDB(this).getReadableDatabase();

            spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    SharedPreferences prefsHost = Lista_produtos.this.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                    sprecoprincipal = prefsHost.getInt("spreco", 0);
                    tabanterior = sprecoprincipal;
                    if (sprecoprincipal == 0) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }

                    }
                    if (sprecoprincipal == 1) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 2) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 3) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 4) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 5) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 6) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 7) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            try {
                List<String> DadosListTabPreco = new ArrayList<String>();

                Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                CursorParametro.moveToFirst();
                tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));

                CursorParametro.close();

                String vlvendapadrao = produto_cursor.getString(produto_cursor.getColumnIndex("VENDAPADRAO"));
                vlvendapadrao = vlvendapadrao.trim();
                if (!vlvendapadrao.equals("0,0000")) {
                    BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                    String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precopadrao = Precopadrao.replace('.', ',');
                    DadosListTabPreco.add("Preço base R$: " + Precopadrao);
                }

                String vlvenda1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA1"));
                vlvenda1 = vlvenda1.trim();
                if (!vlvenda1.equals("0,0000")) {
                    BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                    String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco1 = Preco1.replace('.', ',');
                    DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                }

                String vlvenda2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA2"));
                vlvenda2 = vlvenda2.trim();
                if (!vlvenda2.equals("0,0000")) {
                    BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                    String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco2 = Preco2.replace('.', ',');
                    DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                }

                String vlvenda3 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA3"));
                vlvenda3 = vlvenda3.trim();
                if (!vlvenda3.equals("0,0000")) {
                    BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                    String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco3 = Preco3.replace('.', ',');
                    DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                }

                String vlvenda4 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA4"));
                vlvenda4 = vlvenda4.trim();
                if (!vlvenda4.equals("0,0000")) {
                    BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                    String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco4 = Preco4.replace('.', ',');
                    DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                }

                String vlvenda5 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA5"));
                vlvenda5 = vlvenda5.trim();
                if (!vlvenda5.equals("0,0000")) {
                    BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                    String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco5 = Preco5.replace('.', ',');
                    DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                }

                String vlvendap1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP1"));
                vlvendap1 = vlvendap1.trim();
                if (!vlvendap1.equals("0,0000")) {
                    BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                    String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precop1 = Precop1.replace('.', ',');
                    DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                }

                String vlvendap2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP2"));
                vlvendap2 = vlvendap2.trim();
                if (!vlvendap2.equals("0,0000")) {
                    BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                    String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precop2 = Precop2.replace('.', ',');
                    DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                }

                ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(Lista_produtos.this, android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spntabpreco.setAdapter(arrayAdapterTabPreco);

            } catch (Exception E) {
                E.toString();

            }

            SqliteProdutoBean prdBean = new SqliteProdutoBean();
            info_txv_codproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRODUTO_SIMPLECURSOR)));
            info_txv_descricaoproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_DESCRICAO_PRODUTO)));
            info_txv_unmedida.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_UNIDADE_MEDIDA)));


            String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PROD_PADRAO));
            BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
            String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
            Preco = Preco.replace('.', ',');

            info_txv_precoproduto.setText(Preco);
            info_txt_quantidadecomprada.setText("");
            //info_txt_quantidadecomprada.selectAll();

            alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                    if (TAMANHO_TEXTO > 0) {
                        SqliteProdutoBean prdBean = new SqliteProdutoBean();
                        Double QUANTIDADE_DIGITADA = Double.parseDouble(info_txt_quantidadecomprada.getText().toString());
                        String COD_PRODUTO = info_txv_codproduto.getText().toString();
                        String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                        String UNIDADE = info_txv_unmedida.getText().toString();

                        if (QUANTIDADE_DIGITADA > 0) {
                            if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > qtdestoque) {
                                Util.msg_toast_personal(getBaseContext(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                                return;
                            }

                            SqliteVendaD_TempBean itemBean1 = new SqliteVendaD_TempBean();
                            SqliteVendaD_TempBean itemBean2 = new SqliteVendaD_TempBean();
                            SqliteVendaD_TempBean itemBean3 = new SqliteVendaD_TempBean();
                            SqliteVendaD_TempDao itemDao = new SqliteVendaD_TempDao(getApplicationContext());

                            itemBean2.setVendad_prd_codigoTEMP(COD_PRODUTO);
                            itemBean3 = itemDao.buscar_item_na_venda(itemBean2);

                            if (itemBean3 == null) {
                                itemBean1.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                itemBean1.setVendad_prd_descricaoTEMP(DESCRICAO);
                                itemBean1.setVendad_prd_unidadeTEMP(UNIDADE);
                                itemBean1.setVendad_quantidadeTEMP(new BigDecimal(QUANTIDADE_DIGITADA));

                                //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                                String ValorItem = info_txv_precoproduto.getText().toString();
                                if (!ValorItem.equals("0,0000")) {
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                    itemBean1.setVendad_preco_vendaTEMP(venda);

                                    //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                    itemBean1.setVendad_totalTEMP(itemBean1.getSubTotal());
                                    itemDao.insere_item(itemBean1);
                                    atualiza_listview_com_os_itens_da_venda();
                                    finish();
                                } else {
                                    Util.msg_toast_personal(getBaseContext(), "produto com preço de venda zerado", Util.ALERTA);
                                }
                            } else {
                                Util.msg_toast_personal(getBaseContext(), "Este produto já foi adicionado", Util.ALERTA);
                            }

                            //}
                        } else {
                            Util.msg_toast_personal(getApplicationContext(), "A quantidade não foi informada", Util.ALERTA);
                        }

                    } else {
                        Util.msg_toast_personal(getApplicationContext(), "A quantidade não foi informada", Util.ALERTA);
                    }

                }
            });
            alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alerta.show();
        } else {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.info_produto_venda, null);
            alerta = new Builder(this);
            alerta.setCancelable(false);
            alerta.setView(view);

            DB = new ConfigDB(this).getReadableDatabase();

            Cursor Bloqueios = DB.rawQuery("SELECT HABITEMNEGATIVO FROM PARAMAPP", null);
            Bloqueios.moveToFirst();
            final String vendenegativo = Bloqueios.getString(Bloqueios.getColumnIndex("HABITEMNEGATIVO"));
            Bloqueios.close();
            CodigoItem = produto_cursor.getInt(produto_cursor.getColumnIndex("CODIGOITEM"));
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (vendenegativo.equals("N") && ConexOk == true) {

                atualizaEstoqueItem(CodigoItem);
            }

            Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodigoItem, null);
            CursItens.moveToFirst();
            qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
            CursItens.close();

            if (qtdestoque <= 0) {
                Util.msg_toast_personal(getBaseContext(), "Produto sem quantidade disponível.", Util.ALERTA);
                return;
            }


            final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
            final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
            final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
            final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
            final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);
        /*final Spinner*/
            spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);
            DB = new ConfigDB(this).getReadableDatabase();

            spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    SharedPreferences prefsHost = Lista_produtos.this.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                    sprecoprincipal = prefsHost.getInt("spreco", 0);
                    tabanterior = sprecoprincipal;
                    if (sprecoprincipal == 0) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }

                    }
                    if (sprecoprincipal == 1) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 2) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 3) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 4) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 5) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 6) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }
                    if (sprecoprincipal == 7) {
                        int novosprecoprincipal = spntabpreco.getSelectedItemPosition();
                        if (novosprecoprincipal == 0) {
                            //sprecoprincipal = novosprecoprincipal;
                            //GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        } else {
                            sprecoprincipal = novosprecoprincipal;
                            GravaPreferencias(sprecoprincipal);
                            spntabpreco.setSelection(sprecoprincipal);
                            spreco = spntabpreco.getSelectedItem().toString();
                            if (tab1 != null) {
                                spreco = spreco.replace(tab1, "");
                            }
                            if (tab2 != null) {
                                spreco = spreco.replace(tab2, "");
                            }
                            if (tab3 != null) {
                                spreco = spreco.replace(tab3, "");
                            }
                            if (tab4 != null) {
                                spreco = spreco.replace(tab4, "");
                            }
                            if (tab5 != null) {
                                spreco = spreco.replace(tab5, "");
                            }
                            if (tab6 != null) {
                                spreco = spreco.replace(tab6, "");
                            }
                            if (tab7 != null) {
                                spreco = spreco.replace(tab7, "");
                            }
                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                            info_txv_precoproduto.setText(spreco);
                        }
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            try {
                List<String> DadosListTabPreco = new ArrayList<String>();

                Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                CursorParametro.moveToFirst();
                tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));

                CursorParametro.close();

                String vlvendapadrao = produto_cursor.getString(produto_cursor.getColumnIndex("VENDAPADRAO"));
                vlvendapadrao = vlvendapadrao.trim();
                if (!vlvendapadrao.equals("0,0000")) {
                    BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                    String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precopadrao = Precopadrao.replace('.', ',');
                    DadosListTabPreco.add("Preço base R$: " + Precopadrao);
                }

                String vlvenda1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA1"));
                vlvenda1 = vlvenda1.trim();
                if (!vlvenda1.equals("0,0000")) {
                    BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                    String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco1 = Preco1.replace('.', ',');
                    DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                }

                String vlvenda2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA2"));
                vlvenda2 = vlvenda2.trim();
                if (!vlvenda2.equals("0,0000")) {
                    BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                    String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco2 = Preco2.replace('.', ',');
                    DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                }

                String vlvenda3 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA3"));
                vlvenda3 = vlvenda3.trim();
                if (!vlvenda3.equals("0,0000")) {
                    BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                    String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco3 = Preco3.replace('.', ',');
                    DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                }

                String vlvenda4 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA4"));
                vlvenda4 = vlvenda4.trim();
                if (!vlvenda4.equals("0,0000")) {
                    BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                    String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco4 = Preco4.replace('.', ',');
                    DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                }

                String vlvenda5 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA5"));
                vlvenda5 = vlvenda5.trim();
                if (!vlvenda5.equals("0,0000")) {
                    BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                    String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco5 = Preco5.replace('.', ',');
                    DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                }

                String vlvendap1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP1"));
                vlvendap1 = vlvendap1.trim();
                if (!vlvendap1.equals("0,0000")) {
                    BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                    String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precop1 = Precop1.replace('.', ',');
                    DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                }

                String vlvendap2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP2"));
                vlvendap2 = vlvendap2.trim();
                if (!vlvendap2.equals("0,0000")) {
                    BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                    String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Precop2 = Precop2.replace('.', ',');
                    DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                }

                ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(Lista_produtos.this, android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spntabpreco.setAdapter(arrayAdapterTabPreco);

            } catch (Exception E) {
                E.toString();

            }

            SqliteProdutoBean prdBean = new SqliteProdutoBean();
            info_txv_codproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRODUTO_SIMPLECURSOR)));
            info_txv_descricaoproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_DESCRICAO_PRODUTO)));
            info_txv_unmedida.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_UNIDADE_MEDIDA)));


            String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PROD_PADRAO));
            BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
            String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
            Preco = Preco.replace('.', ',');

            info_txv_precoproduto.setText(Preco);
            info_txt_quantidadecomprada.setText("");
            //info_txt_quantidadecomprada.selectAll();

            alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                    if (TAMANHO_TEXTO > 0) {
                        SqliteProdutoBean prdBean = new SqliteProdutoBean();
                        Double QUANTIDADE_DIGITADA = Double.parseDouble(info_txt_quantidadecomprada.getText().toString());
                        String COD_PRODUTO = info_txv_codproduto.getText().toString();
                        String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                        String UNIDADE = info_txv_unmedida.getText().toString();

                        if (QUANTIDADE_DIGITADA > 0) {
                            if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > qtdestoque) {
                                Util.msg_toast_personal(getBaseContext(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                            }

                            SqliteVendaDBean itemBean1 = new SqliteVendaDBean();
                            SqliteVendaDBean itemBean2 = new SqliteVendaDBean();
                            SqliteVendaDBean itemBean3 = new SqliteVendaDBean();
                            Sqlite_VENDADAO itemDao = new Sqlite_VENDADAO(getApplicationContext(), sCodvend, true);

                            itemBean2.setVendad_prd_codigo(COD_PRODUTO);
                            //itemBean3 = itemDao.altera_item_na_venda(itemBean2);

                            //if (itemBean3 != null) {
                            itemBean1.setVendad_prd_codigo(COD_PRODUTO);
                            itemBean1.setVendad_prd_descricao(DESCRICAO);
                            itemBean1.setVendad_prd_unidade(UNIDADE);
                            itemBean1.setVendad_quantidade(new BigDecimal(QUANTIDADE_DIGITADA));
                            itemBean1.setVendac_chave(chavepedido);

                            //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                            String ValorItem = info_txv_precoproduto.getText().toString();
                            ValorItem = ValorItem.trim();
                            if (!ValorItem.equals("0,0000")) {
                                BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                itemBean1.setVendad_preco_venda(venda);

                                //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                itemBean1.setVendad_total(itemBean1.getSubTotal());
                                itemDao.insere_item_na_venda(itemBean1);
                                //atualiza_listview_com_os_itens_pedido();
                                finish();
                            } else {
                                Util.msg_toast_personal(getBaseContext(), "produto com preço de venda zerado", Util.ALERTA);
                            }
                                                /*} else {
                                                    Util.msg_toast_personal(getBaseContext(), "Este produto já foi adicionado", Util.ALERTA);
                                                }*/

                            //}
                        } else {
                            Util.msg_toast_personal(getApplicationContext(), "A quantidade não foi informada", Util.ALERTA);
                        }

                    } else {
                        Util.msg_toast_personal(getApplicationContext(), "A quantidade não foi informada", Util.ALERTA);
                    }
                }
            });
            alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alerta.show();

        }
    }

    public void GravaPreferencias(int preco) {

        prefs = getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsPrivateEditor = prefs.edit();
        prefsPrivateEditor.putInt("spreco", preco);
        prefsPrivateEditor.commit();

    }

    // utilizado na inclusao de um novo pedido
    public void atualiza_listview_com_os_itens_da_venda() {

        prod_listview_itenstemp = (ListView) findViewById(R.id.prod_listview_produtotemp);
        List<SqliteVendaD_TempBean> itens_da_venda_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
        prod_listview_itenstemp.setAdapter(new ListaItensTemporariosAdapter(getApplicationContext(), itens_da_venda_temp));


        prod_listview_itenstemp.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                confirmar_exclusao_do_produto(listview, posicao);
                return false;
            }
        });
    }

    // utilizado na alteração de um pedido
    public void atualiza_listview_com_os_itens_pedido() {

        ListView_ItensVendidos = (ListView) findViewById(R.id.ListView_ItensVendidos);
        //prod_listview_itenstemp = (ListView) findViewById(R.id.prod_listview_produtotemp);
        List<SqliteVendaDBean> itens_venda = new Sqlite_VENDADAO(getApplicationContext(), sCodvend, true).buscar_itens_vendas_por_numeropedido(chavepedido);
        ListView_ItensVendidos.setAdapter(new ListaItensVendaAdapter(getApplicationContext(), itens_venda));


        ListView_ItensVendidos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                confirmar_exclusao_do_produto(listview, posicao);
                return false;
            }
        });
    }

    private void confirmar_exclusao_do_produto(final AdapterView listview, final int posicao) {
        Builder builder = new Builder(this);
        builder.setTitle("Atençao");
        builder.setMessage("Deseja excluir este produto ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                SqliteVendaD_TempBean item = (SqliteVendaD_TempBean) listview.getItemAtPosition(posicao);
                new SqliteVendaD_TempDao(getApplicationContext()).excluir_um_item_da_venda(item);
                atualiza_listview_com_os_itens_da_venda();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                dlg.dismiss();
            }
        });
        dlg = builder.create();
        dlg.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Lista_produtos Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void run() {
        try {
            actSincronismo.run(Lista_produtos.this);
            sincprod = actSincronismo.SincronizarProdutosStatic(dtUltAtu, Lista_produtos.this, true, usuario, senha);
            if (sincprod == false) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Não foi possivel sincronizar os produtos. Tente novamente.", Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Produtos sincronizados com sucesso!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            Intent intent = (Lista_produtos.this).getIntent();
            (Lista_produtos.this).finish();
            startActivity(intent);
        } finally {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    public void atualizaEstoqueItem(int item) {

        ProgressDialog Dialog = null;

        Dialog = new ProgressDialog(this);
        Dialog.setTitle("Aguarde...");
        Dialog.setMessage("Verificando estoque do produtos");
        Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        Dialog.setProgress(0);
        Dialog.setIcon(R.drawable.icon_sync);
        Dialog.setMax(0);
        Dialog.show();

        String METHOD_NAME = "RetornaQtdItem";
        //String TAG_PRODUTOSINFO = "produtos";
        String TAG_QTDESTOQUE = "qtd_disponivel";

        SharedPreferences prefsHost = this.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        String URLPrincipal = prefsHost.getString("host", null);

        SharedPreferences prefs = this.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
        String usuario = prefs.getString("usuario", null);
        String senha = prefs.getString("senha", null);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aCodigoItem", item);
        soap.addProperty("aUsuario", usuario);
        soap.addProperty("aSenha", senha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS, 60000);
        String RetQTDProdutos = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetQTDProdutos = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            } else {
                Util.msg_toast_personal(getApplicationContext(), "Sem conexão com a internet.Verifique!", Util.ALERTA);
                return;
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
        try {
            Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + item, null);
            try {
                if (CursItens.getCount() > 0) {
                    CursItens.moveToFirst();
                    DB.execSQL(" UPDATE ITENS SET QTDESTPROD = '" + RetQTDProdutos.trim() +
                            "' WHERE CODIGOITEM = " + item);
                } else {
                    Toast.makeText(this, "Produto não encontrado. Verifique!", Toast.LENGTH_SHORT).show();
                }
                CursItens.close();
            } catch (Exception E) {
                System.out.println("Error" + E);
            }


        } catch (Exception E) {
            E.toString();
        }
        if (Dialog.isShowing()) {
            Dialog.dismiss();
        }
        carrega_produto_para_venda();
    }
}