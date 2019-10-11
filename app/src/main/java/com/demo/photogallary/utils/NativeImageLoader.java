package com.demo.photogallary.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Image loading framework
 * Use ExecutorService and LruCache
 */
public class NativeImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(Math.max(2, Math.min(Runtime.getRuntime().availableProcessors() - 1, 4)));

    private NativeImageLoader() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    public static NativeImageLoader getInstance() {
        return mInstance;
    }

    /**
     * @param path  the image path on device
     * @param width
     * @param height
     * @param forceHighQualityImage  if true , bitmap will not save to memory
     * @param mCallBack when image load success ,this method will be called
     */
    //load image
    public void loadNativeImage(final String path, final int width, final int height, final boolean forceHighQualityImage, final NativeImageCallBack mCallBack) {
        Bitmap bitmap = null;
        if (!forceHighQualityImage) {
            bitmap = getBitmapFromMemCache(path);
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                mCallBack.onImageLoader((Bitmap) msg.obj, path);
                return true;
            }
        });

        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap mBitmap = rotate(decodeThumbBitmapForFile(path, width, height), path);
                    Message msg = handler.obtainMessage();
                    msg.obj = mBitmap;
                    handler.sendMessage(msg);
                    if (!forceHighQualityImage) {
                        addBitmapToMemoryCache(path, mBitmap);
                    }
                }
            });
        } else {
            Message message = handler.obtainMessage();
            message.obj = bitmap;
            handler.sendMessage(message);
        }
    }

    // return bitmap from memoryCache
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    // return bitmap from memoryCache

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    //remove bitmap from MemCache
    public void removeBitmapFromMemCache(String key){
        if (mMemoryCache.get(key)!=null) {
            mMemoryCache.remove(key);
        }
    }


    //

    private Bitmap rotate(Bitmap bitmap, String imagePath) {

        int digree = 0;
        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            // Read camera direction information in the picture
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // Calculate the angle of rotation
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }

        if (digree != 0) {
            // rotate picture
            Matrix m = new Matrix();
            m.postRotate(digree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
        }
        return bitmap;
    }

    /**
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */

    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * @param options
     * @param viewWidth  The zoom width size of the image
     * @param viewHeight The zoom height size of the image
     * @return
     */
    // scale origin bitmap
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewWidth == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        if (bitmapWidth > viewWidth || bitmapHeight > viewWidth) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);

            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }


    public interface NativeImageCallBack {
        public void onImageLoader(Bitmap bitmap, String path);
    }
}