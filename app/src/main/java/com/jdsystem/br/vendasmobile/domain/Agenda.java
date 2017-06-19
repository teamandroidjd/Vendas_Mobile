package com.jdsystem.br.vendasmobile.domain;

/**
 * Created by eduardo.costa on 06/12/2016.
 */

public class Agenda {

    private Integer Situacao;
    private String NomeContato;
    private String Dataagendamento;
    private String NumAgenda;
    private String Status;
    private String Novaagenda;
    private String Dtreagendado;


    public Agenda() {
    }

    public Agenda(Integer Situacao, String NomeContato, String Dataagendamento, String NumAgenda, String Status, String Novaagenda, String Dtreagendado) {

        this.Situacao = Situacao;
        this.NomeContato = NomeContato;
        this.Dataagendamento = Dataagendamento;
        this.NumAgenda = NumAgenda;
        this.Status = Status;
        this.Novaagenda = Novaagenda;
        this.Dtreagendado = Dtreagendado;

    }

    public Integer getSituacao() {
        return Situacao;
    }

    public void setSituacao(Integer situacao) {
        Situacao = situacao;
    }

    public String getNomeContato() {
        return NomeContato;
    }

    public void setNomeContato(String nomeContato) {
        NomeContato = nomeContato;
    }

    public String getDataagendamento() {
        return Dataagendamento;
    }

    public void setDataagendamento(String dataagendamento) {
        Dataagendamento = dataagendamento;
    }

    public String getNumAgenda() {

        return NumAgenda;
    }

    public void setNumAgenda(String numAgenda) {

        NumAgenda = numAgenda;
    }

    public String getStatus() {

        return Status;
    }

    public void setStatus(String status) {

        Status = status;
    }

    public String getNovaagenda() {

        return Novaagenda;
    }

    public void setNovaagenda(String novaagenda) {

        Novaagenda = novaagenda;
    }

    public String getDtreagendado() {

        return Dtreagendado;
    }

    public void setDtreagendado(String dtreagendado) {

        Dtreagendado = dtreagendado;
    }

}
