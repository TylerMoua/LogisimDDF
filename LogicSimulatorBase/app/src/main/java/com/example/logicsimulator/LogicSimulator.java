package com.example.logicsimulator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

public class LogicSimulator extends Activity {
    Point size;
    Bitmap blankBitmap;
    ImageView gameView;
    GridAndMenu gridAndMenu;
    TouchProcessor touchProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getResolution();
        setObjects();
        setContentView(gameView);

        gameView.setImageBitmap(blankBitmap);
        gridAndMenu.updateScreen();
    }

    //Create our objects
    void setObjects() {
        blankBitmap = Bitmap.createBitmap(size.x, size.y,
                Bitmap.Config.ARGB_8888);
        gameView = new ImageView(this);
        gridAndMenu = new GridAndMenu(this,size.x, blankBitmap);
        touchProcessor = new TouchProcessor(gridAndMenu);
    }
    void getResolution() {
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Has the player removed their finger from the screen?
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            gameView.setImageBitmap(blankBitmap);
            touchProcessor.processTouch(motionEvent);
            gridAndMenu.updateScreen();
        }
        return true;
    }
}
