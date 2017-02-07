package com.jdsystem.br.vendasmobile.Model;

import java.math.BigDecimal;

/**
 * Created by JAVA on 28/08/2015.
 */
public class SqliteProdutoBean {

    //public static final String P_PRODUTO_SIMPLECURSOR = "CODITEMANUAL";
    public static final String P_PRODUTO_SIMPLECURSOR   = "_id";
    public static final String P_CODIGO_PRODUTO         = "CODITEMANUAL";
    public static final String P_CODIGO_BARRAS          = "CODITEMANUAL";
    public static final String P_DESCRICAO_PRODUTO      = "DESCRICAO";
    public static final String P_UNIDADE_MEDIDA         = "UNIVENDA";
    //public static final String P_CUSTO_PRODUTO        = "prd_custo";
    // public static final String P_QUANTIDADE_PRODUTO  = "prd_quantidade";
    public static final String P_PRECO_PROD_PADRAO      = "VLVENDA1";
    public static final String P_PRECO_PROD_VLVENDA1    = "VENDAPADRAO";
    public static final String P_PRECO_PROD_VLVENDA2    = "VLVENDA2";
    public static final String P_PRECO_PROD_VLVENDA3    = "VLVENDA3";
    public static final String P_PRECO_PROD_VLVENDA4    = "VLVENDA4";
    public static final String P_PRECO_PROD_VLVENDA5    = "VLVENDA5";
    public static final String P_PRECO_PROD_VLVENDAP1   = "VLVENDAP1";
    public static final String P_PRECO_PROD_VLVENDAP2   = "VLVENDAP2";
    public static final String P_CATEGORIA_PRODUTO      = "CLASSE";
    public static final String P_STATUS_PRODUTO         = "ATIVO";
    public static final String P_APRESENTACAO_PRODUTO   = "APRESENTACAO";

    private String prd_codigo;
    //private String prd_EAN13;
    private String prd_descricao;
    private String prd_unmedida;
    //private BigDecimal prd_custo;
    //private BigDecimal prd_quantidade;
    private BigDecimal prd_preco;
    private BigDecimal prd_preco_padrao;
    private String prd_categoria;
    private String prd_status;
    private String prd_apresentacao;

    public SqliteProdutoBean() {
    }

    public BigDecimal getPrd_preco_padrao() {
        return prd_preco_padrao;
    }

    public void setPrd_preco_padrao(BigDecimal prd_preco_padrao) {
        this.prd_preco_padrao = prd_preco_padrao;
    }

    public SqliteProdutoBean(String prd_codigo) {
        this.prd_codigo = prd_codigo;
    }

    public String getPrd_codigo() {
        return prd_codigo;
    }

    public void setPrd_codigo(String prd_codigo) {
        this.prd_codigo = prd_codigo;
    }


    public String getPrd_descricao() {
        return prd_descricao;
    }

    public void setPrd_descricao(String prd_descricao) {
        this.prd_descricao = prd_descricao;
    }

    public String getPrd_unmedida() {
        return prd_unmedida;
    }

    public void setPrd_unmedida(String prd_unmedida) {
        this.prd_unmedida = prd_unmedida;
    }

    public BigDecimal getPrd_preco() {
        return prd_preco;
    }

    public void setPrd_preco(BigDecimal prd_preco) {
        this.prd_preco = prd_preco;
    }

    public String getPrd_categoria() {
        return prd_categoria;
    }

    public void setPrd_categoria(String prd_categoria) {
        this.prd_categoria = prd_categoria;
    }

    public String getPrd_status () {
        return prd_status;
    }

    public void setPrd_status (String prd_status) {
        this.prd_status = prd_status;
    }

    public String getPrd_apresentacao () {
        return prd_apresentacao;
    }

    public void setPrd_apresentacao (String prd_apresentacao) {
        this.prd_apresentacao = prd_apresentacao;
    }
}
