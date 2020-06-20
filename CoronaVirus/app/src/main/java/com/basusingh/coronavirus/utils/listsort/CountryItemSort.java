package com.basusingh.coronavirus.utils.listsort;

import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.database.tracker.TrackerItems;

import java.util.Comparator;

public class CountryItemSort implements Comparator<TrackerItems> {

    @Override
    public int compare(TrackerItems o1, TrackerItems o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}
