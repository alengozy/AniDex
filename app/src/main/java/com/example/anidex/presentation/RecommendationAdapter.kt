import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.databinding.CardLayoutBinding
import com.example.anidex.model.AnimeDetail
import com.example.anidex.presentation.CardViewHolder
import com.example.anidex.presentation.onClick

class RecommendationAdapter(
    val itemClickListener: (View, Int, Int) -> Unit,
    private val dataSource: ArrayList<AnimeDetail>?,
    private val context: Context
) :
    RecyclerView.Adapter<CardViewHolder>() {
    override fun getItemCount(): Int {
        return dataSource?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val viewCVH = CardViewHolder.createHolder(parent)
        viewCVH.onClick(itemClickListener)
        return viewCVH
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val data = dataSource?.get(position)
        holder.bind(data)
    }

}
