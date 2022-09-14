package com.louis.app.cavity.ui.account.fileimport

import android.net.Uri
import com.louis.app.cavity.db.WineRepository

class WineBinder(private val name: String) : FileBinder {
    override suspend fun bind(repository: WineRepository, uri: Uri) {
        val id = getBindedObjectId(name)
        val wine = repository.getWineByIdNotLive(id)
        repository.updateWine(wine.copy(imgPath = uri.toString()))
    }
}

class FriendBinder(private val name: String) : FileBinder {
    override suspend fun bind(repository: WineRepository, uri: Uri) {
        val id = getBindedObjectId(name)
        val friend = repository.getFriendByIdNotLive(id)
        repository.updateFriend(friend.copy(imgPath = uri.toString()))
    }
}

class BottleBinder(private val name: String) : FileBinder {
    override suspend fun bind(repository: WineRepository, uri: Uri) {
        val id = getBindedObjectId(name)
        val bottle = repository.getBottleByIdNotLive(id)
        repository.updateBottle(bottle.copy(pdfPath = uri.toString()))
    }
}
