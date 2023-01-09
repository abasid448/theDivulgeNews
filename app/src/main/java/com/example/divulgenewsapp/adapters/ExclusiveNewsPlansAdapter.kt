package com.example.divulgenewsapp.adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.ItemSubscriptionPlanCardBinding
import com.example.divulgenewsapp.models.ExclusiveNewsPlan

class ExclusiveNewsPlansAdapter(
    private val context: Context, private val list: List<ExclusiveNewsPlan>
) : RecyclerView.Adapter<ExclusiveNewsPlansAdapter.PlanViewHolder>() {

    var selectedItemPosition = -1

    inner class PlanViewHolder(val binding: ItemSubscriptionPlanCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(
            ItemSubscriptionPlanCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = list[position]
        holder.binding.apply {
            tvPlanHead.text = "${plan.title} (Rs.${plan.price})"
            tvPlanSubHead.text = plan.subTitle

            root.setOnClickListener {
                onItemClickListener?.let { it(plan) }
                setSingleItemSelection(position)
            }
        }

        val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_circle_check_solid)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, Color.RED)

        if (selectedItemPosition == position) {
            //holder.binding.rlOuterLayout.setBackgroundColor(context.getColor(R.color.white))
            holder.binding.ivCardCheck.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_check_solid))

            DrawableCompat.setTint(
                DrawableCompat.wrap(holder.binding.ivCardCheck.drawable), ContextCompat.getColor(context, R.color.app_green)
            )

        } else {
            //holder.binding.rlOuterLayout.setBackgroundColor(context.getColor(R.color.white))
            holder.binding.ivCardCheck.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_regular))

            DrawableCompat.setTint(
                DrawableCompat.wrap(holder.binding.ivCardCheck.drawable), ContextCompat.getColor(context, R.color.light_grey)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onItemClickListener: ((ExclusiveNewsPlan) -> Unit)? = null

    fun setOnItemClickListener(listener: (ExclusiveNewsPlan) -> Unit) {
        onItemClickListener = listener
    }

    private fun setSingleItemSelection(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        notifyItemChanged(selectedItemPosition)
        selectedItemPosition = position
        notifyItemChanged(selectedItemPosition)
    }
}