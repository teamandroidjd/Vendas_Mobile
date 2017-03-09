package com.jdsystem.br.vendasmobile.Model;

import android.content.ContentValues;
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


public class Sqlite_VENDADAO {

    private Context ctx;
    private String CodVend;
    private String sql;
    private SQLiteStatement stmt;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Boolean bAltera;


    public Sqlite_VENDADAO(Context ctx, String CodVend, Boolean bAltera) {

        this.ctx = ctx;
        this.CodVend = CodVend;
        this.bAltera = bAltera;
    }

    public Long grava_venda(SqliteVendaCBean venda, List<SqliteVendaD_TempBean> itens_temp) {

        int numero_item = 1;
        for (SqliteVendaD_TempBean item_transf : itens_temp) {
            SqliteVendaDBean vendaDBean = new SqliteVendaDBean();
            vendaDBean.setVendac_chave(venda.getVendac_chave());
            vendaDBean.setVendad_nro_item(numero_item);
            vendaDBean.setVendad_ean(item_transf.getVendad_eanTEMP());
            vendaDBean.setVendad_prd_codigo(item_transf.getVendad_prd_codigoTEMP());
            vendaDBean.setVendad_prd_descricao(item_transf.getVendad_prd_descricaoTEMP());
            vendaDBean.setVendad_quantidade(item_transf.getVendad_quantidadeTEMP());
            vendaDBean.setVendad_prd_unidade(item_transf.getVendad_prd_unidadeTEMP());
            vendaDBean.setVendad_preco_venda(item_transf.getVendad_preco_vendaTEMP());
            vendaDBean.setVendad_total(item_transf.getSubTotal());
            venda.getItens_da_venda().add(vendaDBean);
            new SqliteVendaD_TempDao(ctx).excluir_um_item_da_venda(item_transf);
            numero_item++;
        }
        // aqui comeca a gravacao das 2 tabelas pedoper e peditens

        long id_venda = -1;
        db = new ConfigDB(ctx).getWritableDatabase();

        try {
            if (bAltera.equals(true)) {
                db.execSQL(" UPDATE PEDOPER SET CHAVE_PEDIDO = '" + venda.getVendac_chave().toString() +
                        "', DATAEMIS  = '" + venda.getVendac_datahoravenda().toString() +
                        "', DATAPREVISTAENTREGA = '" + venda.getVendac_previsaoentrega().toString() +
                        "', CODCLIE = " + venda.getVendac_cli_codigo().toString() +
                        ", CODCLIE_EXT = " + venda.getVendac_cli_codigo_ext().toString() +
                        ", NOMECLIE = '" + venda.getVendac_cli_nome().toString() +
                        "', CODEMPRESA = " + venda.getCodEmpresa().toString() +
                        ", CODVENDEDOR = " + CodVend +
                        ", VLMERCAD = " + venda.getTotal().toString() +
                        ", VALORTOTAL = " + venda.getTotal().toString() +
                        ", VLDESCONTO = " + venda.getVendac_desconto().toString() +
                        ", STATUS = '" + venda.getVendac_enviada().toString() +
                        "', LATITUDEPEDIDO = " + venda.getVendac_latitude() +
                        ", OBS = '" + venda.getObservacao().toString() +
                        "', FLAGINTEGRADO = '" + 1 +
                        "', LONGITUDEPEDIDO = " + venda.getVendac_longitude() +
                        " WHERE CHAVE_PEDIDO = '" + venda.getVendac_chave().toString() + "'");
            } else {
                db.execSQL("INSERT INTO PEDOPER (CHAVE_PEDIDO, DATAEMIS, DATAPREVISTAENTREGA, CODCLIE, CODCLIE_EXT, NOMECLIE, CODEMPRESA, CODVENDEDOR, " +
                        "VALORTOTAL, VLDESCONTO, STATUS, LATITUDEPEDIDO, OBS, FLAGINTEGRADO, LONGITUDEPEDIDO, VLMERCAD) VALUES(" +
                        "'" + venda.getVendac_chave().toString() + "','" + venda.getVendac_datahoravenda().toString() + "','" + venda.getVendac_previsaoentrega().toString() +
                        "'," + venda.getVendac_cli_codigo().toString() + ",' " + venda.getVendac_cli_codigo_ext().toString().toString()
                        + "',' " + venda.getVendac_cli_nome().toString() +
                        "'," + venda.getCodEmpresa().toString() + ", '" + CodVend + "'," + venda.getVendac_valor().toString() +
                        "," + venda.getVendac_desconto().toString() + ", '" + venda.getVendac_enviada().toString() +
                        "'," + venda.getVendac_latitude() + ",'" + venda.getObservacao().toString() + "','1'," + venda.getVendac_longitude() +
                        "," + venda.getTotal().toString() + ");");
            }
            id_venda = 1;
        } catch (Exception E) {
            System.out.println("Error" + E.toString());
            id_venda = -1;
        }
            /*ContentValues vendaC = new ContentValues();
            vendaC.put(venda.CHAVE_DA_VENDA, venda.getVendac_chave());
            vendaC.put(venda.DATA_HORA_DA_VENDA, venda.getVendac_datahoravenda());
            vendaC.put(venda.PREVISAO_ENTREGA, venda.getVendac_previsaoentrega());
            vendaC.put(venda.CODIGO_DO_CLIENTE, venda.getVendac_cli_codigo());
            vendaC.put(venda.CODIGOCLIE_EXT, venda.getVendac_cli_codigo_ext());
            vendaC.put(venda.NOME_DO_CLIENTE, venda.getVendac_cli_nome());
            vendaC.put(venda.CODEMPRESA, venda.getCodEmpresa());
            vendaC.put(venda.CODIGO_DO_USUARIO_VENDEDOR, CodVend);
            vendaC.put(venda.VLMERCADORIA, venda.getTotal().toString());
            vendaC.put(venda.FORMA_DE_PAGAMENTO, venda.getVendac_formapgto());
            vendaC.put(venda.VALOR_DA_VENDA, venda.getTotal().toString());
            vendaC.put(venda.DESCONTO, venda.getVendac_desconto().toString());
            vendaC.put(venda.VENDA_ENVIADA_SERVIDOR, venda.getVendac_enviada());
            vendaC.put(venda.LATITUDE, venda.getVendac_latitude());
            vendaC.put(venda.CODIGO_DA_VENDA, venda.getVendac_id());
            vendaC.put(venda.OBSERVACAO, venda.getObservacao());
            vendaC.put(venda.INTEGRADO, "1");
            vendaC.put(venda.LONGITUDE, venda.getVendac_longitude());

            id_venda = db.insert("PEDOPER", null, vendaC);*/

        if (id_venda != -1) {
            boolean Erro = false;
            try {
                for (int i = 0; i < venda.getItens_da_venda().size(); i++) {
                    SqliteVendaDBean vendaD_item = (SqliteVendaDBean) venda.getItens_da_venda().get(i);
                    Cursor CursorItem = db.rawQuery(" SELECT DESCRICAO, CHAVEPEDIDO FROM PEDITENS WHERE CODITEMANUAL = '" + vendaD_item.getVendad_prd_codigo().toString().trim()
                            + "' AND CHAVEPEDIDO = '" + vendaD_item.getVendac_chave() + "'", null);
                    if (CursorItem.getCount() > 0) {
                        db.execSQL(" UPDATE PEDITENS SET CHAVEPEDIDO = '" + vendaD_item.getVendac_chave().toString() +
                                "', NUMEROITEM = '" + vendaD_item.getVendad_nro_item().toString() +
                                "', CODITEMANUAL = '" + vendaD_item.getVendad_prd_codigo().toString() +
                                "', DESCRICAO = '" + vendaD_item.getVendad_prd_descricao().toString() +
                                "', UNIDADE = '" + vendaD_item.getVendad_prd_unidade().toString() +
                                "', QTDMENORPED = " + vendaD_item.getVendad_quantidade().toString() +
                                ", VLUNIT = " + vendaD_item.getVendad_preco_venda().setScale(4, BigDecimal.ROUND_UP).doubleValue() +
                                ", VLTOTAL = " + vendaD_item.getSubTotal().setScale(2, BigDecimal.ROUND_UP).doubleValue() +
                                " WHERE CODITEMANUAL = '" + vendaD_item.getVendad_prd_codigo().toString() + "'");
                    } else {
                        db.execSQL(" INSERT INTO PEDITENS (CHAVEPEDIDO, NUMEROITEM, CODITEMANUAL, DESCRICAO, UNIDADE, QTDMENORPED, VLUNIT, VLTOTAL)" +
                                " VALUES('" + vendaD_item.getVendac_chave() + "','" + vendaD_item.getVendad_nro_item() +
                                "', '" + vendaD_item.getVendad_prd_codigo() + "','" + vendaD_item.getVendad_prd_descricao()
                                + "','" + vendaD_item.getVendad_prd_unidade() + "'," + vendaD_item.getVendad_quantidade().toString() +
                                "," + vendaD_item.getVendad_preco_venda().setScale(4, BigDecimal.ROUND_UP).doubleValue() + "," +
                                vendaD_item.getSubTotal().setScale(2, BigDecimal.ROUND_UP).doubleValue() + ");");
                    }
                    CursorItem.close();
                }

                    /*SqliteVendaDBean vendaD_item = (SqliteVendaDBean) venda.getItens_da_venda().get(i);
                    ContentValues vendaD = new ContentValues();
                    vendaD.put(vendaD_item.CHAVE_DA_VENDA, vendaD_item.getVendac_chave());
                    //vendaD.put(vendaD_item.NUMPED, vendaD_item.getVendac_prd_numped());
                    vendaD.put(vendaD_item.NUMERO_ITEM, vendaD_item.getVendad_nro_item());
                    //vendaD.put(vendaD_item.EAN, vendaD_item.getVendad_ean());
                    vendaD.put(vendaD_item.CODPRODUTO, vendaD_item.getVendad_prd_codigo());
                    vendaD.put(vendaD_item.DESCRICAOPROD, vendaD_item.getVendad_prd_descricao());
                    vendaD.put(vendaD_item.UNIDADEPROD, vendaD_item.getVendad_prd_unidade());
                    vendaD.put(vendaD_item.QUANTVENDIDA, vendaD_item.getVendad_quantidade().toString());
                    vendaD.put(vendaD_item.PRECOPRODUTO, vendaD_item.getVendad_preco_venda().setScale(4, BigDecimal.ROUND_UP).doubleValue());
                    vendaD.put(vendaD_item.TOTALPRODUTO, vendaD_item.getSubTotal().setScale(2, BigDecimal.ROUND_UP).doubleValue());

                    if (db.insert("PEDITENS", null, vendaD) == -1) {
                        Erro = true;
                        break;
                    }
                }
                if (!Erro) {
                    db.setTransactionSuccessful();
                }*/

            } catch (SQLiteException e) {
                Util.log("SQLiteException grava_venda" + e.getMessage());
            } /*finally {
                db.endTransaction();
                db.close();*/
        }
        return id_venda;

    }


    public List<SqliteVendaCBean> lista_pedidos_do_cliente(Integer cli_codigo) {
        List<SqliteVendaCBean> lista_registros_vendaC = new ArrayList<SqliteVendaCBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from PEDOPER where CODCLIE = ? order by DATAEMIS desc ", new String[]{cli_codigo.toString()});
            while (cursor.moveToNext()) {

                SqliteVendaCBean vendac = new SqliteVendaCBean();
                vendac.setVendac_chave(cursor.getString(cursor.getColumnIndex(vendac.CHAVE_DA_VENDA)));
                vendac.setVendac_datahoravenda(cursor.getString(cursor.getColumnIndex(vendac.DATA_HORA_DA_VENDA)));
                vendac.setVendac_previsaoentrega(cursor.getString(cursor.getColumnIndex(vendac.PREVISAO_ENTREGA)));
                vendac.setVendac_cli_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_CLIENTE)));
                vendac.setVendac_cli_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_CLIENTE)));
                vendac.setVendac_usu_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_USUARIO_VENDEDOR)));
                //vendac.setVendac_usu_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_USUARIO_VENDEDOR)));
                vendac.setVendac_formapgto(cursor.getString(cursor.getColumnIndex(vendac.FORMA_DE_PAGAMENTO)));
                vendac.setVendac_valor(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.VALOR_DA_VENDA))).setScale(4, BigDecimal.ROUND_UP));
                vendac.setVendac_desconto(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.DESCONTO))));
                //vendac.setVendac_pesototal(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.PESO_TOTAL_DOS_PRODUTOS))));
                vendac.setVendac_enviada(cursor.getString(cursor.getColumnIndex(vendac.VENDA_ENVIADA_SERVIDOR)));
                vendac.setVendac_latitude(cursor.getDouble(cursor.getColumnIndex(vendac.LATITUDE)));
                vendac.setVendac_longitude(cursor.getDouble(cursor.getColumnIndex(vendac.LONGITUDE)));
                vendac.setObservacao(cursor.getString(cursor.getColumnIndex(vendac.OBSERVACAO)));

                lista_registros_vendaC.add(vendac);
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_vendas_nao_enviadas" + e.getMessage());
        } finally {
            db.close();
        }
        return lista_registros_vendaC;
    }


    public List<SqliteVendaCBean> buscar_vendas_nao_enviadas() {
        List<SqliteVendaCBean> lista_registros_vendaC = new ArrayList<SqliteVendaCBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from PEDOPER where STATUS = '1'", null);
            while (cursor.moveToNext()) {

                SqliteVendaCBean vendac = new SqliteVendaCBean();
                vendac.setVendac_chave(cursor.getString(cursor.getColumnIndex(vendac.CHAVE_DA_VENDA)));
                vendac.setVendac_datahoravenda(cursor.getString(cursor.getColumnIndex(vendac.DATA_HORA_DA_VENDA)));
                vendac.setVendac_previsaoentrega(cursor.getString(cursor.getColumnIndex(vendac.PREVISAO_ENTREGA)));
                vendac.setVendac_cli_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_CLIENTE)));
                vendac.setVendac_cli_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_CLIENTE)));
                vendac.setVendac_usu_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_USUARIO_VENDEDOR)));
                //vendac.setVendac_usu_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_USUARIO_VENDEDOR)));
                vendac.setVendac_formapgto(cursor.getString(cursor.getColumnIndex(vendac.FORMA_DE_PAGAMENTO)));
                vendac.setVendac_valor(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.VALOR_DA_VENDA))).setScale(2, BigDecimal.ROUND_UP));
                vendac.setVendac_desconto(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.DESCONTO))));
                //vendac.setVendac_pesototal(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.PESO_TOTAL_DOS_PRODUTOS))));
                vendac.setVendac_enviada(cursor.getString(cursor.getColumnIndex(vendac.VENDA_ENVIADA_SERVIDOR)));
                vendac.setVendac_latitude(cursor.getDouble(cursor.getColumnIndex(vendac.LATITUDE)));
                vendac.setVendac_longitude(cursor.getDouble(cursor.getColumnIndex(vendac.LONGITUDE)));
                vendac.setObservacao(cursor.getString(cursor.getColumnIndex(vendac.OBSERVACAO)));
                lista_registros_vendaC.add(vendac);
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_vendas_nao_enviadas" + e.getMessage());
        } finally {
            db.close();
        }
        return lista_registros_vendaC;
    }

    public SqliteVendaCBean buscar_vendas_por_numeropedido(String NumPedido) {
        List<SqliteVendaCBean> lista_registros_vendaC = new ArrayList<SqliteVendaCBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        SqliteVendaCBean vendac = new SqliteVendaCBean();
        try {
            cursor = db.rawQuery("select * from PEDOPER where NUMPED = '" + NumPedido + "'", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    vendac.setVendac_chave(cursor.getString(cursor.getColumnIndex(vendac.CHAVE_DA_VENDA)));
                    vendac.setVendac_datahoravenda(cursor.getString(cursor.getColumnIndex(vendac.DATA_HORA_DA_VENDA)));
                    vendac.setVendac_previsaoentrega(cursor.getString(cursor.getColumnIndex(vendac.PREVISAO_ENTREGA)));
                    vendac.setVendac_cli_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_CLIENTE)));
                    vendac.setVendac_cli_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_CLIENTE)));
                    vendac.setVendac_usu_codigo(cursor.getInt(cursor.getColumnIndex(vendac.CODIGO_DO_USUARIO_VENDEDOR)));
                    vendac.setCodEmpresa(cursor.getString(cursor.getColumnIndex(vendac.CODEMPRESA)));
                    vendac.setCodVendedor(cursor.getString(cursor.getColumnIndex(vendac.CODIGO_DO_USUARIO_VENDEDOR)));
                    //vendac.setVendac_usu_nome(cursor.getString(cursor.getColumnIndex(vendac.NOME_DO_USUARIO_VENDEDOR)));
                    vendac.setVendac_formapgto(cursor.getString(cursor.getColumnIndex(vendac.FORMA_DE_PAGAMENTO)));
                    vendac.setVendac_valor(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.VALOR_DA_VENDA))).setScale(2, BigDecimal.ROUND_UP));
                    vendac.setVendac_desconto(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.DESCONTO))));
                    //vendac.setVendac_pesototal(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendac.PESO_TOTAL_DOS_PRODUTOS))));
                    vendac.setVendac_enviada(cursor.getString(cursor.getColumnIndex(vendac.VENDA_ENVIADA_SERVIDOR)));
                    vendac.setVendac_latitude(cursor.getDouble(cursor.getColumnIndex(vendac.LATITUDE)));
                    vendac.setVendac_longitude(cursor.getDouble(cursor.getColumnIndex(vendac.LONGITUDE)));
                    vendac.setObservacao(cursor.getString(cursor.getColumnIndex(vendac.OBSERVACAO)));
                    lista_registros_vendaC.add(vendac);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_vendas_nao_enviadas" + e.getMessage());
        } finally {
            db.close();
        }
        return vendac;
    }

    public void excluir_um_item_da_venda(SqliteVendaDBean item) {
        db = new ConfigDB(ctx).getWritableDatabase();
        try {
            db.delete("PEDITENS", "CODITEMANUAL = ?", new String[]{item.getVendad_prd_codigo().toString()});
        } catch (SQLiteException e) {
            Util.log("SQLiteException excluir_um_item_da_venda" + e.getMessage());
        } finally {
            db.close();
        }
    }

    public List<SqliteVendaDBean> buscar_itens_vendas_por_numeropedido(String Chave_Venda) {
        List<SqliteVendaDBean> lista_registros_vendaD = new ArrayList<SqliteVendaDBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        SqliteVendaDBean vendaD = new SqliteVendaDBean();
        try {
            if (!Chave_Venda.equals("0")) {
                cursor = db.rawQuery("select * from PEDITENS where CHAVEPEDIDO = '" + Chave_Venda + "'", null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        vendaD.setVendad_prd_codigo(cursor.getString(cursor.getColumnIndex("CODITEMANUAL")));
                        vendaD.setVendad_prd_descricao(cursor.getString(cursor.getColumnIndex("DESCRICAO")));
                        vendaD.setVendad_prd_unidade(cursor.getString(cursor.getColumnIndex("UNIDADE")));
                        vendaD.setVendad_quantidade(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("QTDMENORPED"))).setScale(2, BigDecimal.ROUND_UP));
                        vendaD.setVendad_preco_venda(new BigDecimal(cursor.getString(cursor.getColumnIndex("VLUNIT"))).setScale(4, BigDecimal.ROUND_UP));
                        vendaD.setVendad_total(new BigDecimal(cursor.getString(cursor.getColumnIndex("VLTOTAL"))).setScale(2, BigDecimal.ROUND_UP));
                        lista_registros_vendaD.add(vendaD);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Util.log("SQLiteException buscar_vendas_nao_enviadas" + e.getMessage());
        } finally {
            db.close();
        }
        return lista_registros_vendaD;
    }

    // select em todos os itens das vendas que ainda nao foram exportadas
    public List<SqliteVendaDBean> buscar_itens_das_vendas_nao_enviadas() {
        List<SqliteVendaDBean> lista_registros_vendaD = new ArrayList<SqliteVendaDBean>();
        db = new ConfigDB(ctx).getReadableDatabase();
        try { //STATUS 1=ORÇAMENTO 2=ENVIADO 3=FATURADO
            cursor = db.rawQuery("select * from PEDITENS where CHAVEPEDIDO = (select CHAVEPEDIDO from PEDOPER where  STATUS = '2')", null);
            while (cursor.moveToNext()) {

                SqliteVendaDBean vendad = new SqliteVendaDBean();

                vendad.setVendac_chave(cursor.getString(cursor.getColumnIndex(vendad.CHAVE_DA_VENDA)));
                vendad.setVendad_nro_item(cursor.getInt(cursor.getColumnIndex(vendad.NUMERO_ITEM)));
                vendad.setVendad_prd_unidade(cursor.getString(cursor.getColumnIndex(vendad.UNIDADEPROD)));
                vendad.setVendad_prd_codigo(cursor.getString(cursor.getColumnIndex(vendad.CODPRODUTO)));
                vendad.setVendad_prd_descricao(cursor.getString(cursor.getColumnIndex(vendad.DESCRICAOPROD)));
                vendad.setVendad_quantidade(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendad.QUANTVENDIDA))));
                vendad.setVendad_preco_venda(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendad.PRECOPRODUTO))).setScale(4, BigDecimal.ROUND_UP));
                vendad.setVendad_total(new BigDecimal(cursor.getDouble(cursor.getColumnIndex(vendad.TOTALPRODUTO))));
                lista_registros_vendaD.add(vendad);
            }
        } catch (SQLiteException e) {
            Util.log("SQLiteException buscar_itens_das_vendas_nao_enviadas" + e.getMessage());
        } finally {
            db.close();
        }
        return lista_registros_vendaD;
    }


    public void atualiza_vendac_enviada(String vendac_chave) {
        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            sql = "update PEDOPER set STATUS = '3' where CHAVEPEDIDO = ?   ";

            stmt = db.compileStatement(sql);
            stmt.bindString(1, vendac_chave);
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Util.log("SQLiteException atualiza_cliente" + e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }

    public void atualiza_cli_codigo_cliente_offline(int NOVO_CODIGO, int CHAVE_ENVIADA_MYSQL) {
        try {
            db = new ConfigDB(ctx).getWritableDatabase();
            sql = "update PEDOPER set CODCLIE = (select CODCLIE_INT from clientes where CODCLIE_INT = ? ) where CODCLIE = ? ";

            stmt = db.compileStatement(sql);
            stmt.bindLong(1, NOVO_CODIGO);
            stmt.bindLong(2, CHAVE_ENVIADA_MYSQL);
            stmt.executeUpdateDelete();
            stmt.clearBindings();
        } catch (SQLiteException e) {
            Util.log("SQLiteException atualiza_cli_codigo_cliente_offline" + e.getMessage());
        } finally {
            db.close();
            stmt.close();
        }
    }


}
