package com.shuruta.sergey.ftpclient.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.shuruta.sergey.ftpclient.EventBusMessenger;
import com.shuruta.sergey.ftpclient.FtpService;
import com.shuruta.sergey.ftpclient.R;

import de.greenrobot.event.EventBus;

/**
 * Author: Sergey Shuruta
 * Date: 08/15/15
 * Time: 22:11
 */
public class FilesActivity extends BaseActivity implements FFilesFragment.FtpFragmentListener, Toolbar.OnMenuItemClickListener {

    private FtpService mFtpConnectionService;
    private Menu menu;
    private boolean isFtpListReading, bound;
    private ListType mSelectedList = ListType.FTP;

    public enum ListType {
        FTP,
        LOCAL
    }

    public static final String TAG = FilesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        menu = setupToolBar(R.drawable.ic_launcher, R.string.app_name, R.string.list_of_connections, R.menu.menu_files, FilesActivity.this);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.ftpFrameLayout, new FFilesFragment());

        if(null != getSupportFragmentManager().findFragmentById(R.id.localFrameLayout)) {
            fragmentTransaction.replace(R.id.localFrameLayout, new LFilesFragment());
            mSelectedList = ListType.LOCAL;
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_refresh:

                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(FilesActivity.this, FtpService.class), mServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bound) return;
        unbindService(mServiceConnection);
        bound = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // TODO onBack()
    }

    public void onEventMainThread(EventBusMessenger event) {
        Log.d(TAG, "onEvent: " + event.state);
        MenuItem menuItem = menu.findItem(R.id.action_refresh);
        switch (event.state) {
            case READ_FTP_LIST_START:
                if(isFtpListReading) break;
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView iv = (ImageView)inflater.inflate(R.layout.refresh, null);
                Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
                menuItem.setActionView(iv);
                isFtpListReading = true;
                break;
            case READ_FTP_LIST_FINISH:
                if(null != menuItem.getActionView()) {
                    menuItem.getActionView().clearAnimation();
                    menuItem.setActionView(null);
                }
                isFtpListReading = false;
                break;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected()");
            mFtpConnectionService = ((FtpService.ConnectionBinder) binder).getService();
            bound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()");
            bound = false;
        }
    };

    @Override
    public FtpService getFtpConnectionService() {
        return mFtpConnectionService;
    }

    @Override
    public boolean isFtpListReading() {
        return isFtpListReading;
    }
}