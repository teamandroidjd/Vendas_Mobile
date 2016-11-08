package com.jdsystem.br.vendasmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ConfigWeb extends AppCompatActivity {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    private EditText edthost;
    private Button btsalvhost;
    public SharedPreferences prefs;
    public String host;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_configweb);

        btsalvhost = (Button) findViewById(R.id.btsalvhost);
        edthost = (EditText) findViewById(R.id.edthost);

        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        host  = prefs.getString("host", null);

        if (host != null) {
            edthost.setText(host);
        }

        btsalvhost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edthost.getText().length() == 0) {
                    edthost.setError("Digite o caminho do host!");
                    edthost.requestFocus();
                    return;
                }

                SharedPreferences.Editor editorhost = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE).edit();
                editorhost.putString("host", edthost.getText().toString());
                editorhost.apply();
                Toast.makeText(ConfigWeb.this, "Host Salvo com Sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        }
    }