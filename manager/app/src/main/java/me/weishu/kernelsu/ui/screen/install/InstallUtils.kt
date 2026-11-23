package me.weishu.kernelsu.ui.screen.install

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.util.BootInfoResult

@Parcelize
internal sealed class InstallMethod : Parcelable {
    data class SelectFile(
        val uri: Uri? = null,
        @get:StringRes override val label: Int = R.string.select_file,
        override val summary: String?
    ) : InstallMethod()

    data class DownloadFile(
        val url: String? = null,
        val partition: String? = null,
        @get:StringRes override val label: Int = R.string.download_file,
        override val summary: String?
    ) : InstallMethod()

    data object DirectInstall : InstallMethod() {
        override val label: Int
            get() = R.string.direct_install
    }

    data object DirectInstallToInactiveSlot : InstallMethod() {
        override val label: Int
            get() = R.string.install_inactive_slot
    }

    abstract val label: Int

    @IgnoredOnParcel
    open val summary: String? = null
}

fun isKoFile(context: Context, uri: Uri): Boolean {
    val seg = uri.lastPathSegment ?: ""
    if (seg.endsWith(".ko", ignoreCase = true)) return true

    return try {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx != -1 && cursor.moveToFirst()) {
                val name = cursor.getString(idx)
                name?.endsWith(".ko", ignoreCase = true) == true
            } else {
                false
            }
        } ?: false
    } catch (_: Throwable) {
        false
    }
}

internal sealed class TargetProblem {
    data class PartitionCommandFailed(val exitCode: Int, val stderr: String) : TargetProblem()
    data class NoPartitions(val slotSuffix: String) : TargetProblem()
    data object NoPartitionSelected : TargetProblem()
    data class SlotCommandFailed(val exitCode: Int, val stderr: String) : TargetProblem()
    data object NoSlotResolved : TargetProblem()
}

internal data class InstallTarget(
    val partition: String?,
    val slotSuffix: String?,
    val problem: TargetProblem?,
    val probing: Boolean = false,
) {
    val isResolved: Boolean
        get() = !probing && problem == null && partition != null
}

internal fun resolveInstallTarget(
    requirePartition: Boolean,
    requireSlot: Boolean,
    partitions: List<String>,
    selectedIndex: Int,
    partitionResult: BootInfoResult?,
    slotResult: BootInfoResult?,
): InstallTarget {
    val suffix = slotResult?.value?.takeIf { it.isNotBlank() }
    val partition = partitions.getOrNull(selectedIndex)?.takeIf { it.isNotBlank() }
    if (!requirePartition) return InstallTarget(partition, suffix, null)

    val parts = partitionResult
        ?: return InstallTarget(partition, suffix, null, probing = true)
    if (requireSlot && slotResult == null) {
        return InstallTarget(partition, suffix, null, probing = true)
    }

    val partitionProblem = when {
        parts.code != 0 -> TargetProblem.PartitionCommandFailed(parts.code, parts.stderr)
        partitions.isEmpty() -> TargetProblem.NoPartitions(suffix.orEmpty())
        partition == null -> TargetProblem.NoPartitionSelected
        else -> null
    }
    val slotProblem = if (!requireSlot) null else {
        val slot = slotResult ?: return InstallTarget(partition, suffix, null, probing = true)
        when {
            slot.code != 0 -> TargetProblem.SlotCommandFailed(slot.code, slot.stderr)
            suffix == null -> TargetProblem.NoSlotResolved
            else -> null
        }
    }
    return InstallTarget(partition, suffix, partitionProblem ?: slotProblem)
}

