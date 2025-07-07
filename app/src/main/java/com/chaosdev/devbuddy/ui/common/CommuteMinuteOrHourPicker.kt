package com.chaosdev.devbuddy.ui.common
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.text.font.FontWeight


@Composable
fun CommuteMinuteOrHourPicker(
  onSetCommuteTime: (String) -> Unit
) {
    val minuteList = (0..59).map { "$it mins" }
    val hourList = (1..5).map { "${it} h" }
    val pickerList = minuteList + hourList

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val selectedItemIndex by remember {
        derivedStateOf {
            val center = listState.firstVisibleItemIndex +
                    (listState.firstVisibleItemScrollOffset / 50)
            center.coerceIn(0, pickerList.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .height(140.dp)
            .background(color = Color(0xFFF0F2F5), shape = RoundedCornerShape(15.dp))
            .padding(vertical = 8.dp)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 40.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(pickerList) { index, item ->
                Text(
                    text = item,
                    fontSize = if (index == selectedItemIndex) 20.sp else 16.sp,
                    fontWeight = if (index == selectedItemIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == selectedItemIndex) Color.Black else Color.Gray,

                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewDurationPicker() {
    MaterialTheme { // Use your app's theme
        CommuteMinuteOrHourPicker(onSetCommuteTime = { commuteTime: String -> {} })
    }
}