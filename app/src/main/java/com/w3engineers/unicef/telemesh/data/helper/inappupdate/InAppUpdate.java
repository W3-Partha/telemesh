package com.w3engineers.unicef.telemesh.data.helper.inappupdate;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.gson.JsonObject;
import com.w3engineers.unicef.telemesh.R;
import com.w3engineers.unicef.telemesh.data.helper.constants.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

public class InAppUpdate {

    private final String LIVE_JSON_URL = "demo.com/jsonfile"; // Configure json file that was uploaded in Main server
    private final String MAIN_JSON = "updatedJSon.json";
    private final String MAIN_APK = "updatedApk.apk";
    private static File rootFile;
    private static Context mContext;

    // lock the default constructor
    private InAppUpdate() {
        // we can do initial stuff or first time stuff in here
    }

    public static InAppUpdate getInstance(Context context) {
        mContext = context;
        rootFile = mContext.getApplicationContext().getFilesDir();
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static InAppUpdate INSTANCE = new InAppUpdate();
    }

    /**
     * This method is responsible to update app from Main server
     * <p>
     * Yo
     */
    public void appUpdateFromInternet() {
        AppUpdater appUpdater = new AppUpdater(mContext) // This context may be Activity context
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON(LIVE_JSON_URL);
        appUpdater.start();
    }

    //TODO this task is still pending
    public void appUpdateFromLocal() {

    }

    /**
     * This method will be called in every time when main activity open
     * Why? Because we don`t know my apk is updated or not.
     * <p>
     * One solution may have: Save the previous version in shared preference and
     * check the current app version. Then call it
     */
    public void prepareLocalServer() {
        File apkFile = backUpMainApk();
        File jsonFile = createAndUploadJsonFile();

        File f1 = new File(rootFile, MAIN_JSON);
        copyFile(jsonFile, f1);
        File f2 = new File(rootFile, MAIN_APK);
        copyFile(apkFile, f2);
    }

    /**
     * This method is responsible for upload updated apk
     * in local server. for sharing app
     *
     * @return Apk file
     */
    private File backUpMainApk() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List pkgAppsList = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
        String myApplicationPackageName = mContext.getPackageName();
        String myApplicationName = mContext.getResources().getString(R.string.app_name);
        for (Object object : pkgAppsList) {

            ResolveInfo resolveInfo = (ResolveInfo) object;
            File appFile = new File(resolveInfo.activityInfo.applicationInfo.publicSourceDir);

            String file_name = resolveInfo.loadLabel(mContext.getPackageManager()).toString();

            if (file_name.equalsIgnoreCase(myApplicationName) &&
                    appFile.toString().contains(myApplicationPackageName)) {
                return appFile;
            }
        }
        return null;
    }

    private File createAndUploadJsonFile() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.InAppUpdate.LATEST_VERSION_KEY, version);
            jsonObject.addProperty(Constants.InAppUpdate.LATEST_VERSION_CODE_KEY, "" + pInfo.versionCode);
            jsonObject.addProperty(Constants.InAppUpdate.URL_KEY, "http://192.168.43.1:8990/" + MAIN_APK); // TODO change correct url
            jsonObject.addProperty(Constants.InAppUpdate.RELEASE_NOTE_KEY, "Some feature added and bug fixed");

            File file = new File(Environment.getExternalStorageDirectory().toString() + "/" +
                    mContext.getString(R.string.app_name));

            file.mkdir();

            File jsonFile = new File(file, MAIN_JSON);
            try {
                Writer output = new BufferedWriter(new FileWriter(jsonFile));
                output.write(jsonObject.toString());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" +
                mContext.getString(R.string.app_name) + "/" + MAIN_JSON);
        return file;
    }

    private void copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}