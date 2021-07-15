package com.ynsuper.slideshowver1.dao

import androidx.room.*
import com.ynsuper.slideshowver1.util.entity.AudioEntity


@Dao
interface AudioDao {

    @Query("Select * From AudioEntity Limit 1")
    fun first(): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(audioEntity: AudioEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(audioEntity: AudioEntity)

    @Delete
    fun delete(audioEntity: AudioEntity)

    @Query("Delete From AudioEntity")
    fun deleteAll()

    @Transaction
    fun upsert(vararg audio: AudioEntity) {
        for (audioEntity in audio) {
            save(audioEntity)
            update(audioEntity)
        }
    }
}