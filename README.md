# ppbannerview
kotlin编写的横向rv显示banner，已发布在jitpack平台，在RecyclerViewBanner基础上开发，[感谢loonggg的RecyclerViewBanner](https://github.com/loonggg/RecyclerViewBanner)

![](https://jitpack.io/v/gdmec07120731/ppbannerview.svg)

支持以下特点

1. 轮播开关
1. 轮播间隔
1. dot显示图片或颜色
1. dot大小
1. dot间隔
1. banner图片ScaleType
1. dot位置
1. 是否显示dot

![](https://s1.ax1x.com/2018/05/14/CrKRSI.jpg)

### 使用方法
```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven {url "https://jitpack.io"} //add
    }
}

dependencies {
    implementation 'com.android.support:appcompat-v7:27.1.1'
    implementation 'com.github.gdmec07120731:ppbannerview:1.1' //add
    }
```

```xml
<com.ppjun.android.ppbannerview.PPBannerView
        android:id="@+id/pp"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:background="@color/white"
        app:pp_autoPlay="true"  //自动化播放         
        app:pp_imageViewScaleType="FIT_CENTER" //图片scaletype
        app:pp_indicatorGravity="right" //dot显示位置
        app:pp_indicatorSelectedSrc="@color/colorAccent" //dot选中颜色
        app:pp_indicatorUnSelectedSrc="@color/normal_bg"  //dot默认颜色
        app:pp_interval="5000"  //播放间隔时间
        app:pp_showIndicator="true" />  //是否显示dot
```


#### 在kotlin上的使用
```kotlin
 val list = ArrayList<String>()
        list.add("https://y.zdmimg.com/201801/06/5a50ac71168fc2320.jpg_d200.jpg")
        list.add("https://qna.smzdm.com/201805/11/5af51a9811b295276.jpg_a200.jpg")
        list.add("https://qny.smzdm.com/201805/13/5af8366c02b294584.jpg_d200.jpg")
        ppbanner.setBannerData(list)
        ppbanner.onBannerSwitchListener=object:PPBannerView.OnBannerSwitchListener{
            override fun onSwitch(position: Int, imageView: AppCompatImageView) {
                Glide.with(this@MainActivity).load(list[position]).into(imageView)
            }
        }
```
