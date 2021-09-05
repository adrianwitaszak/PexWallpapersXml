package com.adwi.pexwallpapers

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.adwi.pexwallpapers.databinding.ActivityMainBinding
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNav: ChipNavigationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        bottomNav = binding.bottomNav
        binding.apply {
            bottomNav.setItemSelected(R.id.wallpapersFragment)
            bottomNav.setOnItemSelectedListener { itemId ->
                when (itemId) {
                    R.id.wallpapersFragment -> {
                        navController.navigate(R.id.wallpapersFragment)
                    }
                    R.id.searchFragment -> {
                        navController.navigate(R.id.searchFragment)
                    }
                    R.id.favoritesFragment -> {
                        navController.navigate(R.id.favoritesFragment)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.wallpapersFragment ||
                destination.id == R.id.searchFragment ||
                destination.id == R.id.favoritesFragment
//                destination.id == R.id.previewFragment
            )
                bottomNavIsVisible(true) else bottomNavIsVisible(false)
        }
    }

    private fun bottomNavIsVisible(isVisible: Boolean) {
        binding.bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }
}