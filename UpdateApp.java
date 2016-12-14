        
        //onCreate()
        notiManage = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        note = new Notification();
        /***************** start **********************/
        // 增加空意图 消除 IllegalArgumentException
        Intent intent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        note.contentIntent = contentIntent;
        /****************** end *********************/

        try {
            getNewVersionApk();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    private NotificationManager notiManage;
    private Notification note;
    private RemoteViews remoteViews;
    private int NOTIFY_ID = 16;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x15:
                    //下载成功
                    notiManage.cancel(NOTIFY_ID);
                    final String newApkPath = (String) msg.obj;
                    MyApp.getApp().showToast("下载成功，请确认安装");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            File file = new File(newApkPath);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.fromFile(file),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                            MainActivity.this.finish();
                            //overridePendingTransition(R.anim.lening_common_all_activity_anim_enter,
                                   // R.anim.lening_common_all_activity_anim_exit);
                            android.os.Process.killProcess(android.os.Process
                                    .myPid());
                        }
                    }, 2500);
                    break;
                case 0x16:
                    //下载失败
                    notiManage.cancel(NOTIFY_ID);
                    MyApp.getApp().showToast("下载失败，请检查网络");
                    break;
                case 0x17:
                    //下载开始
                    remoteViews = new RemoteViews(getPackageName(),
                            R.layout.not_download_layout);
                    note.contentView = remoteViews;
                    note.flags = Notification.FLAG_ONGOING_EVENT;
                    note.tickerText = "正在下载";
                    note.icon = R.mipmap.ic_launcher;
                    notiManage.notify(NOTIFY_ID, note);
                    break;
                case 0x18:
                    //下载中
                    String name = (String) msg.obj;
                    int current = msg.arg1;
                    note.contentView.setTextViewText(R.id.notificationTitle,
                            "版本更新：" + name);
                    note.contentView.setProgressBar(R.id.notificationProgress, 100,
                            current, false);
                    note.contentView.setTextViewText(R.id.notificationPercent,
                            "    " + current + "%");
                    notiManage.notify(NOTIFY_ID, note);
                    break;
            }
        }
    };

    private void getNewVersionApk() throws UnsupportedEncodingException, JSONException {
        final int version = IsAppInstallUtil.getVersionCode(MainActivity.this);
        HttpClient.checkAppUpdate(String.valueOf(version), new HttpClient.JsonResponseHandler() {
            @Override
            public void onSuccess(ArrayList<SingleResponse> response) throws UnsupportedEncodingException, JSONException {
                VersionResponse versionResponse = new Gson().fromJson(response.get(0).getResp(), VersionResponse.class);
                Log.v("xqq","wode "+ versionResponse.getIsupdate());
                if (versionResponse != null) {
                    if (versionResponse.getIsupdate() == 1) {
                        final String version = versionResponse.getVersion();
                        final String updatemessage = versionResponse.getUpdatemessage();
                        final String updateurl = versionResponse.getUpdateurl();
                        //正常更新
                        if (versionResponse.getUpdatetype() == 0) {
                            CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
                            builder.setTitle("版本更新")
                                    .setMessage(updatemessage)
                                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //确定更新版本，开始下载并安装
                                            downloadNewVersion(updateurl);
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.create().show();
                        }
                        //强制更新
                        else if (versionResponse.getUpdatetype() == 1) {

                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    private void downloadNewVersion(String url) {
        //发送handler消息：0x15->下载成功，0x16->下载失败，0x17->下载开始，0x18->下载中
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final String now = sdf.format(date);
        final String apkPath = Environment.getExternalStorageDirectory().toString() + File.separator
                + ".qeebu$longman$module" + File.separator + now + ".apk";
        //LogUtil.e("@@@@", "apkPath-->" + apkPath);
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.download(url, apkPath, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Message message = new Message();
                message.what = 0x15;
                message.obj = apkPath;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Message message = new Message();
                message.what = 0X16;
                handler.sendMessage(message);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                Message message = new Message();
                message.what = 0X18;
                int n = (int) ((100 * current) / total);
                message.arg1 = n;
                message.obj = now;
                handler.sendMessage(message);
            }

            @Override
            public void onStart() {
                super.onStart();
                Message message = new Message();
                message.what = 0X17;
                message.obj = now;
                handler.sendMessage(message);
            }
        });
    }
