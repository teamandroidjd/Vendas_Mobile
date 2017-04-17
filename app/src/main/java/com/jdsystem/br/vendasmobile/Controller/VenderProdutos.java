package com.jdsystem.br.vendasmobile.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaClientes;
import com.jdsystem.br.vendasmobile.ConsultaProdutos;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoDao;
import com.jdsystem.br.vendasmobile.Model.SqliteParametroDao;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.Model.Sqlite_VENDADAO;
import com.jdsystem.br.vendasmobile.Pagamento.Avista;
import com.jdsystem.br.vendasmobile.Pagamento.Mensal;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Gps;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.ConsultaPedidos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterItensTemporarios;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterItensVenda;
import com.jdsystem.br.vendasmobile.interfaces.iPagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class VenderProdutos extends Activity implements View.OnKeyListener, View.OnFocusChangeListener, Runnable {

    private BigDecimal TOTAL_DA_VENDA;
    private Integer CLI_CODIGO, CLI_CODIGO_ANT, CLI_CODIGO_EXT;
    private String CodEmpresa, vendenegativo, numpedido, nomeclievenda, tab1, tab2, tab3, tab4, tab5, tab6, tab7, CodVendedor;
    private String ObsPedido, NumPedido, CodClie_Int, DATA_DE_ENTREGA;
    private String PREFS_PRIVATE = "PREFS_PRIVATE";
    private int sprecoprincipal, tabanterior;
    private ListView ListView_ItensVendidos, prod_listview_produtotemp;
    private List<SqliteVendaD_TempBean> itens_temp = new ArrayList<>();
    //private List<SqliteVendaDBean> itens_ped = new ArrayList<>();
    private List<SqliteVendaDBean> itens_venda = new ArrayList<>();
    private EditText venda_txt_desconto;
    private Double DESCONTO_PADRAO_VENDEDOR, qtdestoque;
    private SimpleDateFormat dateFormatterBR, dateFormatterUSA;
    private DatePickerDialog datePicker;
    private TextView venda_txv_datavenda, venda_txv_codigo_cliente, venda_txv_empresa, venda_txv_dataentrega;
    private TextView venda_txv_desconto, venda_txv_total_da_Venda;
    private Toolbar toolbar;
    private SqliteClienteBean cliBean;
    public String Chave_Venda, dataent, sCodVend, URLPrincipal, spreco, COD_PRODUTO, usuario, senha, DataHoraVenda, nomeabrevemp;
    public ProgressDialog dialog;
    public Long venda_ok;
    public AlertDialog alerta, dlg;
    private Builder alerta1;
    private Spinner spntabpreco;
    Handler handler = new Handler();
    public static final String DATA_ENT = "DATA DE ENTREGA";
    public SharedPreferences prefs;
    private SimpleCursorAdapter adapter;
    private SqliteVendaCBean vendaCBean;
    private SqliteVendaDBean vendaDBean;
    private SqliteConfPagamentoDao confDao;
    private SqliteConfPagamentoBean confBean;
    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vender_produtos);

        declaraObjetos();

        ObsPedido = "";
        setDateTimeField();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                CodEmpresa = params.getString("codempresa");
                senha = params.getString(getString(R.string.intent_senha));
                DATA_DE_ENTREGA = params.getString("dataentrega");
            }
        }

        Intent CLI_CODIGO_INTENT = getIntent();
        CLI_CODIGO = CLI_CODIGO_INTENT.getIntExtra("CLI_CODIGO", 0);
        CodVendedor = CLI_CODIGO_INTENT.getStringExtra(getString(R.string.intent_codvendedor));
        NumPedido = CLI_CODIGO_INTENT.getStringExtra(getString(R.string.intent_numpedido));
        CodEmpresa = CLI_CODIGO_INTENT.getStringExtra(getString(R.string.intent_codigoempresa));
        CLI_CODIGO_ANT = CLI_CODIGO;

        prefs = getSharedPreferences(DATA_ENT, MODE_PRIVATE);
        dataent = prefs.getString("dataentrega", null);
        if (dataent != null && dataent != "") {
            venda_txv_dataentrega.setText(dataent);
        }

        venda_txt_desconto.setOnKeyListener(this);
        venda_txt_desconto.setOnFocusChangeListener(this);
        venda_txt_desconto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                venda_txt_desconto.setSelectAllOnFocus(true);
                view.clearFocus();
                view.requestFocus();
            }

        });

        carregadadosAlterarpedido();

        venda_txv_codigo_cliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                alterarcliente();
            }
        });
        venda_txv_empresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                alterarempresa();
            }
        });

        carregarempresa();

        DESCONTO_PADRAO_VENDEDOR = new SqliteParametroDao(this).busca_parametros().getP_desconto_do_vendedor();

        if (NumPedido.equals("0")) {
            CLI_CODIGO_EXT = Integer.valueOf(cliBean.getCli_codigo_ext().toString());
            //CLI_CODIGO_EXT = vendaCBean.getVendac_cli_codigo_ext();
        }

        FloatingActionButton incluirProduto = (FloatingActionButton) findViewById(R.id.fab_inclui_produto);
        incluirProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                Intent Lista_produtos = new Intent(getBaseContext(), ConsultaProdutos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_numpedido), NumPedido);
                params.putString(getString(R.string.intent_chavepedido), Chave_Venda);
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal),URLPrincipal);
                params.putInt(getString(R.string.intent_flag), 2);
                Lista_produtos.putExtras(params);
                startActivity(Lista_produtos);
            }
        });

        // atualiza_listview_e_calcula_total();

        toolbar = (Toolbar) findViewById(R.id.inc_toolbar);
        toolbar.inflateMenu(R.menu.menu_vender_produtos);
        toolbar.findViewById(R.id.finalizar_venda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                finalzarvenda(true);
            }
        });

        toolbar.findViewById(R.id.cancel_venda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarvenda();
            }
        });

        toolbar.findViewById(R.id.item_formapgto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                incluirformadepagamento();
            }
        });

        toolbar.findViewById(R.id.item_dtentrega).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                datePicker.setTitle("Entrega Prevista");
                datePicker.show();
            }
        });

        toolbar.findViewById(R.id.item_obspedido).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                incluirobs();
            }
        });

        //obterConfiguracoesPagamento();
    }

    private void incluirobs() {
        View view = (LayoutInflater.from(VenderProdutos.this)).inflate(R.layout.input_obs_pedido, null);
        final EditText userInput = (EditText) view.findViewById(R.id.inputobspedido);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VenderProdutos.this);
        alertBuilder.setView(view);

        if (!ObsPedido.equals("")) {
            userInput.setText(ObsPedido);
        }
        alertBuilder.setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ObsPedido = String.valueOf(userInput.getText());
                    }
                });
        Dialog dialog = alertBuilder.create();
        dialog.show();
    }

    private void incluirformadepagamento() {
        if (venda_txt_desconto.getText().toString().isEmpty()) {
            venda_txt_desconto.setText("0");
        }
        // if (verifica_limite_desconto()) {
        Boolean AtuPed = true;
        if (NumPedido.equals("0")) {
            AtuPed = false;
        }
        if (!NumPedido.equals("0")) {
            itens_venda = new Sqlite_VENDADAO(getApplicationContext(), sCodVend, true).buscar_itens_vendas_por_numeropedido(Chave_Venda);
            if (!itens_venda.isEmpty()) {
                AtuPed = true;
                Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                it.putExtra("CLI_CODIGO", CLI_CODIGO);
                it.putExtra("AtuPedido", AtuPed);
                it.putExtra("ChavePedido", Chave_Venda);
                startActivity(it);
            }
        } else {
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
            if (!itens_temp.isEmpty()) {
                Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                it.putExtra("CLI_CODIGO", CLI_CODIGO);
                it.putExtra("AtuPedido", AtuPed);
                it.putExtra("ChavePedido", Chave_Venda);
                startActivity(it);
            } else {
                Util.msg_toast_personal(getBaseContext(), "Adicione itens na venda", Util.PADRAO);
            }
        }
        // }
    }

    private void carregadadosAlterarpedido() {
        if (!NumPedido.equals("0") && (CLI_CODIGO == 0)) {
            vendaCBean = new SqliteVendaCBean();
            vendaCBean = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).buscar_vendas_por_numeropedido(NumPedido.toString());
            CLI_CODIGO_EXT = vendaCBean.getVendac_cli_codigo_ext();
            CLI_CODIGO = vendaCBean.getVendac_cli_codigo();
            nomeclievenda = vendaCBean.getVendac_cli_nome().toString();
            Chave_Venda = vendaCBean.getVendac_chave();
            CodEmpresa = vendaCBean.getCodEmpresa();
            CodVendedor = vendaCBean.getCodVendedor();
            DATA_DE_ENTREGA = vendaCBean.getVendac_previsaoentrega();
            ObsPedido = vendaCBean.getObservacao();
            if (CLI_CODIGO_EXT != 0) {
                venda_txv_codigo_cliente.setText("Cliente: " + CLI_CODIGO_EXT.toString() + " - " + vendaCBean.getVendac_cli_nome().toString());
            } else {
                venda_txv_codigo_cliente.setText("Cliente: " + CLI_CODIGO.toString() + " - " + vendaCBean.getVendac_cli_nome().toString());
            }
            venda_txt_desconto.setText(vendaCBean.getVendac_percdesconto().toString());
            DataHoraVenda = vendaCBean.getVendac_datahoravenda();
            venda_txv_datavenda.setText("Data/Hora Venda : " + Util.FormataDataDDMMAAAA_ComHoras(vendaCBean.getVendac_datahoravenda()));
            Alterar_Pedido_listview_e_calcula_total();

        } else if (!NumPedido.equals("0") && CLI_CODIGO != 0) {
            cliBean = new SqliteClienteBean();
            cliBean = new SqliteClienteDao(getApplicationContext()).buscar_cliente_pelo_codigo(CLI_CODIGO.toString());
            CLI_CODIGO_EXT = cliBean.getCli_codigo_ext();
            CLI_CODIGO = cliBean.getCli_codigo();
            nomeclievenda = cliBean.getCli_nome().toString();
            venda_txv_codigo_cliente.setText("Cliente: " + CLI_CODIGO_EXT.toString() + " - " + cliBean.getCli_nome().toString());
            venda_txv_codigo_cliente.requestFocus();
            vendaCBean = new SqliteVendaCBean();
            vendaCBean = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).buscar_vendas_por_numeropedido(NumPedido.toString());
            Chave_Venda = vendaCBean.getVendac_chave();
            //CodEmpresa = vendaCBean.getCodEmpresa();
            CodVendedor = vendaCBean.getCodVendedor();
            DATA_DE_ENTREGA = vendaCBean.getVendac_previsaoentrega();
            ObsPedido = vendaCBean.getObservacao();
            DataHoraVenda = vendaCBean.getVendac_datahoravenda();
            venda_txt_desconto.setText(vendaCBean.getVendac_percdesconto().toString());
            venda_txv_datavenda.setText("Data/Hora Venda : " + Util.FormataDataDDMMAAAA_ComHoras(vendaCBean.getVendac_datahoravenda()));
            Alterar_Pedido_listview_e_calcula_total();
        } else {
            if (CLI_CODIGO.equals(0)) {
                CLI_CODIGO = vendaCBean.getVendac_cli_codigo();
            }

            cliBean = new SqliteClienteBean();
            cliBean = new SqliteClienteDao(getApplicationContext()).buscar_cliente_pelo_codigo(CLI_CODIGO.toString());
            int codclie_ext = cliBean.getCli_codigo_ext();
            if (codclie_ext != 0) {
                venda_txv_codigo_cliente.setText("Cliente: " + codclie_ext + " - " + cliBean.getCli_nome().toString());
                venda_txv_codigo_cliente.requestFocus();
            } else {
                venda_txv_codigo_cliente.setText("Cliente: " + CLI_CODIGO.toString() + " - " + cliBean.getCli_nome().toString());
                venda_txv_codigo_cliente.requestFocus();
            }
        }
        if (!NumPedido.equals("0") && (dataent == null || dataent == "")) {
            dataent = "Data da entrega: " + Util.FormataDataDDMMAAAA(DATA_DE_ENTREGA);
            venda_txv_dataentrega.setText(dataent);
        }
    }

    private void cancelarvenda() {
        if (NumPedido.equals("0")) {
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();

            if (!itens_temp.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Deseja realmente cancelar a venda?")
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent it = new Intent();
                                it.putExtra("atualizalista", true); //true: Atualiza a Tela anterior
                                setResult(1, it);

                                new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

                                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                                editor.putString("dataentrega", "");
                                editor.commit();

                                finish();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Deseja realmente cancelar a venda?")
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent it = new Intent();
                                it.putExtra("atualizalista", true); //true: Atualiza a Tela anterior
                                setResult(1, it);
                                //new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

                                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                                editor.putString("dataentrega", "");
                                editor.commit();

                                finish();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }

        } else if (!NumPedido.equals("0")) {
            itens_venda = new Sqlite_VENDADAO(getApplicationContext(), sCodVend, true).buscar_itens_vendas_por_numeropedido(Chave_Venda);
            if (!itens_venda.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Deseja realmente cancelar a alteração do pedido?")
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //finalzarvenda(false);

                                new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

                                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                                editor.putString("dataentrega", "");
                                editor.commit();

                                finish();
                                Toast.makeText(VenderProdutos.this, "Pedido não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                                Bundle params = new Bundle();
                                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                params.putString(getString(R.string.intent_usuario), usuario);
                                params.putString(getString(R.string.intent_senha), senha);
                                i.putExtras(params);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                builder.setTitle(R.string.app_namesair);
                builder.setIcon(R.drawable.logo_ico);
                builder.setMessage("Não existe produto incluso neste pedido " + NumPedido + ". O mesmo será cancelado. Deseja continuar?")
                        .setCancelable(false)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                                editor.putString("dataentrega", "");
                                editor.commit();

                                finish();
                                Boolean pedcancelado = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).atualizar_pedido_para_cancelado(Chave_Venda);
                                if (pedcancelado == true) {
                                    Toast.makeText(VenderProdutos.this, "Pedido cancelado com sucesso!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    i.putExtras(params);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(VenderProdutos.this, " Houve um problema ao cancelar o pedido. Verifique!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    i.putExtras(params);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        } else {
            Intent it = new Intent();
            it.putExtra("atualizalista", true); //true: Atualiza a Tela anterior
            setResult(1, it);
            new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

            SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
            editor.putString("dataentrega", "");
            editor.commit();

            finish();
        }
    }

    private void carregarempresa() {
        DB = new ConfigDB(this).getReadableDatabase();
        //TextView venda_txv_empresa = (TextView) findViewById(R.id.venda_txv_empresa);
        try {
            Cursor CursorEmpresa = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
            if (CursorEmpresa.getCount() > 0) {
                CursorEmpresa.moveToFirst();
                do {
                    String Empresa = CursorEmpresa.getString(CursorEmpresa.getColumnIndex("NOMEABREV"));
                    venda_txv_empresa.setText("Empresa: " + Empresa);
                }
                while (CursorEmpresa.moveToNext());
                CursorEmpresa.close();
            }
        } catch (Exception E) {
            Toast.makeText(this, E.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarcliente() {
        if (!NumPedido.equals("0")) {
            Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putInt(getString(R.string.intent_codcliente), CLI_CODIGO);
            params.putInt(getString(R.string.intent_flag), 2);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_numpedido), NumPedido);
            params.putString(getString(R.string.intent_urlprincipal),URLPrincipal);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();

        } else {
            Intent intent = new Intent(getBaseContext(), ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString("TELA_QUE_CHAMOU", "VENDER_PRODUTOS");
            params.putString(getString(R.string.intent_codvendedor), sCodVend);
            params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
            params.putInt(getString(R.string.intent_codcliente), CLI_CODIGO);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_numpedido), NumPedido);
            params.putString(getString(R.string.intent_urlprincipal),URLPrincipal);
            params.putInt(getString(R.string.intent_flag), 2);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();
        }
    }

    private void alterarempresa() {
        if (!NumPedido.equals("0")) {
            nomeabrevemp = "0";
            try {
                Cursor CursEmpr = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE ATIVO = 'S' ", null);
                CursEmpr.moveToFirst();
                if (CursEmpr.getCount() > 1) {
                    List<String> DadosListEmpresa = new ArrayList<String>();
                    do {
                        DadosListEmpresa.add(CursEmpr.getString(CursEmpr.getColumnIndex("NOMEABREV")));
                    } while (CursEmpr.moveToNext());

                    View viewEmp = (LayoutInflater.from(VenderProdutos.this)).inflate(R.layout.input_empresa_corrente_pedido, null);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VenderProdutos.this);
                    alertBuilder.setView(viewEmp);
                    final Spinner spEmpresaInput = (Spinner) viewEmp.findViewById(R.id.spnEmpresa);

                    ArrayAdapter<String> arrayEmpresa = new ArrayAdapter<String>(VenderProdutos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa);
                    ArrayAdapter<String> spArrayEmpresa = arrayEmpresa;
                    spArrayEmpresa.setDropDownViewResource(android.R.layout.simple_list_item_1);
                    spEmpresaInput.setAdapter(spArrayEmpresa);

                    alertBuilder.setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String NomeEmpresa = spEmpresaInput.getSelectedItem().toString();
                                    try {
                                        Cursor CursEmpr2 = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE NOMEABREV = '" + NomeEmpresa + "'", null);
                                        CursEmpr2.moveToFirst();
                                        if (CursEmpr2.getCount() > 0) {
                                            CodEmpresa = CursEmpr2.getString(CursEmpr2.getColumnIndex("CODEMPRESA"));
                                            nomeabrevemp = CursEmpr2.getString(CursEmpr2.getColumnIndex("NOMEABREV"));
                                            venda_txv_empresa.setText("Empresa: " + nomeabrevemp);
                                        }
                                        CursEmpr2.close();

                                    } catch (Exception E) {
                                        System.out.println("Error" + E);
                                    }
                                }
                            });
                    Dialog dialog = alertBuilder.create();
                    dialog.show();

                }
            } catch (Exception E) {
                E.toString();
            }
        } else {
            nomeabrevemp = "0";
            try {
                Cursor CursEmpr = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE ATIVO = 'S' ", null);
                CursEmpr.moveToFirst();
                if (CursEmpr.getCount() > 1) {
                    List<String> DadosListEmpresa = new ArrayList<String>();
                    do {
                        DadosListEmpresa.add(CursEmpr.getString(CursEmpr.getColumnIndex("NOMEABREV")));
                    } while (CursEmpr.moveToNext());

                    View viewEmp = (LayoutInflater.from(VenderProdutos.this)).inflate(R.layout.input_empresa_corrente_pedido, null);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VenderProdutos.this);
                    alertBuilder.setView(viewEmp);
                    final Spinner spEmpresaInput = (Spinner) viewEmp.findViewById(R.id.spnEmpresa);

                    ArrayAdapter<String> arrayEmpresa = new ArrayAdapter<String>(VenderProdutos.this, android.R.layout.simple_spinner_dropdown_item, DadosListEmpresa);
                    ArrayAdapter<String> spArrayEmpresa = arrayEmpresa;
                    spArrayEmpresa.setDropDownViewResource(android.R.layout.simple_list_item_1);
                    spEmpresaInput.setAdapter(spArrayEmpresa);

                    alertBuilder.setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String NomeEmpresa = spEmpresaInput.getSelectedItem().toString();
                                    try {
                                        Cursor CursEmpr2 = DB.rawQuery(" SELECT CODEMPRESA, NOMEABREV  FROM EMPRESAS WHERE NOMEABREV = '" + NomeEmpresa + "'", null);
                                        CursEmpr2.moveToFirst();
                                        if (CursEmpr2.getCount() > 0) {
                                            CodEmpresa = CursEmpr2.getString(CursEmpr2.getColumnIndex("CODEMPRESA"));
                                            nomeabrevemp = CursEmpr2.getString(CursEmpr2.getColumnIndex("NOMEABREV"));
                                            venda_txv_empresa.setText("Empresa: " + nomeabrevemp);
                                        }
                                        CursEmpr2.close();

                                    } catch (Exception E) {
                                        System.out.println("Error" + E);
                                    }
                                }
                            });
                    Dialog dialog = alertBuilder.create();
                    dialog.show();

                }
            } catch (Exception E) {
                E.toString();
            }
        }
    }

    private void setDateTimeField() {
        dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormatterUSA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                venda_txv_dataentrega.setText("Data de Entrega: " + dateFormatterBR.format(newDate.getTime()));
                DATA_DE_ENTREGA = dateFormatterUSA.format(newDate.getTime());
                Util.log(DATA_DE_ENTREGA);

                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                editor.putString("dataentrega", venda_txv_dataentrega.getText().toString());
                editor.commit();
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarempresa();
        if (venda_txt_desconto.getText().toString().isEmpty()) {
            venda_txt_desconto.setText("0");
        }
        if (!NumPedido.equals("0")) {
            Alterar_Pedido_listview_e_calcula_total();
            obterConfiguracoesPagamento();
        } else {
            atualiza_listview_e_calcula_total();
            obterConfiguracoesPagamento();
        }
    }

    private void obterConfiguracoesPagamento() {
        confBean = new SqliteConfPagamentoBean();

        if (!NumPedido.equals("0")) {
            confBean = confDao.busca_CONFPAGAMENTO_Pedido(Chave_Venda);
          /* if (confBean != null) {
                if (confBean.isAvista()) {
                    venda_txv_desconto.setVisibility(View.VISIBLE);
                    venda_txt_desconto.setVisibility(View.VISIBLE);
                }
            }*/
        } else {
            confBean = confDao.busca_CONFPAGAMENTO_sem_chave();
            /*if (confBean == null) {
                 if (confBean.isAvista()) {
                    venda_txv_desconto.setVisibility(View.VISIBLE);
                    venda_txt_desconto.setVisibility(View.VISIBLE);
                }
            }*/
        }
    }

    public boolean verifica_limite_desconto() {

        if (Double.parseDouble(venda_txt_desconto.getText().toString()) > DESCONTO_PADRAO_VENDEDOR) {
            Util.msg_toast_personal(getBaseContext(), "Limite de desconto incompatível", Util.PADRAO);
            //venda_txt_desconto.setText("0");
            venda_txt_desconto.requestFocus();
            return false;
        }

        return true;
    }

    private void finalzarvenda(boolean sincpedido) {
        BigDecimal valor_recebido = null;
        BigDecimal total_venda = null;
        if (venda_txt_desconto.getText().toString().isEmpty()) {
            venda_txt_desconto.setText("0");
        }
        if (!NumPedido.equals("0") && itens_venda.isEmpty()) {
            Toast.makeText(this, "Nenhum produto foi selecionado", Toast.LENGTH_SHORT).show();
            Intent Lista_produtos = new Intent(getBaseContext(), ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_numpedido), NumPedido);
            Lista_produtos.putExtras(params);
            startActivity(Lista_produtos);
            return;
        } else if (NumPedido.equals("0") && itens_temp.isEmpty()) {
            Toast.makeText(this, "Nenhum produto foi selecionado", Toast.LENGTH_SHORT).show();
            Intent Lista_produtos = new Intent(getBaseContext(), ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_numpedido), NumPedido);
            Lista_produtos.putExtras(params);
            startActivity(Lista_produtos);
            return;
        }
        if (DATA_DE_ENTREGA == null) {
            Toast.makeText(this, "A data prevista da entrega não foi selecionada!", Toast.LENGTH_SHORT).show();
            datePicker.show();
            return;
        }
        if (confBean == null) {
            Toast.makeText(this, "A forma de pagamento não foi escolhida!", Toast.LENGTH_SHORT).show();
            Boolean AtuPed = true;
            if (NumPedido.equals("0")) {
                AtuPed = false;
            }
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
            if (!itens_temp.isEmpty()) {
                Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                it.putExtra("CLI_CODIGO", CLI_CODIGO);
                it.putExtra("AtuPedido", AtuPed);
                it.putExtra("ChavePedido", Chave_Venda);
                startActivity(it);
            } else {
                Util.msg_toast_personal(getBaseContext(), "Adicione itens na venda", Util.PADRAO);
            }
        } else if (confBean.getConf_valor_recebido() == null) {
            Util.msg_toast_personal(getBaseContext(), "Você deve refazer o pagamento!", Util.PADRAO);
            Intent it = new Intent(getBaseContext(), ConfPagamento.class);
            it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
            it.putExtra("CLI_CODIGO", CLI_CODIGO);
            startActivity(it);
        } else if (confBean.getConf_valor_recebido() != null) {
            Double ValorVENDA = TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue();

            valor_recebido = new BigDecimal(confBean.getConf_valor_recebido().toString().trim()).setScale(2, RoundingMode.HALF_EVEN);
            //total_venda = new BigDecimal(TOTAL_DA_VENDA.setScale(2, RoundingMode.HALF_EVEN).subtract(calculaDesconto()).toString());
            total_venda = new BigDecimal(ValorVENDA.toString()).setScale(2, RoundingMode.HALF_EVEN);
            if ((total_venda.doubleValue()) != (valor_recebido.doubleValue())) {
                Util.msg_toast_personal(getBaseContext(), "Valor total do pedido diferente do valor total do pagamento", Util.PADRAO);

                if (!NumPedido.equals("0")) {
                    Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                    it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                    it.putExtra("CLI_CODIGO", CLI_CODIGO);
                    it.putExtra("ChavePedido", Chave_Venda);
                    it.putExtra("AtuPedido", true);
                    startActivity(it);
                } else {
                    Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                    it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                    it.putExtra("CLI_CODIGO", CLI_CODIGO);
                    startActivity(it);
                }

            } else {
                // if (verifica_limite_desconto()) {
                Gps gps = new Gps(getApplicationContext());
                vendaCBean = new SqliteVendaCBean();
                if (NumPedido.equals("0")) {
                    Random numero_aleatorio = new Random();
                    Integer chave = numero_aleatorio.nextInt(999999);
                    vendaCBean.setVendac_chave(String.valueOf(CLI_CODIGO + chave));
                    vendaCBean.setVendac_cli_codigo_ext(cliBean.getCli_codigo_ext());
                    vendaCBean.setVendac_cli_nome(cliBean.getCli_nome());
                    vendaCBean.setVendac_datahoravenda(Util.DataHojeComHorasUSA());
                } else {
                    vendaCBean.setVendac_chave(String.valueOf(Chave_Venda));
                    vendaCBean.setVendac_cli_codigo_ext(CLI_CODIGO_EXT);
                    vendaCBean.setVendac_cli_nome(nomeclievenda);
                    vendaCBean.setVendac_datahoravenda(DataHoraVenda);
                }
                vendaCBean.setVendac_previsaoentrega(DATA_DE_ENTREGA);
                vendaCBean.setVendac_cli_codigo(CLI_CODIGO);
                //vendaCBean.setVendac_cli_codigo_ext(cliBean.getCli_codigo_ext());
                //vendaCBean.setVendac_cli_nome(cliBean.getCli_nome());
                vendaCBean.setVendac_formapgto(confBean.getConf_tipo_pagamento());
                vendaCBean.setObservacao(ObsPedido);
                vendaCBean.setVendac_valor(total_venda);//(BigDecimal.ZERO);

                BigDecimal PERCENTUAL_DESCONTO = new BigDecimal(venda_txt_desconto.getText().toString());
                vendaCBean.setVendac_percdesconto(PERCENTUAL_DESCONTO);
                BigDecimal VALOR_DESCONTO = PERCENTUAL_DESCONTO.multiply(TOTAL_DA_VENDA).divide(new BigDecimal(100));
                vendaCBean.setVendac_desconto(VALOR_DESCONTO.setScale(2, BigDecimal.ROUND_UP));
                vendaCBean.setVendac_pesototal(BigDecimal.ZERO);
                vendaCBean.setVendac_enviada("1");
                vendaCBean.setCodEmpresa(CodEmpresa);
                vendaCBean.setVendac_latitude(gps.getLatitude());
                vendaCBean.setVendac_longitude(gps.getLongitude());

                SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                editor.putString("dataentrega", "");
                editor.commit();

                Sqlite_VENDADAO gravavenda = null;
                if (NumPedido.equals("0")) {
                    gravavenda = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, false);
                    venda_ok = gravavenda.grava_venda(vendaCBean, itens_temp);
                } else {
                    gravavenda = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true);
                    venda_ok = gravavenda.grava_vendasalva(vendaCBean, itens_venda);
                }
                if (venda_ok > 0) {
                    gerar_parcelas_venda();
                    // atualizando a chave da venda nas configuracoes de pagamento
                    new SqliteConfPagamentoDao(this).AtualizaVendac_chave_CONFPAGAMENTO(vendaCBean.getVendac_chave());
                    if (sincpedido == true) {
                        sincronizaPedidosAposSalvar();
                    } else {
                        Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), sCodVend);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        i.putExtras(params);
                        startActivity(i);
                        finish();
                    }
                }

                //onBackPressed();
                // }
            }
        }
    }

    public void gerar_parcelas_venda() {

        if (confBean.isMensal()) {
            iPagamento mensal = new Mensal();
            mensal.gerar_parcela(confBean, vendaCBean, this);
        }

        if (confBean.isAvista()) {
            iPagamento avista = new Avista();
            avista.gerar_parcela(confBean, vendaCBean, this);
        }
    }

    public void declaraObjetos() {
        confBean = new SqliteConfPagamentoBean();
        confDao = new SqliteConfPagamentoDao(this);
        venda_txv_codigo_cliente = (TextView) findViewById(R.id.venda_txv_codigo_cliente);
        venda_txv_empresa = (TextView) findViewById(R.id.venda_txv_empresa);
        venda_txv_total_da_Venda = (TextView) findViewById(R.id.venda_txv_total_da_Venda);
        ListView_ItensVendidos = (ListView) findViewById(R.id.ListView_ItensVendidos);
        venda_txv_dataentrega = (TextView) findViewById(R.id.venda_txv_dataentrega);
        venda_txv_datavenda = (TextView) findViewById(R.id.venda_txv_datavenda);
        //ListView_ItensVendidos = (ListView) findViewById(R.id.ListView_ItensVendidos);
        venda_txt_desconto = (EditText) findViewById(R.id.venda_txt_desconto);
        venda_txv_desconto = (TextView) findViewById(R.id.venda_txv_desconto);

    }

    public void atualiza_listview_e_calcula_total() {
        declaraObjetos();
        obterConfiguracoesPagamento();
        venda_txv_datavenda.setText("Data/Hora Venda : " + Util.DataHojeComHorasBR());
        if (verifica_limite_desconto()) {
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
            ListView_ItensVendidos.setAdapter(new ListAdapterItensTemporarios(getApplicationContext(), itens_temp));
            if (!itens_temp.isEmpty()) {
                TOTAL_DA_VENDA = BigDecimal.ZERO;
                for (SqliteVendaD_TempBean item : itens_temp) {
                    TOTAL_DA_VENDA = TOTAL_DA_VENDA.add(item.getVendad_quantidadeTEMP().multiply(item.getVendad_preco_vendaTEMP()));
                }
                Double ValorVENDA = TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue();

                venda_txv_total_da_Venda.setText("Total da venda = R$ " + new BigDecimal(ValorVENDA.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
            } else {
                venda_txv_total_da_Venda.setText("Total da venda = R$ " + "0,00");
            }

        }
        ListView_ItensVendidos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return false;
                }
                confirmar_exclusao_do_produto(listview, posicao);

                return false;
            }
        });
        ListView_ItensVendidos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                alterarproduto(listview1, posicao);


            }
        });
    }

    public void Alterar_Pedido_listview_e_calcula_total() {
        declaraObjetos();
        obterConfiguracoesPagamento();
        //venda_txv_datavenda.setText("Data/Hora Venda : " + vendaCBean.getVendac_datahoravenda());
        itens_venda = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).buscar_itens_vendas_por_numeropedido(Chave_Venda);
        ListView_ItensVendidos.setAdapter(new ListAdapterItensVenda(getApplicationContext(), itens_venda));
        if (!itens_venda.isEmpty()) {
            TOTAL_DA_VENDA = BigDecimal.ZERO;
            for (SqliteVendaDBean item : itens_venda) {
                TOTAL_DA_VENDA = TOTAL_DA_VENDA.add(item.getVendad_quantidade().multiply(item.getVendad_preco_venda()));
            }
            Double ValorVENDA = TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue();

            venda_txv_total_da_Venda.setText("Total da venda = R$ " + new BigDecimal(ValorVENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));
        } else {
            venda_txv_total_da_Venda.setText("Total da venda = R$ " + "0,00");
        }
        ListView_ItensVendidos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return false;
                }
                confirmar_exclusao_do_produto(listview, posicao);
                return false;
            }
        });
        ListView_ItensVendidos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (!verifica_limite_desconto()) {
                    return;
                }
                alterarproduto(listview1, posicao);

            }
        });
    }

    private BigDecimal calculaDesconto() {
        String VLDesconto = venda_txt_desconto.getText().toString();
        BigDecimal VALOR_DESCONTO = null;
        BigDecimal PERCENTUAL_DESCONTO = new BigDecimal(VLDesconto);
        VALOR_DESCONTO = PERCENTUAL_DESCONTO.multiply(TOTAL_DA_VENDA).divide(new BigDecimal(100));

        return VALOR_DESCONTO;
    }

    private void confirmar_exclusao_do_produto(final AdapterView listview, final int posicao) {
        if (!NumPedido.equals("0")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Atençao");
            builder.setMessage("Deseja excluir este produto ?");
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {

                    SqliteVendaDBean item = (SqliteVendaDBean) listview.getItemAtPosition(posicao);
                    new Sqlite_VENDADAO(getApplicationContext(), sCodVend, true).excluir_um_item_da_venda(item);

                    Alterar_Pedido_listview_e_calcula_total();
                }
            });
            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    dlg.dismiss();
                }
            });
            dlg = builder.create();
            dlg.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Atençao");
            builder.setMessage("Deseja excluir este produto ?");
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {

                    SqliteVendaD_TempBean item = (SqliteVendaD_TempBean) listview.getItemAtPosition(posicao);
                    new SqliteVendaD_TempDao(getApplicationContext()).excluir_um_item_da_venda(item);

                    atualiza_listview_e_calcula_total();
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

    }

    private void alterarproduto(final AdapterView listview, final int posicao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção");
        builder.setMessage("Deseja alterar a quantidade deste produto ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (NumPedido.equals("0")) {
                            SqliteVendaD_TempBean item = (SqliteVendaD_TempBean) listview.getItemAtPosition(posicao);
                            new SqliteVendaD_TempDao(getApplicationContext()).buscar_item_na_venda(item);
                            if (item != null) {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = inflater.inflate(R.layout.info_produto_venda, null);
                                alerta1 = new Builder(VenderProdutos.this);
                                alerta1.setCancelable(false);
                                alerta1.setView(view);

                                final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                                final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                                final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                                final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
                                final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);
                                spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);

                                spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        GravaPreferencias(spntabpreco.getSelectedItemPosition());
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });

                                try {
                                    String codprod = (item.getVendad_prd_codigoTEMP());
                                    codprod = codprod.trim();
                                    List<String> DadosListTabPreco = new ArrayList<String>();
                                    DB = new ConfigDB(VenderProdutos.this).getReadableDatabase();
                                    Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                                    CursorParametro.moveToFirst();
                                    vendenegativo = CursorParametro.getString(CursorParametro.getColumnIndex("HABITEMNEGATIVO"));
                                    tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                                    tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                                    tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                                    tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                                    tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                                    tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                                    tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                                    CursorParametro.close();

                                    DB = new ConfigDB(VenderProdutos.this).getReadableDatabase();
                                    Cursor produto_cursor = DB.rawQuery("SELECT CODITEMANUAL,QTDESTPROD,TABELAPADRAO,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2 FROM ITENS WHERE CODITEMANUAL ='" + codprod + "'", null);
                                    produto_cursor.moveToFirst();

                                    qtdestoque = produto_cursor.getDouble(produto_cursor.getColumnIndex("QTDESTPROD"));

                                    String tabelaPadrao = produto_cursor.getString(produto_cursor.getColumnIndex("TABELAPADRAO"));
                                    /*vlvendapadrao = vlvendapadrao.trim();
                                    if (!vlvendapadrao.equals("0,0000")) {
                                        BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                                        String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Precopadrao = Precopadrao.replace('.', ',');
                                        DadosListTabPreco.add(tab1 + " R$: " + Precopadrao);
                                    }*/

                                    if (!tab1.equals("")) {
                                        String vlvenda1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA1"));
                                        vlvenda1 = vlvenda1.trim();
                                        if (!vlvenda1.equals("0,0000")) {
                                            BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                                            String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco1 = Preco1.replace('.', ',');
                                            DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                                        }
                                    }

                                    if (!tab2.equals("")) {
                                        String vlvenda2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA2"));
                                        vlvenda2 = vlvenda2.trim();
                                        if (!vlvenda2.equals("0,0000")) {
                                            BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                                            String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco2 = Preco2.replace('.', ',');
                                            DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                                        }
                                    }

                                    if (!tab3.equals("")) {
                                        String vlvenda3 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA3"));
                                        vlvenda3 = vlvenda3.trim();
                                        if (!vlvenda3.equals("0,0000")) {
                                            BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                                            String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco3 = Preco3.replace('.', ',');
                                            DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                                        }
                                    }

                                    if (!tab4.equals("")) {
                                        String vlvenda4 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA4"));
                                        vlvenda4 = vlvenda4.trim();
                                        if (!vlvenda4.equals("0,0000")) {
                                            BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                                            String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco4 = Preco4.replace('.', ',');
                                            DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                                        }
                                    }

                                    if (!tab5.equals("")) {
                                        String vlvenda5 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA5"));
                                        vlvenda5 = vlvenda5.trim();
                                        if (!vlvenda5.equals("0,0000")) {
                                            BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                                            String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco5 = Preco5.replace('.', ',');
                                            DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                                        }
                                    }

                                    if (!tab6.equals("")) {
                                        String vlvendap1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP1"));
                                        vlvendap1 = vlvendap1.trim();
                                        if (!vlvendap1.equals("0,0000")) {
                                            BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                                            String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop1 = Precop1.replace('.', ',');
                                            DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                                        }
                                    }

                                    if (!tab7.equals("")) {
                                        String vlvendap2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP2"));
                                        vlvendap2 = vlvendap2.trim();
                                        if (!vlvendap2.equals("0,0000")) {
                                            BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                                            String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop2 = Precop2.replace('.', ',');
                                            DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                                        }
                                    }
                                    ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(VenderProdutos.this, android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                                    arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spntabpreco.setAdapter(arrayAdapterTabPreco);

                                    produto_cursor.close();
                                } catch (Exception E) {
                                    E.toString();

                                }
                                info_txv_codproduto.setText(item.getVendad_prd_codigoTEMP());
                                info_txv_descricaoproduto.setText(item.getVendad_prd_descricaoTEMP());
                                info_txv_unmedida.setText(item.getVendad_prd_unidadeTEMP());

                                String ValorItem = String.valueOf(item.getVendad_preco_vendaTEMP());
                                ValorItem = ValorItem.trim();
                                BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                Preco = Preco.replace('.', ',');
                                info_txv_precoproduto.setText(Preco);

                                String qtd = String.valueOf(item.getVendad_quantidadeTEMP());
                                info_txt_quantidadecomprada.setText(qtd);
                                info_txt_quantidadecomprada.requestFocus();
                                //info_txt_quantidadecomprada.selectAll();


                                alerta1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
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

                                                SqliteVendaD_TempBean itemBean1 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempBean itemBean2 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempBean itemBean3 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempDao itemDao = new SqliteVendaD_TempDao(getApplicationContext());

                                                itemBean2.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                                itemBean3 = itemDao.buscar_item_na_venda(itemBean2);

                                                if (itemBean3 != null) {
                                                    itemBean1.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                                    itemBean1.setVendad_prd_descricaoTEMP(DESCRICAO);
                                                    itemBean1.setVendad_prd_unidadeTEMP(UNIDADE);
                                                    itemBean1.setVendad_quantidadeTEMP(new BigDecimal(QUANTIDADE_DIGITADA));

                                                    //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                                                    String ValorItem = info_txv_precoproduto.getText().toString();
                                                    ValorItem = ValorItem.trim();
                                                    if (!ValorItem.equals("0,0000")) {
                                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                                        venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                                        itemBean1.setVendad_preco_vendaTEMP(venda);

                                                        //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                                        itemBean1.setVendad_totalTEMP(itemBean1.getSubTotal());
                                                        itemDao.atualizar_item_na_venda(itemBean1);
                                                        atualiza_listview_e_calcula_total();
                                                        //finish();
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
                                        VenderProdutos.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // fecha o teclado quando confirma a alteração  da quantidade.
                                    }

                                });
                                alerta1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                alerta1.show();
                            } else {
                                SqliteVendaDBean item2 = (SqliteVendaDBean) listview.getItemAtPosition(posicao);
                                if (NumPedido.equals("0")) {
                                    new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, false).excluir_um_item_da_venda(item2);
                                } else {
                                    new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).excluir_um_item_da_venda(item2);
                                }
                            }

                        } else {
                            SqliteVendaDBean item = (SqliteVendaDBean) listview.getItemAtPosition(posicao);
                            //SqliteVendaD_TempBean item = (SqliteVendaD_TempBean) listview.getItemAtPosition(posicao);
                            new Sqlite_VENDADAO(getApplicationContext(), sCodVend, true);
                            //new SqliteVendaD_TempDao(getApplicationContext()).buscar_item_na_venda(item);
                            if (item != null) {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = inflater.inflate(R.layout.info_produto_venda, null);
                                alerta1 = new Builder(VenderProdutos.this);
                                alerta1.setCancelable(false);
                                alerta1.setView(view);

                                final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                                final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                                final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                                final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
                                final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);
                                spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);

                                spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        GravaPreferencias(spntabpreco.getSelectedItemPosition());
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });

                                try {
                                    String codprod = (item.getVendad_prd_codigo());
                                    codprod = codprod.trim();
                                    List<String> DadosListTabPreco = new ArrayList<String>();
                                    DB = new ConfigDB(VenderProdutos.this).getReadableDatabase();
                                    Cursor CursorParametro = DB.rawQuery(" SELECT TIPOCRITICQTDITEM,HABITEMNEGATIVO,DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                                    CursorParametro.moveToFirst();
                                    vendenegativo = CursorParametro.getString(CursorParametro.getColumnIndex("HABITEMNEGATIVO"));
                                    tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                                    tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                                    tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                                    tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                                    tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                                    tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                                    tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                                    CursorParametro.close();
                                    Cursor produto_cursor = DB.rawQuery("SELECT CODITEMANUAL,QTDESTPROD,TABELAPADRAO,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2 FROM ITENS WHERE CODITEMANUAL ='" + codprod + "'", null);
                                    produto_cursor.moveToFirst();

                                    qtdestoque = produto_cursor.getDouble(produto_cursor.getColumnIndex("QTDESTPROD"));

                                    String tabelaPadrao = produto_cursor.getString(produto_cursor.getColumnIndex("TABELAPADRAO"));
                                    /*vlvendapadrao = vlvendapadrao.trim();
                                    if (!vlvendapadrao.equals("0,0000")) {
                                        BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                                        String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Precopadrao = Precopadrao.replace('.', ',');
                                        DadosListTabPreco.add(tab1 + " R$: " + Precopadrao);
                                    }*/

                                    if (!tab1.equals("")) {
                                        String vlvenda1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA1"));
                                        vlvenda1 = vlvenda1.trim();
                                        if (!vlvenda1.equals("0,0000")) {
                                            BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                                            String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco1 = Preco1.replace('.', ',');
                                            DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                                        }
                                    }

                                    if (!tab2.equals("")) {
                                        String vlvenda2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA2"));
                                        vlvenda2 = vlvenda2.trim();
                                        if (!vlvenda2.equals("0,0000")) {
                                            BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                                            String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco2 = Preco2.replace('.', ',');
                                            DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                                        }
                                    }

                                    if (!tab3.equals("")) {
                                        String vlvenda3 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA3"));
                                        vlvenda3 = vlvenda3.trim();
                                        if (!vlvenda3.equals("0,0000")) {
                                            BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                                            String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco3 = Preco3.replace('.', ',');
                                            DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                                        }
                                    }

                                    if (!tab4.equals("")) {
                                        String vlvenda4 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA4"));
                                        vlvenda4 = vlvenda4.trim();
                                        if (!vlvenda4.equals("0,0000")) {
                                            BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                                            String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco4 = Preco4.replace('.', ',');
                                            DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                                        }
                                    }

                                    if (!tab5.equals("")) {
                                        String vlvenda5 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDA5"));
                                        vlvenda5 = vlvenda5.trim();
                                        if (!vlvenda5.equals("0,0000")) {
                                            BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                                            String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco5 = Preco5.replace('.', ',');
                                            DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                                        }
                                    }

                                    if (!tab6.equals("")) {
                                        String vlvendap1 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP1"));
                                        vlvendap1 = vlvendap1.trim();
                                        if (!vlvendap1.equals("0,0000")) {
                                            BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                                            String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop1 = Precop1.replace('.', ',');
                                            DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                                        }
                                    }

                                    if (!tab7.equals("")) {
                                        String vlvendap2 = produto_cursor.getString(produto_cursor.getColumnIndex("VLVENDAP2"));
                                        vlvendap2 = vlvendap2.trim();
                                        if (!vlvendap2.equals("0,0000")) {
                                            BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                                            String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop2 = Precop2.replace('.', ',');
                                            DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                                        }
                                    }
                                    ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(VenderProdutos.this, android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                                    arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spntabpreco.setAdapter(arrayAdapterTabPreco);

                                    produto_cursor.close();
                                } catch (Exception E) {
                                    E.toString();

                                }
                                info_txv_codproduto.setText(item.getVendad_prd_codigo());
                                info_txv_descricaoproduto.setText(item.getVendad_prd_descricao());
                                info_txv_unmedida.setText(item.getVendad_prd_unidade());

                                String ValorItem = String.valueOf(item.getVendad_preco_venda());
                                ValorItem = ValorItem.trim();
                                BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                Preco = Preco.replace('.', ',');
                                info_txv_precoproduto.setText(Preco);

                                Cursor cursor = DB.rawQuery("select CODITEMANUAL, CHAVEPEDIDO, QTDMENORPED from peditens where CHAVEPEDIDO = '" + Chave_Venda + "'", null);
                                cursor.moveToFirst();
                                String qtdpedido = cursor.getString(cursor.getColumnIndex("QTDMENORPED"));
                                cursor.close();

                                info_txt_quantidadecomprada.setText(qtdpedido);
                                info_txt_quantidadecomprada.requestFocus();

                                //info_txt_quantidadecomprada.selectAll();


                                alerta1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
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
                                                Sqlite_VENDADAO itemDao = new Sqlite_VENDADAO(getApplicationContext(), sCodVend, true);

                                                itemBean2.setVendad_prd_codigo(COD_PRODUTO);
                                                //itemBean3 = itemDao.altera_item_na_venda(itemBean2);

                                                //if (itemBean3 != null) {
                                                itemBean1.setVendad_prd_codigo(COD_PRODUTO);
                                                itemBean1.setVendad_prd_descricao(DESCRICAO);
                                                itemBean1.setVendad_prd_unidade(UNIDADE);
                                                itemBean1.setVendad_quantidade(new BigDecimal(QUANTIDADE_DIGITADA));

                                                //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                                                String ValorItem = info_txv_precoproduto.getText().toString();
                                                ValorItem = ValorItem.trim();
                                                if (!ValorItem.equals("0,0000")) {
                                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                                    venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                                    itemBean1.setVendad_preco_venda(venda);

                                                    //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                                    itemBean1.setVendad_total(itemBean1.getSubTotal());
                                                    itemDao.atualizar_alteracao_item_na_venda(itemBean1);
                                                    Alterar_Pedido_listview_e_calcula_total();
                                                    //finish();
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
                                        VenderProdutos.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // fecha o teclado quando confirma a alteração  da quantidade.
                                    }

                                });
                                alerta1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                SharedPreferences prefsHost = VenderProdutos.this.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                                sprecoprincipal = prefsHost.getInt("spreco", 0);
                                spntabpreco.setSelection(sprecoprincipal);
                                alerta1.show();
                            } else {
                                SqliteVendaDBean item2 = (SqliteVendaDBean) listview.getItemAtPosition(posicao);
                                if (NumPedido.equals("0")) {
                                    new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, false).excluir_um_item_da_venda(item2);
                                } else {
                                    new Sqlite_VENDADAO(getApplicationContext(), CodVendedor, true).excluir_um_item_da_venda(item2);
                                }
                            }

                        }
                    }
                }

        );
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener()

                {
                    public void onClick(DialogInterface arg0, int arg1) {
                        dlg.dismiss();
                    }
                }

        );
        dlg = builder.create();
        dlg.show();

    }

    public void GravaPreferencias(int preco) {

        prefs = getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsPrivateEditor = prefs.edit();

        prefsPrivateEditor.putInt("spreco", preco);

        prefsPrivateEditor.commit();

    }


    @Override
    public void onBackPressed() {
        if (!NumPedido.equals("0")) {
            new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();
        }
        itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
        if (!itens_temp.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Deseja realmente cancelar a venda?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent it = new Intent();
                            it.putExtra("atualizalista", true); //true: Atualiza a Tela anterior
                            setResult(1, it);
                            new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

                            SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
                            editor.putString("dataentrega", "");
                            editor.commit();

                            finish();
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        } else {
            Intent it = new Intent();
            it.putExtra("atualizalista", true); //true: Atualiza a Tela anterior
            setResult(1, it);
            new SqliteVendaD_TempDao(getApplicationContext()).excluir_itens();

            SharedPreferences.Editor editor = getSharedPreferences(DATA_ENT, MODE_PRIVATE).edit();
            editor.putString("dataentrega", "");
            editor.commit();

            finish();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            if (venda_txt_desconto.getText().toString().isEmpty()) {
                venda_txt_desconto.setText("0");
            }
            if (!NumPedido.equals("0")) {
                Alterar_Pedido_listview_e_calcula_total();
                ((InputMethodManager) VenderProdutos.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        venda_txt_desconto.getWindowToken(), 0);
                return true;

            } else {
                atualiza_listview_e_calcula_total();
                ((InputMethodManager) VenderProdutos.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        venda_txt_desconto.getWindowToken(), 0);
                return true;
            }

        }
        return false;
    }

    public void sincronizaPedidosAposSalvar() {


        final AlertDialog.Builder builderAut = new AlertDialog.Builder(this);
        builderAut.setTitle("Gerar Pedido?");
        builderAut.setMessage("Sim - Pedido | Não - Orçamento");
        builderAut.setCancelable(true);
        builderAut.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Chave_Venda = vendaCBean.getVendac_chave();
                Cursor CursorPedido = DB.rawQuery(" SELECT NUMPED,CHAVE_PEDIDO, CODCLIE_EXT, CODCLIE FROM PEDOPER WHERE CHAVE_PEDIDO = " + Chave_Venda, null);
                CursorPedido.moveToFirst();
                numpedido = CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED"));
                CodClie_Int = CursorPedido.getString(CursorPedido.getColumnIndex("CODCLIE"));
                String chavepedido = CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO"));
                numpedido = Util.AcrescentaZeros(numpedido.toString(), 4);

                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '5' WHERE CHAVE_PEDIDO = '" + chavepedido + "'");
                Boolean ConexOk = Util.checarConexaoCelular(VenderProdutos.this);
                if (ConexOk == true) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                    builder.setTitle("Sincronizar");
                    builder.setMessage("Deseja Sincronizar o Pedido " + numpedido + " agora?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            dialog = new ProgressDialog(VenderProdutos.this);
                            dialog.setMessage("Sincronizando pedido nº " + numpedido);
                            dialog.setCancelable(false);
                            dialog.setTitle("Aguarde");
                            dialog.show();
                            Thread thread = new Thread(VenderProdutos.this);
                            thread.start();

                        }

                    });

                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Toast.makeText(VenderProdutos.this, "Pedido não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codvendedor), sCodVend);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            i.putExtras(params);
                            startActivity(i);
                            finish();

                            //onBackPressed();
                        }
                    });
                    alerta = builder.create();
                    alerta.show();
                } else {
                    Toast.makeText(VenderProdutos.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    i.putExtras(params);
                    startActivity(i);
                    finish();
                    //onBackPressed();
                }
            }

        });
        builderAut.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(VenderProdutos.this, "Pedido não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                i.putExtras(params);
                startActivity(i);
                finish();
                //onBackPressed();
            }
        });
        alerta = builderAut.create();
        alerta.show();


    }

    public void run() {
        try {
            String sitclieenvio;
            String pedidoendiado;
            Cursor CursorClie = DB.rawQuery("SELECT CODCLIE_EXT, FLAGINTEGRADO FROM CLIENTES WHERE CODCLIE_INT = '" + CodClie_Int + "'", null);
            CursorClie.moveToFirst();
            int CodClie_Ext = CursorClie.getInt(CursorClie.getColumnIndex("CODCLIE_EXT"));
            String FlagIntegrado = CursorClie.getString(CursorClie.getColumnIndex("FLAGINTEGRADO"));
            if (FlagIntegrado.equals("1")) {
                sitclieenvio = Sincronismo.SincronizarClientesEnvioStatic(CodClie_Int, this, usuario, senha);
                if (sitclieenvio.equals("OK")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VenderProdutos.this, "Cliente sincronizado com sucesso!", Toast.LENGTH_LONG).show();
                        }
                    });
                    pedidoendiado = Sincronismo.SincronizarPedidosEnvioStatic(usuario, senha, this, NumPedido);
                    if (pedidoendiado.equals("OK")) {
                        dialog.dismiss();
                        Intent intent = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                        Bundle params = new Bundle();
                        params.putString(getString(R.string.intent_codvendedor), sCodVend);
                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                        params.putString(getString(R.string.intent_usuario), usuario);
                        params.putString(getString(R.string.intent_senha), senha);
                        intent.putExtras(params);
                        startActivityForResult(intent, 1);
                        finish();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VenderProdutos.this, "Pedido sincronizado com sucesso!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            } else {
                pedidoendiado = Sincronismo.SincronizarPedidosEnvioStatic(usuario, senha, this, NumPedido);
                if (pedidoendiado.equals("OK")) {
                    dialog.dismiss();
                    Intent intent = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    intent.putExtras(params);
                    startActivityForResult(intent, 1);
                    finish();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VenderProdutos.this, "Pedido sincronizado com sucesso!", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    dialog.dismiss();
                    Intent intent = new Intent(VenderProdutos.this, ConsultaPedidos.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codvendedor), sCodVend);
                    params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    intent.putExtras(params);
                    startActivityForResult(intent, 1);
                    finish();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VenderProdutos.this, "Houve uma falha ao enviar o pedido. Tente novamente!", Toast.LENGTH_LONG).show();
                        }
                    });

                }

            }
        } catch (Exception e) {
            e.toString();
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        /*if(!hasFocus) {
            if (venda_txt_desconto.getText().toString().isEmpty()) {
                venda_txt_desconto.setText("0");
              //  return;
            }
        }*/
    }
}