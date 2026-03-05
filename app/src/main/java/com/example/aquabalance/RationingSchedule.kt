import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.AquaBalance.R

// Data class to represent a rationing schedule item
data class RationingScheduleItem(
    val day: String,
    val timeRange: String,
    val areas: String
)

class RationingScheduleAdapter : RecyclerView.Adapter<RationingScheduleAdapter.ViewHolder>() {

    private var scheduleItems: List<RationingScheduleItem> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val timeRangeTextView: TextView = view.findViewById(R.id.timeRangeTextView)
        val areasTextView: TextView = view.findViewById(R.id.areasTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rationscheduleitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleItems[position]
        holder.dayTextView.text = item.day
        holder.timeRangeTextView.text = item.timeRange
        holder.areasTextView.text = item.areas
    }

    override fun getItemCount() = scheduleItems.size

    fun updateData(newScheduleItems: List<RationingScheduleItem>) {
        scheduleItems = newScheduleItems
        notifyDataSetChanged()
    }
}