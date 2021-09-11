package com.toolyt.machinetest.ui.main.file_access;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.toolyt.machinetest.R;
import com.toolyt.machinetest.databinding.ActivityFetchFileBinding;
import com.toolyt.machinetest.ui.base.BaseActivity;
import com.toolyt.machinetest.utils.FileUtils;
import com.toolyt.machinetest.utils.Utils.FileType;

import java.util.List;

public class FetchFileActivity
        extends BaseActivity<ActivityFetchFileBinding>
        implements View.OnClickListener {

    private ActivityFetchFileBinding mBinding;

    ActivityResultLauncher<Intent> fetchFileResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri imageUri = data.getData();
                    String path = FileUtils.getPath(this, imageUri);
                    updateUI(path);
                }
            });
    ActivityResultLauncher<Intent> storagePermissionResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.e("TAG", "storagePermissionResult: " + result);
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        fetchFileFromStorage();
                    }
                }
            });

    private FileType mFileType;

    public static void start(Context context) {
        Intent starter = new Intent(context, FetchFileActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_fetch_file;
    }

    @Override
    public void initViews() {
        mBinding = getViewDataBinding();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_fetch_jpg:
                mFileType = FileType.JPG;
                break;
            case R.id.btn_fetch_pdf:
                mFileType = FileType.PDF;
                break;
        }
        if (isStoragePermissionGranted()) {
            fetchFileFromStorage();
        } else {
            requestPermission();
        }
    }

    private boolean isStoragePermissionGranted() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                storagePermissionResult.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storagePermissionResult.launch(intent);
            }
        } else {
            Dexter.withContext(this)
                    .withPermissions(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                fetchFileFromStorage();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        }

    }

    private void fetchFileFromStorage() {
        Intent intent = new Intent();
        intent.setType(mFileType == FileType.JPG ? "image/*" : "application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fetchFileResult.launch(Intent.createChooser(intent, "Select File"));
    }

    private void updateUI(String filePath) {
        mBinding.layoutFileName.setVisibility(View.VISIBLE);
        mBinding.layoutFilePath.setVisibility(View.VISIBLE);
        mBinding.tvFileName.setText(FileUtils.getFileName(filePath));
        mBinding.tvFilePath.setText(filePath);
    }
}