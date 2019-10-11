package com.demo.photogallary;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.demo.photogallary.utils.NativeImageLoader;
import com.demo.photogallary.utils.ScreenUtils;

public class PhotoActivity extends AppCompatActivity {

    public static final String IMAGE_URL = "image_url";

    private  String imageUrl;

    private ImageView mImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
        initData();
    }

    private void initView() {
        mImageView = findViewById(R.id.imageView);

    }

    private void initData() {
       Bundle bundle =  getIntent().getExtras();
       if (bundle!=null){
          imageUrl =  bundle.getString(IMAGE_URL);
       }


       final ProgressDialog dialog =  ProgressDialog.show(this,"","Loading image......");

       NativeImageLoader.getInstance().loadNativeImage(imageUrl,ScreenUtils.getScreenWidth(this)*2, ScreenUtils.getScreenHeight(this)*2,
                true,new NativeImageLoader.NativeImageCallBack() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String path) {
                        mImageView.setImageBitmap(bitmap);
                        dialog.dismiss();
                    }
                });
    }


}
