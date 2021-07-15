/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.ui.camera

import android.content.Context
import android.content.Intent
import com.ynsuper.imagepicker.model.Config

interface CameraModule {
    fun getCameraIntent(context: Context, config: Config): Intent?
    fun getImage(context: Context, isRequireId: Boolean, imageReadyListener: OnImageReadyListener?)
}