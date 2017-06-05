# Window-and-WindowManager

一.Window 和 WindowManager

1.Android中所有的视图都是通过Window来呈现的，Window是一个抽象类，它的具体实现类是PhoneWindow。

2.单击事件由Window传递给DecorView，DecorView是顶层View，包括标题栏和内容栏，内容栏就是创建Activity时，所setContentView的View，DecorView再传递
给setContentView的View。

3.使用WindowManager添加一个Window：
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
        
上述代码将一个Button添加到屏幕（100,300）的位置上，其中WindowManager.LayoutParams中的flags和type两个属性比较重要。

（1）Flags：
FLAG_NOT_FOCUSABLE:源码中解释：this window won't ever get key input focus, so the user can not send key or other button events to it.  Those will
instead go to whatever focusable window is behind it.  This flag will also enable {@link #FLAG_NOT_TOUCH_MODAL} whether or not that
is explicitly set. 此Window不需要获取焦点，也不需要接收各种输入事件，此标记会同时启用FLAG_NOT_TOUCH_MODAL,最终事件会直接传递给下层具有焦点的View.

FLAG_NOT_TOUCH_MODAL:even when this window is focusable (its {@link #FLAG_NOT_FOCUSABLE} is not set), allow any pointer events
outside of the window to be sent to the windows behind it.  Otherwise it will consume all pointer events itself, regardless of whether they
are inside of the window. 系统会将当前Window区域以外的单击事件传递给底层的Window，当前Window区域以内的单击事件会自己消耗。一般来说，此标记位都需要开启
，否则其他Window将无法接受到单击事件。

FLAG_SHOW_WHEN_LOCKED:当锁屏的时候也能显示在界面上。

（2）Type参数：type参数表示Window的类型，Window有三种类型-----应用Window（Acticity..），子Window(Dialog)，系统Window(Toast).Window分层：
每个类型的Window都有对应的z-ordered，大概类似于谁大就显示在最上面的概念。应用Window层级范围是1-99，子Window层级范围是1000-1999，系统Window是
2000-2999。他们所对应的层级就是WindowManager.LayoutParams中的type参数。系统层级是最大的，一般我们选用TYPE_SYSTEM_OVERLAY
或者TYPE_SYSTEM_ERROR。使用TYPE_SYSTEM_ERROR要设置权限。<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>


4.WindowManager所提供的功能很简单，只有三个方法:
(1)添加View
（2）更新View
（3）删除View
这三个方法定义在ViewManager中，WindowManager继承了他：

public interface ViewManager{
    void addView(View view, ViewGroup.LayoutParams params);
    void updateViewLayout(View view,ViewGroup.LayoutParams params);
    void removeView(View view);
}

5.实现拖动Window的效果：根据手指的位置，改变LayoutParams的x,y位置，再使用WindowManager更新View即可。
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


二.Window的内部机制。

1.
















