package edu.northeastern.rhythmrun;

public class RunModel {
    private String date;
    private String distance;
    private String avgCadence;
    private String avgPace;
    private String time;
    private String runId;

    public RunModel() {
        // Default constructor required for Firebase Realtime Database
    }

    public RunModel(String date, String distance, String avgCadence, String avgPace, String time) {
        this.date = date;
        this.distance = distance;
        this.avgCadence = avgCadence;
        this.avgPace = avgPace;
        this.time = time;
        this.runId = null;
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

    public String getRunId() {
        return runId;
    }
    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setAvgCadence(String avgCadence) {
        this.avgCadence = avgCadence;
    }

    public void setAvgPace(String avgPace) {
        this.avgPace = avgPace;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
