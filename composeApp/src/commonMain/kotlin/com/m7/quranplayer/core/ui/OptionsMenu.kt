package com.m7.quranplayer.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OptionsStack(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    animateCollapse: Boolean = true,
    rows: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(expanded) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            if (expanded || animateCollapse)
                rows()
        }
    }
}

@Composable
fun OptionsRow(
    modifier: Modifier = Modifier,
    options: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options()
    }
}