package com.example.transitaccess.ui;

import java.io.Serializable;

public class RouteDesc implements Serializable {

    private String routeId;
    private String name;
    private String operatingHours;
    private String[] stops;

    public RouteDesc(String routeId, String name,
                     String operatingHours, String[] stops) {
        this.routeId = routeId;
        this.name = name;
        this.operatingHours = operatingHours;
        this.stops = stops;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public String[] getStops() {
        return stops;
    }

    public void setStops(String[] stops) {
        this.stops = stops;
    }

    public String getDesc() {
        StringBuilder sb = new StringBuilder();

        if (stops.length == 0)
            sb.append("No known stops.");
        else {
            sb.append("Servicing: ");
            // iterate from beginning until 1 less than end
            for (int i = 0; i < stops.length - 1; i++) {
                sb.append(stops[i]);
                sb.append(", ");
            }
            if (stops.length > 1)
                sb.append("and ");
            sb.append(stops[stops.length - 1]);
            sb.append(".");
        }
        return sb.toString();
    }

}
