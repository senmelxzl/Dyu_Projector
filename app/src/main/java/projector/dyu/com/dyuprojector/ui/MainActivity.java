package projector.dyu.com.dyuprojector.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import projector.dyu.com.dyuprojector.R;
import projector.dyu.com.dyuprojector.adapter.ApplicationsAdapter;
import projector.dyu.com.dyuprojector.util.ApplicationInfo;
import projector.dyu.com.dyuprojector.util.ToastUtil;
import projector.dyu.com.dyuprojector.util.UtilTools;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int ACTION_CLICK_TIME_THRESHOLD = 4000;
    private static final String DEFAULT_FAVORITES_PATH = "etc/favorites.xml";
    private static final String TAG_CLASS = "class";
    private static final String TAG_FAVORITE = "favorite";
    private static final String TAG_FAVORITES = "favorites";
    private static final String TAG_PACKAGE = "package";
    private static ArrayList<ApplicationInfo> mApplicationTypes;
    private static ArrayList<ApplicationInfo> mApplications;
    private static LinkedList<ApplicationInfo> mFavorites;
    private static boolean mHdmiEnabled = false;
    private static long mLastTime = 0L;
    private static int mRepeatTimes = 0;
    private boolean mBlockAnimation;
    private BatteryReceiver batteryReceiver = new BatteryReceiver();
    private BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
    private int batteryScale = 100;
    private int mCurrentType = -1;
    private GridView mGrid;
    private ApplicationsAdapter mApplicationsAdapter;
    private Animation mGridEntry;
    private Animation mGridExit;
    private LayoutAnimationController mHideLayoutAnimation;
    private LayoutAnimationController mShowLayoutAnimation;

    private View mHome, vApp_Container;
    private TextView type_title, not_match_app;
    private ImageView mIvAirLink;
    private ImageView mIvBusinessOffice;
    private ImageView mIvFileManager;
    private ImageView mIvMiracast;
    private ImageView mIvMoreApp;
    private ImageView mIvMovie;
    private ImageView mIvSocial;
    private ImageView mIvSetting;
    private ImageView mIvTv;
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        registerIntentReceivers();
        loadApplications(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("dyu", "Launcher onDestroy ");
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        if (mApplicationsReceiver != null) {
            unregisterReceiver(mApplicationsReceiver);
            mApplicationsReceiver = null;
        }
        if (mApplications != null) {
            mApplications.clear();
            mApplications = null;
        }
        if (mApplicationTypes != null) {
            mApplicationTypes.clear();
            mApplicationTypes = null;
        }
    }

    protected void onResume() {
        super.onResume();
        Log.d("dyu", "Launcher onResume ");
        hideBottomUIMenu();
        if (mGrid.getVisibility() == View.VISIBLE) {
            showApplications(false);
            mHome.setVisibility(View.VISIBLE);
        }
    }

    /**
     * view init!
     */
    private void initView() {
        mIvBusinessOffice = findViewById(R.id.iv_business_office);
        mIvFileManager = findViewById(R.id.iv_file_manager);
        mIvSocial = findViewById(R.id.iv_social);
        mIvMovie = findViewById(R.id.iv_movie);
        mIvMiracast = findViewById(R.id.iv_miracast);
        mIvTv = findViewById(R.id.iv_tv);
        mIvAirLink = findViewById(R.id.iv_air_link);
        mIvSetting = findViewById(R.id.iv_setting);
        mIvMoreApp = findViewById(R.id.iv_more_app);
        mHome = findViewById(R.id.iv_home);
        mToast = Toast.makeText(getApplicationContext(), getString(R.string.click_hdmi_too_fast), Toast.LENGTH_SHORT);
        mGrid = findViewById(R.id.all_apps);
        vApp_Container = findViewById(R.id.all_apps_container);
        type_title = findViewById(R.id.type_title);
        not_match_app = findViewById(R.id.not_match_app);
        mGridEntry = AnimationUtils.loadAnimation(this, R.anim.grid_entry);
        mGridExit = AnimationUtils.loadAnimation(this, R.anim.grid_exit);
    }

    /**
     * init widget event
     */
    private void initEvent() {
        mIvBusinessOffice.setOnClickListener(this);
        mIvFileManager.setOnClickListener(this);
        mIvSocial.setOnClickListener(this);
        mIvMovie.setOnClickListener(this);
        mIvMiracast.setOnClickListener(this);
        mIvTv.setOnClickListener(this);
        mIvAirLink.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mIvMoreApp.setOnClickListener(this);
    }

    /**
     * register all receivers
     */
    private void registerIntentReceivers() {
        IntentFilter pkgIntentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        pkgIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        pkgIntentFilter.addDataScheme("package");
        registerReceiver(mApplicationsReceiver, pkgIntentFilter);

        IntentFilter batteryIntentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        registerReceiver(batteryReceiver, batteryIntentFilter);
    }

    @Override
    public void onClick(View v) {
        Intent localIntentC = new Intent();
        switch (v.getId()) {
            case R.id.iv_setting:
                if (!UtilTools.checkApkExist(this, "com.android.settings")) {
                    new ToastUtil(this).AppNotExist();
                    break;
                }
                localIntentC.setClassName("com.android.settings", "com.android.settings.Settings");
                localIntentC.setAction("android.settings.SETTINGS");
                localIntentC.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntentC);
                break;
            case R.id.iv_miracast:
                if (!UtilTools.checkApkExist(this, "com.hpplay.happyplay.aw")) {
                    new ToastUtil(this).AppNotExist();
                    break;
                }
                localIntentC.setClassName("com.hpplay.happyplay.aw", "com.hpplay.happyplay.aw.WelcomeActivity");
                localIntentC.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntentC);
                break;
            case R.id.iv_air_link:
                if (batteryScale < 15 && !isAcCharge()) {
                    showToast(batteryScale);
                }
                Intent localIntentHdmi = new Intent();
                mHdmiEnabled = isHdmiEnable();
                if (mHdmiEnabled) {
                    localIntentHdmi.setAction("android.intent.action.ACTION_CLOSE_HDMI");
                    mHdmiEnabled = false;
                } else {
                    localIntentHdmi.setAction("android.intent.action.ACTION_OPEN_HDMI");
                    mHdmiEnabled = true;
                }
                sendBroadcast(localIntentHdmi);
                break;
            case R.id.iv_file_manager:
                if (!UtilTools.checkApkExist(this, "com.mediatek.filemanager")) {
                    new ToastUtil(this).AppNotExist();
                    break;
                }
                localIntentC.setClassName("com.mediatek.filemanager", "com.mediatek.filemanager.FileManagerOperationActivity");
                localIntentC.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntentC);
                break;
            case R.id.iv_business_office:
                if (!UtilTools.checkApkExist(this, "cn.wps.moffice_i18n_TV")) {
                    new ToastUtil(this).AppNotExist();
                    break;
                }
                localIntentC.setClassName("cn.wps.moffice_i18n_TV", "cn.wps.moffice.documentmanager.PreStartActivity");
                localIntentC.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntentC);
                break;
            case R.id.iv_more_app:
                showApplicationByType(0);
                break;
            case R.id.iv_movie:
                showApplicationByType(1);
                break;
            case R.id.iv_tv:
                showApplicationByType(2);
                break;
            case R.id.iv_social:
                showApplicationByType(3);
                break;
            default:
                break;
        }
    }

    /**
     * dispatch key event
     *
     * @param paramKeyEvent
     * @return
     */
    public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
        if (paramKeyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && paramKeyEvent.getAction() != KeyEvent.ACTION_UP) {
            if (mHideLayoutAnimation == null) {
                mHideLayoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.hide_applications);
            }
            mGridExit.setAnimationListener(new HideGrid());
            mGrid.startAnimation(mGridExit);
            vApp_Container.setVisibility(View.INVISIBLE);
            mHome.setVisibility(View.VISIBLE);
            return true;
        }
        return super.dispatchKeyEvent(paramKeyEvent);
    }

    /**
     * show app by type
     *
     * @param paramInt
     */
    private void showApplicationByType(int paramInt) {
        if (mCurrentType != paramInt) {
            mCurrentType = paramInt;
            bindApplications();
        }

        if (mShowLayoutAnimation == null) {
            mShowLayoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.show_applications);
        }
        mGridEntry.setAnimationListener(new ShowGrid());
        mGrid.startAnimation(mGridEntry);
        if (vApp_Container.getVisibility() != View.VISIBLE) {
            showApplications(true);
            type_title.setText(getAppsTypeTitle(paramInt));
            type_title.setVisibility(View.VISIBLE);
        } else {
            showApplications(false);
        }

    }

    /**
     * get apps type title
     *
     * @param paramInt
     * @return
     */
    private int getAppsTypeTitle(int paramInt) {
        if (mApplicationTypes == null || mApplicationTypes.size() <= 0) {
            not_match_app.setVisibility(View.VISIBLE);
            not_match_app.setText(R.string.not_match_app_str);
        } else {
            not_match_app.setVisibility(View.GONE);
        }
        int type_string_id = R.string.apps_title_all;
        switch (paramInt) {
            case 0:
                type_string_id = R.string.apps_title_all;
                break;
            case 1:
                type_string_id = R.string.apps_title_movie;
                break;
            case 2:
                type_string_id = R.string.apps_title_game;
                break;
            case 3:
                type_string_id = R.string.apps_title_social;
                break;
            default:
                break;
        }
        return type_string_id;
    }

    /**
     * load all apps
     *
     * @param paramBoolean
     */
    private void loadApplications(boolean paramBoolean) {
        if ((paramBoolean) && (mApplications != null)) {
            mApplications.clear();
        }
        PackageManager localPackageManager = getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List localList = localPackageManager.queryIntentActivities(localIntent, 0);
        int appListSize = localList.size();
        if (appListSize <= 0) {
            return;
        }
        Collections.sort(localList, new ResolveInfo.DisplayNameComparator(localPackageManager));
        Log.i(TAG, "localList size is " + localList.size());
        if (appListSize > 0) {
            mApplications = new ArrayList(appListSize);
            ApplicationInfo localApplicationInfo;
            ResolveInfo localResolveInfo;
            for (int j = 0; j < appListSize; j++) {
                localApplicationInfo = new ApplicationInfo();
                localResolveInfo = (ResolveInfo) localList.get(j);
                if (UtilTools.isNeedAddtoApplication(localResolveInfo.activityInfo.applicationInfo.packageName)) {
                    localApplicationInfo.title = localResolveInfo.loadLabel(localPackageManager);
                    localApplicationInfo.setActivity(new ComponentName(localResolveInfo.activityInfo.applicationInfo.packageName, localResolveInfo.activityInfo.name), Intent.FLAG_ACTIVITY_NEW_TASK);
                    localApplicationInfo.icon = localResolveInfo.activityInfo.loadIcon(localPackageManager);
                    localApplicationInfo.appType = UtilTools.getAppType(localResolveInfo.activityInfo.applicationInfo.packageName, localResolveInfo.activityInfo.applicationInfo.className);
                    mApplications.add(localApplicationInfo);
                    Log.i(TAG, "loadApplications app type:" + localApplicationInfo.appType + " title:" + localApplicationInfo.title);
                }
            }
            mApplicationsAdapter = new ApplicationsAdapter(this, mApplications, mCurrentType);
            mGrid.setAdapter(mApplicationsAdapter);
            mGrid.setSelection(0);
        }

    }

    /**
     * bind apps
     */
    private void bindApplications() {
        Log.i(TAG, "bindApplications :bind apps start!");
        int appSize = mApplications.size();
        if (mApplicationTypes != null) {
            mApplicationTypes.clear();
        }
        mApplicationTypes = new ArrayList();
        Log.i(TAG, "bindApplications :bind apps size: " + appSize);
        for (int j = 0; j < appSize; j++) {
            ApplicationInfo localApplicationInfo = mApplications.get(j);
            if (localApplicationInfo.appType == mCurrentType) {
                mApplicationTypes.add(localApplicationInfo);
            }
        }
        Log.i(TAG, "mAppTypelications :bind type apps size: " + mApplicationTypes.size());
        mApplicationsAdapter.refresh(mApplicationTypes, mCurrentType);
        mGrid.setOnItemClickListener(new ApplicationLauncher());
        Log.i(TAG, "bindApplications :bind apps end!");
    }

    /**
     * show all apps
     *
     * @param paramBoolean
     */
    private void showApplications(boolean paramBoolean) {
        if (paramBoolean) {
            vApp_Container.setVisibility(View.VISIBLE);
            mHome.setVisibility(View.GONE);
        } else {
            vApp_Container.setVisibility(View.INVISIBLE);
            mHome.setVisibility(View.VISIBLE);
        }
    }

    /**
     * showToast to pop up a message
     *
     * @param paramInt
     */
    private void showToast(int paramInt) {
        if (paramInt <= 15) {
            if (mToast != null) {
                mToast.setText(getResources().getString(R.string.low_battery_text));
                mToast.show();
            }
            Intent localIntent = new Intent();
            localIntent.setAction("android.intent.action.ACTION_CLOSE_HDMI");
            mHdmiEnabled = false;
            sendBroadcast(localIntent);
        }
    }

    /**
     * hide bottom UI Menu
     */
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.GONE);
        }
    }

    /**
     * check Ac charge type
     *
     * @return isAcCharge
     */
    private boolean isAcCharge() {
        boolean isAcCharge = false;
        Intent localIntent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (localIntent == null) {
            return false;
        }
        int status = localIntent.getIntExtra("status", -1);
        if ((status != 2) && (status != 5)) {
            if (localIntent.getIntExtra("plugged", -1) != 1)
                Log.d("guohuajun", "isAcCharge = " + isAcCharge);
            isAcCharge = true;
        }
        return isAcCharge;
    }

    /**
     * check HDMI status
     *
     * @return HdmiEnable
     */
    private boolean isHdmiEnable() {
        boolean HdmiEnable = false;
        if (Settings.System.getInt(getContentResolver(), "hdmi_enable_status", 0) != 0) {
            HdmiEnable = true;
        }
        return HdmiEnable;
    }

    /**
     * Application intent
     */

    private class ApplicationLauncher implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView paramAdapterView, View paramView, int paramInt, long paramLong) {
            ApplicationInfo localApplicationInfo = mApplicationTypes.get(paramInt);
            startActivity(localApplicationInfo.intent);
        }
    }

    /**
     * app change receiver
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        public void onReceive(Context paramContext, Intent paramIntent) {
            loadApplications(false);
            bindApplications();
        }
    }

    /**
     * battery listener
     */
    private class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context paramContext, Intent paramIntent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(paramIntent.getAction())) {
                int i = paramIntent.getIntExtra("level", 0);
                int j = paramIntent.getIntExtra("scale", 100);
                batteryScale = (i * 100 / j);
            }
        }
    }

    /**
     * show grid items
     */
    private class ShowGrid implements Animation.AnimationListener {

        public void onAnimationEnd(Animation paramAnimation) {
            mBlockAnimation = false;
        }

        public void onAnimationRepeat(Animation paramAnimation) {
        }

        public void onAnimationStart(Animation paramAnimation) {
        }
    }

    /**
     * hide grid items
     */
    private class HideGrid implements Animation.AnimationListener {

        public void onAnimationEnd(Animation paramAnimation) {
            mBlockAnimation = false;
        }

        public void onAnimationRepeat(Animation paramAnimation) {
        }

        public void onAnimationStart(Animation paramAnimation) {
        }
    }
}

