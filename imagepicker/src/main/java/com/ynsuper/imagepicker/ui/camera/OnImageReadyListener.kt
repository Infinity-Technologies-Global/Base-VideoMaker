/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.ui.camera

import com.ynsuper.imagepicker.model.ImageModel

interface OnImageReadyListener {
    fun onImageReady(imageModels: ArrayList<ImageModel>)
    fun onImageNotReady()
}