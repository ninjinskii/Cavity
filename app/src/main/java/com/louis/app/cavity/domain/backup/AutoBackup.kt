package com.louis.app.cavity.domain.backup

import android.content.Context
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

// On affiche pas le bon texte quand on est en risque d'overwrite
// Gérer le chagement de statut de la backup lors du décocahge du switch
// Supprimer le last backup state de prefs repository ?
class AutoBackup<T>(
    private val wineRepository: WineRepository,
    private val accountRepository: AccountRepository,
    private val context: Context,
    private val listener: BackupFinishedListener<T>,
) {

    companion object {
        const val SUCCESS = 0
        const val FAILURE = 1
    }

    suspend fun tryBackup(): T = withContext(IO) {
        val localHistoryEntries = wineRepository.getAllEntriesNotPagedNotLive()
        val distantHistoryEntries: List<HistoryEntry> =
            accountRepository.getHistoryEntries().let { response ->
                when (response) {
                    is ApiResponse.Success -> response.value
                    is ApiResponse.UnauthorizedError -> {
                        return@withContext listener.onUnauthorized()
                    }

                    else -> {
                        return@withContext listener.onFailure()
                    }
                }
            }

        checkHealth(localHistoryEntries, distantHistoryEntries)?.let {
            return@withContext it
        }

        return@withContext try {
            uploadDatabase()
            listener.onSuccess()
        } catch (e: UncompleteExportException) {
            listener.onFailure(canRetry = true, exception = e)
        } catch (e: Exception) {
            listener.onFailure(exception = e)
        }
    }

    private suspend fun checkHealth(
        local: List<HistoryEntry>,
        distant: List<HistoryEntry>
    ): T? = withContext(Default) {
        val localNewest = local.maxByOrNull { it.date }?.date ?: 0
        val localOldest = local.minByOrNull { it.date }
        val distantNewest = distant.maxByOrNull { it.date }?.date ?: 0
        val distantOldest = distant.minByOrNull { it.date }

        val isOverwritingDistantBackup = localNewest < distantNewest
        val couldBeAccountSwitch =
            (localOldest?.date ?: 0) != (distantOldest?.date ?: 0)
                    && localOldest?.id !== distantOldest?.id

        if (couldBeAccountSwitch) {
            return@withContext listener.onPreventAccountSwitch()
        }

        if (isOverwritingDistantBackup) {
            return@withContext listener.onPreventOverwriting()
        }

        return@withContext null
    }

    private suspend fun uploadDatabase() = withContext(IO) {
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

    private fun backupFilesToExternalDir(fileAssocs: List<FileAssoc>) {
        fileAssocs
            .forEach {
                FileProcessor(context, it).run {
                    copyToExternalDir()
                }
            }
    }

}

class UncompleteExportException : Exception()

interface BackupFinishedListener<T> {
    fun onSuccess(): T
    fun onFailure(canRetry: Boolean = false, exception: Exception? = null): T
    fun onUnauthorized(): T
    fun onPreventOverwriting(): T
    fun onPreventAccountSwitch(): T
}
