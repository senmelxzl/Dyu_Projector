package projector.dyu.com.dyuprojector.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import projector.dyu.com.dyuprojector.R;

/**
 * @author xiezhenlin
 */
public class ToastUtil extends Toast {
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     * or {@link Activity} object.
     */
    private Context mContext;

    public ToastUtil(Context context) {
        super(context);
        mContext = context;
    }

    public void AppNotExist() {
        makeText(mContext, mContext.getResources().getText(R.string.app_not_exist), Toast.LENGTH_SHORT).show();
    }
}
