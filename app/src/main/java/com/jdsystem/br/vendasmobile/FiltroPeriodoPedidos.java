package com.jdsystem.br.vendasmobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FiltroPeriodoPedidos extends AppCompatActivity {


    static final int DATE_DIALOG_ID_Inicio = 999;
    static final int DATE_DIALOG_ID_Fim = 998;
    public String DataInicial, DataFinal, usuario, senha;
    Calendar calendar;
    int year, dayOfMonth, month;
    private DatePickerDialog datePickerDialog;
    private EditText dpResultFinal, dpResult;
    private Date DataIni, DataFim;
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {


        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {


            int tag = ((Integer) view.getTag());

            if (tag == DATE_DIALOG_ID_Inicio) {
                int anoInicio = selectedYear;
                int mesInicio = selectedMonth;
                int diaInicio = selectedDay;

                dpResult.setText(diaInicio + "/" + (mesInicio + 1) + "/" + anoInicio);

                if ((mesInicio + 1) == 12) {
                    mesInicio = 1;
                } else {
                    //MesInicio = MesInicio + 1;
                }

                DataInicial = anoInicio + "-" + Util.AcrescentaZeros(String.valueOf(mesInicio + 1), 2) + "-" + Util.AcrescentaZeros(String.valueOf(diaInicio), 2);
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    DataIni = (Date) formatter.parse(diaInicio + "/" + (mesInicio + 1) + "/" + anoInicio);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else if (tag == DATE_DIALOG_ID_Fim) {
                int anoFim = selectedYear;
                int mesFim = selectedMonth;
                int diaFim = selectedDay;

                dpResultFinal.setText(diaFim + "/" + (mesFim + 1) + "/" + anoFim);

                if ((mesFim + 1) == 12) {
                    mesFim = 1;
                } else {
                    mesFim = mesFim + 1;
                }

                DataFinal =  anoFim + "-" + Util.AcrescentaZeros(String.valueOf((mesFim)), 2) + "-" + Util.AcrescentaZeros(String.valueOf(diaFim), 2);
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    DataFim = (Date) formatter.parse(diaFim + "/" + mesFim + "/" + anoFim);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtro_periodo_pedidos);
        Toolbar toolbar;
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        calendar = Calendar.getInstance();

        declaraobjetos();


        dpResultFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(FiltroPeriodoPedidos.this, datePickerListener, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setTag(DATE_DIALOG_ID_Fim);
                datePickerDialog.setTitle("Data final");
                datePickerDialog.show();
            }
        });

        dpResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(FiltroPeriodoPedidos.this, datePickerListener, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setTag(DATE_DIALOG_ID_Inicio);
                datePickerDialog.setTitle("Data inicial");
                datePickerDialog.show();
            }
        });
    }

    private void declaraobjetos() {

        Button btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
        dpResultFinal = (EditText) findViewById(R.id.dpResultFinal);
        //dpResultFinal.addTextChangedListener(Mask.insert(Mask.DATA_MASK, dpResultFinal));
        dpResult = (EditText) findViewById(R.id.dpResult);
        //dpResult.addTextChangedListener(Mask.insert(Mask.DATA_MASK, dpResult));

    }

    public void confirmafiltro(View view) {
        if (DataIni == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(FiltroPeriodoPedidos.this, "Informar a data inicial!", Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(this, "Informar a data inicial!", Toast.LENGTH_SHORT).show();
            }
            return;
        } else if (DataFim == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(FiltroPeriodoPedidos.this, "Informar a data final", Toast.LENGTH_SHORT);
            }else {
                Toast.makeText(this, "Informar a data final", Toast.LENGTH_SHORT).show();
            }
            return;
        } else if (DataIni.before(DataFim) || (DataIni.equals(DataFim))) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(getString(R.string.intent_datainicial), DataInicial);
            returnIntent.putExtra(getString(R.string.intent_datafinal), DataFinal);
            returnIntent.putExtra(getString(R.string.intent_senha), senha);
            returnIntent.putExtra(getString(R.string.intent_usuario), usuario);

            setResult(3, returnIntent);
            finish();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Util.msg_toast_personal(FiltroPeriodoPedidos.this, getString(R.string.data_inicialfinal_invalida), Toast.LENGTH_SHORT);
            }else {
                Toast.makeText(this, getString(R.string.data_inicialfinal_invalida), Toast.LENGTH_SHORT).show();
            }
        }

    }

}
