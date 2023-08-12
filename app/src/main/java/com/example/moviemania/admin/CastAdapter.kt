package com.example.moviemania.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.CastFragment

class CastAdapter(private val context: Context, private val castList: List<CastFragment.Cast>) : BaseAdapter() {

    override fun getCount(): Int = castList.size

    override fun getItem(position: Int): Any = castList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cast = getItem(position) as CastFragment.Cast
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_cast, parent, false)
        val castImageView = itemView.findViewById<ImageView>(R.id.castImageView)
        val castNameTextView = itemView.findViewById<TextView>(R.id.castNameTextView)
        castNameTextView.text = cast.name
        val imageViewLayoutParams = castImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics

        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.3).toInt()
        imageViewLayoutParams.height = (screenHeight*0.20).toInt()
        val uri = cast.imageUri
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error).placeholder(R.drawable.ic_image_placeholder).into(castImageView)
        return itemView
    }
}