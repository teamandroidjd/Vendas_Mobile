package com.jdsystem.br.vendasmobile.domain;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;

import java.math.BigDecimal;

/**
 * Created by eduardo.costa on 25/11/2016.
 */

public class ItensPedido extends SqliteVendaD_TempBean {

    private String CodigoManual;
    private String Descricao;
    private String UnidVenda;
    private String Preco;
    private String edtqtdItem;

    public ItensPedido() {
    }

    public ItensPedido(String CodigoManual, String Descricao, String UnidVenda, String Preco) {
        this.CodigoManual = CodigoManual;
        this.Descricao = Descricao;
        this.UnidVenda = UnidVenda;
        this.Preco = Preco;
    }

    public String getEdtqtdItem() {

        return edtqtdItem;
    }

    public void setEdtqtdItem(String edtqtdItem) {

        this.edtqtdItem = edtqtdItem;
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

    public String getPreco() {
        BigDecimal venda = new BigDecimal(Double.parseDouble(getPreco().replace(',', '.')));
        Preco = venda.setScale(2, BigDecimal.ROUND_UP).toString();
        return Preco;
    }

    public void setPreco(String preco) {
        Preco = preco;
    }
}
