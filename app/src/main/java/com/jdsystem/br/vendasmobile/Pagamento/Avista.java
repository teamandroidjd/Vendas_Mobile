package com.jdsystem.br.vendasmobile.Pagamento;

import android.content.Context;
import android.util.Log;

import com.jdsystem.br.vendasmobile.Model.SqliteConRecBean;
import com.jdsystem.br.vendasmobile.Model.SqliteConRecDao;
import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteParametroDao;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.interfaces.iPagamento;

import java.math.BigDecimal;


public class Avista implements iPagamento {


    @Override
    public void gerar_parcela(SqliteConfPagamentoBean pagamento, SqliteVendaCBean vendaCBean, Context ctx) {

        BigDecimal PERCENTUAL_DESCONTO = new BigDecimal(new SqliteParametroDao(ctx).busca_parametros().getP_desconto_do_vendedor());
        //BigDecimal PERCENTUAL_DESCONTO = BigDecimal.valueOf(0);
        BigDecimal VALOR_DESCONTO = PERCENTUAL_DESCONTO.multiply(vendaCBean.getTotal()).divide(new BigDecimal(100));
        BigDecimal VALOR_COM_DESCONTO = vendaCBean.getTotal().subtract(VALOR_DESCONTO);

        new SqliteConRecDao(ctx).gravar_parcela(
                new SqliteConRecBean(
                        1,
                        vendaCBean.getVendac_cli_codigo(),
                        vendaCBean.getVendac_cli_nome(),
                        vendaCBean.getVendac_chave(),
                        Util.DataHojeSemHorasUSA(),
                        vendaCBean.getTotal().setScale(2, BigDecimal.ROUND_UP),
                        VALOR_COM_DESCONTO.setScale(2, BigDecimal.ROUND_UP),
                        Util.DataHojeSemHorasUSA(),
                        Util.DataHojeSemHorasUSA(),
                        pagamento.getConf_recebeucom_din_chq_car(),
                        "N"
                ));

        Log.i("script", "+++++++++++++++++++++++++++++");
        Log.i("script", "+++++++++++++++++++++++++++++");
        Log.i("script", "Numero da Parcela : " + 1);
        Log.i("script", "Codigo do cliente : " + vendaCBean.getVendac_cli_codigo());
        Log.i("script", "Nome do cliente : " + vendaCBean.getVendac_cli_nome());
        Log.i("script", "Chave da venda : " + vendaCBean.getVendac_chave());
        Log.i("script", "Data do movimento : " + Util.DataHojeSemHorasUSA());
        Log.i("script", "Valor a receber : " + vendaCBean.getTotal().setScale(2, BigDecimal.ROUND_UP).toString());
        Log.i("script", "Data de vencimento : " + Util.DataHojeSemHorasUSA());
        Log.i("script", "Data do pagamento : " + Util.DataHojeSemHorasUSA());
        Log.i("script", "Valor Pago : " + VALOR_COM_DESCONTO.setScale(2, BigDecimal.ROUND_UP).toString());
        Log.i("script", "Como recebeu : " + "Venda Avista recebida em " + pagamento.getConf_recebeucom_din_chq_car() + " com desc de " + PERCENTUAL_DESCONTO + "%");
        Log.i("script", "Enviado : " + "N");
        Log.i("script", "+++++++ REGISTRO GRAVADO COM SUCESSO ++++++");



    }


}
