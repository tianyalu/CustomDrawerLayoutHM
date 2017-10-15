package com.sty.drawer.layout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.sty.drawer.layout.ui.SlideMenu;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageButton ibBack;
    private SlideMenu smSlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        ibBack = (ImageButton) findViewById(R.id.ib_back);
        ibBack.setOnClickListener(this);
        smSlide = (SlideMenu) findViewById(R.id.sm_slide);
    }

    public void onTabClick(View view){

    }

    @Override
    public void onClick(View view) {
        smSlide.switchState();
    }
}
