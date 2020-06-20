package com.basusingh.coronavirus.database.tracker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class TrackerItems implements Serializable {

    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "uid")
    String ourid;

    @ColumnInfo(name = "title")
    String title;

    @ColumnInfo(name = "code")
    String code;

    @ColumnInfo(name = "total_cases")
    String total_cases;

    @ColumnInfo(name = "total_recovered")
    String total_recovered;

    @ColumnInfo(name = "total_unresolved")
    String total_unresolved;

    @ColumnInfo(name = "total_deaths")
    String total_deaths;

    @ColumnInfo(name = "total_new_case_today")
    String total_new_case_today;

    @ColumnInfo(name = "total_new_deaths_today")
    String total_new_deaths_today;

    @ColumnInfo(name = "total_active_cases")
    String total_active_cases;

    @ColumnInfo(name = "total_serious_cases")
    String total_serious_cases;

    public String getOurid() {
        return ourid;
    }

    public void setOurid(String ourid) {
        this.ourid = ourid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTotal_cases() {
        return total_cases;
    }

    public void setTotal_cases(String total_cases) {
        this.total_cases = total_cases;
    }

    public String getTotal_recovered() {
        return total_recovered;
    }

    public void setTotal_recovered(String total_recovered) {
        this.total_recovered = total_recovered;
    }

    public String getTotal_unresolved() {
        return total_unresolved;
    }

    public void setTotal_unresolved(String total_unresolved) {
        this.total_unresolved = total_unresolved;
    }

    public String getTotal_deaths() {
        return total_deaths;
    }

    public void setTotal_deaths(String total_deaths) {
        this.total_deaths = total_deaths;
    }

    public String getTotal_new_case_today() {
        return total_new_case_today;
    }

    public void setTotal_new_case_today(String total_new_case_today) {
        this.total_new_case_today = total_new_case_today;
    }

    public String getTotal_new_deaths_today() {
        return total_new_deaths_today;
    }

    public void setTotal_new_deaths_today(String total_new_deaths_today) {
        this.total_new_deaths_today = total_new_deaths_today;
    }

    public String getTotal_active_cases() {
        return total_active_cases;
    }

    public void setTotal_active_cases(String total_active_cases) {
        this.total_active_cases = total_active_cases;
    }

    public String getTotal_serious_cases() {
        return total_serious_cases;
    }

    public void setTotal_serious_cases(String total_serious_cases) {
        this.total_serious_cases = total_serious_cases;
    }
}
