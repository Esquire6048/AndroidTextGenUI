package com.example.textgenui.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.textgenui.backend.MsgItem

@Composable
fun MessageList(
    items: List<MsgItem>,
    listState: LazyListState,
    textToSpeech: TextToSpeech?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .fillMaxWidth()
            .background(Color.DarkGray)
    ) {
        items(items) { item ->
            Box( // only for content alignment really
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = MessageAppearance.getMsgAlignmentFromRole(item.role)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MessageAppearance.getMsgBGColor(item.role)),
                    modifier = Modifier.padding(MessageAppearance.getMsgPaddingValuesFromRole(item.role)),
                    shape = RoundedCornerShape(10.dp),
                    onClick = { textToSpeech?.speak(item.content, TextToSpeech.QUEUE_FLUSH, null, "utterance-id-1") }
                ) {
                    Text(
                        text = item.content,
                        color = MessageAppearance.getMsgTextColor(item.role),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}
