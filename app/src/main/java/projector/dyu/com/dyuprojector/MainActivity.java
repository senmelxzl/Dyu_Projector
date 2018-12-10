package projector.dyu.com.dyuprojector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int ACTION_CLICK_TIME_THRESHOLD = 4000;
    private static final String DEFAULT_FAVORITES_PATH = "etc/favorites.xml";
    private static final String TAG_CLASS = "class";
    private static final String TAG_FAVORITE = "favorite";
    private static final String TAG_FAVORITES = "favorites";
    private static final String TAG_PACKAGE = "package";
    private static ArrayList<ApplicationInfo> mAppTypelications;
    private static ArrayList<ApplicationInfo> mApplications;
    private static LinkedList<ApplicationInfo> mFavorites;
    private static boolean mHdmiEnabled = false;
    private static long mLastTime = 0L;
    private static int mRepeatTimes = 0;
    private BatteryReceiver batteryReceiver = null;
    private int batteryscale = 100;
    private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
    private boolean mBlockAnimation;
    private int mCurrentType = -1;
    private GridView mGrid;
    private Animation mGridEntry;
    private Animation mGridExit;
    private LayoutAnimationController mHideLayoutAnimation;
    private LayoutAnimationController mShowLayoutAnimation;

    private View mHome;
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
        bindApplications();
        bindFavorites(true);
        mGridEntry = AnimationUtils.loadAnimation(this, R.anim.grid_entry);
        mGridExit = AnimationUtils.loadAnimation(this, R.anim.grid_exit);
        mGrid.setOnItemClickListener(new ApplicationLauncher());
        mToast = Toast.makeText(getApplicationContext(), getString(R.string.click_hdmi_too_fast), Toast.LENGTH_SHORT);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("dyu", "Launcher onDestroy ");
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
    }

    protected void onResume() {
        super.onResume();
        Log.d("dyu", "Launcher onResume ");
        hideBottomUIMenu();
        if (mGrid.getVisibility() == View.VISIBLE) {
            hideApplications();
            mHome.setVisibility(View.VISIBLE);
        }
    }

    private class ApplicationLauncher implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView paramAdapterView, View paramView, int paramInt, long paramLong) {
            ApplicationInfo localApplicationInfo = (ApplicationInfo) paramAdapterView.getItemAtPosition(paramInt);
            startActivity(localApplicationInfo.intent);
        }
    }

    private class ShowGrid implements Animation.AnimationListener {
        public void onAnimationEnd(Animation paramAnimation) {
            mBlockAnimation = false;
        }

        public void onAnimationRepeat(Animation paramAnimation) {
        }

        public void onAnimationStart(Animation paramAnimation) {
        }
    }

    private class ShowApplications implements View.OnClickListener {
        public void onClick(View paramView) {
            if (mCurrentType != -1) {
                mCurrentType = -1;
                bindApplications();
            }
            if (mGrid.getVisibility() != View.VISIBLE) {
                mHome.setVisibility(View.GONE);
                showApplications(true);
            }
            hideApplications();
            mHome.setVisibility(View.VISIBLE);
        }
    }

    private class HideGrid implements Animation.AnimationListener {

        public void onAnimationEnd(Animation paramAnimation) {
            mBlockAnimation = false;
        }

        public void onAnimationRepeat(Animation paramAnimation) {
        }

        public void onAnimationStart(Animation paramAnimation) {
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
     * show all apps
     *
     * @param paramBoolean
     */
    private void showApplications(boolean paramBoolean) {
        if (mBlockAnimation) ;
        while (true) {
            mBlockAnimation = true;
            if (mShowLayoutAnimation == null)
                mShowLayoutAnimation = AnimationUtils.loadLayoutAnimation(MainActivity.this, R.anim.show_applications);
            if (paramBoolean) {
                mGridEntry.setAnimationListener(new ShowGrid());
                mGrid.startAnimation(mGridEntry);
            }
            mGrid.setVisibility(View.VISIBLE);
            if (paramBoolean)
                mBlockAnimation = false;
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
        mGrid = findViewById(R.id.all_apps);
        mHome = findViewById(R.id.iv_home);
    }

    private void initEvent() {
        mIvBusinessOffice.setOnClickListener(this);
        mIvFileManager.setOnClickListener(this);
        mIvSocial.setOnClickListener(this);
        mIvMovie.setOnClickListener(this);
        mIvMiracast.setOnClickListener(this);
        mIvTv.setOnClickListener(this);
        mIvAirLink.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mIvMoreApp.setOnClickListener(new ShowApplications());
    }

    @Override
    public void onClick(View v) {
        Intent localIntent1 = new Intent();
        switch (v.getId()) {
            case R.id.iv_setting:
                localIntent1.setClassName("com.android.settings", "com.android.settings.Settings");
                localIntent1.setAction("android.settings.SETTINGS");
                localIntent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntent1);
                break;
            case R.id.iv_miracast:
                localIntent1.setClassName("com.hpplay.happyplay.aw", "com.hpplay.happyplay.aw.WelcomeActivity");
                localIntent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntent1);
                break;
            case R.id.iv_air_link:
                if (batteryscale < 15 && !isAcCharge()) {
                    showToast(batteryscale);
                }
                Intent localIntent2 = new Intent();
                mHdmiEnabled = isHdmiEnable();
                if (mHdmiEnabled) {
                    localIntent2.setAction("android.intent.action.ACTION_CLOSE_HDMI");
                    mHdmiEnabled = false;
                } else {
                    localIntent2.setAction("android.intent.action.ACTION_OPEN_HDMI");
                    mHdmiEnabled = true;
                }
                sendBroadcast(localIntent2);
                break;
            case R.id.iv_file_manager:
                localIntent1.setClassName("com.mediatek.filemanager", "com.mediatek.filemanager.FileManagerOperationActivity");
                localIntent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntent1);
                break;
            case R.id.iv_business_office:
                localIntent1.setClassName("cn.wps.moffice_i18n_TV", "cn.wps.moffice.documentmanager.PreStartActivity");
                localIntent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(localIntent1);
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

    private static void beginDocument(XmlPullParser paramXmlPullParser, String paramString)
            throws XmlPullParserException, IOException {
        int i;
        do
            i = paramXmlPullParser.next();
        while ((i != 2) && (i != 1));
        if (i != 2)
            throw new XmlPullParserException("No start tag found");
        if (!paramXmlPullParser.getName().equals(paramString))
            throw new XmlPullParserException("Unexpected start tag: found " + paramXmlPullParser.getName() + ", expected " + paramString);
    }

    private void showApplicationByType(int paramInt) {
        if (mCurrentType != paramInt) {
            mCurrentType = paramInt;
            bindApplications();
        }
        if (mGrid.getVisibility() != View.VISIBLE) {
            mHome.setVisibility(View.GONE);
            showApplications(true);
        }
        hideApplications();
        this.mHome.setVisibility(View.VISIBLE);

    }

    private void bindApplications() {
        if (mGrid == null) {
            mGrid = ((GridView) findViewById(R.id.all_apps));
        }
        int appsize = mApplications.size();
        if (mAppTypelications == null) {
            mAppTypelications = new ArrayList(appsize);
            mAppTypelications.clear();
        }
        for (int j = 0; j < appsize; j++) {
            ApplicationInfo localApplicationInfo = (ApplicationInfo) mApplications.get(j);
            if ((localApplicationInfo.appType != this.mCurrentType) && (this.mCurrentType != -1)) {
                mAppTypelications.add(localApplicationInfo);
            }
        }
        mGrid.setAdapter(new ApplicationsAdapter(this, mAppTypelications));
        mGrid.setSelection(0);
    }

    /**
     * bind favorite apps
     *
     * @param paramBoolean
     */
    private void bindFavorites(boolean paramBoolean) {
        if ((!paramBoolean) || (mFavorites == null)) {
            if (mFavorites != null) {
                mFavorites = new LinkedList();
                mFavorites.clear();
            }
        }
        File localFile = new File(Environment.getRootDirectory(), "etc/favorites.xml");
        FileReader localFileReader;
        Intent localIntent;
        PackageManager localPackageManager;
        try {
            localFileReader = new FileReader(localFile);
            localIntent = new Intent("android.intent.action.MAIN", null);
            localIntent.addCategory("android.intent.category.LAUNCHER");
            localPackageManager = getPackageManager();
            XmlPullParser localXmlPullParser = Xml.newPullParser();
            localXmlPullParser.setInput(localFileReader);
            beginDocument(localXmlPullParser, "favorites");
            nextElement(localXmlPullParser);
            if ("favorite".equals(localXmlPullParser.getName())) ;
            {
                localIntent.setComponent(new ComponentName(localXmlPullParser.getAttributeValue(null, "package"), localXmlPullParser.getAttributeValue(null, "class")));
                localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationInfo localApplicationInfo = getApplicationInfo(localPackageManager, localIntent);
                if (localApplicationInfo == null) {
                    localApplicationInfo.intent = localIntent;
                    mFavorites.addFirst(localApplicationInfo);
                }
            }
        } catch (FileNotFoundException localFileNotFoundException) {
            Log.w("dyu", "Got exception parsing favorites.", localFileNotFoundException);
        } catch (XmlPullParserException localXmlPullParserException) {
            Log.w("dyu", "Got exception parsing favorites.", localXmlPullParserException);
        } catch (IOException localIOException) {
            Log.w("dyu", "Got exception parsing favorites.", localIOException);
        }
    }

    /**
     * next element
     *
     * @param paramXmlPullParser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void nextElement(XmlPullParser paramXmlPullParser)
            throws XmlPullParserException, IOException {
        int i;
        do
            i = paramXmlPullParser.next();
        while ((i != 2) && (i != 1));
    }

    /**
     * @param paramString1
     * @param paramString2
     * @return
     */
    private int getAppType(String paramString1, String paramString2) {
        int i = 0;
        if ((paramString1 == null) || (paramString2 == null)) {
            i = -1;
        }
        if ((paramString1 != null) && (paramString2 != null)) {
            if (paramString1.contains("video") || paramString1.contains("myvst") || paramString1.contains("sohu.tv")) {
                i = 1;
            } else if (paramString1.contains("game") || paramString2.contains("game")) {
                i = 2;
            } else if (paramString1.contains("mobileqq") || paramString1.contains("tencent.mm")) {
                i = 3;
            }
        }
        return i;
    }

    /**
     * gets app information
     *
     * @param paramPackageManager
     * @param paramIntent
     * @return
     */
    private static ApplicationInfo getApplicationInfo(PackageManager paramPackageManager, Intent paramIntent) {
        ResolveInfo localResolveInfo = paramPackageManager.resolveActivity(paramIntent, 0);
        ApplicationInfo localApplicationInfo;
        if (localResolveInfo == null) {
            localApplicationInfo = null;
        }
        localApplicationInfo = new ApplicationInfo();
        if (localApplicationInfo.title != null) {
            localApplicationInfo.title = "";
        }
        ActivityInfo localActivityInfo = localResolveInfo.activityInfo;
        localApplicationInfo.icon = localActivityInfo.loadIcon(paramPackageManager);
        if ((localApplicationInfo.title == null) || (localApplicationInfo.title.length() == 0)) {
            localApplicationInfo.title = localActivityInfo.loadLabel(paramPackageManager);
        }

        return localApplicationInfo;
    }

    private void hideApplications() {
        if (mBlockAnimation) {
            mBlockAnimation = true;
            if (mHideLayoutAnimation == null) {
                mHideLayoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.hide_applications);
            }
            mGridExit.setAnimationListener(new HideGrid());
            mGrid.startAnimation(mGridExit);
            mGrid.setVisibility(View.INVISIBLE);
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
     * check hdmi status
     *
     * @return ishdmienable
     */
    private boolean isHdmiEnable() {
        boolean ishdmienable = false;
        if (Settings.System.getInt(getContentResolver(), "hdmi_enable_status", 0) != 0) {
            ishdmienable = true;
        }
        return ishdmienable;
    }

    /**
     * register all receivers
     */
    private void registerIntentReceivers() {
        IntentFilter pkgIntentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        pkgIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        pkgIntentFilter.addDataScheme("package");
        registerReceiver(this.mApplicationsReceiver, pkgIntentFilter);

        IntentFilter batteryIntentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, batteryIntentFilter);
    }

    /**
     * app intent
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        public void onReceive(Context paramContext, Intent paramIntent) {
            loadApplications(false);
            bindApplications();
            bindFavorites(false);
        }
    }

    /**
     * battery listener
     */
    class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context paramContext, Intent paramIntent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(paramIntent.getAction())) {
                int i = paramIntent.getIntExtra("level", 0);
                int j = paramIntent.getIntExtra("scale", 100);
                MainActivity.this.batteryscale = (i * 100 / j);
            }
        }
    }

    /**
     * check is need add
     *
     * @param paramString
     * @return
     */
    private boolean isNeedAddtoApplication(String paramString) {
        if ((paramString.equals("cn.wps.moffice_i18n_TV"))
                || (paramString.equals("com.mediatek.filemanager"))
                || (paramString.equals("com.mediatek.camera"))
                || (paramString.equals("com.hpplay.happyplay.aw"))
                || (paramString.equals("com.elinkway.tvlive2"))
                || (paramString.equals("com.android.settings"))
                || (paramString.equals("com.iflytek.inputmethod"))
                || (paramString.equals("com.android.music"))) {
            return true;
        }
        return false;
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
        PackageManager localPackageManager;
        List localList;
        localPackageManager = getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        localList = localPackageManager.queryIntentActivities(localIntent, 0);
        Collections.sort(localList, new ResolveInfo.DisplayNameComparator(localPackageManager));
        int i = localList.size();
        if (mApplications == null) {
            mApplications = new ArrayList(i);
        }
        ApplicationInfo localApplicationInfo;
        ResolveInfo localResolveInfo;
        for (int j = 0; j < i; j++) {
            localApplicationInfo = new ApplicationInfo();
            localResolveInfo = (ResolveInfo) localList.get(j);
            if (isNeedAddtoApplication(localResolveInfo.activityInfo.applicationInfo.packageName)) {
                localApplicationInfo.title = localResolveInfo.loadLabel(localPackageManager);
                localApplicationInfo.setActivity(new ComponentName(localResolveInfo.activityInfo.applicationInfo.packageName, localResolveInfo.activityInfo.name), Intent.FLAG_ACTIVITY_NEW_TASK);
                localApplicationInfo.icon = localResolveInfo.activityInfo.loadIcon(localPackageManager);
                localApplicationInfo.appType = getAppType(localResolveInfo.activityInfo.applicationInfo.packageName, localResolveInfo.activityInfo.applicationInfo.className);
                mApplications.add(localApplicationInfo);
            }
        }
    }

    /**
     * adapter for app details
     */
    private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
        private Rect mOldBounds = new Rect();

        public ApplicationsAdapter(Context mContext, ArrayList<ApplicationInfo> arg2) {
            super(mContext, 0);
        }

        public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
            ApplicationInfo localApplicationInfo = (ApplicationInfo) MainActivity.mAppTypelications.get(paramInt);
            if (paramView == null)
                paramView = MainActivity.this.getLayoutInflater().inflate(R.layout.application, paramViewGroup, false);
            Object localObject = localApplicationInfo.icon;
            int i;
            int j;
            int k;
            int m;
            float f;
            Bitmap.Config localConfig;
            if (!localApplicationInfo.filtered) {
                i = 150;
                j = 150;
                k = ((Drawable) localObject).getIntrinsicWidth();
                m = ((Drawable) localObject).getIntrinsicHeight();
                if ((localObject instanceof PaintDrawable)) {
                    PaintDrawable localPaintDrawable = (PaintDrawable) localObject;
                    localPaintDrawable.setIntrinsicWidth(i);
                    localPaintDrawable.setIntrinsicHeight(j);
                }
                if ((i > 0) && (j > 0)) {
                    f = k / m;
                    if (k <= m)
                        j = (int) (i / f);
                    if (((Drawable) localObject).getOpacity() == PixelFormat.OPAQUE) {
                        localConfig = Bitmap.Config.ARGB_8888;
                        Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
                        Canvas localCanvas = new Canvas(localBitmap);
                        localCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 0));
                        this.mOldBounds.set(((Drawable) localObject).getBounds());
                        ((Drawable) localObject).setBounds(0, 0, i, j);
                        ((Drawable) localObject).draw(localCanvas);
                        ((Drawable) localObject).setBounds(this.mOldBounds);
                        localObject = new BitmapDrawable(localBitmap);
                        localApplicationInfo.icon = ((Drawable) localObject);
                        localApplicationInfo.filtered = true;
                    }
                }
                TextView localTextView = paramView.findViewById(R.id.label);
                localTextView.setCompoundDrawablesWithIntrinsicBounds(null, (Drawable) localObject, null, null);
                localTextView.setText(localApplicationInfo.title);
                if (MainActivity.this.mCurrentType == -1)
                    localTextView.setVisibility(View.VISIBLE);
                while (true) {
                    if (m <= k)
                        break;
                    localConfig = Bitmap.Config.RGB_565;
                    if (MainActivity.this.mCurrentType == localApplicationInfo.appType) {
                        localTextView.setVisibility(View.VISIBLE);
                        continue;
                    }
                    localTextView.setVisibility(View.GONE);
                }
            }
            return paramView;
        }
    }
}