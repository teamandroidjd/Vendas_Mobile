package com.jdsystem.br.vendasmobile.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;


import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JAVA on 28/08/2015.
 */
public class SqliteProdutoDao {

    public static final int DESCRICAO_PRODUTO = 1;
    public static final int CODIGO_PRODUTO = 2;
    public static final int CATEGORIA_PRODUTO = 3;




    private Context ctx;
    private String sql;
    private boolean gravacao;
    private SQLiteStatement stmt;
    private SQLiteDatabase db;
    private Cursor cursor;

    public SqliteProdutoDao(Context ctx)
    {
        this.ctx = ctx;
    }

/*

    public boolean gravar_produto(SqliteProdutoBean produto) {
        gravacao = false;
        try {
            db = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
            ConfigDB.ConectarBanco(db);

            sql = "insert into PRODUTOS (prd_codigo,prd_EAN13,prd_descricao,prd_unmedida,prd_custo,prd_quantidade,prd_preco,prd_categoria)  values (?,?,?,?,?,?,?,?) ";
            stmt = db.compileStatement(sql);
            stmt.bindLong(1, produto.getPrd_codigo());
            stmt.bindString(2, produto.getPrd_EAN13());
            stmt.bindString(3, produto.getPrd_descricao());
            stmt.bindString(4, produto.getPrd_unmedida());
            stmt.bindDouble(5, produto.getPrd_custo().doubleValue());
            stmt.bindDouble(6, produto.getPrd_quantidade().doubleValue());
            stmt.bindDouble(7, produto.getPrd_preco().doubleValue());
            stmt.bindString(8, produto.getPrd_categoria());
            if (stmt.executeInsert() > 0) {
                gravacao = true;
            }

        } catch (SQLiteException e) {
            gravacao = false;
            Util.log("SQLiteException gravar_produto" + e.getMessage());

        } finally {
            db.close();
            stmt.close();
        }
        return gravacao;
    }

    public void atualiza_produto(SqliteProdutoBean produto) {
        try {
            db = new Db(ctx).getWritableDatabase();
            sql = "update PRODUTOS set prd_EAN13=?, prd_descricao=?,prd_unmedida=?,prd_custo=?, prd_quantidade=?,prd_preco=?,prd_categoria=?  where prd_codigo =?  ";
            stmt = db.compileStatement(sql);
            stmt.bindString(1, produto.getPrd_EAN13());
            stmt.bindString(2, produto.getPrd_descricao());
            stmt.bindString(3, produto.getPrd_unmedida());
            stmt.bindDouble(4, produto.getPrd_custo().doubleValue());
            stmt.bindDouble(5, produto.getPrd_quantidade().doubleValue());
            stmt.bindDouble(6, produto.getPrd_preco().doubleValue());
            stmt.bindString(7, produto.getPrd_categoria());
            stmt.bindLong(8, produto.getPrd_codigo());
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Util.log("SQLiteException atualiza_produto" + e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }

    public void atualiza_estoque(SqliteProdutoBean produto) {
        try {
            db = new Db(ctx).getWritableDatabase();
            sql = "update PRODUTOS set prd_quantidade=?  where prd_codigo =?  ";
            stmt = db.compileStatement(sql);
            stmt.bindDouble(1, produto.getPrd_quantidade().doubleValue());
            stmt.bindLong(2, produto.getPrd_codigo());
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Util.log("SQLiteException atualiza_estoque" + e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }*/


    public SqliteProdutoBean buscar_produto_pelo_codigo(String prd_codigo) {
        db = new ConfigDB(ctx).getReadableDatabase();
        SqliteProdutoBean produto = null;
        try {
            cursor = db.rawQuery("select * from ITENS where CODITEMANUAL = ? ", new String[]{prd_codigo});
            if (cursor.moveToFirst()) {
                produto = new SqliteProdutoBean();
                produto.setPrd_codigo(cursor.getString(cursor.getColumnIndex(produto.P_CODIGO_PRODUTO)));
                //produto.setPrd_EAN13(cursor.getString(cursor.getColumnIndex(produto.P_CODIGO_BARRAS)));
                produto.setPrd_descricao(cursor.getString(cursor.getColumnIndex(produto.P_DESCRICAO_PRODUTO)));
                produto.setPrd_unmedida(cursor.getString(cursor.getColumnIndex(produto.P_UNIDADE_MEDIDA)));
                //produto.setPrd_custo(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.P_CUSTO_PRODUTO))));
                //produto.setPrd_quantidade(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.P_QUANTIDADE_PRODUTO))));
                produto.setPrd_preco(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(produto.P_PRECO_PROD_PADRAO))));
                produto.setPrd_categoria(cursor.getString(cursor.getColumnIndex(produto.P_CATEGORIA_PRODUTO)));
                produto.setPrd_status(cursor.getString(cursor.getColumnIndex(produto.P_STATUS_PRODUTO)));
                produto.setPrd_apresentacao(cursor.getString(cursor.getColumnIndex(produto.P_APRESENTACAO_PRODUTO)));
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_produto_pelo_codigo" + e.getMessage());
        } finally {
           db.close();
           cursor.close();
        }
        return produto;
    }

    public List<SqliteProdutoBean> listar_todos_produtos(int Param) {
        List<SqliteProdutoBean> lista_de_produtos = new ArrayList<SqliteProdutoBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        if (Param == 1) {
            try {
                cursor = db.rawQuery("select * from ITENS", null);
                do{
                    SqliteProdutoBean prd = new SqliteProdutoBean();
                    prd.setPrd_codigo(cursor.getString(cursor.getColumnIndex(prd.P_CODIGO_PRODUTO)));
                    //prd.setPrd_EAN13(cursor.getString(cursor.getColumnIndex(prd.P_CODIGO_BARRAS)));
                    prd.setPrd_descricao(cursor.getString(cursor.getColumnIndex(prd.P_DESCRICAO_PRODUTO)));
                    prd.setPrd_unmedida(cursor.getString(cursor.getColumnIndex(prd.P_UNIDADE_MEDIDA)));
                    //prd.setPrd_custo(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_CUSTO_PRODUTO))));
                    //prd.setPrd_quantidade(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_QUANTIDADE_PRODUTO))));
                    prd.setPrd_preco(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_PRECO_PROD_PADRAO))));
                    prd.setPrd_categoria(cursor.getString(cursor.getColumnIndex(prd.P_CATEGORIA_PRODUTO)));
                    prd.setPrd_status(cursor.getString(cursor.getColumnIndex(prd.P_STATUS_PRODUTO)));
                    prd.setPrd_apresentacao(cursor.getString(cursor.getColumnIndex(prd.P_APRESENTACAO_PRODUTO)));
                    lista_de_produtos.add(prd);
                }while (cursor.moveToNext());
            } catch (SQLiteException e) {
                Util.log("SQLiteException listar_todos_produtos" + e.getMessage());
            } finally {
                db.close();
            }


        } else {
            try {
                cursor = db.rawQuery("select * from ITENS where ATIVO = '1' ", null);
                do{
                    SqliteProdutoBean prd = new SqliteProdutoBean();
                    prd.setPrd_codigo(cursor.getString(cursor.getColumnIndex(prd.P_CODIGO_PRODUTO)));
                    //prd.setPrd_EAN13(cursor.getString(cursor.getColumnIndex(prd.P_CODIGO_BARRAS)));
                    prd.setPrd_descricao(cursor.getString(cursor.getColumnIndex(prd.P_DESCRICAO_PRODUTO)));
                    prd.setPrd_unmedida(cursor.getString(cursor.getColumnIndex(prd.P_UNIDADE_MEDIDA)));
                    //prd.setPrd_custo(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_CUSTO_PRODUTO))));
                    //prd.setPrd_quantidade(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_QUANTIDADE_PRODUTO))));
                    prd.setPrd_preco(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(prd.P_PRECO_PROD_PADRAO))));
                    prd.setPrd_categoria(cursor.getString(cursor.getColumnIndex(prd.P_CATEGORIA_PRODUTO)));
                    prd.setPrd_status(cursor.getString(cursor.getColumnIndex(prd.P_STATUS_PRODUTO)));
                    prd.setPrd_apresentacao(cursor.getString(cursor.getColumnIndex(prd.P_APRESENTACAO_PRODUTO)));
                    lista_de_produtos.add(prd);
                }while (cursor.moveToNext());
            } catch (SQLiteException e) {
                Util.log("SQLiteException listar_todos_produtos" + e.getMessage());
            } finally {
                db.close();
            }
        }
        return lista_de_produtos;
    }


    public Cursor buscar_produtos(int Param) {

        SqliteProdutoBean produto = new SqliteProdutoBean();
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            if (Param == 1) {
                cursor = db.rawQuery("select CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                        "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO from ITENS", null);
            } else {
                cursor = db.rawQuery("select CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                        "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO from ITENS where ATIVO = '1' ", null);
            }

            if (cursor != null) {
                cursor.moveToFirst();
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_produtos" + e.getMessage());
        }
        return cursor;
    }


    public Cursor buscar_produto_na_pesquisa_edittext(String valor_campo, int field, int Param) {
        SqliteProdutoBean produto = new SqliteProdutoBean();
        db = new ConfigDB(ctx).getReadableDatabase();
        Cursor cursor = null;
        try {
            if (valor_campo == null || valor_campo.length() == 0) {
                if (Param == 1) {
                    cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                            "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS " , null);
                } else {
                    cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                            "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS WHERE ATIVO = '1'", null);
                }
            } else {
                switch (field) {
                    case DESCRICAO_PRODUTO:
                        if (Param == 1) {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS  WHERE  DESCRICAO LIKE '%" + valor_campo + "%'", null);
                        } else {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS  WHERE ATIVO = '1'" +
                                    " AND DESCRICAO LIKE '%" + valor_campo + "%'", null);

                        }
                        break;
                    case CATEGORIA_PRODUTO:
                        if (Param == 1) {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS  WHERE CLASSE LIKE '%" + valor_campo + "%'", null);
                        } else {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS  WHERE ATIVO = '1'" +
                                    " AND CLASSE LIKE '%" + valor_campo + "%'", null);
                        }
                        break;

                    case CODIGO_PRODUTO:
                        if (Param == 1) {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS " +
                                    " WHERE CODITEMANUAL LIKE '%" + valor_campo + "%'", null);
                        } else {
                            cursor = db.rawQuery("SELECT CODITEMANUAL as _id, CODITEMANUAL, DESCRICAO, UNIVENDA, VENDAPADRAO, CLASSE, " +
                                    "CASE ATIVO WHEN 1 THEN 'ATIVO' WHEN 2 THEN 'INATIVO' END AS ATIVO, APRESENTACAO FROM ITENS  WHERE ATIVO = '1'" +
                                    " AND CODITEMANUAL LIKE '%" + valor_campo + "%'", null);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Util.log("SQLiteException buscar_produto_na_pesquisa_edittext" + e.toString());
        }
        return cursor;
    }


}
