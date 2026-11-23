package me.weishu.kernelsu.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModuleIconCatalogTest {

    @Test
    fun `supported modules keep their verified companion packages`() {
        assertEquals("org.lsposed.manager", ModuleIconCatalog.companionPackage("zygisk_lsposed"))
        assertEquals("io.github.xiaotong6666.fusehide", ModuleIconCatalog.companionPackage("zygisk_fusehide"))
        assertEquals("org.frknkrc44.hma_oss", ModuleIconCatalog.companionPackage("hma_oss_zygisk"))
        assertEquals("dev.ujhhgtg.wekit", ModuleIconCatalog.companionPackage("wekit_zygisk"))
    }

    @Test
    fun `supported modules bundle their project icon`() {
        for (id in listOf("zygisk_lsposed", "zygisk_fusehide", "hma_oss_zygisk", "wekit_zygisk")) {
            assertNotEquals("no bundled icon for $id", 0, ModuleIconCatalog.bundledIcon(id) ?: 0)
        }
    }

    @Test
    fun `an id that looks like a package is not treated as one`() {
        assertNull(ModuleIconCatalog.companionPackage("com.example.somemodule"))
        assertNull(ModuleIconCatalog.companionPackage("zygisk_lsposed_extra"))
        assertNull(ModuleIconCatalog.bundledIcon("com.example.somemodule"))
    }
}
