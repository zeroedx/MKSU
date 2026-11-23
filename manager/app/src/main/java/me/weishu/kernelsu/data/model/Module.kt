package me.weishu.kernelsu.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Module(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val enabled: Boolean,
    val update: Boolean,
    val remove: Boolean,
    val updateJson: String,
    val hasWebUi: Boolean,
    val hasActionScript: Boolean,
    val metamodule: Boolean,
    val actionIconPath: String?,
    val webUiIconPath: String?,
    // preinit state for the next boot: "", "ok", "no_space", "error" or "bootloop"
    val preinit: String = "",
) {
    /**
     * [update] only means the module was installed and waits for a reboot. The WebUI and the
     * action script run against the module as it is now, so a pending update must not hide them.
     * The update check keeps its original rule and skips such modules until the reboot.
     *
     * Ported from KOWX712/KernelSU cb11859 (GPL-3.0).
     */
    val canOpenWebUi: Boolean get() = hasWebUi && enabled && !remove
    val canRunAction: Boolean get() = hasActionScript && enabled && !remove
    val canCheckForUpdate: Boolean get() = updateJson.isNotEmpty() && enabled && !remove && !update
}
