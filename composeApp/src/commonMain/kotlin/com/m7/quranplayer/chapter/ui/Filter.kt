package com.m7.quranplayer.chapter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m7.quranplayer.chapter.domain.model.Part
import org.jetbrains.compose.resources.stringResource
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.filter_by_part

@Composable
fun Filter(
    expand: () -> Boolean,
    selectedPart: () -> Part?,
    onPartSelected: (Part) -> Unit,
) {
    AnimatedVisibility(expand()) {
        Column {
            // header
            Text(
                stringResource(Res.string.filter_by_part) + ":",
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyRow(contentPadding = PaddingValues(20.dp)) {
                items(
                    items = Part.entries.toList(),
                    key = { it.name }
                ) { part ->
                    PartItem(
                        part = { part },
                        isSelected = { selectedPart() == part },
                        modifier = Modifier.clickable { onPartSelected(part) }
                    )
                }
            }
        }

    }
}