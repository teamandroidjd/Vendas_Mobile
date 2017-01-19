package com.jdsystem.br.vendasmobile.Controller;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ConfPagamento extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, Spinner.OnItemSelectedListener {

    private SimpleDateFormat dateFormatterBR;
    private SimpleDateFormat dateFormatterUSA;
    private DatePickerDialog datePicker;
    private RadioGroup conf_rgPagamentos;
    private EditText conf_txtqtdparcelas, conf_txtvalorrecebido;
    private TextView conf_txvvalorvenda, conf_valorparcela, conf_txvlabelvalorrecebido, conf_txvlabelparcelas;
    private Spinner conf_spfpgto;
    private List<String> array_forma_pagamento = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private String RECEBIMENTO_DIN_CAR_CHQ = "";
    private String COMENTRADA_SEMENTRADA = "";
    private String TIPO_PAGAMENTO = "";
    private Double SUBTOTAL_VENDA;
    private BigDecimal VALORRECEBIDO;

    private Intent INTENT_SOBTOTAL_VENDA, INTENT_CLI_CODIGO;
    private Integer CLI_CODIGO;
    private RadioButton conf_rbdinheiro, conf_rbboleto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conf_pagamento);

        declaraObjetosListeners();

        new SqliteConfPagamentoDao(this).excluir_CONFPAGAMENTO();

        INTENT_SOBTOTAL_VENDA = getIntent();
        SUBTOTAL_VENDA = INTENT_SOBTOTAL_VENDA.getDoubleExtra("SUBTOTAL_VENDA", 0);
        INTENT_CLI_CODIGO = getIntent();
        CLI_CODIGO = INTENT_CLI_CODIGO.getIntExtra("CLI_CODIGO", 0);

        conf_txvvalorvenda.setText("Valor Venda : " + new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.',','));
        conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
        VALORRECEBIDO = new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, BigDecimal.ROUND_UP);
        array_forma_pagamento.add("AVISTA");
        array_forma_pagamento.add("MENSAL");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_forma_pagamento);
        conf_spfpgto.setAdapter(arrayAdapter);
    }

    public void salvar_fpgto(View v) {


        if (validar_forma_de_pagamento()) {

            Util.log("TIPO_PAGAMENTO :" + TIPO_PAGAMENTO);
            Util.log("PARCELAS :" + conf_txtqtdparcelas.getText().toString());
            Util.log("COM_ENTRADA :" + COMENTRADA_SEMENTRADA);
            Util.log("VALOR_RECEBIDO :" + conf_txtvalorrecebido.getText().toString());
            Util.log("COMO_RECEBEU :" + RECEBIMENTO_DIN_CAR_CHQ);

            new SqliteConfPagamentoDao(this).gravar_CONFPAGAMENTO(
                    new SqliteConfPagamentoBean(
                            COMENTRADA_SEMENTRADA,
                            TIPO_PAGAMENTO,
                            RECEBIMENTO_DIN_CAR_CHQ,
                            new BigDecimal(conf_txtvalorrecebido.getText().toString().trim()).setScale(2, RoundingMode.HALF_EVEN),
                            Integer.parseInt(conf_txtqtdparcelas.getText().toString()),
                            "",
                            "N"
                    )
            );
            //Util.msg_toast_personal(getBaseContext(), "SALVO COM SUCESSO", Util.SUCESSO);
            finish();
        }


    }


    private boolean validar_forma_de_pagamento() {

        boolean fechar = true;
/*
        // venda avista
        if (TIPO_PAGAMENTO.equals("AVISTA")) {
            if (conf_txtvalorrecebido.getText().toString().trim().length() > 0) {
                BigDecimal valor_recebido = new BigDecimal(conf_txtvalorrecebido.getText().toString());
                if (RECEBIMENTO_DIN_CAR_CHQ.equals("DINHEIRO") && valor_recebido.doubleValue() > SUBTOTAL_VENDA) {
                    fechar = false;
                    Util.msg_toast_personal(getBaseContext(), "Valor avista maior que valor da venda", Util.ALERTA);
                } else if (RECEBIMENTO_DIN_CAR_CHQ.equals("DINHEIRO") && valor_recebido.doubleValue() < SUBTOTAL_VENDA) {
                    fechar = false;
                    Util.msg_toast_personal(getBaseContext(), "Valor avista menor que valor da venda", Util.ALERTA);
                }
            } else {
                Util.msg_toast_personal(getBaseContext(), "informe o valor recebido", Util.ALERTA);
            }
        }
*/

        if (TIPO_PAGAMENTO.equals("MENSAL")) {

            // condicao sem entrada
            if (conf_txtqtdparcelas.getText().toString().trim().length() <= 0) {
                fechar = false;
                Util.msg_toast_personal(getBaseContext(), "informe a quantidade de parcelas", Util.ALERTA);
            }

        }

        return fechar;
    }


    @Override
    public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {

        TIPO_PAGAMENTO = spinner.getItemAtPosition(position).toString();

        if (TIPO_PAGAMENTO.equals("AVISTA")) {

            conf_txvlabelparcelas.setVisibility(View.GONE);
            conf_txtqtdparcelas.setVisibility(View.GONE);
            conf_valorparcela.setVisibility(View.GONE);
            conf_rbdinheiro.setVisibility(View.VISIBLE);
            conf_rgPagamentos.setVisibility(View.VISIBLE);
            conf_txvlabelvalorrecebido.setVisibility(View.GONE);
            conf_txtvalorrecebido.setVisibility(View.GONE);
            conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
        }

        if (TIPO_PAGAMENTO.equals("MENSAL")) {

            conf_txvlabelparcelas.setVisibility(View.VISIBLE);
            conf_rbboleto.setChecked(true);
            conf_txtqtdparcelas.setVisibility(View.VISIBLE);
            conf_rbdinheiro.setVisibility(View.GONE);
            conf_valorparcela.setVisibility(View.VISIBLE);
            conf_txtqtdparcelas.setText("1");
            conf_rgPagamentos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {

            case R.id.conf_rbdinheiro:
                RECEBIMENTO_DIN_CAR_CHQ = "DINHEIRO";
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
                //Util.msg_toast_personal(getBaseContext(), "dinheiro", Util.ALERTA);
                break;

            case R.id.conf_rbboleto:
                RECEBIMENTO_DIN_CAR_CHQ = "BOLETO";
                conf_txtvalorrecebido.setText(new BigDecimal(SUBTOTAL_VENDA.toString()).setScale(2, RoundingMode.HALF_EVEN).toString());
                //Util.msg_toast_personal(getBaseContext(), "cartao", Util.ALERTA);
                break;
        }
    }

    public void calcular_valor_parcela(CharSequence valor_digitado) {
        if (valor_digitado.length() > 0) {
            String QUANTIDADE_PARCELAS = conf_txtqtdparcelas.getText().toString();
            if (Integer.parseInt(QUANTIDADE_PARCELAS) > 0) {
                BigDecimal divisor = new BigDecimal(Integer.parseInt(QUANTIDADE_PARCELAS));
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, RoundingMode.HALF_UP);
                conf_valorparcela.setText("Valor parcela : " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.',','));
            } else {
                BigDecimal divisor = new BigDecimal("1");
                BigDecimal valor_venda = new BigDecimal(SUBTOTAL_VENDA.toString());
                BigDecimal valor_parcela = valor_venda.divide(divisor, 2, RoundingMode.HALF_UP);
                conf_valorparcela.setText("Valor parcela : " + new BigDecimal(valor_parcela.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.',','));
            }

        } else {
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
        conf_spfpgto = (Spinner) findViewById(R.id.conf_spfpgto);
        conf_rgPagamentos = (RadioGroup) findViewById(R.id.conf_rgPagamentos);
        conf_txtqtdparcelas = (EditText) findViewById(R.id.conf_txtqtdparcelas);

        conf_spfpgto.setOnItemSelectedListener(this);
        conf_rgPagamentos.setOnCheckedChangeListener(this);

        conf_rbdinheiro = (RadioButton) findViewById(R.id.conf_rbdinheiro);
        conf_rbboleto = (RadioButton) findViewById(R.id.conf_rbboleto);

        Integer opcao_cartao_dinheiro_cheque = conf_rgPagamentos.getCheckedRadioButtonId();
        if (opcao_cartao_dinheiro_cheque == conf_rbdinheiro.getId()) {
            RECEBIMENTO_DIN_CAR_CHQ = "DINHEIRO";
            conf_txtqtdparcelas.setText("0");
        }

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
