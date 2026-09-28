package com.capyreader.app

import android.content.Context
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.initialArticleIndex
import com.capyreader.app.ui.articles.neighborArticleIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class ArticleNavigationTest {
    private lateinit var context: Context
    private lateinit var appPreferences: AppPreferences

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        appPreferences = AppPreferences(context).also { it.clearAll() }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `bridge starts with no callbacks`() {
        val bridge = ArticleNavigationBridge()

        assertNull(bridge.onSelectPreviousArticle)
        assertNull(bridge.onSelectNextArticle)
    }

    @Test
    fun `bridge routes to latest registration and forgets on unregister`() {
        val bridge = ArticleNavigationBridge()
        val first = Any()
        val second = Any()
        var calls = mutableListOf<String>()
        bridge.register(
            first,
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = { calls.add("first-prev") },
                onSelectNextArticle = { calls.add("first-next") },
            ),
        )
        bridge.register(
            second,
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = { calls.add("second-prev") },
                onSelectNextArticle = { calls.add("second-next") },
            ),
        )

        bridge.onSelectNextArticle?.invoke()
        bridge.onSelectPreviousArticle?.invoke()
        assertEquals(listOf("second-next", "second-prev"), calls)

        bridge.unregister(second)
        bridge.onSelectNextArticle?.invoke()
        assertEquals(listOf("second-next", "second-prev", "first-next"), calls)

        bridge.unregister(first)
        assertNull(bridge.onSelectPreviousArticle)
        assertNull(bridge.onSelectNextArticle)
    }

    @Test
    fun `unregistering stale owner keeps latest registration`() {
        val bridge = ArticleNavigationBridge()
        val stale = Any()
        val current = Any()
        bridge.register(
            stale,
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = {},
                onSelectNextArticle = {},
            ),
        )
        val next = { }
        val prev = { }
        bridge.register(
            current,
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = prev,
                onSelectNextArticle = next,
            ),
        )

        bridge.unregister(stale)

        assertEquals(next, bridge.onSelectNextArticle)
        assertEquals(prev, bridge.onSelectPreviousArticle)
    }

    @Test
    fun `volume key navigation defaults to disabled`() {
        assertFalse(appPreferences.readerOptions.enableVolumeKeyNavigation.get())
    }

    @Test
    fun `volume routing skips registrations that opt out`() {
        val bridge = ArticleNavigationBridge()
        var volumeCalls = 0
        bridge.register(
            Any(),
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = { volumeCalls++ },
                onSelectNextArticle = { volumeCalls++ },
                handlesVolumeKeys = false,
            ),
        )

        assertNull(bridge.onVolumeUp)
        assertNull(bridge.onVolumeDown)
    }

    @Test
    fun `volume routing prefers latest volume-capable registration`() {
        val bridge = ArticleNavigationBridge()
        var calls = mutableListOf<String>()
        bridge.register(
            Any(),
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = { calls.add("reader-prev") },
                onSelectNextArticle = { calls.add("reader-next") },
            ),
        )
        bridge.register(
            Any(),
            ArticleNavigationBridge.Callbacks(
                onSelectPreviousArticle = { calls.add("list-prev") },
                onSelectNextArticle = { calls.add("list-next") },
                handlesVolumeKeys = false,
            ),
        )

        bridge.onVolumeUp?.invoke()
        bridge.onVolumeDown?.invoke()

        assertEquals(listOf("reader-prev", "reader-next"), calls)
    }

    @Test
    fun `keyboard navigation walks ids and skips placeholders`() {
        val ids = listOf("a", null, "b", "c")

        assertEquals(2, neighborArticleIndex(ids, 0, 1))
        assertEquals(0, neighborArticleIndex(ids, 2, -1))
        assertEquals(-1, neighborArticleIndex(ids, 3, 1))
        assertEquals(-1, neighborArticleIndex(ids, 0, -1))
    }

    @Test
    fun `keyboard navigation lands on first visible loaded article`() {
        val ids = listOf(null, "a", "b")

        assertEquals(1, initialArticleIndex(ids, 0))
        assertEquals(2, initialArticleIndex(ids, 2))
        assertEquals(-1, initialArticleIndex(listOf(null, null), 0))
        assertEquals(-1, initialArticleIndex(emptyList(), 0))
    }
}
