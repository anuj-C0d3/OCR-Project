package com.example.ocr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ocr.databinding.ActivitySavedDetailsListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedDetailsLIst : AppCompatActivity() {
    private lateinit var binding:ActivitySavedDetailsListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedDetailsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
       val rv =  binding.rv
        rv.layoutManager = LinearLayoutManager(this)
        val datalist = ArrayList<SaveDetailInfo>()
        val adapterrv = Adapterrv(this,datalist)
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("user").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val value = snap.getValue(SaveDetailInfo::class.java)
                    value?.let {
                        datalist.add(it)
                    }
                    adapterrv.notifyDataSetChanged()
                    rv.adapter = adapterrv
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}