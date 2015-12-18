package pg.eti.biedrzycki.findmyfriends;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ActionBarActivity extends AppCompatActivity {
    Menu menu;
    MenuItem actionStatus;
    MenuItem friends;

    App appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appState = ((App)getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.map_menu, menu);

        actionStatus = this.menu.findItem(R.id.action_status);
        friends = this.menu.findItem(R.id.friends);

        if (appState.getUserStatus() == App.ONLINE_STATUS) {
            actionStatus.setIcon(R.drawable.user_visible);
        } else {
            actionStatus.setIcon(R.drawable.user_not_visible);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                Intent settings = new Intent(this, Settings.class);
                startActivity(settings);

                return true;

            case R.id.action_camera:
                Intent camera = new Intent(this, CameraPreview.class);
                startActivity(camera);

                return true;

            case R.id.action_map:
                Intent map = new Intent(this, Map.class);
                startActivity(map);

                return true;

            case R.id.action_logout:

                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent home = new Intent(ActionBarActivity.this, Home.class);
                                appState.setUserStatus(App.OFFLINE_STATUS);
                                appState.setCurrentUser(null);
                                appState.setSecurityToken(null);
                                startActivity(home);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;

            case R.id.action_status:
                if (appState.getUserStatus() == App.ONLINE_STATUS) {
                    actionStatus.setIcon(R.drawable.user_not_visible);
                    appState.setUserStatus(App.OFFLINE_STATUS);
                } else {
                    actionStatus.setIcon(R.drawable.user_visible);
                    appState.setUserStatus(App.ONLINE_STATUS);
                }

                return true;
            case R.id.friends:

                Intent friendsList = new Intent(this, FriendsList.class);
                startActivity(friendsList);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
