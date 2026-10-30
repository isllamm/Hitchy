package com.hitchy.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp

// Helpers for laying screens out from the Figma frames (393x852).

/**
 * [template] with each `%n$s` replaced by [links], those parts drawn in [linkStyle].
 * Figma styles "Terms", "Privacy Policy" and "Create an account" inside one text node.
 */
fun withLinks(
    template: String,
    links: List<String>,
    linkStyle: SpanStyle,
): AnnotatedString = buildAnnotatedString {
    val placeholder = Regex("%(\\d+)[$]s")
    var last = 0
    placeholder.findAll(template).forEach { match ->
        append(template.substring(last, match.range.first))
        withStyle(linkStyle) { append(links[match.groupValues[1].toInt() - 1]) }
        last = match.range.last + 1
    }
    append(template.substring(last))
}

/** A Figma offset from the screen top, never under the status bar. */
@Composable
fun figmaTop(offset: Dp): Dp = maxOf(offset, WindowInsets.statusBars.asPaddingValues().calculateTopPadding())

/** A Figma offset from the screen bottom, never under the navigation bar. */
@Composable
fun figmaBottom(offset: Dp): Dp = maxOf(offset, WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
