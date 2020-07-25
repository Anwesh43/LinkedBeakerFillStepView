package com.anwesh.uiprojects.beakerfillstepview

/**
 * Created by anweshmishra on 26/07/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF

val colors : Array<String> = arrayOf("#3F51B5", "#F44336","#4CAF50", "#FF9800", "#FFEB3B")
val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 2f
val beakerSizeFactor : Float = 4f
val waterColor : Int = Color.parseColor("#03A9F4")
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

