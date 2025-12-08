
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hu.bme.ait.sean.R


val Montserrat = FontFamily(
    Font(R.font.montserrat_black, FontWeight.W600),
    Font(R.font.montserrat_black, FontWeight.W600),
    Font(R.font.montserrat_medium, FontWeight.W500),
    Font(R.font.montserrat_medium, FontWeight.W400),
            Font(R.font.montserrat_medium, FontWeight.W300),
Font(R.font.montserrat_thin, FontWeight.W200),
    Font(R.font.montserrat_thin, FontWeight.W100)
)
val SeanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Montserrat,
        fontSize = 30.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontSize = 14.sp
    )
    // override any other styles you want
)