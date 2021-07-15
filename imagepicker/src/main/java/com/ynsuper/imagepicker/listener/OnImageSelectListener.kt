/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.listener

import com.ynsuper.imagepicker.model.ImageModel

interface OnImageSelectListener {
    fun onSelectedImagesChanged(selectedImageModels: ArrayList<ImageModel>)
    fun onSingleModeImageSelected(imageModel: ImageModel)
}