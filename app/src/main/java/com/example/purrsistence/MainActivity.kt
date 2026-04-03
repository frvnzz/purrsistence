package com.example.purrsistence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.repository.DataRepository
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val dao = db.dao()
        val repo = DataRepository(dao)

        val exampleUser = User(
            username = "testuser",
            balance = 100,
            friends = listOf("alice", "bob"),
            collectedCatsIds = listOf("cat1", "cat2")
        )

        lifecycleScope.launch {
            dao.insertUser(exampleUser)
        }

        setContent {
            PurrsistenceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PurrsistenceTheme {
        Greeting("Android")
    }
}