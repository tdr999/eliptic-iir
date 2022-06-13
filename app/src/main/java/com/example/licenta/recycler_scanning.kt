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



class CustomAdapter(private val dataSet: List<ScanResult>,
                    private val listener : OnItemClickListener
) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener{

        val nume_device: TextView
        val imagine_device : ImageView
        val signal_strength : TextView
        val statut_device : TextView



        init {
            // Define click listener for the ViewHolder's View.
            nume_device = view.findViewById(R.id.nume_device)
            signal_strength = view.findViewById(R.id.signal_strength)
            imagine_device = view.findViewById(R.id.imagine_viewholder)
            statut_device = view.findViewById(R.id.textView_statut)
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
            .inflate(R.layout.dev_card_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.nume_device.text = dataSet[position]
//        viewHolder.signal_strength.text = dataSet[position]
        viewHolder.nume_device.text = dataSet[position].device.name
        viewHolder.signal_strength.text = dataSet[position].rssi.toString() + "db"
        if (dataSet[position].device.name == "Mi Band 3") {
            viewHolder.imagine_device.setImageResource(R.drawable.miband3)
        }

        else if (dataSet[position].device.name == "B01H_M4") {
            viewHolder.imagine_device.setImageResource(R.drawable.m4)
        }
        else{
            viewHolder.imagine_device.setImageResource(R.drawable.jumper)
        }

        if (globalDatabase.db.checkIfUserHasDevice(dataSet[position].device.address) == false){
            viewHolder.statut_device.text = "Unknown Device"
            viewHolder.statut_device.setTextColor(Color.RED)
        }
        else{
            viewHolder.statut_device.text = "Known device"
            viewHolder.statut_device.setTextColor(Color.GREEN)
        }



    }



    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


}

