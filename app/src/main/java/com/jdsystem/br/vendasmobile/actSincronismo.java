package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.jdsystem.br.vendasmobile.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class actSincronismo extends AppCompatActivity implements Runnable {

    private static SQLiteDatabase DB;
    Handler hd;
    ProgressBar PrgGeral;
    int it;
    private ProgressDialog Dialog;
    private static ProgressDialog DialogECB;
    Handler progressHandler;
    //SQLiteDatabase DB;
    Button btnSinc;
    TextView txtSinc;
    ProgressBar prgSinc;
    private static String usuario, senha, sCodVend, URLPrincipal;
    private static Context ctx;

    private static BaseFont bfBold;
    private static BaseFont bf;
    private static int pageNumber = 0;


    public static String DataUltSt2 = null;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_sincronismo);


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
            }
        }
        //DB = new ConfigDB(ctx).getReadableDatabase();

        btnSinc = (Button) findViewById(R.id.btnSincronizar);

        btnSinc.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Dialog = new ProgressDialog(actSincronismo.this);
                                           Dialog.setTitle("Aguarde...");
                                           Dialog.setMessage("");
                                           Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                           Dialog.setProgress(0);
                                           Dialog.setIcon(R.drawable.icon_sync);
                                           Dialog.setMax(0);
                                           Dialog.show();
                                           Dialog.setCancelable(false);

                                           hd = new Handler();
                                           if (VerificaConexao()) {
                                               Thread td = new Thread(actSincronismo.this);
                                               td.start();
                                           } else {
                                               Dialog.cancel();
                                               AlertDialog.Builder builder = new AlertDialog.Builder(actSincronismo.this);
                                               builder.setTitle(R.string.app_namesair);
                                               builder.setIcon(R.drawable.logo_ico);
                                               builder.setMessage("Atenção! Sem conexão com a Internet. Não há possibilidade de sincronização de " +
                                                       "informação com o servidor até que a conexão com a internet seja restabelecida em seu dispositivo. Clique em OK para retornar ou em" +
                                                       "Configurações para ativar a internet")
                                                       .setCancelable(false)
                                                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                           public void onClick(DialogInterface dialog, int id) {
                                                               Intent intent = (actSincronismo.this).getIntent();
                                                               (actSincronismo.this).finish();
                                                               startActivity(intent);
                                                           }
                                                       })
                                                       .setNegativeButton("Configurações", new DialogInterface.OnClickListener() {
                                                           public void onClick(DialogInterface dialog, int id) {
                                                               Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                                               startActivity(intent);
                                                           }
                                                       });
                                               AlertDialog alert = builder.create();
                                               alert.show();
                                               return;
                                           }
                                       }
                                   }
        );

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void run() {
        DB = new ConfigDB(this).getReadableDatabase();

        Cursor CursosParam = DB.rawQuery(" SELECT DT_ULT_ATU FROM PARAMAPP ", null);
        CursosParam.moveToFirst();

        String DataUlt = null;
        String HoraAtu = null;

        try {
            if (CursosParam.getCount() > 0) {
                String dataEmUmFormato = CursosParam.getString(CursosParam.getColumnIndex("DT_ULT_ATU"));
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                Date data = formato.parse(dataEmUmFormato);
                formato.applyPattern("dd/MM/yyyy");
                DataUlt = formato.format(data);

                SimpleDateFormat SdfHora = new SimpleDateFormat("HH:mm");
                HoraAtu = (SdfHora.format(data));
            } else {
                DataUlt = "01/01/2000 12:20:30";
                DB.execSQL(" INSERT INTO PARAMAPP(DT_ULT_ATU) VALUES(datetime());");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String DataUlt2 = DataUlt + " " + HoraAtu;

        SharedPreferences prefs = getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        SincronizarClientes(sCodVend, usuario, senha, DataUlt2);
        SincronizarProdutos(usuario, senha, DataUlt2);
        SincronizarClientesEnvio();
        SincronizarPedidosEnvio();

        DB.execSQL(" UPDATE PARAMAPP SET DT_ULT_ATU = DATETIME();");
        finish();
    }

    public boolean VerificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }


    private void SincronizarClientesEnvio() {
        String Jcliente = null;
        String METHOD_NAMEENVIO = "Cadastrar";
        DB = new ConfigDB(this).getReadableDatabase();

        try {
            Cursor CursorClieEnv = DB.rawQuery(" SELECT CLIENTES.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                    " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN" +
                    " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO WHERE FLAGINTEGRADO = '1' " +
                    " ORDER BY NOMEFAN, NOMERAZAO ", null);

            int jumpTime = 0;
            String CodClie_ext = null;
            final int totalProgressTime = CursorClieEnv.getCount();
            Dialog.setMax(totalProgressTime);
            Dialog.setProgress(jumpTime);
            CursorClieEnv.moveToFirst();
            if (CursorClieEnv.getCount() > 0) {
                CursorClieEnv.moveToFirst();
                do {
                    for (int i = 0; i < CursorClieEnv.getCount(); i++) {
                        do {
                            try {
                                jumpTime += 1;
                                Dialog.setProgress(jumpTime);
                                //Dialog.setMessage("Enviando clientes");
                                Jcliente = "{razao_social: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMERAZAO")) + "'," +
                                        "nome_fantasia: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMEFAN")) + "'," +
                                        "tipo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TIPOPESSOA")) + "'," +
                                        "cnpj_cpf: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'," +
                                        "inscricao_estadual: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("INSCREST")) + "'," +
                                        "Logradouro: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ENDERECO")) + "'," +
                                        "numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NUMERO")) + "'," +
                                        "codvendedor: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODVENDEDOR")) + "'," +
                                        "complemento: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("COMPLEMENT")) + "'," +
                                        "bairro: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("BAIRRO")) + "'," +
                                        "cidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CIDADE")) + "'," +
                                        "estado: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("UF")) + "'," +
                                        "cep: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CEP")) + "'," +
                                        "observacao: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("OBS")) + "'," +
                                        "identidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("REGIDENT")) + "'," +
                                        "emails: [{email: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("EMAIL")) + "'}," +
                                        "{email: ''}]," +
                                        "ativo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ATIVO")) + "'," +
                                        "telefones: [{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL1")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL2")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TELFAX")) + "'}]";

                                String Contatos = "";
                                Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " +
                                        CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODCLIE_INT")), null);

                                CursorContatosEnv.moveToFirst();
                                while (CursorContatosEnv.moveToNext()) {
                                    Contatos = Contatos + "{nome: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("NOME")) + "'," +
                                            "cargo: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CARGO")) + "'," +
                                            "emails: [{email: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("EMAIL")) + "'}]," +
                                            "telefones: [{numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL1")) + "'," +
                                            "numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL2")) + "'}]},";
                                }

                                if (Contatos != "") {
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";
                                } else {
                                    Contatos = "{nome: ''," +
                                            "cargo: ''," +
                                            "emails: [{email: ''}]," +
                                            "telefones: [{numero: ''," +
                                            "numero: ''}]}";
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";
                                }
                                String Dependentes = "{nome: ''," +
                                        "dataadesao: ''," +
                                        "datanascimento: ''," +
                                        "redident: ''," +
                                        "codclie: ''}";
                                Jcliente = Jcliente + ",dependentes: " + "[" + Dependentes + "]}";

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                soap.addProperty("aJson", Jcliente);
                                soap.addProperty("aUsuario", usuario);
                                soap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);
                                String RetClieEnvio = "0";

                                try {
                                    //Boolean ConexOk = true;
                                    Boolean ConexOk = Util.checarConexaoCelular(this);
                                    if (ConexOk == true) {
                                        Envio.call("", envelope);
                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                        RetClieEnvio = (String) envelope.getResponse();
                                        CodClie_ext = RetClieEnvio;
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error" + e);
                                }
                            } catch (Exception E) {
                                Dialog.dismiss();
                                Toast.makeText(this, "Cliente não enviado! Verifique.", Toast.LENGTH_SHORT).show();
                                return;
                                //E.printStackTrace();
                            }
                        }
                        while (jumpTime < totalProgressTime);
                    }
                    try {
                        if (!CodClie_ext.equals("0")) {
                            Cursor CursClieAtu = DB.rawQuery(" SELECT * FROM CLIENTES WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'", null);
                            CursClieAtu.moveToFirst();
                            if (CursClieAtu.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET FLAGINTEGRADO = '2', CODCLIE_EXT = " + CodClie_ext + " WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'");
                            }
                            CursClieAtu.close();
                        }
                    } catch (Exception E) {
                    }
                }
                while (CursorClieEnv.moveToNext());
                CursorClieEnv.close();
            }
            //  if (Dialog.isShowing())
            //    Dialog.dismiss();
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
    }

    public void SincronizarPedidosEnvio() {
        String JPedidos = null;
        String METHOD_NAMEENVIO = "CadastrarPedidos";
        DB = new ConfigDB(this).getReadableDatabase();
        Cursor CursorPedido;
        String RetClieEnvio = null;
        try {
            CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE FLAGINTEGRADO = '5'", null);

            int jumpTime = 0;
            final int totalProgressTime = CursorPedido.getCount();
            Dialog.setMax(totalProgressTime);
            Dialog.setProgress(jumpTime);
            CursorPedido.moveToFirst();

            if (CursorPedido.getCount() > 0) {
                CursorPedido.moveToFirst();
                do {
                    for (int i = 0; i < CursorPedido.getCount(); i++) {
                        do try {
                            jumpTime += 1;
                            Dialog.setProgress(jumpTime);
                            Dialog.setMessage("Sincronizando Tabelas - Pedidos");

                            String ValorFrete = CursorPedido.getString(CursorPedido.getColumnIndex("VLFRETE"));
                            if (Util.isNullOrEmpty(ValorFrete)) {
                                ValorFrete = "0";
                            }
                            String ValorSeguro = CursorPedido.getString(CursorPedido.getColumnIndex("VALORSEGURO"));
                            if (Util.isNullOrEmpty(ValorSeguro)) {
                                ValorSeguro = "0";
                            }

                            String Observacao = CursorPedido.getString(CursorPedido.getColumnIndex("OBS"));
                            String line_separator = System.getProperty("line.separator");
                            String OBS = Observacao.replaceAll("\n|" + line_separator, "");


                            JPedidos = "{codclie_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODCLIE_EXT")) + "'," +
                                    "data_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "hora_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "valor_mercad: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLMERCAD")).replace(".", ",") + "'," +
                                    "valor_frete: '" + ValorFrete + "'," +
                                    "valor_seguro: '" + ValorSeguro + "'," +
                                    "dataentregaprevista: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAPREVISTAENTREGA")) + "'," +
                                    "valor_desconto: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLPERCACRES")).replace(".", ",") + "'," +
                                    "obs_pedido: '" + OBS + "'," +
                                    "numpedido_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED")) + "'," +
                                    "chavepedido: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'," +
                                    "codempresa: '" + 1 + "'," +
                                    "cod_vendedor: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODVENDEDOR")) + "',";

                            String PedItems = "";
                            Cursor CursorItensEnv = DB.rawQuery(" SELECT * FROM PEDITENS WHERE CHAVEPEDIDO = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);

                            CursorItensEnv.moveToFirst();
                            do {
                                PedItems = PedItems + "{codigo_manual: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODITEMANUAL")) + "'," +
                                        "descricao: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")) + "'," +
                                        "numeroitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("NUMEROITEM")) + "'," +
                                        "qtdmenorped: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")) + "'," +
                                        "vlunit: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ",") + "'," +
                                        "valortotal: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ",") + "'}";

                                if (!CursorItensEnv.isLast()) {
                                    PedItems = PedItems + ",";
                                }

                            } while (CursorItensEnv.moveToNext());

                            if (PedItems != "") {
                                JPedidos = JPedidos + "produtos: " + "[" + PedItems + "]";
                            }
                            String PedParcelas = "";
                            Cursor CursorParcelasEnv = DB.rawQuery(" SELECT * FROM CONREC WHERE vendac_chave = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                            CursorParcelasEnv.moveToFirst();
                            do {
                                PedParcelas = PedParcelas + "{chavepedido: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave")) + "'," +
                                        "numparcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela")) + "'," +
                                        "valor_receber: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber")) + "'," +
                                        "datavencimento: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_datavencimento")) + "'}";

                                if (!CursorParcelasEnv.isLast()) {
                                    PedParcelas = PedParcelas + ",";
                                }
                            } while (CursorParcelasEnv.moveToNext());

                            if (PedParcelas != "") {
                                JPedidos = JPedidos + ",formapgto: " + "[" + PedParcelas + "]";
                            }

                            JPedidos = JPedidos + '}';

                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                            soap.addProperty("aJson", JPedidos);
                            soap.addProperty("aUsuario", usuario);
                            soap.addProperty("aSenha", senha);
                            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelope.setOutputSoapObject(soap);
                            HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                            try {
                                Boolean ConexOk = true;
                                //Boolean ConexOk = Util.checarConexaoCelular(this);
                                if (ConexOk == true) {
                                    Envio.call("", envelope);
                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                    RetClieEnvio = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                System.out.println("Error" + e);
                            }
                            try {
                                DB = new ConfigDB(this).getReadableDatabase();
                                Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                                CursPedAtu.moveToFirst();
                                if (CursPedAtu.getCount() > 0) {
                                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '2', NUMPEDIDOERP = " + RetClieEnvio + " WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'");
                                }
                                CursPedAtu.close();
                            } catch (Exception E) {
                                Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception E) {
                            // TODO Auto-generated catch block
                            E.printStackTrace();
                        }
                        while (jumpTime < totalProgressTime);
                    }

                    JPedidos = "";
                }
                while (CursorPedido.moveToNext());
                CursorPedido.close();
            }

            if (Dialog.isShowing())
                Dialog.dismiss();
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
    }

    private void SincronizarProdutos(String nUsuario, String nSenha, String DtUlt) {
        String METHOD_NAME = "Carregar";
        String TAG_PRODUTOSINFO = "produtos";

        String TAG_CODIGOITEM = "codigoitem";
        String TAG_CODMANUAL = "coditemanual";
        String TAG_DESCRICAO = "descricao";
        String TAG_UNIVENDA = "univenda";
        String TAG_VLVENDA1 = "vlvenda1";
        String TAG_VLVENDA2 = "vlvenda2";
        String TAG_VLVENDA3 = "vlvenda3";
        String TAG_VLVENDA4 = "vlvenda4";
        String TAG_VLVENDA5 = "vlvenda5";
        String TAG_VLVENDAP1 = "vlvendap1";
        String TAG_VLVENDAP2 = "vlvendap2";
        String TAG_VENDAPADRAO = "vendapadrao";
        String TAG_MARCA = "marca";
        String TAG_CLASSE = "classe";
        String TAG_FABRICANTE = "fabricante";
        String TAG_FORNECEDOR = "fornecedor";
        String TAG_APRESENTACAO = "apresentacao";
        String TAG_ATIVO = "ativo";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aParam", "D" + DtUlt);
        soap.addProperty("aUsuario", nUsuario);
        soap.addProperty("aSenha", nSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS);
        String RetProdutos = null;

        try {
            //Boolean ConexOk = true;
            Boolean ConexOk = Util.checarConexaoCelular(this);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetProdutos = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetProdutos);
            JSONArray ProdItens = jsonObj.getJSONArray(TAG_PRODUTOSINFO);

            int jumpTime = 0;
            final int totalProgressTime = ProdItens.length();
            DB = new ConfigDB(this).getReadableDatabase();
            Dialog.setMax(totalProgressTime);
            Dialog.setProgress(jumpTime);
            for (int i = 0; i < ProdItens.length(); i++) {
                do {
                    try {
                        JSONObject CItens = ProdItens.getJSONObject(jumpTime);
                        jumpTime += 1;
                        Dialog.setProgress(jumpTime);

                        //Dialog.setMessage("Sincronizando Tabelas - Produtos");

                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM), null);
                        try {
                            CursItens.moveToFirst();
                            if (CursItens.getCount() > 0) {
                                DB.execSQL(" UPDATE ITENS SET CODITEMANUAL = '" + CItens.getString(TAG_CODMANUAL) +
                                        "', DESCRICAO = '" + CItens.getString(TAG_DESCRICAO) +
                                        "', FABRICANTE = '" + CItens.getString(TAG_FABRICANTE) +
                                        "', FORNECEDOR = '" + CItens.getString(TAG_FORNECEDOR) +
                                        "', CLASSE = '" + CItens.getString(TAG_CLASSE) +
                                        "', MARCA = '" + CItens.getString(TAG_MARCA) +
                                        "', UNIVENDA = '" + CItens.getString(TAG_UNIVENDA) +
                                        "', VLVENDA1 = '" + CItens.getString(TAG_VLVENDA1) +
                                        "', VLVENDA2 = '" + CItens.getString(TAG_VLVENDA2) +
                                        "', VLVENDA3 = '" + CItens.getString(TAG_VLVENDA3) +
                                        "', VLVENDA4 = '" + CItens.getString(TAG_VLVENDA4) +
                                        "', VLVENDA5 = '" + CItens.getString(TAG_VLVENDA5) +
                                        "', VLVENDAP1 = '" + CItens.getString(TAG_VLVENDAP1) +
                                        "', VENDAPADRAO = '" + CItens.getString(TAG_VENDAPADRAO) +
                                        "', VLVENDAP2 = '" + CItens.getString(TAG_VLVENDAP2) +
                                        "', ATIVO = '" + CItens.getString(TAG_ATIVO) +
                                        "', APRESENTACAO = '" + CItens.getString(TAG_APRESENTACAO) + "'" +
                                        " WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM));
                            } else {
                                DB.execSQL("INSERT INTO ITENS (CODIGOITEM, CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA, " +
                                        "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2, VENDAPADRAO, " +
                                        "ATIVO, APRESENTACAO) VALUES(" + "'" + CItens.getString(TAG_CODIGOITEM) +
                                        "',' " + CItens.getString(TAG_CODMANUAL) +
                                        "','" + CItens.getString(TAG_DESCRICAO) +
                                        "',' " + CItens.getString(TAG_FABRICANTE) +
                                        "',' " + CItens.getString(TAG_FORNECEDOR) +
                                        "',' " + CItens.getString(TAG_CLASSE) +
                                        "',' " + CItens.getString(TAG_MARCA) +
                                        "', '" + CItens.getString(TAG_UNIVENDA) +
                                        "',' " + CItens.getString(TAG_VLVENDA1) +
                                        "', '" + CItens.getString(TAG_VLVENDA2) +
                                        "',' " + CItens.getString(TAG_VLVENDA3) +
                                        "',' " + CItens.getString(TAG_VLVENDA4) +
                                        "','" + CItens.getString(TAG_VLVENDA5) +
                                        "',' " + CItens.getString(TAG_VLVENDAP1) +
                                        "',' " + CItens.getString(TAG_VLVENDAP2) +
                                        "', '" + CItens.getString(TAG_VENDAPADRAO) +
                                        "', '" + CItens.getString(TAG_ATIVO) +
                                        "',' " + CItens.getString(TAG_APRESENTACAO) + "');");
                            }
                            CursItens.close();
                        } catch (Exception E) {
                            System.out.println("Error" + E);
                        }

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }
                while (jumpTime < totalProgressTime);
            }
            //  if (Dialog.isShowing())
            //     Dialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void SincronizarClientes(String sCodVend, String nUsuario, String nSenha, String DtUlt) {
        String METHOD_NAME = "Carregar";
        String TAG_CLIENTESINFO = "clientes";
        String TAG_TELEFONESINFO = "telefones";
        String TAG_CONTATOSINFO = "contatos";

        String TAG_CODIGO = "codigo";
        String TAG_RAZAOSOCIAL = "razao_social";
        String TAG_NOMEFANTASIA = "nome_fantasia";
        String TAG_TIPO = "tipo";
        String TAG_CNPJCPF = "cnpj_cpf";
        String TAG_INSCESTADUAL = "inscricao_estadual";
        String TAG_LOGRADOURO = "Logradouro";
        String TAG_NUMERO = "numero";
        String TAG_COMPLEMENTO = "complemento";
        String TAG_BAIRRO = "bairro";
        String TAG_CIDADE = "cidade";
        String TAG_ESTADO = "estado";
        String TAG_CEP = "cep";
        String TAG_RG = "identidade";
        String TAG_OBS = "observacao";
        String TAG_EMAILS = "emails";
        String TAG_ATIVO = "ativo";

        String CodVendedor = sCodVend;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
        soap.addProperty("aUsuario", nUsuario);
        soap.addProperty("aSenha", nSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);
        String RetClientes = null;

        try {
            Boolean ConexOk = true;
            //Boolean ConexOk =
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetClientes = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            //String SHA1Ret = RetClientes.substring(0, 40);
            //String ArrayClientes = RetClientes.substring(40, (RetClientes.length()));

            JSONObject jsonObj = new JSONObject(RetClientes);
            JSONArray pedidosblq = jsonObj.getJSONArray(TAG_CLIENTESINFO);

            int jumpTime = 0;
            Dialog.setProgress(jumpTime);
            final int totalProgressTime = pedidosblq.length();
            Dialog.setMax(totalProgressTime);

            String CodCliente = null;
            String CodClienteExt = null;

            DB = new ConfigDB(this).getReadableDatabase();

            for (int i = 0; i < pedidosblq.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = pedidosblq.getJSONObject(jumpTime);

                        String Telefone = c.getString(TAG_TELEFONESINFO);
                        Telefone = "{\"telefones\":" + Telefone + "\t}";
                        JSONObject ObjTel = new JSONObject(Telefone);
                        JSONArray Telef = ObjTel.getJSONArray("telefones");
                        String Tel1 = null;
                        String Tel2 = null;
                        String Tel3 = null;

                        for (int t = 0; t < Telef.length(); t++) {
                            JSONObject tt = Telef.getJSONObject(t);
                            if (t == 0) {
                                Tel1 = tt.getString("numero");
                            }
                            if (t == 1) {
                                Tel2 = tt.getString("numero");
                            }
                            if (t == 2) {
                                Tel3 = tt.getString("numero");
                            }
                        }

                        jumpTime += 1;
                        Dialog.setProgress(jumpTime);
                        //Dialog.setMessage("Sincronizando Tabelas - Clientes");

                        Cursor cursor = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'", null);

                        String CodEstado = RetornaEstado(c.getString(TAG_ESTADO));
                        int CodCidade = RetornaCidade(c.getString(TAG_CIDADE), CodEstado);
                        int CodBairro = RetornaBairro(c.getString(TAG_BAIRRO), CodCidade);

                        try {
                            if (cursor.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + c.getString(TAG_RAZAOSOCIAL) +
                                        "', NOMEFAN = '" + c.getString(TAG_NOMEFANTASIA) +
                                        "', REGIDENT = '" + c.getString(TAG_RG) +
                                        "', INSCREST = '" + c.getString(TAG_INSCESTADUAL) + "', EMAIL = '" + c.getString(TAG_EMAILS) +
                                        "', TEL1 = '" + Tel1 + "', TEL2 = '" + Tel2 + "', ENDERECO = '" + c.getString(TAG_LOGRADOURO) +
                                        "', NUMERO = '" + c.getString(TAG_NUMERO) + "', COMPLEMENT = '" + c.getString(TAG_COMPLEMENTO) +
                                        "', CODBAIRRO = '" + CodBairro + "', OBS = '" + c.getString(TAG_OBS) + "', CODCIDADE = '" + CodCidade + "', UF = '" + CodEstado +
                                        "', CEP = '" + c.getString(TAG_CEP) + "', CODCLIE_EXT = '" + c.getString(TAG_CODIGO) + "', " +
                                        " TIPOPESSOA = '" + c.getString(TAG_TIPO) + "', ATIVO = '" + c.getString(TAG_ATIVO) + "'" +
                                        ", CODVENDEDOR = '" + CodVendedor + "', FLAGINTEGRADO = '2' " +
                                        " WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'");
                            } else {
                                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, REGIDENT, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                                        "CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO, FLAGINTEGRADO) VALUES(" +
                                        "'" + c.getString(TAG_CNPJCPF) + "','" + c.getString(TAG_RAZAOSOCIAL) + "','" + c.getString(TAG_RG) +
                                        "',' " + c.getString(TAG_NOMEFANTASIA) + "',' " + c.getString(TAG_INSCESTADUAL) + "',' " + c.getString(TAG_EMAILS) +
                                        "',' " + Tel1 + "', '" + Tel2 + "', '" + c.getString(TAG_LOGRADOURO) +
                                        "',' " + c.getString(TAG_NUMERO) + "', '" + c.getString(TAG_COMPLEMENTO) +
                                        "','" + CodBairro + "',' " + c.getString(TAG_OBS) + "','" + CodCidade + "',' " + CodEstado +
                                        "',' " + c.getString(TAG_CEP) + "', '" + c.getString(TAG_CODIGO) +
                                        "','" + CodVendedor + "','" + c.getString(TAG_TIPO) + "','" + c.getString(TAG_ATIVO)
                                        + "','" + "2" + "');"); // FLAGINTEGRADO = 2, Significa que o cliente já está integrado e existe na base da retaguarda.
                            }

                            Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'", null);
                            cursor1.moveToFirst();
                            CodCliente = cursor1.getString(cursor1.getColumnIndex("CODCLIE_INT"));
                            CodClienteExt = cursor1.getString(cursor1.getColumnIndex("CODCLIE_EXT"));

                            cursor.close();
                            cursor1.close();

                        } catch (Exception E) {
                            System.out.println("Error" + E);
                        }

                        Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " + CodCliente, null);
                        if ((CursorContatosEnv.getCount() > 0)) {
                            DB.execSQL("DELETE FROM CONTATO WHERE CODCLIENTE = " + CodCliente);
                            CursorContatosEnv.close();
                        }

                        String Contatos = c.getString(TAG_CONTATOSINFO);
                        Contatos = "{\"contatos\":" + Contatos + "\t}";
                        JSONObject ObjCont = new JSONObject(Contatos);
                        JSONArray Cont = ObjCont.getJSONArray("contatos");
                        String NomeContato = null;
                        String CargoContato = null;
                        String EmailContato = null;
                        String Tel1Contato = null;
                        String Tel2Contato = null;

                        try {
                            for (int co = 0; co < Cont.length(); co++) {
                                JSONObject cc = Cont.getJSONObject(co);
                                if (co == 0) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont1 = cc.getString("telefones");
                                    TelCont1 = "{\"telefones\":" + TelCont1 + "\t}";
                                    JSONObject ObjTelC1 = new JSONObject(TelCont1);
                                    JSONArray TelefC1 = ObjTelC1.getJSONArray("telefones");

                                    for (int tc1 = 0; tc1 < TelefC1.length(); tc1++) {
                                        JSONObject tt1 = TelefC1.getJSONObject(tc1);
                                        if (tc1 == 0) {
                                            Tel1Contato = tt1.getString("numero");
                                        }
                                        if (tc1 == 1) {
                                            Tel2Contato = tt1.getString("numero");
                                        }
                                    }
                                }
                                if (co == 1) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont2 = cc.getString("telefones");
                                    TelCont2 = "{\"telefones\":" + TelCont2 + "\t}";
                                    JSONObject ObjTelC2 = new JSONObject(TelCont2);
                                    JSONArray TelefC2 = ObjTelC2.getJSONArray("telefones");

                                    for (int tc2 = 0; tc2 < TelefC2.length(); tc2++) {
                                        JSONObject tt2 = TelefC2.getJSONObject(tc2);
                                        if (tc2 == 0) {
                                            Tel1Contato = tt2.getString("numero");
                                        }
                                        if (tc2 == 1) {
                                            Tel2Contato = tt2.getString("numero");
                                        }
                                    }
                                }
                                if (co == 2) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont3 = cc.getString("telefones");
                                    TelCont3 = "{\"telefones\":" + TelCont3 + "\t}";
                                    JSONObject ObjTelC3 = new JSONObject(TelCont3);
                                    JSONArray TelefC3 = ObjTelC3.getJSONArray("telefones");

                                    for (int tc3 = 0; tc3 < TelefC3.length(); tc3++) {
                                        JSONObject tt3 = TelefC3.getJSONObject(tc3);
                                        if (tc3 == 0) {
                                            Tel1Contato = tt3.getString("numero");
                                        }
                                        if (tc3 == 1) {
                                            Tel2Contato = tt3.getString("numero");
                                        }
                                    }
                                }

                                try {
                                    DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, CODCLIENTE ) VALUES(" +
                                            "'" + NomeContato + "','" + CargoContato +
                                            "',' " + EmailContato + "',' " + Tel1Contato + "',' " + Tel2Contato + "'" +
                                            "," + CodCliente + ");");

                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }

                            }
                        } catch (Exception e) {
                            e.toString();
                        }

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }
            }
            //if (Dialog.isShowing())
            //   Dialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static int RetornaBairro(String NomeBairro, int CodCidade) {
        int Bairro = 0;
        try {
            Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, CODCIDADE, DESCRICAO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
            if (CursorBairro.getCount() > 0) {
                CursorBairro.moveToFirst();
                Bairro = CursorBairro.getInt(CursorBairro.getColumnIndex("CODBAIRRO"));
            } else {
                DB.execSQL(" INSERT INTO BAIRROS (CODCIDADE, DESCRICAO )" +
                        " VALUES('" + CodCidade + "','" + NomeBairro + "');");
                Cursor CursorBairro2 = DB.rawQuery(" SELECT CODBAIRRO, CODCIDADE, DESCRICAO FROM BAIRROS WHERE CODCIDADE = '" + CodCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
                CursorBairro2.moveToFirst();
                Bairro = CursorBairro2.getInt(CursorBairro2.getColumnIndex("CODBAIRRO"));
                CursorBairro2.close();
            }
            CursorBairro.close();
            return Bairro;
        } catch (Exception E) {
            return Bairro;
        }
    }

    private static int RetornaCidade(String NomeCidade, String NomeEstado) {
        int Cidade = 0;
        try {
            Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
            if (CursorCidade.getCount() > 0) {
                CursorCidade.moveToFirst();
                Cidade = CursorCidade.getInt(CursorCidade.getColumnIndex("CODCIDADE"));
            } else {
                DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF)" +
                        " VALUES('" + NomeCidade + "','" + NomeEstado + "');");
                Cursor CursorCidade2 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF FROM CIDADES WHERE UF = '" + NomeEstado + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                CursorCidade2.moveToFirst();
                Cidade = CursorCidade2.getInt(CursorCidade2.getColumnIndex("CODCIDADE"));
                CursorCidade2.close();
            }
            CursorCidade.close();
            return Cidade;
        } catch (Exception E) {
            return Cidade;
        }
    }

    private static String RetornaEstado(String NomeEstado) {
        String Estado = null;
        try {
            Cursor CursosEstado = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + NomeEstado + "'", null);
            if (CursosEstado.getCount() > 0) {
                CursosEstado.moveToFirst();
                Estado = CursosEstado.getString(CursosEstado.getColumnIndex("UF"));
            } else {
                DB.execSQL("INSERT INTO ESTADOS VALUES('" + NomeEstado + "','" + NomeEstado + "');");
                Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + NomeEstado + "'", null);
                cursor1.moveToFirst();
                Estado = cursor1.getString(cursor1.getColumnIndex("UF"));
                cursor1.close();
            }
            CursosEstado.close();

            return Estado;
        } catch (Exception E) {
            return null;
        }
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("actSincronismo Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /*
    * Métodos que podem ser invocados de outras activity
    * */

    public static void SincronizarPedidosEnvioStatic(String sUsuario, String sSenha, Context ctxPedEnv) {
        String JPedidos = null;
        String METHOD_NAMEENVIO = "CadastrarPedidos";
        DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
        ProgressDialog Dialog = null;
        Cursor CursorPedido;
        String RetClieEnvio = null;

        Dialog = new ProgressDialog(ctxPedEnv);
        Dialog.setTitle("Aguarde...");
        Dialog.setMessage("Sincronizando");
        Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        Dialog.setProgress(0);
        Dialog.setIcon(R.drawable.icon_sync);
        Dialog.setMax(0);
        Dialog.show();

        try {
            CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE FLAGINTEGRADO = '5' ", null);

            int jumpTime = 0;
            final int totalProgressTime = CursorPedido.getCount();
            Dialog.setMax(totalProgressTime);
            Dialog.setProgress(jumpTime);
            CursorPedido.moveToFirst();

            if (CursorPedido.getCount() > 0) {
                CursorPedido.moveToFirst();
                do {
                    for (int i = 0; i < CursorPedido.getCount(); i++) {
                        do try {
                            jumpTime += 1;
                            Dialog.setProgress(jumpTime);
                            Dialog.setMessage("Sincronizando Tabelas - Pedidos");

                            String ValorFrete = CursorPedido.getString(CursorPedido.getColumnIndex("VLFRETE"));
                            if (Util.isNullOrEmpty(ValorFrete)) {
                                ValorFrete = "0";
                            }
                            String ValorSeguro = CursorPedido.getString(CursorPedido.getColumnIndex("VALORSEGURO"));
                            if (Util.isNullOrEmpty(ValorSeguro)) {
                                ValorSeguro = "0";
                            }

                            String Observacao = CursorPedido.getString(CursorPedido.getColumnIndex("OBS"));
                            String line_separator = System.getProperty("line.separator");
                            String OBS = Observacao.replaceAll("\n|" + line_separator, "");


                            JPedidos = "{codclie_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODCLIE_EXT")) + "'," +
                                    "data_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "hora_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "valor_mercad: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLMERCAD")).replace(".", ",") + "'," +
                                    "valor_frete: '" + ValorFrete + "'," +
                                    "valor_seguro: '" + ValorSeguro + "'," +
                                    "dataentregaprevista: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAPREVISTAENTREGA")) + "'," +
                                    "valor_desconto: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLPERCACRES")).replace(".", ",") + "'," +
                                    "obs_pedido: '" + OBS + "'," +
                                    "numpedido_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED")) + "'," +
                                    "chavepedido: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'," +
                                    "codempresa: '" + 1 + "'," +
                                    "cod_vendedor: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODVENDEDOR")) + "',";

                            String PedItems = "";
                            Cursor CursorItensEnv = DB.rawQuery(" SELECT * FROM PEDITENS WHERE CHAVEPEDIDO = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);

                            CursorItensEnv.moveToFirst();
                            do {
                                PedItems = PedItems + "{codigo_manual: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODITEMANUAL")) + "'," +
                                        "descricao: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")) + "'," +
                                        "numeroitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("NUMEROITEM")) + "'," +
                                        "qtdmenorped: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")) + "'," +
                                        "vlunit: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ",") + "'," +
                                        "valortotal: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ",") + "'}";

                                if (!CursorItensEnv.isLast()) {
                                    PedItems = PedItems + ",";
                                }

                            } while (CursorItensEnv.moveToNext());

                            if (PedItems != "") {
                                JPedidos = JPedidos + "produtos: " + "[" + PedItems + "]";
                            }
                            String PedParcelas = "";
                            Cursor CursorParcelasEnv = DB.rawQuery(" SELECT * FROM CONREC WHERE vendac_chave = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                            CursorParcelasEnv.moveToFirst();
                            do {
                                PedParcelas = PedParcelas + "{chavepedido: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave")) + "'," +
                                        "numparcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela")) + "'," +
                                        "valor_receber: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber")) + "'," +
                                        "datavencimento: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_datavencimento")) + "'}";

                                if (!CursorParcelasEnv.isLast()) {
                                    PedParcelas = PedParcelas + ",";
                                }
                            } while (CursorParcelasEnv.moveToNext());

                            if (PedParcelas != "") {
                                JPedidos = JPedidos + ",formapgto: " + "[" + PedParcelas + "]";
                            }

                            JPedidos = JPedidos + '}';

                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            SharedPreferences prefsHost = ctxPedEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
                            URLPrincipal = prefsHost.getString("host", null);

                            SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                            soap.addProperty("aJson", JPedidos);
                            soap.addProperty("aUsuario", sUsuario);
                            soap.addProperty("aSenha", sSenha);
                            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelope.setOutputSoapObject(soap);
                            HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                            try {
                                Boolean ConexOk = true;
                                //Boolean ConexOk = Util.checarConexaoCelular(this);
                                if (ConexOk == true) {
                                    Envio.call("", envelope);
                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                    RetClieEnvio = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                System.out.println("Error" + e);
                            }
                            try {
                                DB = new ConfigDB(ctxPedEnv).getReadableDatabase();
                                Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                                CursPedAtu.moveToFirst();
                                if (CursPedAtu.getCount() > 0) {
                                    DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '2', NUMPEDIDOERP = " + RetClieEnvio + " WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'");
                                }
                                CursPedAtu.close();
                            } catch (Exception E) {
                                Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception E) {
                            // TODO Auto-generated catch block
                            E.printStackTrace();
                        }
                        while (jumpTime < totalProgressTime);
                    }

                    JPedidos = "";
                }
                while (CursorPedido.moveToNext());
                CursorPedido.close();
            }

            if (Dialog.isShowing())
                Dialog.dismiss();
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
    }

    public static Boolean SincAtuEstado(final Context ctxEnv) {
        DialogECB = new ProgressDialog(ctxEnv);
        DialogECB.setTitle("Aguarde...");
        DialogECB.setMessage("Sincronizando");
        DialogECB.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DialogECB.setProgress(0);
        DialogECB.setIcon(R.drawable.icon_sync);
        DialogECB.setMax(0);
        DialogECB.show();

        String Estado = null;
        String Cidade = null;
        String Bairro = null;
        Boolean AtualizaEst = true;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Estados");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLDADOSCEP);
        String RetEstados = null;

        try {
            Boolean ConexOk = true;
            //Boolean ConexOk = actLogin.VerificaConexao();
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetEstados = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetEstados);
            JSONArray JEstados = jsonObj.getJSONArray("estados");

            int jumpTime = 0;
            DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JEstados.length();
            DialogECB.setMax(totalProgressTime);
            DB = new ConfigDB(ctxEnv).getReadableDatabase();


            Cursor CursorAtu = DB.rawQuery(" SELECT * FROM ESTADOS ", null);
            if (CursorAtu.getCount() > 0) {
                AtualizaEst = true;
            }

            if (AtualizaEst.equals(true)) {

                for (int i = 0; i < JEstados.length(); i++) {
                    while (jumpTime < totalProgressTime) {
                        try {
                            JSONObject c = JEstados.getJSONObject(jumpTime);
                            jumpTime += 1;
                            DialogECB.setProgress(jumpTime);
                            DialogECB.setMessage("Sincronizando Tabelas - Estados");
                            String SiglaEstado = c.getString("uf");
                            String NomeEstado = c.getString("estado");

                            Cursor CursosEstado = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);

                            if (CursosEstado.getCount() > 0) {
                                DB.execSQL(" UPDATE ESTADOS SET UF = '" + SiglaEstado + "', DESCRICAO = '" + NomeEstado + "'" +
                                        " WHERE UF = '" + SiglaEstado + "'");
                                Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);
                                cursor1.moveToFirst();
                                Estado = cursor1.getString(CursosEstado.getColumnIndex("UF"));
                                cursor1.close();
                            } else {
                                DB.execSQL("INSERT INTO ESTADOS VALUES('" + SiglaEstado + "','" + NomeEstado + "');");
                                Cursor cursor1 = DB.rawQuery(" SELECT UF, DESCRICAO FROM ESTADOS WHERE UF = '" + SiglaEstado + "'", null);
                                cursor1.moveToFirst();
                                Estado = cursor1.getString(cursor1.getColumnIndex("UF"));
                                cursor1.close();
                            }
                            CursosEstado.close();
                            SincAtualizaCidade(Estado, ctxEnv);
                        } catch (Exception E) {
                            // TODO Auto-generated catch block
                            E.printStackTrace();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();
        return AtualizaEst;
    }

    private static void SincAtualizaCidade(String UF, Context ctxEnv) {
        int CodCidade = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Cidades");
        soap.addProperty("aUF", UF);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLDADOSCEP);
        String RetCidades = null;

        try {
            //Boolean ConexOk = true;
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetCidades = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetCidades);
            JSONArray JCidades = jsonObj.getJSONArray("cidades");

            int jumpTime = 0;
            DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JCidades.length();
            DialogECB.setMax(totalProgressTime);
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JCidades.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = JCidades.getJSONObject(jumpTime);
                        jumpTime += 1;
                        DialogECB.setProgress(jumpTime);
                        DialogECB.setMessage("Sincronizando Tabelas - Cidades");
                        String NomeCidade = c.getString("cidade");
                        int CodCidadeExt = c.getInt("id_cidade");

                        Cursor CursorCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT UF FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                        if (CursorCidade.getCount() > 0) {
                            DB.execSQL(" UPDATE CIDADES SET UF = '" + UF + "', DESCRICAO = '" + NomeCidade + "', CODCIDADE_EXT = '" + CodCidadeExt + "'" +
                                    " WHERE DESCRICAO = '" + NomeCidade + "'");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidadeExt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE_EXT"));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO CIDADES (DESCRICAO, UF, CODCIDADE_EXT)" +
                                    " VALUES('" + NomeCidade + "','" + UF + "', '" + CodCidadeExt + "');");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, UF, CODCIDADE_EXT FROM CIDADES WHERE UF = '" + UF + "' AND DESCRICAO = '" + NomeCidade + "'", null);
                            cursor1.moveToFirst();
                            CodCidadeExt = cursor1.getInt(cursor1.getColumnIndex("CODCIDADE_EXT"));
                            cursor1.close();
                        }
                        CursorCidade.close();
                        SincAtualizaBairro(CodCidadeExt, ctxEnv);

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

    }

    private static void SincAtualizaBairro(int codCidade, Context ctxEnv) {
        int CodBairro = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "Bairros");
        soap.addProperty("aIdCidade", codCidade);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLDADOSCEP);
        String RetBairros = null;

        try {
            Boolean ConexOk = true;
            //Boolean ConexOk = actLogin.VerificaConexao();
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetBairros = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetBairros);
            JSONArray JBairros = jsonObj.getJSONArray("bairros");

            int jumpTime = 0;
            DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JBairros.length();
            DialogECB.setMax(totalProgressTime);
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JBairros.length(); i++) {
                do {
                    try {
                        JSONObject c = JBairros.getJSONObject(jumpTime);
                        jumpTime += 1;
                        DialogECB.setProgress(jumpTime);
                        DialogECB.setMessage("Sincronizando Tabelas - Bairros");
                        String NomeBairro = c.getString("bairro");
                        int CodBairroExt = c.getInt("id_bairro");

                        Cursor CursorBairro = DB.rawQuery(" SELECT CODBAIRRO, DESCRICAO, CODBAIRRO_EXT, CODCIDADE FROM BAIRROS WHERE CODCIDADE = '" + codCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
                        if (CursorBairro.getCount() > 0) {
                            DB.execSQL(" UPDATE BAIRROS SET CODCIDADE = '" + codCidade + "', DESCRICAO = '" + NomeBairro + "', CODBAIRRO_EXT = '" + CodBairroExt + "'" +
                                    " WHERE DESCRICAO = '" + NomeBairro + "' AND CODCIDADE = '" + codCidade + "'");
                            Cursor cursor1 = DB.rawQuery(" SELECT DESCRICAO, CODCIDADE, CODBAIRRO_EXT FROM BAIRROS WHERE CODCIDADE = '" + codCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
                            cursor1.moveToFirst();
                            CodBairroExt = cursor1.getInt(cursor1.getColumnIndex("CODBAIRRO_EXT"));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO BAIRROS (DESCRICAO, CODBAIRRO_EXT, CODCIDADE)" +
                                    " VALUES('" + NomeBairro + "','" + CodBairroExt + "', '" + codCidade + "');");
                            Cursor cursor1 = DB.rawQuery(" SELECT DESCRICAO, CODCIDADE, CODBAIRRO_EXT FROM BAIRROS WHERE CODCIDADE = '" + codCidade + "' AND DESCRICAO = '" + NomeBairro + "'", null);
                            cursor1.moveToFirst();
                            CodBairroExt = cursor1.getInt(cursor1.getColumnIndex("CODBAIRRO_EXT"));
                            cursor1.close();
                        }
                        CursorBairro.close();

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                } while (jumpTime < totalProgressTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DialogECB.isShowing())
            DialogECB.dismiss();

    }

    public static boolean CancelarPedidoAberto(String NumPedido, Context ctxCanc) {

        DB = new ConfigDB(ctxCanc).getReadableDatabase();

        try {
            DB = new ConfigDB(ctxCanc).getReadableDatabase();
            Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + NumPedido + "'", null);
            CursPedAtu.moveToFirst();
            if (CursPedAtu.getCount() > 0) {
                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '4' WHERE NUMPED = '" + NumPedido + "'");
            }
            CursPedAtu.close();
        } catch (Exception E) {
            E.toString();
            return false;
        }
        return true;
    }

    public static boolean AutorizaPedidoAberto(String NumPedido, Context ctxCanc) {

        DB = new ConfigDB(ctxCanc).getReadableDatabase();

        try {
            DB = new ConfigDB(ctxCanc).getReadableDatabase();
            Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + NumPedido + "'", null);
            CursPedAtu.moveToFirst();
            if (CursPedAtu.getCount() > 0) {
                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '5' WHERE NUMPED = '" + NumPedido + "'");
            }
            CursPedAtu.close();
        } catch (Exception E) {
            E.toString();
            return false;
        }
        return true;
    }


    public static String GerarPdf(String NumPedido, Context ctxRetPed) {
        DB = new ConfigDB(ctxRetPed).getReadableDatabase();

        String Pedido = "";
        String ItensPedido = "";
        String Situacao = "";
        PdfWriter docWriter = null;

        try {
            DB = new ConfigDB(ctxRetPed).getReadableDatabase();
            Cursor CursPedido = DB.rawQuery(" SELECT PEDOPER.*, EMPRESAS.NOMEEMPRE, USUARIOS.USUARIO, EMPRESAS.LOGO FROM PEDOPER LEFT OUTER JOIN " +
                    " EMPRESAS ON PEDOPER.CODEMPRESA = EMPRESAS.CODEMPRESA LEFT OUTER JOIN " +
                    " USUARIOS ON PEDOPER.CODVENDEDOR = USUARIOS.CODVEND " +
                    " WHERE NUMPED = '" + NumPedido + "'", null);
            CursPedido.moveToFirst();

            String dataEmUmFormato = CursPedido.getString(CursPedido.getColumnIndex("DATAEMIS"));
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            Date data = formato.parse(dataEmUmFormato);
            formato.applyPattern("dd/MM/yyyy");
            String sDataVenda = formato.format(data);

            Double VlTotal = (CursPedido.getDouble(CursPedido.getColumnIndex("VALORTOTAL")) -
                    CursPedido.getDouble(CursPedido.getColumnIndex("VLPERCACRES")));

            String VlDesc = CursPedido.getString(CursPedido.getColumnIndex("VLPERCACRES"));
            VlDesc = VlDesc.replace('.', ',');
            Double VlSubTot = CursPedido.getDouble(CursPedido.getColumnIndex("VALORTOTAL"));

            String STotal = String.valueOf(VlSubTot);
            java.math.BigDecimal Subvenda = new java.math.BigDecimal(Double.parseDouble(STotal.replace(',', '.')));
            String SubTotal = Subvenda.setScale(2, java.math.BigDecimal.ROUND_UP).toString();
            SubTotal = SubTotal.replace('.', ',');

            String valor = String.valueOf(VlTotal);
            java.math.BigDecimal venda = new java.math.BigDecimal(Double.parseDouble(valor.replace(',', '.')));
            String ValorTotal = venda.setScale(2, java.math.BigDecimal.ROUND_UP).toString();
            ValorTotal = ValorTotal.replace('.', ',');

            Document PedidoPdf = new Document();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            Log.d("PDFCreator", "PDF Path: " + path);
            File file = new File(dir, NumPedido + ".pdf");
            FileOutputStream fOut = new FileOutputStream(file);
            docWriter = PdfWriter.getInstance(PedidoPdf, fOut);

            PedidoPdf.addAuthor("JD System");
            PedidoPdf.addCreationDate();
            PedidoPdf.addProducer();
            PedidoPdf.addCreator("jdsystem.com.br");
            PedidoPdf.addTitle("Força de Vendas");
            PedidoPdf.setPageSize(PageSize.A4);


            PedidoPdf.open();
            PdfContentByte cb = docWriter.getDirectContent();
            initializeFonts();

            /*byte[] byteArray = CursPedido.getBlob(CursPedido.getColumnIndex("LOGO"));

            String Caminho = Environment.getExternalStorageDirectory().getAbsolutePath() + "/forcavendas/pdf/";

            File arquivo = new File(Caminho + byteArray + ".jpg");
            Uri localFotoUri = Uri.fromFile(arquivo);

            Blob b = CursPedido.getBlob(2);
            byte barr[] = new byte[(int)b.length()];
            barr = b.getBytes(1,(int)b.length());

            FileOutputStream fout = new FileOutputStream("D:\\sonoo.jpg");
            fout.write(barr);

            Image companyLogo = Image.getInstance(Caminho + byteArray + ".jpg");
            companyLogo.setAbsolutePosition(25, 700);
            companyLogo.scalePercent(25);
            PedidoPdf.add(companyLogo);*/

            generateLayout(PedidoPdf, cb);

            String SitPed = CursPedido.getString(CursPedido.getColumnIndex("FLAGINTEGRADO"));
            if (SitPed.equals("1")) {
                Situacao = "Orçamento";
            }
            if (SitPed.equals("2")) {
                Situacao = "Pedido Gerado: " + CursPedido.getString(CursPedido.getColumnIndex("NUMPEDIDOERP"));
            }
            if (SitPed.equals("3")) {
                Situacao = "NFe Gerada: " + CursPedido.getString(CursPedido.getColumnIndex("NUMFISCAL"));
            }
            if (SitPed.equals("4")) {
                Situacao = "Cancelado";
            }
            if (SitPed.equals("5")) {
                Situacao = "Gerar Venda";
            }

            createHeadings(cb, 200, 800, "Empresa: " + CursPedido.getString(CursPedido.getColumnIndex("NOMEEMPRE")));
            createHeadings(cb, 200, 785, "Cliente: " + CursPedido.getString(CursPedido.getColumnIndex("NOMECLIE")));
            createHeadings(cb, 200, 770, "Data Emissão: " + sDataVenda);
            createHeadings(cb, 200, 755, "Vendedor: " + CursPedido.getString(CursPedido.getColumnIndex("USUARIO")));
            createHeadings(cb, 200, 740, "Situação: " + Situacao);

            Cursor CursorItensEnv = DB.rawQuery(" SELECT PEDITENS.CODITEMANUAL, PEDITENS.DESCRICAO, PEDITENS.QTDMENORPED, " +
                    " PEDITENS.UNIDADE, PEDITENS.VLUNIT, PEDITENS.VLTOTAL FROM PEDITENS " +
                    " WHERE PEDITENS.CHAVEPEDIDO = '" + CursPedido.getString(CursPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
            int item = 1;
            int y = 670;
            CursorItensEnv.moveToFirst();
            do {
                createContent(cb, 30, y, Util.AcrescentaZeros(String.valueOf(item), 3), PdfContentByte.ALIGN_CENTER);
                createContent(cb, 60, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODITEMANUAL")), PdfContentByte.ALIGN_CENTER);
                createContent(cb, 152, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")), PdfContentByte.ALIGN_LEFT);
                createContent(cb, 430, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")), PdfContentByte.ALIGN_RIGHT);
                createContent(cb, 460, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("UNIDADE")), PdfContentByte.ALIGN_LEFT);
                createContent(cb, 540, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ","), PdfContentByte.ALIGN_RIGHT);
                createContent(cb, 580, y, CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ","), PdfContentByte.ALIGN_RIGHT);
                y = y - 15;
                item++;
            } while (CursorItensEnv.moveToNext());

            CursPedido.close();
            CursorItensEnv.close();

            createTotal(cb, 430, 180, "Sub-Total:    R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 550, 180, SubTotal, PdfContentByte.ALIGN_RIGHT);

            createTotal(cb, 430, 150, "Vl. Desconto: R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 550, 150, VlDesc, PdfContentByte.ALIGN_RIGHT);

            createTotal(cb, 430, 120, "Valor Total:  R$ ", PdfContentByte.ALIGN_RIGHT);
            createTotal(cb, 550, 120, ValorTotal, PdfContentByte.ALIGN_RIGHT);

           // printPageNumber(cb);

            InserirRodape(cb, "Desenvolvido por JD System - www.jdsystem.com.br");

            PedidoPdf.close();
            docWriter.close();

        } catch (Exception E) {
            E.toString();
            return "0";
        }
        return NumPedido + ".pdf";
    }

    private static void generateLayout(Document doc, PdfContentByte cb) {
        try {

            cb.setLineWidth(1f);

            // Invoice Detail box layout
            //cb.rectangle(20, 50, 550, 600);
            cb.rectangle(20, 200, 570, 500);

            cb.moveTo(20, 680);
            cb.lineTo(590, 680); //Linha de Baixo ____ (Laterais / Altura)

            //Linhas |
            cb.moveTo(50, 200);
            cb.lineTo(50, 700);
            cb.moveTo(150, 200);
            cb.lineTo(150, 700);
            cb.moveTo(400, 200);
            cb.lineTo(400, 700);
            cb.moveTo(450, 200);
            cb.lineTo(450, 700);
            cb.moveTo(500, 200);
            cb.lineTo(500, 700);
            cb.moveTo(550, 200);
            cb.lineTo(550, 700);
            cb.stroke();

            // Invoice Detail box Text Headings
            createHeadings(cb, 22, 683, "Item");
            createHeadings(cb, 52, 683, "Código");
            createHeadings(cb, 152, 683, "Descrição");
            createHeadings(cb, 402, 683, "Qtd");
            createHeadings(cb, 452, 683, "Und.");
            createHeadings(cb, 502, 683, "Vl. Unit.");
            createHeadings(cb, 552, 683, "Vl. Total.");

            //add the images
            Image companyLogo = Image.getInstance(".gif");
            companyLogo.setAbsolutePosition(25, 700);
            companyLogo.scalePercent(25);
            doc.add(companyLogo);

        } catch (DocumentException dex) {
            dex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void createHeadings(PdfContentByte cb, float x, float y, String text) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.setTextMatrix(x, y);
        cb.showText(text.trim());
        cb.endText();
    }

    private static void createTotal(PdfContentByte cb, float x, float y, String text, int align) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 14);
        cb.showTextAligned(align, text.trim(), x, y, 0);
        cb.endText();
    }

    private static void printPageNumber(PdfContentByte cb) {

        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Pg. " + (pageNumber + 1), 570, 25, 0);
        cb.endText();

        pageNumber++;

    }

    private static void InserirRodape(PdfContentByte cb, String Texto) {
        BaseFont bfRodaPe = null;
        try {
            bfRodaPe = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cb.beginText();
        cb.setFontAndSize(bfRodaPe, 7);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, Texto, 150, 25, 0);
        cb.endText();
    }

    private static void createContent(PdfContentByte cb, float x, float y, String text, int align) {

        cb.beginText();
        cb.setFontAndSize(bf, 8);
        cb.showTextAligned(align, text.trim(), x, y, 0);
        cb.endText();

    }

    private static void initializeFonts() {
        try {
            bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void SincronizarPedidosEnvio(String NumPedido, Context ctxEnv, Boolean bdialog) {

        String JPedidos = null;
        ProgressDialog Dialog = null;
        String METHOD_NAMEENVIO = "CadastrarPedidos";
        DB = new ConfigDB(ctxEnv).getReadableDatabase();
        Cursor CursorPedido;

        try {
            CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPED = '" + NumPedido + "' ", null);
            if (bdialog == false) {
                Dialog = new ProgressDialog(ctxEnv);
                Dialog.setTitle("Aguarde...");
                Dialog.setMessage("Sincronizando Pedido Nº: " + NumPedido);
                Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                Dialog.setProgress(0);
                Dialog.setIcon(R.drawable.icon_sync);
                Dialog.setMax(0);
                Dialog.show();
            }

            SharedPreferences prefs = ctxEnv.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            senha = prefs.getString("senha", null);

            SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
            URLPrincipal = prefsHost.getString("host", null);
            String RetClieEnvio = null;

            int jumpTime = 0;
            final int totalProgressTime = CursorPedido.getCount();
            if (bdialog == false) {
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
            }
            CursorPedido.moveToFirst();
            if (CursorPedido.getCount() > 0) {
                CursorPedido.moveToFirst();
                do {
                    for (int i = 0; i < CursorPedido.getCount(); i++) {
                        do try {
                            jumpTime += 1;
                            if (bdialog == false) {
                                Dialog.setProgress(jumpTime);
                                Dialog.setMessage("Sincronizando Tabelas - Pedidos");
                            }

                            String ValorFrete = CursorPedido.getString(CursorPedido.getColumnIndex("VLFRETE"));
                            if (Util.isNullOrEmpty(ValorFrete)) {
                                ValorFrete = "0";
                            }
                            String ValorSeguro = CursorPedido.getString(CursorPedido.getColumnIndex("VALORSEGURO"));
                            if (Util.isNullOrEmpty(ValorSeguro)) {
                                ValorSeguro = "0";
                            }
                            String OBS = CursorPedido.getString(CursorPedido.getColumnIndex("OBS"));
                            String line_separator = System.getProperty("line.separator");
                            String Observacao = OBS.replaceAll("\n|" + line_separator, " ");

                            JPedidos = "{codclie_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODCLIE_EXT")) + "'," +
                                    "data_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "hora_emissao: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAEMIS")) + "'," +
                                    "valor_mercad: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLMERCAD")).replace(".", ",") + "'," +
                                    "valor_frete: '" + ValorFrete + "'," +
                                    "valor_seguro: '" + ValorSeguro + "'," +
                                    "dataentregaprevista: '" + CursorPedido.getString(CursorPedido.getColumnIndex("DATAPREVISTAENTREGA")) + "'," +
                                    "valor_desconto: '" + CursorPedido.getString(CursorPedido.getColumnIndex("VLPERCACRES")).replace(".", ",") + "'," +
                                    "obs_pedido: '" + OBS + "'," +
                                    "numpedido_ext: '" + CursorPedido.getString(CursorPedido.getColumnIndex("NUMPED")) + "'," +
                                    "chavepedido: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'," +
                                    "codempresa: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODEMPRESA")) + "'," +
                                    "cod_vendedor: '" + CursorPedido.getString(CursorPedido.getColumnIndex("CODVENDEDOR")) + "',";

                            String PedItems = "";
                            Cursor CursorItensEnv = DB.rawQuery(" SELECT * FROM PEDITENS WHERE CHAVEPEDIDO = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);

                            CursorItensEnv.moveToFirst();
                            do {
                                PedItems = PedItems + "{codigo_manual: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("CODITEMANUAL")) + "'," +
                                        "descricao: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("DESCRICAO")) + "'," +
                                        "numeroitem: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("NUMEROITEM")) + "'," +
                                        "qtdmenorped: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("QTDMENORPED")) + "'," +
                                        "vlunit: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLUNIT")).replace(".", ",") + "'," +
                                        "valortotal: '" + CursorItensEnv.getString(CursorItensEnv.getColumnIndex("VLTOTAL")).replace(".", ",") + "'}";

                                if (!CursorItensEnv.isLast()) {
                                    PedItems = PedItems + ",";
                                }

                            } while (CursorItensEnv.moveToNext());

                            if (PedItems != "") {
                                JPedidos = JPedidos + "produtos: " + "[" + PedItems + "]";
                            }
                            String PedParcelas = "";
                            Cursor CursorParcelasEnv = DB.rawQuery(" SELECT * FROM CONREC WHERE vendac_chave = '" +
                                    CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                            CursorParcelasEnv.moveToFirst();
                            do {
                                PedParcelas = PedParcelas + "{chavepedido: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("vendac_chave")) + "'," +
                                        "numparcela: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_numparcela")) + "'," +
                                        "valor_receber: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_valor_receber")) + "'," +
                                        "datavencimento: '" + CursorParcelasEnv.getString(CursorParcelasEnv.getColumnIndex("rec_datavencimento")) + "'}";

                                if (!CursorParcelasEnv.isLast()) {
                                    PedParcelas = PedParcelas + ",";
                                }
                            } while (CursorParcelasEnv.moveToNext());

                            if (PedParcelas != "") {
                                JPedidos = JPedidos + ",formapgto: " + "[" + PedParcelas + "]";
                            }

                            JPedidos = JPedidos + '}';

                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                            soap.addProperty("aJson", JPedidos);
                            soap.addProperty("aUsuario", usuario);
                            soap.addProperty("aSenha", senha);
                            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelope.setOutputSoapObject(soap);
                            HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                            try {
                                //Boolean ConexOk = true;
                                Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
                                if (ConexOk == true) {
                                    Envio.call("", envelope);
                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                    RetClieEnvio = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                System.out.println("Error" + e);
                            }
                        } catch (Exception E) {
                            // TODO Auto-generated catch block
                            E.printStackTrace();
                        }
                        while (jumpTime < totalProgressTime);
                    }
                    try {
                        DB = new ConfigDB(ctxEnv).getReadableDatabase();
                        if (!RetClieEnvio.equals("0")) {
                            Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'", null);
                            CursPedAtu.moveToFirst();
                            if (CursPedAtu.getCount() > 0) {
                                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '2', NUMPEDIDOERP = " + RetClieEnvio + " WHERE CHAVE_PEDIDO = '" + CursorPedido.getString(CursorPedido.getColumnIndex("CHAVE_PEDIDO")) + "'");
                            }
                            CursPedAtu.close();
                        }
                    } catch (Exception E) {
                        Toast.makeText(ctxEnv, E.toString(), Toast.LENGTH_SHORT).show();
                    }
                    JPedidos = "";
                }
                while (CursorPedido.moveToNext());
                CursorPedido.close();
            }
            if (bdialog == false) {
                if (Dialog.isShowing())
                    Dialog.dismiss();
            }
        } catch (Exception E) {
            System.out.println("Error" + E);
        }

    }

    public static void AtualizaStatusPedido(String NumPedido, Context ctxEnv) {

        String JPedidos = null;
        ProgressDialog Dialog = null;
        String METHOD_NAMEENVIO = "RetornaStatusPedidos";
        DB = new ConfigDB(ctxEnv).getReadableDatabase();
        Cursor CursorPedido;
        String NumFiscal = "";

        try {
            CursorPedido = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPEDIDOERP = " + NumPedido, null);

            Dialog = new ProgressDialog(ctxEnv);
            Dialog.setTitle("Aguarde...");
            Dialog.setMessage("Atualizando Pedido Nº: " + NumPedido);
            Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            Dialog.setProgress(0);
            Dialog.setIcon(R.drawable.icon_sync);
            Dialog.setMax(0);
            Dialog.show();

            SharedPreferences prefs = ctxEnv.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            senha = prefs.getString("senha", null);

            SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
            URLPrincipal = prefsHost.getString("host", null);
            String RetStatusPedido = null;

            int jumpTime = 0;
            final int totalProgressTime = CursorPedido.getCount();
            Dialog.setMax(totalProgressTime);
            Dialog.setProgress(jumpTime);
            CursorPedido.moveToFirst();
            if (CursorPedido.getCount() > 0) {
                CursorPedido.moveToFirst();
                do {
                    for (int i = 0; i < CursorPedido.getCount(); i++) {
                        do try {
                            jumpTime += 1;
                            Dialog.setProgress(jumpTime);
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                            soap.addProperty("aNumPedido", NumPedido);
                            soap.addProperty("aUsuario", usuario);
                            soap.addProperty("aSenha", senha);
                            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelope.setOutputSoapObject(soap);
                            HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPEDIDOS);

                            try {
                                //Boolean ConexOk = true;
                                Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
                                if (ConexOk == true) {
                                    Envio.call("", envelope);
                                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                    RetStatusPedido = (String) envelope.getResponse();
                                    System.out.println("Response :" + resultsRequestSOAP.toString());
                                }
                            } catch (Exception e) {
                                System.out.println("Error" + e);
                            }
                        } catch (Exception E) {
                            // TODO Auto-generated catch block
                            E.printStackTrace();
                        }
                        while (jumpTime < totalProgressTime);
                    }
                    try {
                        DB = new ConfigDB(ctxEnv).getReadableDatabase();
                        Cursor CursPedAtu = DB.rawQuery(" SELECT * FROM PEDOPER WHERE NUMPEDIDOERP = " + NumPedido, null);
                        CursPedAtu.moveToFirst();
                        if (CursPedAtu.getCount() > 0) {
                            if (!RetStatusPedido.equals("Orçamento")) {
                                DB.execSQL(" UPDATE PEDOPER SET FLAGINTEGRADO = '3', NUMFISCAL = " + RetStatusPedido + " WHERE NUMPEDIDOERP = '" + NumPedido + "'");
                            }
                        }
                        CursPedAtu.close();
                    } catch (Exception E) {
                        Toast.makeText(ctxEnv, E.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                while (CursorPedido.moveToNext());
                CursorPedido.close();
            }
            if (Dialog.isShowing())
                Dialog.dismiss();
        } catch (Exception E) {
            System.out.println("Error" + E);
        }

    }

    public static void SincParametros(String sUsuario, String sSenha, Context ctxEnv) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "RetornaParametros");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
        String RetParamApp = null;

        try {
            //Boolean ConexOk = true;
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetParamApp = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetParamApp);
            JSONArray JParamApp = jsonObj.getJSONArray("param_app");

            int jumpTime = 0;
            final int totalProgressTime = JParamApp.length();
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JParamApp.length(); i++) {
                do {
                    try {
                        JSONObject c = JParamApp.getJSONObject(jumpTime);
                        jumpTime += 1;
                        Double PercDescMax = c.getDouble("percdescmaxped");

                        DB.execSQL(" UPDATE PARAMAPP SET PERCACRESC = " + PercDescMax);

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                        E.toString();
                    }
                } while (jumpTime < totalProgressTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void SincEmpresas(String sUsuario, String sSenha, Context ctxEnv) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefsHost = ctxEnv.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "RetornaEmpresas");
        soap.addProperty("aUsuario", sUsuario);
        soap.addProperty("aSenha", sSenha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLUSUARIOS);
        String RetBairros = null;

        try {
            //Boolean ConexOk = true;
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnv);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetBairros = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetBairros);
            JSONArray JBairros = jsonObj.getJSONArray("empresas");

            int jumpTime = 0;
            //DialogECB.setProgress(jumpTime);
            final int totalProgressTime = JBairros.length();
            //DialogECB.setMax(totalProgressTime);
            DB = new ConfigDB(ctxEnv).getReadableDatabase();

            for (int i = 0; i < JBairros.length(); i++) {
                do {
                    try {
                        JSONObject c = JBairros.getJSONObject(jumpTime);
                        jumpTime += 1;
                        //DialogECB.setProgress(jumpTime);
                        // DialogECB.setMessage("Sincronizando Tabelas - Empresas");
                        int CodEmpresa = c.getInt("codigo");
                        String NomeEmpresa = c.getString("nome_empresa");
                        String NomeAbreviado = c.getString("nome_abreviado");
                        String Cnpj = c.getString("cnpj");
                        String Tel1 = c.getString("tel1");
                        String Tel2 = c.getString("tel2");
                        String Email = c.getString("email");
                        String Ativo = c.getString("ativo");

                        byte[] imgRecebida = null;

                        try {
                            String LogoEmpresa = c.getString("logo");
                            imgRecebida = Base64.decode(LogoEmpresa, Base64.DEFAULT);
                            Bitmap imgLogo = BitmapFactory.decodeByteArray(imgRecebida, 0, imgRecebida.length);
                        }catch (Exception e){
                            e.toString();
                        }

                        Cursor CursorEmpresa = DB.rawQuery(" SELECT CODEMPRESA, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
                        if (CursorEmpresa.getCount() > 0) {
                            DB.execSQL(" UPDATE EMPRESAS SET CODEMPRESA = '" + CodEmpresa + "', NOMEEMPRE = '" + NomeEmpresa + "', NOMEABREV = '" + NomeAbreviado + "'," +
                                    " CNPJ = '" + Cnpj + "', TEL1 = '" + Tel1 + "', TEL2 = '" + Tel2 + "', EMAIL = '" + Email + "', ATIVO = '" + Ativo + "'," +
                                    " LOGO = '" + imgRecebida + "'" +
                                    " WHERE CODEMPRESA = " + CodEmpresa);
                            Cursor cursor1 = DB.rawQuery(" SELECT CODEMPRESA, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
                            cursor1.moveToFirst();
                            //CodBairroExt = cursor1.getInt(cursor1.getColumnIndex("CODBAIRRO_EXT"));
                            cursor1.close();
                        } else {
                            DB.execSQL(" INSERT INTO EMPRESAS (CODEMPRESA, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL, ATIVO, LOGO)" +
                                    " VALUES(" + CodEmpresa + ",'" + NomeEmpresa + "', '" + NomeAbreviado + "','" + Cnpj + "','" + Tel1 + "','" + Tel2 +
                                    "','" + Email + "','" + Ativo + "', " + imgRecebida + " );");
                            Cursor cursor1 = DB.rawQuery(" SELECT CODEMPRESA, NOMEEMPRE, NOMEABREV, CNPJ, TEL1, TEL2, EMAIL FROM EMPRESAS WHERE CODEMPRESA = " + CodEmpresa, null);
                            cursor1.moveToFirst();
                            //CodBairroExt = cursor1.getInt(cursor1.getColumnIndex("CODBAIRRO_EXT"));
                            cursor1.close();
                        }
                        CursorEmpresa.close();

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                } while (jumpTime < totalProgressTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void SincronizarClientesEnvioStatic(String CodClie_Int, Context ctxEnvClie, Boolean bDialogo) {
        String Jcliente = null;
        ProgressDialog Dialog = null;
        String METHOD_NAMEENVIO = "Cadastrar";
        DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

        try {
            Cursor CursorClieEnv = null;
            if (CodClie_Int.equals("0")) {
                CursorClieEnv = DB.rawQuery(" SELECT CLIENTES.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN" +
                        " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO WHERE FLAGINTEGRADO = '1' " +
                        " ORDER BY NOMEFAN, NOMERAZAO ", null);
            } else {
                CursorClieEnv = DB.rawQuery(" SELECT CLIENTES.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                        " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN" +
                        " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO WHERE  CODCLIE_INT = " + CodClie_Int, null);
            }

            if (bDialogo == false) {
                Dialog = new ProgressDialog(ctxEnvClie);
                Dialog.setTitle("Aguarde...");
                Dialog.setMessage("Sincronizando Clientes");
                Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                Dialog.setProgress(0);
                Dialog.setIcon(R.drawable.icon_sync);
                Dialog.setMax(0);
                Dialog.show();
            }

            SharedPreferences prefs = ctxEnvClie.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            senha = prefs.getString("senha", null);

            SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
            URLPrincipal = prefsHost.getString("host", null);
            String RetClieEnvio = null;

            int jumpTime = 0;
            String CodClie_ext = null;
            final int totalProgressTime = CursorClieEnv.getCount();
            if (bDialogo == false) {
                Dialog.setMax(totalProgressTime);
                Dialog.setProgress(jumpTime);
            }
            CursorClieEnv.moveToFirst();
            if (CursorClieEnv.getCount() > 0) {
                CursorClieEnv.moveToFirst();
                do {
                    for (int i = 0; i < CursorClieEnv.getCount(); i++) {
                        do {
                            try {
                                jumpTime += 1;
                                if (bDialogo == false) {
                                    Dialog.setProgress(jumpTime);
                                    Dialog.setMessage("Enviado clientes");
                                }
                                Jcliente = "{razao_social: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMERAZAO")) + "'," +
                                        "nome_fantasia: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NOMEFAN")) + "'," +
                                        "tipo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TIPOPESSOA")) + "'," +
                                        "cnpj_cpf: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'," +
                                        "inscricao_estadual: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("INSCREST")) + "'," +
                                        "Logradouro: '" + (CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ENDERECO"))).trim() + "'," +
                                        "numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("NUMERO")) + "'," +
                                        "codvendedor: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODVENDEDOR")) + "'," +
                                        "complemento: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("COMPLEMENT")) + "'," +
                                        "bairro: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("BAIRRO")) + "'," +
                                        "cidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CIDADE")) + "'," +
                                        "estado: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("UF")) + "'," +
                                        "cep: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CEP")) + "'," +
                                        "observacao: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("OBS")) + "'," +
                                        "identidade: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("REGIDENT")) + "'," +
                                        "emails: [{email: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("EMAIL")) + "'}," +
                                        "{email: ''}]," +
                                        "ativo: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("ATIVO")) + "'," +
                                        "telefones: [{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL1")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TEL2")) + "'}," +
                                        "{numero: '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("TELFAX")) + "'}]";

                                String Contatos = "";
                                Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " +
                                        CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CODCLIE_INT")), null);

                                CursorContatosEnv.moveToFirst();
                                while (CursorContatosEnv.moveToNext()) {
                                    Contatos = Contatos + "{nome: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("NOME")) + "'," +
                                            "cargo: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("CARGO")) + "'," +
                                            "emails: [{email: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("EMAIL")) + "'}]," +
                                            "telefones: [{numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL1")) + "'," +
                                            "numero: '" + CursorContatosEnv.getString(CursorContatosEnv.getColumnIndex("TEL2")) + "'}]},";
                                }

                                if (Contatos != "") {
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";
                                } else {
                                    Contatos = "{nome: ''," +
                                            "cargo: ''," +
                                            "emails: [{email: ''}]," +
                                            "telefones: [{numero: ''," +
                                            "numero: ''}]}";
                                    Jcliente = Jcliente + ",contatos: " + "[" + Contatos + "]";
                                }
                                String Dependentes = "{nome: ''," +
                                        "datanascimento: ''," +
                                        "redident: ''," +
                                        "codclie: ''}";
                                Jcliente = Jcliente + ",dependentes: " + "[" + Dependentes + "]}";

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAMEENVIO);
                                soap.addProperty("aJson", Jcliente);
                                soap.addProperty("aUsuario", usuario);
                                soap.addProperty("aSenha", senha);
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.setOutputSoapObject(soap);
                                HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);

                                try {
                                    //Boolean ConexOk = true;
                                    Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
                                    if (ConexOk == true) {
                                        Envio.call("", envelope);
                                        SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                                        RetClieEnvio = (String) envelope.getResponse();
                                        System.out.println("Response :" + resultsRequestSOAP.toString());
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error" + e);
                                }
                            } catch (Exception E) {
                                if (bDialogo == false) {
                                    Dialog.dismiss();
                                }
                                Toast.makeText(ctxEnvClie, "Cliente não enviado! Verifique.", Toast.LENGTH_SHORT).show();
                                return;
                                //E.printStackTrace();
                            }
                        }
                        while (jumpTime < totalProgressTime);
                    }
                    try {
                        if (!RetClieEnvio.equals("0")) {
                            Cursor CursClieAtu = DB.rawQuery(" SELECT * FROM CLIENTES WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'", null);
                            CursClieAtu.moveToFirst();
                            if (CursClieAtu.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET FLAGINTEGRADO = '2', CODCLIE_EXT = " + RetClieEnvio + " WHERE CNPJ_CPF = '" + CursorClieEnv.getString(CursorClieEnv.getColumnIndex("CNPJ_CPF")) + "'");
                            }
                            CursClieAtu.close();
                        }
                    } catch (Exception E) {
                    }
                }
                while (CursorClieEnv.moveToNext());
                CursorClieEnv.close();
            }
            if (bDialogo == false) {
                if (Dialog.isShowing())
                    Dialog.dismiss();
            }
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
    }

    public static void SincronizarProdutosStatic(String DtUlt, Context ctxSincProd, Boolean bDialogo) {

        ProgressDialog Dialog = null;
        if (bDialogo == false) {
            Dialog = new ProgressDialog(ctxSincProd);
            Dialog.setTitle("Aguarde...");
            Dialog.setMessage("Sincronizando Produtos");
            Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            Dialog.setProgress(0);
            Dialog.setIcon(R.drawable.icon_sync);
            Dialog.setMax(0);
            Dialog.show();
        }

        String METHOD_NAME = "Carregar";
        String TAG_PRODUTOSINFO = "produtos";

        String TAG_CODIGOITEM = "codigoitem";
        String TAG_CODMANUAL = "coditemanual";
        String TAG_DESCRICAO = "descricao";
        String TAG_UNIVENDA = "univenda";
        String TAG_VLVENDA1 = "vlvenda1";
        String TAG_VLVENDA2 = "vlvenda2";
        String TAG_VLVENDA3 = "vlvenda3";
        String TAG_VLVENDA4 = "vlvenda4";
        String TAG_VLVENDA5 = "vlvenda5";
        String TAG_VLVENDAP1 = "vlvendap1";
        String TAG_VLVENDAP2 = "vlvendap2";
        String TAG_VENDAPADRAO = "vendapadrao";
        String TAG_MARCA = "marca";
        String TAG_CLASSE = "classe";
        String TAG_FABRICANTE = "fabricante";
        String TAG_FORNECEDOR = "fornecedor";
        String TAG_APRESENTACAO = "apresentacao";
        String TAG_ATIVO = "ativo";

        SharedPreferences prefs = ctxSincProd.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        DtUlt = DataUltSt2;

        SharedPreferences prefsHost = ctxSincProd.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aParam", "D" + DtUlt);
        soap.addProperty("aUsuario", usuario);
        soap.addProperty("aSenha", senha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLPRODUTOS);
        String RetProdutos = null;

        try {
            //Boolean ConexOk = true;
            Boolean ConexOk = Util.checarConexaoCelular(ctxSincProd);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetProdutos = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            JSONObject jsonObj = new JSONObject(RetProdutos);
            JSONArray ProdItens = jsonObj.getJSONArray(TAG_PRODUTOSINFO);

            int jumpTime = 0;

            final int totalProgressTime = ProdItens.length();
            DB = new ConfigDB(ctxSincProd).getReadableDatabase();
            if (bDialogo == false) {
                Dialog.setProgress(jumpTime);
                Dialog.setMax(totalProgressTime);
            }

            for (int i = 0; i < ProdItens.length(); i++) {
                do {
                    try {
                        JSONObject CItens = ProdItens.getJSONObject(jumpTime);
                        jumpTime += 1;
                        if (bDialogo == false) {
                            Dialog.setProgress(jumpTime);
                        }

                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM), null);
                        try {
                            CursItens.moveToFirst();
                            if (CursItens.getCount() > 0) {
                                DB.execSQL(" UPDATE ITENS SET CODITEMANUAL = '" + CItens.getString(TAG_CODMANUAL) +
                                        "', DESCRICAO = '" + CItens.getString(TAG_DESCRICAO) +
                                        "', FABRICANTE = '" + CItens.getString(TAG_FABRICANTE) +
                                        "', FORNECEDOR = '" + CItens.getString(TAG_FORNECEDOR) +
                                        "', CLASSE = '" + CItens.getString(TAG_CLASSE) +
                                        "', MARCA = '" + CItens.getString(TAG_MARCA) +
                                        "', UNIVENDA = '" + CItens.getString(TAG_UNIVENDA) +
                                        "', VLVENDA1 = '" + CItens.getString(TAG_VLVENDA1) +
                                        "', VLVENDA2 = '" + CItens.getString(TAG_VLVENDA2) +
                                        "', VLVENDA3 = '" + CItens.getString(TAG_VLVENDA3) +
                                        "', VLVENDA4 = '" + CItens.getString(TAG_VLVENDA4) +
                                        "', VLVENDA5 = '" + CItens.getString(TAG_VLVENDA5) +
                                        "', VLVENDAP1 = '" + CItens.getString(TAG_VLVENDAP1) +
                                        "', VLVENDAP2 = '" + CItens.getString(TAG_VLVENDAP2) +
                                        "', VENDAPADRAO = '" + CItens.getString(TAG_VENDAPADRAO) +
                                        "', ATIVO = '" + CItens.getString(TAG_ATIVO) +
                                        "', APRESENTACAO = '" + CItens.getString(TAG_APRESENTACAO) +
                                        "' WHERE CODIGOITEM = " + CItens.getString(TAG_CODIGOITEM));
                            } else {
                                DB.execSQL("INSERT INTO ITENS (CODIGOITEM, CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA, " +
                                        "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2, VENDAPADRAO, " +
                                        "ATIVO, APRESENTACAO) VALUES(" + "'" + CItens.getString(TAG_CODIGOITEM) +
                                        "',' " + CItens.getString(TAG_CODMANUAL) +
                                        "','" + CItens.getString(TAG_DESCRICAO) +
                                        "',' " + CItens.getString(TAG_FABRICANTE) +
                                        "',' " + CItens.getString(TAG_FORNECEDOR) +
                                        "',' " + CItens.getString(TAG_CLASSE) +
                                        "',' " + CItens.getString(TAG_MARCA) +
                                        "', '" + CItens.getString(TAG_UNIVENDA) +
                                        "',' " + CItens.getString(TAG_VLVENDA1) +
                                        "', '" + CItens.getString(TAG_VLVENDA2) +
                                        "',' " + CItens.getString(TAG_VLVENDA3) +
                                        "',' " + CItens.getString(TAG_VLVENDA4) +
                                        "','" + CItens.getString(TAG_VLVENDA5) +
                                        "',' " + CItens.getString(TAG_VLVENDAP1) +
                                        "',' " + CItens.getString(TAG_VLVENDAP2) +
                                        "', '" + CItens.getString(TAG_VENDAPADRAO) +
                                        "', '" + CItens.getString(TAG_ATIVO) +
                                        "',' " + CItens.getString(TAG_APRESENTACAO) + "');");
                            }
                            CursItens.close();
                        } catch (Exception E) {
                            System.out.println("Error" + E);
                        }

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }
                while (jumpTime < totalProgressTime);
            }
            if (bDialogo == false) {
                if (Dialog.isShowing())
                    Dialog.dismiss();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void SincronizarClientesStatic(String sCodVend, Context ctxEnvClie, Boolean bDialogo) {

        ProgressDialog Dialog = null;

        if (bDialogo == false) {
            Dialog = new ProgressDialog(ctxEnvClie);
            Dialog.setTitle("Aguarde...");
            Dialog.setMessage("Sincronizando Clientes");
            Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            Dialog.setProgress(0);
            Dialog.setIcon(R.drawable.icon_sync);
            Dialog.setMax(0);
            Dialog.show();
        }

        String METHOD_NAME = "Carregar";
        String TAG_CLIENTESINFO = "clientes";
        String TAG_TELEFONESINFO = "telefones";
        String TAG_CONTATOSINFO = "contatos";

        String TAG_CODIGO = "codigo";
        String TAG_RAZAOSOCIAL = "razao_social";
        String TAG_NOMEFANTASIA = "nome_fantasia";
        String TAG_TIPO = "tipo";
        String TAG_CNPJCPF = "cnpj_cpf";
        String TAG_INSCESTADUAL = "inscricao_estadual";
        String TAG_LOGRADOURO = "Logradouro";
        String TAG_NUMERO = "numero";
        String TAG_COMPLEMENTO = "complemento";
        String TAG_BAIRRO = "bairro";
        String TAG_CIDADE = "cidade";
        String TAG_ESTADO = "estado";
        String TAG_CEP = "cep";
        String TAG_RG = "identidade";
        String TAG_OBS = "observacao";
        String TAG_EMAILS = "emails";
        String TAG_ATIVO = "ativo";

        String CodVendedor = sCodVend;


        SharedPreferences prefs = ctxEnvClie.getSharedPreferences(actLogin.NOME_USUARIO, MODE_PRIVATE);
        usuario = prefs.getString("usuario", null);
        senha = prefs.getString("senha", null);

        String DtUlt = DataUltSt2;

        SharedPreferences prefsHost = ctxEnvClie.getSharedPreferences(ConfigWeb.CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefsHost.getString("host", null);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, METHOD_NAME);
        soap.addProperty("aParam", "V" + CodVendedor + "%" + DtUlt);
        // + "%" + DtUlt
        soap.addProperty("aUsuario", usuario);
        soap.addProperty("aSenha", senha);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(URLPrincipal + ConfigConex.URLCLIENTES);
        String RetClientes = null;

        try {
            Boolean ConexOk = Util.checarConexaoCelular(ctxEnvClie);
            if (ConexOk == true) {
                Envio.call("", envelope);
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;

                RetClientes = (String) envelope.getResponse();
                System.out.println("Response :" + resultsRequestSOAP.toString());
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }

        try {
            //String SHA1Ret = RetClientes.substring(0, 40);
            //String ArrayClientes = RetClientes.substring(40, (RetClientes.length()));

            JSONObject jsonObj = new JSONObject(RetClientes);
            JSONArray pedidosblq = jsonObj.getJSONArray(TAG_CLIENTESINFO);

            int jumpTime = 0;
            final int totalProgressTime = pedidosblq.length();
            if (bDialogo == false) {
                Dialog.setProgress(jumpTime);
                Dialog.setMax(totalProgressTime);
            }

            String CodCliente = null;

            DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

            for (int i = 0; i < pedidosblq.length(); i++) {
                while (jumpTime < totalProgressTime) {
                    try {
                        JSONObject c = pedidosblq.getJSONObject(jumpTime);

                        String Telefone = c.getString(TAG_TELEFONESINFO);
                        Telefone = "{\"telefones\":" + Telefone + "\t}";
                        JSONObject ObjTel = new JSONObject(Telefone);
                        JSONArray Telef = ObjTel.getJSONArray("telefones");
                        String Tel1 = null;
                        String Tel2 = null;
                        String Tel3 = null;

                        for (int t = 0; t < Telef.length(); t++) {
                            JSONObject tt = Telef.getJSONObject(t);
                            if (t == 0) {
                                Tel1 = tt.getString("numero");
                            }
                            if (t == 1) {
                                Tel2 = tt.getString("numero");
                            }
                            if (t == 2) {
                                Tel3 = tt.getString("numero");
                            }
                        }

                        jumpTime += 1;
                        if (bDialogo == false) {
                            Dialog.setProgress(jumpTime);
                            Dialog.setMessage("Sincronizando Tabelas - Clientes");
                        }

                        Cursor cursor = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'", null);

                        String CodEstado = RetornaEstado(c.getString(TAG_ESTADO));
                        int CodCidade = RetornaCidade(c.getString(TAG_CIDADE), CodEstado);
                        int CodBairro = RetornaBairro(c.getString(TAG_BAIRRO), CodCidade);

                        try {
                            if (cursor.getCount() > 0) {
                                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + c.getString(TAG_RAZAOSOCIAL) +
                                        "', NOMEFAN = '" + c.getString(TAG_NOMEFANTASIA) +
                                        "', REGIDENT = '" + c.getString(TAG_RG) +
                                        "', INSCREST = '" + c.getString(TAG_INSCESTADUAL) + "', EMAIL = '" + c.getString(TAG_EMAILS) +
                                        "', TEL1 = '" + Tel1 + "', TEL2 = '" + Tel2 + "', ENDERECO = '" + c.getString(TAG_LOGRADOURO) +
                                        "', NUMERO = '" + c.getString(TAG_NUMERO) + "', COMPLEMENT = '" + c.getString(TAG_COMPLEMENTO) +
                                        "', CODBAIRRO = '" + CodBairro + "', OBS = '" + c.getString(TAG_OBS) + "', CODCIDADE = '" + CodCidade + "', UF = '" + CodEstado +
                                        "', CEP = '" + c.getString(TAG_CEP) + "', CODCLIE_EXT = '" + c.getString(TAG_CODIGO) + "', " +
                                        " TIPOPESSOA = '" + c.getString(TAG_TIPO) + "', ATIVO = '" + c.getString(TAG_ATIVO) + "'" +
                                        ", CODVENDEDOR = '" + CodVendedor + "', FLAGINTEGRADO = '2' " +
                                        " WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'");
                            } else {
                                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, NOMEFAN, REGIDENT, INSCREST, EMAIL, TEL1, TEL2, " +
                                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                                        "CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO, FLAGINTEGRADO) VALUES(" +
                                        "'" + c.getString(TAG_CNPJCPF) + "','" + c.getString(TAG_RAZAOSOCIAL) + "','" + c.getString(TAG_RG) +
                                        "',' " + c.getString(TAG_NOMEFANTASIA) + "',' " + c.getString(TAG_INSCESTADUAL) + "',' " + c.getString(TAG_EMAILS) +
                                        "',' " + Tel1 + "', '" + Tel2 + "', '" + c.getString(TAG_LOGRADOURO) +
                                        "',' " + c.getString(TAG_NUMERO) + "', '" + c.getString(TAG_COMPLEMENTO) +
                                        "','" + CodBairro + "',' " + c.getString(TAG_OBS) + "','" + CodCidade + "',' " + CodEstado +
                                        "',' " + c.getString(TAG_CEP) + "', '" + c.getString(TAG_CODIGO) +
                                        "','" + CodVendedor + "','" + c.getString(TAG_TIPO) + "','" + c.getString(TAG_ATIVO)
                                        + "','" + "2" + "');"); // FLAGINTEGRADO = 2, Significa que o cliente já está integrado e existe na base da retaguarda.
                            }

                            Cursor cursor1 = DB.rawQuery(" SELECT CODCLIE_INT, CNPJ_CPF, NOMERAZAO FROM CLIENTES WHERE CNPJ_CPF = '" + c.getString(TAG_CNPJCPF) + "'", null);
                            cursor1.moveToFirst();
                            CodCliente = cursor1.getString(cursor1.getColumnIndex("CODCLIE_INT"));

                            cursor.close();
                            cursor1.close();

                        } catch (Exception E) {
                            System.out.println("Error" + E);
                        }

                        String Contatos = c.getString(TAG_CONTATOSINFO);
                        Contatos = "{\"contatos\":" + Contatos + "\t}";
                        JSONObject ObjCont = new JSONObject(Contatos);
                        JSONArray Cont = ObjCont.getJSONArray("contatos");
                        String NomeContato = null;
                        String CargoContato = null;
                        String EmailContato = null;
                        String Tel1Contato = null;
                        String Tel2Contato = null;

                        try {
                            for (int co = 0; co < Cont.length(); co++) {
                                JSONObject cc = Cont.getJSONObject(co);
                                if (co == 0) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont1 = c.getString("telefones");
                                    TelCont1 = "{\"telefones\":" + TelCont1 + "\t}";
                                    JSONObject ObjTelC1 = new JSONObject(TelCont1);
                                    JSONArray TelefC1 = ObjTelC1.getJSONArray("telefones");

                                    for (int tc1 = 0; tc1 < TelefC1.length(); tc1++) {
                                        JSONObject tt1 = TelefC1.getJSONObject(tc1);
                                        if (tc1 == 0) {
                                            Tel1Contato = tt1.getString("numero");
                                        }
                                        if (tc1 == 1) {
                                            Tel2Contato = tt1.getString("numero");
                                        }
                                    }
                                }
                                if (co == 1) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont2 = c.getString("telefones");
                                    TelCont2 = "{\"telefones\":" + TelCont2 + "\t}";
                                    JSONObject ObjTelC2 = new JSONObject(TelCont2);
                                    JSONArray TelefC2 = ObjTelC2.getJSONArray("telefones");

                                    for (int tc2 = 0; tc2 < TelefC2.length(); tc2++) {
                                        JSONObject tt2 = Telef.getJSONObject(tc2);
                                        if (tc2 == 0) {
                                            Tel1Contato = tt2.getString("numero");
                                        }
                                        if (tc2 == 1) {
                                            Tel2Contato = tt2.getString("numero");
                                        }
                                    }
                                }
                                if (co == 2) {
                                    NomeContato = cc.getString("nome");
                                    CargoContato = cc.getString("cargo");
                                    EmailContato = cc.getString("email");

                                    String TelCont3 = c.getString("telefones");
                                    TelCont3 = "{\"telefones\":" + TelCont3 + "\t}";
                                    JSONObject ObjTelC3 = new JSONObject(TelCont3);
                                    JSONArray TelefC3 = ObjTelC3.getJSONArray("telefones");

                                    for (int tc3 = 0; tc3 < TelefC3.length(); tc3++) {
                                        JSONObject tt3 = Telef.getJSONObject(tc3);
                                        if (tc3 == 0) {
                                            Tel1Contato = tt3.getString("numero");
                                        }
                                        if (tc3 == 1) {
                                            Tel2Contato = tt3.getString("numero");
                                        }
                                    }
                                }

                                Cursor CursorContatosEnv = DB.rawQuery(" SELECT * FROM CONTATO WHERE CODCLIENTE = " + CodCliente + " AND NOME = '" + NomeContato + "'", null);

                                try {
                                    if (!(CursorContatosEnv.getCount() > 0)) {
                                        DB.execSQL("INSERT INTO CONTATO (NOME, CARGO, EMAIL, TEL1, TEL2, CODCLIENTE ) VALUES(" +
                                                "'" + NomeContato + "','" + CargoContato +
                                                "',' " + EmailContato + "',' " + Tel1Contato + "',' " + Tel2Contato + "'" +
                                                "," + CodCliente + ");");

                                    }
                                    CursorContatosEnv.close();
                                } catch (Exception E) {
                                    System.out.println("Error" + E);
                                }

                            }
                        } catch (Exception e) {
                            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception E) {
                        // TODO Auto-generated catch block
                        E.printStackTrace();
                    }
                }
            }
            if (bDialogo == false) {
                if (Dialog.isShowing())
                    Dialog.dismiss();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void run(Context ctxEnvClie) {
        DB = new ConfigDB(ctxEnvClie).getReadableDatabase();

        Cursor CursosParam = DB.rawQuery(" SELECT DT_ULT_ATU FROM PARAMAPP ", null);
        CursosParam.moveToFirst();

        String DataUlt = null;
        String HoraAtu = null;

        try {
            if (CursosParam.getCount() > 0) {
                String dataEmUmFormato = CursosParam.getString(CursosParam.getColumnIndex("DT_ULT_ATU"));
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                Date data = formato.parse(dataEmUmFormato);
                formato.applyPattern("dd/MM/yyyy");
                DataUlt = formato.format(data);

                SimpleDateFormat SdfHora = new SimpleDateFormat("HH:mm");
                HoraAtu = (SdfHora.format(data));
            } else {
                DataUlt = "01/01/2000 12:20:30";
                DB.execSQL(" INSERT INTO PARAMAPP(DT_ULT_ATU) VALUES(datetime());");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DataUltSt2 = DataUlt + " " + HoraAtu;

        DB.execSQL(" UPDATE PARAMAPP SET DT_ULT_ATU = DATETIME();");
    }
}

//comentario2

