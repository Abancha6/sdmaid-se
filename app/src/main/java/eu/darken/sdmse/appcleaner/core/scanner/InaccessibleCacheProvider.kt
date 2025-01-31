package eu.darken.sdmse.appcleaner.core.scanner

import dagger.Reusable
import eu.darken.sdmse.common.debug.logging.Logging
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.asLog
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.funnel.IPCFunnel
import eu.darken.sdmse.common.hasApiLevel
import eu.darken.sdmse.common.pkgs.features.Installed
import eu.darken.sdmse.common.storage.StorageId
import eu.darken.sdmse.common.storage.StorageStatsManager2
import javax.inject.Inject


@Reusable
class InaccessibleCacheProvider @Inject constructor(
    private val ipcFunnel: IPCFunnel,
    private val storageStatsManager: StorageStatsManager2,
) {

    suspend fun determineCache(pkg: Installed): InaccessibleCache? {
        val applicationInfo = pkg.applicationInfo

        if (applicationInfo == null) {
            log(TAG, WARN) { "Application info was NULL for ${pkg.id}" }
            return null
        }

        val storageStats = try {
            ipcFunnel.use {
                storageStatsManager.queryStatsForPkg(
                    StorageId(internalId = null, externalId = applicationInfo.storageUuid),
                    pkg,
                )
            }
        } catch (e: Exception) {
            log(TAG, Logging.Priority.ERROR) { "Failed to query app size for ${pkg.id}: ${e.asLog()}" }
            return null
        }

        return InaccessibleCache(
            pkg.installId,
            itemCount = 1,
            cacheBytes = storageStats.cacheBytes,
            externalCacheBytes = if (hasApiLevel(31)) {
                @Suppress("NewApi")
                storageStats.externalCacheBytes
            } else null,
        )
    }

    companion object {
        val TAG = logTag("AppCleaner", "Scanner", "Inaccessible")
    }
}