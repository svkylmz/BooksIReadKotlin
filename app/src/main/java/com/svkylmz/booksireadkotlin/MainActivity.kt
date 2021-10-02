package com.svkylmz.booksireadkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.svkylmz.booksireadkotlin.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    //view binding start
    private lateinit var binding: ActivityMainBinding

    private lateinit var bookList: ArrayList<Book>
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bookList = ArrayList<Book>()

        bookAdapter = BookAdapter(bookList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = bookAdapter

        //Getting Datas to Main Activity
        try {
            val database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM books", null)
            val idIndex = cursor.getColumnIndex("id")
            val bookNameIndex = cursor.getColumnIndex("bookname")

            while (cursor.moveToNext()) {
                val name = cursor.getString(bookNameIndex)
                val id = cursor.getInt(idIndex)

                val book = Book(name, id)
                bookList.add(book)
            }
            bookAdapter.notifyDataSetChanged()
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Menu Bar Operations
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menu binding operations
        //with inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.book_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //operations when menu is clicked
        if(item.itemId == R.id.addBook) {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}