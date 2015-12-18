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
import android.widget.RadioButton;

import org.springframework.http.HttpEntity;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.User;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;

public class Register extends AppCompatActivity {
    EditText firstNameField;
    EditText lastNameField;
    EditText loginField;
    EditText passwordField;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameField = (EditText) findViewById(R.id.field_first_name);
        lastNameField = (EditText) findViewById(R.id.field_last_name);
        loginField = (EditText) findViewById(R.id.field_login);
        passwordField = (EditText) findViewById(R.id.field_password);
    }

    public void goToLogin(View view) {
        Intent register = new Intent(Register.this, Home.class);
        startActivity(register);
    }

    public void onGenderChange(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.gender_male:
                if (checked) {
                    gender = "M";
                }

                break;
            case R.id.gender_female:
                if (checked) {
                    gender = "F";
                }

                break;
        }
    }

    public void registerAction(View view) {
        new HttpRequestTask().execute(
                firstNameField.getText().toString(),
                lastNameField.getText().toString(),
                gender,
                loginField.getText().toString(),
                passwordField.getText().toString()
        );
    }

    private class HttpRequestTask extends AsyncTask<String, Void, User> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(Register.this, "Wait", "Loading...", true);
        }

        @Override
        protected User doInBackground(final String... params) {
            apiInterceptor.setEndpoint("users");
            apiInterceptor.setHttpMethod("post");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("first_name", params[0]));
            query.add(new Param("last_name", params[1]));
            query.add(new Param("gender", params[2]));
            query.add(new Param("login", params[3]));
            query.add(new Param("password", params[4]));

            HttpEntity result = apiInterceptor.call(query, User.class);

            if (result != null) {
                return (User) result.getBody();
            }

            return null;
        }

        @Override
        protected void onPostExecute(User result) {
            progress.dismiss();

            if (result != null && apiInterceptor.error == null) {
                App appState = ((App) getApplicationContext());
                appState.setSecurityToken(result.getToken());

                Intent map = new Intent(Register.this, Map.class);
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
                    default:
                        title = "Error";
                        message = "Required fields are empty";
                }

                new AlertDialog.Builder(Register.this)
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
