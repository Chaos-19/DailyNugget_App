package com.chaosdev.devbuddy.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * A generic, reusable wheel-style picker.
 *
 * @param items The list of strings to display in the wheel.
 * @param onItemSelected A callback invoked when the user stops scrolling on a new item.
 * @param modifier Modifier for this composable.
 * @param initialIndex The index of the item to be centered initially.
 * @param itemHeight The height of each item in the wheel.
 * @param visibleItemsCount The number of items visible in the picker viewport.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    onItemSelected: (index: Int, item: String) -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    itemHeight: Dp = 60.dp,
    visibleItemsCount: Int = 3
) {
    val listState = rememberLazyListState()
    val pickerHeight = itemHeight * visibleItemsCount

    // Find the index of the item that is closest to the center of the viewport.
    val centralItemIndex by remember {
        derivedStateOf {
            if (listState.layoutInfo.visibleItemsInfo.isEmpty() || listState.layoutInfo.viewportSize.height == 0) {
                -1
            } else {
                val viewportCenter = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height / 2
                listState.layoutInfo.visibleItemsInfo
                    .minByOrNull { (it.offset + it.size / 2 - viewportCenter).absoluteValue }
                    ?.index ?: -1
            }
        }
    }

    // Call the callback when scrolling stops and the central item is valid.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && centralItemIndex != -1 && items.isNotEmpty()) {
            onItemSelected(centralItemIndex, items[centralItemIndex])
        }
    }

    // Scroll to the initial item on the first composition.
    val density = LocalDensity.current
    LaunchedEffect(listState, items) {
        if (initialIndex in items.indices) {
            val pickerHeightPx = with(density) { pickerHeight.toPx() }
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val scrollOffset = (pickerHeightPx / 2 - itemHeightPx / 2).toInt()
            listState.scrollToItem(initialIndex, scrollOffset)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(pickerHeight),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = pickerHeight / 2 - itemHeight / 2)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == centralItemIndex
            Box(
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF2E3A59) else Color.Gray
                    )
                )
            }
        }
    }
}