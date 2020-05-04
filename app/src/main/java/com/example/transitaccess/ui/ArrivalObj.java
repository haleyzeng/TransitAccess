package com.example.transitaccess.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ArrivalObj implements Comparable<ArrivalObj> {
    private String routeId;
    private String routeName;
    private StopObj stopObj;
    private ArrayList<Date> arrivals;

    public ArrivalObj(String routeId,
                      String routeName,
                      StopObj stopObj,
                      ArrayList<String> arrivals) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.stopObj = stopObj;
        this.arrivals = new ArrayList<Date>();
        for (String s : arrivals)
            this.arrivals.add(stringToDate(s));
    }

    // String format: yyyy-mm-ddThh:mm:ss-04:00
    private Date stringToDate(String s) {
        int frontOffset = 11;
        int lengthOfTimeStr = 5;
        String time = s.substring(frontOffset, frontOffset + lengthOfTimeStr);

        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        try {
            Date d = parser.parse(time);
            return d;
        } catch (ParseException e) {
            return null;
        }
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public StopObj getStopObj() {
        return stopObj;
    }

    public String getStopId() {
        return stopObj.getStopId();
    }

    public String getStopName() {
        return stopObj.getStopName();
    }

    public String getStopLat() {
        return stopObj.getLat();
    }

    public String getStopLng() {
        return stopObj.getLng();
    }

    public ArrayList<Date> getArrivals() {
        return arrivals;
    }

    public Date getFirstArrival() {
        return arrivals.get(0);
    }

    public String getArrivalsText() {
        SimpleDateFormat twelveHr = new SimpleDateFormat("hh:mm aa", Locale.US);
        StringBuilder sb = new StringBuilder();
        sb.append(routeName);
        sb.append(" arriving at: ");
        sb.append(twelveHr.format(arrivals.get(0)));
        for (int i = 1; i < arrivals.size(); i++) {
            sb.append(", ");
            sb.append(twelveHr.format(arrivals.get(i)));
        }
        return sb.toString();
    }

    public int numArrivals() {
        return arrivals.size();
    }

    @Override
    public int compareTo(ArrivalObj that) {
        int res = this.getFirstArrival().compareTo(that.getFirstArrival());
        int i = 1;
        while (res == 0 && i < this.numArrivals() && i < that.numArrivals()) {
            res = this.getArrivals().get(i).compareTo(that.getArrivals().get(i));
            i++;
        }
        return res;
    }
}
