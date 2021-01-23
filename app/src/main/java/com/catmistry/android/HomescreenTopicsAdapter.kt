package com.catmistry.android

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.homescreen_list_item.view.*


class HomescreenTopicsAdapter(
        private val data: ArrayList<HomeTopics?>,
        private val clickListener: RecyclerViewClickListener,
        private val context: Context,
        private val showArrow: Boolean = true
): RecyclerView.Adapter<HomescreenTopicsAdapter.ViewHolder>() {

    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T { // Adapter for onClickListener
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.homescreen_list_item, parent, false)

        // Return the view holder
        return ViewHolder(view)
            .listen { pos, _ ->
                clickListener.itemClicked(pos)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resources: Resources = context.resources
        val resourceId = resources.getIdentifier(
            data[position]?.icon,
            "drawable", context.packageName
        )

        try {
            holder.iconImg.setImageResource(resourceId) // Might throw error if resID is a number
        } catch (e: Exception) {}
        holder.title.text = data[position]?.title

        // Check if the right arrow should be shown
        holder.rightIcon.visibility = if (showArrow) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return data.size // Gets length of dataset
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: MaterialTextView = itemView.listTitle
        val iconImg: SquareImageView = itemView.listIcon
        val rightIcon: SquareImageView = itemView.rightArrow
    }
}