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

        //BigDecimal PERCENTUAL_DESCONTO = new BigDecimal(new SqliteParametroDao(ctx).busca_parametros().getP_desconto_do_vendedor());
        //BigDecimal PERCENTUAL_DESCONTO = BigDecimal.valueOf(0);
        BigDecimal teste = vendaCBean.getVendac_desconto();
        BigDecimal VALOR_DESCONTO = vendaCBean.getVendac_valor();//PERCENTUAL_DESCONTO.multiply(vendaCBean.getTotal()).divide(new BigDecimal(100));
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


    }


}
