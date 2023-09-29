package com.zml.skin.chart

import android.animation.TypeEvaluator


class ChartDifEvaluator : TypeEvaluator<ChartView.DiffEval> {
    override fun evaluate(fraction: Float, startValue: ChartView.DiffEval?, endValue: ChartView.DiffEval?): ChartView.DiffEval {

        val start: MutableList<Float> = startValue?.diffs!!
        val end: MutableList<Float> = endValue?.diffs!!
        val endValue = arrayListOf<Float>()
        for (i in 0 until end.size){
            if (i>=start.size){
                endValue.add(( fraction * (end[i] - 0)))
            }else{
                endValue.add((start[i] + fraction * (end[i] - start[i])))
            }
        }

        return ChartView.DiffEval(endValue)


    }
}