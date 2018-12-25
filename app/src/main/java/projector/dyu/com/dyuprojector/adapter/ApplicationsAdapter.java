package projector.dyu.com.dyuprojector.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import projector.dyu.com.dyuprojector.R;
import projector.dyu.com.dyuprojector.util.ApplicationInfo;

/**
 * app adapter class
 * @author xiezhenlin
 */
public class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
    private final static String TAG = "ApplicationsAdapter";
    private Rect mOldBounds = new Rect();
    private ArrayList<ApplicationInfo> listApps;
    private Context aContext;
    private int aCurrentType;

    public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> objects, int mCurrentType) {
        super(context, 0, objects);
        Log.i(TAG, "ApplicationsAdapter init!");
        listApps = objects;
        aContext = context;
        aCurrentType = mCurrentType;
    }

    @Override
    public int getCount() {
        return (listApps != null && listApps.size() > 0) ? listApps.size() : 0;
    }


    public void refresh(ArrayList<ApplicationInfo> list, int mCurrentType) {
        listApps = list;
        aCurrentType = mCurrentType;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        ApplicationInfo localApplicationInfo = listApps.get(paramInt);
        Log.i(TAG, "ApplicationsAdapter getView  start mAppTypelications: " + listApps.size());
        if (paramView == null) {
            LayoutInflater inflater = LayoutInflater.from(aContext);
            paramView = inflater.inflate(R.layout.application, null);
        }
        TextView localTextView = paramView.findViewById(R.id.label);
        Object localObject = localApplicationInfo.icon;
        int i = 130;
        int j = 130;
        int k;
        int m;
        float f;
        Bitmap.Config localConfig;
        Log.i(TAG, "ApplicationsAdapter getView ");
        k = ((Drawable) localObject).getIntrinsicWidth();
        m = ((Drawable) localObject).getIntrinsicHeight();
        if ((localObject instanceof PaintDrawable)) {
            PaintDrawable localPaintDrawable = (PaintDrawable) localObject;
            localPaintDrawable.setIntrinsicWidth(i);
            localPaintDrawable.setIntrinsicHeight(j);
        }
        if (i > 0 && j > 0) {
            f = k / m;
            if (k <= m) {
                j = (int) (i / f);
            } else {
                i = (int) (f * j);
            }
            localConfig = Bitmap.Config.ARGB_8888;
            Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
            Canvas localCanvas = new Canvas(localBitmap);
            localCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 0));
            mOldBounds.set(((Drawable) localObject).getBounds());
            ((Drawable) localObject).setBounds(0, 0, i, j);
            ((Drawable) localObject).draw(localCanvas);
            ((Drawable) localObject).setBounds(this.mOldBounds);
            localObject = new BitmapDrawable(localBitmap);
            localApplicationInfo.icon = ((Drawable) localObject);
            localApplicationInfo.filtered = true;
        }
        localTextView.setCompoundDrawablesWithIntrinsicBounds(null, (Drawable) localObject, null, null);
        localTextView.setText(localApplicationInfo.title);
        if (aCurrentType == localApplicationInfo.appType) {
            localTextView.setVisibility(View.VISIBLE);
        } else {
            localTextView.setVisibility(View.GONE);
        }
        return paramView;
    }
}
