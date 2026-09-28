package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
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

        Spacer(Modifier.height(16.dp))
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
