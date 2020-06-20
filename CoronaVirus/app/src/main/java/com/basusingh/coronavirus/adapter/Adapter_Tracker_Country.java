package com.basusingh.coronavirus.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.CountryTimelineViewer;
import com.basusingh.coronavirus.IndiaStateList;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.tracker.TrackerItems;

import java.util.List;
import java.util.Locale;

public class Adapter_Tracker_Country extends RecyclerView.Adapter<Adapter_Tracker_Country.ViewHolder> {

    private Context context;
    private List<TrackerItems> list;

    public Adapter_Tracker_Country(Context mContext, List<TrackerItems> mItems){
        this.context = mContext;
        this.list = mItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title, code, total_cases, total_recovered, total_deaths, flag;
        ImageView arrow_button;

        public ViewHolder(View v){
            super(v);
            arrow_button = v.findViewById(R.id.arrow_button);
            title = v.findViewById(R.id.title);
            code = v.findViewById(R.id.code);
            total_cases = v.findViewById(R.id.total_cases);
            total_recovered = v.findViewById(R.id.total_recovered);
            total_deaths = v.findViewById(R.id.total_deaths);
            flag = v.findViewById(R.id.flag);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_tracker_country, parent, false);
        return new ViewHolder(v);
    }

    private String localeToEmoji(String countryCode) {
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position){
        final TrackerItems i = list.get(position);
        holder.title.setText(i.getTitle());
        holder.code.setText("[" + i.getCode() + "]");
        holder.total_cases.setText(i.getTotal_cases());
        holder.total_recovered.setText(i.getTotal_recovered());
        holder.total_deaths.setText(i.getTotal_deaths());
        holder.flag.setText(localeToEmoji(i.getCode()));
    }


    @Override
    public int getItemCount(){
        return list.size();
    }
}

