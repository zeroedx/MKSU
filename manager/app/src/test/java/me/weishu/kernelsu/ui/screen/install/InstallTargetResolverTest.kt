package me.weishu.kernelsu.ui.screen.install

import me.weishu.kernelsu.ui.util.BootInfoResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallTargetResolverTest {

    private fun probe(vararg lines: String, code: Int = 0, stderr: String = "") =
        BootInfoResult(lines = lines.toList(), code = code, stderr = stderr)

    @Test
    fun directInstallBlocksWhenNoPartitionReported() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = false,
            partitions = emptyList(),
            selectedIndex = 0,
            partitionResult = probe(),
            slotResult = null,
        )

        assertEquals(TargetProblem.NoPartitions(""), target.problem)
        assertFalse(target.isResolved)
        assertNull(target.partition)
    }

    @Test
    fun directInstallResolvesSelectedPartition() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = false,
            partitions = listOf("boot", "init_boot"),
            selectedIndex = 1,
            partitionResult = probe("boot", "init_boot"),
            slotResult = null,
        )

        assertEquals("init_boot", target.partition)
        assertNull(target.problem)
        assertTrue(target.isResolved)
    }

    @Test
    fun probeFailureCarriesExitCodeAndStderr() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = false,
            partitions = emptyList(),
            selectedIndex = 0,
            partitionResult = probe(code = 127, stderr = "libksud.so: not found"),
            slotResult = null,
        )

        assertEquals(
            TargetProblem.PartitionCommandFailed(127, "libksud.so: not found"),
            target.problem,
        )
    }

    @Test
    fun probingUntilPartitionResultArrives() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = false,
            partitions = emptyList(),
            selectedIndex = 0,
            partitionResult = null,
            slotResult = null,
        )

        assertTrue(target.probing)
        assertFalse(target.isResolved)
        assertNull(target.problem)
    }

    @Test
    fun inactiveSlotInstallRequiresResolvedSlot() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = true,
            partitions = listOf("boot"),
            selectedIndex = 0,
            partitionResult = probe("boot"),
            slotResult = probe(""),
        )

        assertEquals(TargetProblem.NoSlotResolved, target.problem)
        assertFalse(target.isResolved)
    }

    @Test
    fun inactiveSlotInstallResolvesWithSlotSuffix() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = true,
            partitions = listOf("boot"),
            selectedIndex = 0,
            partitionResult = probe("boot"),
            slotResult = probe("_a"),
        )

        assertEquals("_a", target.slotSuffix)
        assertEquals("boot", target.partition)
        assertNull(target.problem)
        assertTrue(target.isResolved)
    }

    @Test
    fun inactiveSlotInstallReportsSlotProbeFailure() {
        val target = resolveInstallTarget(
            requirePartition = true,
            requireSlot = true,
            partitions = listOf("boot"),
            selectedIndex = 0,
            partitionResult = probe("boot"),
            slotResult = probe(code = 1, stderr = "permission denied"),
        )

        assertEquals(TargetProblem.SlotCommandFailed(1, "permission denied"), target.problem)
    }

    @Test
    fun fileSelectionDoesNotRequirePartition() {
        val target = resolveInstallTarget(
            requirePartition = false,
            requireSlot = false,
            partitions = emptyList(),
            selectedIndex = 0,
            partitionResult = probe(),
            slotResult = null,
        )

        assertNull(target.problem)
        assertFalse(target.probing)
        assertNull(target.partition)
    }
}
