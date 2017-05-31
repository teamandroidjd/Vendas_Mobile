package com.jdsystem.br.vendasmobile.Model;


import java.math.BigDecimal;

public class SqliteConfPagamentoBean {

    public static final String CONF_CODIGO_CONFPAGAMENTO = "CONF_CODIGO";
    public static final String CONF_SEMENTADA_COMENTRADA = "conf_sementrada_comentrada";
    public static final String CONF_TIPO_DO_PAGAMENTO = "conf_tipo_pagamento";
    public static final String CONF_DINHEIRO_CARTAO_CHEQUE = "conf_recebeucom_din_chq_car";
    public static final String CONF_VALOR_RECEBIDO = "conf_valor_recebido";
    public static final String CONF_QUANTIDADE_PARCELAS = "conf_parcelas";
    public static final String CONF_VENDAC_CHAVE = "vendac_chave";
    public static final String CONF_ENVIADO = "conf_enviado";
    public static final String CONF_CODFORMPGTO = "CONF_CODFORMPGTO_EXT";
    public static final String CONF_DIASVENCIMENTO = "CONF_DIAS_VENCIMENTO";
    public static final String CONF_DATAVENCIMENTO = "CONF_DATA_VENCIMENTO";
    public static final String CONF_DESCFORMPGTO = "conf_descricao_formpgto";



    private Integer conf_codigo;
    private String conf_sementrada_comentrada;
    private String conf_tipo_pagamento;
    private String conf_recebeucom_din_chq_car;
    private BigDecimal conf_valor_recebido;
    private Integer conf_parcelas;
    private String vendac_chave;
    private String conf_enviado;
    private Boolean AtuPedido;
    private String conf_temp;
    private String conf_codformpgto;
    private String conf_diasvencimento;
    private String conf_datavencimento;
    private String conf_descformpgto;


    public SqliteConfPagamentoBean() {
    }


    public SqliteConfPagamentoBean(String conf_sementrada_comentrada, String conf_tipo_pagamento, String conf_recebeucom_din_chq_car,
                                   BigDecimal conf_valor_recebido, Integer conf_parcelas, String vendac_chave, String conf_enviado,
                                   String conf_codformpgto_ext, String conf_diasvenc, String conf_descformpgtoext,String conf_dtvencimento) {
        this.conf_sementrada_comentrada = conf_sementrada_comentrada;
        this.conf_tipo_pagamento = conf_tipo_pagamento;
        this.conf_recebeucom_din_chq_car = conf_recebeucom_din_chq_car;
        this.conf_valor_recebido = conf_valor_recebido;
        this.conf_parcelas = conf_parcelas;
        this.vendac_chave = vendac_chave;
        this.conf_enviado = conf_enviado;
        this.conf_codformpgto = conf_codformpgto_ext;
        this.conf_diasvencimento = conf_diasvenc;
        this.conf_descformpgto = conf_descformpgtoext;
        this.conf_datavencimento = conf_dtvencimento;

    }

    public boolean isComEntrada() {
        return this.getConf_sementrada_comentrada().equals("S") ? true : false;
    }

    public boolean isAvista() {
        return this.getConf_tipo_pagamento().equals("À VISTA") ? true : false;
    }

    public boolean isMensal() {
        return this.getConf_tipo_pagamento().equals("PARCELADO") ? true : false;
    }

    public boolean isSemanal() {
        return this.getConf_tipo_pagamento().equals("SEMANAL") ? true : false;
    }

    public boolean isQuinzenal() {
        return this.getConf_tipo_pagamento().equals("QUINZENAL") ? true : false;
    }

    public boolean isCheque() {
        return this.getConf_recebeucom_din_chq_car().equals("CHEQUE") ? true : false;
    }

    public Integer getConf_codigo() {
        return conf_codigo;
    }

    public void setConf_codigo(Integer conf_codigo) {
        this.conf_codigo = conf_codigo;
    }

    public String getConf_sementrada_comentrada() {
        return conf_sementrada_comentrada;
    }

    public void setConf_sementrada_comentrada(String conf_sementrada_comentrada) {
        this.conf_sementrada_comentrada = conf_sementrada_comentrada;
    }

    public String getConf_tipo_pagamento() {
        return conf_tipo_pagamento;
    }

    public void setConf_tipo_pagamento(String conf_tipo_pagamento) {
        this.conf_tipo_pagamento = conf_tipo_pagamento;
    }

    public String getConf_recebeucom_din_chq_car() {
        return conf_recebeucom_din_chq_car;
    }

    public void setConf_recebeucom_din_chq_car(String conf_recebeucom_din_chq_car) {
        this.conf_recebeucom_din_chq_car = conf_recebeucom_din_chq_car;
    }

    public BigDecimal getConf_valor_recebido() {
        return conf_valor_recebido;
    }

    public void setConf_valor_recebido(BigDecimal conf_valor_recebido) {
        this.conf_valor_recebido = conf_valor_recebido;
    }

    public Boolean getAtuPedido() {
        return AtuPedido;
    }

    public void setAtuPedido(Boolean atuPedido) {
        AtuPedido = atuPedido;
    }

    public Integer getConf_parcelas() {
        return conf_parcelas;
    }

    public void setConf_parcelas(Integer conf_parcelas) {
        this.conf_parcelas = conf_parcelas;
    }

    public String getVendac_chave() {
        return vendac_chave;
    }

    public void setVendac_chave(String vendac_chave) {
        this.vendac_chave = vendac_chave;
    }

    public String getConf_enviado() {
        return conf_enviado;
    }

    public void setConf_enviado(String conf_enviado) {
        this.conf_enviado = conf_enviado;
    }

    public String getConf_temp() {
        return conf_temp;
    }

    public void setConf_temp(String confTemp) {
        this.conf_temp = confTemp;
    }

    public String getConf_codformpgto() {
        return conf_codformpgto;
    }

    public void setConf_codformpgto(String confcodformpgto) {
        this.conf_codformpgto = confcodformpgto;
    }
    public String getConf_diasvencimento() {
        return conf_diasvencimento;
    }

    public void setConf_diasvencimento(String confdiasvencimento) {
        this.conf_diasvencimento = confdiasvencimento;
    }

    public String getConf_descformpgto() {
        return conf_descformpgto;
    }

    public void setConf_descformpgto(String conf_descformpgto) {
        this.conf_descformpgto = conf_descformpgto;
    }

    public String getConf_datavencimento() {
        return conf_datavencimento;
    }

    public void setConf_datavencimento(String conf_dtvenc) {
        this.conf_datavencimento = conf_dtvenc;
    }
}
