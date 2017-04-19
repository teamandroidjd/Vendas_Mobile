package com.jdsystem.br.vendasmobile.Controller;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.view.MotionEvent;

import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.R.layout.simple_spinner_dropdown_item;


public class ConfPagamento extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, Spinner.OnItemSelectedListener {

    private SimpleDateFormat dateFormatterBR;
    private SimpleDateFormat dateFormatterUSA;
    private DatePickerDialog datePicker;
    private RadioGroup conf_rgPagamentos;
    private EditText conf_txtqtdparcelas, conf_txtvalorrecebido;
    private TextView conf_txvvalorvenda, conf_valorparcela, conf_valorparcela2, conf_valorparcela3,
            conf_valorparcela4, conf_valorparcela5, conf_valorparcela6, conf_valorparcela7, conf_valorparcela8, conf_valorparcela9, conf_valorparcela10,
            conf_valorparcela11, conf_valorparcela12, conf_txvlabelvalorrecebido, conf_txvlabelparcelas;
    private Spinner conf_spfpgto;
    private List<String> array_forma_pagamento = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String RECEBIMENTO_DIN_CAR_CHQ = "";
    private String COMENTRADA_SEMENTRADA = "";
    private String TIPO_PAGAMENTO = "";
    private String ChavePedido = "";
    private Double SUBTOTAL_VENDA;
    private BigDecimal VALORRECEBIDO;
    private Boolean AtuPedido;
    public SharedPreferences prefs;
    public static final String DADOS_PG = "DADOS DO PAGAMENTO";
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    private float mx, my;
    private float curX, curY;
    private String din_boleto, avista_parcelado, qtdparcelas;
    private Intent INTENT_SOBTOTAL_VENDA, INTENT_CLI_CODIGO;
    private Integer CLI_CODIGO;
    private RadioButton conf_rbdinheiro, conf_rbboleto;
    private SqliteConfPagamentoDao confDao;
    private SqliteConfPagamentoBean confBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conf_pagamento);

        declaraObjetosListeners();

        INTENT_SOBTOTAL_VENDA = getIntent();
        SUBTOTAL_VENDA = INTENT_SOBTOTAL_VENDA.getDoubleExtra("SUBTOTAL_VENDA", 0);
        INTENT_CLI_CODIGO = getIntent();
        CLI_CODIGO = INTENT_CLI_CODIGO.getIntExtra("CLI_CODIGO", 0);
        ChavePedido = INTENT_CLI_CODIGO.getStringExtra("ChavePedido");
        AtuPedido = INTENT_CLI_CODIGO.getBooleanExtra("AtuPedido", false);
        conf_txvvalorvenda.setText("Valor Venda: R$ " + new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));
        conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
        VALORRECEBIDO = new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, BigDecimal.ROUND_UP);
        array_forma_pagamento.add(getString(R.string.confpagamento_avista));
        array_forma_pagamento.add(getString(R.string.confpagamento_parcelado));
        arrayAdapter = new ArrayAdapter<String>(this, simple_spinner_dropdown_item, array_forma_pagamento);
        conf_spfpgto.setAdapter(arrayAdapter);

        obterConfiguracoesPagamento();
    }

    private void obterConfiguracoesPagamento() {
        int sppg = 0;
        if (AtuPedido) {
            confBean = new SqliteConfPagamentoDao(getApplicationContext()).busca_CONFPAGAMENTO_Pedido(ChavePedido);
            if (confBean != null) {

                qtdparcelas = confBean.getConf_parcelas().toString();

                avista_parcelado = confBean.getConf_tipo_pagamento().toString();
                if (avista_parcelado.equals(getString(R.string.confpagamento_avista))) {
                    sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_avista));
                    conf_spfpgto.setSelection(sppg);

                }
                if (avista_parcelado.equals(getString(R.string.confpagamento_parcelado))) {
                    sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_parcelado));
                    conf_spfpgto.setSelection(sppg);

                }

                din_boleto = confBean.getConf_recebeucom_din_chq_car().toString();
                if (din_boleto.equals(getString(R.string.confpagamento_dinheiro))) {
                    conf_rbdinheiro.setChecked(true);
                }
                if (din_boleto.equals(getString(R.string.confpagamento_boleto))) {
                    conf_rbboleto.setChecked(true);
                }
            }
        }
        if (!AtuPedido) {
            declaraPreferencias();
        }
    }

    private void declaraPreferencias() {
        int sppg = 0;
        confBean = new SqliteConfPagamentoDao(getApplicationContext()).busca_CONFPAGAMENTO_sem_chave();

        if (confBean == null) {
            prefs = getSharedPreferences(DADOS_PG, MODE_PRIVATE);
            avista_parcelado = prefs.getString("avista_parcelado", null);
            din_boleto = prefs.getString("din_boleto", null);
            if (avista_parcelado != null && avista_parcelado != "") {
                if (avista_parcelado.equals(getString(R.string.confpagamento_avista))) {
                    sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_avista));
                    conf_spfpgto.setSelection(sppg);

                }
                if (avista_parcelado.equals(getString(R.string.confpagamento_parcelado))) {
                    sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_parcelado));
                    conf_spfpgto.setSelection(sppg);

                }
            }
            if (din_boleto != null && din_boleto != "") {
                if (din_boleto.equals(getString(R.string.confpagamento_dinheiro))) {
                    conf_rbdinheiro.setChecked(true);
                }
                if (din_boleto.equals(getString(R.string.confpagamento_boleto))) {
                    conf_rbboleto.setChecked(true);
                }
            }
        }
        if (confBean != null) {
            qtdparcelas = confBean.getConf_parcelas().toString();

            avista_parcelado = confBean.getConf_tipo_pagamento().toString();
            if (avista_parcelado.equals(getString(R.string.confpagamento_avista))) {
                sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_avista));
                conf_spfpgto.setSelection(sppg);

            }
            if (avista_parcelado.equals(getString(R.string.confpagamento_parcelado))) {
                sppg = arrayAdapter.getPosition(getString(R.string.confpagamento_parcelado));
                conf_spfpgto.setSelection(sppg);

            }

        din_boleto = confBean.getConf_recebeucom_din_chq_car().toString();
        if (din_boleto.equals(getString(R.string.confpagamento_dinheiro))) {
            conf_rbdinheiro.setChecked(true);
        }
        if (din_boleto.equals(getString(R.string.confpagamento_boleto))) {
            conf_rbboleto.setChecked(true);
        }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                break;
        }

        return true;
    }


    public void salvar_fpgto(View v) {

        if (validar_forma_de_pagamento()) {

            if (ChavePedido == null) {
                new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
            } else {
                new SqliteConfPagamentoDao(this).excluir_FormaPgto_Chave(ChavePedido);
            }

            Util.log("TIPO_PAGAMENTO :" + TIPO_PAGAMENTO);
            Util.log("PARCELAS :" + conf_txtqtdparcelas.getText().toString());
            Util.log("COM_ENTRADA :" + COMENTRADA_SEMENTRADA);
            Util.log("VALOR_RECEBIDO :" + conf_txtvalorrecebido.getText().toString());
            Util.log("COMO_RECEBEU :" + RECEBIMENTO_DIN_CAR_CHQ);

            String rec = conf_txtvalorrecebido.getText().toString();

            new SqliteConfPagamentoDao(this).gravar_CONFPAGAMENTO(
                    new SqliteConfPagamentoBean(
                            COMENTRADA_SEMENTRADA,
                            TIPO_PAGAMENTO,
                            RECEBIMENTO_DIN_CAR_CHQ,
                            new BigDecimal(conf_txtvalorrecebido.getText().toString().trim()).setScale(2, RoundingMode.HALF_EVEN),
                            Integer.parseInt(conf_txtqtdparcelas.getText().toString()),
                            "",
                            "N"
                    ), AtuPedido, ChavePedido
            );

            SharedPreferences.Editor editor = getSharedPreferences(DADOS_PG, MODE_PRIVATE).edit();
            editor.putString("avista_parcelado", TIPO_PAGAMENTO);
            editor.putString("din_boleto", RECEBIMENTO_DIN_CAR_CHQ);
            editor.commit();

            finish();
        }
    }

    private boolean validar_forma_de_pagamento() {
        boolean fechar = true;
        if (TIPO_PAGAMENTO.equals(getString(R.string.confpagamento_parcelado))) {
            // condicao sem entrada
            if (conf_txtqtdparcelas.getText().toString().trim().equals("") || conf_txtqtdparcelas.getText().toString().trim().equals("0")) {
                fechar = false;
                Util.msg_toast_personal(getBaseContext(), getString(R.string.enter_quntity), Util.ALERTA);
            }
        }
        return fechar;
    }


    @Override
    public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {

        TIPO_PAGAMENTO = spinner.getItemAtPosition(position).toString();


            if (TIPO_PAGAMENTO.equals(getString(R.string.confpagamento_avista))) {

                conf_txvlabelparcelas.setVisibility(View.GONE);
                conf_txtqtdparcelas.setVisibility(View.GONE);
                conf_valorparcela.setVisibility(View.GONE);
                conf_valorparcela2.setVisibility(View.GONE);
                conf_valorparcela3.setVisibility(View.GONE);
                conf_valorparcela4.setVisibility(View.GONE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_rbdinheiro.setVisibility(View.VISIBLE);
                conf_rgPagamentos.setVisibility(View.VISIBLE);
                conf_txtqtdparcelas.setText("1");
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());

            }

            if (TIPO_PAGAMENTO.equals(getString(R.string.confpagamento_parcelado))) {

                conf_txvlabelparcelas.setVisibility(View.VISIBLE);
                conf_rbboleto.setChecked(true);
                conf_txtqtdparcelas.setVisibility(View.VISIBLE);
                conf_rbdinheiro.setVisibility(View.GONE);
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_txtqtdparcelas.setFocusable(true);
                conf_rgPagamentos.setVisibility(View.VISIBLE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
                if (confBean != null) {
                    conf_txtqtdparcelas.setText(qtdparcelas);
                } if (!AtuPedido && confBean == null) {
                    conf_txtqtdparcelas.setText("");
                }


            }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {

            case R.id.conf_rbdinheiro:
                RECEBIMENTO_DIN_CAR_CHQ = getString(R.string.confpagamento_dinheiro);
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
                //Util.msg_toast_personal(getBaseContext(), "dinheiro", Util.ALERTA);
                break;

            case R.id.conf_rbboleto:
                RECEBIMENTO_DIN_CAR_CHQ = getString(R.string.confpagamento_boleto);
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
                //Util.msg_toast_personal(getBaseContext(), "cartao", Util.ALERTA);
                break;
        }
    }

    public void calcular_valor_parcela(CharSequence valor_digitado) {
        String vl = valor_digitado.toString();
        if (!vl.equals("0") && !vl.equals("")) {
            String QUANTIDADE_PARCELAS = conf_txtqtdparcelas.getText().toString();
            if (Integer.parseInt(QUANTIDADE_PARCELAS) == 1) {
                conf_valorparcela2.setVisibility(View.GONE);
                conf_valorparcela3.setVisibility(View.GONE);
                conf_valorparcela4.setVisibility(View.GONE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal valor_parcela = new BigDecimal(SUBTOTAL_VENDA.toString());
                conf_valorparcela.setText("Parcela 1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','));

            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 2) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.GONE);
                conf_valorparcela4.setVisibility(View.GONE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_DOWN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 3) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.GONE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_DOWN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_DOWN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 4) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 5) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 6) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 7) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.   7/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 8) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.VISIBLE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.   7/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela8.setText("Parc.   8/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 9) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.VISIBLE);
                conf_valorparcela9.setVisibility(View.VISIBLE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.   7/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela8.setText("Parc.   8/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela9.setText("Parc.   9/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 10) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.VISIBLE);
                conf_valorparcela9.setVisibility(View.VISIBLE);
                conf_valorparcela10.setVisibility(View.VISIBLE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.   7/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela8.setText("Parc.   8/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela9.setText("Parc.   9/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela10.setText("Parc. 10/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 11) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.VISIBLE);
                conf_valorparcela9.setVisibility(View.VISIBLE);
                conf_valorparcela10.setVisibility(View.VISIBLE);
                conf_valorparcela11.setVisibility(View.VISIBLE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.  2/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.  3/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.  4/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.  5/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.  6/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.  7/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela8.setText("Parc.  8/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela9.setText("Parc.  9/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela10.setText("Parc. 10/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela11.setText("Parc. 11/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else if (Integer.parseInt(QUANTIDADE_PARCELAS) == 12) {
                conf_valorparcela.setVisibility(View.VISIBLE);
                conf_valorparcela2.setVisibility(View.VISIBLE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.VISIBLE);
                conf_valorparcela5.setVisibility(View.VISIBLE);
                conf_valorparcela6.setVisibility(View.VISIBLE);
                conf_valorparcela7.setVisibility(View.VISIBLE);
                conf_valorparcela8.setVisibility(View.VISIBLE);
                conf_valorparcela9.setVisibility(View.VISIBLE);
                conf_valorparcela10.setVisibility(View.VISIBLE);
                conf_valorparcela11.setVisibility(View.VISIBLE);
                conf_valorparcela12.setVisibility(View.VISIBLE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela2.setText("Parc.   2/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela3.setText("Parc.   3/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela4.setText("Parc.   4/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela5.setText("Parc.   5/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela6.setText("Parc.   6/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela7.setText("Parc.   7/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela8.setText("Parc.   8/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela9.setText("Parc.   9/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela10.setText("Parc. 10/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela11.setText("Parc. 11/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                conf_valorparcela12.setText("Parc. 12/" + QUANTIDADE_PARCELAS + ":  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
            } else {
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, BigDecimal.ROUND_DOWN);
                BigDecimal recalculo = valor_parcela.multiply(divisor);
                BigDecimal diferenca = valor_venda.subtract(recalculo);
                BigDecimal parc_1 = valor_parcela.add(diferenca);
                conf_valorparcela2.setVisibility(View.GONE);
                conf_valorparcela3.setVisibility(View.VISIBLE);
                conf_valorparcela4.setVisibility(View.GONE);
                conf_valorparcela5.setVisibility(View.GONE);
                conf_valorparcela6.setVisibility(View.GONE);
                conf_valorparcela7.setVisibility(View.GONE);
                conf_valorparcela8.setVisibility(View.GONE);
                conf_valorparcela9.setVisibility(View.GONE);
                conf_valorparcela10.setVisibility(View.GONE);
                conf_valorparcela11.setVisibility(View.GONE);
                conf_valorparcela12.setVisibility(View.GONE);
                conf_txtvalorrecebido.setVisibility(View.GONE);
                conf_txvlabelvalorrecebido.setVisibility(View.GONE);

                if (parc_1.equals(valor_parcela)) {
                    conf_valorparcela.setText("Valor das parcelas: R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                    conf_valorparcela3.setText("Quantidade de parcelas: " + QUANTIDADE_PARCELAS + " parcelas");
                } else {
                    conf_valorparcela.setText("Parc.   1/" + QUANTIDADE_PARCELAS + ": R$ " + new BigDecimal(parc_1.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                    conf_valorparcela3.setText("Demais parcelas:  R$ " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',') + " ");
                }
            }

        } else {
            conf_valorparcela2.setVisibility(View.GONE);
            conf_valorparcela3.setVisibility(View.GONE);
            conf_valorparcela4.setVisibility(View.GONE);
            conf_valorparcela5.setVisibility(View.GONE);
            conf_valorparcela6.setVisibility(View.GONE);
            conf_valorparcela7.setVisibility(View.GONE);
            conf_valorparcela8.setVisibility(View.GONE);
            conf_valorparcela9.setVisibility(View.GONE);
            conf_valorparcela10.setVisibility(View.GONE);
            conf_valorparcela11.setVisibility(View.GONE);
            conf_valorparcela12.setVisibility(View.GONE);
            conf_txtvalorrecebido.setVisibility(View.GONE);
            conf_txvlabelvalorrecebido.setVisibility(View.GONE);
            conf_valorparcela.setText("Valor parcela : 0,00");
        }

    }


    private void declaraObjetosListeners() {

        mostraCalendario();

        conf_txtvalorrecebido = (EditText) findViewById(R.id.conf_txtvalorrecebido);
        conf_txvvalorvenda = (TextView) findViewById(R.id.conf_txvvalorvenda);
        conf_txvlabelparcelas = (TextView) findViewById(R.id.conf_txvlabelparcelas);
        conf_txvlabelvalorrecebido = (TextView) findViewById(R.id.conf_txvlabelvalorrecebido);
        conf_valorparcela = (TextView) findViewById(R.id.conf_valorparcela);
        conf_valorparcela2 = (TextView) findViewById(R.id.conf_valorparcela2);
        conf_valorparcela3 = (TextView) findViewById(R.id.conf_valorparcela3);
        conf_valorparcela4 = (TextView) findViewById(R.id.conf_valorparcela4);
        conf_valorparcela5 = (TextView) findViewById(R.id.conf_valorparcela5);
        conf_valorparcela6 = (TextView) findViewById(R.id.conf_valorparcela6);
        conf_valorparcela7 = (TextView) findViewById(R.id.conf_valorparcela7);
        conf_valorparcela8 = (TextView) findViewById(R.id.conf_valorparcela8);
        conf_valorparcela9 = (TextView) findViewById(R.id.conf_valorparcela9);
        conf_valorparcela10 = (TextView) findViewById(R.id.conf_valorparcela10);
        conf_valorparcela11 = (TextView) findViewById(R.id.conf_valorparcela11);
        conf_valorparcela12 = (TextView) findViewById(R.id.conf_valorparcela12);
        conf_spfpgto = (Spinner) findViewById(R.id.conf_spfpgto);
        conf_rgPagamentos = (RadioGroup) findViewById(R.id.conf_rgPagamentos);
        conf_txtqtdparcelas = (EditText) findViewById(R.id.conf_txtqtdparcelas);

        vScroll = (ScrollView) findViewById(R.id.scrollView);
        hScroll = (HorizontalScrollView) findViewById(R.id.scrollViewh);

        conf_spfpgto.setOnItemSelectedListener(this);
        conf_rgPagamentos.setOnCheckedChangeListener(this);

        conf_rbdinheiro = (RadioButton) findViewById(R.id.conf_rbdinheiro);
        conf_rbboleto = (RadioButton) findViewById(R.id.conf_rbboleto);


        conf_txtqtdparcelas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence char_digitado, int start, int before, int count) {
                calcular_valor_parcela(char_digitado);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void mostraCalendario() {
        dateFormatterBR = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormatterUSA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();
    }
}
