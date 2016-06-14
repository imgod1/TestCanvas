package com.imgod.gaokang.testcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 项目名称：TestCanvas
 * 包名称：com.imgod.gaokang.testcanvas
 * 类描述：签名View
 * 创建人：gaokang
 * 创建时间：2016-06-14 11:48
 * 修改人：gaokang
 * 修改时间：2016-06-14 11:48
 * 修改备注：
 */
public class SignView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    public static final String FILE_PATH = "filepath";
    public static final String TEMP_BITMAP = "tempbitmap";
    public static final String CONTEXT = "context";

    private Paint paint = new Paint();
    private Path path = new Path();

    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    public SignView(Context context) {
        super(context);
    }

    public SignView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        paint.setColor(Color.RED);
        paint.setTextSize(30);
        paint.setStrokeWidth(25);//设置线条粗细
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        setOnTouchListener(this);
    }

    /**
     * 设置画笔颜色
     *
     * @param color 画笔颜色
     */
    public void setPainColor(int color) {
        paint.setColor(color);
    }

    /**
     * 设置画笔粗细
     *
     * @param width 宽度
     */
    public void setPainStokenSize(float width) {
        paint.setStrokeWidth(width);
    }

    public void draw() {
        Canvas canvas = getHolder().lockCanvas();
        tempCanvas.drawColor(Color.WHITE);
        tempCanvas.drawPath(path, paint);
        canvas.drawBitmap(tempBitmap, 0, 0, null);
        getHolder().unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(event.getX(), event.getY());
                draw();
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(event.getX(), event.getY());
                draw();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    /**
     * 清理画布
     */
    public void clearPaint() {
        path.reset();
        draw();
    }

    /**
     * @return 返回绘制的bitmap
     */
    public Bitmap getBitmap() {
        return tempBitmap;
    }


//asyncTask 的get方法会阻塞ui线程

    /**
     * 依赖权限： <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     *
     * @param filePath 保存到的文件路径
     * @param saveListener 保存结果的监听
     */
    public void saveBitmap(String filePath, SaveListener saveListener) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(FILE_PATH, filePath);
        hashMap.put(TEMP_BITMAP, tempBitmap);
        hashMap.put(CONTEXT, getContext());
        new SaveFile(saveListener).execute(hashMap);
    }

    public static class SaveFile extends AsyncTask<HashMap, Void, Boolean> {
        SaveListener saveListener;
        public void setSaveListener(SaveListener saveListener) {
            this.saveListener = saveListener;
        }
        public SaveFile(SaveListener saveListener) {
            this.saveListener = saveListener;
        }

        @Override
        protected Boolean doInBackground(HashMap... params) {
            boolean saveResult = false;
            String filePath = params[0].get(FILE_PATH).toString();
            Bitmap tempBitmap = (Bitmap) params[0].get(TEMP_BITMAP);
            Context context = (Context) params[0].get(CONTEXT);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(filePath));
                saveResult = tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return saveResult;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (saveListener != null) {
                saveListener.saveResult(aBoolean);
            }
        }
    }

    /**
     * 保存结果的监听
     */
    public interface SaveListener {
        public void saveResult(Boolean result);
    }

}
