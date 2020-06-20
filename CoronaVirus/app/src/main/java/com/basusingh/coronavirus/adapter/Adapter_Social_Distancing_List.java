package com.basusingh.coronavirus.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListItems;

import java.util.List;

public class Adapter_Social_Distancing_List extends RecyclerView.Adapter<Adapter_Social_Distancing_List.ViewHolder> {

    private Context context;
    private List<SocialDistancingListItems> list;

    public Adapter_Social_Distancing_List(Context mContext, List<SocialDistancingListItems> mItems){
        this.context = mContext;
        this.list = mItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, url;
        LinearLayout topLayout;
        public ViewHolder(View v){
            super(v);
            name = v.findViewById(R.id.name);
            url = v.findViewById(R.id.url);
            topLayout = v.findViewById(R.id.topLayout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_social_distancing, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position){
        final SocialDistancingListItems i = list.get(position);
        viewHolder.name.setText(i.getName());
        viewHolder.url.setText(i.getUrl());
        viewHolder.topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView(i.getUrl());
            }
        });
    }

    private void openWebView(String s){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(s));
        context.startActivity(i);
    }
    @Override
    public int getItemCount(){
        return list.size();
    }
}
