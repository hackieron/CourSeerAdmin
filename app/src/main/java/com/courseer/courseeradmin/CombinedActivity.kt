package com.courseer.courseeradmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics

class CombinedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combined)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val fragmentList = listOf(AdminFragment(), AdminProgram(), AddTagFragment(), Transaction(), Ranking(), KeywordRanking(), Careers())
        val adapter = CombinedPagerAdapter(this, fragmentList)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Scholarships"
                1 -> tab.text = "Programs"
                2 -> tab.text = "Add Keywords"
                3 -> tab.text = "Records"
                4 -> tab.text = "Rankings"
                5 -> tab.text = "Interests"
                6 -> tab.text = "Careers"
            }
        }.attach()
    }
}
