package com.example.textgenui.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object MessageAppearance {
    fun getMsgBGColor(role: String): Color {
        return when (role) {
            "assistant" -> Color(0xFF0D47A1) // dark blue
            "system" -> Color.Black
            "user" -> Color(0xFF006400) // dark green
            else -> error("unknown role $role")
        }
    }

    fun getMsgTextColor(role: String): Color {
        return when (role) {
            "assistant" -> Color.White
            "system" -> Color.Red
            "user" -> Color.White
            else -> error("unknown role $role")
        }
    }

    fun getMsgPaddingValuesFromRole(role: String): PaddingValues {
        return when (role) {
            "assistant" -> PaddingValues(start = 0.dp, end = 30.dp, top = 0.dp, bottom = 0.dp)
            "system" -> PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
            "user" -> PaddingValues(start = 30.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
            else -> error("unknown role $role")
        }
    }

    fun getMsgAlignmentFromRole(role: String): Alignment {
        return when (role) {
            "assistant" -> Alignment.CenterStart
            "system" -> Alignment.Center
            "user" -> Alignment.CenterEnd
            else -> error("unknown role $role")
        }
    }
}