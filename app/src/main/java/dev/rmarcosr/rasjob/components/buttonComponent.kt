package dev.rmarcosr.rasjob.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Component:
 * Button group to navigate on different screens.
 * @param options The list of options to navigate.
 * @param selectedIndex The index of the selected option.
 * @param onOptionSelected The function to call when an option is selected.
 */
@Composable
fun ButtonGroup(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            TextButton(
                onClick = { onOptionSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .background(backgroundColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = option, color = textColor)
            }
        }
    }
}

/**
 * Function used when press a button on the button group.
 * @param index The index of the button pressed.
 * @param navController The navigation controller to navigate between screens.
 * @see ButtonGroup
 */
fun onClick(index: Int, navController: NavController) {
    when (index) {
        0 -> navController.navigate("home")
        1 -> navController.navigate("add")
        2 -> navController.navigate("export")
    }
}