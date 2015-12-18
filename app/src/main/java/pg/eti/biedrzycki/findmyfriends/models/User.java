package pg.eti.biedrzycki.findmyfriends.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;

    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String gender;

    private String avatar;

    @JsonProperty("is_active")
    private int isActive;

    @JsonProperty("refresh_timeout")
    private int refreshTimeout;

    private String token;

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public int getId() {
        return this.id;
    }

    public String getGender() {
        return this.gender;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public int getIsActive() {
        return this.isActive;
    }

    public int getRefreshTimeout() {
        return this.refreshTimeout;
    }

    public String getToken() {
        return this.token;
    }
}
