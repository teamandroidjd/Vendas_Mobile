package com.jdsystem.br.vendasmobile.domain;

import android.widget.Button;

import java.math.BigDecimal;

/**
 * Created by eduardo.costa on 25/11/2016.
 */

public class Produtos extends ItensPedido {

    private String CodigoManual;
    private String Descricao;
    private String UnidVenda;
    private String Preco;
    private String Status;
    private String Apresentacao;

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
         return Preco;
    }

    public void setPreco(String preco) {
        Preco = preco;
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


    public Produtos(String CodigoManual, String Descricao, String UnidVenda, BigDecimal Preco, String Status, String Apresentacao) {
            this.CodigoManual = CodigoManual;
        this.Descricao = Descricao;
        this.UnidVenda = UnidVenda;
        this.Preco = Preco.setScale(2, BigDecimal.ROUND_UP).toString();
        this.Status = Status;
        this.Apresentacao = Apresentacao;
    }

}
