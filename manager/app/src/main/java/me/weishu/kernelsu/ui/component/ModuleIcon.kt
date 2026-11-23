package me.weishu.kernelsu.ui.component

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.screen.modulerepo.RepoModuleIcon
import me.weishu.kernelsu.ui.util.AppIconCache

/**
 * Icon in front of an installed module's name, from the first source that resolves: the icon of the
 * module's verified companion app, the registry icon of the module, then the project icon the
 * manager bundles for it. Renders nothing when all three are missing, so the card keeps its layout.
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
        ModuleIconCatalog.companionPackage(module.id)
            ?.let { pkg -> runCatching { context.packageManager.getApplicationInfo(pkg, 0) }.getOrNull() }
    }
    val bundledIcon = ModuleIconCatalog.bundledIcon(module.id)
    val fallback: (@Composable () -> Unit)? = bundledIcon?.let { res ->
        { BundledIcon(res, modifier, size) }
    }

    when (val state = rememberAppIcon(applicationInfo, size)) {
        is AppIconState.Loaded -> Image(
            bitmap = state.bitmap.asImageBitmap(),
            contentDescription = module.name,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp)),
        )
        AppIconState.Loading -> Unit
        AppIconState.Missing -> RepoModuleIcon(
            url = repoIconUrl,
            modifier = modifier,
            size = size,
            fallback = fallback,
        )
    }
}

private sealed interface AppIconState {
    data object Loading : AppIconState
    data object Missing : AppIconState
    data class Loaded(val bitmap: Bitmap) : AppIconState
}

@Composable
private fun rememberAppIcon(applicationInfo: ApplicationInfo?, size: Dp): AppIconState {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val state by produceState<AppIconState>(
        initialValue = when {
            applicationInfo == null -> AppIconState.Missing
            else -> AppIconCache.getCached(applicationInfo, sizePx)
                ?.let(AppIconState::Loaded) ?: AppIconState.Loading
        },
        applicationInfo,
        sizePx,
    ) {
        if (applicationInfo != null && value !is AppIconState.Loaded) {
            value = withContext(Dispatchers.IO) {
                runCatching { AppIconCache.loadIcon(context, applicationInfo, sizePx) }.getOrNull()
            }?.let(AppIconState::Loaded) ?: AppIconState.Missing
        }
    }
    return state
}

@Composable
private fun BundledIcon(@DrawableRes res: Int, modifier: Modifier, size: Dp) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp)),
    )
}
