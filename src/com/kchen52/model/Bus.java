package com.kchen52.model;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Bus {
    private int vehicleNumber;
    private int tripId;
    private String routeNumber;
    private String direction;
    private String destination;
    private String pattern;
    private double longitude;
    private double latitude;
    private String recordedTime;

    public Bus() {
        vehicleNumber = -1;
        tripId = -1;
        routeNumber = "-1";
        direction = "weast";
        destination = "hell";
        pattern = "null";
        longitude = 0.0;
        latitude = 0.0;
        recordedTime = "0000";
    }

    public void init(String initInput) {
        // Takes translink's input, parse it, and set values accordingly
        // Reference: initInput comes in the following format
        // <Bus><VehicleNo>1234</VehicleNo><TripId>1234</TripId><RouteNo>1234</RouteNo><Direction>EAST</Direction>
        // <Destination>SomePlace</Destination><Pattern>EB1</Pattern><Latitude>49.188817</Latitude><Longitude>
        // 122.848500</Longitude><RecordedTime><02:40:48 pm</RecordedTime><RouteMap><Href>http://....</Href></RouteMap>
        // </Bus>
        vehicleNumber = Integer.parseInt(setValue("VehicleNo", initInput));
        tripId = Integer.parseInt(setValue("TripId", initInput));
        // Route number can also include numbers, such as C28, N9, etc.
        routeNumber = setValue("RouteNo", initInput);
        direction = setValue("Direction", initInput);
        destination = setValue("Destination", initInput);
        pattern = setValue("Pattern", initInput);
        latitude = Double.parseDouble(setValue("Latitude", initInput));
        longitude = Double.parseDouble(setValue("Longitude", initInput));
        recordedTime = setValue("RecordedTime", initInput);
    }

    // Performs a regex search for the value specified, and returns the value(s) in those tags
    private String setValue(String valueToSet, String input) {
        Pattern pattern = Pattern.compile("<" + valueToSet + ">(.+?)</" + valueToSet + ">");
        Matcher matcher = pattern.matcher(input);
        matcher.find();
        return(matcher.group(1));
    }

    // A bunch of getters
    public int getVehicleNumber() {
        return vehicleNumber;
    }
    public int getTripId() {
        return tripId;
    }
    public String getRouteNumber() {
        return routeNumber;
    }
    public String getDirection() {
        return direction;
    }
    public String getDestination() {
        return destination;
    }
    public String getPattern() {
        return pattern;
    }
    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public String getRecordedTime() {
        return recordedTime;
    }
}
