package me.weishu.kernelsu.ui.animation.predictiveback

import org.junit.Assert.assertEquals
import org.junit.Test
import top.yukonga.miuix.kmp.nav.transition.NavTransitions

class NavTransitionFactoryTest {

    @Test
    fun `every option index maps to a distinct stored value`() {
        val values = (0..4).map { predictiveBackAnimationValue(it) }
        assertEquals(
            listOf(
                PREDICTIVE_BACK_MIUIX,
                PREDICTIVE_BACK_AOSP,
                PREDICTIVE_BACK_SCALE,
                PREDICTIVE_BACK_CLASSIC,
                PREDICTIVE_BACK_NONE,
            ),
            values,
        )
        assertEquals(values.size, values.toSet().size)
    }

    @Test
    fun `index and value round trip`() {
        (0..4).forEach { index ->
            assertEquals(index, predictiveBackAnimationIndex(predictiveBackAnimationValue(index)))
        }
    }

    @Test
    fun `legacy values keep working`() {
        assertEquals(0, predictiveBackAnimationIndex("default"))
        assertEquals(1, predictiveBackAnimationIndex("slide"))
        assertEquals(0, predictiveBackAnimationIndex(""))
        assertEquals(NavTransitions.MiuixDefault, navTransitionFor("default"))
        assertEquals(AospNavTransition, navTransitionFor("slide"))
    }

    @Test
    fun `styles resolve to distinct transitions`() {
        val transitions = listOf(
            PREDICTIVE_BACK_MIUIX,
            PREDICTIVE_BACK_AOSP,
            PREDICTIVE_BACK_SCALE,
            PREDICTIVE_BACK_CLASSIC,
            PREDICTIVE_BACK_NONE,
        ).map { navTransitionFor(it) }

        assertEquals(transitions.size, transitions.toSet().size)
        assertEquals(NoPredictiveBackTransition, navTransitionFor(PREDICTIVE_BACK_NONE))
    }
}
