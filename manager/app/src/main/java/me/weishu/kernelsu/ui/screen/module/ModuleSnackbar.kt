package me.weishu.kernelsu.ui.screen.module

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Snackbar slot of the module page, anchored just above the app's bottom bar.
 *
 * [bottomBarPadding] is the bar height reported by the app scaffold, which already contains the
 * system bar inset, so the snackbar clears the gesture area without applying that inset again.
 * Both snackbar hosts keep 12 dp of their own padding at the bottom, which is the gap above the bar.
 * [onHeightChanged] reports the measured host height for [rememberSnackbarClearance].
 */
@Composable
internal fun BoxScope.ModuleSnackbar(
    bottomBarPadding: Dp,
    onHeightChanged: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = bottomBarPadding),
    ) {
        // Separate node so the reported height is the snackbar alone, without the bar padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { onHeightChanged(it.height) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            content()
        }
    }
}

/**
 * Distance the install button moves up while a snackbar is shown, so the button never overlaps it
 * and returns to its original place once the snackbar is gone.
 */
@Composable
internal fun rememberSnackbarClearance(shown: Boolean, hostHeightPx: Int): Dp {
    val target = if (shown) with(LocalDensity.current) { hostHeightPx.toDp() } else 0.dp
    return animateDpAsState(targetValue = target, label = "snackbarClearance").value
}
