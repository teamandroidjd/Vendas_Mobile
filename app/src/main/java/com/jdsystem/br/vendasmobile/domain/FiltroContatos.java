package com.jdsystem.br.vendasmobile.domain;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FiltroContatos {

    private String NOME;
    private String CARGO;
    private String EMAIL;
    private String TEL1;
    private String TEL2;
    private String DOCUMENTO;
    private String DATA;
    private String CEP;
    private String ENDERECO;
    private String NUMERO;
    private String COMPLEMENTO;
    private int CODCIDADE;
    private String UF;
    private int CODVENDEDOR;
    private int CODBAIRRO;
    private int CODCLIENTE;
    private int CODCLIE_EXT;
    private String NOMERAZAO;

    public FiltroContatos(String nmcont, String cargo, String email, String telefone1, String telefone2, String doc, String data, String cep, String endereco,
                          String num, String compl, int cidade, int bairro, String uf, int codvend, int codclieint, int codClieExt, String nomeCliente) {

        NOME = nmcont;
        //Cnpj = nCnpj;
        CARGO = cargo;
        //  NumEndereco = NumeroEndereco;
        //  Complemento = complemento;
        EMAIL = email;
        TEL1 = telefone1;
        TEL2 = telefone2;
        DOCUMENTO = doc;
        DATA = data;
        CEP = cep;
        ENDERECO = endereco;
        NUMERO = num;
        COMPLEMENTO = compl;
        CODCIDADE = cidade;
        CODBAIRRO = bairro;
        UF = uf;
        CODVENDEDOR = codvend;
        CODCLIENTE = codclieint;
        CODCLIE_EXT = codClieExt;
        NOMERAZAO = nomeCliente;



    }

    public String getNomeCont() {
        return NOME;
    }

    public void setNomeCont(String NomeCont) {
        this.NOME = NomeCont;
    }

    public String getCargo () {
        return CARGO;
    }

    public void setCargo (String cargo) {
        this.CARGO = cargo;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String email) {
        this.EMAIL = email;
    }

    public String getTEL1() {
        return TEL1;
    }

    public void setTEL1(String tel1) {
        this.TEL1 = tel1;
    }

    public String getTEL2() {
        return TEL2;
    }

    public void setTEL2(String tel2) {
        this.TEL2 = tel2;
    }

    public String getDOCUMENTO() {
        return DOCUMENTO;
    }

    public void setDOCUMENTO(String doc) {
        this.DOCUMENTO = doc;
    }

    public String getDATA() {
        return DATA;
    }

    public void setDATA(String data) {
        this.DATA = data;
    }

    public String getCEP() {
        return CEP;
    }

    public void setCEP(String cep) {
        this.CEP = cep;
    }

    public String getENDERECO() {
        return ENDERECO;
    }

    public void ENDERECO(String endereco) {
        this.ENDERECO = endereco;
    }

    public String getNUMERO() {
        return NUMERO;
    }

    public void setNUMERO(String num) {
        this.NUMERO = num;
    }

    public String getCOMPLEMENTO() {
        return COMPLEMENTO;
    }

    public void setCOMPLEMENTO(String complemento) {
        this.COMPLEMENTO = complemento;
    }

    public int getCODCIDADE() {return CODCIDADE;}

    public void setCODCIDADE(int codcidade) {
        this.CODCIDADE = codcidade;
    }

    public int getCODBAIRRO() {return CODBAIRRO;}

    public void setCODBAIRRO(int codbairro) {
        this.CODBAIRRO = codbairro;
    }

    public String getUF() {return UF;}

    public void setUF(String uf) {
        this.UF = uf;
    }

    public int getCODVENDEDOR() {return CODVENDEDOR;}

    public void setCODVENDEDOR(int codvend) {
        this.CODVENDEDOR = codvend;
    }

    public int getCODCLIENTE() {return CODCLIENTE;}

    public void setCODCLIENTE(int codclieint) {
        this.CODCLIENTE = codclieint;
    }

    public int getCODCLIEEXT() {return CODCLIE_EXT;}

    public void setCODCLIEEXT(int codcliente) {
        this.CODCLIE_EXT = codcliente;
    }
    public String getNOMECLIENTE() {return NOMERAZAO;}

    public void setNOMECLIENTE(String nomecliente) {
        this.NOMERAZAO = nomecliente;
    }
}
