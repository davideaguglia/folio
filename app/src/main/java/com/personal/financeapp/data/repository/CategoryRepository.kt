package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.CategoryDao
import com.personal.financeapp.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val dao: CategoryDao) {
    fun getAll(): Flow<List<CategoryEntity>> = dao.getAll()
    fun getByType(type: String): Flow<List<CategoryEntity>> = dao.getByType(type)
    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)
    suspend fun insert(category: CategoryEntity) = dao.insert(category)
    suspend fun update(category: CategoryEntity) = dao.update(category)
    suspend fun delete(category: CategoryEntity) = dao.delete(category)
}
