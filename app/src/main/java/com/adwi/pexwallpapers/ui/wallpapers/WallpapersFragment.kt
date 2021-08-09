package com.adwi.pexwallpapers.ui.wallpapers

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.adwi.pexwallpapers.R
import com.adwi.pexwallpapers.databinding.FragmentWallpapersBinding
import com.adwi.pexwallpapers.shared.adapter.WallpaperListAdapter
import com.adwi.pexwallpapers.shared.base.BaseFragment
import com.adwi.pexwallpapers.shared.tools.SharingTools
import com.adwi.pexwallpapers.ui.preview.PreviewFragment
import com.adwi.pexwallpapers.util.*
import com.adwi.pexwallpapers.util.Constants.Companion.WALLPAPER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class WallpapersFragment :
    BaseFragment<FragmentWallpapersBinding>(FragmentWallpapersBinding::inflate, false) {

    override val viewModel: WallpaperViewModel by viewModels()

    private var _wallpaperListAdapter: WallpaperListAdapter? = null
    private val wallpaperListAdapter get() = _wallpaperListAdapter!!

    override fun setupViews() {
        setHasOptionsMenu(true)

        _wallpaperListAdapter = WallpaperListAdapter(
            onItemClick = { wallpaper ->
                navigateToFragmentWithArgumentInt(WALLPAPER_ID, wallpaper.id, PreviewFragment())
            },
            onShareClick = { wallpaper ->
                wallpaper.url?.let {
                    SharingTools(requireContext()).share(it)
                }
            },
            onFavoriteClick = { wallpaper ->
                viewModel.onFavoriteClick(wallpaper)
            },
            onPexelLogoClick = { wallpaper ->
                val uri = Uri.parse(wallpaper.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            requireActivity = requireActivity()
        )

        binding.apply {
            shimmerFrameLayout.startShimmer()

            recyclerView.apply {
                adapter = wallpaperListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                // hide item strange animation even when favorite clicked
                itemAnimator = null
                itemAnimator?.changeDuration = 0
                val divider =
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                divider.setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.list_tem_separator
                    )!!
                )
                addItemDecoration(divider)
            }



            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.wallpaperList.collect {
                    val result = it ?: return@collect

                    swipeRefreshLayout.isRefreshing = result is Resource.Loading
                    shimmerFrameLayout.apply {
                        if (result.data.isNullOrEmpty()) startShimmer() else stopShimmer()
                    }
                    shimmerFrameLayout.isVisible = result.data.isNullOrEmpty()
                    recyclerView.isVisible = !result.data.isNullOrEmpty()
                    errorTextview.isVisible = result.error != null && result.data.isNullOrEmpty()
                    retryButton.isVisible = result.error != null && result.data.isNullOrEmpty()
                    errorTextview.text = getString(
                        R.string.could_not_refresh,
                        result.error?.localizedMessage
                            ?: getString(R.string.unknown_error_occurred)
                    )

                    wallpaperListAdapter.submitList(result.data) {
                        if (viewModel.pendingScrollToTopAfterRefresh) {
                            recyclerView.smoothScrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            retryButton.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect { event ->
                    when (event) {
                        is WallpaperViewModel.Event.ShowErrorMessage -> showSnackbar(
                            getString(
                                R.string.could_not_refresh,
                                event.error.localizedMessage
                                    ?: getString(R.string.unknown_error_occurred)
                            )
                        )
                    }.exhaustive
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_wallpapers, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun onPause() {
        binding.shimmerFrameLayout.stopShimmer()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _wallpaperListAdapter = null
    }
}