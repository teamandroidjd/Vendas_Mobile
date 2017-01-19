package com.jdsystem.br.vendasmobile.domain;

/**
 * Created by eduardo.costa on 06/12/2016.
 */

public class Pedidos {

    private String Situacao;
    private String NomeCliente;
    private String ValorTotal;
    private String Vendedor;
    private String DataVenda;
    private String NumPedido;
    private String NumPedidoExt;
    private String NumFiscal;
    private String Empresa;

    public Pedidos() {
    }

    public Pedidos(String Situacao, String NomeCliente, String ValorTotal, String Vendedor, String DataVenda, String NumPedido, String NumPedidoExt, String NumFiscal, String Empresa) {

        this.Situacao = Situacao;
        this.NomeCliente = NomeCliente;
        this.ValorTotal = ValorTotal;
        this.Vendedor = Vendedor;
        this.DataVenda = DataVenda;
        this.NumPedido = NumPedido;
        this.NumPedidoExt = NumPedidoExt;
        this.NumFiscal = NumFiscal;
        this.Empresa = Empresa;

    }

    public String getSituacao() {
        return Situacao;
    }

    public void setSituacao(String situacao) {
        Situacao = situacao;
    }

    public String getNomeCliente() {
        return NomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        NomeCliente = nomeCliente;
    }

    public String getValorTotal() {
        return ValorTotal;
    }

    public void setValorTotal(String valorTotal) {
        ValorTotal = valorTotal;
    }

    public String getVendedor() {
        return Vendedor;
    }

    public void setVendedor(String vendedor) {
        Vendedor = vendedor;
    }

    public String getDataVenda() {
        return DataVenda;
    }

    public void setDataVenda(String dataVenda) {
        DataVenda = dataVenda;
    }

    public String getNumPedido() {

        return NumPedido;
    }

    public void setNumPedido(String numPedido) {

        NumPedido = numPedido;
    }

    public String getNumPedidoExt() {

        return NumPedidoExt;
    }

    public void setNumPedidoExt(String numPedidoExt) {

        NumPedidoExt = numPedidoExt;
    }

    public String getNumFiscal() {
        return NumFiscal;
    }

    public void setNumFiscal(String numFiscal) {
        NumFiscal = numFiscal;
    }

    public String getEmpresa () {
        return Empresa;
    }
    public void setEmpresa (String empresa) {
        Empresa = empresa;
    }
}
