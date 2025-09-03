package com.example.to_do_list

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.to_do_list.ui.EditTaskScreen
import com.example.to_do_list.ui.theme.To_Do_ListTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: ListViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        enableEdgeToEdge()

        setContent {
            To_Do_ListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    // RunningService kontrolü için tasks burada izleniyor
                    val tasks by vm.tasks.collectAsState()

                    LaunchedEffect(tasks) {
                        val incomplete = vm.getFirstIncompleteTask()
                        if (incomplete != null) {
                            if(vm.lastNotifiedId != incomplete.id){
                                vm.changeLastNotified(incomplete.id)
                                Intent(context, RunningService::class.java).also {
                                    it.action = RunningService.Actions.START.name
                                    it.putExtra("TASK_TEXT", incomplete.title)
                                    ContextCompat.startForegroundService(context, it)
                                }
                            }
                        } else {
                            Intent(context, RunningService::class.java).also {
                                it.action = RunningService.Actions.STOP.name
                                context.startService(it)
                            }
                        }
                    }

                    // NavController ve NavHost
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            // List screen composable - dosya içinde ikinci fonksiyon
                            ListScreen(vm = vm, onItemClick = { id ->
                                navController.navigate("edit/$id")
                            })
                        }

                        composable(
                            route = "edit/{taskId}",
                            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("taskId") ?: return@composable
                            // EditTaskScreen daha önce verdiğimiz dosya olmalı:
                            EditTaskScreen(
                                taskId = id,
                                vm = vm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ListScreen: MainActivity içinde ikinci bir fonksiyon olarak tanımlandı.
 * - vm: ListViewModel (task ekleme / toggle / remove)
 * - onItemClick: (id) -> Unit — tıklandığında edit ekranına gitmesi için navController tarafından verilir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    vm: ListViewModel,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val task by vm.task.collectAsState()
    val tasks by vm.tasks.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() } // snackbar için state
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Scaffold'a host'u bağladık
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = !showAdd },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Yeni görev ekle")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "To-Do List",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 96.dp) // FAB için boşluk
                ) {
                    items(items = tasks, key = { it.id }) { currentTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = currentTask.isDone,
                                    onCheckedChange = { vm.toggleDone(currentTask) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentTask.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (!currentTask.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onItemClick(currentTask.id) } // tıklayınca düzenleye git
                                )
                            }

                            IconButton(
                                onClick = {
                                    vm.removeTask(currentTask)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Görev kaldırıldı"
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Görevi Sil"
                                )
                            }
                        }
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }

            // FAB'in hemen üstünde belirecek küçük ekleme kutusu
            AnimatedVisibility(
                visible = showAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 92.dp) // FAB yüksekliğine göre ayarla
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .widthIn(min = 280.dp, max = 420.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = task,
                            onValueChange = { text -> vm.updateTaskText(text) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Görev ekle...", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (task.isNotBlank()) {
                                        vm.addTask()
                                        //vm.updateTaskText("") // temizle
                                        showAdd = false
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (task.isNotBlank()) {
                                    vm.addTask()
                                    //vm.updateTaskText("") // temizle
                                    showAdd = false
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Ekle")
                        }
                    }
                }
            }
        }
    }
}
