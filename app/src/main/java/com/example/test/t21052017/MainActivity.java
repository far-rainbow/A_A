package com.example.test.t21052017;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public final class MainActivity extends Activity {

    static MediaPlayer mPlayer;
    static boolean bGlobalPaused = true;

    static double sRndMix = 0.5;
    static double dXSinCf = 1.0;

    static DrawView MainView;
    static Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainView = new DrawView(this);
        setContentView(MainView);

        bGlobalPaused = false;

        mPlayer = MediaPlayer.create(this, R.raw.title2);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        /*
        Redraw timer loop
         */
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainView.invalidate();
                    }
                });
            }
        }, 0, 15);
    }

    /*
    Play music again if app gone foreground
     */
    @Override
    protected void onResume() {
        super.onResume();
        bGlobalPaused = false;
    }

    /*
    Stop music while app is paused (gone background in system by phone call or user task switching e.t.c)
     */
    @Override
    protected void onPause() {
        super.onPause();
        bGlobalPaused = true;
        mPlayer.pause();
    }

    /*
    Touch the screen to adjust formaulae params... harcoded randomly =)
    sRndMix goes through this values: 0.5,1.0,2.0,4.0,8.0,16.0 and then loops back to 0.5
    dXSinCf goes through this values: 1.0,2.0,3.0,...,7.0,8.0 and then loops back to 2.0
     */
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            sRndMix = sRndMix + sRndMix;
            if (sRndMix > 16.0) sRndMix = 0.5;

            dXSinCf = dXSinCf + 1.0;
            if (dXSinCf > 8.0) dXSinCf = 2.0;

        }
        return true;
    }
}