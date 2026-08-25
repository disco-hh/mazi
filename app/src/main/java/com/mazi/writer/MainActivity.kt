package com.mazi.writer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.mazi.writer.data.AppDatabase
import com.mazi.writer.data.WriterRepository
import com.mazi.writer.ui.MaziApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mazi.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        val viewModel = ViewModelProvider(this, WriterViewModelFactory(WriterRepository(database)))[WriterViewModel::class.java]
        setContent { MaziApp(viewModel) }
    }
}
