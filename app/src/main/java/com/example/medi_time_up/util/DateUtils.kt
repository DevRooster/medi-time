package com.example.medi_time_up.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

val hhmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")