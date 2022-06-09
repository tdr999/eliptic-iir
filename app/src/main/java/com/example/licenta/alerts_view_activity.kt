package com.example.licenta

import AlertAdapter
import CustomAdapter
import alerta
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast

class alerts_view_activity : AppCompatActivity(), AlertAdapter.OnItemClickListener {



    private val lista_alerte = mutableListOf<alerta>()
    var adaptorAlerte = AlertAdapter(lista_alerte, this)




    override fun onCreate(savedInstanceState: Bundle?) {


        var cursor = globalDatabase.db.fetchAlerts()
        cursor?.moveToFirst()
        while (cursor?.moveToNext() != false){
            var index_alert_id = cursor?.getColumnIndex("alert_id")
            var index_user_id = cursor?.getColumnIndex("user_id")
            var index_date_time = cursor?.getColumnIndex("timp")
            var index_descriere = cursor?.getColumnIndex("descriere")
            var temp = alerta(
                alert_id = cursor?.getInt(index_alert_id!!),
                user_id = cursor?.getInt(index_user_id!!),
                descriere = cursor?.getString(index_descriere!!),
                calendar = cursor?.getString(index_date_time!!)
            )

            lista_alerte.add(temp)

        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_view_layout)

        var rec_view = findViewById<RecyclerView>(R.id.recycler_view_alert)
        rec_view.layoutManager = LinearLayoutManager(this)
        rec_view.adapter = adaptorAlerte



    }



    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()


    }



    override fun onStop() {
        super.onStop()

    }


    override fun onItemClick(position: Int) { //functia asta e rulata cand dai click pe un item din lista
        Toast.makeText(this@alerts_view_activity, "Item $position click", Toast.LENGTH_SHORT).show()
        val clickedItem = lista_alerte[position]
        adaptorAlerte.notifyItemChanged(position)
        //https://www.youtube.com/watch?v=wKFJsrdiGS8/

    }

}