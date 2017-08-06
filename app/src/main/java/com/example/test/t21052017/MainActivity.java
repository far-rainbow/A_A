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

    boolean bGlobalPaused = true;
    boolean bFirstRun = true;
    boolean bSplashOn = false;
    boolean bCalcDone = false;

    private int iTextWidthHalfHelper = 0;
    private int iProgressBar = 0;

    private int iFrameNum = 0;

    static final double PI_VAL = 3.14159;
    static final double PI_VAL_HALF = 3.14159 / 2;

    static double sRndMix = 0.5;

    private Bitmap[] bmSpriteArray;

    static final String TITLE_TEXT = "-=A&A=-";
    static final String SPLASH_TEXT = "NeoCortexLab (L) 2017";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainView = new DrawView(this);
        setContentView(MainView);

        bGlobalPaused = false;

        mPlayer = MediaPlayer.create(this, R.raw.title);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        Force redraw. Most devices done calculations and draw preparations faster than 15 ms. If no than frame shall be droped...
         */
        Timer timer = new Timer();
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
    Main activity view
     */
    class DrawView extends View {

        public DrawView(Context context) {
            super(context);
        }

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private Bitmap bmBG;
        private Bitmap bmSplashB;
        private Bitmap bmTitleB;
        private Bitmap bmTitleW;

        final double BG_HEIGHT = (double) BitmapFactory.decodeResource(this.getResources(), R.drawable.aa).getHeight() / 2;
        final double BG_WIDTH = (double) BitmapFactory.decodeResource(this.getResources(), R.drawable.aa).getWidth() / 2;
        final double BG_ASPECT_RATIO_IMG = BG_HEIGHT / BG_WIDTH;

        @Override
        protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);

            final int CANVAS_WIDTH = canvas.getWidth();
            final int CANVAS_HEIGHT = canvas.getHeight();
            final int CANVAS_WIDTH_HALF = CANVAS_WIDTH / 2;
            final int CANVAS_HEIGHT_HALF = CANVAS_HEIGHT / 2;
            final int CANVAS_HEIGHT_HALF_AA = CANVAS_HEIGHT / 8;
            final int RESIZE_PIXEL_SIZE = CANVAS_WIDTH / 333;

            /*
            I was born in 1979 so 79 sprites are well enough to show love on =)
            ~13 Mb mem allocation at all with current constants and other stuff... so no need to bother
             */
            final int BOBS_NUM = 79;

            /*
            If aspect ratio of BG is equal to canvas aspect ratio then let it be equal. If not then it shall be changed in if {} blocks respectively
            */
            int iBgWidth = CANVAS_WIDTH;
            int iBgHeight = CANVAS_HEIGHT;

            /*
            Background calculations at the splash screen stage. Array calculation moved out in separate thread to free main contex runtime and screen refresh invalidation (progress bar and e.t.c)
             */
            if (bSplashOn) {
                bSplashOn = false;

                /*
                Setup main screen BG in respect of viewer screen size
                */
                double dBgAspectRatio = (double) CANVAS_HEIGHT / (double) CANVAS_WIDTH;

                if (dBgAspectRatio < BG_ASPECT_RATIO_IMG) {
                    iBgHeight = (int) (BG_HEIGHT * (CANVAS_WIDTH / BG_WIDTH));
                }
                if (dBgAspectRatio > BG_ASPECT_RATIO_IMG) {
                    iBgWidth = (int) (BG_WIDTH * (CANVAS_HEIGHT / BG_HEIGHT));
                }
                bmBG = getResizedBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.aa), iBgWidth, iBgHeight);

                /*
                Setup thread for calculations of bitmap array of sprites: from smallest one to bigger one. This is not so fast operation.
                 */
                Thread ThreadSplash = new Thread(new Runnable() {
                    public void run() {
                        bmSpriteArray = new Bitmap[BOBS_NUM];
                        Context Temp = getApplicationContext();
                        for (int k = 0; k < BOBS_NUM; k++) {
                            bmSpriteArray[k] = getResizedBitmap(decodeResource(Temp.getResources(), R.drawable.h1), 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE);
                            iProgressBar = k;
                        }
                        bCalcDone = true;
                        iFrameNum = 0;
                    }
                });
                ThreadSplash.start();
            }

            /*
            First run: setup splash and title text bitmaps, draw splash text bitmap, change switches
             */
            if (bFirstRun) {
                bmSplashB = textAsBitmap(SPLASH_TEXT, 32, Color.BLACK);
                bmTitleB = textAsBitmap(TITLE_TEXT, 128, Color.BLACK);
                bmTitleW = textAsBitmap(TITLE_TEXT, 124, Color.WHITE);
                iTextWidthHalfHelper = bmSplashB.getWidth() / 2;
                canvas.drawBitmap(bmSplashB, CANVAS_WIDTH_HALF - iTextWidthHalfHelper, CANVAS_HEIGHT_HALF, mPaint);
                bFirstRun = false;
                bSplashOn = true;
            }

            /*
            PROGRESS BAR. iTextWidthHalfHelper for bmSplashB was inited previously in bFirstRun block
             */
            if (!bFirstRun && !bSplashOn && !bCalcDone) {
                canvas.drawBitmap(bmSplashB, CANVAS_WIDTH_HALF - iTextWidthHalfHelper, CANVAS_HEIGHT_HALF, mPaint);
                canvas.drawRect(2, 2, (float) iProgressBar * CANVAS_WIDTH / BOBS_NUM, CANVAS_HEIGHT / 50, mPaint);
                mPaint.setTextSize(CANVAS_HEIGHT / 50);
                canvas.drawText(String.valueOf((iProgressBar * 100 / BOBS_NUM)) + "%", 0, CANVAS_HEIGHT / 25, mPaint);
            }

            /*
            MAIN DRAW ANIMATION: BG, TEXT, SPRITES. iTextWidthHalfHelper must be reinited for each TITLE_TEXT bitmap text sprites (it has splash text sprite value at this point of runtime)
             */
            if (!bFirstRun && !bSplashOn && bCalcDone) {

                canvas.drawBitmap(bmBG, 0, 0, mPaint);

                /*
                Half the last biggest sprite to make sprite pivot a little left from the center of canvas
                and draw array sprites one by one with position change calculated by formulae
                 */
                int iXHelper = CANVAS_WIDTH_HALF - bmSpriteArray[BOBS_NUM - 1].getWidth() / 2;
                int iYHelper = CANVAS_HEIGHT_HALF - bmSpriteArray[BOBS_NUM - 1].getWidth() / 2;
                int iSpriteFlowHelper = (int) ((iXHelper + iYHelper) / PI_VAL);
                for (int i = 0; i < BOBS_NUM; i++) {
                    double j = (double) i / (PI_VAL * sRndMix) + iFrameNum * 0.005; // (/200)
                    int iXSpritePosition = (int) (Math.cos(j) * Math.sin(j * 2.0) * (iSpriteFlowHelper * Math.sin((double) iFrameNum / 220)) * PI_VAL_HALF);
                    int iYSpritePosition = (int) (Math.sin(j) * Math.cos(j * 0.5) * (iSpriteFlowHelper * Math.sin((double) iFrameNum / 180)) * PI_VAL_HALF);
                    canvas.drawBitmap(bmSpriteArray[i], iXHelper + iXSpritePosition, iYHelper + iYSpritePosition, mPaint);
                }

                /*
                Draw title text over BG and sprites
                 */
                canvas.drawBitmap(bmTitleB, CANVAS_WIDTH_HALF - bmTitleB.getWidth() / 2, CANVAS_HEIGHT_HALF_AA - 2, mPaint);
                canvas.drawBitmap(bmTitleW, CANVAS_WIDTH_HALF - bmTitleW.getWidth() / 2, CANVAS_HEIGHT_HALF_AA, mPaint);

            }
            /*
            Frame count +1
             */
            iFrameNum++;

            /*
            If music is stoped and this is not system pause -- play again! =)
             */
            if (!mPlayer.isPlaying() && !bGlobalPaused) mPlayer.start();
        }
    }

    /*
    Touch the screen to adjust formaulae params... harcoded randomly =)
    The fact is that sRndMix goes through this values: 0.5,1.0,2.0,4.0,8.0,16.0 and then loop back to 0.5
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sRndMix = sRndMix + sRndMix;
            if (sRndMix > 16.0) sRndMix = 0.5;
        }
        return true;
    }

    /*
    Snipet from Stack Overflow: bitmap resize
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int iNewWidth, int iNewHeight) {
        int iWidth = bm.getWidth();
        int iHeight = bm.getHeight();
        float fScaleWidth = ((float) iNewWidth) / iWidth;
        float fScaleHeight = ((float) iNewHeight) / iHeight;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(fScaleWidth, fScaleHeight);
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bm, 0, 0, iWidth, iHeight, matrix, false);
    }

    /*
    Snipet from Stack Overflow: text to bitmap with size and color
    */
    public static Bitmap textAsBitmap(String StringText, float fTextSize, int iTextColor) {
        Paint paint = new Paint();
        paint.setTextSize(fTextSize);
        paint.setColor(iTextColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float fBaseline = -paint.ascent(); // ascent() is negative
        int iWidth = (int) (paint.measureText(StringText) + 0.5f); // round
        int iHeight = (int) (fBaseline + paint.descent() + 0.5f);
        Bitmap bmImage = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmImage);
        canvas.drawText(StringText, 0, fBaseline, paint);
        return bmImage;
    }
}