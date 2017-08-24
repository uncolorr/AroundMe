package com.example.uncolor.aroundme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;

/**
 * Created by uncolor on 23.08.17.
 */

public abstract class AbsRuntimePermission extends AppCompatActivity {

    private SparseIntArray errorString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        errorString = new SparseIntArray();
    }

    public abstract void onPermissonGranted(int requestCode);

    public void requestAppPermissions(final String[] requestedPermissons, final int stringId, final int requestCode) {
        errorString.put(requestCode, stringId);

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean showRequestPermissions = false;

        for (String permission : requestedPermissons) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            showRequestPermissions = showRequestPermissions || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (showRequestPermissions) {
                Snackbar.make(findViewById(android.R.id.content), stringId, Snackbar.LENGTH_INDEFINITE).setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(AbsRuntimePermission.this, requestedPermissons, requestCode);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissons, requestCode);
            }
        } else {
            onPermissonGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissonCheck = PackageManager.PERMISSION_GRANTED;

        for(int permission: grantResults){
            permissonCheck = permissonCheck + permission;
        }

        if((grantResults.length > 0) && permissonCheck == PackageManager.PERMISSION_GRANTED){
            onPermissonGranted(requestCode);
        }else {
            Snackbar.make(findViewById(android.R.id.content), errorString.get(requestCode), Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }).show();
        }


    }
}
