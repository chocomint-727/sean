package hu.bme.ait.sean.ui.screen.review

import android.graphics.RectF
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun ReviewScreen(
    albumID: String,
    album: String,
    artist: String,
    img_url: String,
    viewModel: ReviewViewModel = hiltViewModel(),
    returnToPrevScreen: () -> Unit
) {
    var stars by remember { mutableFloatStateOf(0f) }
    var review by remember { mutableStateOf("") }
    var aiUsed by remember { mutableStateOf(false) }
    val textResult = viewModel.textGenerationResult.collectAsState().value

    Box (
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Review", fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
            HorizontalDivider(modifier = Modifier.padding(8.dp))
            Row {
                AsyncImage(img_url, "", modifier = Modifier.padding(end = 10.dp).weight(3f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(7f)
                ) {
                    Text(
                        "$album - $artist",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    DragRow {
                        stars = 5f * it
                    }
                }
            }

            Spacer(Modifier.padding(8.dp))

            OutlinedTextField(
                value = if (!aiUsed) review else textResult ?: "",
                onValueChange = { review = it },
                label = { Text("Review") },
                enabled = !aiUsed,
                modifier = Modifier
                    .heightIn(400.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
            Row {
                IconButton(
                    {
                        aiUsed = false
                        review = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, "")
                }
                IconButton(
                    {
                        aiUsed = true
                        viewModel.generateContent("You are a wise and traveled music critic. Your opinions on music are absolute. You may be a bit curt but never dismissive, but it comes from a desire to hear more music. Your research skills are unmatched, using information from every corner of the internet, like reddit. You love adding details about choice tracks that stand out to you. You are reviewing the album $album by $artist. You have rated it $stars / 5. Write your point of view on the album, keep it short and sweet")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ViewInAr, "")
                }
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
        Button(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            onClick = {
                val p = if (!aiUsed) review else textResult ?: ""
                if (p != "") {
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
            Text("SUBMIT")
        }
    }
}

@Composable
fun DragRow(
    onChange : (Float) -> Unit
) {
    var w by remember { mutableIntStateOf(1) }
    var pos by remember { mutableFloatStateOf(0f) }

    Box {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            for (i in 1..5) {
                Icon(
                    Icons.Outlined.StarRate,
                    contentDescription = "star",
                    modifier = Modifier
                        .fillMaxWidth().weight(1f).aspectRatio(1f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .onGloballyPositioned {
                    w = it.size.width
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            pos = (offset.x / w).coerceIn(0f, 1f)
                            onChange(pos)
                        },
                        onDrag = { inputChange, offset ->
                            pos = (inputChange.position.x / w).coerceIn(0f, 1f)
                            onChange(pos)
                        }
                    )
                    detectTapGestures(
                        onTap = { offset ->
                            pos = (offset.x / w).coerceIn(0f, 1f)
                            onChange(pos)
                        }
                    )
                }
                .graphicsLayer {
                    clip = true
                    shape = GenericShape { size, _ ->
                        addRect(
                            Rect(
                                0f, 0f,
                                size.width * pos, size.height
                            )
                        )
                    }
                }
        ) {
            for (i in 1..5) {
                Icon(
                    Icons.Filled.StarRate,
                    contentDescription = "star",
                    modifier = Modifier
                        .fillMaxWidth().weight(1f).aspectRatio(1f)
                )
            }
        }
    }
}