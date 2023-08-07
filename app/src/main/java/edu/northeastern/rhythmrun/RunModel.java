package edu.northeastern.rhythmrun;

public class RunModel {
    private String date;
    private String distance;
    private String avgCadence;
    private String avgPace;
    private String time;

    public RunModel() {
        // Default constructor required for Firebase Realtime Database
    }

    public RunModel(String date, String distance, String avgCadence, String avgPace, String time) {
        this.date = date;
        this.distance = distance;
        this.avgCadence = avgCadence;
        this.avgPace = avgPace;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getDistance() {
        return distance;
    }

    public String getAvgCadence() {
        return avgCadence;
    }

    public String getAvgPace() {
        return avgPace;
    }

    public String getTime() {
        return time;
    }
}
