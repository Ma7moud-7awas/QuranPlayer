package com.m7.mediaplayer.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.m7.mediaplayer.ui.theme.Green
import com.m7.mediaplayer.ui.theme.Orange
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun AnimatedChapterNumber(
    isSelected: Boolean = false,
    chapterNum: Int,
    modifier: Modifier = Modifier
) {
    val playingTransition = updateTransition(isSelected, "Select State")

    val selectColor by playingTransition.animateColor(label = "Color") { isPlaying ->
        if (isPlaying) Green else Orange
    }

    val selectProgress by playingTransition.animateFloat(label = "Progress") { isPlaying ->
        if (isPlaying) 1f else 0f
    }

    val morphed by remember {
        derivedStateOf { Morph(MaterialShapes.Sunny, MaterialShapes.Arrow) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(56.dp)
            .padding(8.dp)
    ) {
        // background clipping, applied in a child to rotate the play shape
        Spacer(
            Modifier
                .fillMaxSize()
                .rotate(90f)
                .clip(MorphPolygonShape(morphed, selectProgress))
                .background(selectColor)
        )

        Text(text = "$chapterNum", color = Color.White)
    }
}