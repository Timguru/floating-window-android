package com.example.floatingwindow;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;

public class FloatingActivityWindow extends Service {

    private int LAYOUT_TYPE;
    private Button maximizeBtn;
    private WindowManager windowManager;
    private ViewGroup floatView;
    private WindowManager.LayoutParams floatWindowLayoutParam;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(){
        super.onCreate();

        //calculate the screen height and width for the floating window to scale
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        /*
        * To obtain a window manager of a different display we use a context of that
        * display, so WINDOW_SERVICE is used.
        * */
        windowManager  = (WindowManager) getSystemService(WINDOW_SERVICE);

        //Layout inflater is used for the floating_layout xml
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        maximizeBtn = floatView.findViewById(R.id.maximizeBtn);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        }else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }


        floatWindowLayoutParam = new WindowManager.LayoutParams(
                (int)(width * (0.55f)),
                (int)(height * (0.58f)),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        //Set gravity
        floatWindowLayoutParam.gravity = Gravity.CENTER;

        //x and y values are set
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        windowManager.addView(floatView, floatWindowLayoutParam);

        //Button to maximize the app
        maximizeBtn.setOnClickListener(v -> {
            //stop service if it was started
            stopSelf();

            //window is removed from screen
            windowManager.removeView(floatView);

            //call the MainActivity class again
            Intent backToHome = new Intent(FloatingActivityWindow.this, MainActivity.class);

            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backToHome);
        });

        /*
        * Floating window feature
        * Drag the window at any position of the screen
        * */
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x, y, px, py;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;

                        /*
                        * Return the original raw x co-ordinate
                        * of this event
                        */
                        px = event.getRawX();

                        /*
                        * Return the original raw y co-ordinate
                        * of this event
                        * */
                        py = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);

                        //updated parameter is applied
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;

                }
                return false;
            }
        });

    }

    //called when stopService() method is called in MainActivity
    @Override
    public void onDestroy(){
        super.onDestroy();
        stopSelf();
        windowManager.removeView(floatView);
    }
}
