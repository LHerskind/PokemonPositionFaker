package personal.positionfaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/**
 * Created by Nanochrome on 24-Jul-16.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LocationFaker.class);
        startActivity(intent);
        finish();
    }
}
