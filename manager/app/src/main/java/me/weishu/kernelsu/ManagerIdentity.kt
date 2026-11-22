package me.weishu.kernelsu

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

/**
 * Manager-side mirror of the kernel's manager authentication.
 *
 * The kernel only treats an APK as the manager when both the package name and the
 * signing certificate match the values compiled into the module. This object checks
 * the same two properties from inside the running app as defence in depth, so a
 * mis-signed or renamed build cannot silently use root features.
 */
object ManagerIdentity {

    data class Verification(
        val packageMatches: Boolean,
        val certificateMatches: Boolean,
        val kernelAccepts: Boolean,
    ) {
        val isValid: Boolean
            get() = packageMatches && certificateMatches && kernelAccepts
    }

    fun normalizeFingerprint(value: String?): String =
        value.orEmpty().replace(":", "").trim().lowercase()

    fun certificateSha256(certificate: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(certificate)
            .joinToString("") { byte -> "%02x".format(byte) }

    /**
     * Pure identity check used by unit tests and by [verify].
     *
     * An empty expected fingerprint means the build did not pin a certificate
     * (debug builds); in that case only the package name is checked.
     */
    fun matches(
        actualPackage: String,
        expectedPackage: String,
        actualCertificateSha256: String?,
        expectedCertificateSha256: String?,
    ): Boolean {
        if (actualPackage != expectedPackage) return false
        val expected = normalizeFingerprint(expectedCertificateSha256)
        if (expected.isEmpty()) return true
        return normalizeFingerprint(actualCertificateSha256) == expected
    }

    fun signingCertificateSha256(context: Context): String? = runCatching {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )
        val signer = packageInfo.signingInfo?.apkContentsSigners?.firstOrNull() ?: return null
        certificateSha256(signer.toByteArray())
    }.getOrNull()

    fun verify(context: Context): Verification {
        val expectedPackage = BuildConfig.MKSU_MANAGER_PACKAGE
        val expectedCertificate = BuildConfig.MKSU_CERT_SHA256
        val actualCertificate = signingCertificateSha256(context)
        val certificateMatches = if (normalizeFingerprint(expectedCertificate).isEmpty()) {
            true
        } else {
            normalizeFingerprint(actualCertificate) == normalizeFingerprint(expectedCertificate)
        }
        return Verification(
            packageMatches = context.packageName == expectedPackage,
            certificateMatches = certificateMatches,
            kernelAccepts = Natives.isManager,
        )
    }
}
