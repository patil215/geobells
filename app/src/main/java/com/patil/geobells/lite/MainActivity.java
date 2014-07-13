package com.patil.geobells.lite;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.views.CompletedRemindersFragment;
import com.patil.geobells.lite.views.NavigationDrawerFragment;
import com.patil.geobells.lite.views.UpcomingRemindersFragment;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch(position) {
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
        UpcomingRemindersFragment upcomingFragment = new UpcomingRemindersFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, upcomingFragment)
                .commit();
    }

    public void onCreateReminderClick(View v) {
        startCreateReminderActivity();
    }

    public void startCreateReminderActivity() {
        Intent intent = new Intent(this, CreateReminderActivity.class);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_REMINDER);
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
        switch(item.getItemId()) {
            case R.id.action_create:
                startCreateReminderActivity();
                break;
            case R.id.action_view_map:
                Intent intent1 = new Intent(this, ViewRemindersMapActivity.class);
                startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSettingsClick(View v) {
        mNavigationDrawerFragment.mDrawerLayout.closeDrawer(mNavigationDrawerFragment.mFragmentContainerView);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SETTINGS);
    }

    public void onAboutClick(View v) {

    }

    public void onHelpClick(View v) {

    }
}
