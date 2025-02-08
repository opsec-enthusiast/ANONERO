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


    val ArrowUpRight: ImageVector
        get() {
            if (_ArrowUpRight != null) {
                return _ArrowUpRight!!
            }
            _ArrowUpRight = ImageVector.Builder(
                name = "ArrowUpRight",
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
                    moveTo(7f, 7f)
                    horizontalLineToRelative(10f)
                    verticalLineToRelative(10f)
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
                    moveTo(7f, 17f)
                    lineTo(17f, 7f)
                }
            }.build()
            return _ArrowUpRight!!
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


    val History: ImageVector
        get() {
            if (_History != null) {
                return _History!!
            }
            _History = ImageVector.Builder(
                name = "History",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(480f, 840f)
                    quadToRelative(-138f, 0f, -240.5f, -91.5f)
                    reflectiveQuadTo(122f, 520f)
                    horizontalLineToRelative(82f)
                    quadToRelative(14f, 104f, 92.5f, 172f)
                    reflectiveQuadTo(480f, 760f)
                    quadToRelative(117f, 0f, 198.5f, -81.5f)
                    reflectiveQuadTo(760f, 480f)
                    reflectiveQuadToRelative(-81.5f, -198.5f)
                    reflectiveQuadTo(480f, 200f)
                    quadToRelative(-69f, 0f, -129f, 32f)
                    reflectiveQuadToRelative(-101f, 88f)
                    horizontalLineToRelative(110f)
                    verticalLineToRelative(80f)
                    horizontalLineTo(120f)
                    verticalLineToRelative(-240f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(94f)
                    quadToRelative(51f, -64f, 124.5f, -99f)
                    reflectiveQuadTo(480f, 120f)
                    quadToRelative(75f, 0f, 140.5f, 28.5f)
                    reflectiveQuadToRelative(114f, 77f)
                    reflectiveQuadToRelative(77f, 114f)
                    reflectiveQuadTo(840f, 480f)
                    reflectiveQuadToRelative(-28.5f, 140.5f)
                    reflectiveQuadToRelative(-77f, 114f)
                    reflectiveQuadToRelative(-114f, 77f)
                    reflectiveQuadTo(480f, 840f)
                    moveToRelative(112f, -192f)
                    lineTo(440f, 496f)
                    verticalLineToRelative(-216f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(184f)
                    lineToRelative(128f, 128f)
                    close()
                }
            }.build()
            return _History!!
        }


    val ArrowDownLeft: ImageVector
        get() {
            if (_ArrowDownLeft != null) {
                return _ArrowDownLeft!!
            }
            _ArrowDownLeft = ImageVector.Builder(
                name = "ArrowDownLeft",
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
                    moveTo(17f, 7f)
                    lineTo(7f, 17f)
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
                    moveTo(17f, 17f)
                    horizontalLineTo(7f)
                    verticalLineTo(7f)
                }
            }.build()
            return _ArrowDownLeft!!
        }


    val Share_log: ImageVector
        get() {
            if (_Share != null) {
                return _Share!!
            }
            _Share = ImageVector.Builder(
                name = "Share_windows",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(280f, 600f)
                    verticalLineToRelative(-240f)
                    quadToRelative(0f, -33f, 23.5f, -56.5f)
                    reflectiveQuadTo(360f, 280f)
                    horizontalLineToRelative(326f)
                    lineTo(583f, 177f)
                    lineToRelative(57f, -57f)
                    lineToRelative(200f, 200f)
                    lineToRelative(-200f, 200f)
                    lineToRelative(-57f, -56f)
                    lineToRelative(103f, -104f)
                    horizontalLineTo(360f)
                    verticalLineToRelative(240f)
                    close()
                    moveToRelative(-80f, 240f)
                    quadToRelative(-33f, 0f, -56.5f, -23.5f)
                    reflectiveQuadTo(120f, 760f)
                    verticalLineToRelative(-600f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(600f)
                    horizontalLineToRelative(480f)
                    verticalLineToRelative(-160f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(160f)
                    quadToRelative(0f, 33f, -23.5f, 56.5f)
                    reflectiveQuadTo(680f, 840f)
                    close()
                }
            }.build()
            return _Share!!
        }

    val Clear_all: ImageVector
        get() {
            if (_Clear_all != null) {
                return _Clear_all!!
            }
            _Clear_all = ImageVector.Builder(
                name = "Clear_all",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(120f, 680f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(560f)
                    verticalLineToRelative(80f)
                    close()
                    moveToRelative(80f, -160f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(560f)
                    verticalLineToRelative(80f)
                    close()
                    moveToRelative(80f, -160f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(560f)
                    verticalLineToRelative(80f)
                    close()
                }
            }.build()
            return _Clear_all!!
        }


    val FileEarmarkLock: ImageVector
        get() {
            if (_FileEarmarkLock != null) {
                return _FileEarmarkLock!!
            }
            _FileEarmarkLock = ImageVector.Builder(
                name = "FileEarmarkLock",
                defaultWidth = 16.dp,
                defaultHeight = 16.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(10f, 7f)
                    verticalLineToRelative(1.076f)
                    curveToRelative(0.54f, 0.166f, 1f, 0.597f, 1f, 1.224f)
                    verticalLineToRelative(2.4f)
                    curveToRelative(0f, 0.816f, -0.781f, 1.3f, -1.5f, 1.3f)
                    horizontalLineToRelative(-3f)
                    curveToRelative(-0.719f, 0f, -1.5f, -0.484f, -1.5f, -1.3f)
                    verticalLineTo(9.3f)
                    curveToRelative(0f, -0.627f, 0.46f, -1.058f, 1f, -1.224f)
                    verticalLineTo(7f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4f, 0f)
                    moveTo(7f, 7f)
                    verticalLineToRelative(1f)
                    horizontalLineToRelative(2f)
                    verticalLineTo(7f)
                    arcToRelative(
                        1f,
                        1f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -2f,
                        0f
                    )
                    moveTo(6f, 9.3f)
                    verticalLineToRelative(2.4f)
                    curveToRelative(0f, 0.042f, 0.02f, 0.107f, 0.105f, 0.175f)
                    arcTo(
                        0.64f,
                        0.64f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        6.5f,
                        12f
                    )
                    horizontalLineToRelative(3f)
                    arcToRelative(
                        0.64f,
                        0.64f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        0.395f,
                        -0.125f
                    )
                    curveToRelative(0.085f, -0.068f, 0.105f, -0.133f, 0.105f, -0.175f)
                    verticalLineTo(9.3f)
                    curveToRelative(0f, -0.042f, -0.02f, -0.107f, -0.105f, -0.175f)
                    arcTo(0.64f, 0.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.5f, 9f)
                    horizontalLineToRelative(-3f)
                    arcToRelative(
                        0.64f,
                        0.64f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -0.395f,
                        0.125f
                    )
                    curveTo(6.02f, 9.193f, 6f, 9.258f, 6f, 9.3f)
                }
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(14f, 14f)
                    verticalLineTo(4.5f)
                    lineTo(9.5f, 0f)
                    horizontalLineTo(4f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -2f,
                        2f
                    )
                    verticalLineToRelative(12f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2f)
                    horizontalLineToRelative(8f)
                    arcToRelative(
                        2f,
                        2f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        2f,
                        -2f
                    )
                    moveTo(9.5f, 3f)
                    arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11f, 4.5f)
                    horizontalLineToRelative(2f)
                    verticalLineTo(14f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 1f)
                    horizontalLineTo(4f)
                    arcToRelative(
                        1f,
                        1f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -1f,
                        -1f
                    )
                    verticalLineTo(2f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -1f)
                    horizontalLineToRelative(5.5f)
                    close()
                }
            }.build()
            return _FileEarmarkLock!!
        }


    val Scan: ImageVector
        get() {
            if (_Scan != null) {
                return _Scan!!
            }
            _Scan = ImageVector.Builder(
                name = "Scan",
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
                    moveTo(3f, 7f)
                    verticalLineTo(5f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, -2f)
                    horizontalLineToRelative(2f)
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
                    horizontalLineToRelative(2f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2f)
                    verticalLineToRelative(2f)
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
                    moveTo(21f, 17f)
                    verticalLineToRelative(2f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                    horizontalLineToRelative(-2f)
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
                    moveTo(7f, 21f)
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
                    verticalLineToRelative(-2f)
                }
            }.build()
            return _Scan!!
        }

}

private var _Backspace: ImageVector? = null


private var _QrCode: ImageVector? = null

private var _Home: ImageVector? = null


private var _History: ImageVector? = null


private var _ArrowUpRight: ImageVector? = null


private var _ArrowDownLeft: ImageVector? = null

private var _Share: ImageVector? = null


private var _Clear_all: ImageVector? = null


private var _FileEarmarkLock: ImageVector? = null

private var _Scan: ImageVector? = null
