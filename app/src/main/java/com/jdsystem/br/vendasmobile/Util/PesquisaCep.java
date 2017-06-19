package com.jdsystem.br.vendasmobile.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.CadastroContatos;
import com.jdsystem.br.vendasmobile.ConfigConex;
import com.jdsystem.br.vendasmobile.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by WKS22 on 09/06/2017.
 */

public class PesquisaCep extends Activity{

    Handler handler;

    public String buscarDadosConsultaCep(String cep, final Context context){
        Resources res = context.getResources();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SoapObject soap = new SoapObject(ConfigConex.NAMESPACE, "PesquisaCEP");
        soap.addProperty("aCEP", cep);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soap);
        HttpTransportSE Envio = new HttpTransportSE(ConfigConex.URLDADOSCEP);
        String RetDadosEndereco = "";

        try{
            Boolean ConexOk = Util.checarConexaoCelular(context);
            if (ConexOk) {
                try {
                    Envio.call("", envelope);
                } catch (Exception e) {
                    return res.getString(R.string.failure_communicate);
                }
                try {
                    SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                    RetDadosEndereco = (String) envelope.getResponse();
                    System.out.println("Response :" + resultsRequestSOAP.toString());
                } catch (Exception e) {
                    return res.getString(R.string.failed_return);
                }
            } else
                return res.getString(R.string.no_connection);
        }catch (Exception e){
            e.toString();
        }
        return RetDadosEndereco;
    }

    public static void cadastraCidade(){

    }

}
