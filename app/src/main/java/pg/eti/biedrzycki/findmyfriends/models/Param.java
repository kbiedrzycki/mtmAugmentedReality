package pg.eti.biedrzycki.findmyfriends.models;

public class Param {
    private String key;
    private String value;

    public Param(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String setKey(String key) {
        this.key = key;

        return this.key;
    }

    public String setValue(String value) {
        this.value = value;

        return this.value;
    }
}
