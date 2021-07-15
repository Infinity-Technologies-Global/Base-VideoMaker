/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.helper

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.ynsuper.imagepicker.model.Folder
import com.ynsuper.imagepicker.model.ImageModel
import java.io.File

object ImageHelper {

    private fun getNameFromFilePath(path: String): String {
        return if (path.contains(File.separator)) {
            path.substring(path.lastIndexOf(File.separator) + 1)
        } else path
    }

    fun grantAppPermission(context: Context, intent: Intent, fileUri: Uri) {
        val resolvedIntentActivities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun revokeAppPermission(context: Context, fileUri: Uri) {
        context.revokeUriPermission(fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun singleListFromPath(id: Long, path: String): ArrayList<ImageModel> {
        val images = arrayListOf<ImageModel>()
        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        images.add(ImageModel(id, getNameFromFilePath(path), uri, path))
        return images
    }

    fun singleListFromImage(imageModel: ImageModel): ArrayList<ImageModel> {
        val images = arrayListOf<ImageModel>()
        images.add(imageModel)
        return images
    }

    fun folderListFromImages(imageModels: List<ImageModel>): List<Folder> {
        val folderMap: MutableMap<Long, Folder> = LinkedHashMap()
        for (image in imageModels) {
            val bucketId = image.bucketId
            val bucketName = image.bucketName
            var folder = folderMap[bucketId]
            if (folder == null) {
                folder = Folder(bucketId, bucketName)
                folderMap[bucketId] = folder
            }
            folder.imageModels.add(image)
        }
        return ArrayList(folderMap.values)
    }

    fun filterImages(imageModels: ArrayList<ImageModel>, bukketId: Long?): ArrayList<ImageModel> {
        if (bukketId == null) return imageModels

        val filteredImages = arrayListOf<ImageModel>()
        for (image in imageModels) {
            if (image.bucketId == bukketId) {
                filteredImages.add(image)
            }
        }
        return filteredImages
    }

    fun findImageIndex(imageModel: ImageModel, imageModels: ArrayList<ImageModel>): Int {
        for (i in imageModels.indices) {
            if (imageModels[i].path == imageModel.path) {
                return i
            }
        }
        return -1
    }

    fun findImageIndexes(subImageModels: ArrayList<ImageModel>, imageModels: ArrayList<ImageModel>): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        for (image in subImageModels) {
            for (i in imageModels.indices) {
                if (imageModels[i].path == image.path) {
                    indexes.add(i)
                    break
                }
            }
        }
        return indexes
    }


    fun isGifFormat(imageModel: ImageModel): Boolean {
        val extension = imageModel.path.substring(imageModel.path.lastIndexOf(".") + 1, imageModel.path.length)
        return extension.equals("gif", ignoreCase = true)
    }
}