package com.jdsystem.br.vendasmobile.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.IntegerRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("Registered")
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

            final ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE);

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

    public static String AnoAtual() {
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void msg_toast_personal(Context ctx, String mensagem, int Tipo_toast) {


        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
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

    public static String AcrescentaZeros(String Zeros, int Qtd) {
        int Tamanho;
        String aux;

        aux = Zeros;
        Tamanho = Zeros.length();
        Zeros = "";

        for (int I = 0; I < (Qtd - Tamanho); I++) {
            Zeros = Zeros + "0";
        }
        aux = Zeros + aux;
        return aux;

    }

    public static String AcrescentaEspacosEsquerda(String Espaco, int Qtd) {
        int Tamanho;
        String aux;

        aux = Espaco;
        Tamanho = Espaco.length();
        Espaco = "";

        for (int I = 0; I < (Qtd - Tamanho); I++) {
            Espaco = Espaco + " ";
        }
        aux = Espaco + aux;
        return aux;
    }

    public static String AcrescentaEspacosDireita(String Espaco, int Qtd) {
        int Tamanho;
        String aux;

        aux = Espaco;
        Tamanho = Espaco.length();
        Espaco = "";

        for (int I = 0; I < (Qtd - Tamanho); I++) {
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    public static String DataHojeComHorasBR() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date data = new Date();
        return sdf.format(data.getTime());
    }

    public static String DataHojeComHorasMinSecBR() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

    public static String diaSemana(int num) {
        String mDiaSemana = null;
        switch (num) {
            case 0:
                mDiaSemana = "Domingo";
                break;
            case 1:
                mDiaSemana = "Segunda-feira";
                break;
            case 2:
                mDiaSemana = "Terça-feira";
                break;
            case 3:
                mDiaSemana = "Quarta-feira";
                break;
            case 4:
                mDiaSemana = "Quinta-feira";
                break;
            case 5:
                mDiaSemana = "Sexta-feira";
                break;
            case 6:
                mDiaSemana = "Sábado";
                break;
        }
        return mDiaSemana;
    }

    public static void gravarItensContato(String codProduto, int codProdInterno, int codContato, Context context) {
        SQLiteDatabase db = new ConfigDB(context).getReadableDatabase();
        try {
            db.execSQL("insert into produtos_contatos (cod_produto_manual, cod_interno_contato, cod_item) values " +
                    "('" + codProduto + "', " + codContato + ", " + codProdInterno + ");");
        } catch (Exception E) {
            E.toString();
        }
    }

    public static String converteUf(String uf) {

        switch (uf) {
            case ("AC"):
                uf = "Acre";
                break;
            case ("AL"):
                uf = "Alagoas";
                break;
            case ("AP"):
                uf = "Amapá";
                break;
            case ("AM"):
                uf = "Amazonas";
                break;
            case ("BA"):
                uf = "Bahia";
                break;
            case ("CE"):
                uf = "Ceará";
                break;
            case ("DF"):
                uf = "Distrito Federal";
                break;
            case ("ES"):
                uf = "Espírito Santo";
                break;
            case ("GO"):
                uf = "Goiás";
                break;
            case ("MA"):
                uf = "Maranhão";
                break;
            case ("MT"):
                uf = "Mato Grosso";
                break;
            case ("MS"):
                uf = "Mato Grosso do Sul";
                break;
            case ("MG"):
                uf = "Minas Gerais";
                break;
            case ("PA"):
                uf = "Pará";
                break;
            case ("PB"):
                uf = "Paraíba";
                break;
            case ("PR"):
                uf = "Paraná";
                break;
            case ("PE"):
                uf = "Pernambuco";
                break;
            case ("PI"):
                uf = "Piauí";
                break;
            case ("RJ"):
                uf = "Rio de Janeiro";
                break;
            case ("RN"):
                uf = "Rio Grande do Norte";
                break;
            case ("RS"):
                uf = "Rio Grande do Sul";
                break;
            case ("RO"):
                uf = "Rondônia";
                break;
            case ("RR"):
                uf = "Roraima";
                break;
            case ("SC"):
                uf = "Santa Catarina";
                break;
            case ("SP"):
                uf = "São Paulo";
                break;
            case ("SE"):
                uf = "Sergipe";
                break;
            case ("TO"):
                uf = "Tocantins";
                break;
        }
        return uf;
    }

    public static void gravaContatoSincronizado(Context ctx, String codContato, int codContatoInt) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();

        Cursor cursor = db.rawQuery("select FLAGINTEGRADO, CODCONTATO_EXT from CONTATO " +
                "where CODCONTATO_INT = " + codContatoInt, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                db.execSQL("update CONTATO set FLAGINTEGRADO = 'S', CODCONTATO_EXT = '" + codContato + "'");
            } while (cursor.moveToNext());
        }
        cursor.close();

    }

    public static String removeZerosEsquerda(String linha) {
        String x = null;
        if(linha.startsWith("0")){
            x = linha.replaceAll("0", "");
        } else {
            x = linha;
        }
        return x;
    }

    public static void atualizaCargoContato(String desCargo, String sCodCargo, String atvCargo, Context ctx){
        //int codCargo = Integer.parseInt(sCodCargo);

        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();

        if(!desCargo.equals("")){
            int codCargo = Integer.parseInt(sCodCargo);
            Cursor cursCargos = db.rawQuery("select DES_CARGO, CODCARGO_EXT, ATIVO from CARGOS " +
                    "where CODCARGO_EXT = " + codCargo ,null);
            cursCargos.moveToFirst();
            if((cursCargos.getCount()>0)&&(!cursCargos.getString(cursCargos.getColumnIndex("CODCARGO_EXT")).equals(desCargo))){
                try {
                    db.execSQL("update CARGOS set DES_CARGO = '" + desCargo + "', ATIVO = '" +atvCargo+
                            "' where CODCARGO_EXT = " + codCargo);
                }catch (Exception e){
                    e.toString();
                }
            } else {
                try{
                    db.execSQL("insert into CARGOS (DES_CARGO, CODCARGO_EXT, ATIVO) values ('" + desCargo + "', " + codCargo + ", '" +
                            atvCargo + "');");
                }catch(Exception e){
                    e.toString();
                }
            }
            if(!atvCargo.equals(cursCargos.getString(cursCargos.getColumnIndex("ATIVO")))){
                try {
                    db.execSQL("update CARGOS set ATIVO = '" + atvCargo + "' where CODCARGO_EXT = " + codCargo);
                }catch (Exception E){
                    E.toString();
                }
            }
            cursCargos.close();
        }

    }

    public static String verificaString(String resultJson){
        char caracter = ';';
        int j = 100;
        String retornaCodCargo = "";
        for(int i=0;i < resultJson.length();i++){
            if(resultJson.charAt(i) == caracter){
                j = i;
            }
            if(i > j){
                retornaCodCargo = retornaCodCargo + resultJson.charAt(i);
            }

        }
        return retornaCodCargo.substring(1,retornaCodCargo.length());
    }

    public static String retornaCodContato(String codContato){
        String retornaCodContato = "";
        int i = 0;
        char caracter = ';';
        while(codContato.charAt(i) != caracter){
            retornaCodContato = retornaCodContato + codContato.charAt(i);
            i++;
        }
        return retornaCodContato;
    }

    public static void gravaHorariosContatos(Context ctx, String hrInicio, String hrFinal, int codDiaSemana, int codContato){
        char caracter = ':';
        int i = 0;

        int horaInicio = 0;
        int minutoInicio = 0;
        int horaFinal = 0;
        int minutoFinal = 0;

        String horaInicial = "";

        while(hrInicio.charAt(i) != caracter){
            horaInicial = horaInicial + hrInicio.charAt(i);
            i++;
        }
        horaInicio = Integer.parseInt(horaInicial);

        int j = 100;
        i=0;
        String retornaCodCargo = "";
        for(i=0;i < hrInicio.length();i++){
            if(hrInicio.charAt(i) == caracter){
                j = i;
            }
            if(i > j){
                retornaCodCargo = retornaCodCargo + hrInicio.charAt(i);
            }
        }
        minutoInicio = Integer.parseInt(retornaCodCargo);

        String hourFinal = "";
        i = 0;

        while(hrFinal.charAt(i) != caracter){
            hourFinal = hourFinal + hrFinal.charAt(i);
            i++;
        }
        horaFinal = Integer.parseInt(hourFinal);

        i=0;
        j = 100;
        retornaCodCargo = "";
        for(i=0;i < hrFinal.length();i++){
            if(hrFinal.charAt(i) == caracter){
                j = i;
            }
            if(i > j){
                retornaCodCargo = retornaCodCargo + hrFinal.charAt(i);
            }
        }
        minutoFinal = Integer.parseInt(retornaCodCargo);

        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();

        Cursor cursorDias = db.rawQuery("SELECT CODCONTATOEXT, HORA_INICIO, MINUTO_INICIO, HORA_FINAL, MINUTO_FINAL, " +
                "COD_DIA_SEMANA " +
                "WHERE CODCONTATOEXT = " + codContato + " AND HORA_INICIO = " + horaInicio + " AND MINUTO_INICIO = " +
                minutoInicio + " AND HORA_FINAL = " + horaFinal + " AND MINUTO_FINAL = " + minutoFinal +
                " AND COD_DIA_SEMANA = " + codDiaSemana, null );
        if(cursorDias.getCount()==0){
            db.execSQL("insert into dias_contatos (CODCONTATO_EXT, HORA_INICIO, MINUTO_INICIO, HORA_FINAL, MINUTO_FINAL, " +
                    "COD_DIA_SEMANA) VALUES (" + codContato +"," + horaInicio + "," + minutoInicio + "," + horaFinal + "," +
                    minutoFinal + "," + codDiaSemana);
        }
    }

    public static void setIntegrar(int codContato, Context ctx) {
        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT CODCONTATO_INT, FLAGINTEGRADO FROM CONTATO WHERE CODCONTATO_INT = " + codContato, null);

            if (cursor.getCount() > 0) {
                db.execSQL("UPDATE CONTATO SET FLAGINTEGRADO = 'N'");
            }
        }catch (Exception e){
            e.toString();
        }
    }
}











