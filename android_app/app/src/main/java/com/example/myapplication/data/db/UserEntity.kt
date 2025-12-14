package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_logs")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val date: Long,           // 예: 20231214 (시간 순서 정렬용)
    val serviceName: String,  // 예: "배달의민족", "넷플릭스" (Group By의 기준!)
    val packageName: String,  // 예: "com.sample.baemin" (앱 매칭용)

    val cost: Int = 0,        // 결제 금액 (배민은 이게 높고)
    val timeMinutes: Int = 0, // 사용 시간 (넷플릭스는 이게 높음)

    val logType: String       // "PAYMENT" 또는 "USAGE" (나중에 필터링용)
)