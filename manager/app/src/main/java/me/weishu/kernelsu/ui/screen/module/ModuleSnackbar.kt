package me.weishu.kernelsu.ui.screen.module

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Snackbar slot of the module page, anchored just above the app's bottom bar.
 *
 * [bottomBarPadding] is the bar height reported by the app scaffold, which already contains the
 * system bar inset, so the snackbar clears the gesture area without applying that inset again.
 * Both snackbar hosts keep 12 dp of their own padding at the bottom, which is the gap above the bar.
 */
@Composable
internal fun BoxScope.ModuleSnackbar(
    bottomBarPadding: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = bottomBarPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        content()
    }
}
