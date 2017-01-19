package com.jdsystem.br.vendasmobile.Pagamento;

import android.content.Context;

import com.jdsystem.br.vendasmobile.Model.SqliteConRecBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConRecDao;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BaixaParcelaEntrada {

    public static void baixar_parcela(SqliteConfPagamentoBean pagamento, SqliteVendaCBean vendaCBean, Context ctx) {

        BigDecimal DIVISOR = new BigDecimal(pagamento.getConf_parcelas());
        BigDecimal VALOR_DA_PARCELA = vendaCBean.getTotal().divide(DIVISOR, BigDecimal.ROUND_HALF_UP);
        BigDecimal VALOR_DA_ENTRADA = pagamento.getConf_valor_recebido();
        SqliteConRecBean primeiraParcela = new SqliteConRecDao(ctx).busca_menor_parcela(vendaCBean.getVendac_chave());

        Calendar calendar = Calendar.getInstance(new Locale("pt", "BR"));
        Date data_pagamento = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // VERIFICA VAOR ENTRADA IGUAL VALOR PARCELA
        if (VALOR_DA_ENTRADA.doubleValue() == VALOR_DA_PARCELA.doubleValue()) {

            new SqliteConRecDao(ctx).baixar_parcela_cliente_integral(new SqliteConRecBean(
                    VALOR_DA_PARCELA.setScale(2, BigDecimal.ROUND_UP),
                    dateFormat.format(data_pagamento),
                    pagamento.getConf_recebeucom_din_chq_car(),
                    "N",
                    primeiraParcela.getVendac_chave(),
                    primeiraParcela.getRec_numparcela()
            ));
        }


        // VERIFICA VALOR ENTRADA MENOR DO QUE O VALOR PARCELA
        if (VALOR_DA_ENTRADA.doubleValue() < VALOR_DA_PARCELA.doubleValue()) {
            SqliteConRecBean parcela_parcial = new SqliteConRecBean();
            parcela_parcial.setRec_valor_receber(primeiraParcela.getRec_valor_receber().subtract(VALOR_DA_ENTRADA));
            parcela_parcial.setRec_enviado("N");
            parcela_parcial.setVendac_chave(primeiraParcela.getVendac_chave());
            parcela_parcial.setRec_numparcela(primeiraParcela.getRec_numparcela());
            new SqliteConRecDao(ctx).atualiza_valorparcela(parcela_parcial);
        }

        //VERIFICA VALOR ENTRADA MAIOR QUE O VALOR PARCELA
        if (VALOR_DA_ENTRADA.doubleValue() > VALOR_DA_PARCELA.doubleValue()) {

            List<SqliteConRecBean> lista_de_parcela = new SqliteConRecDao(ctx).busca_parcelas_geradas_na_compra(vendaCBean.getVendac_chave());
            int posicao = 0;

            while (VALOR_DA_ENTRADA.doubleValue() >= VALOR_DA_PARCELA.doubleValue()) {
                BigDecimal VALOR_A_RECEBER = lista_de_parcela.get(posicao).getRec_valor_receber();

                if (VALOR_DA_ENTRADA.doubleValue() >= VALOR_A_RECEBER.doubleValue()) {

                    new SqliteConRecDao(ctx).baixar_parcela_cliente_integral(new SqliteConRecBean(
                            VALOR_DA_PARCELA.setScale(2, BigDecimal.ROUND_UP),
                            dateFormat.format(data_pagamento),
                            pagamento.getConf_recebeucom_din_chq_car(),
                            "N",
                            lista_de_parcela.get(posicao).getVendac_chave(),
                            lista_de_parcela.get(posicao).getRec_numparcela()
                    ));
                    VALOR_DA_ENTRADA = VALOR_DA_ENTRADA.subtract(VALOR_A_RECEBER);

                }

                if (VALOR_DA_ENTRADA.doubleValue() < VALOR_A_RECEBER.doubleValue()) {

                    SqliteConRecBean parcela_menor = new SqliteConRecDao(ctx).busca_menor_parcela(vendaCBean.getVendac_chave());
                    SqliteConRecBean parcela_parcial = new SqliteConRecBean();
                    parcela_parcial.setRec_valor_receber(parcela_menor.getRec_valor_receber().subtract(VALOR_DA_ENTRADA));
                    parcela_parcial.setRec_enviado("N");
                    parcela_parcial.setVendac_chave(parcela_menor.getVendac_chave());
                    parcela_parcial.setRec_numparcela(parcela_menor.getRec_numparcela());
                    new SqliteConRecDao(ctx).atualiza_valorparcela(parcela_parcial);
                    VALOR_DA_ENTRADA = BigDecimal.ZERO;

                }

                posicao++;
            }


        }

    }

}
