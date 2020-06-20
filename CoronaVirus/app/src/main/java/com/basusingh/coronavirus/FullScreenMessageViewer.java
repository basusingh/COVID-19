package com.basusingh.coronavirus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basusingh.coronavirus.utils.ContextWrapper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FullScreenMessageViewer extends AppCompatActivity {

    String title, message, url;
    ImageView image;
    TextView mTitle, mMessage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_message_viewer);

        Intent intent = getIntent();
        if(intent != null){
            title = intent.getStringExtra("title");
            message = intent.getStringExtra("message");
            url = intent.getStringExtra("url");
        } else {
            return;
        }

        progressDialog = new ProgressDialog(FullScreenMessageViewer.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please wait");
        image = findViewById(R.id.image);
        mTitle = findViewById(R.id.title);
        mMessage = findViewById(R.id.message);

        mTitle.setText(title);
        mMessage.setText(message);

        progressDialog.show();

        Glide.with(getApplicationContext())
                .load(url)
                .transition(withCrossFade())
                .placeholder(R.drawable.error_icon)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        image.setVisibility(View.VISIBLE);
                        image.setImageResource(R.drawable.error_icon);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable drawable, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressDialog.dismiss();
                        image.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(image);

        FrameLayout btn_done = findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
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
