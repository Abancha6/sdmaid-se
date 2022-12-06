package eu.darken.sdmse.common.forensics.csi

import android.os.storage.StorageManager
import eu.darken.sdmse.common.StorageEnvironment
import eu.darken.sdmse.common.areas.DataArea
import eu.darken.sdmse.common.areas.DataAreaManager
import eu.darken.sdmse.common.clutter.ClutterRepo
import eu.darken.sdmse.common.clutter.Marker
import eu.darken.sdmse.common.files.core.APath
import eu.darken.sdmse.common.files.core.GatewaySwitch
import eu.darken.sdmse.common.files.core.local.LocalPath
import eu.darken.sdmse.common.forensics.CSIProcessor
import eu.darken.sdmse.common.pkgs.Pkg
import eu.darken.sdmse.common.pkgs.PkgRepo
import eu.darken.sdmse.common.pkgs.pkgops.PkgOps
import eu.darken.sdmse.common.user.UserHandle2
import eu.darken.sdmse.common.user.UserManager2
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Test
import testhelpers.BaseTest

abstract class BaseCSITest : BaseTest() {

    @MockK lateinit var pkgRepo: PkgRepo
    @MockK lateinit var areaManager: DataAreaManager
    @MockK lateinit var clutterRepo: ClutterRepo
    @MockK lateinit var storageManager: StorageManager
    @MockK lateinit var gatewaySwitch: GatewaySwitch
    @MockK lateinit var userManager2: UserManager2
    @MockK lateinit var storageEnvironment: StorageEnvironment
    @MockK lateinit var pkgOps: PkgOps

    open fun setup() {
        if (!::pkgOps.isInitialized) {
            MockKAnnotations.init(this)
        }
        coEvery { clutterRepo.match(any(), any()) } returns emptySet()
        coEvery { pkgRepo.isInstalled(any()) } returns false
        coEvery { gatewaySwitch.listFiles(any()) } returns emptyList()
        coEvery { userManager2.currentUser } returns UserHandle2(0)
        every { storageEnvironment.dataDir } returns LocalPath.build("/data")
    }

    open fun teardown() {

    }

    suspend fun CSIProcessor.assertJurisdiction(type: DataArea.Type) {
        DataArea.Type.values().forEach {
            if (it == type) {
                hasJurisdiction(it) shouldBe true
            } else {
                hasJurisdiction(it) shouldBe false
            }
        }
    }

    open fun mockApp(pkgId: Pkg.Id, source: APath? = null) {
        coEvery { pkgRepo.isInstalled(pkgId) } returns true

//        val packageInfo: SDMPkgInfo = Mockito.mock(SDMPkgInfo::class.java)
//        Mockito.`when`(packageInfo.getPackageName()).thenReturn(pkgId)
//        val applicationInfo = Mockito.mock(ApplicationInfo::class.java)
//        applicationInfo.sourceDir = if (source != null) source.getPath() else null
//        Mockito.`when`(packageInfo.getApplicationInfo()).thenReturn(applicationInfo)
//        if (source != null) {
//            Mockito.`when`(packageInfo.getSourceDir())
//                .then(Answer<String> { invocation: InvocationOnMock? -> source.getPath() } as Answer<String>)
//        }
//        appMap.put(pkgId, packageInfo)
    }

    open fun mockMarker(pkgId: Pkg.Id, location: DataArea.Type, prefixFree: String) {

        val marker = mockk<Marker>().apply {
            every { areaType } returns location
            every { prefixFreeBasePath } returns prefixFree
            every { flags } returns emptySet()
        }

        val match = mockk<Marker.Match>().apply {
            every { packageNames } returns setOf(pkgId)
            every { flags } returns emptySet()
        }
        every { marker.match(location, prefixFree) } returns match

        clutterRepo.apply {
            coEvery { getMarkerForPkg(pkgId) } returns setOf(marker)
            coEvery { getMarkerForLocation(location) } returns setOf(marker)
            coEvery { match(location, prefixFree) } returns setOf(match)
        }
    }

    @Test abstract fun `test jurisdiction`()

    @Test abstract fun `determine area successfully`()

    @Test abstract fun `determine area UNsuccessfully`()

    @Test abstract fun `find default owner`()

    @Test abstract fun `find default owner indirectly`()

    @Test abstract fun `find owner via direct clutter hit`()

    @Test abstract fun `find no owner or fallback`()

}