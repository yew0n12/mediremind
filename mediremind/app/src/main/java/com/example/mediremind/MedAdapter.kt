package com.example.mediremind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediremind.data.Medication
import com.example.mediremind.databinding.ItemMedBinding

// ListAdapter를 상속받아 DiffUtil로 효율적인 갱신 지원
class MedAdapter : ListAdapter<Medication, MedAdapter.VH>(MedDiffCallback()) {

    // ViewHolder: item_med.xml 로 각 아이템 뷰를 바인딩
    inner class VH(private val binding: ItemMedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(m: Medication) {
            binding.tvName.text = m.name
            binding.tvTime.text = m.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

// DiffUtil 콜백: 아이템 간 변경점만 업데이트
class MedDiffCallback : DiffUtil.ItemCallback<Medication>() {
    override fun areItemsTheSame(old: Medication, new: Medication) =
        old.id == new.id

    override fun areContentsTheSame(old: Medication, new: Medication) =
        old == new
}
