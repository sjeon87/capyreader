package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    private val navigationBridge by inject<ArticleNavigationBridge>()

    private var pendingArticleID by mutableStateOf<String?>(null)

    /**
     * Key code of the last Shift shortcut fired from [onKeyDown], awaiting
     * its key-up. Consuming that key-up (instead of re-firing) keeps
     * Shift+Space from falling through to page-down and filters from
     * firing twice, regardless of the order Shift and the key are released.
     */
    private var pendingShiftKeyCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingArticleID = NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)

        setContent {
            App(
                startDestination = startDestination(),
                appPreferences = appPreferences,
                pendingArticleID = pendingArticleID,
                onPendingArticleSelected = { pendingArticleID = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingArticleID = NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (handleVolumeKeyEvent(keyCode)) {
            return true
        }

        if (handleShiftShortcut(keyCode, event)) {
            pendingShiftKeyCode = keyCode
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // Consume the matching event so the system doesn't adjust volume,
        // but don't fire navigation a second time for the same press.
        if (consumesVolumeKeyEvent(keyCode)) {
            return true
        }

        if (pendingShiftKeyCode != null && pendingShiftKeyCode == keyCode) {
            pendingShiftKeyCode = null
            return true
        }

        if (handleKeyboardNavigation(keyCode, event)) {
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    /**
     * Route volume up/down to article navigation when a reader has registered
     * callbacks with volume key support. Returning true consumes the event so
     * the system doesn't adjust the media volume.
     */
    private fun handleVolumeKeyEvent(keyCode: Int): Boolean {
        val callback = volumeKeyCallback(keyCode) ?: return false

        callback.invoke()

        return true
    }

    private fun consumesVolumeKeyEvent(keyCode: Int): Boolean {
        return volumeKeyCallback(keyCode) != null
    }

    private fun volumeKeyCallback(keyCode: Int): (() -> Unit)? {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> navigationBridge.onVolumeUp
            KeyEvent.KEYCODE_VOLUME_DOWN -> navigationBridge.onVolumeDown
            else -> null
        }
    }

    /**
     * Fire Shift-modified shortcuts on key-down, where Shift is guaranteed
     * to still be held. Reading isShiftPressed on key-up misses presses
     * where Shift is released first.
     */
    private fun handleShiftShortcut(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.isCtrlPressed == true || event?.isAltPressed == true || event?.isMetaPressed == true) {
            return false
        }

        if (event?.isShiftPressed != true) {
            return false
        }

        val callback = when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> navigationBridge.onPageUp
            KeyEvent.KEYCODE_1 -> navigationBridge.onShowUnread
            KeyEvent.KEYCODE_2 -> navigationBridge.onShowAll
            KeyEvent.KEYCODE_3 -> navigationBridge.onShowStarred
            else -> null
        } ?: return false

        // Swallow key auto-repeat; the action already fired on first press.
        if (event?.repeatCount != 0) {
            return true
        }

        callback.invoke()

        return true
    }

    /**
     * Route hardware keyboard shortcuts to article navigation and actions when
     * an article surface has registered callbacks. Shift-modified shortcuts
     * fire on key-down (see [handleShiftShortcut]); everything else is
     * consumed on key-up where Compose focus handling is settled.
     */
    private fun handleKeyboardNavigation(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.isCtrlPressed == true || event?.isAltPressed == true || event?.isMetaPressed == true) {
            return false
        }

        if (keyCode == KeyEvent.KEYCODE_H || event?.unicodeChar == '?'.code) {
            val callback = navigationBridge.onShowHelp ?: return false
            callback.invoke()
            return true
        }

        val callback = when (keyCode) {
            KeyEvent.KEYCODE_J -> navigationBridge.onSelectNextArticle
            KeyEvent.KEYCODE_K -> navigationBridge.onSelectPreviousArticle
            KeyEvent.KEYCODE_M -> navigationBridge.onToggleRead
            KeyEvent.KEYCODE_F -> navigationBridge.onToggleStar
            KeyEvent.KEYCODE_W -> navigationBridge.onToggleFullContent
            KeyEvent.KEYCODE_V -> navigationBridge.onOpenInBrowser
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_O -> navigationBridge.onOpenArticle
            KeyEvent.KEYCODE_ESCAPE -> navigationBridge.onBack
            // Shift+Space fires on key-down; a shifted key-up reaching here
            // (overlapping chords) must not fall through to page-down.
            KeyEvent.KEYCODE_SPACE -> navigationBridge.onPageDown.takeUnless { event?.isShiftPressed == true }
            KeyEvent.KEYCODE_R -> navigationBridge.onRefresh
            KeyEvent.KEYCODE_SLASH -> navigationBridge.onFocusSearch
            else -> null
        } ?: return false

        callback.invoke()

        return true
    }

    private fun startDestination(): Route {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount
        } else {
            Route.Articles
        }
    }
}
