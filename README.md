# Window-and-WindowManager

一.Window 和 WindowManager

1.Android中所有的视图都是通过Window来呈现的，Window不是实际存在的，他只是以View的形式存在，Window是一个抽象类，它的具体实现类是PhoneWindow。

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

1.每一个Window都对应着一个View和一个ViewRootImpl，Window和View是通过ViewRootImpl来建立联系的。

2.Window的添加过程：
WindowManager是一个接口，真正的实现类是WindowManagerImpl，WindowManagerImpl中Window的三大操作如下：

public void addView(View view,ViewGroup.LayoutParams params){
       mGlobal.addView(view,params,mDisplay,mParentWindow);
}

public void updateViewLayout(View view,ViewGroup.LayoutParams params){
       mGlobal.updateViewLayout(view,params);
}

public void removeView(View view){
       mGlobal.removeView(view,false);
}
WindowManagerImpl将三大操作交给了mGolbal.他是一个WindowManagerGlobal，WindowManagerGlobal的addView方法分一下几步：
（1）检查参数是否合法，如果是子View还需要调整一些布局参数。
（2）创建ViewRootImpl，并将View添加到列表中。
WindowManagerGlobal中有几个集合：
//储存所有Window对应的View
private final ArrayList<View> mViews = new ArrayList<View>();
//储存所有Window对应的ViewRootImpl
private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
//储存所有Window的布局参数
private final ArrayList<WindowManager.LayoutParams> mViews = new ArrayList<WindowManager.LayoutParams>();
//储存那些正在被删除，或者已经调用RemoteView方法但是删除操作还未完成的Window对象
private final ArraySet<View> mDyingViews = new ArraySet<View>();

在addView中通过如下方式将Window的一系列对象添加到列表中：
root = new ViewRootImpl(view.getContext,display);
view.setLayoutParams(wparams);

mViews.add(view);
mRoot.add(root);
mParams.add(wparams);

（3）通过ViewRootImpl来更新界面并完成Window的添加过程
这个步骤由ViewRootImpl的setView方法完成。在setView内部通过requestLayout来异步刷新view。
public void requestLayout(){
        if(!mHandlingLayoutInLayoutRequest){
                checkThread();
                mLayoutRequested = true;
                scheduleTraversals();------绘制View
         }
}

接着会通过WindowSession最终完成Window的添加过程，mWindowSession的类型是IWindowSession，他是一个Binder对象，实现类是Session，
也就是说Window的添加过程是一个IPC操作。

Session内部会通过WindowManagerService来实现Window的添加。Window添加最后交给WindowManagerService处理。


3.Window的删除过程：
Window的删除过程和添加过程一样，都是先通过WindowManagerImpl后，在进一步通过WindowManagerGlobal来实现的。
WIndowManager提供了两种删除接口，removeView和remoteViewImmediate，他们分别表示异步和同步删除操作，一般使用异步操作，以免发生以外错误。
他们之间的区别：异步操作会发送一个MSG_DIE的空消息，ViewRootImpl中的Handler会处理此消息并调用doDie方法，如果是同步，就会立即删除，不发送消息。
直接调用doDie方法。
在doDie方法中会调用dispatchDetachedFromWindow方法，真正删除View的逻辑在此方法中实现。它主要做四件事：
（1）垃圾回收相关工作
（2）通过Session的remote方法删除Window，最终调用WindowManagerService的removeWindow方法。
（3）dispatchDetachedFromWindow方法：当View从Window中移除时，这个方法会被调用，可以做一些资源回收的工作，比如终止动画，停止线程。
（4）调用WindowManagerGlobal的doRemoveView方法刷新数据，包括mRoots,mParams,以及mDyingViews，需要将当前Window所关联的这三类对象从列表中删除。


4.Window的更新过程：
和前面两种方法一样，将view的LayoutParams替换为新的，更新ViewRootImpl中的LayoutParams，最终通过WindowManagerService的reLayoutWindow（）实现的。

三.Window的创建过程












































