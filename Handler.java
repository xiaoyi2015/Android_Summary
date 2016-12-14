    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 8:
                    if(builder.isShowing()) {
                        MyApp.getApp().showToast("请求超时，请检查您的网络连接");
                        dismissWaitProgress();
                    }
                    return true;
                default:
                    Log.v(TAG, "error");
                    return false;
            }
        }
    });

    private int time;

    public void startTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                time = 8;
                while(time > -1){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time--;
                }
                Message message = new Message();
                message.what = 8;
                handler.sendMessage(message);
            }
    
        }).start();
    }