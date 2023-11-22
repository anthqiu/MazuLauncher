package xyz.qhurc.mazulauncher

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times

object DensityUtil {
    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    fun dpToPx(context: Context, dpValue: Dp): Int {
        // 获取屏幕密度
        val scale = context.resources.displayMetrics.density
        // 结果+0.5是为了int取整时更接近
        return (scale * dpValue.value + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    fun pxToDp(context: Context, pxValue: Float): Dp {
        val scale = context.resources.displayMetrics.density
        return Dp(pxValue / scale + 0.5f)
    }
}