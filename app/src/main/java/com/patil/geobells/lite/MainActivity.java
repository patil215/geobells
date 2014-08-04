package com.patil.geobells.lite;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.service.ActivityRecognitionService;
import com.patil.geobells.lite.service.LocationService;
import com.patil.geobells.lite.utils.Config;
import com.patil.geobells.lite.utils.ConnectivityChecker;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;
import com.patil.geobells.lite.views.AboutDialog;
import com.patil.geobells.lite.views.CompletedRemindersFragment;
import com.patil.geobells.lite.views.NavigationDrawerFragment;
import com.patil.geobells.lite.views.UpcomingRemindersFragment;
import com.patil.geobells.lite.views.UpgradeDialog;

import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    private GeobellsDataManager dataManager;
    private GeobellsPreferenceManager preferenceManager;


    private Handler licenseHandler;
    private LicenseChecker licenseChecker;
    private LicenseCheckerCallback licenseCheckerCallback;
    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = new GeobellsDataManager(this);
        preferenceManager = new GeobellsPreferenceManager(this);



        if (new GeobellsPreferenceManager(this).isFirstTime()) {
            if (!Config.IS_LITE_VERSION) {
                if (new ConnectivityChecker(this).isOnline()) {
                    licenseHandler = new Handler();
                    licenseCheckerCallback = new MyLicenseCheckerCallback();
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    licenseChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(Constants.SALT, getPackageName(), deviceId)), Constants.BASE64_PUBLIC_KEY);
                    doCheck();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.dialog_title_license_connection));
                    builder.setCancelable(false);
                    builder.setMessage(getString(R.string.dialog_message_license_connection));
                    builder.setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.create().show();
                }
            }
        }

        if (isAppInstalled("com.patil.geobells")) {
            if (Config.IS_LITE_VERSION) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_uninstall_lite)).setMessage(getString(R.string.dialog_message_uninstall_lite));
                builder.create().show();
            } else {
                if (isAppInstalled("com.patil.geobells.lite")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_uninstall_pro)).setMessage(getString(R.string.dialog_message_uninstall_pro)).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.create().show();
                }
            }
        }

        //Get a Tracker (should auto-report)
        ((GeobellsApplication) getApplication()).getTracker(GeobellsApplication.TrackerName.APP_TRACKER);

        checkGooglePlayServicesEnabled();

        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            stopService(serviceIntent);
            serviceIntent.putExtra(Constants.EXTRA_ACTIVITY, Constants.ACTIVITY_UNKNOWN);
            startService(serviceIntent);
        }
        if (!isActivityServiceRunning()) {
            Intent serviceIntent = new Intent(this, ActivityRecognitionService.class);
            stopService(serviceIntent);
            startService(serviceIntent);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Hide the upgrade button if it's the pro version
        if (!Config.IS_LITE_VERSION) {
            Button upgradeButton = (Button) mNavigationDrawerFragment.getView().findViewById(R.id.navigation_upgrade);
            View upgradeLine = (View) mNavigationDrawerFragment.getView().findViewById(R.id.navigation_upgrade_line);
            upgradeButton.setVisibility(View.GONE);
            upgradeLine.setVisibility(View.GONE);
        }
        title = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }

    public void checkGooglePlayServicesEnabled() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_google_play_services)).setMessage(getString(R.string.dialog_message_update_google)).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            dialog.create().show();
        } else if (status != ConnectionResult.SUCCESS) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_google_play_services)).setMessage(getString(R.string.dialog_message_no_google)).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            dialog.create().show();
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationServicesEnabled();
        // Hide the upgrade button if it's the pro version
        if (!Config.IS_LITE_VERSION && mNavigationDrawerFragment != null) {
            Button upgradeButton = (Button) mNavigationDrawerFragment.getView().findViewById(R.id.navigation_upgrade);
            View upgradeLine = (View) mNavigationDrawerFragment.getView().findViewById(R.id.navigation_upgrade_line);
            upgradeButton.setVisibility(View.GONE);
            upgradeLine.setVisibility(View.GONE);
        }
    }

    public void checkLocationServicesEnabled() {
        LocationManager lm = null;
        boolean gps_enabled = true, network_enabled = true;
        if (lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled || !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.dialog_title_enable_locationservices);
            dialog.setMessage(getString(R.string.dialog_message_enable_locationservices));
            dialog.setCancelable(false);
            dialog.setPositiveButton(getString(R.string.dialog_button_open_location_settings), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.dismiss();
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.dialog_button_nothanks), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.create().show();

        }
    }

    public boolean isLocationServiceRunning() {
        Iterator iterator = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE).iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningServiceInfo localRunningServiceInfo = (ActivityManager.RunningServiceInfo) iterator.next();
            if (LocationService.class.getName().equals(localRunningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isActivityServiceRunning() {
        Iterator iterator = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE).iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningServiceInfo localRunningServiceInfo = (ActivityManager.RunningServiceInfo) iterator.next();
            if (ActivityRecognitionService.class.getName().equals(localRunningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                UpcomingRemindersFragment upcomingFragment = new UpcomingRemindersFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, upcomingFragment)
                        .commit();
                break;
            case 1:
                CompletedRemindersFragment completedFragment = new CompletedRemindersFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, completedFragment)
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                title = getString(R.string.title_upcoming);
                break;
            case 2:
                title = getString(R.string.title_complete);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshUpcoming();
    }

    public void refreshUpcoming() {
        UpcomingRemindersFragment upcomingFragment = new UpcomingRemindersFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, upcomingFragment)
                .commit();
    }

    public void refreshCompleted() {
        CompletedRemindersFragment completedFragments = new CompletedRemindersFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, completedFragments)
                .commit();
    }

    public void onCreateReminderClick(View v) {
        startCreateReminderActivity();
    }

    public void startCreateReminderActivity() {
        if (Config.IS_LITE_VERSION) {
            ArrayList<Reminder> reminders = dataManager.getSavedReminders();
            if (reminders.size() < Constants.REMINDER_LIMIT) {
                Intent intent = new Intent(this, CreateReminderActivity.class);
                intent.putExtra(Constants.EXTRA_EDIT_REMINDER, false);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_REMINDER);
            } else {
                new UpgradeDialog(this).showUpgradeDialog(getString(R.string.upgrade_numreminders));
            }
        } else {
            Intent intent = new Intent(this, CreateReminderActivity.class);
            intent.putExtra(Constants.EXTRA_EDIT_REMINDER, false);
            startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_REMINDER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_create:
                if (new ConnectivityChecker(this).isOnline()) {
                    startCreateReminderActivity();
                } else {
                    Toast.makeText(this, getString(R.string.toast_connect_internet_create_reminder), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_view_map:
                if (new GeobellsDataManager(this).getSavedReminders().size() > 0) {
                    if (new ConnectivityChecker(this).isOnline()) {
                        Intent intent1 = new Intent(this, ViewRemindersMapActivity.class);
                        startActivityForResult(intent1, Constants.ACTIVITY_REQUEST_CODE_VIEW_REMINDER);
                    } else {
                        Toast.makeText(this, getString(R.string.toast_connect_internet_map), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_no_map_reminders), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onSettingsClick(View v) {
        mNavigationDrawerFragment.mDrawerLayout.closeDrawer(mNavigationDrawerFragment.mFragmentContainerView);
        mNavigationDrawerFragment.mDrawerLayout.closeDrawer(mNavigationDrawerFragment.mFragmentContainerView);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SETTINGS);
    }

    public void onAboutClick(View v) {
        mNavigationDrawerFragment.mDrawerLayout.closeDrawer(mNavigationDrawerFragment.mFragmentContainerView);
        AboutDialog about = new AboutDialog(this);
        about.setTitle(getString(R.string.dialog_title_about));
        about.show();
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://geobells.com/help.html"));
        startActivity(browserIntent);
    }

    public void onUpgradeClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.patil.geobells"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


    private void doCheck() {

        didCheck = false;
        checkingLicense = true;
        setProgressBarIndeterminateVisibility(true);

        licenseChecker.checkAccess(licenseCheckerCallback);
    }


    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("License", "Accepted!");

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = true;
            checkingLicense = false;
            didCheck = true;
            preferenceManager.setFirstTime(false);

        }

        @SuppressWarnings("deprecation")
        @Override
        public void dontAllow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("License", "Denied!");
            Log.i("License", "Reason for denial: " + reason);

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = false;
            checkingLicense = false;
            didCheck = true;

            showDialog(0);

        }

        @SuppressWarnings("deprecation")
        @Override
        public void applicationError(int reason) {
            // TODO Auto-generated method stub
            Log.i("License", "Error: " + reason);
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            licensed = true;
            checkingLicense = false;
            didCheck = false;

            showDialog(0);
        }


    }

    protected Dialog onCreateDialog(int id) {
        // We have only one dialog.
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_unlicensed_title))
                .setMessage(getString(R.string.dialog_unlicensed_message))
                .setPositiveButton(getString(R.string.dialog_button_buy), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_exit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton(getString(R.string.dialog_button_check_again), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doCheck();
                    }
                })

                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        Log.i("License", "Key Listener");
                        finish();
                        return true;
                    }
                })
                .create();

    }


}