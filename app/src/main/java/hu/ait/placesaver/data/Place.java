package hu.ait.placesaver.data;

import android.location.Location;

import java.util.Comparator;
import java.util.Date;

import hu.ait.placesaver.R;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Place extends RealmObject {

    @PrimaryKey
    private String placeID;

    private String locTitle;
    private String locDate;
    private String locTime;
    private String locDescription;
    private double lat, lng;
    private Date pickUpDate;

    public Place() {

    }

    public Place(String locTitle, String locDate, String locTime,
                 String locDescription, Date pickUpDate) {
        this.locTitle = locTitle;
        this.locDate = locDate;
        this.locTime = locTime;
        this.locDescription = locDescription;
        this.pickUpDate = pickUpDate;

    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getLocTitle() {
        return locTitle;
    }

    public void setLocTitle(String locTitle) {
        this.locTitle = locTitle;
    }

    public String getLocDate() {
        return locDate;
    }

    public void setLocDate(String locDate) {
        this.locDate = locDate;
    }

    public String getLocTime() {
        return locTime;
    }

    public void setLocTime(String locTime) {
        this.locTime = locTime;
    }

    public String getLocDescription() {
        return locDescription;
    }

    public void setLocDescription(String locDescription) {
        this.locDescription = locDescription;
    }

    public Date getPickUpDate() {
        return pickUpDate;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLng() {
        return lng;
    }

    public void setPickUpDate(Date pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public String getPlaceID() {
        return placeID;
    }

    private int getLocYear() {
        String date = locDate;
        String[] dates = date.split("/");
        int year = Integer.parseInt(dates[2]);
        return year;
    }

    private int getLocMonth() {
        String date = locDate;
        String [] dates = date.split("/");
        int month = Integer.parseInt(dates[1]);
        return month;
    }

    private int getLocDay() {
        String date = locDate;
        String [] dates = date.split("/");
        int day = Integer.parseInt(dates[0]);
        return day;
    }

    private int getLocHour() {
        String time = locTime;
        String[] times = time.split(":");
        int hour = Integer.parseInt(times[0]);
        return hour;
    }

    private int getLocMin() {
        String time = locTime;
        String[] times = time.split(":");
        int min = Integer.parseInt(times[1]);
        return min;
    }

    public static  class NameCompare implements Comparator<Place> {
        public int compare(Place p1, Place p2){
            return p1.getLocTitle().compareTo(p2.getLocTitle());
        }
    }

    public static class DateCompare implements Comparator<Place> {
        public int compare(Place p1, Place p2) {
            int diff = p1.getLocYear() - p2.getLocYear();

            if (diff == 0) {
                diff = p1.getLocMonth() - p2.getLocMonth();
            } else {
                return diff;
            }

            if (diff == 0) {
                diff = p1.getLocDay() - p2.getLocDay();
            } else {
                return diff;
            }

            if (diff == 0) {
                diff = p1.getLocHour() - p2.getLocHour();
            } else {
                return diff;
            }

            if (diff == 0) {
                diff = p1.getLocMin() - p2.getLocMin();
            }

            return diff;

        }
    }

}

