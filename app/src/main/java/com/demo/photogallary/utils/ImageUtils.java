package com.demo.photogallary.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//utils to find all photos on the phone
public class ImageUtils {

    private ProgressDialog mProgressDialog;
    private Context mContext;

    private static final int SCAN_OK = 0x01;

    private List<String> mImageList = new ArrayList<>();

    private LoadImageCallBack mCallBack;

    private Handler mHandler;

    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(Math.max(2, Math.min(Runtime.getRuntime().availableProcessors() - 1, 4)));


    public interface LoadImageCallBack {
        void loadSuccess(List<String> list);
    }


    public ImageUtils(Context context) {
        mContext = context;
        mHandler = new Handler();

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {//This method will be called after the scan is completed.
                if (msg.what == SCAN_OK) {
                    mProgressDialog.dismiss();
                    mCallBack.loadSuccess(mImageList);
                }

                return true;
            }
        });
    }

    //get all photos on device by contentProvider and ExecutorService

    public void getImages(LoadImageCallBack callBack) {
        mProgressDialog = ProgressDialog.show(mContext, null, "Loading Images...");
        mImageList.clear();
        mCallBack = callBack;

        mImageThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = mContext.getContentResolver();

                //Just query images of jpeg and png
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_ADDED);

                if (mCursor == null) {
                    return;
                }
                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    mImageList.add(0,path);

                }
                Message message = mHandler.obtainMessage();
                message.what = SCAN_OK;
                mHandler.sendMessage(message);
                mCursor.close();
            }
        });

    }

    public void deletePic(String path){
        if(!TextUtils.isEmpty(path)){
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = mContext.getContentResolver();
            String url =  MediaStore.Images.Media.DATA + "='" + path + "'";
            //delete pic
            contentResolver.delete(uri, url, null);
        }
    }
}
