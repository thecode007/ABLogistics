package org.safieddine.ablogistics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.ButtonColor
import io.github.composefluent.component.ButtonColorScheme
import io.github.composefluent.scheme.VisualStateScheme

object ABLogisticsThemeColors {

    /* ------------------------------------------------------ */
    /* Base / Background                                      */
    /* ------------------------------------------------------ */

    val PageBackground = BrandingLightGray
    val BarsBackground = BrandingWhite

    // Elevated surfaces (cards, dialogs, inputs if boxed)
    val SurfaceCard = BrandingWhite
    val SurfaceMuted = Color(0xFFEEEEEE)


    /* ------------------------------------------------------ */
    /* Brand                                                  */
    /* ------------------------------------------------------ */

    // Primary black (logo, focus, primary actions)
    val BrandPrimary = BrandingBlack

    // Soft brand (secondary chips, user badge)
    val BrandSoft = BrandingGray

    // Muted brand (charts, tags, subtle indicators)
    val BrandMuted = BrandingAccent

    // Very soft brand tint (row hover, grouping)
    val BrandTintLight = Color(0xFFF0F0F0)


    /* ------------------------------------------------------ */
    /* Text (ON LIGHT BACKGROUND)                              */
    /* ------------------------------------------------------ */

    // Main content text
    val TextOnLightPrimary = BrandingBlack

    // Secondary labels / placeholder
    val TextOnLightSecondary = BrandingGray

    // Disabled text
    val TextDisabled = TertiaryGray

    // Text shown on brand-colored surfaces
    val TextOnBrand = BrandingWhite


    /* ------------------------------------------------------ */
    /* Strokes / Underlines                                   */
    /* ------------------------------------------------------ */

    // Default thin underline / divider
    val StrokeDefault = Color(0xFFE0E0E0)

    // Focus underline (brand black)
    val StrokeFocus = BrandPrimary

    // Disabled underline
    val StrokeDisabled = Color(0xFFF0F0F0)

    // Error underline
    val StrokeError = ErrorRed


    /* ------------------------------------------------------ */
    /* Status / Semantics                                     */
    /* ------------------------------------------------------ */

    val Success = Color(0xFF28A745)
    val Warning = Color(0xFFFFC107)
    val Error = StrokeError


    /* ------------------------------------------------------ */
    /* Title Bar / Window                                     */
    /* ------------------------------------------------------ */

    // Neutral hover for min/max buttons
    val TitleBarHoverNeutral = Color(0x14000000)

    // Close button hover
    val TitleBarHoverClose = StrokeError


    /* ------------------------------------------------------ */
    /* Icons                                                  */
    /* ------------------------------------------------------ */

    val IconPrimary = TextOnLightPrimary
    val IconSecondary = TextOnLightSecondary
    val IconOnBrand = TextOnBrand
}

@Composable
fun abLogisticsAccentButtonColors(): VisualStateScheme<ButtonColor> =
    ButtonColorScheme(
        default = ButtonColor(
            fillColor = ABLogisticsThemeColors.BrandPrimary,
            contentColor = ABLogisticsThemeColors.TextOnBrand,
            borderBrush = FluentTheme.colors.borders.accentControl
        ),
        hovered = ButtonColor(
            fillColor = ABLogisticsThemeColors.BrandSoft,
            contentColor = ABLogisticsThemeColors.TextOnBrand,
            borderBrush = FluentTheme.colors.borders.accentControl
        ),
        pressed = ButtonColor(
            fillColor = ABLogisticsThemeColors.BrandMuted,
            contentColor = ABLogisticsThemeColors.TextOnBrand,
            borderBrush = FluentTheme.colors.borders.accentControl
        ),
        disabled = ButtonColor(
            fillColor = ABLogisticsThemeColors.StrokeDisabled,
            contentColor = ABLogisticsThemeColors.TextDisabled,
            borderBrush = FluentTheme.colors.borders.accentControl
        )
    )
