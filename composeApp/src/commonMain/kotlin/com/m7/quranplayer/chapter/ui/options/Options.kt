package com.m7.quranplayer.chapter.ui.options

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OptionsStack(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    options: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(expanded) {
        if (expanded)
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                options()
            }
    }
}

@Composable
fun OptionsRow(
    modifier: Modifier = Modifier,
    options: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options()
    }
}