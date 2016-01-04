package com.example.junhong.beachat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Junhong on 2016-01-04.
 */
public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.github.nkzawa.socketio.androidchat.R.layout.activity_main);
    }
}
