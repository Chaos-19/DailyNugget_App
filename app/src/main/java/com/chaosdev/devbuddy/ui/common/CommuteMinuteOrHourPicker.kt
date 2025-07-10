import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaosdev.devbuddy.ui.common.WheelPicker

/**
 * A two-wheel picker for selecting hours and minutes.
 *
 * @param onTimeSelected A callback that provides the selected hour and minute.
 * @param initialHour The hour to be selected initially (0-23).
 * @param initialMinute The minute to be selected initially (0-59).
 */
@Composable
fun HourAndMinutePicker(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    initialHour: Int = 1,
    initialMinute: Int = 30
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    // Notify the parent composable when the selection changes.
    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(selectedHour, selectedMinute)
    }

    val itemHeight = 60.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        // The Box provides the central highlight background for the pickers.
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // The gray highlight "pill" in the background
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F3F5))
            )

            // The row containing the two pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Picker
                WheelPicker(
                    items = hours,
                    onItemSelected = { _, item -> selectedHour = item.toInt() },
                    initialIndex = hours.indexOf(initialHour.toString().padStart(2, '0')),
                    itemHeight = itemHeight
                )

                Text(
                    ":",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF2E3A59),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(bottom = 5.dp)
                )

                // Minute Picker
                WheelPicker(
                    items = minutes,
                    onItemSelected = { _, item -> selectedMinute = item.toInt() },
                    initialIndex = minutes.indexOf(initialMinute.toString().padStart(2, '0')),
                    itemHeight = itemHeight
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 320)
@Composable
fun HourAndMinutePickerPreview() {
    var selectedTime by remember { mutableStateOf("01:30") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HourAndMinutePicker(
            onTimeSelected = { hour, minute ->
                selectedTime =
                    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Selected Time: $selectedTime")
    }
}