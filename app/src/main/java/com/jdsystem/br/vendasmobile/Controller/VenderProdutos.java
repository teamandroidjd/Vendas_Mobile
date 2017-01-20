package com.jdsystem.br.vendasmobile.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoDao;
import com.jdsystem.br.vendasmobile.Model.SqliteParametroDao;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.Model.Sqlite_VENDADAO;
import com.jdsystem.br.vendasmobile.Pagamento.Avista;
import com.jdsystem.br.vendasmobile.Pagamento.Mensal;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Gps;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.actListPedidos;
import com.jdsystem.br.vendasmobile.actSincronismo;
import com.jdsystem.br.vendasmobile.adapter.ListaItensTemporariosAdapter;
import com.jdsystem.br.vendasmobile.interfaces.iPagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class VenderProdutos extends Activity implements View.OnKeyListener, Runnable {

    private BigDecimal TOTAL_DA_VENDA;
    private Intent CLI_CODIGO_INTENT;
    private Integer CLI_CODIGO;
    private Integer CLI_CODIGO_EXT;
    private String CodEmpresa;
    private ListView ListView_ItensVendidos;
    private List<SqliteVendaD_TempBean> itens_temp = new ArrayList<>();
    private List<SqliteVendaDBean> itens_venda = new ArrayList<>();

    private TextView venda_txv_total_da_Venda;
    private EditText venda_txt_desconto;
    private Double DESCONTO_PADRAO_VENDEDOR;
    private SimpleDateFormat dateFormatterBR;
    private SimpleDateFormat dateFormatterUSA;
    private DatePickerDialog datePicker;
    private TextView venda_txv_datavenda;
    private TextView venda_txv_dataentrega, venda_txv_desconto;
    private String DATA_DE_ENTREGA, CodVendedor, ObsPedido, NumPedido;
    private Toolbar toolbar;
    private SqliteClienteBean cliBean;
    public String Chave_Venda;
    public String sCodVend, URLPrincipal;
    public ProgressDialog dialog;
    public Long venda_ok;
    public AlertDialog alerta;

    private SqliteVendaCBean vendaCBean;
    private SqliteVendaDBean vendaDBean;

    private SqliteConfPagamentoDao confDao;
    private SqliteConfPagamentoBean confBean;
    private AlertDialog dlg;
    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vender_produtos);

        declaraObjetos();
        DATA_DE_ENTREGA = "";
        ObsPedido = "";
        setDateTimeField();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("CodVendedor");
                URLPrincipal = params.getString("urlPrincipal");
            }
        }

        CLI_CODIGO_INTENT = getIntent();
        CLI_CODIGO = CLI_CODIGO_INTENT.getIntExtra("CLI_CODIGO", 0);
        CodVendedor = CLI_CODIGO_INTENT.getStringExtra("CodVendedor");
        NumPedido = CLI_CODIGO_INTENT.getStringExtra("numpedido");
        CodEmpresa = CLI_CODIGO_INTENT.getStringExtra("codempresa");

        venda_txt_desconto.setOnKeyListener(this);

        venda_txt_desconto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                venda_txt_desconto.setSelectAllOnFocus(true);
                view.clearFocus();
                view.requestFocus();
            }

        });

        if (!NumPedido.equals("0")) {
            vendaCBean = new SqliteVendaCBean();
            vendaCBean = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor).buscar_vendas_por_numeropedido(NumPedido.toString());
            //vendaDBean = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor).buscar_itens_vendas_por_numeropedido(vendaCBean.getVendac_chave().toString());
            Chave_Venda = vendaCBean.getVendac_chave();
        }

        if (CLI_CODIGO.equals(0)) {
            CLI_CODIGO = vendaCBean.getVendac_cli_codigo();
        }

        cliBean = new SqliteClienteBean();
        cliBean = new SqliteClienteDao(getApplicationContext()).buscar_cliente_pelo_codigo(CLI_CODIGO.toString());
        TextView venda_txv_codigo_cliente = (TextView) findViewById(R.id.venda_txv_codigo_cliente);
        venda_txv_codigo_cliente.setText("Cliente: " + CLI_CODIGO.toString() + " - " + cliBean.getCli_nome().toString());
        venda_txv_codigo_cliente.requestFocus();

        DB = new ConfigDB(this).getReadableDatabase();
        TextView venda_txv_empresa = (TextView) findViewById(R.id.venda_txv_empresa);
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

        DESCONTO_PADRAO_VENDEDOR = new SqliteParametroDao(this).busca_parametros().getP_desconto_do_vendedor();

        CLI_CODIGO_EXT = Integer.valueOf(cliBean.getCli_codigo_ext().toString());
        FloatingActionButton incluirProduto = (FloatingActionButton) findViewById(R.id.fab_inclui_produto);
        incluirProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Lista_produtos = new Intent(getBaseContext(), Lista_produtos.class);
                Bundle params = new Bundle();
                params.putString("numpedido", NumPedido);
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
                finalzarvenda();
            }
        });

        toolbar.findViewById(R.id.cancel_venda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    finish();
                }
            }
        });

        toolbar.findViewById(R.id.item_formapgto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venda_txt_desconto.getText().toString().isEmpty()) {
                    venda_txt_desconto.setText("0");
                }
                if (verifica_limite_desconto()) {

                    itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
                    if (!itens_temp.isEmpty()) {
                        Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                        it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                        //it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.doubleValue());
                        it.putExtra("CLI_CODIGO", CLI_CODIGO);
                        startActivity(it);
                    } else {
                        Util.msg_toast_personal(getBaseContext(), "Adicione itens na venda", Util.PADRAO);
                    }

                }
            }
        });
        toolbar.findViewById(R.id.item_dtentrega).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setTitle("Entrega Prevista");
                datePicker.show();
            }
        });

        toolbar.findViewById(R.id.item_obspedido).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = (LayoutInflater.from(VenderProdutos.this)).inflate(R.layout.input_obs_pedido, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VenderProdutos.this);
                alertBuilder.setView(view);
                final EditText userInput = (EditText) view.findViewById(R.id.inputobspedido);
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
        });

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
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (venda_txt_desconto.getText().toString().isEmpty()) {
            venda_txt_desconto.setText("0");
        }
        atualiza_listview_e_calcula_total();
        obterConfiguracoesPagamento();
    }

    private void obterConfiguracoesPagamento() {
        confBean = confDao.busca_CONFPAGAMENTO_sem_chave();
        if (confBean != null) {
            if (confBean.isAvista()) {
                venda_txv_desconto.setVisibility(View.VISIBLE);
                venda_txt_desconto.setVisibility(View.VISIBLE);
                //venda_txt_desconto.setText("0");
            }
        }
    }

    public boolean verifica_limite_desconto() {

        if (Double.parseDouble(venda_txt_desconto.getText().toString()) > DESCONTO_PADRAO_VENDEDOR) {
            Util.msg_toast_personal(getBaseContext(), "Limite de desconto incompatível", Util.PADRAO);
            return false;
        }

        return true;
    }

    private void finalzarvenda() {
        BigDecimal valor_recebido = null;
        BigDecimal total_venda = null;
        if (itens_temp.isEmpty()) {
            Toast.makeText(this, "Nenhum produto foi selecionado", Toast.LENGTH_SHORT).show();
            Intent Lista_produtos = new Intent(getBaseContext(), Lista_produtos.class);
            Bundle params = new Bundle();
            params.putString("numpedido", NumPedido);
            Lista_produtos.putExtras(params);
            startActivity(Lista_produtos);

        } else if (DATA_DE_ENTREGA.equals("")) {
            Toast.makeText(this, "A data prevista da entrega não foi selecionada!", Toast.LENGTH_SHORT).show();
            datePicker.show();
        } else if (confBean == null) {
            Toast.makeText(this, "A forma de pagamento não foi escolhida!", Toast.LENGTH_SHORT).show();
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
            if (!itens_temp.isEmpty()) {
                Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                it.putExtra("CLI_CODIGO", CLI_CODIGO);
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

                Intent it = new Intent(getBaseContext(), ConfPagamento.class);
                it.putExtra("SUBTOTAL_VENDA", TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue());
                it.putExtra("CLI_CODIGO", CLI_CODIGO);
                startActivity(it);
            } else {
                if (verifica_limite_desconto()) {
                    Gps gps = new Gps(getApplicationContext());
                    vendaCBean = new SqliteVendaCBean();
                    Random numero_aleatorio = new Random();
                    Integer chave = numero_aleatorio.nextInt(90000);
                    vendaCBean.setVendac_chave(String.valueOf(CLI_CODIGO + chave));
                    vendaCBean.setVendac_datahoravenda(Util.DataHojeComHorasUSA());
                    vendaCBean.setVendac_previsaoentrega(DATA_DE_ENTREGA);
                    vendaCBean.setVendac_cli_codigo(CLI_CODIGO);
                    vendaCBean.setVendac_cli_codigo_ext(cliBean.getCli_codigo_ext());
                    vendaCBean.setVendac_cli_nome(cliBean.getCli_nome());
                    vendaCBean.setVendac_formapgto(confBean.getConf_tipo_pagamento());
                    vendaCBean.setObservacao(ObsPedido);
                    vendaCBean.setVendac_valor(BigDecimal.ZERO);

                    BigDecimal PERCENTUAL_DESCONTO = new BigDecimal(venda_txt_desconto.getText().toString());
                    BigDecimal VALOR_DESCONTO = PERCENTUAL_DESCONTO.multiply(TOTAL_DA_VENDA).divide(new BigDecimal(100));
                    vendaCBean.setVendac_desconto(VALOR_DESCONTO.setScale(2, BigDecimal.ROUND_UP));

                    vendaCBean.setVendac_pesototal(BigDecimal.ZERO);
                    vendaCBean.setVendac_enviada("1");
                    vendaCBean.setCodEmpresa(CodEmpresa);
                    vendaCBean.setVendac_latitude(gps.getLatitude());
                    vendaCBean.setVendac_longitude(gps.getLongitude());
                    Sqlite_VENDADAO gravavenda = new Sqlite_VENDADAO(getApplicationContext(), CodVendedor);

                    venda_ok = gravavenda.grava_venda(vendaCBean, itens_temp);

                    if (venda_ok > 0) {
                        gerar_parcelas_venda();
                        // atualizando a chave da venda nas configuracoes de pagamento
                        new SqliteConfPagamentoDao(this).AtualizaVendac_chave_CONFPAGAMENTO(vendaCBean.getVendac_chave());
                    }
                    sincronizaPedidosAposSalvar();
                    //onBackPressed();
                }
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
        venda_txv_total_da_Venda = (TextView) findViewById(R.id.venda_txv_total_da_Venda);
        ListView_ItensVendidos = (ListView) findViewById(R.id.ListView_ItensVendidos);
        venda_txv_dataentrega = (TextView) findViewById(R.id.venda_txv_dataentrega);
        venda_txv_datavenda = (TextView) findViewById(R.id.venda_txv_datavenda);
        ListView_ItensVendidos = (ListView) findViewById(R.id.ListView_ItensVendidos);
        venda_txt_desconto = (EditText) findViewById(R.id.venda_txt_desconto);
        venda_txv_desconto = (TextView) findViewById(R.id.venda_txv_desconto);
    }

    public void atualiza_listview_e_calcula_total() {
        declaraObjetos();
        venda_txv_datavenda.setText("Data/Hora Venda : " + Util.DataHojeComHorasBR());
        if (verifica_limite_desconto()) {
            itens_temp = new SqliteVendaD_TempDao(getApplicationContext()).busca_todos_itens_da_venda();
            ListView_ItensVendidos.setAdapter(new ListaItensTemporariosAdapter(getApplicationContext(), itens_temp));
            if (!itens_temp.isEmpty()) {
                TOTAL_DA_VENDA = BigDecimal.ZERO;
                for (SqliteVendaD_TempBean item : itens_temp) {
                    TOTAL_DA_VENDA = TOTAL_DA_VENDA.add(item.getVendad_quantidadeTEMP().multiply(item.getVendad_preco_vendaTEMP()));
                }
                Double ValorVENDA = TOTAL_DA_VENDA.subtract(calculaDesconto()).doubleValue();

                venda_txv_total_da_Venda.setText("Total da venda = R$ " + new BigDecimal(ValorVENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));
            } else {
                venda_txv_total_da_Venda.setText("Total da venda = R$ " + "0,00");
            }

        }
        ListView_ItensVendidos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                confirmar_exclusao_do_produto(listview, posicao);
                return false;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atençao");
        builder.setMessage("Deseja excluir este produto ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (NumPedido.equals("0")) {
                    SqliteVendaD_TempBean item = (SqliteVendaD_TempBean) listview.getItemAtPosition(posicao);
                    new SqliteVendaD_TempDao(getApplicationContext()).excluir_um_item_da_venda(item);

                } else {
                    SqliteVendaDBean item = (SqliteVendaDBean) listview.getItemAtPosition(posicao);
                    new Sqlite_VENDADAO(getApplicationContext(), CodVendedor).excluir_um_item_da_venda(item);
                }
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


    @Override
    public void onBackPressed() {
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
            atualiza_listview_e_calcula_total();
            ((InputMethodManager) VenderProdutos.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    venda_txt_desconto.getWindowToken(), 0);
            return true;
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
                Cursor CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = " + venda_ok.toString(), null);
                CursorPedido.moveToFirst();
                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '5' WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'");
                Boolean ConexOk = Util.checarConexaoCelular(VenderProdutos.this);
                if (ConexOk == true) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(VenderProdutos.this);
                    builder.setTitle("Sincronizar");
                    builder.setMessage("Deseja Sincronizar o Pedido " + Util.AcrescentaZeros(venda_ok.toString(), 4) + " agora?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            dialog = new ProgressDialog(VenderProdutos.this);
                            dialog.setMessage("Sincronizando pedido nº " + venda_ok);
                            dialog.setTitle("Aguarde");
                            dialog.show();

                            Thread thread = new Thread(VenderProdutos.this);
                            thread.start();
                            Toast.makeText(VenderProdutos.this, "Pedido sincronizado com sucesso!", Toast.LENGTH_SHORT).show();
                        }

                    });

                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Toast.makeText(VenderProdutos.this, "Pedido não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    });
                    alerta = builder.create();
                    alerta.show();
                } else {
                    Toast.makeText(VenderProdutos.this, "Sem conexão com a Internet. Verifique.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }

        });
        builderAut.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(VenderProdutos.this, "Pedido não sincronizado com a base de dados.", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
        alerta = builderAut.create();
        alerta.show();


    }

    public void run() {
        try {
            actSincronismo.SincronizarPedidosEnvio(venda_ok.toString(), this, true);
            Intent intent = new Intent(VenderProdutos.this, actListPedidos.class);
            Bundle params = new Bundle();
            params.putString("codvendedor", sCodVend);
            params.putString("urlPrincipal", URLPrincipal);
            intent.putExtras(params);
            startActivityForResult(intent, 1);
            finish();
        } finally {
            //
        }
    }
}