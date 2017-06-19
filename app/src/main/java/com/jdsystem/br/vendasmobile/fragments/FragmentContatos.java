package com.jdsystem.br.vendasmobile.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import com.jdsystem.br.vendasmobile.CadastroClientes;
import com.jdsystem.br.vendasmobile.CadastroContatos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaClientes;
import com.jdsystem.br.vendasmobile.CadastroAgenda;
import com.jdsystem.br.vendasmobile.ConsultaAgenda;
import com.jdsystem.br.vendasmobile.ConsultaContatos;
import com.jdsystem.br.vendasmobile.DadosContato;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterContatos;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

import static com.google.android.gms.common.internal.zze.DB;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FragmentContatos extends Fragment implements RecyclerViewOnClickListenerHack {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    Context context = getActivity();
    ProgressDialog pDialog;
    private RecyclerView mRecyclerView;
    private String usuario, senha, codVendedor, urlprincipal, codEmpresa, telaInvocada, opcaoagenda;
    private int flag;
    int idPerfil;
    SQLiteDatabase DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        Bundle params = getArguments();
        if (params != null) {
            flag = params.getInt(getString(R.string.intent_flag));
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
            codVendedor = params.getString(getString(R.string.intent_codvendedor));
            codEmpresa = params.getString(getString(R.string.intent_codigoempresa));
            telaInvocada = params.getString(getString(R.string.intent_telainvocada));
        }

        carregarpreferencias();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_sinc);
        mRecyclerView.setHasFixedSize(true);

        //Utilizado para o fast Scroll
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(mRecyclerView);
        mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());
        setRecyclerViewLayoutManager(mRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        List<Contatos> mList = ((ConsultaContatos) getActivity()).carregarcontatos();
        ListAdapterContatos adapter = new ListAdapterContatos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
        //return mRecyclerView;
    }

    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) { // Utilizado para o fast scroll
        int scrollPosition = 0;

        // If a listview_parcelas manager has already been set, get current scroll position.
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
        ListAdapterContatos adapterContatos = (ListAdapterContatos) mRecyclerView.getAdapter();
        int CodigoContato = adapterContatos.CodigoContato(position);
        int CodigoCliente = adapterContatos.ChamaCodigoContato(position);
        int CodigoExtCont = Integer.parseInt(adapterContatos.CodigoContatoExterno(position));

        if (telaInvocada != null) {
            if (telaInvocada.equals("CadastroAgenda")) {
                Intent intentp = new Intent(getActivity(), CadastroAgenda.class);
                Bundle params = new Bundle();
                params.putInt(getString(R.string.intent_codcontato), CodigoContato);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                intentp.putExtras(params);
                startActivity(intentp);
                getActivity().finish();
            } else if (telaInvocada.equals("ConsultaAgenda")) {
                Intent intentp = new Intent(getActivity(), ConsultaAgenda.class);
                Bundle params = new Bundle();
                params.putInt(getString(R.string.intent_codcontato), CodigoContato);
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                intentp.putExtras(params);
                startActivity(intentp);
                getActivity().finish();
            }
        }else {
                if (flag == 0) {
                    Intent intentp = new Intent(getActivity(), DadosContato.class);
                    Bundle params = new Bundle();
                    params.putInt("codCliente", CodigoCliente);
                    params.putInt(getString(R.string.intent_codcontato), CodigoContato);
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                    params.putInt(getString(R.string.intent_codcontato_externo), CodigoExtCont);
                    intentp.putExtras(params);
                    startActivity(intentp);
                    getActivity().finish();
                }
            }

    }

    @Override
    public void onLongClickListener(View view, int position) {
        ListAdapterContatos adapterContatos = (ListAdapterContatos) mRecyclerView.getAdapter();
        int CodigoCliente = adapterContatos.ChamaCodigoContato(position);
        final int CodigoContato = adapterContatos.CodigoContato(position);
        final String CodigoContatoExterno = adapterContatos.CodigoContatoExterno(position);
        final String flagIntegrado = adapterContatos.flagIntegrado(position);
        String nomeContato = adapterContatos.ChamaDados(position);

        if (telaInvocada.equals("CadastroAgenda")) {
            Intent intentp = new Intent(getActivity(), DadosContato.class);
            Bundle params = new Bundle();
            params.putInt("codCliente", CodigoCliente);
            params.putInt(getString(R.string.intent_codcontato), CodigoContato);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();
        } else {
            telaInvocada = "FragmentContatos";
            if (CodigoContatoExterno == null) {

                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") final View formElementsView = inflater.inflate(R.layout.alterar_contato, null, false);
                final RadioGroup genderRadioGroup = (RadioGroup) formElementsView.findViewById(R.id.genderRadioGroup);
                new AlertDialog.Builder(getActivity()).setView(formElementsView)
                        .setTitle("Contato: " + nomeContato)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {
                                int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                                if (selectedId > 0) {
                                    RadioButton selectedRadioButton = (RadioButton) formElementsView.findViewById(selectedId);
                                    if ((selectedRadioButton.getText().toString().trim()).equals("Alterar")) {
                                        if (flagIntegrado.equals("N")) {
                                            //String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
                                            Intent intentp = new Intent(getActivity(), CadastroContatos.class);
                                            Bundle params = new Bundle();
                                            params.putInt(getString(R.string.intent_codcontato), CodigoContato);
                                            params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                            params.putString(getString(R.string.intent_usuario), usuario);
                                            params.putString(getString(R.string.intent_codigoempresa), codEmpresa);
                                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                                            params.putString(getString(R.string.intent_senha), senha);
                                            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                                            params.putInt(getString(R.string.intent_flag), flag);

                                            intentp.putExtras(params);
                                            startActivity(intentp);
                                            getActivity().finish();
                                            telaInvocada = null;
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                                            }else{
                                                Toast.makeText(getActivity(), "Não é possível alterar ou excluir contatos já sincronizados!", Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }

                                    } else if ((selectedRadioButton.getText().toString().trim()).equals("Excluir")) {
                                        DB = new ConfigDB(getActivity()).getReadableDatabase();
                                        if (flagIntegrado.equals("N")) {
                                            try {
                                                DB.execSQL("DELETE FROM CONTATO WHERE CODCLIE_INT = '" + CodigoContato + "' AND CODPERFIL = " + idPerfil);
                                                Intent intent = ((ConsultaClientes) getActivity()).getIntent();
                                                ((ConsultaClientes) getActivity()).finish();
                                                startActivity(intent);

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "Cadastro excluído com sucesso!", Util.ALERTA);
                                                }else{
                                                    Toast.makeText(getContext(), "Cadastro excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                e.toString();
                                            }
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                                            }else{
                                                Toast.makeText(getContext(), "Não é possível alterar ou excluir clientes já sincronizados!", Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }
                                    } /*else if ((selectedRadioButton.getText().toString().trim()).equals("Sincronizar")) {
                                    if (CodigoContato == 0) {
                                        String clieEnvio = Sincronismo.SincronizarClientesEnvioStatic(CodigoClienteInterno, getActivity(), usuario, senha, null, null, null);
                                        if (!clieEnvio.equals("0")) {
                                            Intent intent = ((ConsultaClientes) getActivity()).getIntent();
                                            ((ConsultaClientes) getActivity()).finish();
                                            startActivity(intent);
                                            Util.msg_toast_personal(getActivity(), clieEnvio, Util.ALERTA);
                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Não é possível enviar o clientes. " + clieEnvio + "", Util.ALERTA);
                                            return;
                                        }
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Não é possível alterar ou excluir clientes já sincronizados!", Util.ALERTA);
                                        return;
                                    }

                                }*/

                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                telaInvocada = null;
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
                    Util.msg_toast_personal(getActivity(), "Opção disponível somente para contatos não sincronizados!", Util.ALERTA);
                    telaInvocada = null;
                } else {
                    Toast.makeText(getContext(), "Opção disponível somente para contatos não sincronizados!", Toast.LENGTH_SHORT).show();
                    telaInvocada = null;
                }
                return;
            }
        }
    }

    private void carregarpreferencias() {
        SharedPreferences prefs = getActivity().getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        urlprincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }
}
