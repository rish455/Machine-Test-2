package com.toolyt.machinetest.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.toolyt.machinetest.R;
import com.toolyt.machinetest.databinding.ActivityMainBinding;
import com.toolyt.machinetest.ui.base.BaseActivity;
import com.toolyt.machinetest.ui.main.current_location.FetchCurrentLocationActivity;
import com.toolyt.machinetest.ui.main.file_access.FetchFileActivity;

public class MainActivity
        extends BaseActivity<ActivityMainBinding>
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViews() {

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_fetch_files:
                FetchFileActivity.start(this);
                break;
            case R.id.btn_fetch_location:
                FetchCurrentLocationActivity.start(this);
                finish();
                break;
        }
    }
}