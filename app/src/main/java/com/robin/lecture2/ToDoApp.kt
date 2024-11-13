package com.robin.lecture2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class TodoItem(
    val id: Int,
    var title: String,
    var subtitle: String,
    val timestamp:String,
    val check: MutableState<Boolean> = mutableStateOf(false)
)


@Composable
fun TodoApp() {
    val navController = rememberNavController()
    val todoList = remember { mutableStateListOf<TodoItem>() }

    NavHost(navController = navController, startDestination = "todoList") {
        composable("todoList") { TodoListScreen(navController, todoList) }
        composable("addTodo") { AddTodoScreen(navController, todoList) }
        composable("editTodo/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
            val todoItem = todoList.find { it.id == itemId }
            todoItem?.let { EditTodoScreen(navController, it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(navController: NavController, todoList: MutableList<TodoItem>) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notes List") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addTodo") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Todo")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(todoList) { item ->
                ListItem(
                    leadingContent = {
                        Checkbox(
                            checked = item.check.value,
                            onCheckedChange = {
                                item.check.value = !item.check.value
                        })},
                    headlineContent = {Text(item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis) },
                    supportingContent = {
                        Text(
                            item.subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Added on: ${item.timestamp}",
                            style = MaterialTheme.typography.bodySmall
                        )
            },

                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = { navController.navigate("editTodo/${item.id}") }
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Todo")
                            }
                            IconButton(
                                onClick = { todoList.remove(item) }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Todo")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(navController: NavController, todoList: MutableList<TodoItem>) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val isFormValid = title.length >= 3 && subtitle.length >= 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Todo") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                todoList.add(
                                    TodoItem(
                                        id = todoList.size,
                                        title = title,
                                        subtitle = subtitle,
                                        timestamp = timestamp
                                    )
                                )
                                navController.popBackStack()
                            }
                            else{
                                isSaving = true
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Todo") },
                isError = isSaving && title.length < 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Details") },
                isError = isSaving && subtitle.length < 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isFormValid) {
                        todoList.add(
                            TodoItem(
                                id = todoList.size,
                                title = title,
                                subtitle = subtitle,
                                timestamp = timestamp
                            )
                        )
                        navController.popBackStack()
                    }
                },
                enabled = isFormValid
            )
            {
                Text("Add Todo")
            }
            if (isSaving && !isFormValid) {
                Text(
                    text = "Title and/or Subtitle must be at least 3 characters.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditTodoScreen(navController: NavController, todoItem: TodoItem) {
        var title by remember { mutableStateOf(todoItem.title) }
        var subtitle by remember { mutableStateOf(todoItem.subtitle) }
        var isSaving by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Todo") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
        //    horizontalAlignment = Alignment.CenterHorizontally,
          //  verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Todo") },
                    isError = isSaving && title.length <3
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Details") },
                    isError = isSaving && subtitle.length < 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (title.isNotBlank() && subtitle.isNotBlank()) {
                        todoItem.title = title
                        todoItem.subtitle = subtitle
                        navController.popBackStack()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Ändringar sparade")

                        }
                    }
                    else{
                        isSaving = true
                    }
                }) {
                    Text("Save ")
                }
                if (isSaving && (title.length <3 || subtitle.length < 3)){
                    Text(
                        text = "Title and/or Subtitle must be at least 3 characters.",
                        color = MaterialTheme.colorScheme.error,
                        style  = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
