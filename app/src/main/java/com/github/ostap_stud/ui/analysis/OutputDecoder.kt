package com.github.ostap_stud.ui.analysis

object OutputDecoder {

    fun decode(
        output: Array<Array<FloatArray>>,
        confThresh: Float = 0.4f,
        classId: Int? = null
    ): List<Detection> {
        val rows = output[0][0].size
        val classes = output[0].size - 4

        val detections = mutableListOf<Detection>()

        for (i in 0 until rows) {

            val cx = output[0][0][i]
            val cy = output[0][1][i]
            val w  = output[0][2][i]
            val h  = output[0][3][i]

            val x1 = cx - w / 2f
            val y1 = cy - h / 2f
            val x2 = cx + w / 2f
            val y2 = cy + h / 2f

            var bestClass = -1
            var bestScore = 0f

            if (classId != null){
                val s = output[0][4 + classId][i]
                if (s > bestScore) {
                    bestScore = s
                    bestClass = classId
                }
            } else{
                for (cls in 0 until classes) {
                    val s = output[0][4 + cls][i]
                    if (s > bestScore) {
                        bestScore = s
                        bestClass = cls
                    }
                }
            }

            if (bestScore >= confThresh) {
                detections.add(
                    Detection(x1, y1, x2, y2, bestScore, CarLicenseImageAnalyzer.LABELS[bestClass] ?: "Nothing")
                )
            }
        }

        return detections
    }

    fun ctcDecode(output: Array<Array<FloatArray>>): String {
        val sb = StringBuilder()
        var last = -1

        for (t in output[0]) {
            val idx = t.indices.maxBy { t[it] }
            if (idx != last && idx != BLANK) {
                sb.append(charMap[idx])
            }
            last = idx
        }

        return sb.toString()
    }

    private const val BLANK = 0
//    private val charMap: CharArray = mutableListOf('_')
//        .apply { addAll(('0'..'9') + ('A'..'Z')) }
//        .toCharArray()
    private val charMap: CharArray = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray()

}