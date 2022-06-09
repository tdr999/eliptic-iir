import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.licenta.R
import com.example.licenta.database
import com.example.licenta.globalContext
import com.example.licenta.globalDatabase
import java.util.*


data class alerta(val alert_id : Int?, val user_id : Int?, val descriere : String?, val calendar : String?){
}




class AlertAdapter(private val dataSet: List<alerta>,
                    private val listener : OnItemClickListener
) :
    RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener{

        val alert_date_time: TextView = view.findViewById(R.id.alert_date_time)
        val alert_description : TextView = view.findViewById(R.id.alert_description)




        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener(this)
        }


        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position : Int)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.alerts_card_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.nume_device.text = dataSet[position]
//        viewHolder.signal_strength.text = dataSet[position]
        viewHolder.alert_date_time.text = dataSet[position].calendar
        viewHolder.alert_description.text = dataSet[position].descriere

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


}

