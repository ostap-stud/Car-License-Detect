package com.github.ostap_stud.analysis

import kotlin.math.max
import kotlin.math.min

object NonMaximumSuppression {

    fun nms(
        detections: List<Detection>,
        iouThreshold: Float = 0.45f
    ): List<Detection> {

        val result = mutableListOf<Detection>()
        val sorted = detections.sortedByDescending { it.score }.toMutableList()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            result.add(best)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val other = iterator.next()
                val iou = iou(best, other)
                if (iou > iouThreshold) iterator.remove()
            }
        }

        return result
    }

    private fun iou(a: Detection, b: Detection): Float {
        val x1 = max(a.x1, b.x1)
        val y1 = max(a.y1, b.y1)
        val x2 = min(a.x2, b.x2)
        val y2 = min(a.y2, b.y2)

        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        if (inter <= 0f) return 0f

        val areaA = (a.x2 - a.x1) * (a.y2 - a.y1)
        val areaB = (b.x2 - b.x1) * (b.y2 - b.y1)

        return inter / (areaA + areaB - inter)
    }

}