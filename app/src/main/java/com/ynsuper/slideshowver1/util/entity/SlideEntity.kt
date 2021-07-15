package com.ynsuper.slideshowver1.util.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SlideEntity(
    var path: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
)