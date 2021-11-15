package com.cicoco.cordova.fir;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>
 * </p>
 *
 * @author tafiagu@gmail.com
 * @since 2020/10/14 5:30 PM
 */
public class VersionChecker extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private CheckCallback checkCallback;

    public VersionChecker(Context mContext, CheckCallback checkCallback) {
        this.mContext = mContext;
        this.checkCallback = checkCallback;
    }

    private static final String FIR_URL = "http://api.bq04.com/apps/latest/%s?api_token=%s";


    private static PackageInfo getAppInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();

        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException var2) {
            return null;
        }
    }

    private static String getBody(InputStream is) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new
                InputStreamReader(is, "UTF-8"));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString().trim();
    }

    @Override
    protected void onPostExecute(String update) {
        if (TextUtils.isEmpty(update)) {
            if (null != checkCallback) {
                checkCallback.checkResult(false);
            }
            return;
        }
        super.onPostExecute(update);
        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        int messageId = mContext.getResources().getIdentifier("has_new_version", "string", mContext.getPackageName());
        int titleId = mContext.getResources().getIdentifier("new_version", "string", mContext.getPackageName());
        int ignoreId = mContext.getResources().getIdentifier("ignore_update", "string", mContext.getPackageName());
        int confirmId = mContext.getResources().getIdentifier("go_to_update", "string", mContext.getPackageName());
        dlg.setMessage(messageId == 0 ? "存在新版本，是否前往更新?" : mContext.getString(messageId));
        dlg.setTitle(titleId == 0 ? "新版本" : mContext.getString(titleId));
        dlg.setCancelable(false);
        dlg.setNegativeButton(ignoreId == 0 ? "暂不更新" : mContext.getString(ignoreId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dlg.setPositiveButton(confirmId == 0 ? "前往更新" : mContext.getString(confirmId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openUrl(update);
                dialog.dismiss();
            }
        });

        dlg.create();
        dlg.show();
        if (null != checkCallback) {
            checkCallback.checkResult(true);
        }
    }


    private void openUrl(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        mContext.startActivity(intent);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String appId = null;
        String apiToken = null;

        try {
            ApplicationInfo applicationInfo =
                    mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                appId = applicationInfo.metaData.getString("FIR_ANDROID_APP_ID");
                apiToken = applicationInfo.metaData.getString("FIR_API_TOKEN");
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(apiToken)) {
            return null;
        }


        String urlString = String.format(FIR_URL, appId, apiToken);

        InputStream inputStream = null;
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(30 * 1000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                JSONObject body = new JSONObject(getBody(inputStream));
                int lastBuild = Integer.parseInt(body.getString("build"));
                PackageInfo current = getAppInfo(mContext);
                int curBuild = current.versionCode;
                if (curBuild < lastBuild) {
                    return body.getString("update_url");
                }
            }
        } catch (IOException | JSONException e) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }

            if (null != conn) {
                conn.disconnect();
            }
        }

        return null;
    }

    public interface CheckCallback {
        void checkResult(boolean hasNew);
    }
}
