package com.felkertech.sample.channelsurfer;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.felkertech.channelsurfer.setup.SimpleTvSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.Manifest;

public class SampleTvSetup extends SimpleTvSetup {
    private int PERMISSIONS_CODE = 83;
    @Override
    public void setupTvInputProvider() {
//        toPath = "/data/data/" + getPackageName();  // Your application path
        //We need to override this in order to save our assets to a real file, making them
        //playable in ExoPlayer

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_CODE);
            else
                startSetup();
        } else
            startSetup();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if(results[0] == PackageManager.PERMISSION_GRANTED) {
            startSetup();
        }
    }

    public void startSetup() {
        copyAsset(getAssets(), "atnews.mp4", "androidtvnews.mp4");
        copyAsset(getAssets(), "ectasy.mp3", "ectasy.mp3");
        File channelsurferFolder = new File(LOCAL_FILES_FOLDER);
        channelsurferFolder.mkdir();
        super.setupTvInputProvider();
    }

    public static String LOCAL_FILES_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()+"/channelsurfer";


    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            LOCAL_FILES_FOLDER + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            LOCAL_FILES_FOLDER + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            Log.d("STS", LOCAL_FILES_FOLDER+"/"+toPath);
            File copiedFile = new File(LOCAL_FILES_FOLDER+"/"+toPath);
            Log.d("STS", copiedFile.getAbsolutePath());
            boolean wasSuccessful = copiedFile.createNewFile();
            Log.d("STS", "Was successful in creating? "+wasSuccessful);
            out = new FileOutputStream(LOCAL_FILES_FOLDER+"/"+toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
