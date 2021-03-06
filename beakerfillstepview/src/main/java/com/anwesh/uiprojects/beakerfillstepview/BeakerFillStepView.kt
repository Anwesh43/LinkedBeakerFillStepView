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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBeakerFillStep(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf2 : Float = sf.divideScale(1, parts + 1)
    val sf4 : Float = sf.divideScale(3, parts + 1)
    val sw : Float = w / sizeFactor
    val sh : Float = h / sizeFactor
    val beakerH = h / beakerSizeFactor
    save()
    translate(w / 2 - sw * 0.5f, h / 2 - sh * 0.5f)
    for (j in 0..1) {
        val sfj : Float = sf.divideScale(j * 2, parts + 1)
        val sj : Float = 1f - 2 * j
        save()
        translate(sw * j, sh * j)
        drawLine(0f, 0f, 0f, sh * sfj * sj, paint)
        restore()
    }
    save()
    translate(0f, sh)
    drawLine(0f, 0f, sw * sf2, 0f, paint)
    paint.color = waterColor
    drawRect(RectF(0f, -beakerH * sf4, sw, 0f), paint)
    restore()
    restore()
}

fun Canvas.drawBFSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawBeakerFillStep(scale, w, h, paint)
}

class BeakerFillStepView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class BFSNode(var i : Int, val state : State = State()) {

        private var next : BFSNode? = null
        private var prev : BFSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BFSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBFSNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BFSNode {
            var curr : BFSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BeakerFillStep(var i : Int) {

        private var curr : BFSNode = BFSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class Renderer(var view : BeakerFillStepView) {

        private val animator : Animator = Animator(view)
        private val bfs : BeakerFillStep = BeakerFillStep(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bfs.draw(canvas, paint)
            animator.animate {
                bfs.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bfs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BeakerFillStepView {
            val view : BeakerFillStepView = BeakerFillStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}