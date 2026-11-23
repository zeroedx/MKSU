package me.weishu.kernelsu.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleAvailabilityTest {

    private fun module(
        enabled: Boolean = true,
        update: Boolean = false,
        remove: Boolean = false,
        hasWebUi: Boolean = true,
        hasActionScript: Boolean = true,
        updateJson: String = "https://example.com/update.json",
    ) = Module(
        id = "demo",
        name = "Demo",
        author = "tester",
        version = "1.0",
        versionCode = 1,
        description = "",
        enabled = enabled,
        update = update,
        remove = remove,
        updateJson = updateJson,
        hasWebUi = hasWebUi,
        hasActionScript = hasActionScript,
        metamodule = false,
        actionIconPath = null,
        webUiIconPath = null,
    )

    @Test
    fun `a pending update keeps the webui and the action but skips the update check`() {
        val pending = module(update = true)

        assertTrue(pending.canOpenWebUi)
        assertTrue(pending.canRunAction)
        assertFalse(pending.canCheckForUpdate)
    }

    @Test
    fun `an up to date module keeps the update check`() {
        val current = module()

        assertTrue(current.canCheckForUpdate)
    }

    @Test
    fun `a module waiting for removal stays unavailable`() {
        val removed = module(remove = true)

        assertFalse(removed.canOpenWebUi)
        assertFalse(removed.canRunAction)
        assertFalse(removed.canCheckForUpdate)
    }

    @Test
    fun `a disabled module stays unavailable`() {
        val disabled = module(enabled = false)

        assertFalse(disabled.canOpenWebUi)
        assertFalse(disabled.canRunAction)
        assertFalse(disabled.canCheckForUpdate)
    }

    @Test
    fun `a module without entries stays unavailable`() {
        val bare = module(hasWebUi = false, hasActionScript = false, updateJson = "")

        assertFalse(bare.canOpenWebUi)
        assertFalse(bare.canRunAction)
        assertFalse(bare.canCheckForUpdate)
    }
}
