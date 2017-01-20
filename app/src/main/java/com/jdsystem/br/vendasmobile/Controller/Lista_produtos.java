package com.jdsystem.br.vendasmobile.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoDao;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.actSincronismo;
import com.jdsystem.br.vendasmobile.act_ListProdutos;
import com.jdsystem.br.vendasmobile.adapter.ListaItensTemporariosAdapter;

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
    private Intent Codigo;
    private String NumPedido;

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

        atualiza_listview_com_os_itens_da_venda();

        array_spinner.add(DESCRICAO_PRODUTO);
        array_spinner.add(CODIGO_PRODUTO);
        array_spinner.add(CATEGORIA_PRODUTO);
        Codigo = getIntent();

        NumPedido = Codigo.getStringExtra("numpedido");

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
                //prdBean.P_CODIGO_PRODUTO,
                //prdBean.P_CODIGO_BARRAS,
                prdBean.P_DESCRICAO_PRODUTO,
                //prdBean.P_QUANTIDADE_PRODUTO,
                prdBean.P_CATEGORIA_PRODUTO,
                prdBean.P_PRECO_PROD_PADRAO};

        int[] to = new int[]{
                R.id.prod_txv_prd_codigo,
                //R.id.prod_txv_prd_codigobarras,
                R.id.prod_txv_prd_descricaoproduto,
                //R.id.prod_txv_prd_quantidade,
                R.id.prod_txv_prd_categoria,
                R.id.prod_txv_prd_preco};

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

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.info_produto_venda, null);
        alerta = new Builder(this);
        alerta.setCancelable(false);
        alerta.setView(view);

        final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
        final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
        final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
        final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
        final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);

        SqliteProdutoBean prdBean = new SqliteProdutoBean();
        info_txv_codproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRODUTO_SIMPLECURSOR)));
        info_txv_descricaoproduto.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_DESCRICAO_PRODUTO)));
        info_txv_unmedida.setText(produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_UNIDADE_MEDIDA)));

        String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PROD_PADRAO));
        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
        String Preco = venda.setScale(4, BigDecimal.ROUND_FLOOR).toString();
        Preco = Preco.replace('.', ',');

        info_txv_precoproduto.setText(Preco);
        info_txt_quantidadecomprada.selectAll();

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
                            BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                            venda.toString().replace('.', ',');
                            itemBean1.setVendad_preco_vendaTEMP(venda);

                            //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                            itemBean1.setVendad_totalTEMP(itemBean1.getSubTotal());
                            itemDao.insere_item(itemBean1);
                            atualiza_listview_com_os_itens_da_venda();
                            finish();
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
        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alerta.show();
    }

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


    public void FinalizaPedidoProduto(View v) {

        finish();
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
            actSincronismo.SincronizarProdutosStatic(dtUltAtu, Lista_produtos.this, true);

            Intent intent = (Lista_produtos.this).getIntent();
            (Lista_produtos.this).finish();
            startActivity(intent);
        } finally {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}










