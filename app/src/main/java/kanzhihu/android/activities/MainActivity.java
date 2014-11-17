package kanzhihu.android.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import de.greenrobot.event.EventBus;
import kanzhihu.android.R;
import kanzhihu.android.activities.fragments.ArticlesFragment;
import kanzhihu.android.activities.fragments.CategoryFragment;
import kanzhihu.android.events.ReadArticlesEvent;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new CategoryFragment()).commit();
        }
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(ReadArticlesEvent event) {
        Fragment articlesFragment = ArticlesFragment.newInstance(event.category);
        getFragmentManager().beginTransaction().add(R.id.container, articlesFragment).addToBackStack("").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            SettingActivity.goSetting(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}