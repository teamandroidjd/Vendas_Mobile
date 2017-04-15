package com.jdsystem.br.vendasmobile.domain;

/**
 * Created by Usuário on 10/04/2017.
 */

public class FiltroClientes {
    private String codClieExt;
    private String codClienteInt;
    private String nomeRazao;
    private String NomeFan;
    private String documento;
    private String Estado;
    private String Cidade;
    private String Bairro;
    private String Tel1;
    private String Tel2;
    private String bloqueio;
    private String integrado;

    public FiltroClientes() {
    }

    public FiltroClientes(String codClieExterno, String codclienteinterno, String nmRazao, String nmFan, String doc, String estado, String cidade, String bairro, String tel1, String tel2,
                    String bloqclie, String flagintegrado) {

        codClieExt = codClieExterno;
        codClienteInt = codclienteinterno;
        nomeRazao = nmRazao;
        NomeFan = nmFan;
        documento = doc;
        Estado = estado;
        Cidade = cidade;
        Bairro = bairro;
        Tel1 = tel1;
        Tel2 = tel2;
        bloqueio = bloqclie;
        integrado = flagintegrado;
    }

    public String getCodClienteExt() {
        return codClieExt;
    }

    public void setCodClienteExt(String CodClie) { this.codClieExt = CodClie;  }

    public String getNomeRazao() {
        return nomeRazao;
    }

    public void setNomeRazao(String NomeRazao) { this.nomeRazao = NomeRazao; }

    public String getNomeFan() {
        return NomeFan;
    }

    public void setNomeFan(String NomeFan) {
        this.NomeFan = NomeFan;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {this.documento = documento; }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String Estado) {
        this.Estado = Estado;
    }

    public String getCidade() {
        return Cidade;
    }

    public void setCidade(String Cidade) {
        this.Cidade = Cidade;
    }

    public String getBairro() {
        return Bairro;
    }

    public void setBairro(String Bairro) {
        this.Bairro = Bairro;
    }

    public String getTelefone1() {
        return Tel1;
    }

    public void setTelefone1(String Telefone1) {
        this.Tel1 = Telefone1;
    }

    public String getTelefone2() {
        return Tel2;
    }

    public void setTelefone2(String Telefone2) {
        this.Tel2 = Telefone2;
    }

    public String getCodClienteInt() { return codClienteInt; }

    public void setCodClienteInt(String clieint){this.codClienteInt = clieint;}

    public String getbloqueio() { return bloqueio; }

    public String getflagintegrado() { return integrado; }

}
