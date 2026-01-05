package com.m7.quranplayer.chapter.ui.toolbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.theme.Green40
import com.m7.quranplayer.core.ui.theme.LightGreenGrey
import com.m7.quranplayer.core.ui.theme.Orange
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.app_will_be_restarted
import quranplayer.composeapp.generated.resources.current_language
import quranplayer.composeapp.generated.resources.language

@Preview(showBackground = true, locale = "ar")
@Composable
private fun LanguageRow_ar() {
    LanguageRow(changeLanguage = {})
}

@Preview(showBackground = true)
@Composable
private fun LanguageRow_en() {
    LanguageRow(changeLanguage = {})
}

@Composable
fun LanguageRow(
    changeLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (menuExpanded, expandMenu) = remember { mutableStateOf(false) }
    val languages = mapOf(
        "ar" to "العربية",
        "en" to "English",
    )

    OptionsRow(modifier = modifier) {
        Icon(
            Icons.Rounded.Language, null,
            modifier = Modifier.padding(12.dp)
        )

        // label
        Text(
            stringResource(Res.string.language) + " :",
            modifier = Modifier.fillMaxWidth(.4f)
        )

        // value
        Text(
            stringResource(Res.string.current_language),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // expand btn
        IconButton(onClick = { expandMenu(!menuExpanded) }) {
            Icon(
                Icons.Rounded.KeyboardArrowDown, null,
                modifier = Modifier.let {
                    if (menuExpanded) it.rotate(180f) else it
                }
            )
        }
    }

    AnimatedVisibility(menuExpanded) {
        Column {
            // hint
            Text(
                stringResource(Res.string.app_will_be_restarted),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Orange,
                modifier = Modifier.fillMaxWidth()
            )

            // language options
            languages
                .forEach { lang ->
                    key(lang.key) {
                        val selected by remember {
                            derivedStateOf { lang.key == Locale.current.language }
                        }

                        val color = if (selected) LightGreenGrey else Green40

                        Text(
                            lang.value,
                            textAlign = TextAlign.Center,
                            color = color,
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 60.dp)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .border(1.dp, color, RoundedCornerShape(30))
                                .padding(5.dp)
                                .let {
                                    if (!selected)
                                        it.clickable {
                                            expandMenu(false)
                                            changeLanguage(lang.key)
                                        }
                                    else it
                                }
                        )
                    }
                }
        }
    }
}