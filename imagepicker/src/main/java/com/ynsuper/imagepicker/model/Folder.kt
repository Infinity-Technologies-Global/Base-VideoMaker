/*
 * Copyright (c) 2020 Nguyen Hoang Lam.
 * All rights reserved.
 */

package com.ynsuper.imagepicker.model

import java.util.*

data class Folder(var bucketId: Long, var name: String, var imageModels: ArrayList<ImageModel> = arrayListOf())
