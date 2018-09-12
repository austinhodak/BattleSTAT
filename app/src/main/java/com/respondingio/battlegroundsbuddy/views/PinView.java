package com.respondingio.battlegroundsbuddy.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.respondingio.battlegroundsbuddy.models.Pin;
import java.util.ArrayList;


public class PinView extends SubsamplingScaleImageView {

    private final Paint paint = new Paint();
    private ArrayList<Pin> sPin = new ArrayList<>();
    private ArrayList<String> pinNames = new ArrayList<>();
    private Bitmap pin;

    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    public boolean setPin(Pin sPin) {
        this.sPin.add(sPin);
        //initialise();
        invalidate();
        return true;
    }

    public boolean removePin(String name){
        if (pinNames.contains(name)){
            sPin.remove(pinNames.indexOf(name));
            pinNames.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> getPinNames(){
        return pinNames;
    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }
        paint.reset();
        paint.setAntiAlias(true);

        for (Pin point : sPin){
            if (point != null) {
                pin = point.getBitmap();
                PointF vPin = sourceToViewCoord(point.getPoints());
                float vX = vPin.x - (pin.getWidth()/2);
                float vY = vPin.y - (pin.getHeight()/2);
                canvas.drawBitmap(pin, vX, vY, paint);
            }
        }
    }

}