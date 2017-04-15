package com.jdsystem.br.vendasmobile.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaProdutos;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.Model.Sqlite_VENDADAO;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterProdutos;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.DadosProduto;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class FragmentProdutos extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<Produtos> mList;
    private int flag, sprecoprincipal;
    private String numPedido, chavePedido, usuario, senha, codVendedor, urlprincipal, tab1, tab2, tab3, tab4, tab5, tab6, tab7;
    SQLiteDatabase DB;
    private Spinner spntabpreco;
    private String PREFS_PRIVATE = "PREFS_PRIVATE";
    private SharedPreferences prefs;
    private ListView prod_listview_itenstemp;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_produtos, container, false);

            Bundle params = getArguments();
            if (params != null) {
                flag = params.getInt(getString(R.string.intent_flag));
                numPedido = params.getString(getString(R.string.intent_numpedido));
                chavePedido = params.getString(getString(R.string.intent_chavepedido));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                urlprincipal = params.getString(getString(R.string.intent_urlprincipal));
            }

            mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_Prod);
            mRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);

            mList = ((ConsultaProdutos) getActivity()).carregarprodutos();
            ListAdapterProdutos adapter = new ListAdapterProdutos(getActivity(), mList);
            adapter.setRecyclerViewOnClickListenerHack(this);
            mRecyclerView.setAdapter(adapter);
            return view;
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
        return null;
    }

    @Override
    public void onClickListener(View v, int position) {
        if (flag == 0 && numPedido == null) {
            ListAdapterProdutos adapter = (ListAdapterProdutos) mRecyclerView.getAdapter();

            String CodProd = adapter.ChamaDados(position);
            Intent intentp = new Intent(getActivity(), DadosProduto.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codproduto), CodProd);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();
        } else {
            ListAdapterProdutos adapter = (ListAdapterProdutos) mRecyclerView.getAdapter();
            String CodProd = adapter.ChamaDados(position);
            final Double qtdestoque;
            String codItem = null;
            String descricao = null;
            String unidadeMedida = null;
            String tabelaPadrao = null;
            Cursor cursoritem = null;

            DB = new ConfigDB(getActivity()).getReadableDatabase();

            if (numPedido.equals("0")) {
                Cursor Bloqueios = DB.rawQuery("SELECT HABITEMNEGATIVO FROM PARAMAPP", null);
                Bloqueios.moveToFirst();
                final String vendenegativo = Bloqueios.getString(Bloqueios.getColumnIndex("HABITEMNEGATIVO"));
                Bloqueios.close();
                //CodigoItem = produto_cursor.getInt(produto_cursor.getColumnIndex("CODIGOITEM"));
                boolean ConexOk = Util.checarConexaoCelular(getActivity());
                if (vendenegativo.equals("N") && ConexOk == true) {
                    String sincprod = Sincronismo.SincronizarProdutosStatic(getActivity(), usuario, senha, Integer.parseInt(CodProd));
                    if (sincprod.equals(getString(R.string.sync_products_successfully))) {
                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODITEMANUAL = " + CodProd, null);
                        CursItens.moveToFirst();
                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                        CursItens.close();
                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                            Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                            return;
                        }
                    } else {
                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodProd, null);
                        CursItens.moveToFirst();
                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                        CursItens.close();
                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                            Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                            return;
                        }
                    }
                } else {
                    Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodProd, null);
                    CursItens.moveToFirst();
                    qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                    CursItens.close();
                    if (vendenegativo.equals("N") && qtdestoque <= 0) {
                        Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                        return;
                    }
                }
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.info_produto_venda, null);
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setCancelable(false);
                alerta.setView(view);

                final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
                final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);

                spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);
                DB = new ConfigDB(getActivity()).getReadableDatabase();

                spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        GravaPreferencias(spntabpreco.getSelectedItemPosition());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                try {
                    List<String> DadosListTabPreco = new ArrayList<String>();

                    Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                    CursorParametro.moveToFirst();
                    tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                    tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                    tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                    tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                    tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                    tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                    tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                    CursorParametro.close();

                    cursoritem = DB.rawQuery("SELECT DESCRICAO,UNIVENDA,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2,TABELAPADRAO,CODITEMANUAL FROM ITENS WHERE CODITEMANUAL = " + CodProd + "", null);
                    cursoritem.moveToFirst();
                    if (cursoritem.getCount() > 0) {
                        codItem = cursoritem.getString(cursoritem.getColumnIndex("CODITEMANUAL"));
                        descricao = cursoritem.getString(cursoritem.getColumnIndex("DESCRICAO"));
                        unidadeMedida = cursoritem.getString(cursoritem.getColumnIndex("UNIVENDA"));
                        tabelaPadrao = cursoritem.getString(cursoritem.getColumnIndex("TABELAPADRAO"));
                    }

                    /*String vlvendapadrao = produto_cursor.getString(produto_cursor.getColumnIndex("VENDAPADRAO"));
                    vlvendapadrao = vlvendapadrao.trim();
                    if (!vlvendapadrao.equals("0,0000")) {
                        BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                        String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                        Precopadrao = Precopadrao.replace('.', ',');
                        DadosListTabPreco.add("Preço base R$: " + Precopadrao);
                    }*/
                    if (!tab1.equals("")) {
                        String vlvenda1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                        vlvenda1 = vlvenda1.trim();
                        if (!vlvenda1.equals("0,0000")) {
                            BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                            String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco1 = Preco1.replace('.', ',');
                            DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                        }
                    }
                    if (!tab2.equals("")) {
                        String vlvenda2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                        vlvenda2 = vlvenda2.trim();
                        if (!vlvenda2.equals("0,0000")) {
                            BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                            String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco2 = Preco2.replace('.', ',');
                            DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                        }
                    }
                    if (!tab3.equals("")) {
                        String vlvenda3 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                        vlvenda3 = vlvenda3.trim();
                        if (!vlvenda3.equals("0,0000")) {
                            BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                            String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco3 = Preco3.replace('.', ',');
                            DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                        }
                    }
                    if (!tab4.equals("")) {
                        String vlvenda4 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                        vlvenda4 = vlvenda4.trim();
                        if (!vlvenda4.equals("0,0000")) {
                            BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                            String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco4 = Preco4.replace('.', ',');
                            DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                        }
                    }
                    if (!tab5.equals("")) {
                        String vlvenda5 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                        vlvenda5 = vlvenda5.trim();
                        if (!vlvenda5.equals("0,0000")) {
                            BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                            String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco5 = Preco5.replace('.', ',');
                            DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                        }
                    }
                    if (!tab6.equals("")) {
                        String vlvendap1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                        vlvendap1 = vlvendap1.trim();
                        if (!vlvendap1.equals("0,0000")) {
                            BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                            String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Precop1 = Precop1.replace('.', ',');
                            DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                        }
                    }
                    if (!tab7.equals("")) {
                        String vlvendap2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                        vlvendap2 = vlvendap2.trim();
                        if (!vlvendap2.equals("0,0000")) {
                            BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                            String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Precop2 = Precop2.replace('.', ',');
                            DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                        }
                    }
                    ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                    arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spntabpreco.setAdapter(arrayAdapterTabPreco);

                } catch (Exception E) {
                    E.toString();

                }
                SqliteProdutoBean prdBean = new SqliteProdutoBean();
                info_txv_codproduto.setText(codItem);
                info_txv_descricaoproduto.setText(descricao);
                info_txv_unmedida.setText(unidadeMedida);

                if (tabelaPadrao.equals(tab1)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab2)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab3)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab4)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab5)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab6)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab7)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                }
                cursoritem.close();

                final Double finalQtdestoque = qtdestoque;
                alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                        if (TAMANHO_TEXTO > 0) {
                            SqliteProdutoBean prdBean = new SqliteProdutoBean();
                            Double QUANTIDADE_DIGITADA = Double.parseDouble(info_txt_quantidadecomprada.getText().toString());
                            String COD_PRODUTO = info_txv_codproduto.getText().toString();
                            String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                            String UNIDADE = info_txv_unmedida.getText().toString();

                            if (QUANTIDADE_DIGITADA > 0) {
                                if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > finalQtdestoque) {
                                    Util.msg_toast_personal(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                                    return;
                                }

                                SqliteVendaD_TempBean itemBean1 = new SqliteVendaD_TempBean();
                                SqliteVendaD_TempBean itemBean2 = new SqliteVendaD_TempBean();
                                SqliteVendaD_TempBean itemBean3 = new SqliteVendaD_TempBean();
                                SqliteVendaD_TempDao itemDao = new SqliteVendaD_TempDao(getActivity());

                                itemBean2.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                itemBean3 = itemDao.buscar_item_na_venda(itemBean2);

                                if (itemBean3 == null) {
                                    itemBean1.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                    itemBean1.setVendad_prd_descricaoTEMP(DESCRICAO);
                                    itemBean1.setVendad_prd_unidadeTEMP(UNIDADE);
                                    itemBean1.setVendad_quantidadeTEMP(new BigDecimal(QUANTIDADE_DIGITADA));

                                    //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                                    String ValorItem = info_txv_precoproduto.getText().toString();
                                    if (!ValorItem.equals("0,0000")) {
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                        itemBean1.setVendad_preco_vendaTEMP(venda);

                                        //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                        itemBean1.setVendad_totalTEMP(itemBean1.getSubTotal());
                                        itemDao.insere_item(itemBean1);
                                        atualiza_listview_com_os_itens_da_venda();
                                        getActivity().finish();
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "produto com preço de venda zerado", Util.ALERTA);
                                        return;

                                    }
                                } else {
                                    Util.msg_toast_personal(getActivity(), "Este produto já foi adicionado", Util.ALERTA);
                                    return;
                                }

                                //}
                            } else {
                                Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                return;
                            }

                        } else {
                            Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                            return;
                        }
                    }
                });
                alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                SharedPreferences prefsHost = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                sprecoprincipal = prefsHost.getInt("spreco", 0);
                spntabpreco.setSelection(sprecoprincipal);
                alerta.show();
            } else {

                DB = new ConfigDB(getActivity()).getReadableDatabase();

                Cursor Bloqueios = DB.rawQuery("SELECT HABITEMNEGATIVO FROM PARAMAPP", null);
                Bloqueios.moveToFirst();
                final String vendenegativo = Bloqueios.getString(Bloqueios.getColumnIndex("HABITEMNEGATIVO"));
                Bloqueios.close();
                //CodProd = produto_cursor.getInt(produto_cursor.getColumnIndex("CODIGOITEM"));
                boolean ConexOk = Util.checarConexaoCelular(getActivity());
                if (vendenegativo.equals("N") && ConexOk == true) {
                    String sincprod = Sincronismo.SincronizarProdutosStatic(getActivity(), usuario, senha, Integer.parseInt(CodProd));
                    if (sincprod.equals(getString(R.string.sync_products_successfully))) {
                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODITEMANUAL = " + CodProd, null);
                        CursItens.moveToFirst();
                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                        CursItens.close();
                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                            Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                            return;
                        }
                    } else {
                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODITEMANUAL = " + CodProd, null);
                        CursItens.moveToFirst();
                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                        CursItens.close();
                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                            Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                            return;
                        }
                    }
                } else {
                    Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODITEMANUAL = " + CodProd, null);
                    CursItens.moveToFirst();
                    qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                    CursItens.close();
                    if (vendenegativo.equals("N") && qtdestoque <= 0) {
                        Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                        return;
                    }
                }
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.info_produto_venda, null);
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setCancelable(false);
                alerta.setView(view);

                final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
                final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);

                spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);
                DB = new ConfigDB(getActivity()).getReadableDatabase();

                spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        GravaPreferencias(spntabpreco.getSelectedItemPosition());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                try {
                    List<String> DadosListTabPreco = new ArrayList<String>();

                    Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                    CursorParametro.moveToFirst();
                    tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                    tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                    tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                    tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                    tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                    tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                    tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                    CursorParametro.close();

                    cursoritem = DB.rawQuery("SELECT DESCRICAO,UNIVENDA,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2,TABELAPADRAO,CODITEMANUAL FROM ITENS WHERE CODITEMANUAL = " + CodProd + "", null);
                    cursoritem.moveToFirst();
                    if (cursoritem.getCount() > 0) {
                        codItem = cursoritem.getString(cursoritem.getColumnIndex("CODITEMANUAL"));
                        descricao = cursoritem.getString(cursoritem.getColumnIndex("DESCRICAO"));
                        unidadeMedida = cursoritem.getString(cursoritem.getColumnIndex("UNIVENDA"));
                        tabelaPadrao = cursoritem.getString(cursoritem.getColumnIndex("TABELAPADRAO"));
                    }


                    /*String vlvendapadrao = produto_cursor.getString(produto_cursor.getColumnIndex("VENDAPADRAO"));
                    vlvendapadrao = vlvendapadrao.trim();
                    if (!vlvendapadrao.equals("0,0000")) {
                        BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(vlvendapadrao.replace(',', '.')));
                        String Precopadrao = vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                        Precopadrao = Precopadrao.replace('.', ',');
                        DadosListTabPreco.add("Preço base R$: " + Precopadrao);
                    }*/
                    if (!tab1.equals("")) {
                        String vlvenda1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                        vlvenda1 = vlvenda1.trim();
                        if (!vlvenda1.equals("0,0000")) {
                            BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                            String Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco1 = Preco1.replace('.', ',');
                            DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                        }
                    }
                    if (!tab2.equals("")) {
                        String vlvenda2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                        vlvenda2 = vlvenda2.trim();
                        if (!vlvenda2.equals("0,0000")) {
                            BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                            String Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco2 = Preco2.replace('.', ',');
                            DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                        }
                    }
                    if (!tab3.equals("")) {
                        String vlvenda3 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                        vlvenda3 = vlvenda3.trim();
                        if (!vlvenda3.equals("0,0000")) {
                            BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                            String Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco3 = Preco3.replace('.', ',');
                            DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                        }
                    }
                    if (!tab4.equals("")) {
                        String vlvenda4 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                        vlvenda4 = vlvenda4.trim();
                        if (!vlvenda4.equals("0,0000")) {
                            BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                            String Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco4 = Preco4.replace('.', ',');
                            DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                        }
                    }
                    if (!tab5.equals("")) {
                        String vlvenda5 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                        vlvenda5 = vlvenda5.trim();
                        if (!vlvenda5.equals("0,0000")) {
                            BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                            String Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Preco5 = Preco5.replace('.', ',');
                            DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                        }
                    }
                    if (!tab6.equals("")) {
                        String vlvendap1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                        vlvendap1 = vlvendap1.trim();
                        if (!vlvendap1.equals("0,0000")) {
                            BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                            String Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Precop1 = Precop1.replace('.', ',');
                            DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                        }
                    }
                    if (!tab7.equals("")) {
                        String vlvendap2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                        vlvendap2 = vlvendap2.trim();
                        if (!vlvendap2.equals("0,0000")) {
                            BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                            String Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                            Precop2 = Precop2.replace('.', ',');
                            DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                        }
                    }

                    ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                    arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spntabpreco.setAdapter(arrayAdapterTabPreco);

                } catch (Exception E) {
                    E.toString();

                }
                SqliteProdutoBean prdBean = new SqliteProdutoBean();
                info_txv_codproduto.setText(codItem);
                info_txv_descricaoproduto.setText(descricao);
                info_txv_unmedida.setText(unidadeMedida);

                if (tabelaPadrao.equals(tab1)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab2)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab3)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab4)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab5)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab6)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                } else if (tabelaPadrao.equals(tab7)) {
                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                    Preco = Preco.replace('.', ',');

                    info_txv_precoproduto.setText(Preco);
                    info_txt_quantidadecomprada.setText("");
                    //info_txt_quantidadecomprada.selectAll();
                }
                cursoritem.close();

                alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                        if (TAMANHO_TEXTO > 0) {
                            SqliteProdutoBean prdBean = new SqliteProdutoBean();
                            Double QUANTIDADE_DIGITADA = Double.parseDouble(info_txt_quantidadecomprada.getText().toString());
                            String COD_PRODUTO = info_txv_codproduto.getText().toString();
                            String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                            String UNIDADE = info_txv_unmedida.getText().toString();

                            if (QUANTIDADE_DIGITADA > 0) {
                                if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > qtdestoque) {
                                    Util.msg_toast_personal(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                                    return;
                                }

                                SqliteVendaDBean itemBean1 = new SqliteVendaDBean();
                                SqliteVendaDBean itemBean2 = new SqliteVendaDBean();
                                SqliteVendaDBean itemBean3 = new SqliteVendaDBean();
                                Sqlite_VENDADAO itemDao = new Sqlite_VENDADAO(getActivity(), codVendedor, true);

                                itemBean2.setVendad_prd_codigo(COD_PRODUTO);
                                itemBean3 = itemDao.altera_item_na_venda(itemBean2);

                                if (itemBean3 == null) {
                                    itemBean1.setVendad_prd_codigo(COD_PRODUTO);
                                    itemBean1.setVendad_prd_descricao(DESCRICAO);
                                    itemBean1.setVendad_prd_unidade(UNIDADE);
                                    itemBean1.setVendad_quantidade(new BigDecimal(QUANTIDADE_DIGITADA));
                                    itemBean1.setVendac_chave(chavePedido);

                                    //String ValorItem = produto_cursor.getString(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO));
                                    String ValorItem = info_txv_precoproduto.getText().toString();
                                    ValorItem = ValorItem.trim();
                                    if (!ValorItem.equals("0,0000")) {
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                        itemBean1.setVendad_preco_venda(venda);

                                        //itemBean1.setVendad_preco_vendaTEMP(new BigDecimal(produto_cursor.getDouble(produto_cursor.getColumnIndex(prdBean.P_PRECO_PRODUTO))));
                                        itemBean1.setVendad_total(itemBean1.getSubTotal());
                                        itemDao.insere_item_na_venda(itemBean1);
                                        //atualiza_listview_com_os_itens_pedido();
                                        getActivity().finish();
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "produto com preço de venda zerado", Util.ALERTA);
                                        return;
                                    }
                                } else {
                                    Util.msg_toast_personal(getActivity(), "Este produto já foi adicionado", Util.ALERTA);
                                    return;
                                }
                            } else {
                                Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                return;
                            }
                        } else {
                            Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                            return;
                        }
                    }
                });
                alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                SharedPreferences prefsHost = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                sprecoprincipal = prefsHost.getInt("spreco", 0);
                spntabpreco.setSelection(sprecoprincipal);
                alerta.show();
            }
        }
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }

    public void GravaPreferencias(int preco) {

        prefs = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsPrivateEditor = prefs.edit();
        prefsPrivateEditor.putInt("spreco", preco);
        prefsPrivateEditor.commit();

    }

    public void atualiza_listview_com_os_itens_da_venda() {

        prod_listview_itenstemp = (ListView) getView().findViewById(R.id.prod_listview_produtotemp);
        List<SqliteVendaD_TempBean> itens_da_venda_temp = new SqliteVendaD_TempDao(getActivity()).busca_todos_itens_da_venda();
        /*prod_listview_itenstemp.setAdapter(new ListAdapterItensTemporarios(getActivity(), itens_da_venda_temp));


        prod_listview_itenstemp.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listview, View view, int posicao, long l) {
                //confirmar_exclusao_do_produto(listview, posicao);
                return false;
            }
        });*/
    }
}
