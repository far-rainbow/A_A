package su.kamenka.t21052017;

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

    private static final double PI_VAL = 3.14159265358;
    private static final double PI_VAL_HALF = PI_VAL / 2;
    private static long timer;
    private static double middleFrameTime = 0;
    private static boolean bFirstRun = true;
    private static boolean bSplashOn = false;
    private static boolean bCalcDone = false;
    /*
    Sprite array. Pre-calcs on the init stage by zooming out reference bitmap and slightly bluring it...
     */
    static private Bitmap[] bmSpriteArray;
    /*
    Bitmap objects -- background, text strings, e.t.c. Pre-calcs on the init stage.
     */
    static private Bitmap bmBG;
    static private Bitmap bmSplashTextA;
    static private Bitmap bmSplashTextB;
    static private Bitmap bmSplashTextC;
    static private Bitmap bmTitleB;
    static private Bitmap bmTitleW;
    static private Bitmap Obraz;
    static private Bitmap Obraz_2;
    static private Bitmap Obraz_3;
    /*
    Speedup opt precalc values for bmSplashTextA and bmSplashTextB,bmSplashTextC -- center point on the x axis (width/2)
     */
    static private int iTextWidthHalfHelperA;
    static private int iTextWidthHalfHelper_B_C;
    /*
    Progress bar percentage on the init pre-calc splash screen stage
     */
    static private int iProgressBar;
    /*
    animation frame number
     */
    static private int iFrameNum;
    /*
    Paint object to draw some graphics... (drawBitmap,drawText and e.t.c methods)
     */
    private final Paint mPaint;
    private final int sleepMS = 10;
    /*
    I was born in 1979 so 79 sprites are well enough to show love on =)
    */
    private final int BOBS_NUM = 79;
    private final int BOBS_NUM_M_1 = BOBS_NUM - 1;
    private final String TITLE_TEXT = "2018";
    private final String SPLASH_TEXT = "NeoCortexLab (L) 2018";
    private final String SPLASH_TEXT2 = "Happy New Year =)";
    private final double BG_HEIGHT = (double) decodeResource(this.getResources(), R.drawable.aa2).getHeight();
    private final double BG_WIDTH = (double) decodeResource(this.getResources(), R.drawable.aa2).getWidth();
    private final double BG_ASPECT_RATIO_IMG = BG_HEIGHT / BG_WIDTH;
    private final Matrix matrix;
    //Bitmap dbg1, dbg2, dbg3, dbg4, dbg5, dbg6, dbg7, dbg8, dbg9,dbgOGLVersion;
    private double minFrameTime = 999999999;
    private double maxFrameTime = 0;
    private int CANVAS_WIDTH;
    private int CANVAS_HEIGHT;

    {
        mPaint = new Paint();
    }

    /*
    Class constructor
     */
    public DrawView(Context context) {
        super(context);
        matrix = new Matrix();
    }

    /*
    Snippet from Stack Overflow: text to bitmap with size and color
    */
    private static Bitmap textAsBitmap(String StringText, float fTextSize, int iTextColor) {

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
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        System.out.println("RESIZE = " + xNew);
        CANVAS_WIDTH = xNew;
        CANVAS_HEIGHT = yNew;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //System.out.println("BEFORE SUPER METHOD IMPL");
        super.onDraw(canvas);

        /*
        Speedup opt precalc values
         */
        //CANVAS_WIDTH = 720;
        //System.out.println("GET_WIDTH = " + CANVAS_WIDTH);
        //CANVAS_HEIGHT = 1280;

        final int BLUR_FACTOR = 3500000 / (CANVAS_WIDTH * CANVAS_HEIGHT) + 4;
        //System.out.println("BLURFACTOR = " + BLUR_FACTOR);
        final int CANVAS_WIDTH_HALF = CANVAS_WIDTH / 2;
        final int CANVAS_HEIGHT_HALF = CANVAS_HEIGHT / 2;
        final int CANVAS_HEIGHT_HALF_AA = CANVAS_HEIGHT / 16;
        final int RESIZE_PIXEL_SIZE = CANVAS_WIDTH / 250 + 1;

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

/*            dbg1 = textAsBitmap(String.format("CA:%.3f,BGA:%.3f,CH:%d,CW:%d",dBgAspectRatio,BG_ASPECT_RATIO_IMG,CANVAS_HEIGHT,CANVAS_WIDTH), 24, Color.RED);
            dbg2 = textAsBitmap(String.valueOf(iBgHeight) + " / " + String.valueOf(iBgWidth), 32, Color.RED);
            dbg3 = textAsBitmap(String.valueOf(BG_WIDTH), 32, Color.BLACK);
            dbg4 = textAsBitmap(String.valueOf(BG_HEIGHT), 32, Color.BLACK);*/

/*            dbgOGLVersion = textAsBitmap(String.format("OGL v. = %x",MainActivity.OpenGLVersion), 32, Color.BLACK);*/

            bmBG = getResizedBitmap(decodeResource(this.getResources(), R.drawable.aa2), iBgWidth, iBgHeight, 2);

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

                    for (int k = 0; k < BOBS_NUM; ) {
                        if (k%12==0) {
                            bmSpriteArray[k] = getResizedBitmap(Obraz_3, 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE, ibLUR-- / BLUR_FACTOR);
                            iProgressBar = k++;
                        } else {
                            bmSpriteArray[k] = getResizedBitmap(Obraz, 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE, ibLUR-- / BLUR_FACTOR);
                            iProgressBar = k++;
                        }
                        if (k < BOBS_NUM_M_1) {
                            bmSpriteArray[k] = getResizedBitmap(Obraz_2, 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE, ibLUR-- / BLUR_FACTOR);
                            iProgressBar = k++;
                        }

                        /*
                        sleep sleepMS ms in each sprite setup just for fun to show off progress bar and make some very faked busy state (after speedup optimizations it must go on!) =)
                         */
                        try {
                            Thread.sleep(sleepMS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    /*
                    progress bar at full length (yeah -- it seems it musta be BOBS_NUM long but no -- we have to div/mul it into paint thread with some value to fit it into canvas dim)
                     */
                    iProgressBar = BOBS_NUM;

                    /*
                    sleep 1.62 second for pause between splash screen and intro -- just for fun
                     */
                    try {
                        Thread.sleep(1618);
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
                    Obraz_2.recycle();
                    Obraz_3.recycle();
                    bmSplashTextA.recycle();

                }
            });
            ThreadSplash.start();
        }

        /*
        !!! First run !!!: setup splash and title text bitmaps, draw splash text bitmap, change switches
         */
        if (bFirstRun) {

/*            dbg7 = textAsBitmap(String.format("min: %.2f", minFrameTime), 32, Color.BLUE);
            dbg8 = textAsBitmap(String.format("min: %.2f", minFrameTime), 32, Color.BLUE);
            dbg9 = textAsBitmap(String.format("min: %.2f", minFrameTime), 32, Color.BLUE);*/

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
                    Speedup optimization -- do not repeat decodeResource(this.getResources(), R.drawable.h1) into setup loop! It has a huge resource consumption.
                    Let us make an object once and use it. BTW i`ve got more than x10 speedup on some devices
                     */
            Obraz = decodeResource(getResources(), R.drawable.sn);
            Obraz_2 = decodeResource(getResources(), R.drawable.sn_viber);
            Obraz_3 = decodeResource(getResources(), R.drawable.h1small);

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

            /* snowflake flow */
            //canvas.drawBitmap(Obraz, CANVAS_WIDTH_HALF - iTextWidthHalfHelperA,200, mPaint);

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

            timer = System.nanoTime();

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
            int iXHelper = CANVAS_WIDTH_HALF - (bmSpriteArray[BOBS_NUM_M_1].getWidth() / 2);
            int iYHelper = CANVAS_HEIGHT_HALF - (bmSpriteArray[BOBS_NUM_M_1].getHeight() / 2);

            /*
            Speedup optimisation START
            */
            double iSpriteFlowHelper = (iXHelper + iYHelper) / PI_VAL;

            /*
            Pre-calc sin for speedup optimisation
             */
            double mathSinA = Math.sin((double) iFrameNum / 220);
            double mathSinB = Math.sin((double) iFrameNum / 180);

            int iXSpritePosition;
            int iYSpritePosition;

            /*

             */
            double phi;

            double iFrameNumDiv200 = iFrameNum * 0.004;
            double i_div_sRndMix = (PI_VAL * MainActivity.sRndMix);
            /*
            Speedup opt END
             */

            for (int i = 0; i < BOBS_NUM; i++) {

                phi = i / i_div_sRndMix + iFrameNumDiv200;

                /*
                Calculate X,Y positions for sprite #i
                 */
                iXSpritePosition = (int) (Math.cos(phi) * Math.sin(phi * MainActivity.dXSinCf) * (iSpriteFlowHelper * mathSinA) * PI_VAL_HALF);
                iYSpritePosition = (int) ((Math.sin(phi) * Math.cos(phi * 0.5) * (iSpriteFlowHelper * mathSinB) * PI_VAL_HALF) * dBgAspectRatio);

                /*
                Have some fun with sprite rotation...
                 */
                if (MainActivity.dXSinCf > 4.0) {

                    matrix.reset();
                    matrix.preRotate((i * (360f / (BOBS_NUM_M_1)) + iFrameNum % 360) * (float)MainActivity.squizze, CANVAS_WIDTH_HALF , CANVAS_HEIGHT_HALF );
                    canvas.setMatrix(matrix);
                }

                /*
                Draw sprite #i with calculated X,Y position
                 */
                canvas.drawBitmap(bmSpriteArray[i], iXHelper + iXSpritePosition, iYHelper + iYSpritePosition, mPaint);

            }

            /*
            Draw title text over BG and sprites
             */
            matrix.reset();
            canvas.setMatrix(matrix);
            canvas.drawBitmap(bmTitleB, CANVAS_WIDTH_HALF - bmTitleB.getWidth() / 2, CANVAS_HEIGHT_HALF_AA - 2, mPaint);
            canvas.drawBitmap(bmTitleW, CANVAS_WIDTH_HALF - bmTitleW.getWidth() / 2, CANVAS_HEIGHT_HALF_AA, mPaint);

/*            canvas.drawBitmap(dbg1, 32, 64, mPaint);
            canvas.drawBitmap(dbg2, 32, 96, mPaint);
            canvas.drawBitmap(dbg3, 32, 128, mPaint);
            canvas.drawBitmap(dbg4, 32, 160, mPaint);*/

/*            dbg5 = textAsBitmap(String.format("sRndMix: %.2f / sq: %.2f", MainActivity.sRndMix,MainActivity.squizze), 32, Color.GREEN);
            dbg6 = textAsBitmap(String.format("dXSinCf: %.2f", MainActivity.dXSinCf), 32, Color.GREEN);

            canvas.drawBitmap(dbg5, 32, CANVAS_HEIGHT - 64, mPaint);
            canvas.drawBitmap(dbg6, 32, CANVAS_HEIGHT - 96, mPaint);*/

/*            if (iFrameNum % 50 == 0) {

                middleFrameTime = System.nanoTime() - timer;
                dbg7 = textAsBitmap(String.format("mid: %.2f", middleFrameTime / 50), 32, Color.BLACK);

                if (middleFrameTime < minFrameTime) {
                    minFrameTime = middleFrameTime;
                    dbg8 = textAsBitmap(String.format("min: %.2f", minFrameTime / 50), 32, Color.BLACK);
                }

                if (middleFrameTime > maxFrameTime) {
                    maxFrameTime = middleFrameTime;
                    dbg9 = textAsBitmap(String.format("max: %.2f", maxFrameTime / 50), 32, Color.BLACK);
                }

            }

            canvas.drawBitmap(dbg7, 32, 192, mPaint);
            canvas.drawBitmap(dbg8, 32, 192 + 32, mPaint);
            canvas.drawBitmap(dbg9, 32, 192 + 64, mPaint);

            canvas.drawBitmap(dbgOGLVersion, 32 , 192+96,mPaint);*/

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        /*
        Frame count add
         */
        iFrameNum++;

        /*
        If music is stoped and this is not system pause -- play again! =)
         */
        if (!MainActivity.mPlayer.isPlaying() && !MainActivity.bGlobalPaused) {
            MainActivity.mPlayer.start();
        }
    }

    /*
    Snipet from Stack Overflow: bitmap resize
    */
    private Bitmap getResizedBitmap(Bitmap bm, int iNewWidth, int iNewHeight, int iBLUR) {
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

        Bitmap bitmap;
        //if (smallBitmap.getWidth() > 16) {

        bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        /*} else {
            bitmap = Bitmap.createBitmap(
                    16,16,
                    Bitmap.Config.ARGB_8888);
        }*/

        RenderScript rs = RenderScript.create(this.getContext());

        Allocation blurInput = Allocation.createFromBitmap(rs, smallBitmap);
        //blurInput.
        Allocation blurOutput = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);
        blurOutput.copyTo(bitmap);

        rs.destroy();

        return bitmap;
    }

    private Bitmap RGB565toARGB888(Bitmap img) {
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
