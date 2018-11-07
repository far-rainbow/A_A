package su.kamenka.t21052017;

//import com.google.firebase.perf.FirebasePerformance;
//import com.google.firebase.perf.metrics.Trace;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

//import com.google.firebase.analytics.FirebaseAnalytics;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import com.crashlytics.android.Crashlytics;

public final class MainActivity extends Activity {


    static MediaPlayer mPlayer;
    static boolean bGlobalPaused = true;

    static boolean touched, t1, t2 = false;
    static double sRndMix = 0.5;
    static double dXSinCf = 1.0;
    double sRndMix_next;
    double sRndMix_threshold;
    double dXSinCf_next;
    double dXSinCf_threshold;
    Bundle bundle = new Bundle();

    static int OpenGLVersion;

    private double squizze_threshold;
    static double squizze = 0.0;

    //private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

/*        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "touch");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "screen");*/

        final DrawView MainView = new DrawView(this);
        setContentView(MainView);

        bGlobalPaused = false;

        mPlayer = MediaPlayer.create(this, R.raw.title2);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        OpenGLVersion = detectOpenGL();

        /*
        Redraw timer loop
         */
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (touched) {

                            if (sRndMix < sRndMix_next && !t1) {
                                sRndMix = sRndMix + sRndMix_threshold;
                            } else {
                                sRndMix = sRndMix_next;
                                t1 = true;
                                //System.out.println("SRND = " +sRndMix);
                            }

                            if (dXSinCf < dXSinCf_next && !t2) {
                                dXSinCf = dXSinCf + dXSinCf_threshold;

                                if (squizze<1.0 && dXSinCf > 4.0) squizze = squizze + squizze_threshold;

                                //System.out.println("DXSIN_ADDED =" +dXSinCf);
                            } else {
                                dXSinCf = dXSinCf_next;
                                t2 = true;
                                //System.out.println("DXSIN =" +dXSinCf);
                            }

                            if (t1 && t2) {
                                touched = false;
                            }

                        }

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

        //Trace myTrace = FirebasePerformance.getInstance().newTrace("test_trace");
        //myTrace.start();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (!touched) {

                touched = true;

                if (sRndMix > 8.0) {
                    sRndMix = 0.5;
                }
                if (dXSinCf > 8.0) {
                    dXSinCf = 2.0;
                    squizze = 0.0;
                }

                t1 = false;
                t2 = false;

                sRndMix_next = sRndMix + sRndMix;
                sRndMix_threshold = sRndMix / 400;
                dXSinCf_next = dXSinCf + 1.0;
                dXSinCf_threshold = 1.0 / 400;

                squizze_threshold = 1.0/400;

            }

            //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            //myTrace.incrementCounter("screen_hit");

        }

        /* FireBase Crashalytics symulated crash event */
        //Crashlytics.getInstance().crash();
        //myTrace.stop();

        return true;
    }

    private int detectOpenGL() {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion;
    }

}