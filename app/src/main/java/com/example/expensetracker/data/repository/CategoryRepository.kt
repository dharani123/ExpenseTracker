package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String, colorIndex: Int) {
        categoryDao.insert(CategoryEntity(name = name.trim(), isDefault = false, colorIndex = colorIndex))
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.clearCategoryFromExpenses(category.id)
        categoryDao.delete(category)
    }
}
