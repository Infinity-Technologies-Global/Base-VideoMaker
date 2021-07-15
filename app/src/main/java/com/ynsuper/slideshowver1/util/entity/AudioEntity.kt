package com.ynsuper.slideshowver1.util.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var path: String
)