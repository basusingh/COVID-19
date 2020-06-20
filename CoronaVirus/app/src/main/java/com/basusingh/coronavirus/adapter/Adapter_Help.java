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

import com.basusingh.coronavirus.Help;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.help.HelpItems;

import java.util.List;

public class Adapter_Help extends RecyclerView.Adapter<Adapter_Help.ViewHolder> {

    private Context context;
    private List<HelpItems> list;

    public Adapter_Help(Context mContext, List<HelpItems> mItems){
        this.context = mContext;
        this.list = mItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, number;
        LinearLayout topLayout;
        public ViewHolder(View v){
            super(v);
            name = v.findViewById(R.id.name);
            number = v.findViewById(R.id.number);
            topLayout = v.findViewById(R.id.topLayout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_help, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position){
        final HelpItems i = list.get(position);
        viewHolder.name.setText(i.getName());
        viewHolder.number.setText(i.getNumber());
        viewHolder.topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialer(i.getNumber());
            }
        });
    }

    private void openDialer(String s){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + s));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    public int getItemCount(){
        return list.size();
    }
}

