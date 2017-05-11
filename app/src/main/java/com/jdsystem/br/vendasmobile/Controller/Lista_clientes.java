package com.jdsystem.br.vendasmobile.Controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.CadastroClientes;
import com.jdsystem.br.vendasmobile.CadastroPedidos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class Lista_clientes extends ActionBarActivity implements Runnable {


    public static final String TELA_DE_VENDAS = "VENDER_PRODUTOS";
    public static String PESQUISAR_CLIENTE_NOME = "Razão Social";
    public static String PESQUISAR_CLIENTE_FANTASIA = "Nome Fantasia";
    public static String PESQUISAR_CLIENTE_CIDADE = "Cidade";
    public static String PESQUISAR_CLIENTE_BAIRRO = "Bairro";
    private Spinner adm_sp_filtrarcliente;
    private Handler handler = new Handler();
    private SimpleCursorAdapter AdapterClientes;
    private List<String> array_spinner = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String selecao_spinner;
    private Cursor cursor;
    private EditText adm_txt_pesquisacliente;
    private ListView adm_listview_cliente;
    private Intent TELA_QUE_CHAMOU_INTENT;
    private String TELA_QUE_CHAMOU;
    private String CodVendedor;
    private String usuario, senha, URLPrincipal, NumPedido, DATAENTREGA, sincclieenvio, sincclie;
    private String CodEmpresa;
    public ProgressDialog dialog;
    public SQLiteDatabase DB;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_clientes);

        TELA_QUE_CHAMOU_INTENT = getIntent();
        TELA_QUE_CHAMOU = TELA_QUE_CHAMOU_INTENT.getStringExtra("TELA_QUE_CHAMOU");
        CodVendedor = TELA_QUE_CHAMOU_INTENT.getStringExtra(getString(R.string.intent_codvendedor));
        CodEmpresa = TELA_QUE_CHAMOU_INTENT.getStringExtra("codempresa");
        usuario = TELA_QUE_CHAMOU_INTENT.getStringExtra(getString(R.string.intent_usuario));
        senha = TELA_QUE_CHAMOU_INTENT.getStringExtra((getString(R.string.intent_senha)));
        NumPedido = TELA_QUE_CHAMOU_INTENT.getStringExtra("numpedido");
        DATAENTREGA = TELA_QUE_CHAMOU_INTENT.getStringExtra("dataentrega");

        array_spinner.add(PESQUISAR_CLIENTE_NOME);
        array_spinner.add(PESQUISAR_CLIENTE_FANTASIA);
        array_spinner.add(PESQUISAR_CLIENTE_CIDADE);
        array_spinner.add(PESQUISAR_CLIENTE_BAIRRO);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_spinner);
        adm_sp_filtrarcliente = (Spinner) findViewById(R.id.adm_sp_filtrarcliente);
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

        FloatingActionButton cadastraCliente = (FloatingActionButton) findViewById(R.id.fab_inclui_cliente);
        cadastraCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Lista_clientes.this, CadastroClientes.class);
                Bundle params = new Bundle();
                params.putString("TELA_QUE_CHAMOU", TELA_QUE_CHAMOU);
                params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                params.putString((getString(R.string.intent_usuario)), usuario);
                params.putString((getString(R.string.intent_senha)), senha);
                params.putInt("listaclie", 0);
                intent.putExtras(params);
                startActivity(intent);
                finish();
            }
        });
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
            SQLiteDatabase DB = new ConfigDB(this).getReadableDatabase();
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (ConexOk == true) {
                Cursor cursorVerificaClie = DB.rawQuery("SELECT * FROM CLIENTES", null);
                if (cursorVerificaClie.getCount() == 0) {
                    dialog = new ProgressDialog(Lista_clientes.this);
                    dialog.setTitle(R.string.wait);
                    dialog.setMessage(getString(R.string.primeira_sync_clientes));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setIcon(R.drawable.icon_sync);
                    dialog.setCancelable(false);
                    dialog.show();

                    Thread thread = new Thread(this);
                    thread.start();
                } else {
                    dialog = new ProgressDialog(Lista_clientes.this);
                    dialog.setCancelable(false);
                    dialog.setMessage((getString(R.string.sync_clients)));
                    dialog.setTitle((getString(R.string.wait)));
                    dialog.show();

                    Thread thread = new Thread(this);
                    thread.start();
                }
            } else {
                Toast.makeText(this, (getString(R.string.no_connection)), Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void mostrar_clientes_listview() {

        DB = new ConfigDB(this).getReadableDatabase();

        SqliteClienteBean cliBean = new SqliteClienteBean();
        SqliteClienteDao cliDao = new SqliteClienteDao(this);

        final Cursor cursor = cliDao.buscar_todos_cliente(CodVendedor);

        String[] colunas = new String[]{cliBean.C_CODIGO_CLIENTE_CURSOR, cliBean.C_NOME_DO_CLIENTE, cliBean.C_NOME_FANTASIA, cliBean.C_CIDADE_CLIENTE, cliBean.C_BAIRRO_CLIENTE};
        int[] para = new int[]{R.id.adm_txv_clicodigo, R.id.adm_txv_cli_nome, R.id.adm_txv_cli_fantasia, R.id.adm_txv_cli_cidade, R.id.adm_txv_cli_bairro};

        AdapterClientes = new SimpleCursorAdapter(this, R.layout.lista_cliente_item, cursor, colunas, para, 0);
        adm_listview_cliente = (ListView) findViewById(R.id.adm_listview_cliente);
        adm_listview_cliente.setAdapter(AdapterClientes);

        if (cursor.getCount() <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Lista_clientes.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage(getString(R.string.alertsyncclients))
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

                Cursor cliente_cursor = (Cursor) listview.getItemAtPosition(posicao);

                switch (TELA_QUE_CHAMOU) {
                    case TELA_DE_VENDAS:
                        String BloqClie = null;
                        String bloqueio = null;
                        String FlagIntegrado = null;
                        try {
                            Cursor cursorbloqclie = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP", null);
                            cursorbloqclie.moveToFirst();
                            BloqClie = cursorbloqclie.getString(cursorbloqclie.getColumnIndex("HABCRITSITCLIE"));
                            cursorbloqclie.close();
                            bloqueio = cliente_cursor.getString(cursor.getColumnIndex("BLOQUEIO"));
                            FlagIntegrado = cliente_cursor.getString(cursor.getColumnIndex("FLAGINTEGRADO"));
                        } catch (Exception e) {
                            e.toString();
                        }

                        if (FlagIntegrado.equals("1")) {
                            if (NumPedido == null) {
                                Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                Bundle params = new Bundle();
                                params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                params.putString("numpedido", "0");
                                params.putString("codempresa", CodEmpresa);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                Bundle params = new Bundle();
                                params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                params.putString("numpedido", NumPedido);
                                params.putString("codempresa", CodEmpresa);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();
                            }
                        } else if (BloqClie.equals("S")) {
                            Boolean ConexOk = Util.checarConexaoCelular(Lista_clientes.this);
                            if (ConexOk == true) {
                                Sincronismo.SincronizarClientesStatic(CodVendedor, Lista_clientes.this, usuario, senha, cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_EXT")),null,null,null);
                                Cursor cursorclie = DB.rawQuery("SELECT BLOQUEIO, CODCLIE_INT FROM CLIENTES WHERE CODCLIE_INT = " + cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")) + "", null);
                                cursorclie.moveToFirst();
                                bloqueio = cursorclie.getString(cursorclie.getColumnIndex("BLOQUEIO"));
                            }
                            if (bloqueio.equals("01") || bloqueio.equals("1")) {
                                if (NumPedido == null) {
                                    Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                    params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                    params.putString("numpedido", "0");
                                    params.putString("codempresa", CodEmpresa);
                                    params.putString("dataentrega", DATAENTREGA);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                    params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                    params.putString("numpedido", NumPedido);
                                    params.putString("codempresa", CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Util.msg_toast_personal(getBaseContext(), getString(R.string.customer_without_purchase_permission), Util.ALERTA);
                                return;
                            }
                        }
                        if (BloqClie.equals("N")) {
                            if (NumPedido == null) {
                                Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                Bundle params = new Bundle();
                                params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                params.putString("numpedido", "0");
                                params.putString("codempresa", CodEmpresa);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(getBaseContext(), CadastroPedidos.class);
                                Bundle params = new Bundle();
                                params.putInt("CLI_CODIGO", cliente_cursor.getInt(cursor.getColumnIndex("CODCLIE_INT")));
                                params.putString((getString(R.string.intent_codvendedor)), CodVendedor);
                                params.putString("numpedido", NumPedido);
                                params.putString("codempresa", CodEmpresa);
                                intent.putExtras(params);
                                startActivity(intent);
                                finish();
                            }
                        }


                        break;
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
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_DO_CLIENTE, CodVendedor);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_FANTASIA) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_FANTASIA, CodVendedor);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_CIDADE) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_CIDADE, CodVendedor);
                }

                if (selecao_spinner == PESQUISAR_CLIENTE_BAIRRO) {
                    this.cursor = cli.buscar_cliente_na_pesquisa_edittext(valor.toString(), cli.NOME_BAIRRO, CodVendedor);
                }

                if (cursor.getCount() <= 0) {
                    Toast.makeText(Lista_clientes.this, "Nenhum cliente encontrado!", Toast.LENGTH_SHORT).show();
                }

                return cursor;
            }
        });
    }

    @Override
    public void run() {
        try {
            //Sincronismo.run(this);
            sincclieenvio = Sincronismo.SincronizarClientesEnvioStatic("0", this, usuario, senha,null,null,null);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), sincclieenvio, Toast.LENGTH_SHORT).show();
                }
            });
            sincclie = Sincronismo.SincronizarClientesStatic(CodVendedor, this, usuario, senha, 0,null,null,null);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), sincclie, Toast.LENGTH_SHORT).show();
                }
            });
            if (dialog.isShowing())
                dialog.dismiss();
            Intent intent = (Lista_clientes.this).getIntent();
            (Lista_clientes.this).finish();
            startActivity(intent);
        } catch (Exception e) {
            e.toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
