package com.jdsystem.br.vendasmobile;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by eduardo.costa on 08/11/2016.
 */
public class Mask {
    public static String CPF_MASK       = "###.###.###-##";
    public static String CNPJ_MASK      = "##.###.###/####-##";
    public static String TELEFONE_MASK  = "(##) ####-#####";
    public static String CEP_MASK       = "#####-###";
    public static String VALOR_MASK     = "#.###,##";


    public static String unmask(String s) {
        return s.replaceAll("[.]", "").replaceAll("[-]", "")
                .replaceAll("[/]", "").replaceAll("[(]", "")
                .replaceAll("[)]", "").replaceAll(" ", "")
                .replaceAll(",", "");
    }

    public static boolean isASign(char c) {
        if (c == '.' || c == '-' || c == '/' || c == '(' || c == ')' || c == ',' || c == ' ') {
            return true;
        } else {
            return false;
        }
    }

    public static String mask(String mask, String text) {
        int i = 0;
        String mascara = "";
        for (char m : mask.toCharArray()) {
            if (m != '#') {
                mascara += m;
                continue;
            }
            try {
                mascara += text.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }

        return mascara;
    }

    public static TextWatcher insert(final String mask, final TextView ediTxt) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = Mask.unmask(s.toString());
                String mascara = "";
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                int index = 0;
                for (int i = 0; i < mask.length(); i++) {
                    char m = mask.charAt(i);
                    if (m != '#') {
                        if (index == str.length() && str.length() < old.length()) {
                            continue;
                        }
                        mascara += m;
                        continue;
                    }

                    try {
                        mascara += str.charAt(index);
                    } catch (Exception e) {
                        break;
                    }

                    index++;
                }

                if (mascara.length() > 0) {
                    char last_char = mascara.charAt(mascara.length() - 1);
                    boolean hadSign = false;
                    while (isASign(last_char) && str.length() == old.length()) {
                        mascara = mascara.substring(0, mascara.length() - 1);
                        last_char = mascara.charAt(mascara.length() - 1);
                        hadSign = true;
                    }

                    if (mascara.length() > 0 && hadSign) {
                        mascara = mascara.substring(0, mascara.length() - 1);
                    }
                }

                isUpdating = true;
                ediTxt.setText(mascara);
                //ediTxt.setSelection(mascara.length());

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void afterTextChanged(Editable s) {}
        };
    }



    public static TextWatcher insert(final String mask, final EditText ediTxt) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = Mask.unmask(s.toString());
                String mascara = "";
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                int index = 0;
                for (int i = 0; i < mask.length(); i++) {
                    char m = mask.charAt(i);
                    if (m != '#') {
                        if (index == str.length() && str.length() < old.length()) {
                            continue;
                        }
                        mascara += m;
                        continue;
                    }

                    try {
                        mascara += str.charAt(index);
                    } catch (Exception e) {
                        break;
                    }

                    index++;
                }

                if (mascara.length() > 0) {
                    char last_char = mascara.charAt(mascara.length() - 1);
                    boolean hadSign = false;
                    while (isASign(last_char) && str.length() == old.length()) {
                        mascara = mascara.substring(0, mascara.length() - 1);
                        last_char = mascara.charAt(mascara.length() - 1);
                        hadSign = true;
                    }

                    if (mascara.length() > 0 && hadSign) {
                        mascara = mascara.substring(0, mascara.length() - 1);
                    }
                }

                isUpdating = true;
                ediTxt.setText(mascara);
                ediTxt.setSelection(mascara.length());

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void afterTextChanged(Editable s) {}
        };
    }

    public static String addMask(final String textoAFormatar, final String mask){
        String formatado = "";
        int i = 0;
        // vamos iterar a mascara, para descobrir quais caracteres vamos adicionar e quando...
        for (char m : mask.toCharArray()) {
            if (m != '#') { // se não for um #, vamos colocar o caracter informado na máscara
                formatado += m;
                continue;
            }
            // Senão colocamos o valor que será formatado
            try {
                formatado += textoAFormatar.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }
        return formatado;
    }

}

