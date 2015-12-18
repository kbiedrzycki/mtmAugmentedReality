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
import android.widget.EditText;

import org.springframework.http.HttpEntity;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;

public class Invite extends AppCompatActivity {

    App appState;
    EditText friendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        appState = ((App) getApplicationContext());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Friends list");
        }

        friendEmail = (EditText) findViewById(R.id.friend_email);
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

    public void invite(View v) {
        new InviteFriend().execute(friendEmail.getText().toString());
    }

    private class InviteFriend extends AsyncTask<String, Void, Object> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(Invite.this, "Wait", "Loading...", true);
        }

        @Override
        protected Object doInBackground(String ...params) {
            apiInterceptor.setEndpoint("relationships/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("post");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("token", appState.getSecurityToken()));
            query.add(new Param("with", params[0]));

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
                if (result.equals("invited")) {
                    friendEmail.setText("");

                    new AlertDialog.Builder(Invite.this)
                            .setTitle("Success")
                            .setMessage("Person has been invited")
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
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
                    case APIInterceptor.ALREADY_INVITED:
                        title = "Error";
                        message = "You've already invited that person";

                        break;
                    case APIInterceptor.WAITING:
                        title = "Error";
                        message = "Check your pending invitations - that person invited you already";

                        break;
                    case APIInterceptor.DOESNT_EXISTS:
                        title = "Error";
                        message = "User with that email does not exists";

                        break;
                    case APIInterceptor.CANNOT_INVITE_YOURSELF:
                        title = "Error";
                        message = "You cannot invite yourself";

                        break;
                    default:
                        title = "Error";
                        message = "Unhandled exception";
                }

                new AlertDialog.Builder(Invite.this)
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
