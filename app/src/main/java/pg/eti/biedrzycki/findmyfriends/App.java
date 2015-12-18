package pg.eti.biedrzycki.findmyfriends;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import pg.eti.biedrzycki.findmyfriends.models.User;
import pg.eti.biedrzycki.findmyfriends.utils.GPSService;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    public static final int ONLINE_STATUS = 1;
    public static final int OFFLINE_STATUS = 0;

    private User currentUser;
    private String securityToken;
    private int userStatus = this.OFFLINE_STATUS;

    public String getSecurityToken() {
        return this.securityToken;
    }

    public String setSecurityToken(String token) {
        this.securityToken = token;

        return this.securityToken;
    }

    public int getUserStatus() {
        return this.userStatus;
    }

    public int setUserStatus(int status) {
        this.userStatus = status;

        if (status == this.OFFLINE_STATUS) {
            stopService(new Intent(App.this,
                    GPSService.class));
        } else {
            startService(new Intent(App.this,
                    GPSService.class));
        }

        return this.userStatus;
    }



    public User getCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Toast.makeText(this, "Activity stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Toast.makeText(this, "Activity destroyed", Toast.LENGTH_SHORT).show();

        this.setSecurityToken(null);
        this.setCurrentUser(null);
        this.setUserStatus(this.OFFLINE_STATUS);
    }
}
