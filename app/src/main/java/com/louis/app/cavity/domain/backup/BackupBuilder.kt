package com.louis.app.cavity.domain.backup

import android.content.Context
import androidx.annotation.StringRes
import com.louis.app.cavity.R
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.ui.account.Environment
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupBuilder(private val context: Context) {
    suspend fun checkHealth(source: List<HistoryEntry>, target: List<HistoryEntry>): HealthResult =
        withContext(Default) {
            val sourceNewest = source.maxByOrNull { it.date }?.date ?: 0
            val sourceOldest = source.minByOrNull { it.date }
            val targetNewest = target.maxByOrNull { it.date }?.date ?: 0
            val targetOldest = target.minByOrNull { it.date }

            val isOverwritingTarget = sourceNewest < targetNewest
            val couldBeAccountSwitch =
                target.isNotEmpty()
                        && (sourceOldest?.date ?: targetOldest?.date) != (targetOldest?.date ?: 0)
                        && sourceOldest?.id != targetOldest?.id

            if (couldBeAccountSwitch) {
                return@withContext HealthResult.MayBeAccountSwitch
            }

            if (isOverwritingTarget) {
                return@withContext HealthResult.WillOverwriteTarget
            }

            return@withContext HealthResult.Ok
        }

    suspend fun backup(accountRepository: AccountRepository, wineRepository: WineRepository) =
        withContext(IO) {
            with(accountRepository) {
                launch {
                    val wines = wineRepository.getAllWinesNotLive()
                    val bottles = wineRepository.getAllBottlesNotLive()
                    val friends = wineRepository.getAllFriendsNotLive()

                    // Get wines & bottles first, copy them to external dir
                    backupFilesToExternalDir(wines + bottles + friends)

                    listOf(
                        postCounties(wineRepository.getAllCountiesNotLive()),
                        postWines(wines),
                        postBottles(bottles),
                        postFriends(friends),
                        postGrapes(wineRepository.getAllGrapesNotLive()),
                        postReviews(wineRepository.getAllReviewsNotLive()),
                        postHistoryEntries(wineRepository.getAllEntriesNotPagedNotLive()),
                        postTastings(wineRepository.getAllTastingsNotLive()),
                        postTastingActions(wineRepository.getAllTastingActionsNotLive()),
                        postFReviews(wineRepository.getAllFReviewsNotLive()),
                        postQGrapes(wineRepository.getAllQGrapesNotLive()),
                        postTastingFriendsXRefs(wineRepository.getAllTastingXFriendsNotLive()),
                        postHistoryFriendsXRefs(wineRepository.getAllHistoryXFriendsNotLive())
                    ).forEach {
                        if (it !is ApiResponse.Success) {
                            throw UncompleteExportException()
                        }
                    }

                    postAccountLastUser(Environment.getDeviceName())
                }
            }
        }

    @StringRes
    fun getTextForHealthResult(backupHealthResult: HealthResult, isExport: Boolean): Int? {
        return when (backupHealthResult) {
            HealthResult.Ok -> null
            HealthResult.MayBeAccountSwitch -> R.string.auto_backup_account_switch
            HealthResult.WillOverwriteTarget ->
                if (isExport) R.string.healthcheck_export_warn else R.string.healthcheck_import_warn
        }
    }

    private fun backupFilesToExternalDir(fileAssocs: List<FileAssoc>) {
        fileAssocs
            .forEach {
                FileProcessor(context, it).run {
                    copyToExternalDir()
                }
            }
    }

    class UncompleteExportException : Exception()

    sealed class HealthResult {
        data object Ok : HealthResult()
        data object WillOverwriteTarget : HealthResult()
        data object MayBeAccountSwitch : HealthResult()
    }
}
