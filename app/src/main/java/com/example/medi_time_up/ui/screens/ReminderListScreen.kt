package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medi_time_up.data.AppDatabase
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.ui.components.buttons.PrimaryButton
import com.example.medi_time_up.ui.theme.MediDark
import com.example.medi_time_up.ui.theme.MediLight
import com.example.medi_time_up.ui.theme.MediVeryDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onAddMedicationClick: () -> Unit
) {
    val context = LocalContext .current

    val dao = AppDatabase.getDatabase(context).medicamentoDao()
    val viewModel: ReminderListViewModel = viewModel(
        factory = ReminderListViewModelFactory(dao)
    )

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Recordatorios") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicationClick) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Medicamento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (state.medicamentos.isEmpty()) {
                EmptyState(onAddMedicationClick)
            } else {
                MedicationList(
                    medicamentos = state.medicamentos,
                    onDelete = viewModel::deleteMedication
                )
            }
        }
    }
}

@Composable
fun MedicationList(
    medicamentos: List<Medicamento>,
    onDelete: (Medicamento) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(medicamentos, key = { it.id }) { medicamento ->
            MedicationItem(medicamento = medicamento, onDelete = onDelete)
        }
    }
}

/**
 * Item con estilo 3D: base desplazada (oscura) + superficie con gradiente, borde claro y contenido.
 */
@Composable
fun MedicationItem(
    medicamento: Medicamento,
    onDelete: (Medicamento) -> Unit
) {
    val corner = 12.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        // Base (sombra / pedestal)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 6.dp)
                .drawBehind {
                    // base sólida oscura
                    drawRect(MediVeryDark)
                }
                .padding(0.dp)
        )

        // Superficie (arriba) con gradiente y borde claro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.TopStart),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // manejamos la "elevación" visual manualmente
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MediDark, MediDark.copy(alpha = 0.95f))
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicamento.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Dosis: ${medicamento.cantidad}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Frecuencia: ${medicamento.frecuencia}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Próxima Hora: ${medicamento.hora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }

                // Borde iluminado (simula canto) y botón eliminar
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    IconButton(onClick = { onDelete(medicamento) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Borde ligero encima de la superficie para simular canto iluminado
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val stroke = 4f
                    drawIntoCanvas { canvas ->
                        withTransform({
                            // no transform
                        }) {
                            val paint = androidx.compose.ui.graphics.Paint().apply {
                                color = MediLight.copy(alpha = 0.65f)
                                this.asFrameworkPaint().strokeWidth = stroke
                                this.asFrameworkPaint().style = android.graphics.Paint.Style.STROKE
                                this.asFrameworkPaint().isAntiAlias = true
                            }
                            val r = size
                            canvas.drawRoundRect(
                                left = 0f,
                                top = 0f,
                                right = r.width,
                                bottom = r.height - 6.dp.toPx(), // no sobrepasar la base
                                radiusX = 12.dp.toPx(),
                                radiusY = 12.dp.toPx(),
                                paint = paint
                            )
                        }
                    }
                }
        )
    }
}

@Composable
fun EmptyState(onAddMedicationClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Aún no tienes recordatorios!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Presiona el botón '+' para agregar tu primer medicamento.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón 3D principal para agregar (coherente con diseño)
        PrimaryButton(
            text = "Agregar Recordatorio Ahora",
            onClick = onAddMedicationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            enabled = true,
            height = 52.dp,
            cornerRadius = 10.dp
        )
    }
}

/** Estado UI */
data class ReminderListUiState(
    val medicamentos: List<Medicamento> = emptyList(),
    val isLoading: Boolean = false
)

/** ViewModel usando los nombres de tu DAO: obtenerTodos() y eliminar(medicamento) */
class ReminderListViewModel(
    private val dao: com.example.medi_time_up.data.dao.MedicamentoDao
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ReminderListUiState())
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.obtenerTodos().collect { list: List<Medicamento> ->
                _uiState.update { it.copy(medicamentos = list) }
            }
        }
    }

    fun deleteMedication(medicamento: Medicamento) {
        viewModelScope.launch {
            dao.eliminar(medicamento)
        }
    }
}

/** Factory */
class ReminderListViewModelFactory(
    private val dao: com.example.medi_time_up.data.dao.MedicamentoDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}