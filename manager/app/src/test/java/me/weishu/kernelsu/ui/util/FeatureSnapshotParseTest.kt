package me.weishu.kernelsu.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeatureSnapshotParseTest {

    private val marker = "@@@KSU-FEATURE-SNAPSHOT@@@"

    private fun jobOutput(vararg features: Triple<String, String?, String?>): List<String> =
        features.flatMap { (status, persist, current) ->
            buildList {
                add(status)
                add(marker)
                persist?.let { add("Value: $it") }
                add(marker)
                current?.let { add("Value: $it") }
                add(marker)
            }
        }

    @Test
    fun `parses status and values per feature`() {
        val snapshots = parseFeatureSnapshots(
            features = listOf("su_compat", "sulog"),
            out = jobOutput(
                Triple("supported", "1", "0"),
                Triple("forced", "0", "1"),
            ),
        )

        assertEquals("supported", snapshots.getValue("su_compat").status)
        assertEquals(1L, snapshots.getValue("su_compat").persistedValue)
        assertEquals(0L, snapshots.getValue("su_compat").currentValue)
        assertEquals("forced", snapshots.getValue("sulog").status)
        assertEquals(0L, snapshots.getValue("sulog").persistedValue)
        assertEquals(1L, snapshots.getValue("sulog").currentValue)
    }

    @Test
    fun `missing values stay null`() {
        val snapshots = parseFeatureSnapshots(
            features = listOf("kernel_umount"),
            out = jobOutput(Triple("unsupported", null, null)),
        )

        val snapshot = snapshots.getValue("kernel_umount")
        assertEquals("unsupported", snapshot.status)
        assertNull(snapshot.persistedValue)
        assertNull(snapshot.currentValue)
    }

    @Test
    fun `truncated output does not fabricate values`() {
        val snapshots = parseFeatureSnapshots(
            features = listOf("su_compat", "adb_root"),
            out = listOf("supported", marker, "Value: 1", marker),
        )

        assertEquals("supported", snapshots.getValue("su_compat").status)
        assertEquals(1L, snapshots.getValue("su_compat").persistedValue)
        assertNull(snapshots.getValue("su_compat").currentValue)
        assertEquals(null, snapshots["adb_root"])
    }

    @Test
    fun `value line is parsed leniently`() {
        assertEquals(2L, parseFeatureValue(listOf("Value: 2")))
        assertEquals(3L, parseFeatureValue(listOf("noise", "  Value:   3  ")))
        assertNull(parseFeatureValue(listOf("noise")))
        assertNull(parseFeatureValue(listOf("Value: none")))
    }
}
