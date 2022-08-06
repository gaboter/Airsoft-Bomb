package com.example.airsoftbomb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.airsoftbomb.databinding.ActivityMainBinding
import com.example.airsoftbomb.databinding.ActivitySettingsBinding



class settingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bBack()
    }

    fun bBack (){
        binding.bSBack.setOnClickListener {
            finish()
        }
    }

}