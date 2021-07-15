/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.listener

import com.ynsuper.imagepicker.model.ImageModel

interface OnImageLoaderListener {
    fun onImageLoaded(imageModels: ArrayList<ImageModel>)
    fun onFailed(throwable: Throwable)
}