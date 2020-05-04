package com.example.transitaccess.ui;

import android.util.Pair;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ScheduleObj implements Serializable {
    private String[][] schedule;
    private HashMap<String, ArrayList<Integer>> stopIndicesMap;
    private RouteType routeType;

    public ScheduleObj(String[] stopList, String[][] schedule, RouteType routeType) {
        this.schedule = schedule;
        this.routeType = routeType;
        this.stopIndicesMap = new HashMap<String, ArrayList<Integer>>();
        for (int i = 0; i < stopList.length; i++) {
            if (!stopIndicesMap.containsKey(stopList[i])) {
                stopIndicesMap.put(stopList[i], new ArrayList<Integer>());
            }
            stopIndicesMap.get(stopList[i]).add(i);
        }
    }

    public boolean canGoFromAtoB(String a, String b) {
        if (!stopIndicesMap.containsKey(a) || !stopIndicesMap.containsKey(b))
            return false;
        switch (routeType) {
            case LINE:
                return getMinIndex(a) < getMaxIndex(b);
            case LOOP:
                return true;
        }
        return false;
    }

    private int getMinIndex(String a) {
        if (!stopIndicesMap.containsKey(a))
            return -1;
        return stopIndicesMap.get(a).get(0);
    }

    private int getMaxIndex(String a) {
        if (!stopIndicesMap.containsKey(a))
            return -1;
        ArrayList<Integer> indices = stopIndicesMap.get(a);
        return indices.get(indices.size() - 1);
    }

    private Pair<Integer, Integer> getClosestABIndices(String a, String b) {
        if (!canGoFromAtoB(a, b))
            return null;
        ArrayList<Integer> aindices = stopIndicesMap.get(a);
        ArrayList<Integer> bindices = stopIndicesMap.get(b);
        Integer[] ais = new Integer[aindices.size()];
        Integer[] bis = new Integer[bindices.size()];
        ais = aindices.toArray(ais);
        bis = bindices.toArray(bis);

        int champAI = -1;
        int champBI = -1;
        int champDist = Integer.MAX_VALUE;
        int j = 0; // index on ais
        for (int k = 0; k < bis.length; k++) {
            while (j < ais.length && bis[k] > ais[j]) {
                j++;
            }
            if (j > 0) {
                int dif = bis[k] - ais[j - 1];
                if (dif < champDist) {
                    champDist = dif;
                    champAI = ais[j - 1];
                    champBI = bis[k];
                }
            }
        }

        // check if looping around is closest
        if (routeType == RouteType.LOOP) {
            int maxAI = getMaxIndex(a);
            int minBI = getMinIndex(b);
            int distLoopAround = schedule.length - maxAI + minBI;
            if (distLoopAround < champDist) {
                champAI = maxAI;
                champBI = minBI;
            }
        }

        return new Pair<Integer, Integer>(champAI, champBI);

    }

    public ArrayList<ScheduleTimes> getSchedule(String a, String b) {
        ArrayList<ScheduleTimes> arr = new ArrayList<ScheduleTimes>();
        if (!canGoFromAtoB(a, b))
            return arr;
        if (a.equals(b))
            return arr;
        Pair<Integer, Integer> indices = getClosestABIndices(a, b);
        if (indices == null)
            return arr;
        int ai = indices.first;
        int bi = indices.second;
        int aRow = 0;
        int bRow = (ai < bi) ? 0 : 1;
        while (aRow < schedule.length && bRow < schedule.length) {
            if (ai < schedule[aRow].length && bi < schedule[bRow].length) {
                String aStr = schedule[aRow][ai];
                String bStr = schedule[bRow][bi];
                if (!aStr.equals("") && !bStr.equals("")) {
                    arr.add(new ScheduleTimes(stringToDate(aStr), stringToDate(bStr)));
                }
            }
            aRow++;
            bRow++;
        }
        return arr;
    }

    // String format: hh:mm a
    private Date stringToDate(String s) {
        SimpleDateFormat parser = new SimpleDateFormat("hh:mm a", Locale.US);
        try {
            Date d = parser.parse(s);
            return d;
        } catch (ParseException e) {
            return null;
        }
    }
}
