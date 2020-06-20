package com.basusingh.coronavirus.utils.listsort;

import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;

import java.util.Comparator;

public class StateDataSort implements Comparator<StateDataItems>
{
    private String type;
    private int order;

    public StateDataSort(String mType, int mOrder){
        type = mType;
        order = mOrder;
    }


    @Override
    public int compare(StateDataItems o1, StateDataItems o2) {
        if(order == 0){
            if(type.equalsIgnoreCase("name")){
                return o1.getDistrictName().compareTo(o2.getDistrictName());
            } else if(type.equalsIgnoreCase("active")){
                return Integer.compare(Integer.parseInt(o1.getActive()), Integer.parseInt(o2.getActive()));
            } else if(type.equalsIgnoreCase("confirmed")){
                return Integer.compare(Integer.parseInt(o1.getConfirmed()), Integer.parseInt(o2.getConfirmed()));
            } else if(type.equalsIgnoreCase("deaths")){
                return Integer.compare(Integer.parseInt(o1.getDeceased()), Integer.parseInt(o2.getDeceased()));
            } else if(type.equalsIgnoreCase("recovered")){
                return Integer.compare(Integer.parseInt(o1.getRecovered()), Integer.parseInt(o2.getRecovered()));
            }else {
                return o1.getDistrictName().compareTo(o2.getDistrictName());
            }
        } else {
            if(type.equalsIgnoreCase("name")){
                return o2.getDistrictName().compareTo(o1.getDistrictName());
            } else if(type.equalsIgnoreCase("active")){
                return Integer.compare(Integer.parseInt(o2.getActive()), Integer.parseInt(o1.getActive()));
            } else if(type.equalsIgnoreCase("confirmed")){
                return Integer.compare(Integer.parseInt(o2.getConfirmed()), Integer.parseInt(o1.getConfirmed()));
            } else if(type.equalsIgnoreCase("deaths")){
                return Integer.compare(Integer.parseInt(o2.getDeceased()), Integer.parseInt(o1.getDeceased()));
            } else if(type.equalsIgnoreCase("recovered")){
                return Integer.compare(Integer.parseInt(o2.getRecovered()), Integer.parseInt(o1.getRecovered()));
            } else {
                return o2.getDistrictName().compareTo(o1.getDistrictName());
            }
        }
    }
}