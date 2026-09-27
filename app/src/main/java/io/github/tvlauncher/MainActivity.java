package io.github.tvlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSession.PermissionDelegate;
import org.mozilla.geckoview.GeckoView;

// A home-screen button that shows one web page full screen, with no browser bar and no tabs.
// It embeds GeckoView (Firefox's engine as a library), so the page renders exactly as it does in Firefox.
public class MainActivity extends Activity {
    // A process may only ever create one GeckoRuntime.
    private static GeckoRuntime runtime;

    private GeckoSession session;
    private GeckoView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (runtime == null) runtime = GeckoRuntime.create(getApplicationContext());

        session = new GeckoSession();
        // Let the page play sound without waiting for a click.
        session.setPermissionDelegate(new PermissionDelegate() {
            @Override
            public GeckoResult<Integer> onContentPermissionRequest(GeckoSession s, PermissionDelegate.ContentPermission perm) {
                boolean autoplay = perm.permission == PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE
                        || perm.permission == PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE;
                return GeckoResult.fromValue(autoplay
                        ? PermissionDelegate.ContentPermission.VALUE_ALLOW
                        : PermissionDelegate.ContentPermission.VALUE_PROMPT);
            }
        });
        // Some pages (this one included) start their music only after a first click.
        // Once the page has loaded, give it one tap at the right edge, where nothing is clickable.
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStop(GeckoSession s, boolean success) {
                if (success) view.postDelayed(MainActivity.this::tapEdge, 4000);
            }
        });
        session.open(runtime);

        view = new GeckoView(this);
        view.setBackgroundColor(Color.BLACK);
        view.setSession(session);
        setContentView(view);
        hideSystemBars();
        session.loadUri(BuildConfig.LAUNCH_URL);
    }

    private void tapEdge() {
        float x = view.getWidth() - 4f, y = view.getHeight() / 2f;
        long down = SystemClock.uptimeMillis();
        for (int action : new int[] { MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP }) {
            MotionEvent e = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0);
            view.dispatchTouchEvent(e);
            e.recycle();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // Leaving the app (Back or Home) closes the page, so nothing keeps playing in the background.
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        session.close();
        super.onDestroy();
    }
}
