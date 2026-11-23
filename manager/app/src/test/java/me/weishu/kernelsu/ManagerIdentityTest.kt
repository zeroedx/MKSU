package me.weishu.kernelsu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagerIdentityTest {

    private val packageName = "com.zeroedx.mksu"
    private val certificate = "a".repeat(64)

    @Test
    fun acceptsMatchingPackageAndCertificate() {
        assertTrue(
            ManagerIdentity.matches(
                actualPackage = packageName,
                expectedPackage = packageName,
                actualCertificateSha256 = certificate,
                expectedCertificateSha256 = certificate,
            )
        )
    }

    @Test
    fun normalizesColonSeparatedFingerprints() {
        val colonSeparated = certificate.chunked(2).joinToString(":")
        assertTrue(
            ManagerIdentity.matches(
                actualPackage = packageName,
                expectedPackage = packageName,
                actualCertificateSha256 = colonSeparated.uppercase(),
                expectedCertificateSha256 = certificate,
            )
        )
    }

    @Test
    fun rejectsWrongPackageEvenWithCorrectCertificate() {
        assertFalse(
            ManagerIdentity.matches(
                actualPackage = "me.weishu.kernelsu",
                expectedPackage = packageName,
                actualCertificateSha256 = certificate,
                expectedCertificateSha256 = certificate,
            )
        )
    }

    @Test
    fun rejectsWrongCertificateEvenWithCorrectPackage() {
        assertFalse(
            ManagerIdentity.matches(
                actualPackage = packageName,
                expectedPackage = packageName,
                actualCertificateSha256 = "b".repeat(64),
                expectedCertificateSha256 = certificate,
            )
        )
    }

    @Test
    fun rejectsMissingCertificateWhenOneIsExpected() {
        assertFalse(
            ManagerIdentity.matches(
                actualPackage = packageName,
                expectedPackage = packageName,
                actualCertificateSha256 = null,
                expectedCertificateSha256 = certificate,
            )
        )
    }

    @Test
    fun debugBuildsWithoutPinnedCertificateOnlyCheckPackage() {
        assertTrue(
            ManagerIdentity.matches(
                actualPackage = packageName,
                expectedPackage = packageName,
                actualCertificateSha256 = null,
                expectedCertificateSha256 = "",
            )
        )
        assertFalse(
            ManagerIdentity.matches(
                actualPackage = "wrong.package",
                expectedPackage = packageName,
                actualCertificateSha256 = null,
                expectedCertificateSha256 = "",
            )
        )
    }

    @Test
    fun certificateDigestIsStable() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            ManagerIdentity.certificateSha256(ByteArray(0)),
        )
    }

    @Test
    fun certificateDigestTreatsBytesAsUnsigned() {
        assertEquals(
            "f192456d81c614b7586a985db70856924a5e0bbfba3838ebfa3198ceab2e70aa",
            ManagerIdentity.certificateSha256(
                byteArrayOf(0x80.toByte(), 0x00, 0xff.toByte()),
            ),
        )
    }
}
