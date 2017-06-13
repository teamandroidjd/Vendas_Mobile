package com.jdsystem.br.vendasmobile.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import com.jdsystem.br.vendasmobile.CadastroPedidos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaPedidos;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidos;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

//import com.lowagie.text.HeaderFooter;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentPedido extends Fragment implements RecyclerViewOnClickListenerHack, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    int codclie_ext;
    String limitecred, bloqueio, usuario, senha, Codvendedor, flagintegrado, codclie_inte, URLPrincipal;
    int idPerfil, runFlag;
    private RecyclerView mRecyclerView;
    private Context context = this.getActivity();
    private SQLiteDatabase DB;
    String sStatus, sDataVenda, sNumPedido, sTotalVenda;
    Handler handler = new Handler();
    ProgressDialog eDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedidos, container, false);

        Bundle params = getArguments();
        if (params != null) {
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
            Codvendedor = params.getString(getString(R.string.intent_codvendedor));
            URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
        }

        carregarpreferencias();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);

        //Utilizado para o fast Scroll
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(mRecyclerView);
        mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());
        setRecyclerViewLayoutManager(mRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        List<Pedidos> mList = ((ConsultaPedidos) getActivity()).CarregarPedidos();
        ListAdapterPedidos adapter = new ListAdapterPedidos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) { // Utilizado para o fast scroll
        int scrollPosition = 0;

        // If a form_pgto_listview_parcelas manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onClickListener(View view, int position) {
        ListAdapterPedidos adapter = (ListAdapterPedidos) mRecyclerView.getAdapter();
        String Status = adapter.StatusPedido(position);

        if (Status.equals("Orçamento")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Pressione o pedido para escolher uma das opções!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (Status.equals("#")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Pressione o pedido para verificar o seu status!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (Status.equals("Cancelado")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Pedido Cancelado!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onLongClickListener(View view, final int position) {
        try {
            final ListAdapterPedidos adapter = (ListAdapterPedidos) mRecyclerView.getAdapter();
            final String Status = adapter.StatusPedido(position);
            final String NomeVendedor = adapter.ChamaNomeVendedor(position);
            final Boolean ConexOk = Util.checarConexaoCelular(view.getContext());
            //if (ConexOk == true) {
            final String NumPedido = adapter.ChamaDados(position);

            DB = new ConfigDB(getActivity()).getReadableDatabase();

            final Cursor cursorped = DB.rawQuery("SELECT CODCLIE_EXT,DATAVENDA,VALORTOTAL, CODCLIE FROM PEDOPER WHERE NUMPED = " + NumPedido + " AND CODPERFIL = " + idPerfil, null);
            cursorped.moveToFirst();
            codclie_ext = cursorped.getInt(cursorped.getColumnIndex("CODCLIE_EXT"));
            codclie_inte = cursorped.getString(cursorped.getColumnIndex("CODCLIE"));
            final String datavend = cursorped.getString(cursorped.getColumnIndex("DATAVENDA"));
            String vltotal = cursorped.getString(cursorped.getColumnIndex("VALORTOTAL")).replace(".", ",");
            BigDecimal vendatotal = new BigDecimal(Double.parseDouble(vltotal.replace(',', '.')));
            final String totalvenda = vendatotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            cursorped.close();
            try {
                if (codclie_ext != 0) {
                    Cursor cursorclie = DB.rawQuery("SELECT LIMITECRED, FLAGINTEGRADO, BLOQUEIO FROM CLIENTES WHERE CODCLIE_EXT = " + codclie_ext + " AND CODPERFIL = " + idPerfil, null);
                    cursorclie.moveToFirst();
                    limitecred = cursorclie.getString(cursorclie.getColumnIndex("LIMITECRED"));
                    bloqueio = cursorclie.getString(cursorclie.getColumnIndex("BLOQUEIO"));
                    cursorclie.close();
                }
            } catch (Exception e) {
                e.toString();
            }

            // Neese momento é que a tela com as opções do pedido é criada.

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") final View formElementsView = inflater.inflate(R.layout.input_pergunta_list_pedido, null, false);
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
                                    sStatus = Status;
                                    sDataVenda = datavend;
                                    sTotalVenda = totalvenda;
                                    sNumPedido = NumPedido;

                                    runFlag = 1;

                                    eDialog = new ProgressDialog(getContext());
                                    eDialog.setTitle(getString(R.string.wait));
                                    eDialog.setMessage("Sincronizando pedido...");
                                    eDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    eDialog.setCancelable(false);
                                    eDialog.show();

                                    Thread thread = new Thread(FragmentPedido.this);
                                    thread.start();


                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Cancelar")) {
                                    if (Status.equals("Orçamento") || Status.equals("Gerar Venda")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle(R.string.app_namesair);
                                        builder.setIcon(R.drawable.logo_ico);
                                        builder.setMessage("Deseja realmente cancelar o pedido " + NumPedido + "?")
                                                .setCancelable(false)
                                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String Cancelado = Sincronismo.sincronizaAtualizaPedido(NumPedido, getContext(), "C");

                                                        if (Cancelado.equals("ok")) {
                                                            Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                            ((ConsultaPedidos) getActivity()).finish();
                                                            startActivity(intent);
                                                            Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " cancelado com Sucesso!", Util.PADRAO);
                                                        } else {
                                                            Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " não pode ser cancelado, verifique!", Util.PADRAO);
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                        return;

                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Compartilhar")) {
                                    //String TxtPedido = Sincronismo.RetornaPedido(NumPedido, getContext());
                                    int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                        return;
                                    }
                                    String TxtPedido = Sincronismo.GerarPdf(NumPedido, getContext());
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf";

                                    if (!TxtPedido.equals("0")) {
                                        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                                        File fileWithinMyDir = new File(path);

                                        if (fileWithinMyDir.exists()) {
                                            intentShareFile.setType("application/pdf");
                                            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + TxtPedido));

                                            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, " Força de Vendas - Vendedor: " + NomeVendedor + " - Pedido nº " + NumPedido);
                                            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Segue em anexo o Pedido nº " + NumPedido);

                                            startActivity(Intent.createChooser(intentShareFile, "Compartilhar Pedido nº " + NumPedido));
                                        }
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Não foi possivel compartilhar o Pedido nº " + NumPedido + ".", Util.PADRAO);
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Verificar Status")) {
                                    String statusatualizado;
                                    if (Status.equals("#")) {
                                        final String NumPedidoExt = adapter.PedidoExterno(position);
                                        statusatualizado = Sincronismo.sincronizaAtualizaPedido(NumPedidoExt, getContext(), "S");
                                        if (statusatualizado != null) {
                                            if (statusatualizado.equals("Orçamento")) {
                                                Util.msg_toast_personal(getActivity(), "Seu Pedido " + NumPedidoExt + " ainda não foi faturado. Encontra-se com o status de " + statusatualizado + ".", Util.PADRAO);
                                                Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                ((ConsultaPedidos) getActivity()).finish();
                                                startActivity(intent);
                                            } else if (statusatualizado.equals("Faturado")) {
                                                Util.msg_toast_personal(getActivity(), "Seu Pedido " + NumPedidoExt + " foi faturado!", Util.PADRAO);
                                                Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                ((ConsultaPedidos) getActivity()).finish();
                                                startActivity(intent);
                                            } else if (statusatualizado.equals("")) {
                                                Util.msg_toast_personal(getActivity(), "Seu Pedido foi cancelado! Para maiores informações, entre em contato com sua txvempresa.", Util.PADRAO);
                                                Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                ((ConsultaPedidos) getActivity()).finish();
                                                startActivity(intent);
                                            }
                                        } else {
                                            Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                            ((ConsultaPedidos) getActivity()).finish();
                                            startActivity(intent);
                                            Util.msg_toast_personal(getActivity(), "Não foi possivel atualizar o status de Pedido nº " + NumPedidoExt + ".", Util.PADRAO);
                                        }
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Somente Pedidos Sincronizados", Util.PADRAO);
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Gerar Venda")) {
                                    if (Status.equals("Orçamento")) {
                                        String Autorizado = Sincronismo.sincronizaAtualizaPedido(NumPedido, getContext(), "A");
                                        if (Autorizado.equals("ok")) {
                                            Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                            ((ConsultaPedidos) getActivity()).finish();
                                            startActivity(intent);
                                            Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " autorizado a Gerar Venda", Util.PADRAO);
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " não pode ser autorizado a Gerar Venda, verifique!", Util.PADRAO);
                                        }
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Alterar")) {
                                    if (Status.equals("Orçamento") || Status.equals("Gerar Venda")) {
                                        final String NumPedido = adapter.ChamaDados(position);
                                        Intent VendaProd = new Intent((ConsultaPedidos) getActivity(), CadastroPedidos.class);
                                        Bundle params = new Bundle();
                                        params.putString(getString(R.string.intent_numpedido), NumPedido);
                                        params.putString(getString(R.string.intent_codvendedor), Codvendedor);
                                        params.putString(getString(R.string.intent_usuario), usuario);
                                        params.putString(getString(R.string.intent_senha), senha);
                                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                        VendaProd.putExtras(params);
                                        Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                        ((ConsultaPedidos) getActivity()).finish();
                                        startActivityForResult(VendaProd, 1);
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Pedido nº " + NumPedido + " não pode ser alterado, o mesmo já foi transmitido. Verifique!", Util.PADRAO);
                                    }
                                }
                                dialog.cancel();
                            } else {
                                Util.msg_toast_personal(getActivity(), "Você deve escolher uma das opções!!!", Util.PADRAO);
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).
                    show();

            Configuration configuration = getResources().getConfiguration();

            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        } catch (Exception E) {
            E.toString();
        }
    }

    private void carregarpreferencias() {
        SharedPreferences prefs = getActivity().getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (runFlag == 1) {
                        Activity activity = new Activity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String sitclieenvio;
                            String pedidoendiado;
                            String sitcliexvend;
                            try {
                                Cursor cursorclie = DB.rawQuery("SELECT FLAGINTEGRADO, CODCLIE_INT FROM CLIENTES WHERE CODCLIE_INT = '" + codclie_inte + "' AND CODPERFIL = " + idPerfil, null);
                                cursorclie.moveToFirst();
                                flagintegrado = cursorclie.getString(cursorclie.getColumnIndex("FLAGINTEGRADO"));

                                cursorclie.close();
                            } catch (Exception e) {
                                e.toString();
                            }
                            if (sStatus.equals("Orçamento") || sStatus.equals("Gerar Venda")) {
                                if (Util.checarConexaoCelular(getContext())) {
                                    if (flagintegrado.equals("1")) {
                                        sitclieenvio = Sincronismo.sincronizaClientesEnvio(codclie_inte, getActivity(), usuario, senha, null, null, null);
                                        if (sitclieenvio.equals("OK")) {
                                            try {
                                                if (String.valueOf(sDataVenda).equals("null")) {
                                                    String dtvend = Util.DataHojeComHorasBR();
                                                    DB.execSQL(" UPDATE PEDOPER SET DATAVENDA = '" + dtvend + "' WHERE NUMPED = '" + sNumPedido + "' AND CODPERFIL = " + idPerfil);
                                                }
                                                pedidoendiado = Sincronismo.sincronizaPedidosEnvio(usuario, senha, getContext(), sNumPedido, null, null, null);
                                                if (pedidoendiado.equals("OK")) {
                                                    Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                    ((ConsultaPedidos) getActivity()).finish();
                                                    startActivity(intent);
                                                    Util.msg_toast_personal(getActivity(), "Pedido nº " + sNumPedido + " sincronizado com Sucesso!", Util.PADRAO);
                                                } else {
                                                    Util.msg_toast_personal(getActivity(), pedidoendiado, Util.PADRAO);
                                                    return;
                                                }
                                            }catch (Exception e){
                                                e.toString();
                                            }
                                            } else{
                                                Util.msg_toast_personal(getActivity(), "Falha ao enviar Cliente. Tente novamente.", Util.PADRAO);
                                                return;
                                            }


                                    } else {
                                        try {
                                            sitcliexvend = Sincronismo.sincronizaSitClieXPed(sTotalVenda, getActivity(), usuario, senha, codclie_ext);
                                            if (sitcliexvend.equals("OK")) {
                                                if (String.valueOf(sDataVenda).equals("null")) {
                                                    String dtvend = Util.DataHojeComHorasBR();
                                                    DB.execSQL(" UPDATE PEDOPER SET DATAVENDA = '" + dtvend + "' WHERE NUMPED = '" + sNumPedido + "' AND CODPERFIL = " + idPerfil);
                                                }
                                                pedidoendiado = Sincronismo.sincronizaPedidosEnvio(usuario, senha, getContext(), sNumPedido, null, null, null);
                                                if (pedidoendiado.equals("OK")) {
                                                    Intent intent = ((ConsultaPedidos) getActivity()).getIntent();
                                                    ((ConsultaPedidos) getActivity()).finish();
                                                    startActivity(intent);
                                                    Util.msg_toast_personal(getActivity(), "Pedido nº " + sNumPedido + " sincronizado com Sucesso!", Util.PADRAO);
                                                } else {
                                                    Util.msg_toast_personal(getActivity(), pedidoendiado, Util.PADRAO);
                                                    return;
                                                }
                                            } else {
                                                Util.msg_toast_personal(getActivity(), sitcliexvend, Util.PADRAO);
                                                return;
                                            }
                                        }catch (Exception e){
                                            e.toString();
                                        }
                                    }
                                } else {
                                    Util.msg_toast_personal(getActivity(), "Sem Conexão com a Internet", Util.PADRAO);
                                    return;
                                }
                            } else {
                                Util.msg_toast_personal(getActivity(), "Somente para Orçamentos!", Util.PADRAO);
                            }
                        }
                    });
                }
                }
            });
    }
}
