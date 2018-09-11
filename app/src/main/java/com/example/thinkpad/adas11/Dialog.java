package com.example.thinkpad.adas11;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Dialog extends AppCompatActivity {
private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        textView=(TextView) findViewById(R.id.text);
        Intent intent=getIntent();
        String data=intent.getStringExtra("data");
        textView.setText(data);
    }
}
