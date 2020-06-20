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
import com.basusingh.coronavirus.utils.StateDataList;

import java.util.List;

public class Adapter_State extends RecyclerView.Adapter<Adapter_State.ViewHolder> {

    private Context context;
    private List<StateDataList> list;

    public Adapter_State(Context mContext, List<StateDataList> mItems){
        this.context = mContext;
        this.list = mItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView state, recovered, confirmed, deceased;
        public ViewHolder(View v){
            super(v);
            state = v.findViewById(R.id.state);
            recovered = v.findViewById(R.id.recovered);
            confirmed = v.findViewById(R.id.confirmed);
            deceased = v.findViewById(R.id.deceased);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_state, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position){
        StateDataList item = list.get(position);
        viewHolder.state.setText(item.getState());
        viewHolder.deceased.setText(item.getTotalDeaths());
        viewHolder.recovered.setText(item.getTotalRecovered());
        viewHolder.confirmed.setText(item.getTotalCase());
    }

    @Override
    public int getItemCount(){
        return list.size();
    }
}


