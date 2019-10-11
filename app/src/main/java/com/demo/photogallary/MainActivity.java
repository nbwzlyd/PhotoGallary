package com.demo.photogallary;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.Toast;

import com.demo.photogallary.adapter.GridViewAdapter;
import com.demo.photogallary.utils.ImageUtils;
import com.demo.photogallary.utils.NativeImageLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_PERMISSION = 0x01;

    private GridView mPhotoGridView;
    private GridViewAdapter mAdapter;

    private ImageUtils mImageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mPhotoGridView = findViewById(R.id.grid_view);
        mAdapter = new GridViewAdapter(this);
        mPhotoGridView.setAdapter(mAdapter);
        mImageUtils = new ImageUtils(this);
        requestWritePermission();
        mAdapter.setOnClickListener(new GridViewAdapter.onClickListener() {
            @Override
            public void onClick(String imageUrl) {
                goPhotoActivity(imageUrl);
            }

            @Override
            public void onLongClick(String imageUr) {
                showDeleteDialog(imageUr);
            }
        });
    }


    //When the screen is rotated, we have to reset the Columns num
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            mAdapter.setNumColumns(5);
            mPhotoGridView.setNumColumns(5);
        }else{
            mAdapter.setNumColumns(3);
            mPhotoGridView.setNumColumns(3);
        }
    }

    // on Android 6.0 and above ,we need WRITE_EXTERNAL_STORAGE permission to show Image
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == WRITE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "You must allow permission write external storage to your mobile device.", Toast.LENGTH_SHORT).show();
                finish();
            }else {
                loadImage();
            }

        }

    }
    // on Android 6.0 and above ,we need WRITE_EXTERNAL_STORAGE permission to show Image
    private void requestWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
            }else {
                loadImage();
            }
        }

    }



    private void showDeleteDialog(final String imageUr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("delete this image?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mImageUtils.deletePic(imageUr);
                mAdapter.notifyDataSetChanged();
                mAdapter.getData().remove(imageUr);
                NativeImageLoader.getInstance().removeBitmapFromMemCache(imageUr);
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    private void loadImage(){
        mImageUtils.getImages(new ImageUtils.LoadImageCallBack() {
            @Override
            public void loadSuccess(List<String> list) {
                mAdapter.setData(list);
            }
        });
    }


    private void goPhotoActivity(String imageUrl) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(PhotoActivity.IMAGE_URL,imageUrl);
        intent.putExtras(bundle);
        intent.setClass(this,PhotoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
