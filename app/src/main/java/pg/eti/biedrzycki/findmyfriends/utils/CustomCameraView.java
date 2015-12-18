package pg.eti.biedrzycki.findmyfriends.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.R;
import pg.eti.biedrzycki.findmyfriends.models.CameraDrawing;

public class CustomCameraView extends View{

    private ArrayList<CameraDrawing> dane = null;

    public float x;
    public float y;

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCameraView(Context context) {
        super(context);
    }

    public void setDane(ArrayList<CameraDrawing> dane) {
        this.dane = dane;
        j++;
    }

    private int i =0;
    private int j=0;

    private void drawUserBox(CameraDrawing drawing, Canvas canvas) {
        float sx = canvas.getWidth();
        float sy = canvas.getHeight();

        float centerX = -drawing.getX()*sx+sx/2;
        float centerY = drawing.getY()*sy+sy/2;

        if (drawing.getZ() > 0) {
            return;
        }

        Paint p = new Paint();
        p.setARGB(255, 255, 0, 0);

        String firstName = drawing.getFirstName();
        String lastName = drawing.getLastName();
        String userName = firstName + " " + lastName;
        double speed = drawing.getSpeed();
        float distance = drawing.getDistance();

        // body
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        canvas.drawRect(centerX + 10, centerY - 10, centerX + 600, centerY + 300, p);

        // border
        p.setStyle(Style.STROKE);
        p.setColor(Color.BLACK);
        p.setStrokeWidth(2);
        canvas.drawRect(centerX + 10, centerY - 10, centerX + 600, centerY + 300, p);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setTextSize(44f);
        canvas.drawText(userName, centerX + 30, centerY + 40, paint);

        String distanceText = "Distance: " + Math.round(distance) + " meters";
        paint.setTextSize(38f);
        canvas.drawText(distanceText, centerX + 30, centerY + 90, paint);

        String speedText = "Speed: " + (Math.round(speed * 10) / 10) + " m/s";
        paint.setTextSize(38f);
        canvas.drawText(speedText, centerX + 30, centerY + 140, paint);

        String genderText = "Gender:";
        paint.setTextSize(38f);
        canvas.drawText(genderText, centerX + 30, centerY + 190, paint);

        p = new Paint();
        Bitmap b;

        if (drawing.getGender().equals("M")) {
            b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_male);
        } else {
            b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_female);
        }

        p.setColor(Color.RED);
        canvas.drawBitmap(b, centerX + 150, centerY + 190, p);

        b.recycle();

        // draw avatar!

        Bitmap avatar;

        if (drawing.getAvatar() != null) {
            try {
                avatar = ImageManipulator.base64ToBitmap(drawing.getAvatar());
            } catch (Exception e) {
                avatar = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_placeholder);
            }
        } else {
            avatar = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_placeholder);
        }

        p = new Paint();

        p.setColor(Color.RED);

        int calculatedHeight = Math.round(avatar.getHeight() * (100 / (float)avatar.getWidth()));

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(avatar, 100, calculatedHeight, false);

        canvas.drawBitmap(scaledBitmap, centerX + 450, centerY + 160, p);

        avatar.recycle();
        scaledBitmap.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear canvas each draw
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.invalidate();

        Paint p = new Paint();
        p.setARGB(255, 255, 0, 0);

        float sy = canvas.getHeight();
        float sx = canvas.getWidth();
        //canvas.drawCircle(-x*sx+sx/2,y*sy+sy/2,30,p);

        if(dane != null) {
            int daneLength = dane.size();

            for (int i = 0; i < daneLength; i++) {
                drawUserBox(dane.get(i), canvas);
            }
        }
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        final float testTextSize = 48f;

        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        paint.setTextSize(desiredTextSize);
    }

}