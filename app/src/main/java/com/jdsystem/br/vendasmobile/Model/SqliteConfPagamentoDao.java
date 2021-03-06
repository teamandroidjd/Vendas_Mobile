package com.jdsystem.br.vendasmobile.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.jdsystem.br.vendasmobile.ConfigDB;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SqliteConfPagamentoDao {

    private Context ctx;
    private String Sql;
    private SQLiteStatement stmt;
    private SQLiteStatement stmtAtu;
    private SQLiteStatement stmtDel;
    private SQLiteDatabase db;
    private Cursor cursor;

    public SqliteConfPagamentoDao(Context ctx) {
        this.ctx = ctx;
    }

    public boolean gravar_CONFPAGAMENTO(SqliteConfPagamentoBean pagamento, Boolean AtuPedido, String ChavePedido) {
        db = new ConfigDB(ctx).getWritableDatabase();
        boolean gravou = false;
        try {
            if (AtuPedido.equals(true)) {
                Sql = "INSERT INTO CONFPAGAMENTO  (conf_sementrada_comentrada, conf_tipo_pagamento,conf_recebeucom_din_chq_car,conf_valor_recebido,conf_parcelas," +
                        "vendac_chave,conf_enviado,conf_codformpgto_ext,conf_dias_vencimento,conf_descricao_formpgto)  VALUES (?,?,?,?,?,?,?,?,?,?)";
                stmt = db.compileStatement(Sql);
                stmt.bindString(1, pagamento.getConf_sementrada_comentrada());
                stmt.bindString(2, pagamento.getConf_tipo_pagamento());
                stmt.bindString(3, pagamento.getConf_recebeucom_din_chq_car());
                stmt.bindDouble(4, pagamento.getConf_valor_recebido().doubleValue());
                stmt.bindLong(5, pagamento.getConf_parcelas());
                stmt.bindString(6, ChavePedido);
                stmt.bindString(7, pagamento.getConf_enviado());
                stmt.bindString(8, pagamento.getConf_codformpgto());
                stmt.bindString(9, pagamento.getConf_diasvencimento());
                stmt.bindString(10, pagamento.getConf_descformpgto());

                if (stmt.executeInsert() > 0) {
                    gravou = true;
                }
                /*Cursor atuconfpgto = db.rawQuery("SELECT conf_sementrada_comentrada, conf_tipo_pagamento,conf_recebeucom_din_chq_car,conf_valor_recebido,conf_parcelas,vendac_chave,conf_enviado FROM CONFPAGAMENTO WHERE vendac_chave = '"+ ChavePedido +"'",null);
                atuconfpgto.moveToFirst();
                if(atuconfpgto.getCount() > 0){
                    db.execSQL("UPDATE CONFPAGAMENTO SET conf_sementrada_comentrada = '"+ pagamento.getConf_sementrada_comentrada() +
                            "', conf_tipo_pagamento = '"+ pagamento.getConf_tipo_pagamento() +
                            "', conf_recebeucom_din_chq_car = '"+ pagamento.getConf_recebeucom_din_chq_car() +
                            "', conf_valor_recebido = '"+ pagamento.getConf_valor_recebido().doubleValue() +
                            "', conf_parcelas = '"+ pagamento.getConf_parcelas() +
                            "', vendac_chave = '"+ ChavePedido +
                            "', conf_enviado = '"+ pagamento.getConf_enviado() +"'" +
                            " WHERE vendac_chave = '"+ ChavePedido +"'");
                    atuconfpgto.close();*/


            } else {

                Sql = "INSERT INTO CONFPAGAMENTO  (conf_sementrada_comentrada, conf_tipo_pagamento,conf_recebeucom_din_chq_car,conf_valor_recebido," +
                        "conf_parcelas,vendac_chave,conf_enviado,conf_codformpgto_ext,conf_dias_vencimento,conf_descricao_formpgto)  VALUES (?,?,?,?,?,?,?,?,?,?)";
                stmt = db.compileStatement(Sql);
                stmt.bindString(1, pagamento.getConf_sementrada_comentrada());
                stmt.bindString(2, pagamento.getConf_tipo_pagamento());
                stmt.bindString(3, pagamento.getConf_recebeucom_din_chq_car());
                stmt.bindDouble(4, pagamento.getConf_valor_recebido().doubleValue());
                stmt.bindLong(5, pagamento.getConf_parcelas());
                stmt.bindString(6, pagamento.getVendac_chave());
                stmt.bindString(7, pagamento.getConf_enviado());
                stmt.bindString(8, pagamento.getConf_codformpgto());
                stmt.bindString(9, pagamento.getConf_diasvencimento());
                stmt.bindString(10, pagamento.getConf_descformpgto());


                if (stmt.executeInsert() > 0) {
                    gravou = true;
                }
            }

        } catch (Exception e) {
            Log.d("gravar_CONFPAGAMENTO", e.getMessage());
            gravou = false;
        } finally {
            db.close();
            stmt.close();
        }

        return gravou;
    }

    public void AtualizaVendac_chave_CONFPAGAMENTO(String vendac_chave) {
        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            String sql = "UPDATE CONFPAGAMENTO set vendac_chave = ? where vendac_chave  = '' ";
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindString(1, vendac_chave);
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Log.d("AtualizaVendac_chaveC", e.getMessage());
        }
    }

    public void Atualiza_CONFPAGAMENTO_ParaEnviado(String vendac_chave) {

        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            String sql = "update CONFPAGAMENTO set conf_enviado = 'S' where vendac_chave  = ? ";
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindString(1, vendac_chave);
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Log.d("AtualizaConfPagamentoPa", e.getMessage());
        }
    }

    public List<SqliteConfPagamentoBean> lista_de_CONFPAGAMENTO() {
        List<SqliteConfPagamentoBean> lista = new ArrayList<SqliteConfPagamentoBean>();
        try {
            db = new ConfigDB(ctx).getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO", null);
            while (cursor.moveToNext()) {
                SqliteConfPagamentoBean conf = new SqliteConfPagamentoBean();
                conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                lista.add(conf);
            }
        } catch (SQLiteException e) {
            Log.d("lista_das_config", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }

        return lista;
    }


    public List<SqliteConfPagamentoBean> busca_todos_CONFPAGAMENTO_nao_enviados(String chave) {
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        List<SqliteConfPagamentoBean> lista = new ArrayList<SqliteConfPagamentoBean>();
        if (chave.toString().equals("")) {
            try {
                db = new ConfigDB(ctx).getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '' AND conf_temp = 'N' AND CODPERFIL =" + idPerfil, null);
                cursor.moveToFirst();
                do {
                    SqliteConfPagamentoBean conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                    lista.add(conf);
                } while (cursor.moveToNext());
            } catch (SQLiteException e) {
                Log.d("busca_todos_confpag", e.getMessage());
            } finally {
                db.close();
                cursor.close();
                return lista;
            }
        } else {
            try {
                db = new ConfigDB(ctx).getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '" + chave + "' AND conf_temp = 'N' AND CODPERFIL =" + idPerfil, null);
                cursor.moveToFirst();
                do {
                    SqliteConfPagamentoBean conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                    lista.add(conf);
                } while (cursor.moveToNext());
            } catch (SQLiteException e) {
                Log.d("busca_todos_confpag", e.getMessage());
            } finally {
                db.close();
                cursor.close();
                return lista;
            }
        }

        //return lista;
    }

    public SqliteConfPagamentoBean busca_CONFPAGAMENTO_sem_chave() {
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        SqliteConfPagamentoBean conf = null;
        try {
            db = new ConfigDB(ctx).getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '' AND CODPERFIL = " + idPerfil, null);
            if (cursor.moveToFirst()) {
                conf = new SqliteConfPagamentoBean();
                conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));

            }
        } catch (SQLiteException e) {
            Log.d("busca_CONFPAGAMENTO_sem", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }

        return conf;
    }

    public SqliteConfPagamentoBean busca_CONFPAGAMENTO_Pedido(String Chave_pedido) {
        SqliteConfPagamentoBean conf = null;
        try {
            db = new ConfigDB(ctx).getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido + " and conf_temp = 'N'", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Log.d("busca_CONFPAGAMENTO_sem", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return conf;
    }

    public SqliteConfPagamentoBean salva_CONFPAGAMENTO_TEMP_Pedido(String Chave_pedido) { // QUANDO INICIALIZA A ACTIVITY SALVA A FORMA DE PAGAMENTO ORIGINAL DO PEDIDO.
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        SqliteConfPagamentoBean conf = null;
        try {
            db = new ConfigDB(ctx).getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '" + Chave_pedido + "' AND CODPERFIL = " + idPerfil, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                    conf.setConf_temp("T");
                    db.execSQL("INSERT INTO CONFPAGAMENTO (conf_sementrada_comentrada, conf_tipo_pagamento,conf_recebeucom_din_chq_car,conf_valor_recebido," +
                            "conf_parcelas,vendac_chave,conf_enviado,CONF_CODFORMPGTO_EXT,CONF_DIAS_VENCIMENTO,conf_descricao_formpgto," +
                            "CONF_DATA_VENCIMENTO,CODPERFIL,conf_temp) values(" + "'" + conf.getConf_sementrada_comentrada() +
                            "', '" + conf.getConf_tipo_pagamento() +
                            "', '" + conf.getConf_recebeucom_din_chq_car() +
                            "', '" + conf.getConf_valor_recebido() +
                            "', '" + conf.getConf_parcelas() +
                            "','" + conf.getVendac_chave() +
                            "', '" + conf.getConf_enviado() +
                            "', '" + conf.getConf_codformpgto() +
                            "', '" + conf.getConf_diasvencimento() +
                            "', '" + conf.getConf_descformpgto() +
                            "', '" + conf.getConf_datavencimento() +
                            "', " + idPerfil +
                            ",'" + conf.getConf_temp() + "');");
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Log.d("busca_CONFPAGAMENTO_sem", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return conf;
    }

    public SqliteConfPagamentoBean recupera_CONFPAGAMENTO_TEMP_Pedido(String Chave_pedido) { // QUANDO CANCELA A ALTERAÇÃO DO PEDIDO, É MANTIDA A FORMA DE PAGAMENTO ORIGINAL E EXCLUIDA A FORMA DE PAGAMENTO INCLUIDA A NOVA.
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        SqliteConfPagamentoBean conf = null;
        try {
            db = new ConfigDB(ctx).getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '" + Chave_pedido + "' AND conf_temp = 'T' AND CODPERFIL = " + idPerfil, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor.getInt(cursor.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor.getString(cursor.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor.getString(cursor.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor.getString(cursor.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor.getString(cursor.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor.getString(cursor.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor.getString(cursor.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                    db.execSQL("UPDATE CONFPAGAMENTO SET conf_sementrada_comentrada = '" + conf.getConf_codigo() +
                            "', conf_tipo_pagamento = '" + conf.getConf_tipo_pagamento() +
                            "', conf_recebeucom_din_chq_car = '" + conf.getConf_recebeucom_din_chq_car() +
                            "', conf_valor_recebido = '" + conf.getConf_valor_recebido() +
                            "', conf_parcelas = '" + conf.getConf_parcelas() +
                            "', vendac_chave = '" + conf.getVendac_chave() +
                            "', conf_enviado = '" + conf.getConf_enviado() +
                            "', CONF_CODFORMPGTO_EXT = '" + conf.getConf_codformpgto() +
                            "', CONF_DIAS_VENCIMENTO = '" + conf.getConf_diasvencimento() +
                            "', conf_descricao_formpgto = '" + conf.getConf_descformpgto() +
                            "', CONF_DATA_VENCIMENTO = '" + conf.getConf_datavencimento() +
                            "', conf_temp = 'N' WHERE vendac_chave = '" + conf.getVendac_chave() + "' AND CONF_CODIGO = " + conf.getConf_codigo() + " AND conf_temp = 'N' AND CODPERFIL = " + idPerfil);
                } while (cursor.moveToNext());
                db.execSQL("DELETE FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido + " AND CONF_TEMP = 'T' AND CODPERFIL = " + idPerfil);
            }
        } catch (SQLiteException e) {
            Log.d("busca_CONFPAGAMENTO_sem", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return conf;
    }

    public SqliteConfPagamentoBean atualiza_CONFPAGAMENTO_TEMP_Pedido(String Chave_pedido) { // QUANDO SALVA A ALTERAÇÃO DO PEDIDO, É EXCLUIDA A FORMA DE PAGAMENTO ORIGINAL E INCLUIDA A NOVA.
        SqliteConfPagamentoBean conf = null;
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        try {
            /*String tipopgtoN = null;
            double vlrecebidoN = 0;
            int parcelaN = 0;*/
            db = new ConfigDB(ctx).getReadableDatabase();
            /*cursor = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido +
                    " AND conf_temp = 'T' AND CODPERFIL = " + idPerfil, null);*/
            Cursor cursor2 = db.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido +
                    " AND conf_temp = 'N' AND CODPERFIL = " + idPerfil, null);
            cursor2.moveToFirst();
            //cursor.moveToFirst();
            double vltotalrecebidoN = 0;
            double vltotalrecebido = 0;
            int parcela;
            String tipopgto = "0";
            /*if (cursor2.getCount() > 0 || cursor.getCount() > 0) {
                do {
                    parcelaN = cursor2.getInt(cursor2.getColumnIndex("conf_parcelas"));
                    parcela = cursor.getInt(cursor.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS));
                } while (parcelaN == parcela && (cursor.moveToNext() || cursor2.moveToNext()));

                if(parcelaN == parcela){
                    do{
                        tipopgtoN = cursor2.getString(cursor2.getColumnIndex("conf_tipo_pagamento"));
                        tipopgto = cursor.getString(cursor.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO));

                    }while (tipopgtoN.equals(tipopgto) && (cursor.moveToNext() || cursor2.moveToNext()));
                } else {

                }
                if(tipopgtoN.equals(tipopgto)){
                    do{
                        vlrecebidoN = cursor2.getDouble(cursor2.getColumnIndex("conf_valor_recebido"));
                        vltotalrecebidoN = vltotalrecebidoN + vlrecebidoN;

                        double vlrecebido = cursor.getDouble(cursor.getColumnIndex(conf.CONF_VALOR_RECEBIDO));
                        vltotalrecebido = vltotalrecebido + vlrecebido;

                    }while (vlrecebidoN == vltotalrecebido && (cursor.moveToNext() || cursor2.moveToNext()));

                } else {

                }
                //TODO: CONTINUAR O PROCESSO DE VERIFICAÇAO DAS PARCELAS.





                if (tipopgtoN.equals(tipopgto) && vltotalrecebidoN == vltotalrecebido && parcelaN == parcela) {
                    db.execSQL("DELETE FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido + " AND CONF_TEMP = 'T' AND CODPERFIL = " + idPerfil);
                    db.close();
                    cursor.close();
                    cursor2.close();
                    return conf;

                }


            } else {*/
            if (cursor2.getCount() > 0) {
                do {
                    conf = new SqliteConfPagamentoBean();
                    conf.setConf_codigo(cursor2.getInt(cursor2.getColumnIndex(conf.CONF_CODIGO_CONFPAGAMENTO)));
                    conf.setConf_parcelas(cursor2.getInt(cursor2.getColumnIndex(conf.CONF_QUANTIDADE_PARCELAS)));
                    conf.setConf_recebeucom_din_chq_car(cursor2.getString(cursor2.getColumnIndex(conf.CONF_DINHEIRO_CARTAO_CHEQUE)));
                    conf.setConf_sementrada_comentrada(cursor2.getString(cursor2.getColumnIndex(conf.CONF_SEMENTADA_COMENTRADA)));
                    conf.setConf_tipo_pagamento(cursor2.getString(cursor2.getColumnIndex(conf.CONF_TIPO_DO_PAGAMENTO)));
                    conf.setConf_valor_recebido(new BigDecimal(cursor2.getDouble(cursor2.getColumnIndex(conf.CONF_VALOR_RECEBIDO))));
                    conf.setVendac_chave(cursor2.getString(cursor2.getColumnIndex(conf.CONF_VENDAC_CHAVE)));
                    conf.setConf_enviado(cursor2.getString(cursor2.getColumnIndex(conf.CONF_ENVIADO)));
                    conf.setConf_codformpgto(cursor2.getString(cursor2.getColumnIndex(conf.CONF_CODFORMPGTO)));
                    conf.setConf_diasvencimento(cursor2.getString(cursor2.getColumnIndex(conf.CONF_DIASVENCIMENTO)));
                    conf.setConf_descformpgto(cursor2.getString(cursor2.getColumnIndex(conf.CONF_DESCFORMPGTO)));
                    conf.setConf_datavencimento(cursor2.getString(cursor2.getColumnIndex(conf.CONF_DATAVENCIMENTO)));
                    db.execSQL("UPDATE CONFPAGAMENTO SET conf_sementrada_comentrada = '" + conf.getConf_sementrada_comentrada() +
                            "', conf_tipo_pagamento = '" + conf.getConf_tipo_pagamento() +
                            "', conf_recebeucom_din_chq_car = '" + conf.getConf_recebeucom_din_chq_car() +
                            "', conf_valor_recebido = '" + conf.getConf_valor_recebido() +
                            "', conf_parcelas = '" + conf.getConf_parcelas() +
                            "', vendac_chave = '" + conf.getVendac_chave() +
                            "', conf_enviado = '" + conf.getConf_enviado() +
                            "', conf_codformpgto_ext = '" + conf.getConf_codformpgto() +
                            "', conf_descricao_formpgto = '" + conf.getConf_descformpgto() +
                            "', conf_dias_vencimento = '" + conf.getConf_diasvencimento() +
                            "', CODPERFIL = " + idPerfil +
                            ",  conf_temp = 'N'" +
                            " WHERE vendac_chave = '" + conf.getVendac_chave() + "' AND CONF_CODIGO = " + conf.getConf_codigo() + " AND conf_temp = 'N' AND CODPERFIL = " + idPerfil);
                } while (cursor2.moveToNext());
                db.execSQL("DELETE FROM CONFPAGAMENTO WHERE vendac_chave = " + Chave_pedido + " AND CONF_TEMP = 'T' AND CODPERFIL = " + idPerfil);
                cursor2.close();
                //}
            }

        } catch (
                SQLiteException e)

        {
            Log.d("busca_CONFPAGAMENTO_sem", e.getMessage());
        } finally

        {
            db.close();
            //cursor.close();
        }
        return conf;
    }

    public void excluir_FormaPgto_Chave(String ChavePedido) {
        SqliteConfPagamentoBean conf = null;
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        db = new ConfigDB(ctx).getWritableDatabase();
        String SqlDel = "DELETE FROM CONFPAGAMENTO WHERE vendac_chave =" + ChavePedido + " and conf_temp = 'N' AND CODPERFIL = " + idPerfil;
        try {
            stmtDel = db.compileStatement(SqlDel);
            stmtDel.executeUpdateDelete();
            stmtDel.clearBindings();
        } catch (SQLiteException e) {
            Log.d("excluir_FormaPgto_Chave", e.getMessage());
        } finally {

            stmtDel.close();
        }
    }

    public void excluir_CONFPAGAMENTO() {
        SqliteConfPagamentoBean conf = null;
        String CONFIG_HOST = "CONFIG_HOST";
        SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        int idPerfil = prefs.getInt("idperfil", 0);
        db = new ConfigDB(ctx).getWritableDatabase();
        Sql = "DELETE FROM CONFPAGAMENTO WHERE vendac_chave = '' AND CODPERFIL = " + idPerfil;
        try {
            stmt = db.compileStatement(Sql);
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Log.d("excluir_CONFPAGAMENTO", e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }

    public void exluiparcela(int codparcela) {
        db = new ConfigDB(ctx).getWritableDatabase();
        try {
            db.execSQL("DELETE FROM CONFPAGAMENTO WHERE CONF_CODIGO =" + codparcela);

        } catch (Exception e) {
            e.toString();
        }
    }
}
