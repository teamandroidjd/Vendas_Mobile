package com.jdsystem.br.vendasmobile.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.ItensPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JAVA on 07/09/2015.
 */
public class SqliteVendaD_TempDao {


    private Context ctx;
    private String sql;
    private boolean gravacao;
    private SQLiteStatement stmt;
    private SQLiteDatabase db;
    private Cursor cursor;

    public SqliteVendaD_TempDao(Context ctx) {
        this.ctx = ctx;
    }

    public boolean insere_item(SqliteVendaD_TempBean item) {

        gravacao = false;
        try {
            try {
                db = new ConfigDB(ctx).getWritableDatabase();
                sql = "insert into VENDAD_TEMP (vendad_prd_codigoTEMP,vendad_prd_descricaoTEMP,vendad_quantidadeTEMP,vendad_preco_vendaTEMP,vendad_totalTEMP,vendad_prd_unidadeTEMP) values (?,?,?,?,?,?) ";
                stmt = db.compileStatement(sql);
                //stmt.bindString(1, item.getVendad_eanTEMP());
                stmt.bindString(1, item.getVendad_prd_codigoTEMP());
                stmt.bindString(2, item.getVendad_prd_descricaoTEMP());
                stmt.bindDouble(3, item.getVendad_quantidadeTEMP().doubleValue());
                stmt.bindDouble(4, item.getVendad_preco_vendaTEMP().setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue());
                stmt.bindDouble(5, item.getVendad_totalTEMP().setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue());
                String und = item.getVendad_prd_unidadeTEMP();
                stmt.bindString(6, item.getVendad_prd_unidadeTEMP());
                if (stmt.executeInsert() > 0) {
                    gravacao = true;
                }
            } catch (SQLiteException e) {
                Util.log("SQLiteException insere_item" + e.getMessage());
                gravacao = false;
            } finally {
                db.close();
                stmt.close();
            }
        }catch (Exception e){
            e.toString();
        }
        return gravacao;
    }

    public void excluir_um_item_da_venda(SqliteVendaD_TempBean item) {
        db = new ConfigDB(ctx).getWritableDatabase();
        try {
            db.delete("VENDAD_TEMP", "vendad_prd_codigoTEMP = ?", new String[]{item.getVendad_prd_codigoTEMP().toString()});
        } catch (SQLiteException e) {
            Util.log("SQLiteException excluir_um_item_da_venda" + e.getMessage());
        } finally {
            db.close();
        }
    }

    public void excluir_itens() {
        db = new ConfigDB(ctx).getWritableDatabase();
        try {
            db.delete("VENDAD_TEMP", null, null);
        } catch (SQLiteException e) {
            Util.log("SQLiteException excluir_um_item_da_venda" + e.getMessage());
        } finally {
            db.close();
        }
    }

    public SqliteVendaD_TempBean buscar_item_na_venda(SqliteVendaD_TempBean item) {
        SqliteVendaD_TempBean produto = null;
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            sql = "select * from VENDAD_TEMP where vendad_prd_codigoTEMP = ? ";
            cursor = db.rawQuery(sql, new String[]{item.getVendad_prd_codigoTEMP().toString()});
            if (cursor.moveToFirst()) {
                produto = new SqliteVendaD_TempBean();
                //produto.setVendad_eanTEMP(cursor.getString(cursor.getColumnIndex(produto.TEMP_EAN)));
                produto.setVendad_prd_codigoTEMP(cursor.getString(cursor.getColumnIndex(produto.TEMP_CODPRODUTO)));
                produto.setVendad_prd_descricaoTEMP(cursor.getString(cursor.getColumnIndex(produto.TEMP_DESCRICAOPROD)));
                produto.setVendad_prd_unidadeTEMP(cursor.getString(cursor.getColumnIndex(produto.TEMP_UNIDADEPRODUTO)));
                produto.setVendad_quantidadeTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.TEMP_QUANTVENDIDA))));
                produto.setVendad_preco_vendaTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.TEMP_PRECOPRODUTO))).setScale(4,BigDecimal.ROUND_HALF_UP));
                produto.setVendad_totalTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.TEMP_TOTALPRODUTO))).setScale(4,BigDecimal.ROUND_HALF_UP));
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_item_na_venda" + e.getMessage());
        }
        return produto;
    }

    public boolean atualizar_item_na_venda(SqliteVendaD_TempBean item) {
        gravacao = false;
        try {
            try {
                db = new ConfigDB(ctx).getWritableDatabase();
                sql = "UPDATE VENDAD_TEMP SET vendad_prd_codigoTEMP = ?, vendad_prd_descricaoTEMP = ?,vendad_quantidadeTEMP = ?, " +
                       "vendad_preco_vendaTEMP = ?, vendad_totalTEMP = ?, vendad_prd_unidadeTEMP = ? where vendad_prd_codigoTEMP =  " + item.getVendad_prd_codigoTEMP();
                stmt = db.compileStatement(sql);
                //stmt.bindString(1, item.getVendad_eanTEMP());
                stmt.bindString(1, item.getVendad_prd_codigoTEMP());
                stmt.bindString(2, item.getVendad_prd_descricaoTEMP());
                stmt.bindDouble(3, item.getVendad_quantidadeTEMP().doubleValue());
                stmt.bindDouble(4, item.getVendad_preco_vendaTEMP().setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue());
                stmt.bindDouble(5, item.getVendad_totalTEMP().setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue());
                String und = item.getVendad_prd_unidadeTEMP();
                stmt.bindString(6, item.getVendad_prd_unidadeTEMP());
                if (stmt.executeUpdateDelete() > 0) {
                    gravacao = true;
                }
            } catch (SQLiteException e) {
                Util.log("SQLiteException insere_item" + e.getMessage());
                gravacao = false;
            } finally {
                db.close();
                stmt.close();
            }
        }catch (Exception e){
            e.toString();
        }
        return gravacao;
    }

    public List<SqliteVendaD_TempBean> busca_todos_itens_da_venda() {
        List<SqliteVendaD_TempBean> lista_de_itens_vendidos = new ArrayList<SqliteVendaD_TempBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            sql = "select * from VENDAD_TEMP ";
            cursor = db.rawQuery(sql, null);
            if (cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    SqliteVendaD_TempBean items = new SqliteVendaD_TempBean();
                    //items.setVendad_eanTEMP(cursor.getString(cursor.getColumnIndex(items.TEMP_EAN)));
                    items.setVendad_prd_codigoTEMP(cursor.getString(cursor.getColumnIndex(items.TEMP_CODPRODUTO)));
                    items.setVendad_prd_descricaoTEMP(cursor.getString(cursor.getColumnIndex(items.TEMP_DESCRICAOPROD)));
                    String und = cursor.getString(cursor.getColumnIndex(items.TEMP_UNIDADEPRODUTO));
                    items.setVendad_prd_unidadeTEMP(cursor.getString(cursor.getColumnIndex(items.TEMP_UNIDADEPRODUTO)));
                    items.setVendad_quantidadeTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(items.TEMP_QUANTVENDIDA))));
                    items.setVendad_preco_vendaTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(items.TEMP_PRECOPRODUTO))).setScale(4, BigDecimal.ROUND_HALF_UP));
                    items.setVendad_totalTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(items.TEMP_TOTALPRODUTO))).setScale(4, BigDecimal.ROUND_HALF_UP));
                    lista_de_itens_vendidos.add((SqliteVendaD_TempBean) items);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException busca_todos_itens_da_venda" + e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return lista_de_itens_vendidos;
    }

    public List<SqliteVendaD_TempBean> buscar_itens_pedido(String Chave_Venda) {
        List<SqliteVendaD_TempBean> lista_de_itens_vendidos = new ArrayList<SqliteVendaD_TempBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            sql = "select * from PEDITENS WHERE CHAVEPEDIDO = " + Chave_Venda;
            cursor = db.rawQuery(sql, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    SqliteVendaD_TempBean items = new SqliteVendaD_TempBean();
                    items.setVendad_prd_codigoTEMP(cursor.getString(cursor.getColumnIndex("CODITEMANUAL")));
                    items.setVendad_prd_descricaoTEMP(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
                    items.setVendad_prd_unidadeTEMP(cursor.getString(cursor.getColumnIndex("UNIDADE")));
                    items.setVendad_quantidadeTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("QTDMENORPED"))).setScale(2, BigDecimal.ROUND_UP));
                    items.setVendad_preco_vendaTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("VLUNIT"))).setScale(4, BigDecimal.ROUND_UP));
                    items.setVendad_totalTEMP(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("VLTOTAL"))).setScale(2, BigDecimal.ROUND_UP));
                    lista_de_itens_vendidos.add((SqliteVendaD_TempBean) items);
                    insere_item(items);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_itens_pedido" + e.getMessage());
        } finally {
            db.close();
            cursor.close();
        }
        return lista_de_itens_vendidos;
    }


}













