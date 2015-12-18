package pg.eti.biedrzycki.findmyfriends.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLocation {
    private int id;

    private int user_id;

    private double lat;

    private double lng;

    private double alt;

    private double speed;

    @JsonProperty("report_date")
    private String reportDate;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String avatar;

    private String gender;

    @JsonProperty("is_active")
    private int isActive;

    @JsonProperty("refresh_timeout")
    private int refreshTimeout;

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getAlt() {
        return alt;
    }

    public double getSpeed() {
        return speed;
    }

    public String getReportDate() {
        return reportDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGender() {
        return gender;
    }

    public int getIsActive() {
        return isActive;
    }

    public int getRefreshTimeout() {
        return refreshTimeout;
    }
}
