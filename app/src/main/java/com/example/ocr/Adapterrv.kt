package com.example.ocr

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.ocr.databinding.SavedItemViewBinding
import com.google.firebase.database.FirebaseDatabase

class Adapterrv(val context:Context, val list:ArrayList<SaveDetailInfo>):RecyclerView.Adapter<Adapterrv.ViewHolder>() {
    inner class ViewHolder(val binding:SavedItemViewBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapterrv.ViewHolder {
        val binding = SavedItemViewBinding.inflate(LayoutInflater.from(context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: Adapterrv.ViewHolder, position: Int) {
        val ref = FirebaseDatabase.getInstance().reference
      holder.binding.name.text= list[position].name

      holder.binding.email.text= list[position].email
        if(list[position].company==""){
            holder.binding.companyname.setText("Company name")
        } else {
            holder.binding.companyname.text= list[position].company
        }

      holder.binding.phone.text= list[position].phone
        holder.binding.web.text = list[position].web
      holder.binding.delete.setOnClickListener {
          val alertDialog = AlertDialog.Builder(context)
              .setTitle("Delete")
              .setIcon(R.drawable.deleteicon)
              .setMessage("Are you sure to delete")
              .setPositiveButton("Yes",DialogInterface.OnClickListener { dialog, which ->
                  ref.child("user").child(list[position].notkey).removeValue()
                      .addOnSuccessListener {
                          Toast.makeText(context, "Deleted!!", Toast.LENGTH_SHORT).show()
                          list.removeAt(position)
                          notifyItemRemoved(position)
                      }
              })
              .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                  dialog.dismiss()
              })
              .create()
              .show()

      }
        holder.binding.edit.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java)
            intent.putExtra("name",list[position].name)
            intent.putExtra("phone",list[position].phone)
            intent.putExtra("company",list[position].company)
            intent.putExtra("web",list[position].web)
            intent.putExtra("email",list[position].email)
            intent.putExtra("notkey",list[position].notkey)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}