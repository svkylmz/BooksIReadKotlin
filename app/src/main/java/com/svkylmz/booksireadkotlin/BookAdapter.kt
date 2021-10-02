package com.svkylmz.booksireadkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svkylmz.booksireadkotlin.databinding.RecyclerRowBinding

class BookAdapter(val bookList: ArrayList<Book>) : RecyclerView.Adapter<BookAdapter.BookHolder>() {
    class BookHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookHolder(binding)
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.binding.recyclerViewText.text = bookList.get(position).name
        //route to details activity when book name is clicked on feed
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("info", "recorded")
            intent.putExtra("id", bookList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}