package com.capyreader.app

/**
 * Bridges hardware key events received by [MainActivity] (volume keys and
 * bluetooth-keyboard J/K presses) to whichever article surface is showing.
 *
 * Each surface (reader, article list) registers its navigation callbacks
 * under a unique owner token for the lifetime of its composition and removes
 * exactly its own entry on disposal, so overlapping compositions can't clear
 * each other's registrations. When nothing is registered (or the navigation
 * option is disabled), all callbacks are null and the keys fall through to
 * the system's default behavior.
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

    private val latestRegistration: Callbacks?
        get() = registrations.values.lastOrNull()

    private val latestVolumeRegistration: Callbacks?
        get() = registrations.values.lastOrNull { it.handlesVolumeKeys }

    val onSelectPreviousArticle: (() -> Unit)?
        get() = latestRegistration?.onSelectPreviousArticle

    val onSelectNextArticle: (() -> Unit)?
        get() = latestRegistration?.onSelectNextArticle

    val onVolumeUp: (() -> Unit)?
        get() = latestVolumeRegistration?.onSelectPreviousArticle

    val onVolumeDown: (() -> Unit)?
        get() = latestVolumeRegistration?.onSelectNextArticle

    data class Callbacks(
        val onSelectPreviousArticle: () -> Unit,
        val onSelectNextArticle: () -> Unit,
        val handlesVolumeKeys: Boolean = true,
    )
}
