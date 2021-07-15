/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.ui.imagepicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ynsuper.imagepicker.listener.OnImageLoaderListener
import com.ynsuper.imagepicker.model.CallbackStatus
import com.ynsuper.imagepicker.model.Config
import com.ynsuper.imagepicker.model.ImageModel
import com.ynsuper.imagepicker.model.Result

class ImagePickerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val imageFileLoader: ImageFileLoader = ImageFileLoader(context)
    private lateinit var config: Config

    lateinit var selectedImages: MutableLiveData<ArrayList<ImageModel>>
    val result = MutableLiveData(Result(CallbackStatus.IDLE, arrayListOf()))

    fun setConfig(config: Config) {
        this.config = config
        selectedImages = MutableLiveData(config.selectedImageModels)
    }

    fun getConfig() = config

    fun fetchImages() {
        result.postValue(Result(CallbackStatus.FETCHING, arrayListOf()))
        imageFileLoader.abortLoadImages()
        imageFileLoader.loadDeviceImages(object : OnImageLoaderListener {
            override fun onImageLoaded(imageModels: ArrayList<ImageModel>) {
                result.postValue(Result(CallbackStatus.SUCCESS, imageModels))
            }

            override fun onFailed(throwable: Throwable) {
                result.postValue(Result(CallbackStatus.SUCCESS, arrayListOf()))
            }

        })
    }
}