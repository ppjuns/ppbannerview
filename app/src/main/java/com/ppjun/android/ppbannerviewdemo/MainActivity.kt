package com.ppjun.android.ppbannerviewdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.ppjun.android.ppbannerview.PPBannerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = ArrayList<String>()
        list.add("https://y.zdmimg.com/201801/06/5a50ac71168fc2320.jpg_d200.jpg")
        list.add("https://qna.smzdm.com/201805/11/5af51a9811b295276.jpg_a200.jpg")
        list.add("https://qny.smzdm.com/201805/13/5af8366c02b294584.jpg_d200.jpg")
        ppbanner.setBannerData(list)
        ppbanner.onBannerSwitchListener=object:PPBannerView.OnBannerSwitchListener{
            override fun onSwitch(position: Int, imageView: AppCompatImageView) {
                imageView.setBackgroundResource(R.color.colorAccent)
                Glide.with(this@MainActivity).load(list[position]).into(imageView)
            }
        }
    }
}
