package ca.noble.loanerform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileOutputStream;

public class DrawSignature extends View
{
    private static final String TAG = "DrawSig";

    private Bitmap sigBitmap;

    private static final float STROKE_WIDTH = 5f;
    private static final float HALF_STROKE = STROKE_WIDTH / 2;
    private Paint paint = new Paint();
    private Path path = new Path();
    private float lastX, lastY;
    private final RectF prevRect = new RectF();

    public DrawSignature(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    public void save(View v, String path)
    {
        if (sigBitmap == null)
            sigBitmap = Bitmap.createBitmap(MainActivity.sigCanvas.getWidth(),
                    MainActivity.sigCanvas.getHeight(),
                    Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(sigBitmap);
        try
        {
            FileOutputStream mFOS = new FileOutputStream(path);
            v.draw(canvas);

            sigBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFOS);
            mFOS.flush();
            mFOS.close();
        }
        catch (Exception e) { Log.d(TAG, "error with canvas"); }
    }

    public void clear()
    {
        path.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getParent().requestDisallowInterceptTouchEvent(true);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                path.moveTo(eventX, eventY);
                lastX = eventX;
                lastY = eventY;
                return true;
            }
            case MotionEvent.ACTION_MOVE: {}
            case MotionEvent.ACTION_UP:
            {
                resetPrevRect(eventX, eventY);
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandPrevRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }
                path.lineTo(eventX, eventY);
                break;
            }
            default:
//                    Ignored touch event
                return false;
        }

        invalidate((int) (prevRect.left - HALF_STROKE),
                (int) (prevRect.top - HALF_STROKE),
                (int) (prevRect.right + HALF_STROKE),
                (int) (prevRect.bottom + HALF_STROKE));

        lastX = eventX;
        lastY = eventY;

        return true;
    }

    private void expandPrevRect(float historicalX, float historicalY) {
        if (historicalX < prevRect.left)
            prevRect.left = historicalX;
        else if (historicalX > prevRect.right)
            prevRect.right = historicalX;

        if (historicalY < prevRect.top)
            prevRect.top = historicalY;
        else if (historicalY > prevRect.bottom)
            prevRect.bottom = historicalY;
    }

    private void resetPrevRect(float eventX, float eventY) {
        prevRect.left = Math.min(lastX, eventX);
        prevRect.right = Math.max(lastX, eventX);
        prevRect.top = Math.min(lastY, eventY);
        prevRect.bottom = Math.max(lastY, eventY);
    }
}
