package com.jdsystem.br.vendasmobile.interfaces;


import android.content.Context;

import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;

public interface iPagamento {
    void gerar_parcela(SqliteConfPagamentoBean pagamento, SqliteVendaCBean vendaCBean, Context ctx);
}
