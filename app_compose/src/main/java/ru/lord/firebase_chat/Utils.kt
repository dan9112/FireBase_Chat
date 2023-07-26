package ru.lord.firebase_chat

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlin.math.ceil

fun Resources.getUriById(@DrawableRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(getResourcePackageName(resourceId))
    .appendPath(getResourceTypeName(resourceId))
    .appendPath(getResourceEntryName(resourceId))
    .build()

@Composable
fun WrapTextContent(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    SubcomposeLayout(modifier) { constraints ->
        val composable = @Composable { localOnTextLayout: (TextLayoutResult) -> Unit ->
            Text(
                text = text,
                modifier = modifier,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = localOnTextLayout,
                style = style
            )
        }
        var textWidthOpt: Int? = null
        subcompose("measureView") {
            composable { layoutResult ->
                textWidthOpt = (0 until layoutResult.lineCount)
                    .maxOf { line ->
                        ceil(layoutResult.getLineRight(line) - layoutResult.getLineLeft(line)).toInt()
                    }
            }
        }[0].measure(constraints)
        val textWidth = textWidthOpt!!
        val placeable = subcompose("content") {
            composable(onTextLayout)
        }[0].measure(constraints.copy(minWidth = textWidth, maxWidth = textWidth))

        layout(width = textWidth, height = placeable.height) {
            placeable.place(0, 0)
        }
    }
}

fun LazyListState.isScrolledToTheEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
