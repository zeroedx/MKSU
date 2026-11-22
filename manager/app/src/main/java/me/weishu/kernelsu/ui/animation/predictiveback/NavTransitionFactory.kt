package me.weishu.kernelsu.ui.animation.predictiveback

import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitions

const val PREDICTIVE_BACK_MIUIX = "miuix"
const val PREDICTIVE_BACK_AOSP = "aosp"
const val PREDICTIVE_BACK_SCALE = "scale"
const val PREDICTIVE_BACK_CLASSIC = "ksu_classic"
const val PREDICTIVE_BACK_NONE = "none"

/**
 * Values saved by older builds: "default" was the miuix transition and "slide" never had its own
 * implementation.
 */
private fun normalize(value: String): String = when (value) {
    "default" -> PREDICTIVE_BACK_MIUIX
    "slide" -> PREDICTIVE_BACK_AOSP
    else -> value
}

fun predictiveBackAnimationIndex(value: String): Int = when (normalize(value)) {
    PREDICTIVE_BACK_AOSP -> 1
    PREDICTIVE_BACK_SCALE -> 2
    PREDICTIVE_BACK_CLASSIC -> 3
    PREDICTIVE_BACK_NONE -> 4
    else -> 0
}

fun predictiveBackAnimationValue(index: Int): String = when (index) {
    1 -> PREDICTIVE_BACK_AOSP
    2 -> PREDICTIVE_BACK_SCALE
    3 -> PREDICTIVE_BACK_CLASSIC
    4 -> PREDICTIVE_BACK_NONE
    else -> PREDICTIVE_BACK_MIUIX
}

fun navTransitionFor(value: String): NavTransition = when (normalize(value)) {
    PREDICTIVE_BACK_AOSP -> AospNavTransition
    PREDICTIVE_BACK_SCALE -> scaleNavTransition()
    PREDICTIVE_BACK_CLASSIC -> ClassicNavTransition
    // "none" disables the predictive gesture animation; a regular back still animates.
    PREDICTIVE_BACK_NONE -> NoPredictiveBackTransition
    else -> NavTransitions.MiuixDefault
}
