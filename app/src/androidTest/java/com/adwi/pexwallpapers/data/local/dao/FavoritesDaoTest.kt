package com.adwi.pexwallpapers.data.local.dao

import androidx.test.filters.SmallTest
import com.adwi.pexwallpapers.data.local.WallpaperDatabase
import com.adwi.pexwallpapers.data.local.entity.Wallpaper
import com.adwi.pexwallpapers.data.local.entity.WallpaperMockAndroid
import com.adwi.pexwallpapers.CoroutineAndroidTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class FavoritesDaoTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val coroutineScope = CoroutineAndroidTestRule()

    @Inject
    @Named("test_database")
    lateinit var database: WallpaperDatabase

    private lateinit var favoritesDao: FavoritesDao
    private lateinit var wallpaperDao: WallpapersDao

    private val wallpaperList = WallpaperMockAndroid.list

    @Before
    fun setup() {
        hiltRule.inject()
        favoritesDao = database.favoritesDao()
        wallpaperDao = database.wallpaperDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun updateWallpaperFavorite_getAllFavorites_returnsTrue() =
        coroutineScope.dispatcher.runBlockingTest {

            wallpaperDao.insertWallpapers(wallpaperList)
            val wallpaper = WallpaperMockAndroid.first
            wallpaper.isFavorite = true
            favoritesDao.updateWallpaper(wallpaper)

            val actual = wallpaperDao.getAllWallpapers().first()[0]
            Assert.assertEquals(wallpaper, actual)
        }

    @Test
    fun resetAllFavorites_returnsTrue() = coroutineScope.dispatcher.runBlockingTest {

        wallpaperDao.insertWallpapers(wallpaperList)

        val checked = favoritesDao.getAllFavorites().first()
        // Check if 2 isFavorite wallpapers inserted
        Assert.assertEquals(checked.size, 2)

        favoritesDao.resetAllFavorites()

        val actual: List<Wallpaper> = favoritesDao.getAllFavorites().first()
        // Check if all isFavorite has been removed
        Assert.assertEquals(actual, emptyList<Wallpaper>())
    }

    @Test
    fun deleteNonFavoriteWallpapersOlderThan_returnsTrue() =
        coroutineScope.dispatcher.runBlockingTest {

            // Inserting 4 wallpapers, two of them are isFavorite = true
            wallpaperDao.insertWallpapers(wallpaperList)

            val list = wallpaperDao.getAllWallpapers().first()
            // Confirm list.size = 4
            Assert.assertEquals(4, list.size)

            val checked = favoritesDao.getAllFavorites().first()
            // Confirm that 2 favorites in the list
            Assert.assertEquals(2, checked.size)

            // Delete all old non-favorites
            favoritesDao.deleteNonFavoriteWallpapersOlderThan(1630665095293)

            val actual = wallpaperDao.getAllWallpapers().first()
            // Check if all old isFavorite has been removed, returns list size 2
            Assert.assertEquals(2, actual.size)
        }
}