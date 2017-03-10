package com.jdsystem.br.vendasmobile.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util extends Activity {


    public static final int SUCESSO = 0;
    public static final int ALERTA = 1;
    public static final int ERRO = 2;
    public static final int PADRAO = 3;

    public static final int TYPE_CPF = 10;
    public static final int TYPE_CNPJ = 20;
    public static final int TYPE_CEP = 30;
    public static final int TYPE_PHONE = 40;

    public static boolean checarConexaoCelular(Context ctx) {

        boolean conectado = false;

        try {

            final ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);

            final android.net.NetworkInfo wifi =
                    connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (connMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                conectado = true;
            }

            if (connMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED) {
                conectado = true;
            }

        } catch (Exception e) {
            conectado = false;
        }


        return conectado;
    }

    public static String AnoAtual(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    // Verifica se a String é null ou vazia ou só tem espaços em branco
    public static boolean isNullOrBlank(String s) {

        return (s == null || s.trim().equals(""));
    }

    // Verifica se a String é null ou vazia
    // Pode ser utilizado como suporte em APIs menores que 9 do android onde não está disponivel o metódo de String isEmpty()
    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.equals(""));
    }

    public static String FormataDataDDMMAAAA(String dataAmericanaString) {
        String retorno = "";
        String vc = dataAmericanaString.replace("-", "");
        retorno = vc.substring(6, 8) + "/" + vc.substring(4, 6) + "/" + vc.substring(0, 4);
        return retorno;
    }

    public static String FormataDataAAAAMMDD(String dataBrasilString) {
        String retorno = "";
        String vc = dataBrasilString.replace("/", "");
        retorno = vc.substring(4, 8) + "-" + vc.substring(2, 4) + "-" + vc.substring(0, 2);
        return retorno;
    }

    public static String FormataDataDDMMAAAA_ComHoras(String dataAmericanaString) {
        String retorno = "";
        String vc = dataAmericanaString.replace("-", "");
        retorno = vc.substring(6, 8) + "/" + vc.substring(4, 6) + "/" + vc.substring(0, 4) + vc.substring(8, 14);
        return retorno;
    }

    public static void log(String texto) {
        Log.i("Script", texto);
    }

    public static void msg_toast_personal(Context ctx, String mensagem, int Tipo_toast) {


        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);
        TextView tv = (TextView) layout.findViewById(R.id.txvTexto);
        //tv.setTextColor(Color.BLACK);
        tv.setText(mensagem);
        LinearLayout llRoot = (LinearLayout) layout.findViewById(R.id.llroot);
        Drawable img;
        int bg;

        switch (Tipo_toast) {

            case SUCESSO:
                img = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.appvendas, null);
                //bg = R.drawable.toast_sucesso;
                break;

            case ALERTA:
                img = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.appvendas, null);
                //bg = R.drawable.toast_alerta;
                break;

            case ERRO:
                img = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.appvendas, null);
                //bg = R.drawable.toast_erro;
                break;

            default:
                img = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.appvendas, null);
                //bg = R.drawable.toast_padrao;
                break;

        }

        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        //llRoot.setBackgroundResource(bg);
        Toast toast = new Toast(ctx);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

    }

    public static boolean validaCPF(String cpf) {
        if (cpf.length() == 11) {
            int d1, d2;
            int digito1, digito2, resto;
            int digitoCPF;
            String nDigResult;
            d1 = d2 = 0;
            digito1 = digito2 = resto = 0;
            for (int n_Count = 1; n_Count < cpf.length() - 1; n_Count++) {
                digitoCPF = Integer.valueOf(cpf.substring(n_Count - 1, n_Count)).intValue();
                d1 = d1 + (11 - n_Count) * digitoCPF;
                d2 = d2 + (12 - n_Count) * digitoCPF;
            }
            resto = (d1 % 11);

            if (resto < 2)
                digito1 = 0;
            else
                digito1 = 11 - resto;
            d2 += 2 * digito1;
            resto = (d2 % 11);

            if (resto < 2)
                digito2 = 0;
            else
                digito2 = 11 - resto;
            String nDigVerific = cpf.substring(cpf.length() - 2, cpf.length());
            nDigResult = String.valueOf(digito1) + String.valueOf(digito2);
            return nDigVerific.equals(nDigResult);
        }

        return false;

    }

    public static String AcrescentaZeros(String Zeros, int Qtd ){
        int Tamanho;
        String aux;

        aux = Zeros;
        Tamanho = Zeros.length();
        Zeros = "";

        for (int I = 0; I < (Qtd-Tamanho); I++) {
            Zeros = Zeros + "0";
        }
        aux = Zeros + aux;
        return aux;

    }

    public static String AcrescentaEspacosEsquerda(String Espaco, int Qtd ){
        int Tamanho;
        String aux;

        aux = Espaco;
        Tamanho = Espaco.length();
        Espaco = "";

        for (int I = 0; I < (Qtd-Tamanho); I++) {
            Espaco = Espaco + " ";
        }
        aux = Espaco + aux;
        return aux;
    }
    public static String AcrescentaEspacosDireita(String Espaco, int Qtd ){
        int Tamanho;
        String aux;

        aux = Espaco;
        Tamanho = Espaco.length();
        Espaco = "";

        for (int I = 0; I < (Qtd-Tamanho); I++) {
            Espaco = Espaco + " ";
        }
        aux = aux + Espaco;
        return aux;
    }

    //http://codare.net/2007/02/02/java-gerando-codigos-hash-md5-sha/
    public static byte[] gerarHash(String frase, String algoritmo) {
        try {
            MessageDigest md = MessageDigest.getInstance(algoritmo);
            md.update(frase.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String stringHexa(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
            int parteBaixa = bytes[i] & 0xf;
            if (parteAlta == 0)
                s.append('0');
            s.append(Integer.toHexString(parteAlta | parteBaixa));
        }
        return s.toString();
    }

    public static boolean validaCNPJ(String CNPJ) {

        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") || CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555") || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") || CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999") || (CNPJ.length() != 14))
            return (false);

        char dig13, dig14;
        int sm, i, r, num, peso;
        try {

            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {

                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig13 = '0';
            else
                dig13 = (char) ((11 - r) + 48);
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig14 = '0';
            else
                dig14 = (char) ((11 - r) + 48);


            if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13)))
                return (true);
            else
                return (false);
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    public static boolean validaEmail(String email) {
        boolean isEmailIdValid = false;
        if (email != null && email.length() > 0) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                isEmailIdValid = true;
            }
        }
        return isEmailIdValid;
    }

    public static String DataHojeComHorasUSA() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    public static String DataHojeComHorasBR() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    public static String DataHojeSemHorasBR() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    public static String DataHojeSemHorasUSA() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date data = new Date();
        return sdf.format(data.getTime());
    }


}











