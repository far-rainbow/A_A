package com.example.test.t21052017;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import static com.example.test.t21052017.MainActivity.PI_VAL;
import static com.example.test.t21052017.MainActivity.PI_VAL_HALF;
import static com.example.test.t21052017.MainActivity.bGlobalPaused;
import static com.example.test.t21052017.MainActivity.dXSinCf;
import static com.example.test.t21052017.MainActivity.mPlayer;
import static com.example.test.t21052017.MainActivity.sRndMix;

/*
Main activity view
*/
class DrawView extends View {

    static boolean bFirstRun = true;
    static boolean bSplashOn = false;
    static boolean bCalcDone = false;
    final String TITLE_TEXT = "-=A&A=-";
    final String SPLASH_TEXT = "NeoCortexLab (L) 2017";
    final String SPLASH_TEXT2 = "79 heart-electrons orbiting all around the corner =)";

    final double BG_HEIGHT = (double) BitmapFactory.decodeResource(this.getResources(), R.drawable.aa).getHeight() / 2;
    final double BG_WIDTH = (double) BitmapFactory.decodeResource(this.getResources(), R.drawable.aa).getWidth() / 2;
    final double BG_ASPECT_RATIO_IMG = BG_HEIGHT / BG_WIDTH;
    Context CONTEXT;
    private Bitmap[] bmSpriteArray;
    private Bitmap bmBG;
    private Bitmap bmSplashTextA;
    private Bitmap bmSplashTextB;
    private Bitmap bmTitleB;
    private Bitmap bmTitleW;
    private int iTextWidthHalfHelperA = 0;
    private int iTextWidthHalfHelperB = 0;
    private int iProgressBar = 0;
    private int iFrameNum = 0;
    private Paint mPaint;

    {
        mPaint = new Paint();
    }

    public DrawView(Context context) {
        super(context);
        this.CONTEXT = context;
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
        final int RESIZE_PIXEL_SIZE = CANVAS_WIDTH / 333;

        /*
        I was born in 1979 so 79 sprites are well enough to show love on =)
        */
        final int BOBS_NUM = 79;

        /*
        Default init would be changed while canvas aspect ratio check
        */
        double dBgAspectRatio = 1.62;

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
            bmBG = getResizedBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.aa), iBgWidth, iBgHeight, 2);

            /*
            Setup thread for calculations of bitmap array of sprites: from smallest one to bigger one. This is not so fast operation.
            */

            final Bitmap Obraz = decodeResource(this.getResources(), R.drawable.h1);

            Thread ThreadSplash = new Thread(new Runnable() {
                public void run() {
                    bmSpriteArray = new Bitmap[BOBS_NUM];
                    int ibLUR = BOBS_NUM;
                    for (int k = 0; k < BOBS_NUM; k++) {
                        bmSpriteArray[k] = getResizedBitmap(Obraz, 1 + k * RESIZE_PIXEL_SIZE, 1 + k * RESIZE_PIXEL_SIZE, (int) (ibLUR-- / 16.0));
                        iProgressBar = k;

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    bCalcDone = true;
                    iFrameNum = 0;
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
            iTextWidthHalfHelperB = bmSplashTextB.getWidth() / 2;

            bFirstRun = false;
            bSplashOn = true;
        }

        /*
        PROGRESS BAR. iTextWidthHalfHelperA for bmSplashTextA was inited previously in bFirstRun block
         */
        if (!bFirstRun && !bSplashOn && !bCalcDone) {

            canvas.drawBitmap(bmSplashTextA, CANVAS_WIDTH_HALF - iTextWidthHalfHelperA, CANVAS_HEIGHT_HALF, mPaint);
            canvas.drawBitmap(bmSplashTextB, CANVAS_WIDTH_HALF - iTextWidthHalfHelperB, CANVAS_HEIGHT - CANVAS_HEIGHT / 25, mPaint);

            canvas.drawRect(2, 2, (float) iProgressBar * CANVAS_WIDTH / BOBS_NUM, CANVAS_HEIGHT / 50, mPaint);
            mPaint.setTextSize(CANVAS_HEIGHT / 50);
            canvas.drawText(String.valueOf((iProgressBar * 100 / BOBS_NUM)) + "%", 0, CANVAS_HEIGHT / 25, mPaint);
        }

        /*
        MAIN DRAW ANIMATION: BG, TEXT, SPRITES. iTextWidthHalfHelperA must be reinited for each TITLE_TEXT bitmap text sprites (it has splash text sprite value at this point of runtime)
         */
        if (!bFirstRun && !bSplashOn && bCalcDone) {

            canvas.drawBitmap(bmBG, 0, 0, mPaint);
            canvas.drawBitmap(bmSplashTextB, CANVAS_WIDTH_HALF - iTextWidthHalfHelperB, CANVAS_HEIGHT - CANVAS_HEIGHT / 25, mPaint);

            /*
            Half the last biggest sprite to make sprite pivot a little left from the center of canvas
            and draw array sprites one by one with position change calculated by formulae
             */
            int iXHelper = CANVAS_WIDTH_HALF - (bmSpriteArray[BOBS_NUM - 1].getWidth() / 2);
            int iYHelper = CANVAS_HEIGHT_HALF - (bmSpriteArray[BOBS_NUM - 1].getHeight() / 2);
            double iSpriteFlowHelper = (iXHelper + iYHelper) / PI_VAL;
            for (int i = 0; i < BOBS_NUM; i++) {
                double j = (double) i / (PI_VAL * sRndMix) + iFrameNum * 0.004; // (/200)
                int iXSpritePosition = (int) (Math.cos(j) * Math.sin(j * dXSinCf) * (iSpriteFlowHelper * Math.sin((double) iFrameNum / 220)) * PI_VAL_HALF);
                int iYSpritePosition = (int) ((Math.sin(j) * Math.cos(j * 0.5) * (iSpriteFlowHelper * Math.sin((double) iFrameNum / 180)) * PI_VAL_HALF) * dBgAspectRatio);

/*                int i_temp = (int)((i - j*2.0));

                //if (i==39) System.out.println(i_temp);

                if (i_temp<1) {
                    i_temp=1;
                }
                if (i_temp>78) {
                    i_temp=78;
                }*/
                canvas.drawBitmap(bmSpriteArray[i], iXHelper + iXSpritePosition, iYHelper + iYSpritePosition, mPaint);
            }

            /*
            Draw title text over BG and sprites
             */
            canvas.drawBitmap(bmTitleB, CANVAS_WIDTH_HALF - bmTitleB.getWidth() / 2, CANVAS_HEIGHT_HALF_AA - 2, mPaint);
            canvas.drawBitmap(bmTitleW, CANVAS_WIDTH_HALF - bmTitleW.getWidth() / 2, CANVAS_HEIGHT_HALF_AA, mPaint);

            try {
                Thread.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
