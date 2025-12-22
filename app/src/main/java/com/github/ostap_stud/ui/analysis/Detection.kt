package com.github.ostap_stud.ui.analysis

open class Detection(
    open val x1: Float,
    open val y1: Float,
    open val x2: Float,
    open val y2: Float,
    open val score: Float,
    open val cls: String
){
    fun copy(
        x1: Float = this.x1,
        y1: Float = this.y1,
        x2: Float = this.x2,
        y2: Float = this.y2,
        score: Float = this.score,
        cls: String = this.cls
    ): Detection{
        return Detection(x1 = x1, y1 = y1, x2 = x2, y2 = y2, score = score, cls = cls)
    }
}

data class LicenseDetection(
    override val x1: Float,
    override val y1: Float,
    override val x2: Float,
    override val y2: Float,
    override val score: Float,
    override val cls: String,
    var numberText: String = ""
) : Detection(x1, y1, x2, y2, score, cls)
