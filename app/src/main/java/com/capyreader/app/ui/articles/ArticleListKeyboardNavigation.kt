package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.ArticleNavigationBridge
import com.jocmp.capy.Article
import org.koin.compose.koinInject

/**
 * J/K navigation while the article list is showing and no reader is open.
 *
 * J selects the next article (opening it), K selects the previous one. With
 * nothing selected yet, J lands on the first loaded article at or below the
 * top visible item. Callers must disable the handler while typing, or while
 * a drawer, dialog, sheet, or media viewer is open.
 *
 * Volume keys are never consumed here ([handlesVolumeKeys] is false), so
 * this registration can't hijack the system volume.
 */
@Composable
fun ArticleListKeyboardNavigation(
    enabled: Boolean,
    articles: LazyPagingItems<Article>,
    currentArticleId: String?,
    listState: LazyListState,
    onSelectArticle: (articleID: String) -> Unit,
    onToggleRead: ((article: Article) -> Unit)? = null,
    onToggleStar: ((article: Article) -> Unit)? = null,
    onOpenInBrowser: ((url: String) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onFocusSearch: (() -> Unit)? = null,
    navigationBridge: ArticleNavigationBridge = koinInject(),
) {
    fun select(direction: Int) {
        if (articles.itemCount == 0) {
            return
        }
        val ids = articleIds(articles)
        if (ids.isEmpty()) {
            return
        }
        val currentIndex = currentArticleId?.let { id ->
            ids.indexOfFirst { it == id }
        } ?: -1
        val target = if (currentIndex == -1) {
            initialArticleIndex(ids, listState.firstVisibleItemIndex)
        } else {
            neighborArticleIndex(ids, currentIndex, direction)
        }
        if (target != -1) {
            ids[target]?.let(onSelectArticle)
        }
    }

    fun activeArticle(): Article? {
        if (articles.itemCount == 0) return null
        val snapshot = articles.itemSnapshotList
        if (currentArticleId != null) {
            val found = snapshot.firstOrNull { it?.id == currentArticleId }
            if (found != null) return found
        }
        val targetIndex = initialArticleIndex(articleIds(articles), listState.firstVisibleItemIndex)
        return if (targetIndex != -1) snapshot.getOrNull(targetIndex) else null
    }

    fun openArticle() {
        val target = activeArticle() ?: return
        onSelectArticle(target.id)
    }

    fun toggleRead() {
        val target = activeArticle() ?: return
        onToggleRead?.invoke(target)
    }

    fun toggleStar() {
        val target = activeArticle() ?: return
        onToggleStar?.invoke(target)
    }

    fun openInBrowser() {
        val target = activeArticle() ?: return
        target.url?.let { onOpenInBrowser?.invoke(it.toString()) }
    }

    val latestSelect = rememberUpdatedState(::select)
    val latestOpenArticle = rememberUpdatedState(::openArticle)
    val latestToggleRead = rememberUpdatedState(::toggleRead)
    val latestToggleStar = rememberUpdatedState(::toggleStar)
    val latestOpenInBrowser = rememberUpdatedState(::openInBrowser)
    val latestRefresh = rememberUpdatedState(onRefresh)
    val latestFocusSearch = rememberUpdatedState(onFocusSearch)
    val owner = remember { Any() }

    DisposableEffect(owner, enabled) {
        if (enabled) {
            navigationBridge.register(
                owner,
                ArticleNavigationBridge.Callbacks(
                    onSelectPreviousArticle = { latestSelect.value(-1) },
                    onSelectNextArticle = { latestSelect.value(1) },
                    onOpenArticle = { latestOpenArticle.value() },
                    onToggleRead = { latestToggleRead.value() },
                    onToggleStar = { latestToggleStar.value() },
                    onOpenInBrowser = { latestOpenInBrowser.value() },
                    onRefresh = { latestRefresh.value?.invoke() },
                    onFocusSearch = { latestFocusSearch.value?.invoke() },
                    handlesVolumeKeys = false,
                ),
            )
        }

        onDispose {
            navigationBridge.unregister(owner)
        }
    }
}

/**
 * IDs of loaded articles in snapshot order; null entries are paging
 * placeholders for not-yet-loaded articles.
 */
internal fun articleIds(articles: LazyPagingItems<Article>): List<String?> {
    val snapshot = articles.itemSnapshotList
    return List(snapshot.size) { index -> snapshot[index]?.id }
}

/**
 * Index of the next loaded article from [currentIndex] in [direction]
 * (+1 forward, -1 backward), skipping paging placeholders, or -1 when there
 * is nothing loaded in that direction.
 */
internal fun neighborArticleIndex(ids: List<String?>, currentIndex: Int, direction: Int): Int {
    var i = currentIndex + direction
    while (i in ids.indices) {
        if (ids[i] != null) {
            return i
        }
        i += direction
    }
    return -1
}

/**
 * Landing index when nothing is selected: the first loaded article at or
 * after [startIndex] (typically the top visible item), falling back to the
 * first loaded article overall. Returns -1 when nothing is loaded.
 */
internal fun initialArticleIndex(ids: List<String?>, startIndex: Int): Int {
    if (ids.isEmpty()) {
        return -1
    }
    val start = startIndex.coerceIn(0, ids.lastIndex)
    for (i in start..ids.lastIndex) {
        if (ids[i] != null) {
            return i
        }
    }
    for (i in 0 until start) {
        if (ids[i] != null) {
            return i
        }
    }
    return -1
}
