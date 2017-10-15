package com.sty.drawer.layout.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Shi Tianyi on 2017/10/15/0015.
 */
/**
 * 侧滑面板控件, 抽屉面板.
 * @author poplar
 *
 *   测量             摆放     绘制
measure   ->  layout  ->  draw
|           |          |
onMeasure -> onLayout -> onDraw 重写这些方法, 实现自定义控件

View流程
onMeasure() (在这个方法里指定自己的宽高) -> onDraw() (绘制自己的内容)

ViewGroup流程
onMeasure() (指定自己的宽高, 所有子View的宽高)-> onLayout() (摆放所有子View) -> onDraw() (绘制内容)
 *
 */
public class SlideMenu extends ViewGroup{

    private float downX; //按下的x坐标
    private float downY; //按下的y坐标
    private float moveX;
    private Scroller scroller;
    public static final int MAIN_STATE = 0;
    public static final int MENU_STATE = 1;
    private int currentState = MAIN_STATE; //当前模式

    public SlideMenu(Context context) {
        super(context);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //初始化滚动去，数值模拟器
        scroller = new Scroller(getContext());
    }

    /**
     * 测量并设置所有子View的宽高
     * @param widthMeasureSpec 当前控件的宽度测量规则
     * @param heightMeasureSpec 当前控件的高度测量规则
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //指定左面板的宽高
        View leftMenu = getChildAt(0);
        leftMenu.measure(leftMenu.getLayoutParams().width, heightMeasureSpec);

        //指定主面板的宽高
        View mainContent = getChildAt(1);
        mainContent.measure(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     *
     * @param changed 当前控件的尺寸大小，位置是否发生了变化
     * @param l    当前控件左边距
     * @param t    当前控件顶边距
     * @param r    当前控件右边距
     * @param b    当前控件下边距
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //摆放内容, 左面板
        View leftMenu = getChildAt(0);
        leftMenu.layout(-leftMenu.getMeasuredWidth(), 0, 0, b);

        //主面板
        getChildAt(1).layout(l, t, r, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                Log.i("Tag", "downX:-->" + downX);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();

                //将要发生的偏移量/变化量生效
                int scrollX = - (int) (moveX - downX); //因为是控件(相当于屏幕相对于内容)在移动，所以取反

                //计算将要滚动到的位置,判断是否会超出去，如果超出,不执行scrollBy
                int newScrollPosition = getScrollX() + scrollX;
                Log.i("Tag", "moveX:-->" + moveX + "    scrollX:" + scrollX + "     newScrollPosition:" + newScrollPosition);
                //限定左边界
                if(newScrollPosition < -getChildAt(0).getMeasuredWidth()){// <-240
                    scrollTo(-getChildAt(0).getMeasuredWidth(), 0); //滚动当前控件到内容的某个位置[直接跳过去]
                }else if(newScrollPosition > 0){ // >0
                    scrollTo(0, 0);
                }else {
                    //让变化量生效
                    scrollBy(scrollX, 0);  //在原来的基础上滚动[在刚刚移动到最新位置的基础上滚动]
                }

                downX = moveX;
                break;
            case MotionEvent.ACTION_UP:
                //根据当前滚动到的位置和左面板的一半进行比较
                int leftCenter = (int) (-getChildAt(0).getMeasuredWidth() / 2.0f);

                if(getScrollX() < leftCenter){ //打开，切换成菜单面板
                    currentState = MENU_STATE;
                    updateCurrentContent();
                }else{
                    currentState = MAIN_STATE;
                    updateCurrentContent();
                }
                break;
            default:
                break;
        }

        return true; //消费事件[完全自定义的返回true, 继承已有的返回super]
    }

    //根据当前的状态，执行关闭/开启的动画
    private void updateCurrentContent() {
        int startX = getScrollX();
        int dx = 0;
        //平滑滚动
        if(currentState == MENU_STATE){ //打开菜单
            //scrollTo(-getChildAt(0).getMeasuredWidth(), 0);
            //dx = 结束位置(-240) - 开始位置(-200) = -40
            //dx = 0 - (-10) = 10
            dx = -getChildAt(0).getMeasuredWidth() - startX; //注意这个偏移量是有正负之分的
        }else{ //恢复主界面
            //scrollTo(0, 0);
            dx = 0 - startX; //注意这个偏移量是有正负之分的
        }

        /**
         * startX: 开始的x值
         * startY: 开始的y值
         * dx: 将要发生的水平变化量，移动的x距离
         * dy: 将要发生的竖直变化量，移动的y距离
         * duration: 数据模拟持续的时长
         */
        //1. 开始平滑的数据模拟
        int duration = Math.abs(dx * 2);
        scroller.startScroll(startX, 0, dx, 0, duration);

        invalidate(); //重绘界面-->drawChild()-->computeScroll()
    }

    //2. 维持动画的继续
    @Override
    public void computeScroll() {
        super.computeScroll();

        if(scroller.computeScrollOffset()){ //true，动画还没有结束[在duration时间内返回true]
            int currX = scroller.getCurrX(); //获取当前模拟的数据，也就是要滚动到的位置
            scrollTo(currX, 0);

            invalidate(); //重绘界面
        }
    }

    public void open(){
        currentState = MENU_STATE;
        updateCurrentContent();
    }

    public void close(){
        currentState = MAIN_STATE;
        updateCurrentContent();
    }

    public void switchState(){
        if(currentState == MAIN_STATE){
            open();
        }else{
            close();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float xOffset = Math.abs(ev.getX() - downX);
                float yOffset = Math.abs(ev.getY() - downY);
                Log.i("Tag", "xOffset:" + xOffset + "---yOffset:" + yOffset);
                if( xOffset > yOffset && xOffset > 5){ //水平方向超出一定距离时才拦截
                    return true;  //拦截此次触摸事件，界面滚动
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
