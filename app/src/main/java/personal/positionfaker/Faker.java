package personal.positionfaker;

/**
 * Created by Nanochrome on 23-Jul-16.
 */


import android.content.Context;
import android.location.Location;

import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Faker implements IXposedHookLoadPackage {

    private Context mContext;
    private XSharedPreferences mSharedPreferences;
    private Object mThisObject;
    private Location mLocation;
    private double mLatitude, mLongitude;
    private int[] mWhateverArray;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo")) return;

        findAndHookConstructor("com.nianticlabs.nia.location.NianticLocationManager", lpparam.classLoader, Context.class, long.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                mContext = (Context) param.args[0];
                mSharedPreferences = new XSharedPreferences("personal.positionfaker", "pokemon");
                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        if(mSharedPreferences.hasFileChanged()) {
                            gotoPlace();
                        }
                    }
                }, 0, 500);
            }
        });

        findAndHookMethod("com.nianticlabs.nia.location.NianticLocationManager", lpparam.classLoader, "locationUpdate", Location.class, int[].class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                Location location = (Location) param.args[0];
                if (location != null) {
                    mLocation = location;
                    mThisObject = param.thisObject;
                    mWhateverArray = (int[]) param.args[1];
                }
                return null;
            }
        });
    }

    private void gotoPlace() {
        if (mLocation == null || mThisObject == null || mWhateverArray == null) return;
        mSharedPreferences.reload();
        mLatitude = Double.parseDouble(mSharedPreferences.getString("latitude", "55.859701"));
        mLongitude = Double.parseDouble(mSharedPreferences.getString("longtitude", "12.330113"));
        mLocation.setLatitude(mLatitude);
        mLocation.setLongitude(mLongitude);
        XposedHelpers.callMethod(mThisObject, "nativeLocationUpdate", mLocation, mWhateverArray, mContext);
    }
}
