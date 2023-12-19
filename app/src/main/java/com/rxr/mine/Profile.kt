package com.rxr.mine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.tabLayout
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.tab_background.view.*
import java.util.*

class Profile : AppCompatActivity() {
    private lateinit var view: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_background, null);
        //if(internet is on)
        //else
        //..default font
        setCustomView(0, 1)
        setTextAndImageWithAnimation("MENU", R.drawable.ic_categories);
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    1 -> {
                        setCustomView(1, 0)
                        setTextAndImageWithAnimation("HOME", R.drawable.ic_home);
                        //change to the fragment which you want to display
                        val intent = Intent(this@Profile, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        setCustomView(0, 1)
                        setTextAndImageWithAnimation("MENU", R.drawable.ic_categories);
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        //set profile image
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userPic = sharedPreferences.getString("userPic","https://firebasestorage.googleapis.com/v0/b/earn-money-964b0.appspot.com/o/icons8-user-100.png?alt=media&token=d298488f-7190-49f5-88a8-12ea00b6383d")
        Glide.with(this).load(userPic).circleCrop().into(profile_img)
        linearRowarrowright.setOnClickListener {
            Toast.makeText(this,"Comming Soon",Toast.LENGTH_SHORT).show()
        }
        linearRowarrowrightOne.setOnClickListener {
            Toast.makeText(this,"Comming Soon",Toast.LENGTH_SHORT).show()
        }
        linearRowarrowrightTwo.setOnClickListener {
            Toast.makeText(this,"Comming Soon",Toast.LENGTH_SHORT).show()
        }
        linearRowarrowrightThree.setOnClickListener {
            Toast.makeText(this,"Comming Soon",Toast.LENGTH_SHORT).show()
        }
        linearRowarrowrightFour.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rxrmine.blogspot.com/2023/11/blog-post.html"))
            startActivity(intent)
        }
        viewRectangleThirteen.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/RXRNetwork"))
            startActivity(intent)
        }
        viewRectangleFifteen.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/XRXMine"))
            startActivity(intent)
        }
        viewRectangleFourteen.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@RXNetwork?si=uyU6krcsu3ZcURRq"))
            startActivity(intent)
        }
    }
    private fun setCustomView(selectedtab: Int, non1: Int) {
        Objects.requireNonNull(tabLayout.getTabAt(selectedtab))?.setCustomView(view)
        Objects.requireNonNull(tabLayout.getTabAt(non1))?.setCustomView(null)
    }

    private fun setTextAndImageWithAnimation(text: String, images: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, android.R.anim.slide_in_left)
        animation.setInterpolator(AccelerateDecelerateInterpolator())
        view.tv1.setText(text)
        view.iv1.setImageResource(images)
        view.tv1.startAnimation(animation)
        view.iv1.startAnimation(animation)
    }
}