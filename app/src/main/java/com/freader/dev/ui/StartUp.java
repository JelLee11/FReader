package com.freader.dev.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.freader.dev.MainActivity;
import com.freader.dev.R;
import com.freader.dev.setting.PermissionChecker;

import smith.lib.alerts.dialog.AlertSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.dialog.callbacks.OnClickCallback;

public class StartUp extends AppCompatActivity {

    private ActivityResultLauncher<Intent> manageStoragePermissionLauncher;
    private ImageView iconLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        initialize();
        setupPermissionLauncher();
        initializeLogic();
    }

    private void initialize() {
        iconLogo = findViewById(R.id.logoStartup);
    }

    private void setupPermissionLauncher() {
        manageStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        proceedToMain();
                    } else {
                        showPermissionDialog();
                    }
                }
            }
        );
    }

    private void initializeLogic() {
        new Handler(Looper.getMainLooper()).postDelayed(this::checkPermission, 1500);
    }

    private void checkPermission() {
        if (PermissionChecker.checkStoragePermissionGranted(this)) {
            proceedToMain();
        } else {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        AlertSDialog dialog = new AlertSDialog(this);
        dialog.setTitle("Request Permission");
        dialog.setText("Please grant FReader storage permission access! We need this to allow downloading books into your storage.");
        dialog.setPositiveButton("Grant", new OnClickCallback() {
            @Override
            public void onClick() {
                requestStoragePermission();
            }
        });
        dialog.setNegativeButton("Close", new OnClickCallback() {
            @Override
            public void onClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    manageStoragePermissionLauncher.launch(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    manageStoragePermissionLauncher.launch(intent);
                }
                return;
            }
        }

        // For Android 10 and below
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                100
            );
        } else {
            proceedToMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMain();
            } else {
                Toast.makeText(this, "Permission denied. Cannot proceed.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void proceedToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}