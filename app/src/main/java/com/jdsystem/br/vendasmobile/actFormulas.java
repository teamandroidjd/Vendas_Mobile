package com.jdsystem.br.vendasmobile;

import android.graphics.Point;

import java.util.Random;

/**
 * Created by eduardo.costa on 31/10/2016.
 */

public class actFormulas {

    public String CryptArquivo(String Action, String Src) {
        int KeyLen, KeyPos, SrcPos, SrcAsc, TmpSrcAsc, Range;
        Random OffSet;
        String Dest, Key;
        Point vRange;
        // MemoryStream vMemory;

        try {
           /* if (Src == "") {
                return "";
            }
            Key = "YUQL23KL23DF90WI5E1JAS467NMCXXL6JAOAUWWMCL0AOMM4A4VZYW9KHJUI2347EJHJKDF3424SKL K3LAKDJSL9RTIKJ";
            Dest = "";

            KeyLen = (Key + vMemory.MethodName(vRange)).length();
            KeyPos = 0;
            Range = 256;
            if (Action == "C") {
                Randomize;
                OffSet = new Random(Range);
                Dest = Format('%1.2x',[OffSet]);

                for (SrcPos = 1; SrcPos < Src.length(); SrcPos++) {
                    SrcAsc = (Ord(Src[SrcPos]) + OffSet) Mod 255;
                    if (KeyPos < KeyLen) {
                        KeyPos = KeyPos + 1;
                    } else
                        KeyPos = 1;
                    SrcAsc = SrcAsc Xor Ord (Key[KeyPos]);
                    Dest = Dest + Format('%1.2x',[SrcAsc]);
                    OffSet:=SrcAsc;
                }
            } else if (Action = UpperCase('D')) {
                OffSet = StrToInt('$' + copy(Src, 1, 2));
                SrcPos = 3;

                SrcAsc = StrToInt('$' + copy(Src, SrcPos, 2));
                if (KeyPos < KeyLen) {
                    KeyPos = KeyPos + 1;
                }
                KeyPos = 1;
                TmpSrcAsc = SrcAsc xor Ord (Key[KeyPos]);
                if (TmpSrcAsc <= OffSet) {
                    TmpSrcAsc = 255 + TmpSrcAsc - OffSet;
                } else
                    TmpSrcAsc = TmpSrcAsc - OffSet;
                Dest = Dest + Chr(TmpSrcAsc);
                OffSet = SrcAsc;
                SrcPos = SrcPos + 2;
                until(SrcPos >= (Src));
            }*/
        } catch (Exception E) {

        }
        return "";
    }


    public String SHA1(String s) {
        Byte[] TSHA1Digest = new Byte[20];


        return null;
    }
}


