package com.example.image_picker.adapter

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.image_picker.R
import com.example.image_picker.model.Image
import com.example.image_picker.page.OnSelectedChange

 class GridAdapter(private val context:Context, var onChange: OnSelectedChange, private val temp:List<Image>, private val itemWidth:Int): RecyclerView.Adapter<ViewHolder>() {
    private var selected:MutableList<Image> = temp.toMutableList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var type = ImageView.ScaleType.CENTER_CROP
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.width = itemWidth
        params.height = itemWidth
        params.gravity = Gravity.CENTER
        holder.imageView.scaleType = type
        holder.container.layoutParams = params
        holder.checkBox.setImageResource(R.drawable.check)
        if(selected.contains(temp[position])){
            holder.checkBox.setImageResource(R.drawable.checked)
        }
        holder.checkBox.setOnClickListener{
            if(selected.contains(temp[position])){
                selected.remove(temp[position])
                holder.checkBox.setImageResource(R.drawable.check)
            }else{
                selected.add(temp[position])
                holder.checkBox.setImageResource(R.drawable.checked)
            }
            onChange.onSelect(position)
        }
        Glide.with(context).load(Uri.parse(temp[position].uri)).apply(
            RequestOptions.fitCenterTransform())
            .into(holder.imageView);
    }

    override fun getItemCount(): Int {
        return temp.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.selecte_image_view, parent, false)
        return ViewHolder(v)
    }
}

 class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.target_image)
    var checkBox : ImageView = itemView.findViewById(R.id.checkbox)
    var container : ConstraintLayout = itemView.findViewById(R.id.image_container)
}

