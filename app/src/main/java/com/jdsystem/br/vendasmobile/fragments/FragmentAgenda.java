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

import com.jdsystem.br.vendasmobile.CadastroAgenda;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaAgenda;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterAgenda;
import com.jdsystem.br.vendasmobile.domain.Agenda;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;
/**
 * Created by wks on 01/06/2017.
 */

public class FragmentAgenda extends Fragment implements RecyclerViewOnClickListenerHack {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    String  usuario, senha, Codvendedor, codcont, URLPrincipal, novaagenda;
    int idPerfil;
    private RecyclerView mRecyclerView;
    private Context context = this.getActivity();
    private SQLiteDatabase DB;
    ProgressDialog dialogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agenda, container, false);

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

        List<Agenda> mList = ((ConsultaAgenda) getActivity()).CarregarAgenda();
        ListAdapterAgenda adapter = new ListAdapterAgenda(getActivity(), mList);
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
        final ListAdapterAgenda adapter = (ListAdapterAgenda) mRecyclerView.getAdapter();
        final String NovaAgenda = adapter.ChamaNovaAgenda(position);

        if (NovaAgenda != null) {
            Intent CadAgenda = new Intent((ConsultaAgenda) getActivity(), CadastroAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_novaagenda), NovaAgenda);
            params.putString(getString(R.string.intent_codvendedor), Codvendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            CadAgenda.putExtras(params);
            Intent intent = ((ConsultaAgenda) getActivity()).getIntent();
            ((ConsultaAgenda) getActivity()).finish();
            startActivityForResult(CadAgenda, 4);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_namesair);
            builder.setIcon(R.drawable.logo_ico);
            builder.setMessage("Pressione o agendamento para escolher uma das opções!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
    }

    @Override
    public void onLongClickListener(View view, final int position) {
        try {
            final ListAdapterAgenda adapter = (ListAdapterAgenda) mRecyclerView.getAdapter();
            final String Status = adapter.StatusAgenda(position);
            final Integer Situacao = adapter.SituacaoAgenda(position);
            final String NumAgenda = adapter.ChamaDados(position);
            final Boolean ConexOk = Util.checarConexaoCelular(view.getContext());

            DB = new ConfigDB(getActivity()).getReadableDatabase();

            /*final Cursor cursorag = DB.rawQuery("SELECT CODIGO, NOMECONTATO, CODCONTATO, STATUS, CODPERFIL, DATAAGEND, DESCRICAO FROM AGENDA WHERE CODIGO = " + NumAgenda + " AND CODPERFIL = " + idPerfil, null);
            cursorag.moveToFirst();
            codcont = cursorag.getString(cursorag.getColumnIndex("CODCONTATO"));
            final String dataag = cursorag.getString(cursorag.getColumnIndex("DATAAGEND"));
            cursorag.close();*/

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") final View formElementsView = inflater.inflate(R.layout.input_pergunta_agenda, null, false);
            final RadioGroup genderRadioGroup = (RadioGroup) formElementsView.findViewById(R.id.genderRadioGroup);
            new AlertDialog.Builder(getActivity()).setView(formElementsView)
                    .setTitle("Agendamento: " + NumAgenda)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @android.support.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
                        public void onClick(DialogInterface dialog, int id) {
                            int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                            if (selectedId > 0) {
                                if (Situacao == 4) {
                                    Util.msg_toast_personal(getActivity(), "O agendamento nº " + NumAgenda + " já foi reagendado. Favor clicar uma vez no agendamento para ser redirecionado a nova agenda.", Util.PADRAO);
                                } else {
                                RadioButton selectedRadioButton = (RadioButton) formElementsView.findViewById(selectedId);
                                if ((selectedRadioButton.getText().toString().trim()).equals("Sincronizar")) {
                                    if (Status.equals("N")) {
                                        if (ConexOk) {

                                            dialogo = new ProgressDialog(getActivity());
                                            dialogo.setMessage("Sincronizando agendamento nº " + NumAgenda);
                                            dialogo.setCancelable(false);
                                            dialogo.setTitle("Aguarde");
                                            dialogo.show();

                                            final String agendaenviada = Sincronismo.SincronizarAgendaEnvio(context, NumAgenda, null, dialogo);

                                            if (agendaenviada.equals("OK")) {
                                                Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " foi sincronizado com Sucesso!", Util.PADRAO);
                                            }

                                        } else {
                                            Util.msg_toast_personal(getActivity(), "Sem Conexão com a Internet", Util.PADRAO);
                                            return;
                                        }
                                    } else {
                                        Util.msg_toast_personal(getActivity(), "Somente para agendamentos não sincronizados!", Util.PADRAO);
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Cancelar")) {
                                    if (Situacao == 1) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle(R.string.app_namesair);
                                        builder.setIcon(R.drawable.logo_ico);
                                        builder.setMessage("Deseja realmente cancelar o agendamento " + NumAgenda + "?")
                                                .setCancelable(false)
                                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {

                                                            CancelarAgendamento(NumAgenda);

                                                            Intent intent = ((ConsultaAgenda) getActivity()).getIntent();
                                                            ((ConsultaAgenda) getActivity()).finish();
                                                            startActivity(intent);
                                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " cancelado com Sucesso!", Util.PADRAO);
                                                        } catch (Exception e) {
                                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser cancelado, verifique!", Util.PADRAO);
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
                                        if (Situacao == 2) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser cancelado pois o mesmo já foi finalizado. Verifique!", Util.PADRAO);
                                        } else if (Situacao == 3) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser cancelado pois o mesmo já está cancelado. Verifique!", Util.PADRAO);
                                        }
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Remarcar")) {
                                    if (Situacao == 1) {
                                        Intent CadAgenda = new Intent((ConsultaAgenda) getActivity(), CadastroAgenda.class);
                                        Bundle params = new Bundle();
                                        params.putString(getString(R.string.intent_numagenda), NumAgenda);
                                        params.putString(getString(R.string.intent_codvendedor), Codvendedor);
                                        params.putString(getString(R.string.intent_usuario), usuario);
                                        params.putString(getString(R.string.intent_senha), senha);
                                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                        params.putString(getString(R.string.intent_telainvocada), "Remarcar");
                                        CadAgenda.putExtras(params);
                                        Intent intent = ((ConsultaAgenda) getActivity()).getIntent();
                                        ((ConsultaAgenda) getActivity()).finish();
                                        startActivityForResult(CadAgenda, 1);
                                    } else {
                                        if (Situacao == 2) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser alterado pois o mesmo já foi finalizado. Verifique!", Util.PADRAO);

                                        } else if (Situacao == 3) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser alterado pois o mesmo já foi cancelado. Verifique!", Util.PADRAO);
                                        }
                                    }
                                } else if ((selectedRadioButton.getText().toString().trim()).equals("Finalizar")) {
                                    if (Situacao == 1) {
                                        Intent CadAgenda = new Intent((ConsultaAgenda) getActivity(), CadastroAgenda.class);
                                        Bundle params = new Bundle();
                                        params.putString(getString(R.string.intent_numagenda), NumAgenda);
                                        params.putString(getString(R.string.intent_codvendedor), Codvendedor);
                                        params.putString(getString(R.string.intent_usuario), usuario);
                                        params.putString(getString(R.string.intent_senha), senha);
                                        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                        params.putString(getString(R.string.intent_telainvocada), "Finalizar");
                                        CadAgenda.putExtras(params);
                                        Intent intent = ((ConsultaAgenda) getActivity()).getIntent();
                                        ((ConsultaAgenda) getActivity()).finish();
                                        startActivityForResult(CadAgenda, 1);
                                    } else {
                                        if (Situacao == 2) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser finalizado pois o mesmo já foi finalizado. Verifique!", Util.PADRAO);

                                        } else if (Situacao == 3) {
                                            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser finalizado pois o mesmo já foi cancelado. Verifique!", Util.PADRAO);
                                        }
                                    }
                                }
                                dialog.cancel();
                            }
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void CancelarAgendamento(String NumAgenda) {
        try {
            DB = new ConfigDB(getActivity()).getReadableDatabase();
            Cursor cursoragenda = DB.rawQuery("SELECT * FROM AGENDAS WHERE CODAGENDA_INT = " + NumAgenda + " AND CODPERFIL = " + idPerfil, null);
            if (cursoragenda.getCount() > 0) {
                cursoragenda.moveToFirst();

                DB.execSQL(" UPDATE AGENDA SET SITUACAO = 3, STATUS = 'N' WHERE CODAGENDA_INT = '" + NumAgenda + "' AND CODPERFIL = " + idPerfil);
                cursoragenda.close();
            }
        } catch (Exception e) {
            Util.msg_toast_personal(getActivity(), "Agendamento nº " + NumAgenda + " não pode ser cancelado, verifique!", Util.PADRAO);
        }
    }

    private void carregarpreferencias() {
        SharedPreferences prefs = getActivity().getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }
}


