package com.jdsystem.br.vendasmobile.domain;

import java.math.BigDecimal;

/**
 * Created by wks on 13/03/2017.
 */

public class FiltroProdutos {

    private String Descricao;
    private String CodigoManual;
    private String Status;
    private String UnidVenda;
    private String Apresentacao;
    private String PrecoPadrao;
    private String Preco1;
    private String Preco2;
    private String Preco3;
    private String Preco4;
    private String Preco5;
    private String PrecoP1;
    private String PrecoP2;
    private String Quantidade;
    private String Tabela1;
    private String Tabela2;
    private String Tabela3;
    private String Tabela4;
    private String Tabela5;
    private String Tabpromo1;
    private String Tabpromo2;
    private String TipoEstoque;


    public FiltroProdutos() {
    }

    public FiltroProdutos(String Descricao, String CodigoManual, String Status, String UnidVenda, String Apresentacao, BigDecimal Preco1,  BigDecimal Preco2,BigDecimal Preco3,BigDecimal Preco4,BigDecimal Preco5,BigDecimal PrecoP1,BigDecimal PrecoP2, String Quantidade, String Tabela1, String Tabela2, String Tabela3, String Tabela4, String Tabela5, String Tabpromo1, String Tabpromo2, String TipoEstoque, BigDecimal PrecoPadrao) {
        this.Descricao = Descricao;
        this.CodigoManual = CodigoManual;
        this.Status = Status;
        this.UnidVenda = UnidVenda;
        this.Apresentacao = Apresentacao;
        this.Preco1 = Preco1.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.Preco2 = Preco2.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.Preco3 = Preco3.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.Preco4 = Preco4.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.Preco5 = Preco5.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.PrecoP1 = PrecoP1.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.PrecoP2 = PrecoP2.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
        this.Quantidade = Quantidade;
        this.Tabela1 = Tabela1;
        this.Tabela2 = Tabela2;
        this.Tabela3 = Tabela3;
        this.Tabela4 = Tabela4;
        this.Tabela5 = Tabela5;
        this.Tabpromo1 = Tabpromo1;
        this.Tabpromo2 = Tabpromo2;
        this.TipoEstoque = TipoEstoque;
        this.PrecoPadrao = PrecoPadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');


    }

    public String getCodigoManual() {
        return CodigoManual;
    }

    public void setCodigoManual(String codigoManual) {
        CodigoManual = codigoManual;
    }

    public String getDescricao() {
        return Descricao;
    }

    public void setDescricao(String descricao) {
        Descricao = descricao;
    }

    public String getUnidVenda() {
        return UnidVenda;
    }

    public void setUnidVenda(String unidVenda) {
        UnidVenda = unidVenda;
    }

    public String getPreco1() {
        return Preco1;
    }

    public void setPreco1(String preco1) {
        Preco1 = preco1;
    }

    public String getPreco2() {
        return Preco2;
    }

    public void setPreco2(String preco2) {
        Preco2 = preco2;
    }

    public String getPreco3() {
        return Preco3;
    }

    public void setPreco3(String preco3) {
        Preco3 = preco3;
    }

    public String getPreco4() {
        return Preco4;
    }

    public void setPreco4(String preco4) {
        Preco4 = preco4;
    }

    public String getPreco5() {
        return Preco5;
    }

    public void setPreco5(String preco5) {
        Preco5 = preco5;
    }

    public String getPrecoP1() {
        return PrecoP1;
    }

    public void setPrecop1(String precoP1) {
        PrecoP1 = precoP1;
    }

    public String getPrecoP2() {
        return PrecoP2;
    }

    public void setPrecoP2(String precoP2) {
        PrecoP2 = precoP2;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getApresentacao() {
        return Apresentacao;
    }

    public void setApresentacao(String apresentacao) {
        Apresentacao = apresentacao;
    }

    public String getQuantidade() {
        return Quantidade;
    }

    public void setQuantidade(String quantidade) {
        Quantidade = quantidade;
    }

    public String getTabela1 () {
        return Tabela1 ;
    }

    public void setTabela1 (String tabela1 ) {
        Tabela1  = tabela1;
    }
    public String getTabela2 () {
        return Tabela2 ;
    }

    public void setTabela2 (String tabela2 ) {
        Tabela2  = tabela2;
    }

    public String getTabela3 () {
        return Tabela3 ;
    }

    public void setTabela3 (String tabela3 ) {
        Tabela3  = tabela3;
    }

    public String getTabela4 () {
        return Tabela4 ;
    }

    public void setTabela4 (String tabela4 ) {
        Tabela4  = tabela4;
    }

    public String getTabela5 () {
        return Tabela5 ;
    }

    public void setTabela5 (String tabela5 ) {
        Tabela5  = tabela5;
    }

    public String getTabpromo1 () {
        return Tabpromo1 ;
    }

    public void setTabpromo1 (String tabpromo1 ) {
        Tabpromo1  = tabpromo1;
    }

    public String getTabpromo2() {
        return Tabpromo2;
    }

    public void setTabpromo2(String tabpromo2) {
        Tabpromo2 = tabpromo2;
    }

    public String getTipoEstoque() {
        return TipoEstoque;
    }

    public void setTipoEstoque(String tipoEstoque) {
        TipoEstoque = tipoEstoque;
    }

    public String getPrecoPadrao() {
        return PrecoPadrao;
    }

    public void setPrecoPadrao(String precoPadrao) {
        PrecoPadrao = precoPadrao;
    }

}
