package com.adwi.pexwallpapers.ui.favorites

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.adwi.pexwallpapers.R
import com.adwi.pexwallpapers.databinding.FragmentFavoritesBinding
import com.adwi.pexwallpapers.shared.WallpaperListAdapter
import com.adwi.pexwallpapers.shared.base.BaseFragment
import com.adwi.pexwallpapers.ui.TAG_PREVIEW_FRAGMENT
import com.adwi.pexwallpapers.ui.preview.PreviewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FavoritesFragment :
    BaseFragment<FragmentFavoritesBinding, FavoritesViewModel>(FragmentFavoritesBinding::inflate) {

    override val viewModel: FavoritesViewModel by viewModels()

    override fun setupViews() {
        setHasOptionsMenu(true)

        val favoritesAdapter = WallpaperListAdapter(
            onItemClick = { wallpaper ->
                val fragmentManager = parentFragmentManager.beginTransaction()
                fragmentManager.replace(R.id.fragmentContainerView, PreviewFragment(wallpaper))
                fragmentManager.addToBackStack(TAG_PREVIEW_FRAGMENT)
                fragmentManager.commit()
            },
            onFavoriteClick = { wallpaper ->
                viewModel.onFavoriteClick(wallpaper)
            },
            onShareClick = { TODO() },
            onDownloadClick = { TODO() }
        )

        binding.apply {
            recyclerView.apply {
                adapter = favoritesAdapter
                layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favorites.collect {
                    val favorites = it ?: return@collect

                    favoritesAdapter.submitList(favorites)
                    noFavoritesTextview.isVisible = favorites.isEmpty()
                    recyclerView.isVisible = favorites.isNotEmpty()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_favorites, menu)


    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_delete_all_favorites -> {
                viewModel.onDeleteAllFavorites()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}