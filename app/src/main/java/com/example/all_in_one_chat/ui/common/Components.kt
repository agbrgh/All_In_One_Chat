package com.example.all_in_one_chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun ServiceBadge(serviceType: ChatServiceType, isSmall: Boolean = false) {
    val color = getServiceColor(serviceType)
    val contentColor = getOnServiceColor(serviceType)

    Surface(
        color = color,
        shape = RoundedCornerShape(if (isSmall) 6.dp else 8.dp),
        shadowElevation = if (isSmall) 0.dp else 4.dp,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = serviceType.name.take(1),
            color = contentColor,
            fontSize = if (isSmall) 10.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = if (isSmall) 4.dp else 6.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceBadgePreview() {
    All_In_One_ChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChatServiceType.entries.forEach { serviceType ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServiceBadge(serviceType = serviceType, isSmall = false)
                    ServiceBadge(serviceType = serviceType, isSmall = true)
                }
            }
        }
    }
}
