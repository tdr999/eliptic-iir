import android.bluetooth.le.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.licenta.R



class CustomAdapter(private val dataSet: List<ScanResult>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nume_device: TextView
        val imagine_device : ImageView
        val signal_strength : TextView


        init {
            // Define click listener for the ViewHolder's View.
            nume_device = view.findViewById(R.id.nume_device)
            signal_strength = view.findViewById(R.id.signal_strength)
            imagine_device = view.findViewById(R.id.imagine_viewholder)
        }
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
        viewHolder.imagine_device.setImageResource(R.drawable.jumper)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

