package edu.northeastern.rhythmrun;

public class RunModel {
    private String date;
    private String distance;
    private String cadenceTime;
    private String pace;
    private String calories;
    private String time;

    public RunModel() {
        // Default constructor required for Firebase Realtime Database
    }

    public RunModel(String date, String distance, String cadenceTime, String pace, String calories, String time) {
        this.date = date;
        this.distance = distance;
        this.cadenceTime = cadenceTime;
        this.pace = pace;
        this.calories = calories;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getDistance() {
        return distance;
    }

    public String getCadenceTime() {
        return cadenceTime;
    }

    public String getPace() {
        return pace;
    }

    public String getCalories() {
        return calories;
    }

    public String getTime() {
        return time;
    }
}
