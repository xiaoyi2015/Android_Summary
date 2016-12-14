package ac.p2p.app.network;
/**
 * Created by zln on 16/1/1.
 */
public class HttpClient {

    //public static final String BASE_URL = "http://tapi.ebank99.com/api";
    public static final String BASE_URL;
    public static final String FILE_URL;
    
    private static AsyncHttpClient asyncHttpClient;

    public static AsyncHttpClient getAsyncHttpClient() {
        if (asyncHttpClient == null) {
            asyncHttpClient = new AsyncHttpClient(getSchemeRegistry());
        }
        return asyncHttpClient;
    }

    //加入https许可协议
    public static SchemeRegistry getSchemeRegistry() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            return registry;
        } catch (Exception e) {
            return null;
        }
    }

    public interface JsonResponseHandler {
        void onSuccess(ArrayList<SingleResponse> response) throws UnsupportedEncodingException, JSONException;

        void onFailure(Throwable throwable);
    }

    private static JSONObject reqWith(String name, String version, JSONObject paras) throws JSONException, UnsupportedEncodingException {
        return new JSONObject().put(NET_KEY_NAME, name).put(NET_KEY_PARAMS, paras).put(NET_KEY_VERSION, version);
    }

    private static void sendRequest(final JSONArray requestArray, final JsonResponseHandler handler) throws JSONException {
        sendRequest(requestArray, handler, false);
    }

    private static void sendRequest(final JSONArray requestArray, final JsonResponseHandler handler, boolean isOpen) throws JSONException {
        if (!MyApp.getApp().isNetWorkConnected()) {
            handler.onFailure(new Throwable());
            return;
        }

        JSONObject request = new JSONObject();
        request.put(NET_KEY_COUNT, requestArray.length());
        if (isOpen) {
            request.put(NET_KEY_SESSIONKEY, "");
            request.put(NET_KEY_UID, "");
        } else {
            request.put(NET_KEY_SESSIONKEY, MyApp.getApp().getSessionKey());
            request.put(NET_KEY_UID, MyApp.getApp().getUserDataManager().getMyUser().getUserid());
        }
        request.put(NET_KEY_SOURCE, NET_VALUE_SOURCE);
        request.put(NET_KEY_CLIVER, NET_VALUE_CLIVER);
        request.put(NET_KEY_REQS, requestArray);
        RequestParams params = new RequestParams();
        String reqString = request.toString();
        Log.v("zln", "send request :" + reqString);
        params.put("c", reqString);

        BaseJsonHttpResponseHandler<CommonResponse> thisHandler = new BaseJsonHttpResponseHandler<CommonResponse>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, CommonResponse response) {
                if (response.getSuccesscount() == requestArray.length() && response.getFailcount() == 0) {
                    if (handler != null) {
                        if (response.getResps() != null) {
                            try {
                                handler.onSuccess(response.getResps());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.v("zln", "response is null");
                        }
                    } else {
                        Log.v("zln", "handler is null");
                    }
                } else {
                    handler.onFailure(new Throwable());
                    onFailure(statusCode, headers, rawJsonResponse, null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, CommonResponse errorResponse) {
                if (errorResponse == null) {
                    return;
                }

                if ("1006".equals(errorResponse.getResult())) {
                    MyApp.getApp().showToast(errorResponse.getResult());
                    BaseActivity baseActivity = new BaseActivity();
                    baseActivity.offLine();
                }

                if (!TextUtils.isEmpty(errorResponse.getMsg())) {
                    MyApp.getApp().showToast(errorResponse.getMsg());
                    return;
                }
                for (int i = 0; i < errorResponse.getResps().size(); i++) {
                    if (1006 == (errorResponse.getResps().get(i).getResult())) {
                        MyApp.getApp().showToast(errorResponse.getResps().get(i).getResult());
                        BaseActivity baseActivity = new BaseActivity();
                        baseActivity.offLine();
                    } else {
                        if (!TextUtils.isEmpty(errorResponse.getResps().get(i).getMsg())) {
                            MyApp.getApp().showToast(errorResponse.getResps().get(i).getMsg());
                            return;
                        }
                    }
                }

                if (handler != null) {
                    handler.onFailure(throwable);
                }

            }


            @Override
            protected CommonResponse parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                Log.v("zln", "response rawJsonData:" + rawJsonData.replace("\n", ""));
                try {
                    return new Gson().fromJson(rawJsonData, CommonResponse.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        asyncHttpClient = getAsyncHttpClient();
        if (reqString.contains(".post")) {
            asyncHttpClient.post(BASE_URL, params, thisHandler);
        } else {
            asyncHttpClient.get(BASE_URL, params, thisHandler);
        }
    }

    //上传组图，单独根据API构造，调用时自己写Callback
    public static void uploadImage(ArrayList<File> file, Handler handler) throws IOException, JSONException {

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"resourceid\""),
                        RequestBody.create(null, ""))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"resourcekey\""),
                        RequestBody.create(null, ConstantKey.NET_KEY_LOAN))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"filetype\""),
                        RequestBody.create(null, "img"))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"source\""),
                        RequestBody.create(null, NET_VALUE_SOURCE))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"uid\""),
                        RequestBody.create(null, MyApp.getApp().getUserDataManager().getMyUser().getUserid()));
        for (int i = 0; i < file.size(); i++) {
            multipartBody.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"files[]\"; filename=\"test.jpg\""),
                    RequestBody.create(MediaType.parse("image/jpeg"), file.get(i)));
        }

        RequestBody requestBody = multipartBody.build();
        Request request = new Request.Builder()
                .header("Content-Type", "multipart/form-data")
                .url(FILE_URL)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            CommonResponse commonResponse = new Gson().fromJson(response.body().string(), CommonResponse.class);
            if (commonResponse.getSuccesscount() == 1) {
                if (commonResponse.getResps().get(0).getResp() != null) {
                    ImageListResponse imageListResponse = new Gson().fromJson(commonResponse.getResps().get(0).getResp(), ImageListResponse.class);
                    if (imageListResponse.getFilecount() != 0) {
                        ArrayList<String> urlList = new ArrayList<>();
                        for (int i = 0; i < imageListResponse.getFiles().size(); i++) {
                            urlList.add(imageListResponse.getFiles().get(i).getUrl());
                        }
                        Message message = Message.obtain();
                        message.obj = urlList;
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {
                        MyApp.getApp().showToast("No file Url");

                    }
                } else {
                    MyApp.getApp().showToast("No file Url");
                }
            } else {
                MyApp.getApp().showToast(commonResponse.getMsg());
            }

        } else {
            Log.v("zln", "upload failed, response:" + response.toString());
        }
    }

}
