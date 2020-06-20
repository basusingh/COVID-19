package com.basusingh.coronavirus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.basusingh.coronavirus.database.tracker.TrackerItems;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.NetworkSingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CountryChartViewer extends AppCompatActivity {

    TrackerItems items;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    AnyChartView anyChartView;
    List<DataEntry> seriesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_chart_viewer);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            items = (TrackerItems) getIntent().getSerializableExtra("data");
        } else {
            finish();
            return;
        }
        TextView title = findViewById(R.id.title);
        title.setText(items.getTitle());

        seriesData = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(progressBar);
        progressDialog = new ProgressDialog(CountryChartViewer.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        FrameLayout cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadData();

        TextView flag = findViewById(R.id.flag);
        flag.setText(localeToEmoji(items.getCode()));
    }

    private String localeToEmoji(String countryCode) {
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    private void loadData(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_COUNTRY_DATA_ALTERNATE + items.getTitle(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        decodeData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Get all country");
                        showError("Internet connection problem. Please retry.");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private void decodeData(final String response){
        new LoadDataTask().execute(response);
    }

    private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            try{
                JSONArray a = new JSONArray(urls[0]);
                for(int i = 1; i<a.length(); i++){
                    JSONObject o2 = a.getJSONObject(i);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
                    Date date = simpleDateFormat.parse(o2.getString("Date").substring(0, 10));
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd-mm");
                    seriesData.add(new CustomDataEntry(simpleDateFormat1.format(date), o2.getInt("Confirmed"), o2.getInt("Deaths"), o2.getInt("Recovered")));
                }
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                showError("An error occurred! Please retry.");
            } else {
                if(!seriesData.isEmpty()){
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            makeGraph();
                        }
                    });
                } else {
                    showError("An error occurred! Please retry.");
                }
            }
        }
    }

    private void makeGraph(){
        Cartesian cartesian = AnyChart.line();
        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Distribution of Coronavirus in " + items.getTitle());

        cartesian.xAxis(0).title("Case distribution every week").labels().padding(5d, 5d, 5d, 5d);

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("Total cases");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        series1.stroke("#00695C");

        Line series2 = cartesian.line(series2Mapping);
        series2.name("Total deaths");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
        series2.stroke("#f80759");

        Line series3 = cartesian.line(series3Mapping);
        series3.name("Total recovered");
        series3.hovered().markers().enabled(true);
        series3.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series3.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
        series3.stroke("#38b6ff");

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(cartesian);
    }


    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
        }

    }

    private void showError(String message){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
