package me.weishu.kernelsu.ui.component

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.screen.modulerepo.RepoModuleIcon
import me.weishu.kernelsu.ui.util.AppIconCache

/**
 * Icon in front of an installed module's name. The module id is the only app binding the metadata
 * carries: when it names an installed package that app's icon is used, otherwise the registry
 * icon of the same module id. Renders nothing when neither exists, so the card keeps its layout.
 */
@Composable
fun ModuleIcon(
    module: Module,
    repoIconUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    val context = LocalContext.current
    val applicationInfo = remember(module.id) {
        runCatching { context.packageManager.getApplicationInfo(module.id, 0) }.getOrNull()
    }
    if (applicationInfo != null) {
        ModuleAppIcon(applicationInfo, module.name, modifier, size)
    } else {
        RepoModuleIcon(repoIconUrl, modifier, size)
    }
}

@Composable
private fun ModuleAppIcon(
    applicationInfo: ApplicationInfo,
    label: String,
    modifier: Modifier,
    size: Dp,
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val bitmap by produceState(AppIconCache.getCached(applicationInfo, sizePx), applicationInfo, sizePx) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { AppIconCache.loadIcon(context, applicationInfo, sizePx) }
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = label,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp)),
        )
    }
}
