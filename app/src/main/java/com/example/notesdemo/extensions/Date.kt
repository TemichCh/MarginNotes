package com.example.notesdemo.extensions


import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

const val SECOND = 1000
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.shortFormat(): String {
    val pattern = if (this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.isSameDay(date: Date): Boolean {
    val day1 = this.time / DAY
    val day2 = date.time / DAY
    return day1 == day2

}

fun Date.add(value: Int, units: TimeUnits): Date {
    var time = this.time

    time += when (units) {
        TimeUnits.SECOND -> value.toLong() * SECOND
        TimeUnits.MINUTE -> value.toLong() * MINUTE
        TimeUnits.HOUR -> value.toLong() * HOUR
        TimeUnits.DAY -> value.toLong() * DAY
    }
    this.time = time
    return this
}


enum class TimeUnits {
    SECOND,
    MINUTE,
    HOUR,
    DAY
}

fun daysToString(l: Long, s: String, s1: String, s2: String): Any {
    val inL = abs(l) % 100
    val inL1 = l % 10

    when {
        (inL > 10 && inL < 20) -> return s2
        (inL1 > 1 && inL1 < 5) -> return s1
        (inL1 == 1.toLong()) -> return s
        else -> return s2
    }
}

fun Date.humanizeDiff(date: Date = Date()): String {
    val thisTime = date.time
    val anotherTime = this.time
    val t = thisTime - anotherTime
    println("${date.format()} , ${this.format()}")
    println("$thisTime , $anotherTime, $t")
    if (t > 0)
        when (t) {
            in (0L * SECOND..1L * SECOND) -> return "только что"
            in (1L * SECOND..45L * SECOND) -> return "несколько секунд назад"
            in (75L * SECOND..45L * MINUTE) -> return "${t / MINUTE} ${daysToString(
                t / MINUTE,
                "минуту",
                "минуты",
                "минут"
            )} назад"
            in (45L * MINUTE..75L * MINUTE) -> return "час назад"
            in (75L * MINUTE..22L * HOUR) -> return "${t / HOUR} ${daysToString(
                t / HOUR,
                "час",
                "часа",
                "часов"
            )} назад"
            in (22L * HOUR..26L * HOUR) -> return "день назад"
            in (26L * HOUR..360L * DAY) -> {
                return "${t / DAY} ${daysToString(t / DAY, "день", "дня", "дней")} назад"
            }
            else -> return "более года назад"
        }
    else
        when (t.absoluteValue) { /*1L * SECOND) -> return "только что"
            in (1L * SECOND..*/
            in (0L * SECOND..45L * SECOND) -> return "через несколько секунд"
            in (75L * SECOND..45L * MINUTE) -> return "через ${t.absoluteValue / MINUTE} ${daysToString(
                t.absoluteValue / MINUTE,
                "минуту",
                "минуты",
                "минут"
            )}"
            in (45L * MINUTE..75L * MINUTE) -> return "через час"
            in (75L * MINUTE..22L * HOUR) -> return "через ${t.absoluteValue / HOUR} ${daysToString(
                t.absoluteValue / HOUR,
                "час",
                "часа",
                "часов"
            )}"
            in (22L * HOUR..26L * HOUR) -> return "через день"
            in (26L * HOUR..360L * DAY) -> {
                return "через ${t.absoluteValue / DAY} ${daysToString(
                    t.absoluteValue / DAY,
                    "день",
                    "дня",
                    "дней"
                )}"
            }
            else -> return "более чем через год"
        }

}

