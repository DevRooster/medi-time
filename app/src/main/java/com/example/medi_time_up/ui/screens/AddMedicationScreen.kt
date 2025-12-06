package com.example.medi_time_up.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.medi_time_up.ui.components.form.CustomTextField
import com.example.medi_time_up.ui.theme.MediLight
import com.example.medi_time_up.ui.theme.MediVeryDark
import java.util.Calendar

@Composable
fun AddMedicationScreen(
    navController: NavController,
    onSaveMedication: (
        name: String,
        dosage: String,
        time: String,
        days: List<String>,
        remindBefore: Boolean
    ) -> Unit
) {

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var remindBefore by remember { mutableStateOf(false) }

    // Selección de días (Calendario simple semanal)
    val daysOfWeek = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )
    val selectedDays = remember { mutableStateListOf<String>() }

    fun toggleDay(day: String) {
        if (day in selectedDays) selectedDays.remove(day)
        else selectedDays.add(day)
    }

    val calendar = Calendar.getInstance()

    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTime = String.format("%02d:%02d", hour, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Agregar Medicamento",
            style = MaterialTheme.typography.headlineMedium,
            color = MediVeryDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Nombre
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre del medicamento"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Dosificación
        CustomTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = "Dosis",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selección de hora
        Text("Hora de toma", color = MediVeryDark)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(3.dp, MediVeryDark, RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { timePicker.show() }
                .padding(14.dp)
        ) {
            Text(
                text = if (selectedTime.isEmpty()) "Seleccionar hora" else selectedTime,
                color = if (selectedTime.isEmpty()) Color.Gray else MediVeryDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calendario Semanal
        Text("¿Qué días tomará el medicamento?", color = MediVeryDark)

        Spacer(modifier = Modifier.height(12.dp))

        daysOfWeek.chunked(3).forEach { rowDays ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowDays.forEach { day ->
                    DaySelector(
                        day = day,
                        isSelected = day in selectedDays,
                        onClick = { toggleDay(day) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Checkbox Recordatorio 5 min antes
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = remindBefore, onCheckedChange = { remindBefore = it })
            Text("Recordarme 5 minutos antes")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Guardar
        Button(
            onClick = {
                if (name.isBlank() || dosage.isBlank() || selectedTime.isBlank() || selectedDays.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Completa todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onSaveMedication(name, dosage, selectedTime, selectedDays.toList(), remindBefore)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar medicamento")
        }
    }
}

@Composable
fun DaySelector(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp)
            .background(
                if (isSelected) MediLight else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp)
            )
            .border(
                2.dp,
                if (isSelected) MediVeryDark else Color.Gray,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day, color = MediVeryDark)
    }
}