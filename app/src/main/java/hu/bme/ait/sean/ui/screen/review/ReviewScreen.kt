package hu.bme.ait.sean.ui.screen.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ReviewScreen(
    albumID : String,
    album : String,
    artist : String,
    img_url : String,
    viewModel: ReviewViewModel = hiltViewModel(),
    returnToPrevScreen : () -> Unit
) {
    var stars by remember { mutableFloatStateOf(0f) }
    var review by remember {mutableStateOf("")}
    var aiUsed by remember {mutableStateOf(false)}
    val textResult = viewModel.textGenerationResult.collectAsState().value

    Column (
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Review for", fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
        Text("$album - $artist", fontSize = 32.sp, modifier = Modifier.align(Alignment.Start))
        Row (
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            for (i in 1..5){
                Spacer(Modifier.weight(1f))
                Icon(if (stars >= i) Icons.Outlined.Star else Icons.Outlined.Clear, contentDescription = "star")
            }
            Spacer(Modifier.weight(1f))
        }
        Slider(
            value = stars,
            onValueChange = {stars = it},
            valueRange = 1f..5f,
            steps = 9
        )
        OutlinedTextField(
            value = if (!aiUsed) review else textResult?:"",
            onValueChange = {review = it},
            label = {Text("Review")},
            enabled = !aiUsed,
            trailingIcon = {Icon(
                Icons.Filled.ViewInAr,
                "Use AI",
                modifier = Modifier
                    .clickable{
                        aiUsed = true
                        viewModel.generateContent("You are a wise and traveled music critic. Your opinions on music are absolute. You may be a bit curt but never dismissive, but it comes from a desire to hear more music. Your research skills are unmatched, using information from every corner of the internet, like reddit. You love adding details about choice tracks that stand out to you. You are reviewing the album $album by $artist. You have rated it $stars / 5. Write your point of view on the album, keep it short and sweet")
                    }
                )
            },
            modifier = Modifier.heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                val p = if (!aiUsed) review else textResult ?: ""
                if (p != ""){
                    viewModel.pushReview(
                        albumID,
                        album,
                        artist,
                        img_url,
                        content = p,
                        rating = stars
                    )
                }
            }
        ) {
            Text("Submit")
        }

        when (viewModel.writeReviewUIState) {
            is WriteReviewUIState.Idle -> {}
            is WriteReviewUIState.Loading -> {
                CircularProgressIndicator()
            }
            is WriteReviewUIState.Success -> {
                Text("Review Upload Success")
                viewModel.runAfterDelay(1000, returnToPrevScreen)
            }
            is WriteReviewUIState.Error -> {
                Text("Error: ${(viewModel.writeReviewUIState as WriteReviewUIState.Error).msg}")
            }
        }
    }
}