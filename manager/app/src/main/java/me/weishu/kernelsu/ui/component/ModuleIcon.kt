package me.weishu.kernelsu.ui.component

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.screen.modulerepo.RepoModuleIcon
import me.weishu.kernelsu.ui.util.AppIconCache

/**
 * Icon in front of an installed module's name, from the first source that resolves: the icon of the
 * module's verified companion app, the registry icon of the module, then the project icon the
 * manager bundles for it. Renders nothing when all three are missing, so the card keeps its layout.
 *
 * The companion app is looked up off the main thread and resolved again whenever the manager resumes,
 * so an app that was updated or removed meanwhile is picked up. While the first lookup runs the slot
 * stays empty, so a lower-priority icon never flashes before the one that wins.
 */
@Composable
fun ModuleIcon(
    module: Module,
    repoIconUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    val bundledIcon = ModuleIconCatalog.bundledIcon(module.id)
    val fallback: (@Composable () -> Unit)? = bundledIcon?.let { res ->
        { BundledIcon(res, modifier, size) }
    }
    val companionPackage = remember(module.id) { ModuleIconCatalog.companionPackage(module.id) }

    when (val companion = rememberCompanionApp(companionPackage)) {
        CompanionApp.Pending -> Unit
        CompanionApp.Absent -> RepoModuleIcon(
            url = repoIconUrl,
            modifier = modifier,
            size = size,
            fallback = fallback,
        )
        is CompanionApp.Found -> when (val state = rememberAppIcon(companion.info, size)) {
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
}

private sealed interface CompanionApp {
    data object Pending : CompanionApp
    data object Absent : CompanionApp
    data class Found(val info: ApplicationInfo) : CompanionApp
}

@Composable
private fun rememberCompanionApp(packageName: String?): CompanionApp {
    if (packageName == null) return CompanionApp.Absent
    val context = LocalContext.current
    val state = remember(packageName) { mutableStateOf<CompanionApp>(CompanionApp.Pending) }
    val resumeCount = rememberResumeCount()

    // Nothing is kept across resumes: a companion app can only be installed, updated or removed while
    // the manager is in the background, so a resume is where the previous answer may be stale. The
    // last icon stays on screen until the fresh answer arrives.
    LaunchedEffect(packageName, resumeCount) {
        state.value = withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
        }?.let(CompanionApp::Found) ?: CompanionApp.Absent
    }
    return state.value
}

/** Increments every time the host lifecycle reaches resumed, i.e. when the user comes back. */
@Composable
private fun rememberResumeCount(): Int {
    val lifecycleOwner = LocalLifecycleOwner.current
    var count by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) count++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return count
}

private sealed interface AppIconState {
    data object Loading : AppIconState
    data object Missing : AppIconState
    data class Loaded(val bitmap: Bitmap) : AppIconState
}

@Composable
private fun rememberAppIcon(applicationInfo: ApplicationInfo, size: Dp): AppIconState {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val state by produceState<AppIconState>(
        initialValue = AppIconCache.getCached(applicationInfo, sizePx)
            ?.let(AppIconState::Loaded) ?: AppIconState.Loading,
        applicationInfo,
        sizePx,
    ) {
        if (value !is AppIconState.Loaded) {
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
