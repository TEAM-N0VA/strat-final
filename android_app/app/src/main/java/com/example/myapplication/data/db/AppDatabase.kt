package com.example.myapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. 여기에 사용할 Entity(일기장 양식)를 등록합니다.
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // 2. DAO(도구)를 꺼내 쓰는 함수를 만듭니다.
    abstract fun userDao(): UserDao

    // 3. 싱글톤 패턴 (앱 전체에서 DB를 하나만 쓰도록 설정)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subscription_database" // 폰에 저장될 실제 파일 이름
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}