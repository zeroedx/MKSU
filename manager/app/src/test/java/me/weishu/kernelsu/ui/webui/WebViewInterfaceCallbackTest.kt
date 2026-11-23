package me.weishu.kernelsu.ui.webui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WebViewInterfaceCallbackTest {

    @Test
    fun `callbacks are posted to the main thread instead of running inline`() {
        val state = WebUIState()
        val queued = mutableListOf<Runnable>()
        val webUi = WebViewInterface(state) { queued += it }

        webUi.toast("installed")

        assertEquals(1, queued.size)
    }

    @Test
    fun `a late callback is dropped once the page is gone`() {
        val state = WebUIState()
        val queued = mutableListOf<Runnable>()
        val webUi = WebViewInterface(state) { queued += it }

        webUi.toast("installed")
        webUi.fullScreen(true)

        assertEquals(2, queued.size)
        assertFalse(state.isInsetsEnabled)

        // The WebView is null after dispose(), so these runnables stand in for a shell command or
        // a JS callback that finishes after the user left the page.
        queued.forEach(Runnable::run)

        assertFalse("a closed page must not change the inset mode", state.isInsetsEnabled)
    }
}
