package com.adwi.pexwallpapers.ui.settings

import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adwi.pexwallpapers.R
import com.adwi.pexwallpapers.data.local.entity.Settings
import com.adwi.pexwallpapers.databinding.FragmentSettingsBinding
import com.adwi.pexwallpapers.ui.base.BaseFragment
import com.adwi.pexwallpapers.util.Constants.Companion.WORK_AUTO_WALLPAPER
import com.adwi.pexwallpapers.util.launchCoroutine
import com.adwi.pexwallpapers.util.showSnackbar
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding, Any>(
    inflate = FragmentSettingsBinding::inflate
) {
    override val viewModel: SettingsViewModel by viewModels()

    private lateinit var settings: Settings

    private var currentSliderValue = 5f

    override fun setupToolbar() {}

    override fun setupViews() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            launchCoroutine {
                checkPermissions(this@SettingsFragment)
            }
        }
    }

    override fun setupListeners() {
        binding.apply {

            // Toolbar
            menuButton.setOnClickListener {
                showMenu(it, R.menu.settings_menu)
            }

            // Switches
            newWallpaperSwitch.setOnCheckedChangeListener { _, checked ->
                viewModel.updateNewWallpaperSet(checked)
            }
            wallpaperRecomendationsSwitch.setOnCheckedChangeListener { _, checked ->
                viewModel.updateWallpaperRecommendations(checked)
            }
            autoWallpaperSwitch.setOnCheckedChangeListener { _, checked ->
                viewModel.updateAutoChangeWallpaper(checked)
                if (!checked) {
                    viewModel.cancelWorks(WORK_AUTO_WALLPAPER)
                    val message =
                        "${getString(R.string.auto_change_wallpaper)} ${getString(R.string.is_disabled)}"
                    showSnackbar(
                        message = message,
                        actionTitle = R.string.enable,
                        action = { viewModel.updateAutoChangeWallpaper(true) }
                    )
                }
            }
            downloadOverWifiSwitch.setOnCheckedChangeListener { _, checked ->
                viewModel.updateDownloadOverWiFi(checked)
            }

            // Radios
            changePeriodRadioGroup.setOnCheckedChangeListener { _, itemId ->
                viewModel.updateChangePeriodType(itemId)
                setSlider(itemId)
            }

            // Checkboxes
            homeCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoHome(isChecked)
            }
            lockCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoLock(isChecked)
            }

            // Slider
            periodSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                override fun onStopTrackingTouch(slider: Slider) {
                    currentSliderValue = slider.value
                    viewModel.updateChangePeriodValue(slider.value)
                    periodDurationValue.text = slider.value.toInt().toString()
                }
            })

            // Buttons
            saveAutomationButton.setOnClickListener {
                launchCoroutine {
                    when {
                        !settings.autoHome && !settings.autoLock -> showSnackbar(
                            getString(R.string.minimum_one_screen_need_to_be_selected)
                        )
                        viewModel.favorites.value.size < 2 -> showSnackbar(
                            getString(R.string.add_minimum_2_wallpapers_to_favorites)
                        )
                        else -> checkStoragePermission(
                            this@SettingsFragment,
                            granted = {
                                viewModel.saveSettings(settings)
                                showSnackbar(
                                    getString(
                                        R.string.wallpaper_will_change_in,
                                        settings.sliderValue.toInt().toString(),
                                        getTimeRange(settings.sliderValue, settings.selectedButton)
                                    )
                                )
                            }
                        )
                    }
                }
            }
            fixButton.setOnClickListener {
                launchCoroutine {
                    checkPermissions(this@SettingsFragment)
                }
            }
            aboutButton.setOnClickListener { }
            supportButton.setOnClickListener { viewModel.contactSupport() }
            privacyPolicyButton.setOnClickListener {}
        }
    }

    override fun setupFlows() {
        binding.apply {
            launchCoroutine {
                viewModel.isStoragePermissionGranted.collect { permission ->
                    fixLayout.isVisible = !permission
                }
            }
            launchCoroutine {
                viewModel.settings.collect {
                    settings = it

                    newWallpaperSwitch.isChecked = it.newWallpaperSet
                    wallpaperRecomendationsSwitch.isChecked = it.wallpaperRecommendations
                    autoWallpaperSwitch.isChecked = it.autoChangeWallpaper
                    changePeriodRadioGroup.check(it.selectedButton)
                    periodSlider.value = it.sliderValue
                    downloadOverWifiSwitch.isChecked = it.downloadOverWiFi
                    homeCheckbox.isChecked = it.autoHome
                    lockCheckbox.isChecked = it.autoLock

                    autoChangeDependantViewsLayout.alpha =
                        if (settings.autoChangeWallpaper) 1f else .5f

                    with(settings.autoChangeWallpaper) {
                        homeCheckbox.isEnabled = this
                        lockCheckbox.isEnabled = this
                        periodSlider.isEnabled = this
                        daysRadioButton.isEnabled = this
                        hoursRadioButton.isEnabled = this
                        minutesRadioButton.isEnabled = this
                        saveAutomationButton.isEnabled = this
                    }
                }
            }
        }
    }

    private suspend fun checkPermissions(fragment: Fragment) {
        checkStoragePermission(
            fragment = fragment,
            granted = { viewModel.setIsStoragePermissionGranted(true) },
            denied = { viewModel.setIsStoragePermissionGranted(false) },
            deniedPermanently = { viewModel.setIsStoragePermissionGranted(false) }
        )
    }

    private fun setSlider(radioButton: Int) {
        binding.apply {

            var min = 1f
            val max: Float
            var step = 1f

            when (radioButton) {
                R.id.minutes_radio_button -> {
                    min = 5f
                    max = 60f
                    step = 5f
                }
                R.id.hours_radio_button -> max = 24f
                else -> max = 7f
            }

            sliderMinValue.text = "$min"
            sliderMaxValue.text = "$max"

            periodSlider.apply {
                valueFrom = min
                valueTo = max
                stepSize = step
                value =
                    if (currentSliderValue > max || currentSliderValue < min || currentSliderValue % min != 0f)
                        max else currentSliderValue
            }

            periodDurationValue.text = periodSlider.value.toInt().toString()
            viewModel.updateChangePeriodValue(periodSlider.value)
        }
    }

    private fun getTimeRange(value: Float, selectedButton: Int): String {
        val timeRange = when (selectedButton) {
            R.id.minutes_radio_button -> getString(R.string.minute)
            R.id.hours_radio_button -> getString(R.string.hour)
            else -> getString(R.string.day)
        }
        return if (value.toInt() == 1) timeRange else timeRange + "s"
    }

    override fun setupAdapters() {}

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_reset_settings -> {
                viewModel.resetSettings()
                true
            }
            R.id.action_sign_out -> {
                true
            }
            else -> false
        }
    }
}