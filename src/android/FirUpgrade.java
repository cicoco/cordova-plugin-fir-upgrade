package com.cicoco.cordova.fir;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * <p>
 * </p>
 *
 * @author tafiagu@gmail.com
 * @since 2021/10/29 9:36 上午
 */
public class FirUpgrade extends CordovaPlugin {

    private static final String TAG = "FirUpgrade";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("versionCheck")) {
            LOG.d(TAG, "versionCheck, args:" + (null == args ? "nil" : args.toString()));
            versionCheck(callbackContext);
            return true;
        }
        return false;

    }

    private void versionCheck(CallbackContext callbackContext) {
        VersionChecker versionChecker = new VersionChecker(webView.getContext(), hasNew -> {
            PluginResult result = new PluginResult(PluginResult.Status.OK, hasNew);
            callbackContext.sendPluginResult(result);
        });
        versionChecker.execute();
    }

}
