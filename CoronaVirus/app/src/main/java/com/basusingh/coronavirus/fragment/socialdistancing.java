package com.basusingh.coronavirus.fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.adapter.Adapter_Social_Distancing_List;
import com.basusingh.coronavirus.adapter.Adapter_Social_Distancing_News;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListItems;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsItems;
import com.basusingh.coronavirus.utils.RecyclerViewItemDivider;

import java.util.ArrayList;
import java.util.List;


public class socialdistancing extends Fragment {

    public socialdistancing() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView_news, recyclerView_ent, recyclerView_edu,  recyclerView_health,  recyclerView_virtual,  recyclerView_gaming,  recyclerView_bizarre;
    SwipeRefreshLayout mySwipeRefreshLayout;
    private Adapter_Social_Distancing_List mAdapter_ent, mAdapter_edu, mAdapter_health, mAdapter_virtual, mAdapter_gaming, mAdapter_bizarre;
    private Adapter_Social_Distancing_News mAdapter_news;
    List<SocialDistancingListItems> mList_ent, mList_edu, mList_healthcare, mList_virtual, mList_gaming, mList_bizarre;
    List<SocialDistancingNewsItems> mList_news;
    LinearLayout entertainment_layout, education_layout, healthcare_layout ,virtual_socializing_layout, gaming_layout, bizarre_layout, news_layout;
    ImageView arrow_button_ent, arrow_button_edu, arrow_button_health, arrow_button_virtual, arrow_button_gaming, arrow_button_bizarre, arrow_button_news;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_socialdistancing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle s){

        mySwipeRefreshLayout = v.findViewById(R.id.swipeContainer);

        mList_ent = new ArrayList<>();
        mList_edu = new ArrayList<>();
        mList_healthcare = new ArrayList<>();
        mList_virtual = new ArrayList<>();
        mList_gaming = new ArrayList<>();
        mList_bizarre = new ArrayList<>();
        mList_news = new ArrayList<>();

        recyclerView_bizarre = v.findViewById(R.id.recyclerView_bizarre);
        recyclerView_edu = v.findViewById(R.id.recyclerView_edu);
        recyclerView_ent = v.findViewById(R.id.recyclerView_ent);
        recyclerView_gaming = v.findViewById(R.id.recyclerView_gaming);
        recyclerView_health = v.findViewById(R.id.recyclerView_health);
        recyclerView_virtual = v.findViewById(R.id.recyclerView_virtual);
        recyclerView_news = v.findViewById(R.id.recyclerView_news);

        entertainment_layout = v.findViewById(R.id.entertainment_layout);
        education_layout = v.findViewById(R.id.education_layout);
        healthcare_layout = v.findViewById(R.id.healthcare_layout);
        virtual_socializing_layout = v.findViewById(R.id.virtual_socializing_layout);
        gaming_layout = v.findViewById(R.id.gaming_layout);
        bizarre_layout = v.findViewById(R.id.bizarre_layout);
        news_layout = v.findViewById(R.id.news_layout);

        arrow_button_ent = v.findViewById(R.id.arrow_button_ent);
        arrow_button_edu = v.findViewById(R.id.arrow_button_edu);
        arrow_button_health = v.findViewById(R.id.arrow_button_health);
        arrow_button_virtual = v.findViewById(R.id.arrow_button_virtual);
        arrow_button_gaming = v.findViewById(R.id.arrow_button_gaming);
        arrow_button_bizarre = v.findViewById(R.id.arrow_button_bizarre);
        arrow_button_news = v.findViewById(R.id.arrow_button_news);

        setUpRecyclerView();
        setUpLayoutClick();


        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadData();
                    }
                }
        );

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    private void setUpLayoutClick(){
        entertainment_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_ent.getVisibility() == View.VISIBLE){
                    recyclerView_ent.setVisibility(View.GONE);
                    arrow_button_ent.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_ent.setVisibility(View.VISIBLE);
                    arrow_button_ent.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        education_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_edu.getVisibility() == View.VISIBLE){
                    recyclerView_edu.setVisibility(View.GONE);
                    arrow_button_edu.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_edu.setVisibility(View.VISIBLE);
                    arrow_button_edu.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        healthcare_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_health.getVisibility() == View.VISIBLE){
                    recyclerView_health.setVisibility(View.GONE);
                    arrow_button_health.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_health.setVisibility(View.VISIBLE);
                    arrow_button_health.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        virtual_socializing_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_virtual.getVisibility() == View.VISIBLE){
                    recyclerView_virtual.setVisibility(View.GONE);
                    arrow_button_virtual.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_virtual.setVisibility(View.VISIBLE);
                    arrow_button_virtual.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        gaming_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_gaming.getVisibility() == View.VISIBLE){
                    recyclerView_gaming.setVisibility(View.GONE);
                    arrow_button_gaming.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_gaming.setVisibility(View.VISIBLE);
                    arrow_button_gaming.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        bizarre_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_bizarre.getVisibility() == View.VISIBLE){
                    recyclerView_bizarre.setVisibility(View.GONE);
                    arrow_button_bizarre.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_bizarre.setVisibility(View.VISIBLE);
                    arrow_button_bizarre.setImageResource(R.drawable.arrow_up);
                }
            }
        });

        news_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_news.getVisibility() == View.VISIBLE){
                    recyclerView_news.setVisibility(View.GONE);
                    arrow_button_news.setImageResource(R.drawable.arrow_down);
                } else {
                    recyclerView_news.setVisibility(View.VISIBLE);
                    arrow_button_news.setImageResource(R.drawable.arrow_up);
                }
            }
        });
    }

    private void setUpRecyclerView(){
        recyclerView_ent.setHasFixedSize(true);
        recyclerView_ent.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_ent.setNestedScrollingEnabled(false);
        recyclerView_ent.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_edu.setHasFixedSize(true);
        recyclerView_edu.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_edu.setNestedScrollingEnabled(false);
        recyclerView_edu.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_health.setHasFixedSize(true);
        recyclerView_health.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_health.setNestedScrollingEnabled(false);
        recyclerView_health.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_virtual.setHasFixedSize(true);
        recyclerView_virtual.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_virtual.setNestedScrollingEnabled(false);
        recyclerView_virtual.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_gaming.setHasFixedSize(true);
        recyclerView_gaming.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_gaming.setNestedScrollingEnabled(false);
        recyclerView_gaming.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_bizarre.setHasFixedSize(true);
        recyclerView_bizarre.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_bizarre.setNestedScrollingEnabled(false);
        recyclerView_bizarre.addItemDecoration(new RecyclerViewItemDivider(getActivity()));

        recyclerView_news.setHasFixedSize(true);
        recyclerView_news.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_news.setNestedScrollingEnabled(false);
        recyclerView_news.addItemDecoration(new RecyclerViewItemDivider(getActivity()));
    }

    private void loadData(){
        if(!mySwipeRefreshLayout.isRefreshing()){
            mySwipeRefreshLayout.setRefreshing(true);
            mList_ent.clear();
            mList_edu.clear();
            mList_healthcare.clear();
            mList_gaming.clear();
            mList_virtual.clear();
            mList_bizarre.clear();
            mList_news.clear();
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... v){
                mList_ent = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Entertainment");
                mAdapter_ent = new Adapter_Social_Distancing_List(getActivity(), mList_ent);

                mList_edu = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Education");
                mAdapter_edu = new Adapter_Social_Distancing_List(getActivity(), mList_edu);

                mList_healthcare = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Healthcare");
                mAdapter_health = new Adapter_Social_Distancing_List(getActivity(), mList_healthcare);

                mList_virtual = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Virtual");
                mAdapter_virtual = new Adapter_Social_Distancing_List(getActivity(), mList_virtual);

                mList_gaming = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Gaming");
                mAdapter_gaming = new Adapter_Social_Distancing_List(getActivity(), mList_gaming);

                mList_bizarre = SocialDistancingListDatabaseClient.getInstance(getActivity()).getSocialDistancingListAppDatabase().SocialDistancingListDao().getAll("Bizarre");
                mAdapter_bizarre = new Adapter_Social_Distancing_List(getActivity(), mList_bizarre);

                mList_news = SocialDistancingNewsDatabaseClient.getInstance(getActivity()).getSocialDistancingNewsAppDatabase().SocialDistancingNewsDao().getAll();
                mAdapter_news = new Adapter_Social_Distancing_News(getActivity(), mList_news);

                return null;
            }
            @Override
            public void onPostExecute(Void a){
               new Handler().post(new Runnable() {
                   @Override
                   public void run() {
                       recyclerView_ent.setAdapter(mAdapter_ent);
                       recyclerView_edu.setAdapter(mAdapter_edu);
                       recyclerView_health.setAdapter(mAdapter_health);
                       recyclerView_gaming.setAdapter(mAdapter_gaming);
                       recyclerView_bizarre.setAdapter(mAdapter_bizarre);
                       recyclerView_virtual.setAdapter(mAdapter_virtual);
                       recyclerView_news.setAdapter(mAdapter_news);
                       if(mySwipeRefreshLayout.isRefreshing()){
                           mySwipeRefreshLayout.setRefreshing(false);
                       }
                   }
               });
            }
        }.execute();
    }

    @Override
    public void onDestroy(){
        if(mySwipeRefreshLayout.isRefreshing()){
            mySwipeRefreshLayout.setRefreshing(false);
        }
        super.onDestroy();
    }
}
