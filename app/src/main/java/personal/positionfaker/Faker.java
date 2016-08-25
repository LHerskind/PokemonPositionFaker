package personal.positionfaker;

/**
 * Created by Nanochrome on 23-Jul-16.
 */

import android.content.Context;
import android.location.Location;

import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Faker implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private Context mContext;
    private XSharedPreferences mSharedPreferences;
    private Object mThisObject;
    private Location mLocation;
    private double mLatitude, mLongitude;
    private int[] mWhateverArray;

    private boolean isModuleOn;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mSharedPreferences = new XSharedPreferences(Faker.class.getPackage().getName(), "PokemonGoCoordinates");
        mSharedPreferences.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo")) return;
//        XposedBridge.log("TAG Starting pokemon");

        findAndHookConstructor("com.nianticlabs.nia.location.NianticLocationManager", lpparam.classLoader, Context.class, long.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                mContext = (Context) param.args[0];
                mSharedPreferences.reload();
                isModuleOn = mSharedPreferences.getBoolean("moduleOn", false);
//                XposedBridge.log("TAG isModuleOn = " + isModuleOn);
//                XposedBridge.log("TAG " + mSharedPreferences.getFile().exists() + ":" + mSharedPreferences.getAll().size());
                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        if (mSharedPreferences.hasFileChanged()) {
                            gotoPlace();
                        }
                    }
                }, 0, 250);
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
                    if (!isModuleOn) {
                        XposedHelpers.callMethod(mThisObject, "nativeLocationUpdate", mLocation, mWhateverArray, mContext);
                    }
                }
                return null;
            }
        });

        /*
        findAndHookMethod("com.nianticlabs.nia.location.NianticLocationManager", lpparam.classLoader, "gpsStatusUpdate", Location.class, int[].class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return null;
            }
        });*/
    }

    private void gotoPlace() {
//        XposedBridge.log("TAG : GOTOPLACE");

        if (mLocation == null || mThisObject == null || mWhateverArray == null) {
            return;
        }
        mSharedPreferences.reload();

        if (!isModuleOn) {
//            XposedBridge.log("TAG LORT!" + mSharedPreferences.getAll().size()+ ":" + mSharedPreferences.getString("latitude","420"));
            return;
        }

//        XposedBridge.log("TAG " + mSharedPreferences.getAll().size() + " : " + mSharedPreferences.getFile().exists());
        if (mSharedPreferences.getAll().size() < 2) {
            return;
        }

        mLatitude = Double.parseDouble(mSharedPreferences.getString("latitude", "420"));
        mLongitude = Double.parseDouble(mSharedPreferences.getString("longtitude", "420"));
        if (mLatitude == 420 || mLongitude == 420 || (mLatitude == 0 && mLongitude == 0)) {
            return;
        }
        mLocation.setLatitude(mLatitude);
        mLocation.setLongitude(mLongitude);
        XposedHelpers.callMethod(mThisObject, "nativeLocationUpdate", mLocation, mWhateverArray, mContext);
    }


}
