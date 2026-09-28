package com.capyreader.app

/**
 * Bridges hardware key events received by [MainActivity] (volume keys and
 * bluetooth-keyboard J/K presses) to whichever article surface is showing.
 *
 * Each surface (reader, article list) registers its navigation callbacks
 * under a unique owner token for the lifetime of its composition and removes
 * exactly its own entry on disposal, so overlapping compositions can't clear
 * each other's registrations. When nothing is registered, all callbacks are
 * null and the keys fall through to the system's default behavior.
 *
 * Volume keys only route to registrations with [Callbacks.handlesVolumeKeys]
 * set, so the article list can offer J/K navigation without hijacking the
 * system volume. J/K route to the latest registration regardless.
 *
 * Key events and composition both run on the main thread, so no additional
 * synchronization is needed.
 */
class ArticleNavigationBridge internal constructor() {
    private val registrations = linkedMapOf<Any, Callbacks>()

    fun register(owner: Any, callbacks: Callbacks) {
        registrations[owner] = callbacks
    }

    fun unregister(owner: Any) {
        registrations.remove(owner)
    }

    private val latestVolumeRegistration: Callbacks?
        get() = registrations.values.lastOrNull { it.handlesVolumeKeys }

    val onSelectPreviousArticle: (() -> Unit)?
        get() = findLatestCallback { it.onSelectPreviousArticle }

    val onSelectNextArticle: (() -> Unit)?
        get() = findLatestCallback { it.onSelectNextArticle }

    val onToggleRead: (() -> Unit)?
        get() = findLatestCallback { it.onToggleRead }

    val onToggleStar: (() -> Unit)?
        get() = findLatestCallback { it.onToggleStar }

    val onToggleFullContent: (() -> Unit)?
        get() = findLatestCallback { it.onToggleFullContent }

    val onOpenInBrowser: (() -> Unit)?
        get() = findLatestCallback { it.onOpenInBrowser }

    val onOpenArticle: (() -> Unit)?
        get() = findLatestCallback { it.onOpenArticle }

    val onBack: (() -> Unit)?
        get() = findLatestCallback { it.onBack }

    val onPageDown: (() -> Unit)?
        get() = findLatestCallback { it.onPageDown }

    val onPageUp: (() -> Unit)?
        get() = findLatestCallback { it.onPageUp }

    val onRefresh: (() -> Unit)?
        get() = findLatestCallback { it.onRefresh }

    val onFocusSearch: (() -> Unit)?
        get() = findLatestCallback { it.onFocusSearch }

    val onShowHelp: (() -> Unit)?
        get() = findLatestCallback { it.onShowHelp }

    val onVolumeUp: (() -> Unit)?
        get() = latestVolumeRegistration?.onSelectPreviousArticle

    val onVolumeDown: (() -> Unit)?
        get() = latestVolumeRegistration?.onSelectNextArticle

    private fun findLatestCallback(selector: (Callbacks) -> (() -> Unit)?): (() -> Unit)? {
        for (callbacks in registrations.values.reversed()) {
            val callback = selector(callbacks)
            if (callback != null) {
                return callback
            }
        }
        return null
    }

    data class Callbacks(
        val onSelectPreviousArticle: (() -> Unit)? = null,
        val onSelectNextArticle: (() -> Unit)? = null,
        val onToggleRead: (() -> Unit)? = null,
        val onToggleStar: (() -> Unit)? = null,
        val onToggleFullContent: (() -> Unit)? = null,
        val onOpenInBrowser: (() -> Unit)? = null,
        val onOpenArticle: (() -> Unit)? = null,
        val onBack: (() -> Unit)? = null,
        val onPageDown: (() -> Unit)? = null,
        val onPageUp: (() -> Unit)? = null,
        val onRefresh: (() -> Unit)? = null,
        val onFocusSearch: (() -> Unit)? = null,
        val onShowHelp: (() -> Unit)? = null,
        val handlesVolumeKeys: Boolean = true,
    )
}
