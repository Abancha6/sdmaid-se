package eu.darken.sdmse.common.forensics.csi.sys

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.areas.DataArea
import eu.darken.sdmse.common.areas.DataAreaManager
import eu.darken.sdmse.common.areas.currentAreas
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.core.APath
import eu.darken.sdmse.common.forensics.AreaInfo
import eu.darken.sdmse.common.forensics.CSIProcessor
import eu.darken.sdmse.common.forensics.csi.LocalCSIProcessor
import java.io.File
import javax.inject.Inject

@Reusable
class CSICachePartition @Inject constructor(
    private val areaManager: DataAreaManager,
) : LocalCSIProcessor {

    override suspend fun hasJurisdiction(type: DataArea.Type): Boolean = type == DataArea.Type.DOWNLOAD_CACHE

    override suspend fun identifyArea(target: APath): AreaInfo? =
        areaManager.currentAreas()
            .filter { it.type == DataArea.Type.DOWNLOAD_CACHE }
            .mapNotNull { area ->
                val base = "${area.path.path}${File.separator}"
                if (!target.path.startsWith(base)) return@mapNotNull null

                AreaInfo(
                    dataArea = area,
                    file = target,
                    prefix = base,
                    isBlackListLocation = false
                )
            }
            .singleOrNull()

    override suspend fun findOwners(areaInfo: AreaInfo): CSIProcessor.Result = CSIProcessor.Result()

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: CSICachePartition): CSIProcessor
    }

    companion object {
        val TAG: String = logTag("CSI", "System", "CachePartition")
    }
}