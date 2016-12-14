package ac.p2p.app;

/**
 * Created by zln on 15/12/30.
 */
public class MyApp extends Application {

    private static MyApp INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        //TODO Init()
    }

    public static MyApp getApp() {
        return INSTANCE;
    }

    //第一次进入app，不检测
    private static boolean appActive = true;

    public static boolean isAppActive() {
        return appActive;
    }

    public static void setAppActive(boolean appActive) {
        MyApp.appActive = appActive;
    }

    public static Context context() {
        return getApp().getApplicationContext();
    }

    //统一的提示格式
    private Toast toast;
    /**
     * call for show toast
     *
     * @param stringId string resource id
     */
    public void showToast(int stringId) {
        showToast(getString(stringId));
    }

    /**
     * call for show toast
     *
     * @param string string will be show
     */
    public void showToast(final String string) {
        toast = Toast.makeText(MyApp.this, string, Toast.LENGTH_SHORT);
        toast.show();
    }  

    public static boolean isAppOnForeground() {

        ActivityManager activityManager = (ActivityManager) context().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = MyApp.context().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    public static void appResume() throws UnsupportedEncodingException, JSONException {
        if (MyApp.isAppOnForeground() && !isAppActive()) {
            setAppActive(true);
            enterForeground();
        }
    }

    public static void appStop() {
        if (!MyApp.isAppOnForeground()) {
            setAppActive(false);
            enterBackground();
        }
    }

    public static void enterForeground() {
        Log.v("zln", "进入前台");
    }

    private static void enterBackground() {
        Log.v("zln", "进入后台");
    }

}
