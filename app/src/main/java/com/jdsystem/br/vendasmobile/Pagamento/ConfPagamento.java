package com.jdsystem.br.vendasmobile.Pagamento;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterFormpgtoTemp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class ConfPagamento extends AppCompatActivity implements Spinner.OnItemSelectedListener {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    private EditText conf_txtqtdparcelas, conf_txtvalorrecebido, edtdiasvenc;
    private TextView txvdatavenc, txvValorRestante,conf_txvvalorvenda;
    private Spinner conf_spfpgto;
    FloatingActionButton btnincluirpagamento;
    private String TIPO_PAGAMENTO = "", ChavePedido = "",descformpgto, qtdparcela;
    private Double SUBTOTAL_VENDA;
    private Boolean AtuPedido;
    private SqliteConfPagamentoDao confDao;
    private SqliteConfPagamentoBean confBean;
    private List<SqliteConfPagamentoBean> itens_temp = new ArrayList<>();
    private ListView ListView_formapgto;
    private int idPerfil, codformpgto, flag;
    private SQLiteDatabase DB;
    List<String> DadosList = new ArrayList<String>();
    Toolbar toolbar;
    private AlertDialog.Builder builderformpgto, builderalterexcluiparc;
    private AlertDialog.Builder alertaformpgto, alertaalterexcluiparc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conf_pagamento);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.toString();
        }

        declaraObjetosListeners();
        carregarpreferencias();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                SUBTOTAL_VENDA = params.getDouble("SUBTOTAL_VENDA", 0);
                ChavePedido = params.getString("ChavePedido");
                AtuPedido = params.getBoolean("AtuPedido", false);
                conf_txvvalorvenda.setText("Valor Venda: R$ " + new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));
            }
        }
        txvValorRestante.setVisibility(View.GONE);

        /*Intent INTENT_SOBTOTAL_VENDA = getIntent();
        SUBTOTAL_VENDA = INTENT_SOBTOTAL_VENDA.getDoubleExtra("SUBTOTAL_VENDA", 0);
        Intent INTENT_CLI_CODIGO = getIntent();
        Integer CLI_CODIGO = INTENT_CLI_CODIGO.getIntExtra("CLI_CODIGO", 0);
        ChavePedido = INTENT_CLI_CODIGO.getStringExtra("ChavePedido");
        AtuPedido = INTENT_CLI_CODIGO.getBooleanExtra("AtuPedido", false);
        conf_txvvalorvenda.setText("Valor Venda: R$ " + new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));
        txvValorRestante.setVisibility(View.GONE);*/

        carregaformapagamento();
        atualizalistviewparcelas();
        obterConfiguracoesPagamento();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    public void incluirformapagamento(final View view) {
        builderformpgto = new AlertDialog.Builder(this);
        builderformpgto.setTitle("Atenção");
        builderformpgto.setCancelable(true);
        builderformpgto.setMessage("Deseja incluir parcela?");
        builderformpgto.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        carregarparcelas();
                        int parcelas = 0;
                        if (qtdparcela == null) {
                            parcelas += 1;
                            //qtdparcela = String.valueOf(1);
                        } else {
                            parcelas = Integer.parseInt(qtdparcela);
                            parcelas += 1;
                        }

                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.input_form_pgto, null);
                        alertaformpgto = new AlertDialog.Builder(ConfPagamento.this);
                        alertaformpgto.setCancelable(false);
                        alertaformpgto.setView(v);


                        TextView desctotalvend = (TextView) v.findViewById(R.id.txvdescparcela);
                        TextView numparcela = (TextView) v.findViewById(R.id.txvnumparcelaformpgto);
                        final Spinner formpgto = (Spinner) v.findViewById(R.id.spnformpgtopercela);
                        edtdiasvenc = (EditText) v.findViewById(R.id.edtdiasvencimento);
                        txvdatavenc = (TextView) v.findViewById(R.id.txvdatavencimento);
                        final EditText edtvlparcela = (EditText) v.findViewById(R.id.edtvalorparcela);

                        numparcela.setText(String.valueOf(parcelas));

                        Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO FROM FORMAPAGAMENTO WHERE STATUS = 'A' AND CODPERFIL = " + idPerfil, null);
                        cursorformpgto.moveToFirst();
                        List<String> DadosList = new ArrayList<String>();
                        if (cursorformpgto.getCount() > 0) {
                            do {
                                DadosList.add(cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO")));
                            } while (cursorformpgto.moveToNext());
                        } else {
                            Toast.makeText(getBaseContext(), "Não existe forma de pagamento habilitada. Verifique!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cursorformpgto.close();
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        formpgto.setAdapter(arrayAdapter);

                        formpgto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                                TIPO_PAGAMENTO = spinner.getSelectedItem().toString();
                                SQLiteDatabase DB = new ConfigDB(ConfPagamento.this).getReadableDatabase();
                                Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO, CODEXTERNO FROM FORMAPAGAMENTO WHERE DESCRICAO = '" + TIPO_PAGAMENTO + "' AND CODPERFIL = " + idPerfil, null);
                                cursorformpgto.moveToFirst();
                                if (cursorformpgto.getCount() > 0) {

                                    codformpgto = cursorformpgto.getInt(cursorformpgto.getColumnIndex("CODEXTERNO"));
                                    descformpgto = cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO"));
                                }
                                cursorformpgto.close();

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        edtdiasvenc.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence char_digitado, int start, int before, int count) {
                                try {
                                    calcular_data_parcela(char_digitado);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        txvdatavenc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calcular_dias_parcela();
                            }
                        });

                        final int finalParcelas = parcelas;
                        alertaformpgto.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (AtuPedido) {
                                    String vlparcela = edtvlparcela.getText().toString();
                                    if (vlparcela.equals("0") || vlparcela.equals("")) {
                                        Util.msg_toast_personal(getBaseContext(), "Informe o valor da parcela", Util.ALERTA);
                                        return;

                                    } else if (Double.parseDouble(vlparcela) > SUBTOTAL_VENDA) {
                                        Util.msg_toast_personal(getBaseContext(), "Valor superior ao total do pedido. Verifique!", Util.ALERTA);
                                        return;
                                    }
                                    BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.')));
                                    vlparcela = valorparcela.setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                    try {
                                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL,CONF_DATA_VENCIMENTO)VALUES(" +
                                                "'" + String.valueOf(codformpgto) +
                                                "','" + edtdiasvenc.getText().toString() +
                                                "','" + TIPO_PAGAMENTO +
                                                "','" + vlparcela +
                                                "'," + finalParcelas +
                                                ",'" + ChavePedido +
                                                "','" + TIPO_PAGAMENTO +
                                                "','S'" +
                                                ", " + idPerfil +
                                                ", '" + txvdatavenc.getText().toString() + "');");

                                    } catch (Exception e) {
                                        e.toString();
                                    }
                                    flag = 1;
                                    conf_txtqtdparcelas.setText(String.valueOf(finalParcelas));
                                    atualizalistviewparcelas();
                                } else {
                                    ChavePedido = "";
                                    String vlparcela = edtvlparcela.getText().toString();
                                    if (vlparcela.equals("0") || vlparcela.equals("")) {
                                        Util.msg_toast_personal(getBaseContext(), "Informe o valor da parcela", Util.ALERTA);
                                        return;

                                    } else if (Double.parseDouble(vlparcela) > SUBTOTAL_VENDA) {
                                        Util.msg_toast_personal(getBaseContext(), "Valor superior ao total do pedido. Verifique!", Util.ALERTA);
                                        return;
                                    }
                                    BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.')));
                                    vlparcela = valorparcela.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    try {
                                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL,CONF_DATA_VENCIMENTO)VALUES(" +
                                                "'" + String.valueOf(codformpgto) +
                                                "','" + edtdiasvenc.getText().toString() +
                                                "','" + TIPO_PAGAMENTO +
                                                "','" + vlparcela +
                                                "'," + finalParcelas +
                                                ",'" + ChavePedido +
                                                "','" + TIPO_PAGAMENTO +
                                                "','S'" +
                                                ", " + idPerfil +
                                                ", '" + txvdatavenc.getText().toString() + "');");
                                    } catch (Exception e) {
                                        e.toString();
                                    }
                                }
                                flag = 1;
                                conf_txtqtdparcelas.setText(String.valueOf(finalParcelas));
                                atualizalistviewparcelas();
                            }
                        });

                        alertaformpgto.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                atualizalistviewparcelas();
                            }
                        });

                        alertaformpgto.show();

                    }
                }
        );
        builderformpgto.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //atualizalistviewparcelas();
                    }
                }
        );
        builderformpgto.show();


    }

    private void carregaformapagamento() {
        Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO FROM FORMAPAGAMENTO WHERE STATUS = 'A' AND CODPERFIL = " + idPerfil, null);
        cursorformpgto.moveToFirst();
        DadosList.add("Selecione a forma de pagamento");
        if (cursorformpgto.getCount() > 0) {
            do {
                DadosList.add(cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO")));
            } while (cursorformpgto.moveToNext());
        } else {
            Toast.makeText(this, "Não existe forma de pagamento habilitada. Verifique!", Toast.LENGTH_SHORT).show();
            return;
        }
        cursorformpgto.close();
        new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList).setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        conf_spfpgto.setAdapter(new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList));

    }

    private void obterConfiguracoesPagamento() {
        if (AtuPedido) {
            confBean = confDao.busca_CONFPAGAMENTO_Pedido(ChavePedido);
            if (confBean != null) {
                carregarparcelas();
                //if (qtdparcela.equals("1")) {
                TIPO_PAGAMENTO = confBean.getConf_descformpgto();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                int pos = arrayAdapter.getPosition(TIPO_PAGAMENTO);
                conf_spfpgto.setSelection(pos);
                //}
                //confBean = confDao.busca_CONFPAGAMENTO_Pedido(ChavePedido);
                flag = 1;
                conf_txtqtdparcelas.setText(qtdparcela);
                atualizalistviewparcelas();
            }

        } else {
            confBean = confDao.busca_CONFPAGAMENTO_sem_chave();
            if (confBean != null) {
                TIPO_PAGAMENTO = confBean.getConf_descformpgto();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                int pos = arrayAdapter.getPosition(TIPO_PAGAMENTO);
                conf_spfpgto.setSelection(pos);
                carregarparcelas();
                //new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
                flag = 1;
                conf_txtqtdparcelas.setText(qtdparcela);


            }
        }
    }

    private void carregarparcelas() {
        if (AtuPedido) {
            try {
                Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_parcelas FROM CONFPAGAMENTO WHERE vendac_chave = '" + ChavePedido + "' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {
                    do {
                        qtdparcela = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_parcelas"));

                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();
                }
            } catch (Exception e) {
                e.toString();
            }
        } else {
            try {
                Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_parcelas FROM CONFPAGAMENTO WHERE vendac_chave = '' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {
                    do {
                        qtdparcela = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_parcelas"));

                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();

                }
            } catch (Exception e) {
                e.toString();
            }
        }
    }

    public void salvar_fpgto(View v) {
        if (itens_temp.isEmpty()) {
            Util.msg_toast_personal(getBaseContext(), "Informe a forma de pagamento", Util.ALERTA);
            return;
        }
        // condicao sem entrada
        if (conf_txtqtdparcelas.getText().toString().trim().equals("") || conf_txtqtdparcelas.getText().toString().trim().equals("0")) {
            Util.msg_toast_personal(getBaseContext(), getString(R.string.enter_quntity), Util.ALERTA);
            return;
        }
        if (AtuPedido) {
            try {
                Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_valor_recebido FROM CONFPAGAMENTO WHERE vendac_chave = '" + ChavePedido + "' AND conf_temp = 'N' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {
                    double vltotal = 0;
                    do {
                        double vlparcela = cursorconfpagamento.getDouble(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                        vltotal = vltotal + vlparcela;
                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();
                    BigDecimal VALORRECEBIDO = new BigDecimal(vltotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                    vltotal = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                    if (vltotal != SUBTOTAL_VENDA) {
                        Util.msg_toast_personal(getBaseContext(), "Valor total das parcelas difere do valor total do pedido. Verifique!", Util.ALERTA);
                        return;
                    }else {
                        finish();
                    }
                }
            } catch (Exception e) {
                e.toString();

            }
        } else {
            try {
                Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_valor_recebido FROM CONFPAGAMENTO WHERE vendac_chave = '' AND conf_temp = 'N' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {
                    double vltotal = 0;
                    do {
                        double vlparcela = cursorconfpagamento.getDouble(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                        BigDecimal VALORRECEBIDO = new BigDecimal(vlparcela).setScale(2, BigDecimal.ROUND_HALF_UP);
                        vlparcela = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                        vltotal = vltotal + vlparcela;
                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();
                    BigDecimal VALORRECEBIDO = new BigDecimal(vltotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                    vltotal = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                    BigDecimal subtotal = new BigDecimal(SUBTOTAL_VENDA).setScale(2, BigDecimal.ROUND_HALF_UP);
                    SUBTOTAL_VENDA = Double.parseDouble(String.valueOf(subtotal));
                    if (vltotal != SUBTOTAL_VENDA) {
                        Util.msg_toast_personal(getBaseContext(), "Valor total das parcelas difere do valor total do pedido. Verifique!", Util.ALERTA);
                        return;
                    }else {
                        finish();
                    }
                }
            } catch (Exception e) {
                e.toString();

            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
        TIPO_PAGAMENTO = spinner.getSelectedItem().toString();
        SQLiteDatabase DB = new ConfigDB(this).getReadableDatabase();
        Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO, CODEXTERNO FROM FORMAPAGAMENTO WHERE DESCRICAO = '" + TIPO_PAGAMENTO + "' AND CODPERFIL = " + idPerfil, null);
        cursorformpgto.moveToFirst();
        if (cursorformpgto.getCount() > 0) {

            codformpgto = cursorformpgto.getInt(cursorformpgto.getColumnIndex("CODEXTERNO"));
            descformpgto = cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO"));
        }
        cursorformpgto.close();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void calcular_valor_parcela(CharSequence valor_digitado, int tipo) throws ParseException {
        if (tipo == 1) {
            flag = 0;
            return;
        }
        if (TIPO_PAGAMENTO.equals("Selecione a forma de pagamento")) {
            Util.msg_toast_personal(getBaseContext(), "Informe a forma de pagamento", Util.ALERTA);
            flag = 1;
            conf_txtqtdparcelas.setText("");
            return;
        }
        if (valor_digitado.toString().equals("")) {
            if (AtuPedido) {
                //confBean = confDao.busca_CONFPAGAMENTO_Pedido(ChavePedido);
                new SqliteConfPagamentoDao(this).excluir_FormaPgto_Chave(ChavePedido);

            } else {
                new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
            }
            atualizalistviewparcelas();
            return;
        } else if (Integer.parseInt(String.valueOf(valor_digitado)) == 0) {
            Toast.makeText(this, "Informe a quantidade de parcela!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (AtuPedido) {
            new SqliteConfPagamentoDao(this).excluir_FormaPgto_Chave(ChavePedido);
            String date = Util.DataHojeSemHorasBR();
            SimpleDateFormat dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy");
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.setTime(dateFormatterBR.parse(date));
            int qtdparcela = 0;
            int dias = 0;
            int diasVenc = 0;
            String QUANTIDADE_PARCELAS = conf_txtqtdparcelas.getText().toString();
            BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
            BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
            BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
            BigDecimal recalculo = valor_parcela.multiply(divisor);
            BigDecimal diferenca = valor_venda.subtract(recalculo);
            BigDecimal parc_1 = valor_parcela.add(diferenca).setScale(2, BigDecimal.ROUND_HALF_UP);
            try {
                do {
                    qtdparcela += 1;
                    dias += 30;
                    diasVenc += 30;
                    if (qtdparcela == 1) {
                        dias += 30;
                    }
                    newCalendar.add(Calendar.DATE, dias);
                    date = dateFormatterBR.format(newCalendar.getTime());
                    if (qtdparcela == 1) {
                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL, conf_temp,CONF_DATA_VENCIMENTO)VALUES(" +
                                "'" + String.valueOf(codformpgto) +
                                "','" + dias +
                                "','" + TIPO_PAGAMENTO +
                                "','" + parc_1 +
                                "'," + qtdparcela +
                                ",'" + ChavePedido +
                                "','" + TIPO_PAGAMENTO +
                                "','S'" +
                                ", " + idPerfil +
                                ", 'N'" +
                                ", '" + date + "');");
                    } else {
                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL,conf_temp,CONF_DATA_VENCIMENTO)VALUES(" +
                                "'" + String.valueOf(codformpgto) +
                                "','" + dias +
                                "','" + TIPO_PAGAMENTO +
                                "','" + valor_parcela +
                                "'," + qtdparcela +
                                ",'" + ChavePedido +
                                "','" + TIPO_PAGAMENTO +
                                "','S'" +
                                ", " + idPerfil +
                                ", 'N'" +
                                ", '" + date + "');");
                    }


                } while (qtdparcela < Integer.parseInt(String.valueOf(valor_digitado)));
            } catch (Exception e) {
                e.toString();
            }

            atualizalistviewparcelas();
        } else {
            new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
            String date = Util.DataHojeSemHorasBR();
            SimpleDateFormat dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy");
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.setTime(dateFormatterBR.parse(date));
            int qtdparcela = 0;
            int dias = 0;
            int diasVenc = 0;
            ChavePedido = "";
            String QUANTIDADE_PARCELAS = conf_txtqtdparcelas.getText().toString();
            BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
            BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
            BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
            BigDecimal recalculo = valor_parcela.multiply(divisor);
            BigDecimal diferenca = valor_venda.subtract(recalculo);
            BigDecimal parc_1 = valor_parcela.add(diferenca);
            try {
                do {
                    qtdparcela += 1;
                    diasVenc += 30;
                    if (qtdparcela == 1) {
                        dias += 30;
                    }
                    newCalendar.add(Calendar.DATE, dias);
                    date = dateFormatterBR.format(newCalendar.getTime());
                    if (qtdparcela == 1) {

                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL,CONF_DATA_VENCIMENTO)VALUES(" +
                                "'" + String.valueOf(codformpgto) +
                                "','" + diasVenc +
                                "','" + TIPO_PAGAMENTO +
                                "','" + parc_1 +
                                "'," + qtdparcela +
                                ",'" + ChavePedido +
                                "','" + TIPO_PAGAMENTO +
                                "','S'" +
                                ", " + idPerfil +
                                ", '" + date + "');");
                    } else {
                        DB.execSQL("INSERT INTO CONFPAGAMENTO(CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto,conf_valor_recebido,conf_parcelas," +
                                "vendac_chave,conf_tipo_pagamento,conf_sementrada_comentrada,CODPERFIL,CONF_DATA_VENCIMENTO)VALUES(" +
                                "'" + String.valueOf(codformpgto) +
                                "','" + diasVenc +
                                "','" + TIPO_PAGAMENTO +
                                "','" + valor_parcela +
                                "'," + qtdparcela +
                                ",'" + ChavePedido +
                                "','" + TIPO_PAGAMENTO +
                                "','S'" +
                                ", " + idPerfil +
                                ", '" + date + "');");
                    }


                } while (qtdparcela < Integer.parseInt(String.valueOf(valor_digitado)));
            } catch (Exception e) {
                e.toString();
            }

            atualizalistviewparcelas();
        }
    }

    private void atualizalistviewparcelas() {
        if (AtuPedido) {
            itens_temp = new SqliteConfPagamentoDao(getApplicationContext()).busca_todos_CONFPAGAMENTO_nao_enviados(ChavePedido);
            ListView_formapgto.setAdapter(new ListAdapterFormpgtoTemp(getApplicationContext(), itens_temp));
            ListView_formapgto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
                    alteraexcluiparcela(listview1, posicao);
                }
            });
        } else {
            ChavePedido = "";
            itens_temp = new SqliteConfPagamentoDao(getApplicationContext()).busca_todos_CONFPAGAMENTO_nao_enviados(ChavePedido);
            ListView_formapgto.setAdapter(new ListAdapterFormpgtoTemp(getApplicationContext(), itens_temp));
            ListView_formapgto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
                    alteraexcluiparcela(listview1, posicao);
                }
            });

        }
        if (!itens_temp.isEmpty()) {
            if (AtuPedido) {
                try {
                    Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_valor_recebido FROM CONFPAGAMENTO WHERE vendac_chave = '" + ChavePedido + "' AND conf_temp = 'N' AND CODPERFIL = " + idPerfil, null);
                    cursorconfpagamento.moveToFirst();
                    if (cursorconfpagamento.getCount() > 0) {
                        double vltotal = 0;
                        do {
                            double vlparcela = cursorconfpagamento.getDouble(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                            vltotal = vltotal + vlparcela;
                        } while (cursorconfpagamento.moveToNext());
                        cursorconfpagamento.close();
                        BigDecimal VALORRECEBIDO = new BigDecimal(vltotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                        vltotal = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                        BigDecimal subtotal = new BigDecimal(SUBTOTAL_VENDA).setScale(2, BigDecimal.ROUND_HALF_UP);
                        if (vltotal != SUBTOTAL_VENDA) {
                            BigDecimal vlrestante = subtotal.subtract(BigDecimal.valueOf(vltotal));
                            txvValorRestante.setVisibility(View.VISIBLE);
                            txvValorRestante.setText(String.valueOf("Valor restante: " + vlrestante));
                            txvValorRestante.setTextColor(Color.RED);
                        }
                    }
                } catch (Exception e) {
                    e.toString();

                }
            } else {
                try {
                    Cursor cursorconfpagamento = DB.rawQuery("SELECT conf_valor_recebido FROM CONFPAGAMENTO WHERE vendac_chave = '' AND conf_temp = 'N' AND CODPERFIL = " + idPerfil, null);
                    cursorconfpagamento.moveToFirst();
                    if (cursorconfpagamento.getCount() > 0) {
                        double vltotal = 0;
                        do {
                            double vlparcela = cursorconfpagamento.getDouble(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                            BigDecimal VALORRECEBIDO = new BigDecimal(vlparcela).setScale(2, BigDecimal.ROUND_HALF_UP);
                            vlparcela = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                            vltotal = vltotal + vlparcela;
                        } while (cursorconfpagamento.moveToNext());
                        cursorconfpagamento.close();
                        BigDecimal VALORRECEBIDO = new BigDecimal(vltotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                        vltotal = Double.parseDouble(String.valueOf(VALORRECEBIDO));
                        BigDecimal subtotal = new BigDecimal(SUBTOTAL_VENDA).setScale(2, BigDecimal.ROUND_HALF_UP);
                        SUBTOTAL_VENDA = Double.parseDouble(String.valueOf(subtotal));
                        if (vltotal != SUBTOTAL_VENDA) {
                            BigDecimal vlrestante = subtotal.subtract(BigDecimal.valueOf(vltotal));
                            txvValorRestante.setVisibility(View.VISIBLE);
                            txvValorRestante.setText(String.valueOf("Valor restante: " + vlrestante).replace(".", ","));
                            txvValorRestante.setTextColor(Color.RED);
                        }
                    }
                } catch (Exception e) {
                    e.toString();

                }

            }

        } else {
            txvValorRestante.setVisibility(View.GONE);
        }
    }

    private void alteraexcluiparcela(final AdapterView<?> listview1, final int posicao) {
        final SqliteConfPagamentoBean parc = (SqliteConfPagamentoBean) listview1.getItemAtPosition(posicao);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção");
        builder.setCancelable(true);
        builder.setMessage("Deseja alterar ou excluir parcela?");
        builder.setPositiveButton("Alterar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        final String[] descricaoformpgto = {parc.getConf_descformpgto()};


                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.input_form_pgto, null);
                        AlertDialog.Builder alerta = new AlertDialog.Builder(ConfPagamento.this);
                        alerta.setCancelable(false);
                        alerta.setView(v);

                        TextView desctotalvend = (TextView) v.findViewById(R.id.txvdescparcela);
                        TextView numparcela = (TextView) v.findViewById(R.id.txvnumparcelaformpgto);
                        final Spinner formpgto = (Spinner) v.findViewById(R.id.spnformpgtopercela);
                        edtdiasvenc = (EditText) v.findViewById(R.id.edtdiasvencimento);
                        txvdatavenc = (TextView) v.findViewById(R.id.txvdatavencimento);
                        final EditText edtvlparcela = (EditText) v.findViewById(R.id.edtvalorparcela);

                        numparcela.setText(parc.getConf_parcelas().toString());
                        edtdiasvenc.setText(parc.getConf_diasvencimento());
                        txvdatavenc.setText(parc.getConf_datavencimento());
                        edtvlparcela.setText(parc.getConf_valor_recebido().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO FROM FORMAPAGAMENTO WHERE STATUS = 'A' AND CODPERFIL = " + idPerfil, null);
                        cursorformpgto.moveToFirst();
                        List<String> DadosList = new ArrayList<String>();
                        if (cursorformpgto.getCount() > 0) {
                            do {
                                DadosList.add(cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO")));
                            } while (cursorformpgto.moveToNext());
                        } else {
                            Toast.makeText(getBaseContext(), "Não existe forma de pagamento habilitada. Verifique!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cursorformpgto.close();
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ConfPagamento.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        formpgto.setAdapter(arrayAdapter);
                        int pos = arrayAdapter.getPosition(parc.getConf_descformpgto());
                        formpgto.setSelection(pos);

                        formpgto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                                TIPO_PAGAMENTO = spinner.getSelectedItem().toString();
                                SQLiteDatabase DB = new ConfigDB(ConfPagamento.this).getReadableDatabase();
                                Cursor cursorformpgto = DB.rawQuery("SELECT DESCRICAO, CODEXTERNO FROM FORMAPAGAMENTO WHERE DESCRICAO = '" + TIPO_PAGAMENTO + "' AND CODPERFIL = " + idPerfil, null);
                                cursorformpgto.moveToFirst();
                                if (cursorformpgto.getCount() > 0) {

                                    codformpgto = cursorformpgto.getInt(cursorformpgto.getColumnIndex("CODEXTERNO"));
                                    descformpgto = cursorformpgto.getString(cursorformpgto.getColumnIndex("DESCRICAO"));
                                }
                                cursorformpgto.close();

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        edtdiasvenc.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence char_digitado, int start, int before, int count) {
                                try {
                                    calcular_data_parcela(char_digitado);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        txvdatavenc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calcular_dias_parcela();
                            }
                        });

                        alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (AtuPedido) {
                                    if (!descricaoformpgto[0].equals(descformpgto)) {
                                        descricaoformpgto[0] = descformpgto;
                                    }
                                    String vlparcela = edtvlparcela.getText().toString();
                                    BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.'))).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    try {
                                        DB.execSQL("UPDATE CONFPAGAMENTO SET CONF_CODFORMPGTO_EXT = '" + codformpgto +
                                                "',CONF_DIAS_VENCIMENTO = '" + edtdiasvenc.getText().toString() +
                                                "',conf_descricao_formpgto = '" + descricaoformpgto[0] +
                                                "',conf_valor_recebido = '" + valorparcela +
                                                "',conf_parcelas = '" + parc.getConf_parcelas().toString() +
                                                "',vendac_chave = '" + ChavePedido +
                                                "',conf_tipo_pagamento = '" + TIPO_PAGAMENTO +
                                                "',conf_sementrada_comentrada = 'S'" +
                                                ",CONF_DATA_VENCIMENTO = '" + txvdatavenc.getText().toString() +
                                                "' where CONF_CODIGO = " + parc.getConf_codigo());
                                    } catch (Exception e) {
                                        e.toString();
                                    }
                                } else {
                                    if (!descricaoformpgto[0].equals(descformpgto)) {
                                        descricaoformpgto[0] = descformpgto;
                                    }
                                    String vlparcela = edtvlparcela.getText().toString();
                                    BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.'))).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    //vlparcela = valorparcela.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    try {
                                        DB.execSQL("UPDATE CONFPAGAMENTO SET CONF_CODFORMPGTO_EXT = '" + codformpgto +
                                                "',CONF_DIAS_VENCIMENTO = '" + edtdiasvenc.getText().toString() +
                                                "',conf_descricao_formpgto = '" + descricaoformpgto[0] +
                                                "',conf_valor_recebido = '" + valorparcela +
                                                "',conf_parcelas = '" + parc.getConf_parcelas().toString() +
                                                "',vendac_chave = ''" +
                                                ",conf_tipo_pagamento = '" + TIPO_PAGAMENTO +
                                                "',conf_sementrada_comentrada = 'S'" +
                                                ",CONF_DATA_VENCIMENTO = '" + txvdatavenc.getText().toString() +
                                                "' where CONF_CODIGO = " + parc.getConf_codigo());
                                    } catch (Exception e) {
                                        e.toString();
                                    }
                                }
                                atualizalistviewparcelas();

                            }
                        });

                        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        alerta.show();
                    }
                }
        );
        builder.setNegativeButton("Excluir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (AtuPedido) {
                            BigDecimal qtdparcela = new BigDecimal(conf_txtqtdparcelas.getText().toString());
                            qtdparcela = qtdparcela.subtract(BigDecimal.valueOf(1));
                            new SqliteConfPagamentoDao(getApplicationContext()).exluiparcela(parc.getConf_codigo());
                            if (qtdparcela.equals("0")) {
                                flag = 1;
                                conf_txtqtdparcelas.setText("");
                            } else {
                                flag = 1;
                                conf_txtqtdparcelas.setText(qtdparcela.toString());
                            }
                        } else {
                            BigDecimal qtdparcela = new BigDecimal(conf_txtqtdparcelas.getText().toString());
                            qtdparcela = qtdparcela.subtract(BigDecimal.valueOf(1));
                            new SqliteConfPagamentoDao(getApplicationContext()).exluiparcela(parc.getConf_codigo());
                            if (qtdparcela.equals("0")) {
                                flag = 1;
                                conf_txtqtdparcelas.setText("");
                            } else {
                                flag = 1;
                                conf_txtqtdparcelas.setText(qtdparcela.toString());
                            }
                        }
                        atualizalistviewparcelas();
                    }
                }
        );
        builder.create().show();
    }

    private void calcular_dias_parcela() {
        final SimpleDateFormat dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        final Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                txvdatavenc.setText(dateFormatterBR.format(newDate.getTime()));

                long diff = newDate.getTimeInMillis() - newCalendar.getTimeInMillis();
                long days = diff / (24 * 60 * 60 * 1000);

                edtdiasvenc.setText(String.valueOf(days));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePicker.setTitle("Data Prevista");
        datePicker.show();

    }

    private String calcular_data_parcela(CharSequence char_digitado) throws ParseException {
        if (char_digitado.toString().equals("") || char_digitado.toString().equals("0")) {
            txvdatavenc.setText(Util.DataHojeSemHorasBR().toString());
            return Util.DataHojeSemHorasBR().toString();
        }
        String date = Util.DataHojeSemHorasBR();
        SimpleDateFormat dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy");
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(dateFormatterBR.parse(date));
        newCalendar.add(Calendar.DATE, Integer.parseInt(String.valueOf(char_digitado)));
        date = dateFormatterBR.format(newCalendar.getTime());
        txvdatavenc.setText(date);

        return date;
    }

    private void declaraObjetosListeners() {
        confDao = new SqliteConfPagamentoDao(this);
        confBean = new SqliteConfPagamentoBean();
        DB = new ConfigDB(this).getReadableDatabase();
        txvValorRestante = (TextView) findViewById(R.id.txvvalortotalrestante);
        conf_txvvalorvenda = (TextView) findViewById(R.id.conf_txvvalorvenda);
        //conf_txvlabelparcelas = (TextView) findViewById(R.id.conf_txvlabelparcelas);
        conf_spfpgto = (Spinner) findViewById(R.id.conf_spfpgto);
        conf_spfpgto.setOnItemSelectedListener(this);
        conf_txtqtdparcelas = (EditText) findViewById(R.id.conf_txtqtdparcelas);
        btnincluirpagamento = (FloatingActionButton) findViewById(R.id.btnincluirpagamento);
        ListView_formapgto = (ListView) findViewById(R.id.lstparcelas);

        conf_txtqtdparcelas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence char_digitado, int start, int before, int count) {
                try {
                    if (flag == 0) {
                        calcular_valor_parcela(char_digitado, 0);
                    } else {
                        calcular_valor_parcela(char_digitado, 1);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!AtuPedido) {
            new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
            finish();
        } else {
            finish();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("tipopgto", conf_spfpgto.getSelectedItemPosition());
        try {
            outState.putString("qtdparcela", conf_txtqtdparcelas.getText().toString());
        } catch (Exception e) {
            e.toString();
        }
        try{
            outState.putBoolean("builderformpgto",builderformpgto.show().isShowing());
        }catch (Exception e ){
            e.toString();
        }
        try{
            outState.putBoolean("alertaformpgto",alertaformpgto.show().isShowing());
        }catch (Exception e){
            e.toString();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        conf_spfpgto.setSelection(savedInstanceState.getInt("tipopgto"));
        String qtdparc = savedInstanceState.getString("qtdparcela");
        if(!qtdparc.equals("")) {
            try {
                flag = 1;
                conf_txtqtdparcelas.setText(savedInstanceState.getString("qtdparcela"));
                atualizalistviewparcelas();
            } catch (Exception e) {
                e.toString();
            }
        }
        try {
            if (savedInstanceState.getBoolean("builderformpgto")){
                builderformpgto.show();
            }
        }catch (Exception e){
            e.toString();
        }
        try{
            if(savedInstanceState.getBoolean("alertaformpgto")){
                alertaformpgto.show();
            }
        }catch (Exception e){
            e.toString();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
