package com.example.electricapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.electricapp.AddNewActivity
import com.example.electricapp.R
import com.example.electricapp.model.ElectricModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ElectricAdapter(private val electricList: ArrayList<ElectricModel>) :
    RecyclerView.Adapter<ElectricAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener
    private lateinit var database: DatabaseReference

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        database = FirebaseDatabase.getInstance().reference
        return ViewHolder(itemView, mListener, database)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCattle = electricList[position]
        holder.bind(currentCattle)
    }

    override fun getItemCount(): Int {
        return electricList.size
    }

    inner class ViewHolder(
        itemView: View,
        private val listener: OnItemClickListener,
        private val database: DatabaseReference
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvId: TextView = itemView.findViewById(R.id.tvId)
        private val img: ImageView = itemView.findViewById(R.id.img)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }
        }

        fun bind(electricModel: ElectricModel) {
            tvName.text = electricModel.fillName
            tvAddress.text = electricModel.fillAddress
            tvId.text = electricModel.fillId

            // Load image using Glide
            Glide.with(itemView.context)
                .load(electricModel.imageUrl)
                .into(img)

            btnEdit.setOnClickListener {
                val electricModel = electricList[adapterPosition]
                val context = itemView.context
                val intent = Intent(context, AddNewActivity::class.java)
                intent.putExtra("electricModel", electricModel)
                context.startActivity(intent)
            }

            btnDelete.setOnClickListener {
                val electricModel = electricList[adapterPosition]
                deleteElectricData(electricModel.fillId)
            }
        }

        private fun deleteElectricData(fillId: String?) {
            // Remove the item from the local adapter list
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                electricList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, electricList.size)
            }

            // Delete the data from Firebase Realtime Database
            val electricRef = database.child("Meteran").child(fillId!!)

            electricRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        itemView.context,
                        "Data deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        itemView.context,
                        "Failed to delete data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
