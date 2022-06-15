import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.licenta.R
import com.example.licenta.globalDatabase
import com.example.licenta.globalSortedAlerts
import com.example.licenta.setAlarm

data class alerta(
    val alert_id: Int?,
    val user_id: Int?,
    val descriere: String?,
    val calendar: String?
)

class AlertAdapter(
    private val dataSet: MutableList<alerta>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        val alert_date_time: TextView = view.findViewById(R.id.alert_date_time)
        val alert_description: TextView = view.findViewById(R.id.alert_description)
        var delete_button: Button = view.findViewById<Button>(R.id.delete_alert_button)

        init {
            // Define click listener for the ViewHolder's View.
//            view.setOnClickListener(this)
            delete_button.setOnClickListener(this)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
            removeItem(position)
            globalSortedAlerts.updateList(dataSet)
            globalSortedAlerts.getNextAlert()

            var next_alert_idx = globalSortedAlerts.next_alert_index //adaugat acuma
            if (next_alert_idx != null) {

                var next_time = globalSortedAlerts.alerte_sortate?.get(next_alert_idx!!)?.calendar
                setAlarm(next_time.toString())
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
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

    fun removeItem(position: Int) {

        globalDatabase.db.removeAlert(dataSet[position].alert_id)
        dataSet.removeAt(position)

        notifyItemRemoved(position)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

