package com.jdsystem.br.vendasmobile;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class actCadPedidos extends AppCompatActivity {
    private TextView txtData;
    private int Ano, Mes, Dia, Hora, Minuto;
    private DatePicker datePicker;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cad_pedidos);
        txtData = (TextView) findViewById(R.id.txtDtEmis);
        calendar = Calendar.getInstance();
        Ano = calendar.get(Calendar.YEAR);

        Mes = calendar.get(Calendar.MONTH);
        Dia = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(Ano, Mes+1, Dia);
    }

    @SuppressWarnings("deprecation")
    public void LocData(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, Ano, Mes, Dia);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = Ano
                    // arg2 = Mes
                    // arg3 = Dia
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        txtData.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_act_cadpedidos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.act_salvarpedido) {
            SalvarPedido();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean SalvarPedido() {


         return true;


    }

}
