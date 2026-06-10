package com.example.momentquest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.momentquest.databinding.ActivityMainBinding
import com.example.momentquest.ui.fragment.AddChallengeFragment
import com.example.momentquest.ui.fragment.AddMomentFragment
import com.example.momentquest.ui.fragment.StatsFragment
import com.example.momentquest.ui.fragment.TimelineFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupFabMenu()
        setupToolbarActions()

        // Set default fragment if this is first launch
        if (savedInstanceState == null) {
            replaceFragment(TimelineFragment())
            updateBottomNavStyles(R.id.btnTabTimeline)
        }

        // Listen for back stack changes to show/hide bottom nav and FAB
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            updateUiForFragment(currentFragment)
        }
    }

    private fun setupNavigation() {
        binding.btnTabTimeline.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment !is TimelineFragment) {
                replaceFragment(TimelineFragment())
            }
            updateBottomNavStyles(R.id.btnTabTimeline)
        }

        binding.btnTabStats.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment !is StatsFragment) {
                replaceFragment(StatsFragment())
            }
            updateBottomNavStyles(R.id.btnTabStats)
        }
    }

    private fun updateBottomNavStyles(activeTabId: Int) {
        if (activeTabId == R.id.btnTabTimeline) {
            binding.btnTabTimeline.setBackgroundResource(R.drawable.bottom_nav_active_background)
            binding.ivTabTimeline.imageTintList = ColorStateList.valueOf(getColor(R.color.on_primary))
            binding.tvTabTimeline.setTextColor(getColor(R.color.on_primary))

            binding.btnTabStats.setBackgroundColor(Color.TRANSPARENT)
            binding.ivTabStats.imageTintList = ColorStateList.valueOf(getColor(R.color.on_surface_variant))
            binding.tvTabStats.setTextColor(getColor(R.color.on_surface_variant))
        } else {
            binding.btnTabStats.setBackgroundResource(R.drawable.bottom_nav_active_background)
            binding.ivTabStats.imageTintList = ColorStateList.valueOf(getColor(R.color.on_primary))
            binding.tvTabStats.setTextColor(getColor(R.color.on_primary))

            binding.btnTabTimeline.setBackgroundColor(Color.TRANSPARENT)
            binding.ivTabTimeline.imageTintList = ColorStateList.valueOf(getColor(R.color.on_surface_variant))
            binding.tvTabTimeline.setTextColor(getColor(R.color.on_surface_variant))
        }
    }

    private fun setupFabMenu() {
        binding.fabAdd.setOnClickListener {
            toggleSpeedDial()
        }

        binding.btnActionChallenge.setOnClickListener {
            toggleSpeedDial()
            replaceFragment(AddChallengeFragment(), addToBackStack = true)
        }

        binding.btnActionMoment.setOnClickListener {
            toggleSpeedDial()
            replaceFragment(AddMomentFragment(), addToBackStack = true)
        }
    }

    private fun setupToolbarActions() {
        binding.btnSearch.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment is TimelineFragment) {
                showSearchDialog(currentFragment)
            } else {
                replaceFragment(TimelineFragment())
                updateBottomNavStyles(R.id.btnTabTimeline)
                Toast.makeText(this, "Switched to Timeline. Tap search again to filter!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMonitoring.setOnClickListener {
            // Show a mock system monitor connection state
            Toast.makeText(this, "Firestore Sync: Connected (Monitoring OK)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSearchDialog(timelineFragment: TimelineFragment) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Timeline")

        val input = EditText(this)
        input.hint = "Search by title, category, mood..."
        input.setTextColor(getColor(R.color.on_surface))
        input.setPadding(48, 32, 48, 32)
        builder.setView(input)

        builder.setPositiveButton("Search") { _, _ ->
            val query = input.text.toString().trim()
            timelineFragment.filterTimeline(query)
        }
        builder.setNegativeButton("Clear") { _, _ ->
            timelineFragment.filterTimeline("")
        }
        builder.show()
    }

    private fun toggleSpeedDial() {
        if (binding.speedDialContainer.visibility == View.VISIBLE) {
            binding.speedDialContainer.visibility = View.GONE
            binding.fabAdd.setImageResource(R.drawable.ic_add)
        } else {
            binding.speedDialContainer.visibility = View.VISIBLE
            binding.fabAdd.setImageResource(R.drawable.ic_close)
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    private fun updateUiForFragment(fragment: Fragment?) {
        when (fragment) {
            is TimelineFragment -> {
                binding.customBottomNavigation.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.setImageResource(R.drawable.ic_add)
                binding.speedDialContainer.visibility = View.GONE
                updateBottomNavStyles(R.id.btnTabTimeline)
            }
            is StatsFragment -> {
                binding.customBottomNavigation.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.GONE
                binding.speedDialContainer.visibility = View.GONE
                updateBottomNavStyles(R.id.btnTabStats)
            }
            is AddChallengeFragment, is AddMomentFragment -> {
                binding.customBottomNavigation.visibility = View.GONE
                binding.fabAdd.visibility = View.GONE
                binding.speedDialContainer.visibility = View.GONE
            }
        }
    }
}