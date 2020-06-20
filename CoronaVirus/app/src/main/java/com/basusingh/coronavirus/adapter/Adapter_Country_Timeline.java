package com.basusingh.coronavirus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.utils.CoronaTimelineItems;

import java.util.List;

public class Adapter_Country_Timeline extends RecyclerView.Adapter<Adapter_Country_Timeline .ViewHolder> {

    private Context context;
    private List<CoronaTimelineItems> list;

    public Adapter_Country_Timeline (Context mContext, List<CoronaTimelineItems> mItems){
        this.context = mContext;
        this.list = mItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title, total_new_daily_case, total_new_daily_deaths, total_cases, total_recovered, total_deaths;
        ImageView arrow_button;
        LinearLayout info_layout, topLayout;

        public ViewHolder(View v){
            super(v);
            info_layout = v.findViewById(R.id.info_layout);
            arrow_button = v.findViewById(R.id.arrow_button);
            title = v.findViewById(R.id.title);
            total_cases = v.findViewById(R.id.total_cases);
            total_recovered = v.findViewById(R.id.total_recovered);
            total_deaths = v.findViewById(R.id.total_deaths);
            total_new_daily_case = v.findViewById(R.id.total_new_daily_case);
            total_new_daily_deaths = v.findViewById(R.id.total_new_daily_deaths);
            topLayout = v.findViewById(R.id.topLayout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_country_timeline, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position){
        final CoronaTimelineItems i = list.get(position);
        holder.title.setText(i.getDate());
        holder.total_cases.setText(i.getTotal_cases());
        holder.total_recovered.setText(i.getTotal_recoveries());
        holder.total_deaths.setText(i.getTotal_deaths());
        holder.total_new_daily_case.setText(i.getNew_daily_cases());
        holder.total_new_daily_deaths.setText(i.getNew_daily_deaths());
        holder.topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.info_layout.getVisibility() == View.VISIBLE){
                    holder.info_layout.setVisibility(View.GONE);
                    holder.info_layout.animate().alpha(0.0f);
                    holder.arrow_button.setImageResource(R.drawable.arrow_down);
                } else {
                    holder.info_layout.setVisibility(View.VISIBLE);
                    holder.info_layout.animate().alpha(1.0f);
                    holder.arrow_button.setImageResource(R.drawable.arrow_up);
                }
            }
        });
    }


    @Override
    public int getItemCount(){
        return list.size();
    }
}


