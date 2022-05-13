package com.yyzy.soundrecorder.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yyzy.soundrecorder.R;

public class RecorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
    }

    public void back(View view) {
        startActivity(new Intent(this,AudioListActivity.class));
        finish();
    }
}