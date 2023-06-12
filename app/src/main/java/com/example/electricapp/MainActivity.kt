package com.example.electricapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electricapp.adapter.ElectricAdapter
import com.example.electricapp.databinding.ActivityMainBinding
import com.example.electricapp.model.ElectricModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var electricList: ArrayList<ElectricModel>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_logo)
        }

        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.navy_200
                )
            )
        )

        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNewActivity::class.java)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        electricList = arrayListOf()

        // Fetch cattle data
        getElectricData()

        binding.signOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Bye👋", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getElectricData() {
        binding.imgEmpty.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        dbRef = FirebaseDatabase.getInstance().getReference("Meteran")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                electricList.clear()
                if (snapshot.exists()) {
                    for (cattleSnap in snapshot.children) {
                        val cattleData = cattleSnap.getValue(ElectricModel::class.java)
                        electricList.add(cattleData!!)
                    }

                    if (electricList.isEmpty()) {
                        binding.imgEmpty.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        // Initialize adapter
                        val mAdapter = ElectricAdapter(electricList)
                        binding.recyclerView.adapter = mAdapter

                        // Add click listener on item
                        mAdapter.setOnItemClickListener(object : ElectricAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                                intent.putExtra("name", electricList[position].fillName)
                                intent.putExtra("id", electricList[position].fillId)
                                intent.putExtra("image", electricList[position].imageUrl)
                                intent.putExtra("address", electricList[position].fillAddress)
                                intent.putExtra("date", electricList[position].fillDate)
                                intent.putExtra("power", electricList[position].fillPower)
                                intent.putExtra("firstMeter", electricList[position].fillFirstMeter)
                                intent.putExtra("lastMeter", electricList[position].fillLastMeter)
                                startActivity(intent)
                            }
                        })

                        binding.recyclerView.visibility = View.VISIBLE
                        binding.imgEmpty.visibility = View.GONE
                    }
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.imgEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

}