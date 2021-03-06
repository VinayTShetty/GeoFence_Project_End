package com.vithamastech.smartlight.doorbell;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.vithamastech.smartlight.MainActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class RestApi {

    protected MainActivity activity;
    protected RestRequest rest;
    protected RestCallback callback;
    protected RestErrorCallback errorCallback;
    protected String BASE_URL;
    protected String urlSuffix = "";

    protected String loadingMessage;
    private List<NameValuePair> parameters;

    protected int cachePolicy = RestCache.CachePolicy.IGNORE_CACHE;

    public String endpoint = null;

    private String requestType = null;

    public RestApi(MainActivity activity) {

        this.activity = activity;

        this.reset();

        this.rest = new RestRequest();

        this.rest.setHandler(new Handler() {
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();

                if (b.containsKey("data")) {
                    String data = b.getString("data");

                    try {
                        if (data == null) {
                            RestApi.this.onSuccess(null);
                        } else if (data.trim().substring(0, 1).equalsIgnoreCase("{")) {
                            Object returnObject = new JSONObject(data);

                            // we want to save the cache
                            if (RestApi.this.requestType.equalsIgnoreCase("get") && RestApi.this.cachePolicy != RestCache.CachePolicy.IGNORE_CACHE) {
                                RestCache.save(RestApi.this, data.trim());
                            }

                            if (RestApi.this.cachePolicy != RestCache.CachePolicy.UPDATE_CACHE) {
                                RestApi.this.onSuccess(returnObject);
                            }
                        } else if (data.trim().substring(0, 1).equalsIgnoreCase("[")) {
                            Object returnObject = new JSONArray(data);

                            // we want to save the cache
                            if (RestApi.this.requestType.equalsIgnoreCase("get") && RestApi.this.cachePolicy != RestCache.CachePolicy.IGNORE_CACHE) {
                                RestCache.save(RestApi.this, data.trim());
                            }

                            if (RestApi.this.cachePolicy != RestCache.CachePolicy.UPDATE_CACHE) {
                                RestApi.this.onSuccess(returnObject);
                            }
                        } else {
                            // incorrect format
                            Log.d("RestApi", data);
                            //RestApi.this.onError("Unknown format of data");
                            // we want to save the cache
                            if (RestApi.this.requestType.equalsIgnoreCase("get") && RestApi.this.cachePolicy != RestCache.CachePolicy.IGNORE_CACHE) {
                                RestCache.save(RestApi.this, data.trim());
                            }

                            if (RestApi.this.cachePolicy != RestCache.CachePolicy.UPDATE_CACHE) {
                                RestApi.this.onSuccess(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        RestApi.this.onError(e.getMessage());
                    }
                } else if (b.containsKey("error")) {
                    RestApi.this.onError(b.getString("error"));
                } else if (b.containsKey("statusCodeError") && b.containsKey("statusCodeErrorNumber")) {
                    RestApi.this.onError("This email account does not exist");
                    RestApi.this.onStatusCodeError(b.getInt("statusCodeErrorNumber"), b.getString("statusCodeError"));
                } else {
                    RestApi.this.onError("Misconfigured code");
                    System.out.println("Misconfigured code");
                }

                RestApi.this.reset();
                RestApi.this.hideLoadingDialog();
            }
        });
    }

    public void setCachePolicy(int cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    public void reset() {
        this.loadingMessage = "Loading...";
        this.callback = null;
        this.errorCallback = null;
        this.endpoint = null;
        this.cachePolicy = RestCache.CachePolicy.IGNORE_CACHE;

        this.parameters = new ArrayList<NameValuePair>();
    }

    public void addParameter(String name, Object value) {
        this.parameters.add(new BasicNameValuePair(name, value.toString()));
    }

    public void setLoadingMessage(String message) {
        this.loadingMessage = message;
    }

    public void setLoadingMessage(int messageId) {
        this.loadingMessage = this.activity.getResources().getString(messageId);
    }

    public List<NameValuePair> getParameters() {
        return this.parameters;
    }

    public void showLoadingDialog() {

        if (this.loadingMessage != null) {
            this.activity.mUtility.ShowProgress(loadingMessage);
        }
    }

    public void cancelRequest() {
        this.rest.cancelRequest();
        this.hideLoadingDialog();
    }

    public void hideLoadingDialog() {
        // hide the loading progress bar which might be visible in the titlebar
        this.setProgressBarIndeterminateVisibility(false);
        this.activity.mUtility.HideProgress();
    }

    protected void setProgressBarIndeterminateVisibility(boolean visible) {
        this.activity.setProgressBarIndeterminateVisibility(false);
    }

    public void removeProgressDialog() {
        // hide the loading progress bar which might be visible in the titlebar
        this.setProgressBarIndeterminateVisibility(false);

        this.activity.mUtility.HideProgress();
    }

    public void setUserAgent(String agent) {
        this.rest.setUserAgent(agent);
    }

    public void acceptAllSslCertificates() {
        this.rest.acceptAllSslCertificates();
    }

    public RestApi setCallback(RestCallback callback) {
        this.callback = callback;
        return this;
    }

    public RestApi setErrorCallback(RestErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    public void onSuccess(Object obj) {
        if (this.callback != null) {
            this.callback.success(obj);
        } else {
            //Toast.makeText(this.activity, "RestApi Success, no callback", Toast.LENGTH_LONG).show();
        }
    }

    public void onStatusCodeError(int code, String data) {
        if (this.errorCallback != null) {
            this.errorCallback.error(data);
        } else {
            Toast.makeText(this.activity, data, Toast.LENGTH_LONG).show();
        }
    }

    public void onError(String message) {
        if (this.errorCallback != null) {
            this.errorCallback.error(message);
        } else {
            Toast.makeText(this.activity, message, Toast.LENGTH_LONG).show();
        }
    }

    protected void get(String url) {
        boolean gotCache = false;

        this.requestType = "get";

        this.endpoint = this.getEndpoint(url);

        if (this.cachePolicy == RestCache.CachePolicy.CACHE_THEN_NETWORK || this.cachePolicy == RestCache.CachePolicy.CACHE_ELSE_NETWORK) {
            if (RestCache.exists(this)) {
                String data = RestCache.get(this);
                try {
                    if (data.trim().substring(0, 1).equalsIgnoreCase("{")) {
                        Object returnObject = new JSONObject(data);
                        gotCache = true;
                        RestApi.this.onSuccess(returnObject);
                    } else if (data.trim().substring(0, 1).equalsIgnoreCase("[")) {
                        Object returnObject = new JSONArray(data);
                        gotCache = true;
                        RestApi.this.onSuccess(returnObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.cachePolicy == RestCache.CachePolicy.CACHE_ELSE_NETWORK && gotCache) {
            // no need to load anything, we got if from the cache, so all done
        } else {
            if (!gotCache && this.cachePolicy != RestCache.CachePolicy.UPDATE_CACHE) {
                this.showLoadingDialog();
            } else {
                // show loading progress bar in the titlebar
                this.setProgressBarIndeterminateVisibility(true);
            }

            this.rest.setData(this.parameters);
            this.rest.get(this.endpoint);
        }
    }

    protected void post(String url) {
        this.requestType = "post";

        this.endpoint = this.getEndpoint(url);

        this.showLoadingDialog();
        this.rest.setData(this.parameters);
        this.rest.post(this.endpoint);
    }

    protected void put(String url) {
        this.requestType = "put";

        this.endpoint = this.getEndpoint(url);

        this.showLoadingDialog();
        this.rest.setData(this.parameters);
        this.rest.put(this.endpoint);
    }

    protected void delete(String url) {
        this.requestType = "delete";

        this.endpoint = this.getEndpoint(url);

        this.showLoadingDialog();
        this.rest.delete(this.endpoint);
    }

    public String getEndpoint(String part) {
        return BASE_URL + part + this.urlSuffix;
    }

    public MatrixCursor jsonArrayToMatrixCursor(JSONArray data, String[] keys, String keyToBeId) {
        return this.jsonArrayToMatrixCursor(data, keys, keyToBeId, null);
    }

    public JSONObject getObjectFromCursor(JSONArray data, String key, long value) {
        JSONObject returnData = null;

        try {
            for (int i = 0; i < data.length(); i++) {
                returnData = data.getJSONObject(i);
                if (returnData.getInt(key) == value) {
                    return returnData;
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnData;
    }

    public MatrixCursor jsonArrayToMatrixCursor(JSONArray data, String[] keys, String keyToBeId, JSONObject filter) {
        MatrixCursor c = null;
        int i, j;
        MatrixCursor.RowBuilder row;

        if (data == null) {
            return c;
        }

        try {
            String[] keyAttributes = new String[keys.length];

			/*
            JSONObject first = data.getJSONObject(0);
			JSONArray keyArray = first.names();

			keyAttributes = new String[keyArray.length()];
			for(i=0; i<keyArray.length(); i++) {
				String key = keyArray.getString(i);
				if (key.equalsIgnoreCase(keyToBeId) == true) {
					key = "_id";
				}
			    keyAttributes[i] = key;
			}
			*/
            // copy the array, so when we change one of the keys to _id, we still have the original references
            System.arraycopy(keys, 0, keyAttributes, 0, keys.length);

            for (i = 0; i < keyAttributes.length; i++) {
                String key = keyAttributes[i];
                if (key.equalsIgnoreCase(keyToBeId)) {
                    key = "_id";
                    keyAttributes[i] = key;
                    break;
                }
            }

            if (filter == null) {
                c = new MatrixCursor(keyAttributes, data.length());
            } else {
                c = new MatrixCursor(keyAttributes);
            }

            for (j = 0; j < data.length(); j++) {
                JSONObject o = data.getJSONObject(j);
                if (this.checkFilter(o, filter)) {
                    row = c.newRow();
                    for (i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        if (o.has(key) && !o.isNull(key)) {
                            row.add(o.get(key));
                        } else {
                            row.add(null);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return c;
    }

    private boolean checkFilter(JSONObject data, JSONObject filter) throws JSONException {
        if (filter == null) {
            return true;
        }

        boolean ok = true;

        JSONArray names = filter.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String key = names.getString(i);

                if (key.contains(".")) {
                    StringTokenizer tokens = new StringTokenizer(key, ".");

                    JSONObject tempData = new JSONObject(data.toString());

                    String token = null;
                    while (tokens.hasMoreTokens()) {
                        token = tokens.nextToken();

                        // if this isn't the last one, keep getting the object
                        if (tokens.hasMoreTokens()) {
                            if (tempData.has(token) && !tempData.isNull(token)) {
                                tempData = tempData.getJSONObject(token);
                            } else {
                                ok = false;
                                break;
                            }
                        }
                    }

                    if (token != null) {
                        if (tempData.has(token)) {
                            if (!tempData.get(token).equals(filter.get(key))) {
                                ok = false;
                                break;
                            }
                        }
                    }
                } else if (data.has(key) && data.get(key) instanceof JSONArray) {
                    ok = false;

                    JSONArray array = data.getJSONArray(key);
                    for (int j = 0; j < array.length(); j++) {
                        if (array.get(j).equals(filter.get(key))) {
                            ok = true;
                            break;
                        }
                    }

                    break;
                } else {
                    if (data.has(key)) {
                        if (!data.get(key).equals(filter.get(key))) {
                            ok = false;
                            break;
                        }
                    } else {
                        ok = false;
                        break;
                    }
                }
            }
        }

        return ok;
    }

    public static JSONArray replaceObject(JSONArray data, JSONObject obj, String id) {
        JSONObject o;
        JSONArray newData = new JSONArray();

        try {
            for (int i = 0; i < data.length(); i++) {
                o = data.getJSONObject(i);
                if (o.getLong(id) != obj.getLong(id)) {
                    newData.put(o);
                } else {
                    newData.put(obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newData;
    }
}
