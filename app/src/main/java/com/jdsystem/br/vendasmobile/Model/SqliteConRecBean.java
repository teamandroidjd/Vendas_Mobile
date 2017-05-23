package com.jdsystem.br.vendasmobile.Model;

import java.math.BigDecimal;


public class SqliteConRecBean {

    public static final String ID_SIMPLECURSORADAPTER = "_id";
    public static final String CODIGO_RECEBER = "rec_codigo";
    public static final String NUMERO_DA_PARCELA = "rec_numparcela";
    public static final String CODIGO_DO_CLIENTE = "rec_cli_codigo";
    public static final String NOME_DO_CLIENTE = "rec_cli_nome";
    public static final String CHAVE_DA_VENDA = "vendac_chave";
    public static final String DATA_DO_MOVIMENTO = "rec_datamovimento";
    public static final String VALOR_A_RECEBER = "rec_valor_receber";
    public static final String VALOR_PAGO = "rec_valorpago";
    public static final String DATA_DO_VENCIMENTO = "rec_datavencimento";
    public static final String DATA_QUE_PAGOU = "rec_data_que_pagou";
    public static final String FORMATO_RECEBIMENTO = "rec_recebeu_com";
    public static final String REC_ENVIADO = "rec_enviado";

    private Integer rec_codigo;
    private Integer rec_numparcela;
    private Integer rec_cli_codigo;
    private String rec_cli_nome;
    private String vendac_chave;
    private String rec_datamovimento;
    private BigDecimal rec_valor_receber;
    private BigDecimal rec_valorpago;
    private String rec_datavencimento;
    private String rec_data_que_pagou;
    private String rec_recebeu_com;
    private String rec_enviado;
    private boolean selecionado = false;

    public SqliteConRecBean() {
    }

    public SqliteConRecBean(BigDecimal rec_valorpago, String rec_data_que_pagou, String rec_recebeu_com, String rec_enviado, String vendac_chave, Integer rec_numparcela) {
        this.rec_valorpago = rec_valorpago;
        this.rec_data_que_pagou = rec_data_que_pagou;
        this.rec_recebeu_com = rec_recebeu_com;
        this.rec_enviado = rec_enviado;
        this.vendac_chave = vendac_chave;
        this.rec_numparcela = rec_numparcela;
    }

    public SqliteConRecBean(Integer rec_numparcela, Integer rec_cli_codigo, String rec_cli_nome, String vendac_chave, String rec_datamovimento, BigDecimal rec_valor_receber, BigDecimal rec_valorpago, String rec_datavencimento, String rec_data_que_pagou, String rec_recebeu_com, String rec_enviado) {
        this.rec_numparcela = rec_numparcela;
        this.rec_cli_codigo = rec_cli_codigo;
        this.rec_cli_nome = rec_cli_nome;
        this.vendac_chave = vendac_chave;
        this.rec_datamovimento = rec_datamovimento;
        this.rec_valor_receber = rec_valor_receber;
        this.rec_valorpago = rec_valorpago;
        this.rec_datavencimento = rec_datavencimento;
        this.rec_data_que_pagou = rec_data_que_pagou;
        this.rec_recebeu_com = rec_recebeu_com;
        this.rec_enviado = rec_enviado;
    }


    public Integer getRec_codigo() {
        return rec_codigo;
    }

    public void setRec_codigo(Integer rec_codigo) {
        this.rec_codigo = rec_codigo;
    }

    public Integer getRec_numparcela() {
        return rec_numparcela;
    }

    public void setRec_numparcela(Integer rec_numparcela) {
        this.rec_numparcela = rec_numparcela;
    }

    public Integer getRec_cli_codigo() {
        return rec_cli_codigo;
    }

    public void setRec_cli_codigo(Integer rec_cli_codigo) {
        this.rec_cli_codigo = rec_cli_codigo;
    }

    public String getRec_cli_nome() {
        return rec_cli_nome;
    }

    public void setRec_cli_nome(String rec_cli_nome) {
        this.rec_cli_nome = rec_cli_nome;
    }

    public String getVendac_chave() {
        return vendac_chave;
    }

    public void setVendac_chave(String vendac_chave) {
        this.vendac_chave = vendac_chave;
    }

    public String getRec_datamovimento() {
        return rec_datamovimento;
    }

    public void setRec_datamovimento(String rec_datamovimento) {
        this.rec_datamovimento = rec_datamovimento;
    }

    public BigDecimal getRec_valor_receber() {
        return rec_valor_receber;
    }

    public void setRec_valor_receber(BigDecimal rec_valor_receber) {
        this.rec_valor_receber = rec_valor_receber;
    }

    public BigDecimal getRec_valorpago() {
        return rec_valorpago;
    }

    public void setRec_valorpago(BigDecimal rec_valorpago) {
        this.rec_valorpago = rec_valorpago;
    }

    public String getRec_datavencimento() {
        return rec_datavencimento;
    }

    public void setRec_datavencimento(String rec_datavencimento) {
        this.rec_datavencimento = rec_datavencimento;
    }

    public String getRec_data_que_pagou() {
        return rec_data_que_pagou;
    }

    public void setRec_data_que_pagou(String rec_data_que_pagou) {
        this.rec_data_que_pagou = rec_data_que_pagou;
    }

    public String getRec_recebeu_com() {
        return rec_recebeu_com;
    }

    public void setRec_recebeu_com(String rec_recebeu_com) {
        this.rec_recebeu_com = rec_recebeu_com;
    }

    public String getRec_enviado() {
        return rec_enviado;
    }

    public void setRec_enviado(String rec_enviado) {
        this.rec_enviado = rec_enviado;
    }

    public boolean isSelecionado() {
        return selecionado;
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }
}
