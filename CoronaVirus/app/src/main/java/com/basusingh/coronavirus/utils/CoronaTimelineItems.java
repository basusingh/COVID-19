package com.basusingh.coronavirus.utils;

import java.io.Serializable;

public class CoronaTimelineItems implements Serializable {
    private String new_daily_cases, new_daily_deaths, total_cases, total_recoveries, total_deaths, date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNew_daily_cases() {
        return new_daily_cases;
    }

    public void setNew_daily_cases(String new_daily_cases) {
        this.new_daily_cases = new_daily_cases;
    }

    public String getNew_daily_deaths() {
        return new_daily_deaths;
    }

    public void setNew_daily_deaths(String new_daily_deaths) {
        this.new_daily_deaths = new_daily_deaths;
    }

    public String getTotal_cases() {
        return total_cases;
    }

    public void setTotal_cases(String total_cases) {
        this.total_cases = total_cases;
    }

    public String getTotal_recoveries() {
        return total_recoveries;
    }

    public void setTotal_recoveries(String total_recoveries) {
        this.total_recoveries = total_recoveries;
    }

    public String getTotal_deaths() {
        return total_deaths;
    }

    public void setTotal_deaths(String total_deaths) {
        this.total_deaths = total_deaths;
    }
}
