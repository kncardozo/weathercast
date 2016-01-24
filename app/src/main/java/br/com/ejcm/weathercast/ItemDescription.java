package br.com.ejcm.weathercast;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static br.com.ejcm.weathercast.R.id.item_description;

public class ItemDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String description = intent.getStringExtra(ListForecastFragment.DETAIL);
        TextView textView = new TextView(this);
        textView.setText(description);
        RelativeLayout rl = (RelativeLayout) findViewById (item_description);
        rl.addView(textView);
    }

}
