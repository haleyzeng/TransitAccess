package com.example.transitaccess.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScheduleTimes implements Comparable, Serializable {
    private Date departTime;
    private Date arriveTime;

    public ScheduleTimes(Date departTime, Date arriveTime) {
        this.departTime = departTime;
        this.arriveTime = arriveTime;
    }

    public String getDepartTime() {
        SimpleDateFormat twelveHr = new SimpleDateFormat("hh:mm a", Locale.US);
        return twelveHr.format(departTime);
    }

    public String getArriveTime() {
        SimpleDateFormat twelveHr = new SimpleDateFormat("hh:mm a", Locale.US);

        return twelveHr.format(arriveTime);
    }


    public int compareTo(Object that) {
        if (that instanceof ScheduleTimes) {
            return this.departTime.compareTo(((ScheduleTimes)that).departTime);
        } else {
            return 0;
        }
    }
}
