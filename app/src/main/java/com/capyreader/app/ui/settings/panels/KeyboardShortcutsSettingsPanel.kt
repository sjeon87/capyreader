package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun KeyboardShortcutsSettingsPanel() {
    KeyboardShortcutsList(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    )
}

@Composable
fun KeyboardShortcutsList(
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        FormSection(title = stringResource(R.string.settings_keybinding_keyboard_title)) {
            Column {
                KeybindingRow(
                    keys = "J",
                    description = stringResource(R.string.article_vertical_swipe_next_article),
                    context = stringResource(R.string.settings_keybinding_context_list_reader),
                )
                KeybindingRow(
                    keys = "K",
                    description = stringResource(R.string.article_vertical_swipe_previous_article),
                    context = stringResource(R.string.settings_keybinding_context_list_reader),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_enter),
                    description = stringResource(R.string.settings_keybinding_open_article),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_esc),
                    description = stringResource(R.string.settings_keybinding_close_reader),
                    context = stringResource(R.string.settings_keybinding_context_reader),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_space),
                    description = stringResource(R.string.settings_keybinding_scroll_down),
                    context = stringResource(R.string.settings_keybinding_context_reader),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_shift_space),
                    description = stringResource(R.string.settings_keybinding_scroll_up),
                    context = stringResource(R.string.settings_keybinding_context_reader),
                )
                KeybindingRow(
                    keys = "M",
                    description = stringResource(R.string.article_list_row_swipe_toggle_read),
                    context = stringResource(R.string.settings_keybinding_context_list_reader),
                )
                KeybindingRow(
                    keys = "F",
                    description = stringResource(R.string.article_list_row_swipe_toggle_starred),
                    context = stringResource(R.string.settings_keybinding_context_list_reader),
                )
                KeybindingRow(
                    keys = "W",
                    description = stringResource(R.string.article_vertical_swipe_full_content),
                    context = stringResource(R.string.settings_keybinding_context_reader),
                )
                KeybindingRow(
                    keys = "V",
                    description = stringResource(R.string.article_vertical_open_article_in_browser),
                    context = stringResource(R.string.settings_keybinding_context_list_reader),
                )
                KeybindingRow(
                    keys = "R",
                    description = stringResource(R.string.settings_keybinding_refresh),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_slash),
                    description = stringResource(R.string.settings_keybinding_search),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = "Shift + 1",
                    description = stringResource(R.string.filter_unread),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = "Shift + 2",
                    description = stringResource(R.string.filter_all),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = "Shift + 3",
                    description = stringResource(R.string.filter_starred),
                    context = stringResource(R.string.settings_keybinding_context_list),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_help_key),
                    description = stringResource(R.string.settings_keybinding_help),
                    context = stringResource(R.string.settings_keybinding_context_global),
                )
            }
        }

        FormSection(title = stringResource(R.string.settings_keybinding_volume_keys_title)) {
            Column {
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_volume_up),
                    description = stringResource(R.string.article_vertical_swipe_previous_article),
                    context = stringResource(R.string.settings_keybinding_context_reader_volume),
                )
                KeybindingRow(
                    keys = stringResource(R.string.settings_keybinding_volume_down),
                    description = stringResource(R.string.article_vertical_swipe_next_article),
                    context = stringResource(R.string.settings_keybinding_context_reader_volume),
                )
            }
        }
    }
}

@Composable
private fun KeybindingRow(
    keys: String,
    description: String,
    context: String,
) {
    ListItem(
        leadingContent = {
            Text(
                text = keys,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
        },
        headlineContent = { Text(description) },
        supportingContent = { Text(context) },
    )
}

@Preview
@Composable
private fun KeyboardShortcutsSettingsPanelPreview() {
    CapyTheme {
        KeyboardShortcutsSettingsPanel()
    }
}
