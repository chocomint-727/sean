package hu.bme.ait.sean.ui.screen.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    viewModel: ReviewViewModel = hiltViewModel(),
    returnToPrevScreen : () -> Unit
) {
    var stars by remember { mutableFloatStateOf(0f) }
    var review by remember {mutableStateOf("")}

    Column (
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Review for ", fontSize = 32.sp, modifier = Modifier.align(Alignment.Start))
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
            valueRange = 1f..5f
        )
        OutlinedTextField(
            value = review,
            onValueChange = {review = it},
            label = {Text("Review")}
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                if (review != ""){
                    viewModel.pushReview(
                        albumID,
                        album,
                        artist,
                        content = review,
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