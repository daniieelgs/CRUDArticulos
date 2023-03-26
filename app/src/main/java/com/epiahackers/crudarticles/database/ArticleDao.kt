package com.epiahackers.crudarticles.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles")
    fun getArticles(): MutableList<Article>

    @Query("SELECT * FROM articles")
    suspend fun getArticlesAsync(): MutableList<Article>

    @Query("SELECT * FROM articles ORDER BY CODIARTICLE ASC")
    fun getOrderArticles(): MutableList<Article>

    @Query("SELECT * FROM articles WHERE CODIARTICLE = :code")
    fun getByCode(code: String): Article

    @Query("SELECT * FROM articles WHERE CODIARTICLE = :code")
    suspend fun getByCodeAsync(code: String): Article

    @Query("SELECT * FROM articles WHERE ESTOC <= 0")
    suspend fun filterByStock(): MutableList<Article>

    @Query("SELECT * FROM articles WHERE DESCRIPCIO LIKE '%' || :description || '%'")
    suspend fun searchByDescription(description: String): MutableList<Article>

    @Query("SELECT * FROM articles WHERE CODIARTICLE LIKE '%' || :code || '%'")
    suspend fun searchByCode(code: String): MutableList<Article>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(article: Article)

    @Update
    suspend fun update(article: Article)

    @Delete
    suspend fun delete(article: Article)
}