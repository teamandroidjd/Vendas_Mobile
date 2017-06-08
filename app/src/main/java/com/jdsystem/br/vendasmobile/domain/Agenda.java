package com.jdsystem.br.vendasmobile.domain;

/**
 * Created by eduardo.costa on 06/12/2016.
 */

public class Agenda {

    private String Situacao;
    private String NomeContato;
    private String Dataagendamento;
    private String NumAgenda;
    private String Status;


    public Agenda() {
    }

    public Agenda(String Situacao, String NomeContato, String Dataagendamento, String NumAgenda, String Status) {

        this.Situacao = Situacao;
        this.NomeContato = NomeContato;
        this.Dataagendamento = Dataagendamento;
        this.NumAgenda = NumAgenda;
        this.Status = Status;

    }

    public String getSituacao() {
        return Situacao;
    }

    public void setSituacao(String situacao) {
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


}
