package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Icon
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.IosArrowLtr
import io.github.composefluent.icons.filled.IosArrowRtl
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale


fun formatLocalized(value: Double, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val df = DecimalFormat("#,##0.00", symbols) // Added .00 for financial consistency
    return df.format(value)
}

fun formatLocalized(value: java.math.BigDecimal, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val df = DecimalFormat("#,##0.0000", symbols)
    return df.format(value)
}

fun formatLocalized(value: Int, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val df = DecimalFormat("#,##0", symbols)
    return df.format(value)
}

/**
 * Locale-aware safe parser for strings like "1,23" or "1.23"
 */
fun parseLocalizedNumber(amount: String, locale: Locale = Locale.getDefault()): Double {
    val nf = NumberFormat.getInstance(locale)
    return try {
        nf.parse(amount.replace(",", ""))?.toDouble() ?: 0.0
    } catch (_: Exception) {
        0.0
    }
}

fun parseLocalizedBigDecimal(amount: String, locale: Locale = Locale.getDefault()): java.math.BigDecimal {
    return try {
        java.math.BigDecimal(amount.replace(",", ""))
    } catch (_: Exception) {
        java.math.BigDecimal.ZERO
    }
}

@Composable
fun InboundArrow(amount: String, isReverted: Boolean = false) {
    val locale = Locale.getDefault()
    val numericValue = parseLocalizedNumber(amount, locale)

    val animatedValue by animateFloatAsState(
        targetValue = numericValue.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    var trigger by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (trigger) 1.1f else 1f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(numericValue) {
        trigger = true
        delay(300)
        trigger = false
    }

    val formattedValue = formatLocalized(animatedValue.toDouble(), locale)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (!isReverted)
            Alignment.CenterEnd
        else
            Alignment.CenterStart

    ) {

        HorizontalDivider(
            thickness = 2.dp,
            color = Color.Black,
            modifier = Modifier.run{
                if (!isReverted)
                    padding(end = 4.dp)
                else
                    padding(start = 4.dp)

            }.fillMaxWidth()
        )

        Icon(
            imageVector = if (!isReverted)
                Icons.Filled.IosArrowRtl
            else
                Icons.Filled.IosArrowLtr,
            contentDescription = "",
            tint = Color.Black,
            modifier = Modifier.scale(scaleAnim)
        )

        Text(
            text = formattedValue,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun OutboundArrow(amount: String, isReverted: Boolean = false) {
    val locale = Locale.getDefault()
    val numericValue = parseLocalizedNumber(amount, locale)

    val animatedValue by animateFloatAsState(
        targetValue = numericValue.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    var trigger by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (trigger) 1.1f else 1f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(numericValue) {
        trigger = true
        delay(300)
        trigger = false
    }

    val formattedValue = formatLocalized(animatedValue.toDouble(), locale)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment =if (!isReverted)
            Alignment.CenterStart
        else
            Alignment.CenterEnd
    ) {
        Icon(
            imageVector = if (!isReverted)
                Icons.Filled.IosArrowLtr
            else
                Icons.Filled.IosArrowRtl
            ,
            contentDescription = "",
            tint = FluentTheme.colors.system.critical,
            modifier = Modifier.scale(scaleAnim)
        )

        HorizontalDivider(
            thickness = 2.dp,
            color = FluentTheme.colors.system.critical,
            modifier = Modifier.run{
                if (!isReverted)
                    padding(start = 4.dp)
                else
                    padding(end = 4.dp)
            }

                .fillMaxWidth()
        )

        Text(
            text = formattedValue,
            color = FluentTheme.colors.system.critical,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.BottomCenter)
        )
    }
}