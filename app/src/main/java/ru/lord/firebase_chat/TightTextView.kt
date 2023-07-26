package ru.lord.firebase_chat

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.roundToInt

/**
 * Tightly wraps the text when setting the maxWidth.
 * @author sky
 */
class TightTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatTextView(context, attrs, defStyle) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val specModeW = MeasureSpec.getMode(widthMeasureSpec)
        if (specModeW != MeasureSpec.EXACTLY) {
            val layout = layout
            val linesCount = layout.lineCount
            if (linesCount > 1) {
                var textRealWidth = 0f
                for (n in 0 until linesCount) {
                    textRealWidth = maxOf(textRealWidth, layout.getLineWidth(n))
                }
                val w = textRealWidth.roundToInt()
                if (w < measuredWidth) super.onMeasure(
                    MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                    heightMeasureSpec
                )
            }
        }
    }
}
