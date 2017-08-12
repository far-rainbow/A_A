package com.example.test.t21052017;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import static android.graphics.BitmapFactory.decodeResource;

/*
Main activity view
*/
public final class DrawView extends View {

    static final double PI_VAL = 3.14159;
    static final double PI_VAL_HALF = 3.14159 / 2;
    static boolean bFirstRun = true;
    static boolean bSplashOn = false;
    static boolean bCalcDone = false;
    /*
    I was born in 1979 so 79 sprites are well enough to show love on =)
    */
    final int BOBS_NUM = 79;
    final String TITLE_TEXT = "-=A&A=-";
    final String SPLASH_TEXT = "NeoCortexLab (L) 2017";
    final String SPLASH_TEXT2 = "79 heart-electrons on their orbits around thou =)";
    final double BG_HEIGHT = (double) decodeResource(this.getResources(), R.drawable.aa).getHeight();
    final double BG_WIDTH = (double) decodeResource(this.getResources(), R.drawable.aa).getWidth();
    final double BG_ASPECT_RATIO_IMG = BG_HEIGHT / BG_WIDTH;
    Matrix matrix;
    private Bitmap[] bmSpriteArray;
    private Bitmap bmBG;
    private Bitmap bmSplashTextA;
    private Bitmap bmSplashTextB;
    private Bitmap bmSplashTextC;
    private Bitmap bmTitleB;
    private Bitmap bmTitleW;
    private int iTextWidthHalfHelperA = 0;
    private int iTextWidthHalfHelper_B_C = 0;
    private int iProgressBar = 0;
    private int iFrameNum = 0;
    private Paint mPaint;

    {
        mPaint = new Paint();
    }

    public DrawView(Context context) {

        super(context);
        matrix = new Matrix();
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

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int CANVAS_WIDTH = canvas.getWidth();
        final int CANVAS_HEIGHT = canvas.getHeight();
        final int CANVAS_WIDTH_HALF = CANVAS_WIDTH / 2;
        final int CANVAS_HEIGHT_HALF = CANVAS_HEIGHT / 2;
        final int CANVAS_HEIGHT_HALF_AA = CANVAS_HEIGHT / 16;
        final int RESIZE_PIXEL_SIZE = CANVAS_WIDTH / 300;

        /*
        Default init would be changed while canvas aspect ratio check
        */
        double dBgAspectRatio = 1.33333;

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
            dBgAspectRatio = (double) CANVAS_HEIGHT / (double) CANVAS_WIDTH;

            if (dBgAspectRatio < BG_ASPECT_RATIO_IMG) {
                iBgHeight = (int) (BG_HEIGHT * (CANVAS_WIDTH / BG_WIDTH));
            }
            if (dBgAspectRatio > BG_ASPECT_RATIO_IMG) {
                iBgWidth = (int) (BG_WIDTH * (CANVAS_HEIGHT / BG_HEIGHT));
            }

            bmBG = getResizedBitmap(decodeResource(this.getResources(), R.drawable.aa), iBgWidth, iBgHeight, 2);

            /*
            SETUP thread for calculations of bitmap array of sprites: from smallest one to bigger one. This is not so fast operation.
            */
            Thread ThreadSplash = new Thread(new Runnable() {
                public void run() {

                    bmSpriteArray = new Bitmap[BOBS_NUM];

                    /*
                    Minimal (zero) blur is going on closest and biggest sprite. Deeper by Z the sprite -- deeper blur FX
                     */
                    int ibLUR = BOBS_NUM;

                    /*
                    Speedup optimization -- do not repeat decodeResource(this.getResources(), R.drawable.h1) into setup loop! It has the huge resource consuption.
                    Let us make an object once and use it. BTW i`ve got more than x10 speedup on some devices
                     */
                    Bitmap Obraz = decodeResource(getResources(), R.drawable.h1);

                    for (int k = 0; k < BOBS_NUM; k++) {
                        bmSpriteArray[k] = getResizedBitmap(Obraz, 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE, (int) (ibLUR-- / 16.0));
                        iProgressBar = k;

                        /*
                        sleep 20ms in each sprite setup just for fun to show off progress bar and make some very faked busy state (after speedup optimizations it must go on!) =)
                         */
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    /*
                    progress bar at full length (yeah -- it seems it musta be BOBS_NUM long but no -- we have to div/mul it into paint thread with some value to fit it into canvas dim)
                     */
                    iProgressBar = BOBS_NUM;

                    /*
                    sleep 1 second for pause between splash screen and intro
                     */
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    /*
                    Reset animation frame counter
                     */
                    iFrameNum = 0;

                    /*
                    Switch scenario condition
                     */
                    bCalcDone = true;

                    /*
                    ??? i dunno -- is there any sense with all this recycling ??? just for fun... ok
                     */
                    Obraz.recycle();
                    bmSplashTextA.recycle();

                }
            });
            ThreadSplash.start();
        }

        /*
        !!! First run !!!: setup splash and title text bitmaps, draw splash text bitmap, change switches
         */
        if (bFirstRun) {

            bmTitleB = blurRenderScript(textAsBitmap(TITLE_TEXT, CANVAS_WIDTH / 4, Color.BLACK), 16);
            bmTitleW = blurRenderScript(textAsBitmap(TITLE_TEXT, CANVAS_WIDTH / 4 - 4, Color.WHITE), 2);

            bmSplashTextA = blurRenderScript(textAsBitmap(SPLASH_TEXT, CANVAS_WIDTH / 16, Color.BLACK), 8);
            bmSplashTextB = blurRenderScript(textAsBitmap(SPLASH_TEXT2, CANVAS_WIDTH / 25, Color.BLACK), 8);

            iTextWidthHalfHelperA = bmSplashTextA.getWidth() / 2;
            canvas.drawBitmap(bmSplashTextA, CANVAS_WIDTH_HALF - iTextWidthHalfHelperA, CANVAS_HEIGHT_HALF, mPaint);

            bmSplashTextA = blurRenderScript(textAsBitmap(SPLASH_TEXT, CANVAS_WIDTH / 16, Color.BLACK), 2);
            bmSplashTextB = blurRenderScript(textAsBitmap(SPLASH_TEXT2, CANVAS_WIDTH / 25, Color.BLACK), 1);
            bmSplashTextC = blurRenderScript(textAsBitmap(SPLASH_TEXT2, CANVAS_WIDTH / 25, Color.WHITE), 1);

            iTextWidthHalfHelper_B_C = bmSplashTextB.getWidth() / 2;

            /*
            Switch scenario conditions -- next step is go into SPLASH block and never go here into FIRST RUN block
             */
            bFirstRun = false;
            bSplashOn = true;

        }

        /*
        PROGRESS BAR. iTextWidthHalfHelperA for bmSplashTextA was inited previously in bFirstRun block
         */
        if (!bFirstRun && !bSplashOn && !bCalcDone) {

            /*
            NEOCORTEXLAB SPLASH TEXT LOGO
             */
            canvas.drawBitmap(bmSplashTextA, CANVAS_WIDTH_HALF - iTextWidthHalfHelperA, CANVAS_HEIGHT_HALF, mPaint);

            /*
            BOTTOM INFO TEXT SPLASH VARIATION
             */
            canvas.drawBitmap(bmSplashTextB, CANVAS_WIDTH_HALF - iTextWidthHalfHelper_B_C, CANVAS_HEIGHT - CANVAS_HEIGHT / 25, mPaint);

            /*
            PROGRESS BAR WITH DIV/MUL ASPECT RATIO GOT FROM OVERALL SPRITE COUNT (BOBS_NUM)
             */
            canvas.drawRect(2, 2, (float) iProgressBar * CANVAS_WIDTH / BOBS_NUM - 2, CANVAS_HEIGHT / 50, mPaint);

            /*
            PERCENTAGE TEXT
             */
            mPaint.setTextSize(CANVAS_HEIGHT / 50);
            canvas.drawText(String.valueOf((iProgressBar * 100 / BOBS_NUM)) + "%", 0, CANVAS_HEIGHT / 25, mPaint);
        }

        /*
        MAIN DRAW ANIMATION: BG, TEXT, SPRITES. iTextWidthHalfHelperA must be reinited for each TITLE_TEXT bitmap text sprites (it has splash text sprite value at this point of runtime)
         */
        if (!bFirstRun && !bSplashOn && bCalcDone) {

            /*
            BACKGROUND
             */
            canvas.drawBitmap(bmBG, 0, 0, mPaint);

            /*
            BOTTOM INFO TEXT
             */
            canvas.drawBitmap(bmSplashTextB, CANVAS_WIDTH_HALF - iTextWidthHalfHelper_B_C, CANVAS_HEIGHT - CANVAS_HEIGHT / 25, mPaint);
            canvas.drawBitmap(bmSplashTextC, CANVAS_WIDTH_HALF - iTextWidthHalfHelper_B_C - 2, CANVAS_HEIGHT - CANVAS_HEIGHT / 25 - 2, mPaint);

            /*
            Half the last biggest sprite to make sprite pivot a little left from the center of canvas
            and draw array sprites one by one with position change calculated by formulae
             */
            int iXHelper = CANVAS_WIDTH_HALF - (bmSpriteArray[BOBS_NUM - 1].getWidth() / 2);
            int iYHelper = CANVAS_HEIGHT_HALF - (bmSpriteArray[BOBS_NUM - 1].getHeight() / 2);

            /*
            Speedup opt START
            */
            double iSpriteFlowHelper = (iXHelper + iYHelper) / PI_VAL;
            double mathSinA = Math.sin((double) iFrameNum / 220);
            double mathSinB = Math.sin((double) iFrameNum / 180);
            int iXSpritePosition;
            int iYSpritePosition;
            double j;
            double iFrameNumDiv200 = iFrameNum * 0.004;
            double i_div_sRndMix = (PI_VAL * MainActivity.sRndMix);
            /*
            Speedup opt END
             */

            for (int i = 0; i < BOBS_NUM; i++) {

                j = i / i_div_sRndMix + iFrameNumDiv200;

                iXSpritePosition = (int) (Math.cos(j) * Math.sin(j * MainActivity.dXSinCf) * (iSpriteFlowHelper * mathSinA) * PI_VAL_HALF);
                iYSpritePosition = (int) ((Math.sin(j) * Math.cos(j * 0.5) * (iSpriteFlowHelper * mathSinB) * PI_VAL_HALF) * dBgAspectRatio);

                /*
                Some fun with sprite rotation...
                 */
                if (MainActivity.dXSinCf > 4.0) {
                    matrix.reset();
                    matrix.preRotate(i * (360f / (BOBS_NUM - 1)) + iFrameNum % 360, CANVAS_WIDTH_HALF, CANVAS_HEIGHT_HALF);
                    canvas.setMatrix(matrix);
                }

                canvas.drawBitmap(bmSpriteArray[i], iXHelper + iXSpritePosition, iYHelper + iYSpritePosition, mPaint);

            }

            /*
            Draw title text over BG and sprites
             */
            matrix.reset();
            canvas.setMatrix(matrix);
            canvas.drawBitmap(bmTitleB, CANVAS_WIDTH_HALF - bmTitleB.getWidth() / 2, CANVAS_HEIGHT_HALF_AA - 2, mPaint);
            canvas.drawBitmap(bmTitleW, CANVAS_WIDTH_HALF - bmTitleW.getWidth() / 2, CANVAS_HEIGHT_HALF_AA, mPaint);

/*            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

        }
        /*
        Frame count add
         */
        iFrameNum++;

        /*
        If music is stoped and this is not system pause -- play again! =)
         */
        if (!MainActivity.mPlayer.isPlaying() && !MainActivity.bGlobalPaused)
            MainActivity.mPlayer.start();
    }

    /*
    Snipet from Stack Overflow: bitmap resize
    */
    public Bitmap getResizedBitmap(Bitmap bm, int iNewWidth, int iNewHeight, int iBLUR) {
        int iWidth = bm.getWidth();
        int iHeight = bm.getHeight();
        float fScaleWidth = ((float) iNewWidth) / iWidth;
        float fScaleHeight = ((float) iNewHeight) / iHeight;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(fScaleWidth, fScaleHeight);
        // "RECREATE" THE NEW BITMAP

        if (iBLUR > 0) {
            return blurRenderScript(Bitmap.createBitmap(bm, 0, 0, iWidth, iHeight, matrix, false), iBLUR);
        } else {
            return Bitmap.createBitmap(bm, 0, 0, iWidth, iHeight, matrix, false);
        }
    }

    private Bitmap blurRenderScript(Bitmap smallBitmap, int radius) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(this.getContext());

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;
    }

    private Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        /*
        Get JPEG pixels.  Each int is the color values for one pixel.
        */
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        /*
        Create a Bitmap of the appropriate format.
        */
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        /*
        Set RGB pixels.
        */
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }
}
