package me.weishu.kernelsu.ui.component

import androidx.annotation.DrawableRes
import me.weishu.kernelsu.R

/**
 * Icon sources for modules the manager ships support for. Nothing here is inferred from a module
 * id: a companion package is only listed when the module's own project declares it.
 *
 * Companion packages, read from each project's build files:
 * - zygisk_lsposed  -> org.lsposed.manager, LSPosed/LSPosed app/build.gradle.kts
 * - zygisk_fusehide -> io.github.xiaotong6666.fusehide, XiaoTong6666/FuseHide app/build.gradle.kts
 * - hma_oss_zygisk  -> org.frknkrc44.hma_oss, frknkrc44/HMA-OSS build.gradle.kts
 * - wekit_zygisk    -> dev.ujhhgtg.wekit, Ujhhgtg/WeKit gradle/libs.versions.toml
 *
 * Bundled icons are the projects' own launcher icons, copied unmodified apart from downscaling
 * to 192 px. They are redistributed under the source project's license; the manager as a whole is
 * GPL-3.0, which GPL-3.0 section 13 allows to be combined with the AGPL-3.0 one here.
 * - ic_module_zygisk_lsposed.png  LSPosed/LSPosed app/src/main/res/drawable/ic_launcher*.xml
 *   (GPL-3.0, commit df74d83, rasterized: #F48FB1 background, #FFFFFF glyph)
 * - ic_module_zygisk_fusehide.png XiaoTong6666/FuseHide app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
 *   (Apache-2.0, commit 31d6fa2)
 * - ic_module_hma_oss_zygisk.png  frknkrc44/HMA-OSS fastlane/metadata/android/en-US/images/icon.png
 *   (AGPL-3.0, commit 9094d7d)
 * - ic_module_wekit_zygisk.png    Ujhhgtg/WeKit icons/wekit.png (GPL-3.0, commit b51532b)
 */
internal object ModuleIconCatalog {

    private val companionPackages = mapOf(
        "zygisk_lsposed" to "org.lsposed.manager",
        "zygisk_fusehide" to "io.github.xiaotong6666.fusehide",
        "hma_oss_zygisk" to "org.frknkrc44.hma_oss",
        "wekit_zygisk" to "dev.ujhhgtg.wekit",
    )

    private val bundledIcons = mapOf(
        "zygisk_lsposed" to R.drawable.ic_module_zygisk_lsposed,
        "zygisk_fusehide" to R.drawable.ic_module_zygisk_fusehide,
        "hma_oss_zygisk" to R.drawable.ic_module_hma_oss_zygisk,
        "wekit_zygisk" to R.drawable.ic_module_wekit_zygisk,
    )

    /** Verified companion app package of [moduleId], or null when the project has none. */
    fun companionPackage(moduleId: String): String? = companionPackages[moduleId]

    /** Bundled project icon of [moduleId], used when the registry has none for it. */
    @DrawableRes
    fun bundledIcon(moduleId: String): Int? = bundledIcons[moduleId]
}
