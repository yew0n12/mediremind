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
            // 리사이클링 시 이전 리스너 제거 후 상태 반영

            //binding.checkBoxTaken.setOnCheckedChangeListener(null)
            binding.checkBoxTaken.isChecked = m.taken  // 체크박스 초기 상태 설정
            // 체크 상태 변경 리스너 등록
            binding.checkBoxTaken.setOnCheckedChangeListener { _, isChecked ->
                onTakenChecked?.invoke(m, isChecked) // 프래그먼트에 이벤트 전달

            }
        }
    }

    // 체크박스 상태 변경 시 호출될 람다 함수
    var onTakenChecked: ((Medication, Boolean) -> Unit)? = null

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


