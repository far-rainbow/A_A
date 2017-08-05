package com.example.test.t21052017;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.BitmapFactory.decodeResource;

public class MainActivity extends Activity {

    DrawView MainView = null;

    MediaPlayer mPlayer;

    boolean globalPaused = true;
    boolean firstRun = true;
    boolean splashOn = false;
    boolean calcOn = false;

    private int textJump = 0;
    private int pBar = 0;

    private int frameNum = 0;

    static final double piVal = 3.14159;
    static final double piValHalf = 3.14159 * 0.5;

    static double rndMix = 0.25;

    private Bitmap[] bmHrt;

    static final String appTitle = "-=A&A=-";
    static final String appSplash = "NeoCortexLab (L) 2017";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainView = new DrawView(this);
        setContentView(MainView);

        Timer timer;

        globalPaused = false;

        mPlayer = MediaPlayer.create(this, R.raw.title);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @Override
    protected void onResume() {
        super.onResume();
        globalPaused = false;

        System.out.println("TEST: RESUME...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        globalPaused = true;
        mPlayer.pause();
        System.out.println("TEST: PAUSE...");
    }

    class DrawView extends View {

        public DrawView(Context context) {
            super(context);

        }

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private Bitmap bmBG;
        private Bitmap bmSplashB;
        private Bitmap bmTitleB;
        private Bitmap bmTitleW;

        @Override
        protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);

            final int canvasWidth = canvas.getWidth();
            final int canvasHeight = canvas.getHeight();
            final int canvasWidthHalf = canvasWidth / 2;
            final int canvasHeightHalf = canvasHeight / 2;
            final int canvasHeightHalfAA = canvasHeight / 32;

            final int resizePixelSize = canvasWidth / 333;

            int bgWidth;
            int bgHeight;

            /*
            Background calculations at the splash screen stage. Array calculation moved out in separate thread to free main contex runtime and screen refresh invalidation (progress bar and e.t.c)
             */
            if (splashOn) {
                splashOn = false;

                /*
                Setup main screen BG in respect of viewer screen size
                */
                double bgAspectRatio = (double) canvasHeight / (double) canvasWidth;
                double bgAspectRatioImg = 1280.0 / 960.0;
                bgWidth = canvasWidth;
                bgHeight = canvasHeight;
                if (bgAspectRatio < bgAspectRatioImg) {
                    bgHeight = (int) (1280.0 * (double) canvasWidth / 960.0);
                }
                if (bgAspectRatio > bgAspectRatioImg) {
                    bgWidth = (int) (960.0 * ((double) canvasHeight / 1280.0));
                }
                bmBG = getResizedBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.aa), bgWidth, bgHeight);

                /*
                Setup thread for calculations of bitmap array of sprites: from smallest one to bigger one. This is not so fast operation.
                 */
                Thread Splash = new Thread(new Runnable() {
                    public void run() {
                        bmHrt = new Bitmap[79];
                        Context Temp = getApplicationContext();
                        for (int k = 0; k < 79; k++) {
                            bmHrt[k] = getResizedBitmap(decodeResource(Temp.getResources(), R.drawable.h1), 1 + k * resizePixelSize, 1 + k * resizePixelSize);
                            pBar = k;
                        }
                        calcOn = true;
                        frameNum = 0;
                    }
                });
                Splash.start();
            }

            /*
            First run: setup splash and title text bitmaps, draw splash text bitmap, change switches
             */
            if (firstRun) {
                bmSplashB = textAsBitmap(appSplash, 32, Color.BLACK);
                bmTitleB = textAsBitmap(appTitle, 128, Color.BLACK);
                bmTitleW = textAsBitmap(appTitle, 124, Color.WHITE);
                textJump = bmSplashB.getWidth() / 2;
                canvas.drawBitmap(bmSplashB, canvasWidthHalf - textJump, canvasHeightHalf, mPaint);
                firstRun = false;
                splashOn = true;
            }

            /*
            PROGRESS BAR. textJump for bmSplashB was inited previously in firstRun block
             */
            if (!firstRun && !splashOn && !calcOn) {
                canvas.drawBitmap(bmSplashB, canvasWidthHalf - textJump, canvasHeightHalf, mPaint);
                canvas.drawRect(2, 2, (float) pBar * canvasWidth / 79, canvasHeight / 50, mPaint);
                mPaint.setTextSize(canvasHeight / 50);
                canvas.drawText(String.valueOf((pBar * 100 / 79)) + "%", 0, canvasHeight / 25, mPaint);
            }

            /*
            MAIN DRAW ANIMATION: BG, TEXT, SPRITES. textJump must be reinited for each appTitle bitmap text sprites (it has splash text sprite value at this point of runtime)
             */
            if (!firstRun && !splashOn && calcOn) {

                canvas.drawBitmap(bmBG, 0, 0, mPaint);

                textJump = bmTitleB.getWidth() / 2;
                canvas.drawBitmap(bmTitleB, canvasWidthHalf - textJump, canvasHeightHalfAA - 2, mPaint);
                textJump = bmTitleW.getWidth() / 2;
                canvas.drawBitmap(bmTitleW, canvasWidthHalf - textJump, canvasHeightHalfAA, mPaint);

                /*
                Half the last biggest sprite to make sprite pivot a little left from the center of canvas
                 */
                int scrBoundHelper = bmHrt[78].getWidth() / 2;
                int xH1 = canvasWidthHalf - scrBoundHelper;
                int yH1 = canvasHeightHalf - scrBoundHelper;
                int xyMid = (int) ((xH1 + yH1) / piVal);
                for (int i = 0; i <= 78; i++) {
                    double j = (double) i / (piVal * rndMix) + frameNum * 0.005; // (/200)
                    int xH1step = (int) (Math.cos(j) * Math.sin(j * 2.0) * (xyMid * Math.sin((double) frameNum / 220)) * piValHalf);
                    int yH1step = (int) (Math.sin(j) * Math.cos(j * 0.5) * (xyMid * Math.sin((double) frameNum / 180)) * piValHalf);
                    canvas.drawBitmap(bmHrt[i], xH1 + xH1step, yH1 + yH1step, mPaint);
                }
            }
            /*
            Frame count +1
             */
            frameNum++;

            /*
            If music is stoped and this is not system pause -- play again! =)
             */
            if (!mPlayer.isPlaying() && !globalPaused) mPlayer.start();
        }
    }

    /*
    Touch the screen to adjust formaulae params... harcoded randomly =)
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            rndMix = rndMix + rndMix;
            if (rndMix > 8.0) rndMix = 0.5;
        }
        return true;
    }

    /*
    Snipet from Stack Overflow: bitmap resize
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }

    /*
    Snipet from Stack Overflow: text to bitmap with size and color
    */
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
}