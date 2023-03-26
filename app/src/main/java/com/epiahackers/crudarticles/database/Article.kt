package com.epiahackers.crudarticles.database

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.epiahackers.crudarticles.R

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey @ColumnInfo(name = "CODIARTICLE") val codi: String,
    @ColumnInfo(name = "DESCRIPCIO") val descripcio: String,
    @ColumnInfo(name = "FAMILIA") val familia: String?,
    @ColumnInfo(name = "PREUSENSEIVA") val preuSenseIVA: Double,
    @ColumnInfo(name = "ESTOC") val estoc: Double
){
    companion object{

        fun getFamilyValues(context: Context) = arrayOf(context.getString(R.string.family_software), context.getString(R.string.family_hardware), context.getString(R.string.family_default))
    }
}