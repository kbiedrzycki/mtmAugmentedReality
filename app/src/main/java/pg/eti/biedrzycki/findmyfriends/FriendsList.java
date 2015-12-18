package pg.eti.biedrzycki.findmyfriends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pg.eti.biedrzycki.findmyfriends.models.Friend;
import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.User;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;
import pg.eti.biedrzycki.findmyfriends.utils.FriendsListAdapter;

public class FriendsList extends ActionBarActivity {

    FriendsListAdapter friendsListAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Friend>> listDataChild;

    App appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        appState = ((App) getApplicationContext());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Friends list");
        }

        expListView = (ExpandableListView) findViewById(R.id.friends_list);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                final Friend friend = (Friend) listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);

                new android.support.v7.app.AlertDialog.Builder(FriendsList.this)
                        .setMessage("Do you want to remove this friend?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new RemoveFriend().execute(friend.getId());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // call api request to fetch friends list
        new LoadFriendsList().execute();
    }

    public void toFriends(View v) {
        Intent friendsList = new Intent(this, FriendsList.class);
        startActivity(friendsList);
    }

    public void toPending(View v) {
        Intent pendingList = new Intent(this, PendingInvitations.class);
        startActivity(pendingList);
    }

    public void toInvite(View v) {
        Intent invite = new Intent(this, Invite.class);
        startActivity(invite);
    }

    private class LoadFriendsList extends AsyncTask<Void, Void, Friend[]> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(FriendsList.this, "Wait", "Loading...", true);
        }

        @Override
        protected Friend[] doInBackground(Void ...params) {
            apiInterceptor.setEndpoint("relationships/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("get");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("token", appState.getSecurityToken()));

            HttpEntity<Friend[]> result = apiInterceptor.call(query, Friend[].class);

            if (result != null) {
                return result.getBody();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Friend[] result) {
            progress.dismiss();

            if (result != null && apiInterceptor.error == null) {
                listDataHeader = new ArrayList<String>();
                listDataChild = new HashMap<String, List<Friend>>();

                listDataHeader.add("Accepted friends");
                listDataHeader.add("Pending acceptance");

                List<Friend> accepted = new ArrayList<Friend>();
                List<Friend> pending = new ArrayList<Friend>();

                if (result.length > 0) {
                    int friendsCount = result.length;

                    for (int i = 0; i < friendsCount; i++) {
                        if (result[i].getAccepted() == 1) {
                            accepted.add(result[i]);
                        } else {
                            pending.add(result[i]);
                        }
                    }
                }

                listDataChild.put(listDataHeader.get(0), accepted);
                listDataChild.put(listDataHeader.get(1), pending);

                friendsListAdapter = new FriendsListAdapter(FriendsList.this, listDataHeader, listDataChild);
                expListView.setAdapter(friendsListAdapter);
            }

            if (apiInterceptor.error != null) {
                String title;
                String message;

                switch (apiInterceptor.error) {
                    case APIInterceptor.NOT_AUTHENTICATED:
                        title = "Error";
                        message = "You are not authenticated!";

                        break;
                    default:
                        title = "Error";
                        message = "Unhandled exception";
                }

                new AlertDialog.Builder(FriendsList.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        }
    }

    private class RemoveFriend extends AsyncTask<Integer, Void, Object> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(FriendsList.this, "Wait", "Loading...", true);
        }

        @Override
        protected Object doInBackground(Integer ...params) {
            apiInterceptor.setEndpoint("relationships/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("delete");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("token", appState.getSecurityToken()));
            query.add(new Param("friend_id", Integer.toString(params[0])));

            HttpEntity result = apiInterceptor.call(query, String.class);

            if (result != null) {
                return result.getBody();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            progress.dismiss();

            if (result != null && apiInterceptor.error == null) {
                if (result.equals("removed")) {
                    new LoadFriendsList().execute();
                }
            }

            if (apiInterceptor.error != null) {
                String title;
                String message;

                switch (apiInterceptor.error) {
                    case APIInterceptor.NOT_AUTHENTICATED:
                        title = "Error";
                        message = "You are not authenticated!";

                        break;
                    case APIInterceptor.NOT_A_FRIENDS:
                        title = "Error";
                        message = "It's not your friend";

                        break;
                    default:
                        title = "Error";
                        message = "Unhandled exception";
                }

                new AlertDialog.Builder(FriendsList.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        }
    }
}
