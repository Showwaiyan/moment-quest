package com.example.momentquest.ui.adapter

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.momentquest.databinding.ItemChallengeBinding
import com.example.momentquest.databinding.ItemMomentBinding
import com.example.momentquest.model.TimelineItem
import com.example.momentquest.ui.activity.ChallengeDetailActivity
import com.example.momentquest.util.GeocoderHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimelineAdapter(
    private val onMarkDoneClick: (String) -> Unit,
    private val onMomentClick: (com.example.momentquest.model.Moment) -> Unit
) : ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_CHALLENGE = 0
        private const val VIEW_TYPE_MOMENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.ChallengeItem -> VIEW_TYPE_CHALLENGE
            is TimelineItem.MomentItem -> VIEW_TYPE_MOMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CHALLENGE -> {
                val binding = ItemChallengeBinding.inflate(inflater, parent, false)
                ChallengeViewHolder(binding)
            }
            VIEW_TYPE_MOMENT -> {
                val binding = ItemMomentBinding.inflate(inflater, parent, false)
                MomentViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimelineItem.ChallengeItem -> (holder as ChallengeViewHolder).bind(item.challenge)
            is TimelineItem.MomentItem -> (holder as MomentViewHolder).bind(item.moment)
        }
    }

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challenge: com.example.momentquest.model.Challenge) {
            binding.tvTitle.text = challenge.title
            binding.tvCategory.text = challenge.category.uppercase(Locale.getDefault())
            
            if (challenge.deadline != null) {
                binding.deadlineLayout.visibility = View.VISIBLE
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvDeadline.text = "Due " + sdf.format(Date(challenge.deadline))
            } else {
                binding.deadlineLayout.visibility = View.GONE
            }

            if (challenge.status == "COMPLETED") {
                binding.ivTimelineIcon.setImageResource(com.example.momentquest.R.drawable.ic_check)
                binding.btnMarkDone.visibility = View.GONE
                binding.tvStatus.text = "DONE"
                binding.tvStatus.setBackgroundResource(com.example.momentquest.R.drawable.card_background)
                binding.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        binding.root.context.getColor(com.example.momentquest.R.color.tertiary_container)
                    )
                )
                binding.tvStatus.setTextColor(binding.root.context.getColor(com.example.momentquest.R.color.on_tertiary))
                binding.completedMemoryLayout.visibility = View.GONE
            } else {
                binding.ivTimelineIcon.setImageResource(com.example.momentquest.R.drawable.ic_flag)
                binding.btnMarkDone.visibility = View.VISIBLE
                binding.btnMarkDone.setOnClickListener {
                    onMarkDoneClick(challenge.id)
                }
                binding.tvStatus.text = "PENDING"
                binding.tvStatus.setBackgroundResource(com.example.momentquest.R.drawable.card_background)
                binding.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        binding.root.context.getColor(com.example.momentquest.R.color.surface_container_highest)
                    )
                )
                binding.tvStatus.setTextColor(binding.root.context.getColor(com.example.momentquest.R.color.on_surface_variant))
                binding.completedMemoryLayout.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ChallengeDetailActivity::class.java).apply {
                    putExtra("challenge_id", challenge.id)
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    inner class MomentViewHolder(private val binding: ItemMomentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moment: com.example.momentquest.model.Moment) {
            binding.tvTitle.text = moment.title
            binding.tvDescription.text = moment.description
            
            binding.tvTime.text = DateUtils.getRelativeTimeSpanString(
                moment.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            if (!moment.photoUrl.isNullOrEmpty()) {
                binding.imageContainer.visibility = View.VISIBLE
                binding.tvMoodOverlay.text = moment.mood
                Glide.with(binding.ivPhoto.context)
                    .load(moment.photoUrl)
                    .into(binding.ivPhoto)
            } else {
                binding.imageContainer.visibility = View.GONE
            }

            binding.cardMoment.setOnClickListener {
                onMomentClick(moment)
            }

            if (moment.latitude != null && moment.longitude != null) {
                binding.locationLayout.visibility = View.VISIBLE
                binding.tvLocation.text = String.format(
                    Locale.US,
                    "%.4f° N, %.4f° E",
                    moment.latitude,
                    moment.longitude
                )

                binding.locationLayout.setOnClickListener {
                    val lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
                    if (lifecycleOwner != null) {
                        GeocoderHelper.showAddressDialog(
                            binding.root.context,
                            moment.latitude,
                            moment.longitude,
                            lifecycleOwner.lifecycleScope
                        )
                    }
                }
            } else {
                binding.locationLayout.visibility = View.GONE
            }
        }
    }
}

class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
    override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
        return oldItem.itemId == newItem.itemId
    }

    override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
        return oldItem == newItem
    }
}
