package hu.bme.ait.sean.ui.screen.album

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.ui.theme.Primary

@Composable
fun DetailScreen(
    album: String,
    artist: String,
    modifier: Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {

    val postListState = viewModel.loadReviews().collectAsState(
        initial = PostDetailsUIState.Loading
    )

    LaunchedEffect(Unit) {
        viewModel.getDetails(album, artist)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(10.dp)
    ) {
        when (viewModel.albumDetailsUIState) {
            is AlbumDetailsUIState.Loading -> {
                CircularProgressIndicator()
                Text("idk loading or smth")
            }

            is AlbumDetailsUIState.Error -> {
                Text("Error Loading Album details")
            }

            is AlbumDetailsUIState.Success -> {
                AsyncImage(
                    model = (viewModel.albumDetailsUIState as AlbumDetailsUIState.Success).res.album?.image?.last()?.text ?: "",
                    modifier = Modifier.size(200.dp, 200.dp),
                    contentDescription = "Album Cover"
                )
                Text("basic info about the album")
                Row() {
                    Button(
                        { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text("More info")
                    }
                    Button(
                        { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text("Open in music app")
                    }
                    Button(
                        { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text("Write / Edit Review")
                    }
                }
            }
        }

        HorizontalDivider()

//        val userpost = viewModel.findUserPost()
//        if (userpost != null) {
//            ReviewCard(userpost)
//            HorizontalDivider()
//        }

        when (postListState.value) {
            is PostDetailsUIState.Loading -> {
                CircularProgressIndicator()
            }

            is PostDetailsUIState.Error -> {
                Text("Error Loading Posts: \n${(postListState.value as PostDetailsUIState.Error).msg}")
                Log.d("ERROR_TEXT", (postListState.value as PostDetailsUIState.Error).msg)
            }

            is PostDetailsUIState.Success -> {
                LazyColumn {
                    items(items = (postListState.value as PostDetailsUIState.Success).posts) {
                        ReviewCard(it.post)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    post: Post
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .padding(10.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, // controls "elasticity"
                    stiffness = Spring.StiffnessLow // lower = slower, more bounce
                )
            )
            .clickable(
                onClick = {
                    expanded = !expanded
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(10.dp)
            ) {
                // AsyncImage() profile pic
                Text(post.author, fontSize = 24.sp)
            }

            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(10.dp)
            ) {
                Text(
                    post.rating.toString(),
                    fontSize = 24.sp
                )

                if (expanded){
                    Text(post.postBody, fontSize = 16.sp, fontWeight = FontWeight.W200)
                }
            }
        }
    }
}