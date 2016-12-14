package ac.p2p.app.activity.BaseClass;

public class MainActivity extends BaseActivity {

    private CustomViewPager pager;

    BaseFragment[] fragments = new BaseFragment[]{
            new Fragment1(),
            new Fragment2(),
            new Fragment3(),
            new Fragment4()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        for (BaseFragment bf : fragments) {
            bf.setActivity(this);
        }
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //存储一个判断是否是第一次启动的值
        SharedPreferences.Editor editor = getSharedPreferences("data",
                MODE_PRIVATE).edit();
        editor.putBoolean("isFirstLaunch", false);
        editor.commit();

        setImmerseMode();
        initTabIndicator();

    }
    @Override
    public void onResume() {
        super.onResume();

    }

    private TabIndicator[] tabIndicators = null;

    private void initTabIndicator() {

        pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setScrollable(false);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }

        });
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeIndicator(position);
                fragments[position].setTopBar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        LinearLayout tabs = (LinearLayout) findViewById(R.id.tabs);
        tabIndicators = new TabIndicator[]{(TabIndicator) tabs.getChildAt(0),
                (TabIndicator) tabs.getChildAt(1),
                (TabIndicator) tabs.getChildAt(2),
                (TabIndicator) tabs.getChildAt(3)};
        tabIndicators[0].select();
        fragments[0].setTopBar();

        //根据是否登录判断浏览权限
        if (!MyApp.getApp().getIsLogin()) {
            tabIndicators[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(0);
                }
            });
            tabIndicators[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(1);
                }
            });
            tabIndicators[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shortStartActivity(LoginForPwdActivity.class);

                }
            });
            tabIndicators[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shortStartActivity(LoginForPwdActivity.class);
                }
            });
        } else {
            for (int i = 0; i < tabIndicators.length; ++i) {
                final int position = i;
                tabIndicators[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pager.setCurrentItem(position);
                    }
                });
            }
        }

    }

    public void changeIndicator(int position) {
        if (tabIndicators == null) {
            return;
        }
        for (TabIndicator tabIndicator : tabIndicators) {
            tabIndicator.unSelect();
        }
        tabIndicators[position].select();
    }

    long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 判断间隔时间 大于2秒就退出应用
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                exitTime = System.currentTimeMillis();
            } else {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
