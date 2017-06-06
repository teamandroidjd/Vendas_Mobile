package com.jdsystem.br.vendasmobile.Pagamento;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.Model.SqliteConRecBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConRecDao;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.interfaces.iPagamento;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


public class Mensal implements iPagamento {


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public void gerar_parcela(SqliteConfPagamentoBean pagamento, SqliteVendaCBean vendaCBean, Context ctx,Boolean AtuPedido) {
        if(AtuPedido) {

            String chavePedido = vendaCBean.getVendac_chave();
            new SqliteConRecDao(ctx).excluir_Parcela_Chave(chavePedido);

            Calendar calendar_default = Calendar.getInstance(new Locale("pt" , "BR"));
            calendar_default.set(Calendar.YEAR, 2000);
            calendar_default.set(Calendar.MONTH, Calendar.JANUARY);
            calendar_default.set(Calendar.DAY_OF_MONTH, 01);
            Date data_padrao = calendar_default.getTime();

            try {
                final String CONFIG_HOST = "CONFIG_HOST";
                SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
                int idPerfil = prefs.getInt("idperfil" , 0);
                SQLiteDatabase DB;
                DB = new ConfigDB(ctx).getReadableDatabase();
                Cursor cursorconfpagamento = DB.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '" + pagamento.getVendac_chave() + "' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {

                    Calendar calendar_1 = Calendar.getInstance(new Locale("pt" , "BR"));
                    do {
                        int parcela = cursorconfpagamento.getInt(cursorconfpagamento.getColumnIndex("conf_parcelas"));
                        String codformpgto = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_CODFORMPGTO_EXT"));
                        String vlparcela = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                        BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.'))).setScale(2, BigDecimal.ROUND_HALF_UP);
                        String diasvencimento = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_DIAS_VENCIMENTO"));
                        String dataVencimento = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_DATA_VENCIMENTO"));
                        String descformpgto = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_descricao_formpgto"));


                        calendar_1.add(Calendar.MONTH, 1);

                        if (calendar_1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            calendar_1.add(Calendar.DATE, 2);
                        }

                        if (calendar_1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            calendar_1.add(Calendar.DATE, 1);
                        }

                        Date data_de_vencimento = calendar_1.getTime();

                        new SqliteConRecDao(ctx).gravar_parcela(
                                new SqliteConRecBean(
                                        parcela,
                                        vendaCBean.getVendac_cli_codigo(),
                                        vendaCBean.getVendac_cli_nome(),
                                        vendaCBean.getVendac_chave(),
                                        Util.DataHojeSemHorasBR(),
                                        valorparcela,
                                        BigDecimal.ZERO,
                                        dataVencimento,
                                        "" ,
                                        "" ,
                                        "N" ,
                                        codformpgto,
                                        diasvencimento,
                                        descformpgto,
                                        idPerfil
                                ));

                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();
                }
            } catch (Exception e) {
                e.toString();

            }
        } else {
            String chavePedido = vendaCBean.getVendac_chave();
            new SqliteConRecDao(ctx).excluir_Parcela_Chave(chavePedido);

            Calendar calendar_default = Calendar.getInstance(new Locale("pt" , "BR"));
            calendar_default.set(Calendar.YEAR, 2000);
            calendar_default.set(Calendar.MONTH, Calendar.JANUARY);
            calendar_default.set(Calendar.DAY_OF_MONTH, 01);
            Date data_padrao = calendar_default.getTime();

            try {
                final String CONFIG_HOST = "CONFIG_HOST";
                SharedPreferences prefs = ctx.getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
                int idPerfil = prefs.getInt("idperfil" , 0);
                SQLiteDatabase DB;
                DB = new ConfigDB(ctx).getReadableDatabase();
                Cursor cursorconfpagamento = DB.rawQuery("SELECT * FROM CONFPAGAMENTO WHERE vendac_chave = '" + pagamento.getVendac_chave() + "' AND CODPERFIL = " + idPerfil, null);
                cursorconfpagamento.moveToFirst();
                if (cursorconfpagamento.getCount() > 0) {

                    Calendar calendar_1 = Calendar.getInstance(new Locale("pt" , "BR"));
                    do {
                        int parcela = cursorconfpagamento.getInt(cursorconfpagamento.getColumnIndex("conf_parcelas"));
                        String codformpgto = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_CODFORMPGTO_EXT"));
                        String vlparcela = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_valor_recebido"));
                        BigDecimal valorparcela = new BigDecimal(Double.parseDouble(vlparcela.replace(',', '.'))).setScale(2, BigDecimal.ROUND_HALF_UP);
                        String diasvencimento = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_DIAS_VENCIMENTO"));
                        String dataVencimento = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("CONF_DATA_VENCIMENTO"));
                        String descformpgto = cursorconfpagamento.getString(cursorconfpagamento.getColumnIndex("conf_descricao_formpgto"));


                        calendar_1.add(Calendar.MONTH, 1);

                        if (calendar_1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            calendar_1.add(Calendar.DATE, 2);
                        }

                        if (calendar_1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            calendar_1.add(Calendar.DATE, 1);
                        }

                        Date data_de_vencimento = calendar_1.getTime();

                        new SqliteConRecDao(ctx).gravar_parcela(
                                new SqliteConRecBean(
                                        parcela,
                                        vendaCBean.getVendac_cli_codigo(),
                                        vendaCBean.getVendac_cli_nome(),
                                        vendaCBean.getVendac_chave(),
                                        Util.DataHojeSemHorasBR(),
                                        valorparcela,
                                        BigDecimal.ZERO,
                                        dataVencimento,
                                        "" ,
                                        "" ,
                                        "N" ,
                                        codformpgto,
                                        diasvencimento,
                                        descformpgto,
                                        idPerfil
                                ));

                    } while (cursorconfpagamento.moveToNext());
                    cursorconfpagamento.close();
                }
            } catch (Exception e) {
                e.toString();

            }
        }
    }
}

