package com.example.httpsender;

import android.os.Build;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


/**
 * 需要ToolBar的activity都必须继承本类
 */
public abstract class ToolBarActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected ActionBar actionBar;
    private ViewGroup mContainer;

    public <T extends ViewDataBinding> T bindingInflate(@LayoutRes int layoutResID) {
        initView();
        return DataBindingUtil.inflate(getLayoutInflater(), layoutResID, mContainer, true);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        initView();
        getLayoutInflater().inflate(layoutResID, mContainer, true);
    }

    private void initView() {
        super.setContentView(R.layout.toolbar_activity);
        mContainer = findViewById(R.id.container);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            boolean popSuccess = getSupportFragmentManager().popBackStackImmediate();
            if (popSuccess) return true;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbarColor(int color) {
        toolbar.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(color);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (toolbar == null) return;
        toolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (toolbar == null) return;
        toolbar.setTitle(titleId);
    }

    protected int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
