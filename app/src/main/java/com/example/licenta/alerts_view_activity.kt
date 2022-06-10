package com.example.licenta

import AlertAdapter
import alerta
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.Toast

class alerts_view_activity : AppCompatActivity(), AlertAdapter.OnItemClickListener {

    private val lista_alerte = mutableListOf<alerta>()
    var adaptorAlerte = AlertAdapter(lista_alerte, this)






    override fun onCreate(savedInstanceState: Bundle?) {







        var cursor = globalDatabase.db.fetchAlerts()
        cursor?.moveToFirst()
        var result = true
        while (result != false){
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

            result = cursor?.moveToNext()!!

        }
//        lista_alerte.sortBy { it.calendar?.chunked(2)?.get(0) } //dupa ora

//        lista_alerte.sortWith(compareBy{it.calendar})

        lista_alerte.sortBy {"1" + it.calendar?.chunked(2)?.get(0).toString()  } //cand ia primele 2, la alea care
        //au o singura cifra le ia pe urmatoarele si merge

        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_view_layout)

        var rec_view = findViewById<RecyclerView>(R.id.recycler_view_alert)
        rec_view.layoutManager = LinearLayoutManager(this)
        rec_view.adapter = adaptorAlerte



    }



    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        lista_alerte.clear()
        var cursor = globalDatabase.db.fetchAlerts()
        var result = true
        cursor?.moveToFirst()
        while (result != false) {
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
            result = cursor?.moveToNext()!!
        }

        lista_alerte.sortBy { (it.calendar?.split(":")?.get(1)?.let { it1 ->
            it.calendar?.split(":")?.get(0)?.toInt()?.times(100).plus(
                it1.toInt()) //puteam sa fi facut o functie
        })
        } //sunt un zeu printre muritori

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

    fun newAlert(view : View){


        intent = Intent(
            this,
            newalertactivty::class.java
        )//nu inteleg exact ce face scope res operatorul aici dar whatever
        startActivity(intent)

    }


}