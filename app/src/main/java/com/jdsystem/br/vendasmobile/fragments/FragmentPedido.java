package com.jdsystem.br.vendasmobile.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jdsystem.br.vendasmobile.Controller.VenderProdutos;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.actListPedidos;
import com.jdsystem.br.vendasmobile.actLogin;
import com.jdsystem.br.vendasmobile.actSincronismo;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;
//import com.lowagie.text.HeaderFooter;

import java.io.File;
import java.util.List;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentPedido extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<Pedidos> mList;
    private Context context;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedidos, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mList = ((actListPedidos) getActivity()).CarregarPedidos();
        ListAdapterPedidos adapter = new ListAdapterPedidos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClickListener(View view, int position) {
        ListAdapterPedidos adapter = (ListAdapterPedidos) mRecyclerView.getAdapter();
        String Status = adapter.StatusPedido(position);

        if (Status.equals("Orçamento")) {
            Util.msg_toast_personal(getActivity(), "Pressione o Pedido para escolher as opções!", Util.PADRAO);
        }
        if (Status.equals("#")) {
            Util.msg_toast_personal(getActivity(), "Pressione o Pedido para verificar o seu status!", Util.PADRAO);
        }
        if (Status.equals("Cancelado")) {
            Util.msg_toast_personal(getActivity(), "Pedido Cancelado!", Util.PADRAO);
        }
    }

    @Override
    public void onLongClickListener(View view, final int position) {
        try {
            final ListAdapterPedidos adapter = (ListAdapterPedidos) mRecyclerView.getAdapter();
            final String Status = adapter.StatusPedido(position);
            final String NomeVendedor = adapter.ChamaNomeVendedor(position);
            Boolean ConexOk = Util.checarConexaoCelular(view.getContext());
            if (ConexOk == true) {
                final String NumPedido = adapter.ChamaDados(position);
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View formElementsView = inflater.inflate(R.layout.act_pergunta_list_pedido, null, false);
                final RadioGroup genderRadioGroup = (RadioGroup) formElementsView.findViewById(R.id.genderRadioGroup);
                new AlertDialog.Builder(getActivity()).setView(formElementsView)
                        .setTitle("Pedido: " + NumPedido)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {
                                int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                                if (selectedId > 0) {
                                    RadioButton selectedRadioButton = (RadioButton) formElementsView.findViewById(selectedId);
                                    if ((selectedRadioButton.getText().toString().trim()).equals("Sincronizar")) {
                                        if (Status.equals("Orçamento") || Status.equals("Gerar Venda")) {
                                            actSincronismo.SincronizarPedidosEnvio(NumPedido, getContext(), false);

                                            Intent intent = ((actListPedidos) getActivity()).getIntent();
                                            ((actListPedidos) getActivity()).finish();
                                            startActivity(intent);
                                            Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " sincronizado com Sucesso!", Util.PADRAO);
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                                        }
                                    } else if ((selectedRadioButton.getText().toString().trim()).equals("Cancelar")) {
                                        if (Status.equals("Orçamento")) {
                                            boolean Cancelado = actSincronismo.CancelarPedidoAberto(NumPedido, getContext());

                                            if (Cancelado == true) {
                                                Intent intent = ((actListPedidos) getActivity()).getIntent();
                                                ((actListPedidos) getActivity()).finish();
                                                startActivity(intent);

                                                Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " cancelado com Sucesso!", Util.PADRAO);
                                            } else {
                                                Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " não pode ser cancelado, verifique!", Util.PADRAO);
                                            }
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                                        }
                                    } else if ((selectedRadioButton.getText().toString().trim()).equals("Compartilhar")) {
                                        //String TxtPedido = actSincronismo.RetornaPedido(NumPedido, getContext());
                                        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                            return;
                                        }

                                        String TxtPedido = actSincronismo.GerarPdf(NumPedido, getContext());
                                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf";

                                        if (!TxtPedido.equals("0")) {
                                            Intent intentShareFile = new Intent(android.content.Intent.ACTION_SEND);
                                            File fileWithinMyDir = new File(path);

                                            if (fileWithinMyDir.exists()) {
                                                intentShareFile.setType("application/pdf");
                                                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + TxtPedido));

                                                intentShareFile.putExtra(Intent.EXTRA_SUBJECT, " Força de Vendas - Vendedor: " + NomeVendedor + " - Pedido nº " + NumPedido);
                                                intentShareFile.putExtra(Intent.EXTRA_TEXT, "Segue em anexo o Pedido nº " + NumPedido);

                                                startActivity(Intent.createChooser(intentShareFile, "Compartilhar Pedido nº " + NumPedido));
                                            }
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Não foi possivel compartilhar o Pedido nº " + NumPedido, Util.PADRAO);
                                        }
                                    } else if ((selectedRadioButton.getText().toString().trim()).equals("Verificar Status")) {
                                        if (Status.equals("#")) {
                                            final String NumPedidoExt = adapter.PedidoExterno(position);
                                            actSincronismo.AtualizaStatusPedido(NumPedidoExt, getContext());

                                            Intent intent = ((actListPedidos) getActivity()).getIntent();
                                            ((actListPedidos) getActivity()).finish();
                                            startActivity(intent);
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Somente Pedidos Sincronizados", Util.PADRAO);
                                        }
                                    } else if ((selectedRadioButton.getText().toString().trim()).equals("Gerar Venda")) {
                                        if (Status.equals("Orçamento")) {
                                            boolean Autorizado = actSincronismo.AutorizaPedidoAberto(NumPedido, getContext());

                                            if (Autorizado == true) {
                                                Intent intent = ((actListPedidos) getActivity()).getIntent();
                                                ((actListPedidos) getActivity()).finish();
                                                startActivity(intent);

                                                Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " autorizado a Gerar Venda", Util.PADRAO);
                                            } else {
                                                Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " não pode ser autorizado a Gerar Venda, verifique!", Util.PADRAO);
                                            }
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                                        }
                                    } /*else if ((selectedRadioButton.getText().toString().trim()).equals("Alterar")) {
                                        if (Status.equals("Orçamento")) {
                                            final String NumPedido = adapter.ChamaDados(position);
                                            Intent intent = ((actListPedidos) getActivity()).getIntent();
                                            ((actListPedidos) getActivity()).finish();
                                            startActivity(intent);
                                        }

                                    }*/
                                    dialog.cancel();
                                } else {
                                    Util.msg_toast_personal(getActivity(), "Você deve escolher uma das opções!!!", Util.PADRAO);
                                }
                            }
                        }).show();
            } else {
                Util.msg_toast_personal(getActivity(), "Sem Conexão com a Internet", Util.PADRAO);
            }
        } catch (Exception E) {
            E.toString();
        }
    }
}
