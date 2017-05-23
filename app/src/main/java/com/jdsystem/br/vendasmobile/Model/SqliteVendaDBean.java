package com.jdsystem.br.vendasmobile.Model;

import java.math.BigDecimal;

/**
 * Created by JAVA on 09/09/2015.
 */
public class SqliteVendaDBean {

    public static final String CHAVE_DA_VENDA = "CHAVEPEDIDO";
    public static final String NUMPED = "NUMPED";
    public static final String NUMERO_ITEM = "NUMEROITEM";
    public static final String PRECOPRODUTO_TEMP = "VLUNITTEMP";
    public static final String CODPRODUTO = "CODITEMANUAL";
    public static final String CODPROD_INT = "CODIGOITEM";
    public static final String DESCRICAOPROD = "DESCRICAO";
    public static final String UNIDADEPROD = "UNIDADE";
    public static final String QUANTVENDIDA = "QTDMENORPED";
    public static final String PRECOPRODUTO = "VLUNIT";
    public static final String TOTALPRODUTO = "VLTOTAL";
    public static final String VLMERCAD = "VLMERCAD";
    public static final String PRODVIEW = "VIEW";
    public static final String QUANTVENDIDA_TEMP = "QTDMAIORPEDTEMP";

    private String vendac_chave;
    private Integer vendad_nro_item;
    private String vendad_ean;
    private String vendad_prd_codigo;
    private int vendad_prd_codigo_interno;
    private String vendad_prd_descricao;
    private String vendad_prd_unidade;
    private Integer vendad_prd_codigoitem;
    private Integer numped;
    private String vendad_prd_view;
    private String vendac_prd_numped;
    private BigDecimal vendad_quantidade;
    private BigDecimal vendad_quantidade_temp;
    private BigDecimal vendad_preco_venda;
    private BigDecimal vendad_preco_venda_temp;
    private BigDecimal vendad_total;
    private BigDecimal vendad_tmercad;

    public String getVendad_prd_unidade() {
        return vendad_prd_unidade;
    }

    public void setVendad_prd_unidade(String vendad_prd_unidade) {
        this.vendad_prd_unidade = vendad_prd_unidade;
    }

    public String getVendac_prd_numped() {
        return vendac_prd_numped;
    }

    public void setVendac_prd_numped(String vendac_prd_numped) {
        this.vendac_prd_numped = vendac_prd_numped;
    }

    public void setvendad_quantidade_temp(BigDecimal qtd_temp) {
        this.vendad_quantidade_temp = qtd_temp;

    }

    public BigDecimal getvendad_quantidade_temp() {
        return vendad_quantidade_temp;
    }

    public void setvendad_preco_venda_temp(BigDecimal preco_temp) {
        this.vendad_preco_venda_temp = preco_temp;
    }

    public BigDecimal getvendad_preco_venda_temp() {
        return vendad_preco_venda_temp;
    }

    public BigDecimal getVendad_tmercad() {
        return vendad_tmercad;
    }

    public void setVendad_tmercad(BigDecimal vendad_tmercad) {
        this.vendad_tmercad = vendad_tmercad;
    }

    public int getVendac_numped() {
        return numped;
    }

    public void setVendac_numped(int numped) {

        this.numped = numped;
    }

    public String getVendac_chave() {
        return vendac_chave;
    }

    public void setVendac_chave(String vendac_chave) {

        this.vendac_chave = vendac_chave;
    }

    public Integer getVendad_nro_item() {
        return vendad_nro_item;
    }

    public void setVendad_nro_item(Integer vendad_nro_item) {
        this.vendad_nro_item = vendad_nro_item;
    }

    public String getVendad_ean() {
        return vendad_ean;
    }

    public void setVendad_ean(String vendad_ean) {
        this.vendad_ean = vendad_ean;
    }

    public String getVendad_prd_codigo() {
        return vendad_prd_codigo;
    }

    public void setVendad_prd_codigo(String vendad_prd_codigo) {
        this.vendad_prd_codigo = vendad_prd_codigo;
    }

    public int getVendad_prd_codigoiteminterno() {
        return vendad_prd_codigo_interno;
    }

    public void setVendad_prd_codigoiteminterno(int vendad_prd_codigointerno) {
        this.vendad_prd_codigo_interno = vendad_prd_codigointerno;
    }

    public String getVendad_prd_descricao() {
        return vendad_prd_descricao;
    }

    public void setVendad_prd_descricao(String vendad_prd_descricao) {
        this.vendad_prd_descricao = vendad_prd_descricao;
    }

    public BigDecimal getVendad_quantidade() {
        return vendad_quantidade;
    }

    public void setVendad_quantidade(BigDecimal vendad_quantidade) {
        this.vendad_quantidade = vendad_quantidade;
    }

    public BigDecimal getVendad_preco_venda() {
        return vendad_preco_venda;
    }

    public void setVendad_preco_venda(BigDecimal vendad_preco_venda) {
        this.vendad_preco_venda = vendad_preco_venda;
    }

    public BigDecimal getVendad_total() {
        return vendad_total;
    }

    public void setVendad_total(BigDecimal vendad_total) {
        this.vendad_total = vendad_total;
    }

    public BigDecimal getSubTotal() {
        return this.vendad_quantidade.multiply(this.vendad_preco_venda);
    }

    public Integer getVendad_prd_codigoitem() {
        return vendad_prd_codigoitem;
    }

    public void setVendad_prd_codigoitem(Integer vendad_prd_codigoitem) {
        this.vendad_prd_codigoitem = vendad_prd_codigoitem;
    }

    public String getvendad_prd_view() {
        return vendad_prd_view;
    }

    public void setvendad_prd_view(String vendad_prd_view) {
        this.vendad_prd_view = vendad_prd_view;
    }
}
