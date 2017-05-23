package com.jdsystem.br.vendasmobile.Model;

/**
 * Created by JAVA on 24/08/2015.
 */
public class SqliteClienteBean {

    public static final String C_CODIGO_CLIENTE_CURSOR = "_id";
    public static final String C_CODIGO_CLIENTE = "CODCLIE_INT";
    public static final String C_CODIGO_CLIENTE_EXT = "CODCLIE_EXT";
    public static final String C_NOME_DO_CLIENTE = "NOMERAZAO";
    public static final String C_NOME_FANTASIA = "NOMEFAN";
    public static final String C_ENDERECO_CLIENTE = "ENDERECO";
    public static final String C_BAIRRO_CLIENTE = "BAIRRO";
    public static final String C_CEP_CLIENTE = "CEP";
    public static final String C_CIDADE_CLIENTE = "CIDADE";
    public static final String C_TELEFONE_CLIENTE = "TEL1";
    //public static final String C_DATA_NASCIMENTO = "cli_nascimento";
    public static final String C_CNPJCPF = "CNPJ_CPF";
    public static final String C_RGINSCRICAO_ESTADUAL = "INSCREST";
    public static final String C_EMAIL_CLIENTE = "EMAIL";
    public static final String C_ENVIADO = "FLAGINTEGRADO";
    public static final String C_CHAVE_DO_CLIENTE = "CHAVE";
    public static final String C_UF_CLIENTE = "UF";


    private Integer cli_codigo;
    private Integer cli_codigo_ext;
    private String cli_nome;
    private String cli_fantasia;
    private String cli_endereco;
    private String cli_bairro;
    private String cli_cep;

    private String cid_nome;
    private String cli_tel1;
    //private String cli_nascimento;
    private String cli_cpfcnpj;
    private String cli_rginscricaoest;
    private String cli_email;
    private String cli_enviado;
    private String cli_chave;
    private String cli_uf;

    public Integer getCli_codigo_ext() {
        return cli_codigo_ext;
    }

    public void setCli_codigo_ext(Integer cli_codigo_ext) {
        this.cli_codigo_ext = cli_codigo_ext;
    }

    public Integer getCli_codigo() {
        return cli_codigo;
    }

    public void setCli_codigo(Integer cli_codigo) {
        this.cli_codigo = cli_codigo;
    }

    public String getCli_nome() {
        return cli_nome;
    }

    public void setCli_nome(String cli_nome) {

        this.cli_nome = cli_nome;
    }

    public String getCli_tel1() {
        return cli_tel1;
    }

    public void setCli_tel1(String cli_tel1) {
        this.cli_tel1 = cli_tel1;
    }

    public String getCli_fantasia() {
        return cli_fantasia;
    }

    public void setCli_fantasia(String cli_fantasia) {
        this.cli_fantasia = cli_fantasia;
    }

    public String getCli_endereco() {
        return cli_endereco;
    }

    public void setCli_endereco(String cli_endereco) {
        this.cli_endereco = cli_endereco;
    }

    public String getCli_bairro() {
        return cli_bairro;
    }

    public void setCli_bairro(String cli_bairro) {
        this.cli_bairro = cli_bairro;
    }

    public String getCli_cep() {
        return cli_cep;
    }

    public void setCli_cep(String cli_cep) {
        this.cli_cep = cli_cep;
    }

    public String getCid_nome() {
        return cid_nome;
    }

    public void setCid_nome(String cid_nome) {
        this.cid_nome = cid_nome;
    }

    public String getCli_cpfcnpj() {
        return cli_cpfcnpj;
    }

    public void setCli_cpfcnpj(String cli_cpfcnpj) {
        this.cli_cpfcnpj = cli_cpfcnpj;
    }

    public String getCli_rginscricaoest() {
        return cli_rginscricaoest;
    }

    public void setCli_rginscricaoest(String cli_rginscricaoest) {
        this.cli_rginscricaoest = cli_rginscricaoest;
    }

    public String getCli_email() {
        return cli_email;
    }

    public void setCli_email(String cli_email) {
        this.cli_email = cli_email;
    }

    public String getCli_enviado() {
        return cli_enviado;
    }

    public void setCli_enviado(String cli_enviado) {
        this.cli_enviado = cli_enviado;
    }

    public String getCli_chave() {
        return cli_chave;
    }

    public void setCli_chave(String cli_chave) {
        this.cli_chave = cli_chave;
    }


    public String getCli_uf() {
        return cli_uf;
    }

    public void setCli_uf(String cli_uf) {
        this.cli_uf = cli_uf;
    }
}
