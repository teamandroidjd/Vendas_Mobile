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

public class actFiltroPeriodoPedidos extends AppCompatActivity {


    private DatePicker dpResult, dpResultFinal;
    private Button btnConfirmar;

    private int AnoInicio, AnoFim;
    private int MesInicio, MesFim;
    private int DiaInicio, DiaFim;
    private Date DataIni, DataFim;

    public String DataInicial, DataFinal;

    static final int DATE_DIALOG_ID_Inicio = 999;
    static final int DATE_DIALOG_ID_Fim = 998;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_filtro_periodo_pedidos);

        setCurrentDateOnViewInicio();
        setCurrentDateOnViewFim();


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

        dpResultFinal = (DatePicker) findViewById(R.id.dpResultFinal);
        dpResultFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID_Fim);
            }
        });


        dpResult = (DatePicker) findViewById(R.id.dpResult);
        dpResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID_Inicio);
            }
        });
    }

    private void setCurrentDateOnViewFim() {
       // tvDisplayDateFinal = (TextView) findViewById(R.id.tvDateFinal);
        dpResultFinal = (DatePicker) findViewById(R.id.dpResultFinal);

        final Calendar c = Calendar.getInstance();
        AnoFim = c.get(Calendar.YEAR);
        MesFim = c.get(Calendar.MONTH);
        DiaFim = c.get(Calendar.DAY_OF_MONTH);

        dpResultFinal.init(AnoFim, MesFim, DiaFim, null);

        if ((MesFim + 1) == 12){
            MesFim = 1;
        }else {
            MesFim = MesFim + 1;
        }

        // set current date into datepicker
        DataFinal = AnoFim + "-" + Util.AcrescentaZeros(String.valueOf((MesFim)),2) + "-" + (DiaFim +1);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            DataFim = (Date)formatter.parse(DiaFim + "/" + MesFim + "/" + AnoFim);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID_Inicio:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerLstInicio,
                        AnoInicio, MesInicio, DiaInicio);

            case DATE_DIALOG_ID_Fim:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerLstFim,
                        AnoFim, MesFim, DiaFim);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerLstInicio
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            AnoInicio = selectedYear;
            MesInicio = selectedMonth;
            DiaInicio = selectedDay;
            // set selected date into datepicker also
            dpResult.init(AnoInicio, MesInicio, DiaInicio, null);

            DataInicial = AnoInicio + "-" + Util.AcrescentaZeros(String.valueOf(MesInicio+1),2) + "-" + (DiaInicio);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            try {
                DataIni = (Date)formatter.parse(DiaInicio + "/" + (MesInicio+1) + "/" + AnoInicio);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    };

    private DatePickerDialog.OnDateSetListener datePickerLstFim
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            AnoFim = selectedYear;
            MesFim = selectedMonth;
            DiaFim = selectedDay;

            // set selected date into datepicker also
            dpResultFinal.init(AnoFim, MesFim, DiaFim, null);

            if ((MesFim + 1) == 12){
                MesFim = 1;
            }else {
                MesFim = MesFim + 1;
            }

            // set current date into datepicker
            DataFinal = AnoFim + "-" + Util.AcrescentaZeros(String.valueOf((MesFim)),2) + "-" + (DiaFim +1);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            try {
                DataFim = (Date)formatter.parse(DiaFim + "/" + MesFim + "/" + AnoFim);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    };

    private void setCurrentDateOnViewInicio() {

        //tvDisplayDate = (TextView) findViewById(R.id.tvDate);
        dpResult = (DatePicker) findViewById(R.id.dpResult);

        final Calendar ci = Calendar.getInstance();
        AnoInicio = ci.get(Calendar.YEAR);
        MesInicio = ci.get(Calendar.MONTH);
        DiaInicio = ci.get(Calendar.DAY_OF_MONTH);

        dpResult.init(AnoInicio, MesInicio, DiaInicio, null);

        DataInicial = AnoInicio + "-" + Util.AcrescentaZeros(String.valueOf(MesInicio+1 ),2) + "-" + DiaInicio;

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            DataIni = (Date)formatter.parse(DiaInicio + "/" + (MesInicio +1) + "/" + AnoInicio);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


}
