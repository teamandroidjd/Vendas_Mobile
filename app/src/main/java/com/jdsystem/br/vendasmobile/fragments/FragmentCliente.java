package com.jdsystem.br.vendasmobile.fragments;

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
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.CadastroAgenda;
import com.jdsystem.br.vendasmobile.CadastroClientes;
import com.jdsystem.br.vendasmobile.CadastroContatos;
import com.jdsystem.br.vendasmobile.CadastroPedidos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaAgenda;
import com.jdsystem.br.vendasmobile.ConsultaClientes;
import com.jdsystem.br.vendasmobile.ConsultaPedidos;
import com.jdsystem.br.vendasmobile.DadosCliente;
import com.jdsystem.br.vendasmobile.DadosContato;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentCliente extends Fragment implements RecyclerViewOnClickListenerHack, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    int flag, cadContato, CodContato;
    String numPedido, chavePedido, usuario, senha, codVendedor, CodEmpresa, dataEntrega, telaInvocada,
            urlPrincipal, BloqClie, bloqueio, CodigoClienteInterno, CodigoClienteExterno;
    boolean consultaPedido;
    SQLiteDatabase DB;
    int idPerfil, flagRun, iPosition;
    private RecyclerView mRecyclerView;
    ProgressDialog eDialog;
    ListAdapterClientes adapter;
    Activity activity = getActivity();
    private android.os.Handler handler = new android.os.Handler();

    private void carregarpreferencias() {
        SharedPreferences prefs = getActivity().getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        urlPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cliente, container, false);

        Bundle params = getArguments();
        if (params != null) {
            flag = params.getInt(getString(R.string.intent_flag));
            CodEmpresa = params.getString(getString(R.string.intent_codigoempresa));
            numPedido = params.getString(getString(R.string.intent_numpedido));
            chavePedido = params.getString(getString(R.string.intent_chavepedido));
            telaInvocada = params.getString(getString(R.string.intent_telainvocada));
            dataEntrega = params.getString("dataentrega");
            urlPrincipal = params.getString(getString(R.string.intent_urlprincipal));
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
            codVendedor = params.getString(getString(R.string.intent_codvendedor));
            cadContato = params.getInt(getString(R.string.intent_cad_contato));
            CodContato = params.getInt(getString(R.string.intent_codcontato));
            consultaPedido = params.getBoolean("consultapedido");
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

        List<Clientes> mList = ((ConsultaClientes) getActivity()).CarregarClientes();
        ListAdapterClientes adapter = new ListAdapterClientes(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
        //return mRecyclerView;
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
        adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
        if (consultaPedido) {
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            Intent intentp = new Intent(getActivity(), ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
            params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();

        } else if (flag == 0 && cadContato == 0) {
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            Intent intentp = new Intent(getActivity(), DadosCliente.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
            params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            intentp.putExtras(params);
            startActivity(intentp);

        } else if (flag == 0 && cadContato == 1) {
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
            String codClieExt = adapter.ChamaCodigoClienteExterno(position);
            Intent intent = new Intent(getActivity(), CadastroContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
            params.putInt(getString(R.string.intent_codcliente), Integer.parseInt(codClieExt));
            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            params.putInt(getString(R.string.intent_codcontato), CodContato);
            intent.putExtras(params);
            startActivity(intent);
            getActivity().finish();

        } else if (telaInvocada != null) {
            if (telaInvocada.equals("CadastroAgenda")) {
                String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
                Intent intentp = new Intent(getActivity(), CadastroAgenda.class);
                Bundle params = new Bundle();
                params.putInt(getString(R.string.intent_codcliente), Integer.parseInt(CodigoClienteInterno));
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                intentp.putExtras(params);
                startActivity(intentp);
                getActivity().finish();
            } else if (telaInvocada.equals("ConsultaAgenda")) {
                String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
                Intent intentp = new Intent(getActivity(), ConsultaAgenda.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                intentp.putExtras(params);
                startActivity(intentp);
                getActivity().finish();
            }
        } else {
            flagRun = 1;
            iPosition = position;

            eDialog = new ProgressDialog(getContext());
            eDialog.setTitle(getString(R.string.wait));
            eDialog.setMessage("Verificando dados do cliente...");
            eDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eDialog.setCancelable(false);
            eDialog.show();

            Thread thread = new Thread(FragmentCliente.this);
            thread.start();
        }
    }

    @Override
    public void run() {
        if (flagRun == 1) {
            String FlagIntegrado = null;
            FlagIntegrado = adapter.ChamaFlagIntegradoCliente(iPosition);
            String CodigoClienteExterno = adapter.ChamaCodigoClienteExterno(iPosition);
            final String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(iPosition);
            DB = new ConfigDB(getActivity()).getReadableDatabase();
            try {
                Cursor cursorbloqclie = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP where CODPERFIL = " + idPerfil, null);
                cursorbloqclie.moveToFirst();
                BloqClie = cursorbloqclie.getString(cursorbloqclie.getColumnIndex("HABCRITSITCLIE"));
                cursorbloqclie.close();
            } catch (Exception e) {
                e.toString();
            }
            if (BloqClie.equals("S") && !FlagIntegrado.equals("1")) {
                if (Util.checarConexaoCelular(getActivity())) {
                    Sincronismo.sincronizaClientes(codVendedor, getActivity(), usuario, senha, Integer.parseInt(CodigoClienteExterno), null, null, null);
                }
                try {
                    Cursor cursorclie = DB.rawQuery("SELECT BLOQUEIO, CODCLIE_INT FROM CLIENTES WHERE CODCLIE_INT = " + CodigoClienteInterno + " AND CODPERFIL = " + idPerfil, null);
                    cursorclie.moveToFirst();
                    bloqueio = cursorclie.getString(cursorclie.getColumnIndex("BLOQUEIO"));
                    cursorclie.close();
                } catch (Exception e) {
                    e.toString();
                }

            }
            final String finalFlagIntegrado = FlagIntegrado;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    activity = new Activity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bloqueio = adapter.ChamaBloqueioCliente(iPosition);

                            if (finalFlagIntegrado.equals("1")) {
                                if (numPedido == null) {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), "0");
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            }
                            if (bloqueio.equals("01") || bloqueio.equals("1")) {
                                if (numPedido == null) {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), "0");
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    params.putString("dataentrega", dataEntrega);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    Util.msg_toast_personal(getActivity(), getString(R.string.customer_without_purchase_permission), Util.ALERTA);
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.customer_without_purchase_permission), Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                            if (BloqClie.equals("N")) {
                                if (numPedido == null) {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), "0");
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                                    Bundle params = new Bundle();
                                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                                    params.putString(getString(R.string.intent_numpedido), numPedido);
                                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                    params.putString(getString(R.string.intent_usuario), usuario);
                                    params.putString(getString(R.string.intent_senha), senha);
                                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                    intent.putExtras(params);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            }
                        }
                    });
                }
            });
        } else if (flagRun == 2) {
            if (CodigoClienteExterno == null) {
                final String clieEnvio = Sincronismo.sincronizaClientesEnvio(CodigoClienteInterno, getActivity(), usuario, senha, null, null, null);
                if (!clieEnvio.equals("0")) {
                    Intent intent = new Intent(getActivity(), ConsultaClientes.class);
                    Bundle params = new Bundle();
                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                    params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                    intent.putExtras(params);
                    startActivity(intent);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Util.msg_toast_personal(getActivity(), clieEnvio, Util.ALERTA);
                            } else {
                                Toast.makeText(getActivity(), clieEnvio, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    if (eDialog.isShowing())
                        eDialog.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Util.msg_toast_personal(getActivity(), "Não é possível enviar o clientes. " + clieEnvio + "", Util.ALERTA);
                            } else {
                                Toast.makeText(getActivity(), "Não é possível enviar o clientes.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    return;
                }
            } else {
                if (eDialog.isShowing())
                    eDialog.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                        } else {
                            Toast.makeText(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return;
            }
            if (eDialog.isShowing())
                eDialog.dismiss();
        }
    }

    @Override
    public void onLongClickListener(View view, final int position) {
        final ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
        CodigoClienteExterno = adapter.ChamaCodigoClienteExterno(position);
        CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
        String nomeRazao = adapter.ChamaNomeRazaoCliente(position);

        if (telaInvocada.equals("CadastroAgenda")) {
            Intent intentp = new Intent(getActivity(), DadosContato.class);
            Bundle params = new Bundle();
            params.putInt("codCliente", Integer.parseInt(CodigoClienteInterno));
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();
        }
        if (CodigoClienteExterno == null) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") final View formElementsView = inflater.inflate(R.layout.input_alterar_cliente, null, false);
            final RadioGroup genderRadioGroup = (RadioGroup) formElementsView.findViewById(R.id.genderRadioGroup);
            new AlertDialog.Builder(getActivity()).setView(formElementsView)
                    .setTitle("Cliente: " + nomeRazao)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @TargetApi(11)
                        public void onClick(DialogInterface dialog, int id) {
                            int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                            if (selectedId > 0) {
                                RadioButton selectedRadioButton = (RadioButton) formElementsView.findViewById(selectedId);
                                if ((selectedRadioButton.getText().toString().trim()).equals("Alterar")) {
                                    if (CodigoClienteExterno == null) {
                                        String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
                                        Intent intentp = new Intent(getActivity(), CadastroClientes.class);
                                        Bundle params = new Bundle();
                                        params.putInt(getString(R.string.intent_codcliente), Integer.parseInt(CodigoClienteInterno));
                                        params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
                                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                        params.putString(getString(R.string.intent_usuario), usuario);
                                        params.putString(getString(R.string.intent_codigoempresa), CodEmpresa);
                                        params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                        params.putString(getString(R.string.intent_senha), senha);
                                        params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                                        params.putInt(getString(R.string.intent_flag), flag);

                                        intentp.putExtras(params);
                                        startActivity(intentp);
                                        getActivity().finish();
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                                        } else {
                                            Toast.makeText(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Toast.LENGTH_SHORT).show();
                                        }
                                        return;
                                    }

                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Excluir")) {
                                    DB = new ConfigDB(getActivity()).getReadableDatabase();
                                    if (CodigoClienteExterno == null) {
                                        try {
                                            DB.execSQL("DELETE FROM CLIENTES WHERE CODCLIE_INT = '" + CodigoClienteInterno + "' AND CODPERFIL = " + idPerfil);
                                            Intent intent = ((ConsultaClientes) getActivity()).getIntent();
                                            ((ConsultaClientes) getActivity()).finish();
                                            startActivity(intent);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), "Cadastro excluído com sucesso!", Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), "Cadastro excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            e.toString();
                                        }
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                                        } else {
                                            Toast.makeText(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Toast.LENGTH_SHORT).show();
                                        }
                                        return;
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Sincronizar")) {

                                    flagRun = 2;
                                    eDialog = new ProgressDialog(getContext());
                                    eDialog.setTitle(getString(R.string.wait));
                                    eDialog.setMessage("Enviando cadastro do cliente...");
                                    eDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    eDialog.setCancelable(false);
                                    eDialog.show();

                                    Thread thread = new Thread(FragmentCliente.this);
                                    thread.start();
                                }
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

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(getActivity(), "Opção disponível somente para clientes não sincronizados!", Util.ALERTA);
            } else {
                Toast.makeText(getActivity(), "Opção disponível somente para clientes não sincronizados!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }
}