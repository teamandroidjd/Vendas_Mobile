package com.jdsystem.br.vendasmobile.domain;


public class Clientes {

        private String docFormatado;
        private String NomeRazao;
        private String NomeFan;
        private String Cidade;
        private String Estado;
        private String Bairro;
        private String Telefone;
        private String CodCliente;


        public Clientes() {
        }

        public Clientes(String docFormat, String nmRazao, String nmFan, String cidade, String estado, String bairro, String telefone, String codCliente) {

            docFormatado = docFormat;
            NomeRazao = nmRazao;
            NomeFan = nmFan;
            Cidade = cidade;
            Estado = estado;
            Bairro = bairro;
            Telefone = telefone;
            CodCliente = codCliente;
        }

        public String getDocumento() {
            return docFormatado;
        }

        public String getCodCliente() {
            return CodCliente;
        }

        public void setCodCliente(String codCliente) {

            CodCliente = codCliente;
        }

        public void setDocumento(String docFormatado) {

            this.docFormatado = docFormatado;
        }

        public String getNomeRazao() {
            return NomeRazao;
        }

        public void setNomeRazao(String NomeRazao) {

            this.NomeRazao = NomeRazao;
        }

        public String getNomeFan() {
            return NomeFan;
        }

        public void setNomeFan(String NomeFan) {
            this.NomeFan = NomeFan;
        }

        public String getCidade() {
            return Cidade;
        }

        public void setCidade(String Cidade) {
            this.Cidade = Cidade;
        }

        public String getEstado() {
            return Estado;
        }

        public void setEstado(String Estado) {
            this.Estado = Estado;
        }

        public String getBairro() {
            return Bairro;
        }

        public void setBairro(String Bairro) {
            this.Bairro = Bairro;
        }

        public String getTelefone() {
            return Telefone;
        }

        public void setTelefone(String Telefone) {
            this.Telefone = Telefone;
        }
    }