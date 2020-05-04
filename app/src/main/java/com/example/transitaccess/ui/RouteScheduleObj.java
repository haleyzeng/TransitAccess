package com.example.transitaccess.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class RouteScheduleObj implements Serializable {

    private static final String[] dayTypes = {"Weekday", "Saturday", "Sunday", "Holiday"};
    private String routeName;
    private RouteType routeType;
    private HashSet<String> stops;
    private HashMap<String, ArrayList<ScheduleObj>> schedules;

    public RouteScheduleObj(String routeName, RouteType routeType) {
        this.routeName = routeName;
        this.routeType = routeType;
        this.stops = new HashSet<String>();
        schedules = new HashMap<String, ArrayList<ScheduleObj>>();
        for (String t : dayTypes) {
            ArrayList<ScheduleObj> ar = new ArrayList<ScheduleObj>();
            schedules.put(t, ar);
        }
    }

    public String getRouteName() {
        return routeName;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void addSchedule(String dayType, String[] stopList, String[][] schedule, RouteType routeType) {
        this.stops.addAll(Arrays.asList(stopList));
        ScheduleObj so = new ScheduleObj(stopList, schedule, routeType);
        schedules.get(dayType).add(so);
    }

    public HashMap<String, ArrayList<ScheduleObj>> getSchedules() {
        return schedules;
    }

    public String[] getStopsList() {
        String[] ret = new String[this.stops.size()];
        return this.stops.toArray(ret);
    }

    public ArrayList<ScheduleTimes> getSchedulesFor(String dayType, String stopA, String stopB) {
        ArrayList<ScheduleTimes> ret = new ArrayList<ScheduleTimes>();
        ArrayList<ScheduleObj> schedObjs = schedules.get(dayType);
        for (ScheduleObj so : schedObjs) {
            ret.addAll(so.getSchedule(stopA, stopB));
        }
        Collections.sort(ret);
        return ret;
    }
}
