package com.epiahackers.crudarticles.database

class ArticleRepository(private val articleDao: ArticleDao) {

    fun getArticles() = articleDao.getArticles()

    fun getOrderArticles() = articleDao.getOrderArticles()

    suspend fun getArticlesAsync() = articleDao.getArticlesAsync()

    fun getByCode(code: String): Article? {
        return articleDao.getByCode(code)
    }

    suspend fun getByCodeAsync(code: String): Article? {
        return articleDao.getByCodeAsync(code)
    }
    fun getLastId(): String? {
        return getArticles().let { if(it.size > 0) it.last().codi else null }
    }

    suspend fun insert(article: Article) {
        articleDao.insert(article)
    }

    suspend fun update(article: Article) {
        articleDao.update(article)
    }

    suspend fun delete(article: Article) {
        articleDao.delete(article)
    }

    suspend fun filterByStock() = articleDao.filterByStock()

    suspend fun searchByDescription(description: String) = articleDao.searchByDescription(description)

    suspend fun searchByCode(code: String) = articleDao.searchByCode(code)
}