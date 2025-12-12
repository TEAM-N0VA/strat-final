package com.example.myapplication

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PaymentNotificationListener : NotificationListenerService() {

    // 알림이 오면 실행되는 함수
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName = sbn?.packageName // 알림을 보낸 앱 이름
        val extras = sbn?.notification?.extras
        val title = extras?.getString("android.title") // 알림 제목
        val text = extras?.getString("android.text")   // 알림 내용

        Log.d("PaymentListener", "앱: $packageName | 제목: $title | 내용: $text")

        // '결제' 혹은 '승인'이라는 단어가 포함되어 있으면 로그를 띄움
        if (text != null && (text.contains("결제") || text.contains("승인"))) {
            Log.d("PaymentListener", "💰 결제 알림 포착됨! : $text")
            // DB 저장 로직을 추가
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}