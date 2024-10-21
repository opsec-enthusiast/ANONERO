package io.anonero.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


object AnonIcons {


    val Backspace: ImageVector
        get() {
            if (_Backspace != null) {
                return _Backspace!!
            }
            _Backspace = ImageVector.Builder(
                name = "Backspace",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF0F172A)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 9.75f)
                    lineTo(14.25f, 12f)
                    moveTo(14.25f, 12f)
                    lineTo(16.5f, 14.25f)
                    moveTo(14.25f, 12f)
                    lineTo(16.5f, 9.75f)
                    moveTo(14.25f, 12f)
                    lineTo(12f, 14.25f)
                    moveTo(9.42051f, 19.1705f)
                    lineTo(3.04551f, 12.7955f)
                    curveTo(2.6062f, 12.3562f, 2.6062f, 11.6438f, 3.0455f, 11.2045f)
                    lineTo(9.42051f, 4.82951f)
                    curveTo(9.6315f, 4.6185f, 9.9176f, 4.5f, 10.216f, 4.5f)
                    lineTo(19.5f, 4.5f)
                    curveTo(20.7427f, 4.5f, 21.75f, 5.5074f, 21.75f, 6.75f)
                    verticalLineTo(17.25f)
                    curveTo(21.75f, 18.4926f, 20.7427f, 19.5f, 19.5f, 19.5f)
                    horizontalLineTo(10.216f)
                    curveTo(9.9176f, 19.5f, 9.6315f, 19.3815f, 9.4205f, 19.1705f)
                    close()
                }
            }.build()
            return _Backspace!!
        }


    val QrCode: ImageVector
        get() {
            if (_QrCode != null) {
                return _QrCode!!
            }
            _QrCode = ImageVector.Builder(
                name = "QrCode",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 3f)
                    horizontalLineTo(7f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 4f)
                    verticalLineTo(7f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 8f)
                    horizontalLineTo(4f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 7f)
                    verticalLineTo(4f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 3f)
                    close()
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(17f, 3f)
                    horizontalLineTo(20f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 4f)
                    verticalLineTo(7f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 8f)
                    horizontalLineTo(17f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 7f)
                    verticalLineTo(4f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 3f)
                    close()
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 16f)
                    horizontalLineTo(7f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 17f)
                    verticalLineTo(20f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 21f)
                    horizontalLineTo(4f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 20f)
                    verticalLineTo(17f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 16f)
                    close()
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(21f, 16f)
                    horizontalLineToRelative(-3f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -2f,
                        2f
                    )
                    verticalLineToRelative(3f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(21f, 21f)
                    verticalLineToRelative(0.01f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 7f)
                    verticalLineToRelative(3f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                    horizontalLineTo(7f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(3f, 12f)
                    horizontalLineToRelative(0.01f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 3f)
                    horizontalLineToRelative(0.01f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 16f)
                    verticalLineToRelative(0.01f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(16f, 12f)
                    horizontalLineToRelative(1f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(21f, 12f)
                    verticalLineToRelative(0.01f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 21f)
                    verticalLineToRelative(-1f)
                }
            }.build()
            return _QrCode!!
        }

    val Home: ImageVector
        get() {
            if (_Home != null) {
                return _Home!!
            }
            _Home = ImageVector.Builder(
                name = "Home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(15f, 21f)
                    verticalLineToRelative(-8f)
                    arcToRelative(
                        1f,
                        1f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -1f,
                        -1f
                    )
                    horizontalLineToRelative(-4f)
                    arcToRelative(
                        1f,
                        1f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -1f,
                        1f
                    )
                    verticalLineToRelative(8f)
                }
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(3f, 10f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        0.709f,
                        -1.528f
                    )
                    lineToRelative(7f, -5.999f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        2.582f,
                        0f
                    )
                    lineToRelative(7f, 5.999f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 10f)
                    verticalLineToRelative(9f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                    horizontalLineTo(5f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -2f,
                        -2f
                    )
                    close()
                }
            }.build()
            return _Home!!
        }

}

private var _Backspace: ImageVector? = null


private var _QrCode: ImageVector? = null

private var _Home: ImageVector? = null
