package com.weboffline.module;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.saredpreferences.module.SharedPreferencesModule;
import com.webmodule.offlinemodule.module.OfflineWebModule;

public class MainActivity extends AppCompatActivity {

    private OfflineWebModule module;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        module = new OfflineWebModule(new ReactApplicationContext(MainActivity.this));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                module.startWebModule("http://cannad.igorlysenko.com/api/vendors/1/stores/id/presentation", 5);
            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                module.downloadFrom("http://cannad.igorlysenko.com/api/vendors/1/stores/1/presentation",
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwiaWF0IjoxNTMyMDg4NjE3fQ.lHleLyfZBAC8jdMcHerzHIWbPF4jCoI1naD1t8D80Ec",
                        "MY_KEY");
            }
        });
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SharedPreferencesModule(new ReactApplicationContext(MainActivity.this))
                        .getPreferences("MY_KEY");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
