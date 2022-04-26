package com.sharkaboi.yogapartner.ml.utils

import com.google.common.primitives.Floats
import com.google.mlkit.vision.common.PointF3D
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Utility methods for operations on [PointF3D].
 */
object PointF3DUtils {
    @JvmStatic
    fun add(a: PointF3D, b: PointF3D): PointF3D {
        return PointF3D.from(a.x + b.x, a.y + b.y, a.z + b.z)
    }

    @JvmStatic
    fun subtract(b: PointF3D, a: PointF3D): PointF3D {
        return PointF3D.from(a.x - b.x, a.y - b.y, a.z - b.z)
    }

    @JvmStatic
    fun multiply(a: PointF3D, multiple: Float): PointF3D {
        return PointF3D.from(a.x * multiple, a.y * multiple, a.z * multiple)
    }

    @JvmStatic
    fun multiply(a: PointF3D, multiple: PointF3D): PointF3D {
        return PointF3D.from(
            a.x * multiple.x, a.y * multiple.y, a.z * multiple.z
        )
    }

    @JvmStatic
    fun average(a: PointF3D, b: PointF3D): PointF3D {
        return PointF3D.from(
            (a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f, (a.z + b.z) * 0.5f
        )
    }

    @JvmStatic
    fun l2Norm2D(point: PointF3D): Float {
        return hypot(point.x.toDouble(), point.y.toDouble()).toFloat()
    }

    @JvmStatic
    fun maxAbs(point: PointF3D): Float {
        return Floats.max(abs(point.x), abs(point.y), abs(point.z))
    }

    @JvmStatic
    fun sumAbs(point: PointF3D): Float {
        return abs(point.x) + abs(point.y) + abs(point.z)
    }

    @JvmStatic
    fun addAll(pointsList: MutableList<PointF3D>, p: PointF3D) {
        val iterator = pointsList.listIterator()
        while (iterator.hasNext()) {
            iterator.set(add(iterator.next(), p))
        }
    }

    @JvmStatic
    fun subtractAll(p: PointF3D, pointsList: MutableList<PointF3D>) {
        val iterator = pointsList.listIterator()
        while (iterator.hasNext()) {
            iterator.set(subtract(p, iterator.next()))
        }
    }

    @JvmStatic
    fun multiplyAll(pointsList: MutableList<PointF3D>, multiple: Float) {
        val iterator = pointsList.listIterator()
        while (iterator.hasNext()) {
            iterator.set(multiply(iterator.next(), multiple))
        }
    }

    @JvmStatic
    fun multiplyAll(pointsList: MutableList<PointF3D>, multiple: PointF3D) {
        val iterator = pointsList.listIterator()
        while (iterator.hasNext()) {
            iterator.set(multiply(iterator.next(), multiple))
        }
    }
}
