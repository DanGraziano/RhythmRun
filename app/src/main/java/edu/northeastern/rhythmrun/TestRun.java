package edu.northeastern.rhythmrun;

public class TestRun {
    private String cadenceTime;
    private int calories;
    private String date;
    private double distance;
    private double pace;
    private String time;

    public TestRun() {
        // Default constructor required for Firebase
    }

    public String getCadenceTime() {
        return cadenceTime;
    }

    public void setCadenceTime(String cadenceTime) {
        this.cadenceTime = cadenceTime;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getPace() {
        return pace;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public TestRun(String cadenceTime, int calories, String date, double distance, double pace, String time) {
        this.cadenceTime = cadenceTime;
        this.calories = calories;
        this.date = date;
        this.distance = distance;
        this.pace = pace;
        this.time = time;
    }
}