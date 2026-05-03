package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.*

/**
 * Country data model
 */

data class Country(
    val name: String,
    val code: String,
    val phoneCode: String,
    val flag: DrawableResource
) {
    companion object {

        fun getAllCountries(): List<Country> = listOf(
            Country("Afghanistan", "AF", "+93", Res.drawable.flags_af),
            Country("Albania", "AL", "+355", Res.drawable.flags_al),
            Country("Algeria", "DZ", "+213", Res.drawable.flags_dz),
            Country("Andorra", "AD", "+376", Res.drawable.flags_ad),
            Country("Angola", "AO", "+244", Res.drawable.flags_ao),
            Country("Antigua and Barbuda", "AG", "+1-268", Res.drawable.flags_ag),
            Country("Argentina", "AR", "+54", Res.drawable.flags_ar),
            Country("Armenia", "AM", "+374", Res.drawable.flags_am),
            Country("Australia", "AU", "+61", Res.drawable.flags_au),
            Country("Austria", "AT", "+43", Res.drawable.flags_at),
            Country("Azerbaijan", "AZ", "+994", Res.drawable.flags_az),

            Country("Bahamas", "BS", "+1-242", Res.drawable.flags_bs),
            Country("Bahrain", "BH", "+973", Res.drawable.flags_bh),
            Country("Bangladesh", "BD", "+880", Res.drawable.flags_bd),
            Country("Barbados", "BB", "+1-246", Res.drawable.flags_bb),
            Country("Belarus", "BY", "+375", Res.drawable.flags_by),
            Country("Belgium", "BE", "+32", Res.drawable.flags_be),
            Country("Belize", "BZ", "+501", Res.drawable.flags_bz),
            Country("Benin", "BJ", "+229", Res.drawable.flags_bj),
            Country("Bhutan", "BT", "+975", Res.drawable.flags_bt),
            Country("Bolivia", "BO", "+591", Res.drawable.flags_bo),
            Country("Bosnia and Herzegovina", "BA", "+387", Res.drawable.flags_ba),
            Country("Botswana", "BW", "+267", Res.drawable.flags_bw),
            Country("Brazil", "BR", "+55", Res.drawable.flags_br),
            Country("Brunei", "BN", "+673", Res.drawable.flags_bn),
            Country("Bulgaria", "BG", "+359", Res.drawable.flags_bg),
            Country("Burkina Faso", "BF", "+226", Res.drawable.flags_bf),
            Country("Burundi", "BI", "+257", Res.drawable.flags_bi),

            Country("Cambodia", "KH", "+855", Res.drawable.flags_kh),
            Country("Cameroon", "CM", "+237", Res.drawable.flags_cm),
            Country("Canada", "CA", "+1", Res.drawable.flags_ca),
            Country("Cape Verde", "CV", "+238", Res.drawable.flags_cv),
            Country("Central African Republic", "CF", "+236", Res.drawable.flags_cf),
            Country("Chad", "TD", "+235", Res.drawable.flags_td),
            Country("Chile", "CL", "+56", Res.drawable.flags_cl),
            Country("China", "CN", "+86", Res.drawable.flags_cn),
            Country("Colombia", "CO", "+57", Res.drawable.flags_co),
            Country("Comoros", "KM", "+269", Res.drawable.flags_km),
            Country("Congo", "CG", "+242", Res.drawable.flags_cg),
            Country("Costa Rica", "CR", "+506", Res.drawable.flags_cr),
            Country("Croatia", "HR", "+385", Res.drawable.flags_hr),
            Country("Cuba", "CU", "+53", Res.drawable.flags_cu),
            Country("Cyprus", "CY", "+357", Res.drawable.flags_cy),
            Country("Czech Republic", "CZ", "+420", Res.drawable.flags_cz),

            Country("Democratic Republic of the Congo", "CD", "+243", Res.drawable.flags_cd),
            Country("Denmark", "DK", "+45", Res.drawable.flags_dk),
            Country("Djibouti", "DJ", "+253", Res.drawable.flags_dj),
            Country("Dominica", "DM", "+1-767", Res.drawable.flags_dm),
            Country("Dominican Republic", "DO", "+1-809", Res.drawable.flags_do),

            Country("Ecuador", "EC", "+593", Res.drawable.flags_ec),
            Country("Egypt", "EG", "+20", Res.drawable.flags_eg),
            Country("El Salvador", "SV", "+503", Res.drawable.flags_sv),
            Country("Equatorial Guinea", "GQ", "+240", Res.drawable.flags_gq),
            Country("Eritrea", "ER", "+291", Res.drawable.flags_er),
            Country("Estonia", "EE", "+372", Res.drawable.flags_ee),
            Country("Eswatini", "SZ", "+268", Res.drawable.flags_sz),
            Country("Ethiopia", "ET", "+251", Res.drawable.flags_et),

            Country("Fiji", "FJ", "+679", Res.drawable.flags_fj),
            Country("Finland", "FI", "+358", Res.drawable.flags_fi),
            Country("France", "FR", "+33", Res.drawable.flags_fr),

            Country("Gabon", "GA", "+241", Res.drawable.flags_ga),
            Country("Gambia", "GM", "+220", Res.drawable.flags_gm),
            Country("Georgia", "GE", "+995", Res.drawable.flags_ge),
            Country("Germany", "DE", "+49", Res.drawable.flags_de),
            Country("Ghana", "GH", "+233", Res.drawable.flags_gh),
            Country("Greece", "GR", "+30", Res.drawable.flags_gr),
            Country("Grenada", "GD", "+1-473", Res.drawable.flags_gd),
            Country("Guatemala", "GT", "+502", Res.drawable.flags_gt),
            Country("Guinea", "GN", "+224", Res.drawable.flags_gn),
            Country("Guinea-Bissau", "GW", "+245", Res.drawable.flags_gw),
            Country("Guyana", "GY", "+592", Res.drawable.flags_gy),

            Country("Haiti", "HT", "+509", Res.drawable.flags_ht),
            Country("Honduras", "HN", "+504", Res.drawable.flags_hn),
            Country("Hungary", "HU", "+36", Res.drawable.flags_hu),

            Country("Iceland", "IS", "+354", Res.drawable.flags_is),
            Country("India", "IN", "+91", Res.drawable.flags_in),
            Country("Indonesia", "ID", "+62", Res.drawable.flags_id),
            Country("Iran", "IR", "+98", Res.drawable.flags_ir),
            Country("Iraq", "IQ", "+964", Res.drawable.flags_iq),
            Country("Ireland", "IE", "+353", Res.drawable.flags_ie),
            Country("Israel", "IL", "+972", Res.drawable.flags_il),
            Country("Italy", "IT", "+39", Res.drawable.flags_it),
            Country("Ivory Coast", "CI", "+225", Res.drawable.flags_ci),

            Country("Jamaica", "JM", "+1-876", Res.drawable.flags_jm),
            Country("Japan", "JP", "+81", Res.drawable.flags_jp),
            Country("Jordan", "JO", "+962", Res.drawable.flags_jo),

            Country("Kazakhstan", "KZ", "+7", Res.drawable.flags_kz),
            Country("Kenya", "KE", "+254", Res.drawable.flags_ke),
            Country("Kiribati", "KI", "+686", Res.drawable.flags_ki),
            Country("Kuwait", "KW", "+965", Res.drawable.flags_kw),
            Country("Kyrgyzstan", "KG", "+996", Res.drawable.flags_kg),

            Country("Laos", "LA", "+856", Res.drawable.flags_la),
            Country("Latvia", "LV", "+371", Res.drawable.flags_lv),
            Country("Lebanon", "LB", "+961", Res.drawable.flags_lb),
            Country("Lesotho", "LS", "+266", Res.drawable.flags_ls),
            Country("Liberia", "LR", "+231", Res.drawable.flags_lr),
            Country("Libya", "LY", "+218", Res.drawable.flags_ly),
            Country("Liechtenstein", "LI", "+423", Res.drawable.flags_li),
            Country("Lithuania", "LT", "+370", Res.drawable.flags_lt),
            Country("Luxembourg", "LU", "+352", Res.drawable.flags_lu),

            Country("Madagascar", "MG", "+261", Res.drawable.flags_mg),
            Country("Malawi", "MW", "+265", Res.drawable.flags_mw),
            Country("Malaysia", "MY", "+60", Res.drawable.flags_my),
            Country("Maldives", "MV", "+960", Res.drawable.flags_mv),
            Country("Mali", "ML", "+223", Res.drawable.flags_ml),
            Country("Malta", "MT", "+356", Res.drawable.flags_mt),
            Country("Marshall Islands", "MH", "+692", Res.drawable.flags_mh),
            Country("Mauritania", "MR", "+222", Res.drawable.flags_mr),
            Country("Mauritius", "MU", "+230", Res.drawable.flags_mu),
            Country("Mexico", "MX", "+52", Res.drawable.flags_mx),
            Country("Micronesia", "FM", "+691", Res.drawable.flags_fm),
            Country("Moldova", "MD", "+373", Res.drawable.flags_md),
            Country("Monaco", "MC", "+377", Res.drawable.flags_mc),
            Country("Mongolia", "MN", "+976", Res.drawable.flags_mn),
            Country("Montenegro", "ME", "+382", Res.drawable.flags_me),
            Country("Morocco", "MA", "+212", Res.drawable.flags_ma),
            Country("Mozambique", "MZ", "+258", Res.drawable.flags_mz),
            Country("Myanmar", "MM", "+95", Res.drawable.flags_mm),

            Country("Namibia", "NA", "+264", Res.drawable.flags_na),
            Country("Nauru", "NR", "+674", Res.drawable.flags_nr),
            Country("Nepal", "NP", "+977", Res.drawable.flags_np),
            Country("Netherlands", "NL", "+31", Res.drawable.flags_nl),
            Country("New Zealand", "NZ", "+64", Res.drawable.flags_nz),
            Country("Nicaragua", "NI", "+505", Res.drawable.flags_ni),
            Country("Niger", "NE", "+227", Res.drawable.flags_ne),
            Country("Nigeria", "NG", "+234", Res.drawable.flags_ng),
            Country("North Korea", "KP", "+850", Res.drawable.flags_kp),
            Country("North Macedonia", "MK", "+389", Res.drawable.flags_mk),
            Country("Norway", "NO", "+47", Res.drawable.flags_no),

            Country("Oman", "OM", "+968", Res.drawable.flags_om),

            Country("Pakistan", "PK", "+92", Res.drawable.flags_pk),
            Country("Palau", "PW", "+680", Res.drawable.flags_pw),
            Country("Palestine", "PS", "+970", Res.drawable.flags_ps),
            Country("Panama", "PA", "+507", Res.drawable.flags_pa),
            Country("Papua New Guinea", "PG", "+675", Res.drawable.flags_pg),
            Country("Paraguay", "PY", "+595", Res.drawable.flags_py),
            Country("Peru", "PE", "+51", Res.drawable.flags_pe),
            Country("Philippines", "PH", "+63", Res.drawable.flags_ph),
            Country("Poland", "PL", "+48", Res.drawable.flags_pl),
            Country("Portugal", "PT", "+351", Res.drawable.flags_pt),

            Country("Qatar", "QA", "+974", Res.drawable.flags_qa),

            Country("Romania", "RO", "+40", Res.drawable.flags_ro),
            Country("Russia", "RU", "+7", Res.drawable.flags_ru),
            Country("Rwanda", "RW", "+250", Res.drawable.flags_rw),

            Country("Saint Kitts and Nevis", "KN", "+1-869", Res.drawable.flags_kn),
            Country("Saint Lucia", "LC", "+1-758", Res.drawable.flags_lc),
            Country("Saint Vincent and the Grenadines", "VC", "+1-784", Res.drawable.flags_vc),
            Country("Samoa", "WS", "+685", Res.drawable.flags_ws),
            Country("San Marino", "SM", "+378", Res.drawable.flags_sm),
            Country("São Tomé and Príncipe", "ST", "+239", Res.drawable.flags_st),
            Country("Saudi Arabia", "SA", "+966", Res.drawable.flags_sa),
            Country("Senegal", "SN", "+221", Res.drawable.flags_sn),
            Country("Serbia", "RS", "+381", Res.drawable.flags_rs),
            Country("Seychelles", "SC", "+248", Res.drawable.flags_sc),
            Country("Sierra Leone", "SL", "+232", Res.drawable.flags_sl),
            Country("Singapore", "SG", "+65", Res.drawable.flags_sg),
            Country("Slovakia", "SK", "+421", Res.drawable.flags_sk),
            Country("Slovenia", "SI", "+386", Res.drawable.flags_si),
            Country("Solomon Islands", "SB", "+677", Res.drawable.flags_sb),
            Country("Somalia", "SO", "+252", Res.drawable.flags_so),
            Country("South Africa", "ZA", "+27", Res.drawable.flags_za),
            Country("South Korea", "KR", "+82", Res.drawable.flags_kr),
            Country("South Sudan", "SS", "+211", Res.drawable.flags_ss),
            Country("Spain", "ES", "+34", Res.drawable.flags_es),
            Country("Sri Lanka", "LK", "+94", Res.drawable.flags_lk),
            Country("Sudan", "SD", "+249", Res.drawable.flags_sd),
            Country("Suriname", "SR", "+597", Res.drawable.flags_sr),
            Country("Sweden", "SE", "+46", Res.drawable.flags_se),
            Country("Switzerland", "CH", "+41", Res.drawable.flags_ch),
            Country("Syria", "SY", "+963", Res.drawable.flags_sy),

            Country("Taiwan", "TW", "+886", Res.drawable.flags_tw),
            Country("Tajikistan", "TJ", "+992", Res.drawable.flags_tj),
            Country("Tanzania", "TZ", "+255", Res.drawable.flags_tz),
            Country("Thailand", "TH", "+66", Res.drawable.flags_th),
            Country("Timor-Leste", "TL", "+670", Res.drawable.flags_tl),
            Country("Togo", "TG", "+228", Res.drawable.flags_tg),
            Country("Tonga", "TO", "+676", Res.drawable.flags_to),
            Country("Trinidad and Tobago", "TT", "+1-868", Res.drawable.flags_tt),
            Country("Tunisia", "TN", "+216", Res.drawable.flags_tn),
            Country("Turkey", "TR", "+90", Res.drawable.flags_tr),
            Country("Turkmenistan", "TM", "+993", Res.drawable.flags_tm),
            Country("Tuvalu", "TV", "+688", Res.drawable.flags_tv),

            Country("Uganda", "UG", "+256", Res.drawable.flags_ug),
            Country("Ukraine", "UA", "+380", Res.drawable.flags_ua),
            Country("United Arab Emirates", "AE", "+971", Res.drawable.flags_ae),
            Country("United Kingdom", "GB", "+44", Res.drawable.flags_gb),
            Country("United States", "US", "+1", Res.drawable.flags_us),
            Country("Uruguay", "UY", "+598", Res.drawable.flags_uy),
            Country("Uzbekistan", "UZ", "+998", Res.drawable.flags_uz),

            Country("Vanuatu", "VU", "+678", Res.drawable.flags_vu),
            Country("Venezuela", "VE", "+58", Res.drawable.flags_ve),
            Country("Vietnam", "VN", "+84", Res.drawable.flags_vn),

            Country("Yemen", "YE", "+967", Res.drawable.flags_ye),

            Country("Zambia", "ZM", "+260", Res.drawable.flags_zm),
            Country("Zimbabwe", "ZW", "+263", Res.drawable.flags_zw)
        )


        fun findByCode(code: String): Country? {
            return getAllCountries().firstOrNull {
                it.code.equals(code, ignoreCase = true)
            }
        }
    }
}


/**
 * Individual country list item
 */
@Composable
fun CountryListItem(
    country: Country,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Flag
        Image(
            painter = painterResource(country.flag),
            contentDescription = "${country.name} flag",
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        // Country name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = country.code,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Integrated country picker with text field (like the library you wanted)
 */
@Composable
fun CountryPickerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedCountry: Country?,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Country",
    placeholder: String = ""
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        leadingIcon = {
            Row(
                modifier = Modifier
                    .clickable { showDialog = true }
                    .padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedCountry != null) {
                    Image(
                        painter = painterResource(selectedCountry.flag),
                        contentDescription = "${selectedCountry.name} flag",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
                Text("▼", fontSize = 10.sp)
            }
        }
    )

    if (showDialog) {
        CountryPickerDialog(
            onDismiss = { showDialog = false },
            onCountrySelected = {
                onCountrySelected(it)
                showDialog = false
            }
        )
    }
}
