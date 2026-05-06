package org.safieddine.ablogistics.ui.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Button
import io.github.composefluent.component.ButtonColor
import io.github.composefluent.component.ButtonDefaults.buttonColors
import io.github.composefluent.component.SecureTextField
import io.github.composefluent.component.TextField
import io.github.composefluent.component.TextFieldColor
import io.github.composefluent.component.TextFieldColorScheme
import io.github.composefluent.component.TextFieldDefaults
import io.github.composefluent.scheme.VisualStateScheme

@Composable
fun ABLogisticsTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    maxLines: Int = Int.MAX_VALUE,
    header: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    isClearable: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = FluentTheme.shapes.control
) {
    val colors = FluentTheme.colors

    val abColors = TextFieldDefaults.defaultTextFieldColors(
        focused = TextFieldColor(
            fillColor = colors.control.inputActive,
            contentColor = colors.text.text.primary,
            placeholderColor = colors.text.text.secondary,

            bottomLineFillColor = colors.controlStrong.default,

            borderBrush = SolidColor(colors.stroke.control.onAccentDefault),
            cursorBrush = SolidColor( colors.stroke.control.default)
        )
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        header = header,
        leadingIcon = leadingIcon,
        trailing = trailing,
        placeholder = placeholder,
        isClearable = isClearable,
        interactionSource = interactionSource,
        colors = abColors,
        shape = shape
    )
}


@Composable
fun ABLogisticsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    maxLines: Int = Int.MAX_VALUE,
    header: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    isClearable: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = FluentTheme.shapes.control,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var internalValue by remember {
        mutableStateOf(TextFieldValue(value))
    }



    LaunchedEffect(value) {
        if (value != internalValue.text) {
            internalValue = internalValue.copy(text = value)
        }
    }

    val colors = TextFieldColorScheme(
        default = TextFieldColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
            bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeDefault,
            borderBrush = SolidColor(Color.Transparent), // underline only pattern
            cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
        ),
        focused = TextFieldColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
            bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeFocus,
            borderBrush = SolidColor(Color.Transparent),
            cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
        ),
        hovered = TextFieldColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
            bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeDefault,
            borderBrush = SolidColor(Color.Transparent),
            cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
        ),
        pressed = TextFieldColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
            bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeFocus,
            borderBrush = SolidColor(Color.Transparent),
            cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
        ),
        disabled = TextFieldColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextDisabled,
            placeholderColor = ABLogisticsThemeColors.TextDisabled,
            bottomLineFillColor = ABLogisticsThemeColors.StrokeDisabled,
            borderBrush = SolidColor(Color.Transparent),
            cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeDisabled)
        ),
    )

    Column(modifier = modifier) {
        TextField(
            value = internalValue,
            onValueChange = {
                internalValue = it
                onValueChange(it.text)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            maxLines = maxLines,
            header = header,
            leadingIcon = leadingIcon,
            trailing = trailing,
            placeholder = placeholder,
            isClearable = isClearable,
            interactionSource = interactionSource,
            colors = colors,
            shape = shape
        )
        
        if (isError && errorMessage != null) {
            androidx.compose.material.Text(
                text = errorMessage,
                color = ABLogisticsThemeColors.Error,
                style = androidx.compose.material.MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun abLogisticsButtonColors(): VisualStateScheme<ButtonColor> =
    buttonColors(
        default = ButtonColor(
            fillColor = FluentTheme.colors.control.default,
            contentColor = ABLogisticsThemeColors.TextOnBrand,
            borderBrush = FluentTheme.colors.borders.control
        ),
        hovered = ButtonColor(
            fillColor = FluentTheme.colors.control.secondary,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            borderBrush = FluentTheme.colors.borders.control
        ),
        pressed = ButtonColor(
            fillColor = FluentTheme.colors.control.tertiary,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            borderBrush = FluentTheme.colors.borders.control
        ),
        disabled = ButtonColor(
            fillColor = FluentTheme.colors.control.disabled,
            contentColor = ABLogisticsThemeColors.TextDisabled,
            borderBrush = FluentTheme.colors.borders.control
        )
    )

@Composable
fun abLogisticsSubtleButtonColors(): VisualStateScheme<ButtonColor> =
     buttonColors(
        default = ButtonColor(
            fillColor = Color.Transparent,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            borderBrush = SolidColor(Color.Transparent)
        ),
        hovered = ButtonColor(
            fillColor = FluentTheme.colors.subtleFill.secondary,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            borderBrush = SolidColor(Color.Transparent)
        ),
        pressed = ButtonColor(
            fillColor = FluentTheme.colors.subtleFill.secondary,
            contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
            borderBrush = SolidColor(Color.Transparent)
        ),
        disabled = ButtonColor(
            fillColor = Color.Transparent,
            contentColor = ABLogisticsThemeColors.TextDisabled,
            borderBrush = SolidColor(Color.Transparent)
        )
    )

@Composable
fun ABLogisticsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    buttonColors: VisualStateScheme<ButtonColor> = abLogisticsButtonColors(),
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    iconOnly: Boolean = false,
    contentArrangement: Arrangement.Horizontal =
        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        interaction = interaction,
        disabled = disabled,
        buttonColors = buttonColors,
        onClick = onClick,
        iconOnly = iconOnly,
        contentArrangement = contentArrangement,
        content = content
    )
}

@Composable
fun ABLogisticsSubtleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    buttonColors: VisualStateScheme<ButtonColor> = abLogisticsSubtleButtonColors(),
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    iconOnly: Boolean = false,
    contentArrangement: Arrangement.Horizontal =
        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        interaction = interaction,
        disabled = disabled,
        buttonColors = buttonColors,
        onClick = onClick,
        iconOnly = iconOnly,
        contentArrangement = contentArrangement,
        content = content
    )
}

@Composable
fun ABLogisticsAccentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    buttonColors: VisualStateScheme<ButtonColor> = abLogisticsAccentButtonColors(),
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    iconOnly: Boolean = false,
    contentArrangement: Arrangement.Horizontal =
        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        interaction = interaction,
        disabled = disabled,
        buttonColors = buttonColors,
        onClick = onClick,
        iconOnly = iconOnly,
        contentArrangement = contentArrangement,
        content = content
    )
}

@Composable
fun ABLogisticsSecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = FluentTheme.shapes.control
) {
    Column(modifier = modifier) {
        SecureTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            header = header,
            colors = TextFieldColorScheme(
                default = TextFieldColor(
                    fillColor = FluentTheme.colors.control.default,
                    contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
                    placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
                    bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeDefault,
                    borderBrush = SolidColor(Color.Transparent), // underline only pattern
                    cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
                ),
                focused = TextFieldColor(
                    fillColor = FluentTheme.colors.control.default,
                    contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
                    placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
                    bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeFocus,
                    borderBrush = SolidColor(Color.Transparent),
                    cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
                ),
                hovered = TextFieldColor(
                    fillColor = FluentTheme.colors.control.default,
                    contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
                    placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
                    bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeDefault,
                    borderBrush = SolidColor(Color.Transparent),
                    cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
                ),
                pressed = TextFieldColor(
                    fillColor = FluentTheme.colors.control.default,
                    contentColor = ABLogisticsThemeColors.TextOnLightPrimary,
                    placeholderColor = ABLogisticsThemeColors.TextOnLightSecondary,
                    bottomLineFillColor = if (isError) ABLogisticsThemeColors.Error else ABLogisticsThemeColors.StrokeFocus,
                    borderBrush = SolidColor(Color.Transparent),
                    cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeFocus)
                ),
                disabled = TextFieldColor(
                    fillColor = FluentTheme.colors.control.default,
                    contentColor = ABLogisticsThemeColors.TextDisabled,
                    placeholderColor = ABLogisticsThemeColors.TextDisabled,
                    bottomLineFillColor = ABLogisticsThemeColors.StrokeDisabled,
                    borderBrush = SolidColor(Color.Transparent),
                    cursorBrush = SolidColor(ABLogisticsThemeColors.StrokeDisabled)
                ),
            ),
            leadingIcon = leadingIcon,
            interactionSource = interactionSource,
            shape = shape
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ABLogisticsThemeColors.Error,
                style = androidx.compose.material.MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
