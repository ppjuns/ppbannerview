package com.ppjun.android.ppbannerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import java.util.*

class PPBannerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    //默认选中颜色
    private val DEFAULT_SELECT_COLOR = 0xFFFFFF
    //默认非选中颜色
    private val DEFAULT_UNSELECT_COLOR = 0x05FFFF
    //间隔时间
    private var mInterval = 5000
    //dot大小
    private var mDotSize = 0
    //两个dot的间隔
    private var mSpace = 0
    //是否显示dot
    private var isShowIndicators = true
    //选中dot颜色，或者图片
    private var mSelectedDrawable: Drawable
    //非选中dot的颜色或图片
    private var mUnSelectedDrawable: Drawable
    //rv
    private var mRecyclerView: RecyclerView? = null
    //dot容器
    private var mLinearLayout: LinearLayout? = null
    private var mAdapter: RecyclerView.Adapter<*>
    //banner点击事件
    var mOnBannerClickListener: OnBannerClickListener? = null
    //banner切换事件
    var mOnBannerSwitchListener: OnBannerSwitchListener? = null
    //图片源
    var mData = ArrayList<String>()
    //点击x
    var mStartX = 0
    //点击y
    var mStartY = 0
    //当前的banner显示第几张
    var mCurrentIndex = 0
    //是否播放中
    private var isPlaying = false
    var mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {}
    //是否点击中
    var isTouched = false
    //是否自动播放
    var isAutoPlay = true
    var mScaleType = ImageView.ScaleType.FIT_CENTER


    var playTask = object : Runnable {
        override fun run() {
            mRecyclerView?.smoothScrollToPosition(++mCurrentIndex)
            if (isShowIndicators) {
                switchIndicator()
            }
            mHandler.postDelayed(this, mInterval.toLong())
        }

    }


    @Synchronized
    private fun setPlaying(playing: Boolean) {
        if (isAutoPlay) {
            if (!isPlaying && playing && mAdapter.itemCount > 2) {
                mHandler.postDelayed(playTask, mInterval.toLong())
                isPlaying = true
            } else if (isPlaying && playing.not()) {
                mHandler.removeCallbacksAndMessages(null)
                isPlaying = false
            }
        }
    }


    fun setBannerData(data: ArrayList<String>) {
        setPlaying(false)
        mData.clear()
        mData.addAll(data)
        if (mData.size > 1) {
            mCurrentIndex = mData.size
            mAdapter.notifyDataSetChanged()
            mRecyclerView?.scrollToPosition(mCurrentIndex)
            if (isShowIndicators) {
                createIndicators()
            }
            setPlaying(true)
        } else {

            mCurrentIndex = 0
            mAdapter.notifyDataSetChanged()
        }

    }

    interface OnBannerClickListener {

        fun onClick(position: Int)
    }

    interface OnBannerSwitchListener {

        fun onSwitch(position: Int, imageView: AppCompatImageView)
    }


    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.PPBannerView)
        mInterval = typeArray.getInt(R.styleable.PPBannerView_pp_interval, 3000)
        mDotSize = typeArray.getDimensionPixelSize(R.styleable.PPBannerView_pp_indicatorSize, 0)
        isShowIndicators = typeArray.getBoolean(R.styleable.PPBannerView_pp_showIndicator, true)
        isAutoPlay = typeArray.getBoolean(R.styleable.PPBannerView_pp_autoPlay, true)
        val selectedDrawable = typeArray.getDrawable(R.styleable.PPBannerView_pp_indicatorSelectedSrc)
        val unselectDrawable = typeArray.getDrawable(R.styleable.PPBannerView_pp_indicatorUnSelectedSrc)
        mSelectedDrawable = if (selectedDrawable == null) {
            generateDrawable(DEFAULT_SELECT_COLOR)
        } else {
            if (selectedDrawable is ColorDrawable) {
                generateDrawable(selectedDrawable.color)
            } else {
                selectedDrawable
            }
        }
        mUnSelectedDrawable = if (unselectDrawable == null) {
            generateDrawable(DEFAULT_UNSELECT_COLOR)
        } else {
            if (unselectDrawable is ColorDrawable) {
                generateDrawable(unselectDrawable.color)
            } else {
                unselectDrawable
            }
        }

        mSpace = typeArray.getDimensionPixelSize(R.styleable.PPBannerView_pp_indicatorSpace, PPUtils.dp2px(4))
        val margin = typeArray.getDimensionPixelSize(R.styleable.PPBannerView_pp_indicatorMargin, PPUtils.dp2px(8))
        val gavity = typeArray.getInt(R.styleable.PPBannerView_pp_indicatorGravity, 1)
        var mGavity = 0
        mGavity = when (gavity) {
            0 -> Gravity.START
            1 -> Gravity.CENTER
            else -> Gravity.END
        }

        val scaleType = typeArray.getInt(R.styleable.PPBannerView_pp_imageViewScaleType, 1)

        mScaleType = when (scaleType) {
            0 -> ImageView.ScaleType.FIT_XY
            1 -> ImageView.ScaleType.FIT_CENTER
            2 -> ImageView.ScaleType.CENTER
            3 -> ImageView.ScaleType.CENTER_CROP
            4 -> ImageView.ScaleType.CENTER_INSIDE
            else -> ImageView.ScaleType.FIT_CENTER
        }
        typeArray.recycle()


        mRecyclerView = RecyclerView(context)
        mLinearLayout = LinearLayout(context)
        LinearPagerSnapHelper().attachToRecyclerView(mRecyclerView)

        mRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mAdapter = PPAdapter()
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val first = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val last = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    if (first == last && mCurrentIndex != last) {
                        mCurrentIndex = last
                        if (isShowIndicators && isTouched) {
                            isTouched = false
                            switchIndicator()
                        }
                    }
                }
            }
        })

        mLinearLayout?.orientation = LinearLayout.HORIZONTAL
        mLinearLayout?.gravity = Gravity.CENTER
        val vplp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val linearlp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearlp.gravity = Gravity.BOTTOM or mGavity
        linearlp.setMargins(margin, margin, margin, margin)
        addView(mRecyclerView, vplp)
        addView(mLinearLayout, linearlp)

        if (isInEditMode) {
            for (index in 0..3) {
                mData.add("")
            }
            createIndicators()

        }

    }

    private fun createIndicators() {
        mLinearLayout?.removeAllViews()
        for (i in mData.indices) {
            val img = AppCompatImageView(context)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = mSpace / 2
            lp.rightMargin = mSpace / 2
            if (mDotSize >= PPUtils.dp2px(4)) {
                lp.width = mDotSize
                lp.height = mDotSize
            } else {
                img.minimumWidth = PPUtils.dp2px(2)
                img.minimumHeight = PPUtils.dp2px(2)
            }
            img.setImageDrawable(if (i == 0) mSelectedDrawable else mUnSelectedDrawable)
            mLinearLayout?.addView(img, lp)
        }
    }

    fun switchIndicator() {
        if (mLinearLayout?.childCount!! > 0) {
            for (i in 0 until mLinearLayout?.childCount!!) {
                (mLinearLayout?.getChildAt(i) as AppCompatImageView).setImageDrawable(
                        if (i == mCurrentIndex % mData.size) mSelectedDrawable else mUnSelectedDrawable
                )
            }
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = ev.x.toInt()
                mStartY = ev.y.toInt()
                parent.requestDisallowInterceptTouchEvent(true)

            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.x.toInt()
                val moveY = ev.y.toInt()
                val disX = moveX - mStartX
                val disy = moveY - mStartY
                val hasMoved = 2 * Math.abs(disX) > Math.abs(disy)
                parent.requestDisallowInterceptTouchEvent(hasMoved)
                if (hasMoved) {
                    setPlaying(false)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isPlaying.not()) {
                    isTouched = true
                    setPlaying(true)
                }
            }
        }
        return super.dispatchTouchEvent(ev)


    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setPlaying(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setPlaying(false)
    }


    override fun onWindowVisibilityChanged(visibility: Int) {
        setPlaying(!(visibility == View.GONE || visibility == View.INVISIBLE))
        super.onWindowVisibilityChanged(visibility)
    }

    inner class PPAdapter : RecyclerView.Adapter<PPAdapter.ImageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val imageView = AppCompatImageView(context)
            val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.layoutParams = params
            imageView.id = R.id.ppAppCompatImageView
            imageView.scaleType = mScaleType
            imageView.setOnClickListener {
                mOnBannerClickListener?.onClick(mCurrentIndex % mData.size)
            }
            return ImageViewHolder(imageView)
        }

        override fun getItemCount(): Int {
            return if (mData.size < 2) mData.size else Integer.MAX_VALUE
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            mOnBannerSwitchListener?.onSwitch(position % mData.size, holder.img)
        }

        inner class ImageViewHolder(var view: android.view.View) : RecyclerView.ViewHolder(view) {
            val img = view.findViewById<AppCompatImageView>(R.id.ppAppCompatImageView)!!
        }
    }


    inner class LinearPagerSnapHelper : LinearSnapHelper() {
        override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {

            var targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
            val currentView = findSnapView(layoutManager)
            if (targetPos != RecyclerView.NO_POSITION) {
                var currentPos = layoutManager!!.getPosition(currentView)
                val first = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val last = layoutManager.findLastVisibleItemPosition()
                currentPos = if (targetPos < currentPos) last else (if (targetPos > currentPos) first else currentPos)
                targetPos = if (targetPos < currentPos) currentPos - 1 else (if (targetPos > currentPos) currentPos + 1 else currentPos)
            }
            return targetPos
        }
    }

    fun generateDrawable(color: Int): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setSize(PPUtils.dp2px(6), PPUtils.dp2px(6))
        gradientDrawable.cornerRadius = PPUtils.dp2px(6).toFloat()
        gradientDrawable.setColor(color)
        return gradientDrawable
    }
}