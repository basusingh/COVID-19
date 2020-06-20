package com.basusingh.coronavirus.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.utils.DetectConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class news extends Fragment {

    public news() {
        // Required empty public constructor
    }

    private WebView mWebView;
    SwipeRefreshLayout mySwipeRefreshLayout;
    private LinearLayout error_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(View v ,Bundle s){
        mySwipeRefreshLayout = v.findViewById(R.id.swipeContainer);
        error_layout = v.findViewById(R.id.error_layout);
        mWebView = v.findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mySwipeRefreshLayout.setRefreshing(true);
        WebSettings settings = mWebView.getSettings();
        settings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                mySwipeRefreshLayout.setRefreshing(false);
            }
        });


        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        checkInternetAndLoad();
                    }
                }
        );
        checkInternetAndLoad();
    }

    private void checkInternetAndLoad(){
        if(!DetectConnection.checkInternetConnection(getContext())){
            mWebView.setVisibility(View.GONE);
            error_layout.setVisibility(View.VISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
        } else {
            mWebView.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            if(!mySwipeRefreshLayout.isRefreshing()){
                mySwipeRefreshLayout.setRefreshing(true);
            }
            mWebView.loadUrl("https://www.who.int/emergencies/diseases/novel-coronavirus-2019/media-resources/news");
        }
    }


}
