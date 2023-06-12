package com.example.electricapp

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.electricapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //enable back arrow in action bar
        supportActionBar!!.apply {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
        //change action bar color
        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.navy_200
                )
            )
        )
        //change text in action bar
        supportActionBar!!.title = intent.getStringExtra("name")

        setValuestoViews()
    }

    private fun setValuestoViews() {
        binding.tvName.text = intent.getStringExtra("name")
        binding.id.text = intent.getStringExtra("id")
        binding.address.text = intent.getStringExtra("address")
        binding.date.text = intent.getStringExtra("date")
        binding.power.text = intent.getStringExtra("power")
        binding.firstMeter.text = intent.getStringExtra("firstMeter")
        binding.lastMeter.text = intent.getStringExtra("lastMeter")
        val imageUrl = intent.getStringExtra("image")
        Glide.with(this)
            .load(imageUrl)
            .into(binding.image)
    }
}