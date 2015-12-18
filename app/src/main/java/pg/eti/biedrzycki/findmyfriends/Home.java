package pg.eti.biedrzycki.findmyfriends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.User;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;

public class Home extends AppCompatActivity {
    EditText loginField;
    EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loginField = (EditText) findViewById(R.id.field_login);
        passwordField = (EditText) findViewById(R.id.field_password);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void loginAction(View view) {
        new HttpRequestTask().execute(loginField.getText().toString(), passwordField.getText().toString());
    }

    public void goToRegister(View view) {
        Intent register = new Intent(Home.this, Register.class);
        startActivity(register);
    }

    private class HttpRequestTask extends AsyncTask<String, Void, User> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(Home.this, "Wait", "Loading...", true);
        }

        @Override
        protected User doInBackground(final String... params) {
            apiInterceptor.setEndpoint("users");
            apiInterceptor.setHttpMethod("get");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("login", params[0]));
            query.add(new Param("password", params[1]));

            HttpEntity result = apiInterceptor.call(query, User.class);

            if (result != null) {
                return (User)result.getBody();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User result) {
            progress.dismiss();

            if (result != null && apiInterceptor.error == null) {
                App appState = ((App)getApplicationContext());
                appState.setSecurityToken(result.getToken());
                appState.setUserStatus(App.ONLINE_STATUS);
                appState.setCurrentUser(result);

                Intent map = new Intent(Home.this, Map.class);
                startActivity(map);
            }

            if (apiInterceptor.error != null) {
                String title;
                String message;

                switch (apiInterceptor.error) {
                    case APIInterceptor.USER_ALREDY_EXISTS:
                        title = "Error";
                        message = "User with given email already exists";

                        break;
                    case APIInterceptor.REQUIRED_FIELDS_EMPTY:
                        title = "Error";
                        message = "Required fields are empty";

                        break;
                    case APIInterceptor.WRONG_USER_PASSWORD:
                    default:
                        title = "Error";
                        message = "Wrong username or password";
                }

                new AlertDialog.Builder(Home.this)
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
