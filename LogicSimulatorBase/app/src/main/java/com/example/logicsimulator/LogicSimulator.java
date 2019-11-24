package com.example.logicsimulator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class LogicSimulator extends Activity {
    Point size;
    Bitmap blankBitmap;
    ImageView gameView;
    GridAndMenu gridAndMenu;
    TouchProcessor touchProcessor;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getResolution();
        setObjects();
        setContentView(gameView);
        gridAndMenu.updateScreen();
        toast = Toast.makeText(this,
                "Welcome to our App. For a Quick Intro, click 'Change Menu', then Click Intro!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
        gameView.setImageBitmap(blankBitmap);


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
            if(gridAndMenu.introducing){
                mediaPlayer();
            }else
                gridAndMenu.updateScreen();
        }
        return true;
    }


    //Plays Intro Video
    void mediaPlayer(){
        setContentView(R.layout.activity_main);
        final VideoView wview = findViewById(R.id.videoview);
        String videoPath = "android.resource://"+ getPackageName()+ "/" + R.raw.introvid;
        Uri uri = Uri.parse(videoPath);
        wview.setVideoURI(uri);
        wview.start();
        //Disable TouchScreen
        wview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                //write your code after complete video play
                wview.setVisibility(View.GONE);
                setContentView(gameView);
                //Disable introducing
                gridAndMenu.introducing=false;
            }
        });
        //Exit Button
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //write your code after complete video play
                wview.setVisibility(View.GONE);
                setContentView(gameView);
                //Disable introducing
                gridAndMenu.introducing=false;
            }
        });
    }

}
