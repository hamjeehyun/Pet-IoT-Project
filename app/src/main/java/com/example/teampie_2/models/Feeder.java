package com.example.teampie_2.models;

public class Feeder {
    private String hour;
    private String minute;
    public Feeder() {

    }

    public Feeder(String hour, String minute) {
        this.hour = hour;
        this.minute = minute;
    }


    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }
}
