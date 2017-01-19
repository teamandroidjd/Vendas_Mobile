package com.jdsystem.br.vendasmobile.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class SqliteConRecDao {

    private Context ctx;
    private String sql;
    private boolean gravou;
    private SQLiteStatement stmt;
    private SQLiteDatabase db;
    private Cursor cursor;

    public SqliteConRecDao(Context ctx) {
        this.ctx = ctx;
    }

    public boolean gravar_parcela(SqliteConRecBean rec) {
        try {
            gravou = false;
            db = new ConfigDB(ctx).getWritableDatabase();
            sql = "INSERT INTO CONREC (rec_numparcela,rec_cli_codigo,rec_cli_nome,vendac_chave,rec_datamovimento,rec_valor_receber,rec_valorpago,rec_datavencimento,rec_data_que_pagou,rec_recebeu_com,rec_enviado) values (?,?,?,?,?,?,?,?,?,?,?)";
            stmt = db.compileStatement(sql);
            stmt.bindLong(1, rec.getRec_numparcela());
            stmt.bindLong(2, rec.getRec_cli_codigo());
            stmt.bindString(3, rec.getRec_cli_nome());
            stmt.bindString(4, rec.getVendac_chave());
            stmt.bindString(5, rec.getRec_datamovimento());
            stmt.bindDouble(6, rec.getRec_valor_receber().setScale(2, BigDecimal.ROUND_UP).doubleValue());
            stmt.bindDouble(7, rec.getRec_valorpago().setScale(2, BigDecimal.ROUND_UP).doubleValue());
            stmt.bindString(8, rec.getRec_datavencimento());
            stmt.bindString(9, rec.getRec_data_que_pagou());
            stmt.bindString(10, rec.getRec_recebeu_com());
            stmt.bindString(11, rec.getRec_enviado());
            if (stmt.executeInsert() > 0) {
                gravou = true;
                sql = "";
            }
            stmt.clearBindings();
        } catch (SQLiteException e) {
            gravou = false;
            Log.d("grava_receber", e.getMessage());

        } finally {
            db.close();
            stmt.close();
        }
        return gravou;
    }

//***********************************************************************************

    public SqliteConRecBean busca_menor_parcela(String chave_da_venda) {
        db = new ConfigDB(ctx).getReadableDatabase();
        SqliteConRecBean receber = null;
        try {
            cursor = db.rawQuery("select min(rec_numparcela) as rec_numparcela ,rec_cli_codigo,rec_cli_nome,vendac_chave,rec_datamovimento,rec_valor_receber,rec_valorpago,rec_datavencimento,rec_data_que_pagou,rec_recebeu_com,rec_enviado from CONREC where vendac_chave = ? and rec_valorpago = 0", new String[]{chave_da_venda});
            if (cursor.moveToFirst()) {
                receber = new SqliteConRecBean();
                receber.setRec_numparcela(cursor.getInt(cursor.getColumnIndex(receber.NUMERO_DA_PARCELA)));
                receber.setVendac_chave(cursor.getString(cursor.getColumnIndex(receber.CHAVE_DA_VENDA)));
                receber.setRec_valor_receber(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(receber.VALOR_A_RECEBER))));
            }
        } catch (SQLiteException e) {
            Log.d("busca_numero_parcela", e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return receber;
    }


    public void baixar_parcela_cliente_integral(SqliteConRecBean rec) {
        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            gravou = false;
            sql = "update CONREC set rec_valorpago = ? , rec_data_que_pagou  = ? , rec_recebeu_com = ? ,rec_enviado = ? where vendac_chave = ? and rec_numparcela = ? ";
            stmt = db.compileStatement(sql);
            stmt.bindDouble(1, rec.getRec_valorpago().doubleValue());
            stmt.bindString(2, rec.getRec_data_que_pagou());
            stmt.bindString(3, rec.getRec_recebeu_com());
            stmt.bindString(4, "N");
            stmt.bindString(5, rec.getVendac_chave());
            stmt.bindLong(6, rec.getRec_numparcela());
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Log.d("baixar_parcela_cliente", e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }


    public void atualiza_valorparcela(SqliteConRecBean rec) {
        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            gravou = false;
            sql = "update CONREC set rec_valor_receber = ? , rec_enviado = ? where vendac_chave = ? and rec_numparcela = ? ";
            stmt = db.compileStatement(sql);
            stmt.bindDouble(1, rec.getRec_valor_receber().doubleValue());
            stmt.bindString(2, "N");
            stmt.bindString(3, rec.getVendac_chave());
            stmt.bindLong(4, rec.getRec_numparcela());
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Log.d("atualiza_valorparcela", e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }

    public List<SqliteConRecBean> busca_parcelas_geradas_na_compra(String vendac_chave) {
        List<SqliteConRecBean> parcelas_geradas = new ArrayList<SqliteConRecBean>();
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from CONREC where vendac_chave = ? order by rec_numparcela asc ", new String[]{vendac_chave});
            while (cursor.moveToNext()) {
                SqliteConRecBean parcela = new SqliteConRecBean();
                parcela.setRec_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_RECEBER)));
                parcela.setRec_numparcela(cursor.getInt(cursor.getColumnIndex(parcela.NUMERO_DA_PARCELA)));
                parcela.setRec_cli_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_DO_CLIENTE)));
                parcela.setRec_cli_nome(cursor.getString(cursor.getColumnIndex(parcela.NOME_DO_CLIENTE)));
                parcela.setRec_datamovimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_MOVIMENTO)));
                parcela.setRec_datavencimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_VENCIMENTO)));
                parcela.setRec_valor_receber(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_A_RECEBER))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setRec_valorpago(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_PAGO))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setVendac_chave(cursor.getString(cursor.getColumnIndex(parcela.CHAVE_DA_VENDA)));
                parcelas_geradas.add(parcela);
            }
        } catch (SQLiteException e) {
            Log.d("busca_parcelas_gerad", e.getMessage());
        } finally {
            db.close();
        }
        return parcelas_geradas;
    }


    public List<SqliteConRecBean> busca_parcelas_do_cliente(Integer cli_codigo, String vendac_chave) {
        List<SqliteConRecBean> lista_de_pacelas = new ArrayList<SqliteConRecBean>();

        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();

        try {
            cursor = db.rawQuery("select * from CONREC where rec_cli_codigo = ?  and  vendac_chave = ?", new String[]{cli_codigo.toString(), vendac_chave});
            while (cursor.moveToNext()) {
                SqliteConRecBean parcela = new SqliteConRecBean();

                parcela.setRec_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_RECEBER)));
                parcela.setRec_numparcela(cursor.getInt(cursor.getColumnIndex(parcela.NUMERO_DA_PARCELA)));
                parcela.setRec_cli_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_DO_CLIENTE)));
                parcela.setRec_cli_nome(cursor.getString(cursor.getColumnIndex(parcela.NOME_DO_CLIENTE)));
                parcela.setRec_datamovimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_MOVIMENTO)));
                parcela.setRec_datavencimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_VENCIMENTO)));
                parcela.setRec_valor_receber(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_A_RECEBER))).setScale(2, BigDecimal.ROUND_DOWN));
                parcela.setRec_valorpago(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_PAGO))).setScale(2, BigDecimal.ROUND_DOWN));
                parcela.setVendac_chave(cursor.getString(cursor.getColumnIndex(parcela.CHAVE_DA_VENDA)));
                lista_de_pacelas.add(parcela);
            }

        } catch (SQLiteException e) {
            Log.d("busca_parcelas_do_c", e.getMessage());
        } finally {
            db.close();
        }
        return lista_de_pacelas;
    }

    public List<SqliteConRecBean> busca_contas_nao_enviadas() {
        List<SqliteConRecBean> parcelas = new ArrayList<SqliteConRecBean>();
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            sql = "select * from CONREC where rec_enviado = 'N'";
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                SqliteConRecBean parcela = new SqliteConRecBean();
                parcela.setRec_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_RECEBER)));
                parcela.setRec_numparcela(cursor.getInt(cursor.getColumnIndex(parcela.NUMERO_DA_PARCELA)));
                parcela.setRec_cli_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_DO_CLIENTE)));
                parcela.setRec_cli_nome(cursor.getString(cursor.getColumnIndex(parcela.NOME_DO_CLIENTE)));
                parcela.setRec_datamovimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_MOVIMENTO)));
                parcela.setRec_datavencimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_VENCIMENTO)));
                parcela.setRec_valor_receber(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_A_RECEBER))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setRec_valorpago(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_PAGO))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setVendac_chave(cursor.getString(cursor.getColumnIndex(parcela.CHAVE_DA_VENDA)));
                parcelas.add(parcela);
            }

        } catch (SQLiteException e) {
            Log.d("busca_contas_nao", e.getMessage());
        } finally {
            db.close();
        }
        return parcelas;
    }

    public List<SqliteConRecBean> busca_parcelas_do_cliente(Integer cli_codigo, String datainicio, String datafim, boolean titulospagos, String vendac_chave) {
        List<SqliteConRecBean> lista_de_pacelas = new ArrayList<SqliteConRecBean>();

        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();

        try {

            String data1 = Util.FormataDataAAAAMMDD(datainicio);
            String data2 = Util.FormataDataAAAAMMDD(datafim);

            if (titulospagos)
                sql = "select * from CONREC where  vendac_chave  = ? and  rec_cli_codigo = ? and rec_datavencimento  between ? and ?  and rec_valorpago > 0  order by rec_numparcela asc";
            else
                sql = "select * from CONREC where  vendac_chave  = ? and  rec_cli_codigo = ? and rec_datavencimento  between ? and ?  and rec_valorpago = 0  order by rec_numparcela asc  ";

            cursor = db.rawQuery(sql, new String[]{vendac_chave, cli_codigo.toString(), data1, data2}, null);
            Util.log(sql);

            while (cursor.moveToNext()) {
                SqliteConRecBean parcela = new SqliteConRecBean();
                parcela.setRec_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_RECEBER)));
                parcela.setRec_numparcela(cursor.getInt(cursor.getColumnIndex(parcela.NUMERO_DA_PARCELA)));
                parcela.setRec_cli_codigo(cursor.getInt(cursor.getColumnIndex(parcela.CODIGO_DO_CLIENTE)));
                parcela.setRec_cli_nome(cursor.getString(cursor.getColumnIndex(parcela.NOME_DO_CLIENTE)));
                parcela.setRec_datamovimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_MOVIMENTO)));
                parcela.setRec_datavencimento(cursor.getString(cursor.getColumnIndex(parcela.DATA_DO_VENCIMENTO)));
                parcela.setRec_valor_receber(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_A_RECEBER))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setRec_valorpago(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(parcela.VALOR_PAGO))).setScale(2, BigDecimal.ROUND_UP));
                parcela.setVendac_chave(cursor.getString(cursor.getColumnIndex(parcela.CHAVE_DA_VENDA)));
                lista_de_pacelas.add(parcela);
            }

        } catch (SQLiteException e) {
            Log.d("busca_parcelas_do_c", e.getMessage());
        } finally {
            db.close();
        }
        return lista_de_pacelas;
    }


    public void atualiza_parcela_enviada_S_N(String enviada_S_N, String vendac_chave, int rec_num_parcela) {

        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            gravou = false;
            sql = "update CONREC set rec_enviado = ? where vendac_chave = ? and rec_numparcela = ? ";
            stmt = db.compileStatement(sql);
            stmt.bindString(1, enviada_S_N);
            stmt.bindString(2, vendac_chave);
            stmt.bindLong(3, rec_num_parcela);
            stmt.executeUpdateDelete();
            stmt.clearBindings();

        } catch (SQLiteException e) {
            Log.d("atualiza_parcela", e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }

    }


    public Cursor busca_parcelas_do_cliente() {
        SqliteConRecBean rec = new SqliteConRecBean();
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            cursor = db.query("CONREC",
                    new String[]{rec.CODIGO_RECEBER + " as  _id",
                            rec.NUMERO_DA_PARCELA,
                            rec.DATA_DO_MOVIMENTO,
                            rec.DATA_DO_VENCIMENTO,
                            rec.DATA_QUE_PAGOU,
                            rec.VALOR_PAGO,
                            rec.VALOR_A_RECEBER},
                    null, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }
        } catch (SQLiteException e) {
            Log.d("busca_parcelas_", e.getMessage());
        } finally {
            db.close();

        }

        return cursor;
    }

    public Cursor busca_parcelas_do_cliente_cursor(Integer cli_codigo, String datainicio, String datafim, boolean titulospagos) throws SQLiteException {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        SqliteConRecBean rec = new SqliteConRecBean();
        Cursor resultset = null;
        try {

            String data1 = Util.FormataDataAAAAMMDD(datainicio);
            String data2 = Util.FormataDataAAAAMMDD(datafim);

            if (titulospagos) {
                sql = "select rec_codigo as _id , rec_numparcela ,rec_datamovimento , rec_datavencimento , rec_data_que_pagou , rec_valor_pago ,rec_valorreceber from receber where rec_cli_codigo = " + cli_codigo.toString() + " and rec_datavencimento  between '" + data1.trim() + "' and '" + data2.trim() + "'   ";
                resultset = db.rawQuery(sql, null);
            } else {
                sql = "select rec_codigo as _id , rec_numparcela ,rec_datamovimento , rec_datavencimento , rec_data_que_pagou , rec_valor_pago ,rec_valorreceber from receber where rec_cli_codigo = " + cli_codigo.toString() + " and rec_valor_pago = 0";
                resultset = db.rawQuery(sql, null);
            }

            if (resultset != null) {
                resultset.moveToFirst();
            }

        } catch (SQLiteException e) {
            Log.d("busca_parcelas", e.getMessage());
        } finally {

            db.close();

        }

        return resultset;

    }


}
