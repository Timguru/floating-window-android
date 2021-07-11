package com.example.floatingwindow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    public Button minimizeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minimizeBtn = findViewById(R.id.minimizeBtn);

        if(isFloatingServiceRunning()){
            stopService(new Intent(MainActivity.this, FloatingActivityWindow.class));

        }

        minimizeBtn.setOnClickListener(v -> {
            if(checkOverlayPermission()){
                //Floating window service is started
//                Log.isLoggable(checkOverlayPermission());

                startService(new Intent(MainActivity.this, FloatingActivityWindow.class));
                //close MainActivity
                finish();
            }else {
                requestOverlayDisplayPermission();
            }
        });

    }

    private boolean isFloatingServiceRunning() {
        /*
        * The ACTIVITY_SERVICE retrieves Activity manager for interacting with the global system
        * */
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        /*
        * We have to loop through all the activities to get our service
        * */
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            //If service if found running, it will return true else return false
            if(FloatingActivityWindow.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }

        return false;
    }

    private void requestOverlayDisplayPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //This dialog can be closed by tapping outside the dialog box
        builder.setCancelable(true);

        //Title of dialog box
        builder.setTitle("Screen Overlay Permission needed");
        builder.setMessage("Enable Display over other apps");

        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            }

            startActivityForResult(intent, RESULT_OK);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private boolean checkOverlayPermission(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }else {
            return true;
        }
    }
}