package view

import java.util.*
import kotlin.math.max
import kotlin.math.min

enum class InterpolationType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
}
class Animation (
    var startTime :Long = time,
    var duration: Long = 1000,
    var interpolationType: InterpolationType = InterpolationType.LINEAR
){
    fun reset() {
        startTime = time
    }

    fun jumpTo(delta: Double) {
        startTime = time - (duration * delta).toLong()
    }

    fun eval() : Double {
        var t = (time - startTime).toDouble() / duration
        t = max(0.0, min(1.0, t))

        return when(interpolationType) {
            InterpolationType.LINEAR -> t
            InterpolationType.EASE_IN -> t * t
            InterpolationType.EASE_OUT -> t * (2.0 - t)
            InterpolationType.EASE_IN_OUT -> -2.0 * t * t * t + 3.0 * t * t
        }
    }

    companion object {
        var time: Long = 0
        val listOfRegisteredCallbacks = mutableListOf<() -> Unit>()
        val fps : Long = 60

        init {
            val timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    time = System.nanoTime()
                    listOfRegisteredCallbacks.forEach { it() }
                }
            }, 0, 1000 / fps)
        }

        fun registerCallback(callback: () -> Unit) {
            listOfRegisteredCallbacks.add(callback)
        }

    }
}