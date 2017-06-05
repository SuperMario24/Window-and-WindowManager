package com.example.saber.windowtest;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    private static final String TAG = "MainActivity";
    
    private Button mFloatingButton;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        mFloatingButton = new Button(this);
        mFloatingButton.setText("Click me");
        mFloatingButton.setBackgroundResource(R.mipmap.ic_launcher);
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                0,WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, PixelFormat.TRANSPARENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.x = 100;
        mLayoutParams.y = 300;

        mWindowManager.addView(mFloatingButton,mLayoutParams);


//        Dialog dialog = new Dialog(getApplicationContext());
//        TextView textView = new TextView(this);
//        textView.setText("this is toast");
//        //普通dialog必须设置为系统Window，不然不能用Application的Context.必须用Activity的，因为Application的Context没有token
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
//        dialog.setContentView(textView);
//        dialog.show();

        Toast toast = new Toast(this);
        TextView textView = new TextView(this);
        textView.setText("this is toast");
        toast.setView(textView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d(TAG, "onContentChanged ");
        
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mLayoutParams.x = rawX;
                mLayoutParams.y = rawY;
                mWindowManager.updateViewLayout(mFloatingButton,mLayoutParams);
                break;
        }
        return false;
    }
}
