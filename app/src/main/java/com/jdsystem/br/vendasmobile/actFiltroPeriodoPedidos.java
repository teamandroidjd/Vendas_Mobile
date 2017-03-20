package com.jdsystem.br.vendasmobile;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.jdsystem.br.vendasmobile.R.drawable.calendar;

public class actFiltroPeriodoPedidos extends AppCompatActivity {


    private DatePickerDialog datePickerDialog;
    private EditText dpResultFinal, dpResult;
    private Button btnConfirmar;

    Calendar calendar;

    private int AnoInicio, AnoFim;
    private int MesInicio, MesFim;
    private int DiaInicio, DiaFim;
    private Date DataIni, DataFim;

    public String DataInicial, DataFinal;
    int year, dayOfMonth, month;

    static final int DATE_DIALOG_ID_Inicio = 999;
    static final int DATE_DIALOG_ID_Fim = 998;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_filtro_periodo_pedidos);

        calendar = Calendar.getInstance();

        btnConfirmar = (Button)findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataIni.before(DataFim) || (DataIni.equals(DataFim))) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("datainicial", DataInicial);
                    returnIntent.putExtra("datafinal", DataFinal);
                    setResult(3, returnIntent);
                    finish();
                }else {
                    Util.msg_toast_personal(actFiltroPeriodoPedidos.this,"Data Inicial Maior que a Data Final", Toast.LENGTH_SHORT);
                }
            }
        });

        dpResultFinal = (EditText) findViewById(R.id.dpResultFinal);
        dpResultFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(actFiltroPeriodoPedidos.this, datePickerListener, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setTag(DATE_DIALOG_ID_Fim);
                datePickerDialog.setTitle("Data final");
                datePickerDialog.show();
            }
        });


        dpResult = (EditText) findViewById(R.id.dpResult);
        dpResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(actFiltroPeriodoPedidos.this, datePickerListener, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setTag(DATE_DIALOG_ID_Inicio);
                datePickerDialog.setTitle("Data inicial");
                datePickerDialog.show();
            }
        });
    }




    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {


        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

            int tag = ((Integer) view.getTag());

            if (tag == DATE_DIALOG_ID_Inicio) {
                AnoInicio = selectedYear;
                MesInicio = selectedMonth;
                DiaInicio = selectedDay;

                dpResult.setText(DiaInicio + "/" + (MesInicio+1) + "/" + AnoInicio);

                if ((MesInicio + 1) == 12){
                    MesInicio = 1;
                }else {
                    //MesInicio = MesInicio + 1;
                }

                DataInicial = AnoInicio + "-" + Util.AcrescentaZeros(String.valueOf(MesInicio + 1), 2) + "-" + Util.AcrescentaZeros(String.valueOf(DiaInicio),2);;
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    DataIni = (Date) formatter.parse(DiaInicio + "/" + (MesInicio + 1) + "/" + AnoInicio);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else if(tag == DATE_DIALOG_ID_Fim)
            {
                AnoFim = selectedYear;
                MesFim = selectedMonth;
                DiaFim = selectedDay;

                dpResultFinal.setText(DiaFim + "/" + (MesFim+1) + "/" + AnoFim);

                if ((MesFim + 1) == 12){
                    MesFim = 1;
                }else {
                    MesFim = MesFim + 1;
                }

                DataFinal = AnoFim + "-" + Util.AcrescentaZeros(String.valueOf((MesFim)),2) + "-" + Util.AcrescentaZeros(String.valueOf(DiaFim),2);
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    DataFim = (Date)formatter.parse(DiaFim + "/" + MesFim + "/" + AnoFim);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


        }
    };






}
