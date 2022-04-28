package com.sharkaboi.yogapartner.modules.asana_list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.android.material.chip.Chip
import com.sharkaboi.yogapartner.data.models.Asana
import com.sharkaboi.yogapartner.databinding.ItemAsanaBinding

class AsanaListAdapter(
    private val onClick: (Asana) -> Unit
) : RecyclerView.Adapter<AsanaListAdapter.AsanaListViewHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<Asana>() {

        override fun areItemsTheSame(oldItem: Asana, newItem: Asana): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Asana, newItem: Asana): Boolean {
            return oldItem == newItem
        }
    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsanaListViewHolder {
        val binding = ItemAsanaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AsanaListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AsanaListViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }

    fun submitList(list: List<Asana>) {
        listDiffer.submitList(list)
    }

    inner class AsanaListViewHolder(
        private val binding: ItemAsanaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Asana) {
            binding.root.setOnClickListener {
                onClick(item)
            }

            binding.tvName.text = item.name
            binding.ivThumbnail.load(item.asanaThumbnail) {
                scale(Scale.FILL)
                transformations(RoundedCornersTransformation(8f))
            }
            val difficultyChip = Chip(binding.root.context).apply {
                text = item.difficulty.name
            }
            val asanaTypeChip = Chip(binding.root.context).apply {
                text = item.asanaType.name
            }
            binding.cgTags.removeAllViews()
            binding.cgTags.addView(difficultyChip)
            binding.cgTags.addView(asanaTypeChip)
        }
    }
}
