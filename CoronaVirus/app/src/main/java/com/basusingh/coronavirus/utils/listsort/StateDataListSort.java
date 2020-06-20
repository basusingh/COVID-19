package com.basusingh.coronavirus.utils.listsort;

import com.basusingh.coronavirus.utils.StateDataList;
import java.util.Comparator;

public class StateDataListSort implements Comparator<StateDataList>
{
    private String type;
    private int order;

    public StateDataListSort(String mType, int mOrder){
        type = mType;
        order = mOrder;
    }


    @Override
    public int compare(StateDataList o1, StateDataList o2) {
        if(order == 0){
            if(type.equalsIgnoreCase("name")){
                return o1.getState().compareTo(o2.getState());
            } else if(type.equalsIgnoreCase("deaths")){
                return Integer.compare(Integer.parseInt(o1.getTotalDeaths()), Integer.parseInt(o2.getTotalDeaths()));
            } else if(type.equalsIgnoreCase("cases")){
                return Integer.compare(Integer.parseInt(o1.getTotalCase()), Integer.parseInt(o2.getTotalCase()));
            } else if(type.equalsIgnoreCase("recovered")){
                return Integer.compare(Integer.parseInt(o1.getTotalRecovered()), Integer.parseInt(o2.getTotalRecovered()));
            } else {
                return o1.getState().compareTo(o2.getState());
            }
        } else {
            if(type.equalsIgnoreCase("name")){
                return o2.getState().compareTo(o1.getState());
            } else if(type.equalsIgnoreCase("deaths")){
                return Integer.compare(Integer.parseInt(o2.getTotalDeaths()), Integer.parseInt(o1.getTotalDeaths()));
            } else if(type.equalsIgnoreCase("cases")){
                return Integer.compare(Integer.parseInt(o2.getTotalCase()), Integer.parseInt(o1.getTotalCase()));
            } else if(type.equalsIgnoreCase("recovered")){
                return Integer.compare(Integer.parseInt(o2.getTotalRecovered()), Integer.parseInt(o1.getTotalRecovered()));
            } else {
                return o2.getState().compareTo(o1.getState());
            }
        }
    }
}