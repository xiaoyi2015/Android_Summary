package ac.p2p.app.activity.BaseClass;

/**
 * Created by zln on 15/12/30.
 */
public class BaseActivity extends FragmentActivity {

    protected String TAG;

    {
        TAG = getClass().getName();
    }

    private CommonTopBar commonTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commonTopBar = (CommonTopBar) findViewById(R.id.default_top_bar);

    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.appStop();

    }

    public void onResume() {
        super.onResume();
        try {
            MyApp.appResume();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public CommonTopBar getCommonTopBar() {
        return commonTopBar;
    }

    public void offLine() {
        //TODO
        finishAffinity();
    }

    //沉浸模式
    public void setImmerseMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        int result = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = this.getResources().getDimensionPixelSize(resourceId);
        }
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, result);

        //设置导航栏沉浸模式的颜色值
        textView.setBackgroundColor(Color.parseColor("#ffb400"));
        textView.setLayoutParams(lParams);
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
    }

    //无回调跳转，带Gson
    public void shortStartActivity(Class c, String... keyAndValue) {
        Intent intent = new Intent(this, c);
        int keyAndValueLength = keyAndValue.length;
        for (int i = 0; i < keyAndValueLength / 2; ++i) {
            intent.putExtra(keyAndValue[i * 2], keyAndValue[i * 2 + 1]);
        }
        intent.putExtra("activity", TAG);
        startActivity(intent);
    }

    //有回调跳转，带Gson
    public void shortStartActivityForResult(Class c, int requestCode, String... keyAndValue) {
        Intent intent = new Intent(this, c);
        int keyAndValueLength = keyAndValue.length;
        for (int i = 0; i < keyAndValueLength / 2; ++i) {
            intent.putExtra(keyAndValue[i * 2], keyAndValue[i * 2 + 1]);
        }
        intent.putExtra("activity", TAG);
        startActivityForResult(intent, requestCode);
    }

    //保证在布局下正常显示listview高度
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}
