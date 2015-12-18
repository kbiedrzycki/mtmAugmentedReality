package pg.eti.biedrzycki.findmyfriends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.User;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;
import pg.eti.biedrzycki.findmyfriends.utils.ImageManipulator;

public class Settings extends ActionBarActivity {

    private static final int SELECT_PICTURE = 1;
    private Uri outputFileUri;

    ImageView userAvatar;
    App appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appState = ((App) getApplicationContext());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        userAvatar = (ImageView) findViewById(R.id.user_avatar);
        setUserAvatar();
    }

    private void setUserAvatar() {
        User currentUser = appState.getCurrentUser();

        if (currentUser.getAvatar() != null && currentUser.getAvatar().length() > 0) {
            userAvatar.setImageBitmap(ImageManipulator.base64ToBitmap(currentUser.getAvatar()));
        } else {
            userAvatar.setImageResource(R.drawable.avatar_placeholder);
        }
    }

    public void setNewAvatar(View view) throws IOException {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_PICK);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[] { takePhotoIntent }
                );

        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Avatar", Integer.toString(resultCode));

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                try {
                    Bitmap bitmap;

                    if (data.hasExtra("data")) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    }

                    if (bitmap == null) {
                        Toast.makeText(this, "Cannot set avatar", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    String encoded = ImageManipulator.bitmapToBase64(bitmap);

                    new HttpRequestTask().execute(encoded);

                    Log.e("Avatar", encoded);
                } catch (IOException e) {
                    Toast.makeText(this, "Cannot set avatar", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class HttpRequestTask extends AsyncTask<String, Void, User> {
        APIInterceptor apiInterceptor = new APIInterceptor();
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(Settings.this, "Wait", "Loading...", true);
        }

        @Override
        protected User doInBackground(final String... params) {
            apiInterceptor.setEndpoint("users/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("put");

            // query
            ArrayList<Param> query = new ArrayList();
            query.add(new Param("token", appState.getSecurityToken()));

            // body
            apiInterceptor.addToBody("avatar", params[0]);

            // headers
            //apiInterceptor.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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
                appState.setCurrentUser(result);

                if (result.getAvatar() != null) {
                    userAvatar.setImageBitmap(ImageManipulator.base64ToBitmap(result.getAvatar()));
                }
            }

            if (apiInterceptor.error != null) {
                String title;
                String message;

                switch (apiInterceptor.error) {
                    case APIInterceptor.NO_AVATAR:
                        title = "Error";
                        message = "No avatar has been sent";

                        break;
                    case APIInterceptor.NOT_AUTHENTICATED:
                        title = "Error";
                        message = "You are not authenticated!";

                        break;
                    default:
                        title = "Error";
                        message = "Unhandled exception";
                }

                new AlertDialog.Builder(Settings.this)
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
