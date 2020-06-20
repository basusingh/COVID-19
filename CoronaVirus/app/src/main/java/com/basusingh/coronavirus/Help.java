package com.basusingh.coronavirus;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.basusingh.coronavirus.adapter.Adapter_Help;
import com.basusingh.coronavirus.database.help.HelpDatabaseClient;
import com.basusingh.coronavirus.database.help.HelpItems;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.RecyclerViewItemDivider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Help extends AppCompatActivity {

    private RecyclerView recyclerView;
    List<HelpItems> list;
    Adapter_Help mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView btn_fb = findViewById(R.id.btn_fb);
        ImageView btn_insta = findViewById(R.id.btn_insta);
        ImageView btn_github = findViewById(R.id.btn_github);
        ImageView btn_insta_ashish = findViewById(R.id.btn_insta_ashish);

        btn_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAction(1);
            }
        });
        btn_github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAction(3);
            }
        });
        btn_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAction(2);
            }
        });
        btn_insta_ashish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAction(4);
            }
        });


        list = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new RecyclerViewItemDivider(getApplicationContext()));
        loadItem();
    }

    private void startAction(int type){
        switch (type){
            case 1:
                try {
                    getPackageManager()
                            .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("fb://profile/1713863588"));
                    startActivity(i);
                } catch (Exception e) {
                    Intent i =  new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.facebook.com/basusingh22"));
                    startActivity(i);
                }

                break;
            case 2:
                Uri uri1 = Uri.parse("https://www.instagram.com/basusingh/");
                Intent likeIng1 = new Intent(Intent.ACTION_VIEW, uri1);

                likeIng1.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng1);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/basusingh/")));
                }
                break;
            case 3:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/basusingh")));
                break;
            case 4:
                Uri uri2 = Uri.parse("https://www.instagram.com/knowtheashish/");
                Intent likeIng2 = new Intent(Intent.ACTION_VIEW, uri2);

                likeIng2.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng2);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/knowtheashish/")));
                }
                break;
        }
    }


    private void loadItem(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... v){
                list = HelpDatabaseClient.getInstance(getApplicationContext()).getHelpAppDatabase().HelpDao().getAll();
                return null;
            }
            @Override
            public void onPostExecute(Void v){
                mAdapter = new Adapter_Help(getApplicationContext(), list);
                recyclerView.setAdapter(mAdapter);
            }
        }.execute();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale newLocale;
        SharedPreferences sharedPreferences = newBase.getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        newLocale = new Locale(sharedPreferences.getString(Constant.current_language, "en"));
        Context context = ContextWrapper.wrap(newBase, newLocale);
        super.attachBaseContext(context);
    }
}
