package com.example.to_do_list.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.to_do_list.ListViewModel
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    vm: ListViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = remember(vm, taskId) { vm.getTaskById(taskId) }

    val focusManager = LocalFocusManager.current

    if (task == null) {
        // Eğer görev bulunmazsa basit bir bilgi göster ve geri dönme imkanı ver
        Scaffold { inner ->
            Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Görev bulunamadı")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("Geri") }
                }
            }
        }
        return
    }

    var title by rememberSaveable(taskId) { mutableStateOf(task.title) }
    val snackbarHostState = remember { SnackbarHostState() } // snackbar için state
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // host eklendi
        topBar = {
            TopAppBar(
                title = { Text("Görevi Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            // Sol: Vazgeç, Sağ: Kaydet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    // Değişiklikleri kaydetmeden geri dön
                    onBack()
                }) {
                    Text("Vazgeç")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = {
                    // kaydet
                    val newTitle = title.trim()
                    if (newTitle.isNotEmpty()) {
                        vm.updateTaskTitle(taskId, newTitle)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Değişiklikler kaydedildi"
                            )
                        }
                    }
                    focusManager.clearFocus()
                    onBack()
                }) {
                    Text("Kaydet")
                }
            }
        }
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                /*keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // hızlı kaydetme opsiyonu (isteğe bağlı)
                    val newTitle = title.trim()
                    if (newTitle.isNotEmpty()) vm.updateTaskTitle(taskId, newTitle)
                    focusManager.clearFocus()
                }),*/
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Task ID: $taskId", style = MaterialTheme.typography.bodySmall)
            // istersen burada ek alanlar (isDone toggle, notlar vs.) ekleyebilirsin
        }
    }
}
