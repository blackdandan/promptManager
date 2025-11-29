package com.promptmanager.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.promptmanager.app.core.database.entity.PromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY updatedAt DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun getPromptsByFolder(folderId: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE folderId IS NULL ORDER BY updatedAt DESC")
    fun getPromptsWithoutFolder(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    fun getPrompt(id: String): Flow<PromptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<PromptEntity>)

    @Update
    suspend fun updatePrompt(prompt: PromptEntity)

    @Delete
    suspend fun deletePrompt(prompt: PromptEntity)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePromptById(id: String)
    
    @Query("DELETE FROM prompts")
    suspend fun clearPrompts()
}
