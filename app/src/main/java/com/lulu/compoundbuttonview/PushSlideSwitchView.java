package com.lulu.compoundbuttonview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义开关按钮
 * Created by Lu on 2015/9/30.
 */
public class PushSlideSwitchView extends View{

    /** Switch底部灰色样式图片 */
    private Bitmap mSwitchBgUnseleted;
    /** Switch底部绿色样式图  */
    private Bitmap mSwitchBgSeleted;
    /** Switch灰色的球  */
    private Bitmap mSwitchBallUnseleted;
    /** Switch绿色的球  */
    private Bitmap mSwitchBallSeleted;

    /** 当前点击位置  */
    private float mCurrentX = 0;
    /** Switch 开关状态，默认是  开：true  */
    private boolean mSwitchOn = true;
    /** Switch 最大移动距离   */
    private int mMoveLength;
    /** 第一次按下的有效区域 */
    private float mLastX = 0;
    /** 绘制的目标区域大小  */
    private Rect mDest = null;
    /** Switch 移动的偏移量  */
    private int mMoveDeltX = 0;
    /** 画笔工具  */
    private Paint mPaint = null;
    /** Switch 状态监听接口  */
    private OnSwitchChangedListener switchListener = null;
    private boolean mFlag = false;
    /** 触摸是否有效 */
    private boolean mEnabled = true;
    /** 最大透明度，就是不透明 */
    private final int MAX_ALPHA = 255;
    /** 当前透明度，这里主要用于如果控件的enable属性为false时候设置半透明 ，即不可以点击 */
    private int mAlpha = MAX_ALPHA;
    /** Switch 判断是否在拖动 */
    private boolean mIsScrolled =false;

    public PushSlideSwitchView(Context context) {
        super(context, null);
    }

    public PushSlideSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public PushSlideSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化相关资源
     */
    public void init() {
        mSwitchBgUnseleted = BitmapFactory
                .decodeResource(getResources(), R.drawable.push_button_unselected_bg);
        mSwitchBgSeleted = BitmapFactory
                .decodeResource(getResources(), R.drawable.push_button_selected_bg);
        mSwitchBallUnseleted = BitmapFactory
                .decodeResource(getResources(), R.drawable.push_button_ball_unselected);
        mSwitchBallSeleted = BitmapFactory
                .decodeResource(getResources(), R.drawable.push_button_ball_selected);

        mMoveLength = mSwitchBgSeleted.getWidth() - mSwitchBallSeleted.getWidth();
        mDest = new Rect(0, 0, mSwitchBgSeleted.getWidth(), mSwitchBgSeleted.getHeight());

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(225);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSwitchBgSeleted.getWidth(), mSwitchBgSeleted.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.saveLayerAlpha(new RectF(mDest), mAlpha, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        if(!mSwitchOn) {
            //开关处于关闭状态
            if (mMoveDeltX > 0) {
                //向右滑动
                if (mMoveDeltX < mMoveLength / 2) {
                    //滑动距离下于一半，设置背景及按钮为灰色
                    canvas.drawBitmap(mSwitchBgUnseleted, 0, 0, null);
                    canvas.drawBitmap(mSwitchBallUnseleted, mMoveDeltX, 0, null);
                } else {
                    //滑动距离大于一半，设置背景及按钮为绿色
                    canvas.drawBitmap(mSwitchBgSeleted, 0, 0, null);
                    canvas.drawBitmap(mSwitchBallSeleted, mMoveDeltX, 0, null);
                }
            } else {
                canvas.drawBitmap(mSwitchBgUnseleted, 0, 0, null);
                canvas.drawBitmap(mSwitchBallUnseleted, 0, 0, null);
            }
        } else {
            //开关处于打开状态
            if (mMoveDeltX < 0) {
                //向左滑动
                if (Math.abs(mMoveDeltX) < mMoveLength / 2) {
                    canvas.drawBitmap(mSwitchBgSeleted, 0, 0, null);
                    canvas.drawBitmap(mSwitchBallSeleted,
                            mSwitchBgSeleted.getWidth() - mSwitchBallSeleted.getWidth()
                                    + mMoveDeltX, 0, null);
                } else {
                    canvas.drawBitmap(mSwitchBgUnseleted, 0, 0, null);
                    canvas.drawBitmap(mSwitchBallUnseleted,
                            mSwitchBgSeleted.getWidth() - mSwitchBallSeleted.getWidth()
                                    + mMoveDeltX, 0, null);
                }
            } else {
                canvas.drawBitmap(mSwitchBgSeleted, 0, 0, null);
                canvas.drawBitmap(mSwitchBallSeleted, 0, mMoveLength, null);
            }
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果Enabled属性设定为true,触摸效果才有效
        if (!mEnabled)
            return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                mCurrentX = event.getX();
                mMoveDeltX = (int) (mCurrentX - mLastX);
                if (mMoveDeltX > 3) {
                    // 设置了3这个误差距离，可以更好的实现点击效果
                    mIsScrolled = true;
                }
                // 如果开关开着向右滑动，或者开关关着向左滑动,不需要处理
                if ((mSwitchOn && mMoveDeltX > 0) || (!mSwitchOn && mMoveDeltX < 0)) {
                    mFlag = true;
                    mMoveDeltX = 0;
                }

                // 超出最大移动距离时，设置移动距离为最大移动距离
                if (Math.abs(mMoveDeltX) > mMoveLength) {
                    mMoveDeltX = mMoveDeltX > 0 ? mMoveLength : - mMoveLength;
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                //如果没有滑动过，就看作一次点击事件
                if(!mIsScrolled){
                    mMoveDeltX = mSwitchOn ? mMoveLength : -mMoveLength;
                    mSwitchOn = !mSwitchOn;
                    if (switchListener != null) {
                        switchListener.onSwitchChange(this, mSwitchOn);
                    }
                    invalidate();
                    mMoveDeltX = 0;
                    break;
                }

                mIsScrolled = false;
                if (Math.abs(mMoveDeltX) > 0 && Math.abs(mMoveDeltX) < mMoveLength / 2) {
                    mMoveDeltX = 0;
                    invalidate();
                } else if (Math.abs(mMoveDeltX) > mMoveLength / 2
                        && Math.abs(mMoveDeltX) <= mMoveLength) {
                    mMoveDeltX = mMoveDeltX > 0 ? mMoveLength : -mMoveLength;
                    mSwitchOn = !mSwitchOn;
                    if (switchListener != null) {
                        switchListener.onSwitchChange(this, mSwitchOn);
                    }
                    invalidate();
                    mMoveDeltX = 0;
                } else if (mMoveDeltX == 0 && mFlag) {
                    mMoveDeltX = 0;
                    mFlag = false;
                }
            default:
                break;
        }

        invalidate();
        return true;
    }

    /**
     * 设置 switch 状态监听
     * */
    public void setOnChangeListener(OnSwitchChangedListener listener) {
        switchListener = listener;
    }

    /**
     * switch 开关监听接口
     *  */
    public interface OnSwitchChangedListener{
        public void onSwitchChange(PushSlideSwitchView switchView, boolean isChecked);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA/2;
        super.setEnabled(enabled);
        invalidate();
    }

    /** 自动判断切换至相反的属性 : true -->false ;false -->true */
    public void toggle() {
        setChecked(!mSwitchOn);
    }

    /** 设置选中的状态（选中:true   非选中: false） */
    public void setChecked(boolean checked) {
        mSwitchOn = checked;
        invalidate();
    }
}
