package com.jdsystem.br.vendasmobile.Model;

import java.math.BigDecimal;

/**
 * Created by JAVA on 07/09/2015.
 */
public class SqliteVendaD_TempBean {


    //public static final String TEMP_EAN = "vendad_eanTEMP";
    public static final String TEMP_CODPRODUTO = "vendad_prd_codigoTEMP";
    public static final String TEMP_CODPRODUTOINTERNO = "vendad_prd_codigo_internoTEMP";
    public static final String TEMP_DESCRICAOPROD = "vendad_prd_descricaoTEMP";
    public static final String TEMP_QUANTVENDIDA = "vendad_quantidadeTEMP";
    public static final String TEMP_PRECOPRODUTO = "vendad_preco_vendaTEMP";
    public static final String TEMP_UNIDADEPRODUTO = "vendad_prd_unidadeTEMP";
    public static final String TEMP_TOTALPRODUTO = "vendad_totalTEMP";


    private String vendad_eanTEMP;
    private String vendad_prd_codigoTEMP;
    private int vendad_prd_codigoitemTEMP;
    private String vendad_prd_descricaoTEMP;
    private String vendad_prd_unidadeTEMP;
    private BigDecimal vendad_quantidadeTEMP;
    private BigDecimal vendad_preco_vendaTEMP;
    private BigDecimal vendad_totalTEMP;
    private String vendad_prd_view;

    public SqliteVendaD_TempBean() {
    }

    public SqliteVendaD_TempBean(String vendad_eanTEMP, String vendad_prd_codigoTEMP, String vendad_prd_descricaoTEMP, BigDecimal vendad_quantidadeTEMP, BigDecimal vendad_preco_vendaTEMP, BigDecimal vendad_totalTEMP, String vendad_prd_unidadeTEMP,String vendad_prd_view) {
        this.vendad_eanTEMP = vendad_eanTEMP;
        this.vendad_prd_codigoTEMP = vendad_prd_codigoTEMP;
        this.vendad_prd_descricaoTEMP = vendad_prd_descricaoTEMP;
        this.vendad_quantidadeTEMP = vendad_quantidadeTEMP;
        this.vendad_preco_vendaTEMP = vendad_preco_vendaTEMP.setScale(4,BigDecimal.ROUND_HALF_UP);
        this.vendad_totalTEMP = vendad_totalTEMP.setScale(4,BigDecimal.ROUND_HALF_UP);
        this.vendad_prd_unidadeTEMP = vendad_prd_unidadeTEMP;
        this.vendad_prd_view = vendad_prd_view;


    }

    public String getVendad_prd_unidadeTEMP() {
        return vendad_prd_unidadeTEMP;
    }

    public void setVendad_prd_unidadeTEMP(String vendad_prd_unidadeTEMP) {
        this.vendad_prd_unidadeTEMP = vendad_prd_unidadeTEMP;
    }

    public String getVendad_eanTEMP() {
        return vendad_eanTEMP;
    }

    public void setVendad_eanTEMP(String vendad_eanTEMP) {

        this.vendad_eanTEMP = vendad_eanTEMP;
    }

    public String getVendad_prd_codigoTEMP() {
        return vendad_prd_codigoTEMP;
    }

    public void setVendad_prd_codigoTEMP(String vendad_prd_codigoTEMP) {
        this.vendad_prd_codigoTEMP = vendad_prd_codigoTEMP;
    }
    public int getVendad_prd_codigoItemTEMP(){
        return vendad_prd_codigoitemTEMP;
    }
    public void setVendad_prd_codigoItemTEMP(int vendad_prd_codigoitemTEMP) {
        this.vendad_prd_codigoitemTEMP = vendad_prd_codigoitemTEMP;
    }

    public String getVendad_prd_descricaoTEMP() {
        return vendad_prd_descricaoTEMP;
    }

    public void setVendad_prd_descricaoTEMP(String vendad_prd_descricaoTEMP) {
        this.vendad_prd_descricaoTEMP = vendad_prd_descricaoTEMP;
    }

    public BigDecimal getVendad_quantidadeTEMP() {

        return vendad_quantidadeTEMP;
    }

    public void setVendad_quantidadeTEMP(BigDecimal vendad_quantidadeTEMP) {
        this.vendad_quantidadeTEMP = vendad_quantidadeTEMP;
    }

    public BigDecimal getVendad_preco_vendaTEMP() {
        return vendad_preco_vendaTEMP;
    }

    public void setVendad_preco_vendaTEMP(BigDecimal vendad_preco_vendaTEMP) {
        this.vendad_preco_vendaTEMP = vendad_preco_vendaTEMP;
    }

    public BigDecimal getVendad_totalTEMP() {
        return vendad_totalTEMP;
    }

    public void setVendad_totalTEMP(BigDecimal vendad_totalTEMP) {
        this.vendad_totalTEMP = vendad_totalTEMP;
    }

    public BigDecimal getSubTotal (){
        return  this.vendad_preco_vendaTEMP.multiply(this.vendad_quantidadeTEMP);
    }

    public String getvendad_prd_view(){return vendad_prd_view;}

    public void setVendad_prd_view(String vendad_prd_view) {
        this.vendad_prd_view = vendad_prd_view;
    }
}
