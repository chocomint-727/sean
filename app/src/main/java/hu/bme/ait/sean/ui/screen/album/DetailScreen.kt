package hu.bme.ait.sean.ui.screen.album

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import hu.bme.ait.sean.data.AlbumResponse.Album
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.ui.theme.Primary

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this?: "")
        .map { it.range.first }
        .toList()

@Composable
fun DetailScreen(
    album: String,
    artist: String,
    modifier: Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    goToReviewScreen : (String, String, String) -> Unit,
) {

    val ctx = LocalContext.current

    val postListState = viewModel.loadReviews().collectAsState(
        initial = PostDetailsUIState.Loading
    )

    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getDetails(album, artist)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(10.dp)
    ) {
        Column (
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy, // controls "elasticity"
                        stiffness = Spring.StiffnessHigh // lower = slower, more bounce
                    )
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    val details = (viewModel.albumDetailsUIState as AlbumDetailsUIState.Success).res.album!!
                    AsyncImage(
                        model = details.image?.last()?.text
                            ?: "",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f),
                        contentDescription = "Album Cover"
                    )

                    Text(details.name ?: "SLITHERMAN VS NEPHEW", fontSize = 32.sp, fontWeight = FontWeight.W400)
                    Row {
                        Text(
                            details.artist ?: "RXKNephew",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W200
                        )
                        Icon(
                            if (showInfo) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                            contentDescription = "",
                            modifier = Modifier.clickable{
                                showInfo = !showInfo
                            }
                        )
                    }

                    if (showInfo){
                        Text(
                            details.wiki?.summary?.take(details.wiki?.summary.indexesOf("<a", true).firstOrNull() ?: details.wiki?.summary?.length ?: 0) ?: "SLITHERMAN VS NEPHEW",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W200,
                            textAlign = TextAlign.Center
                        )

                        Row(Modifier.fillMaxWidth(0.8f)) {
                            Text(
                                "Plays: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(3f)
                            )
                            Text(
                                details.playcount ?: "SLITHERMAN VS NEPHEW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W200,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(7f)
                            )
                        }
                        Row(Modifier.fillMaxWidth(0.8f)) {
                            Text(
                                "Listeners: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(3f)
                            )
                            Text(
                                details.listeners ?: "SLITHERMAN VS NEPHEW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W200,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(7f)
                            )
                        }
                        Row(Modifier.fillMaxWidth(0.8f)) {
                            Text(
                                "MBID: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(3f)
                            )
                            Text(
                                details.mbid ?: "SLITHERMAN VS NEPHEW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W200,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .weight(7f)
                            )
                        }

                        if (details.tracks != null) {
                            LazyColumn(
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(2.dp)
                                    .heightIn(max = 100.dp)
                                    .fillMaxWidth(0.8f)
                            ) {
                                itemsIndexed(items = details.tracks!!.track!!) { ix, track ->
                                    Row (
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Text(
                                            "$ix. ${track?.name ?: "SLITHERMAN VS NEPHEW"}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.W200,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                        Spacer(Modifier.weight(1f))
                                        IconButton(
                                            {
                                                // call intent to send to music app
                                                viewModel.openInMusic(
                                                    ctx,
                                                    track?.name ?: "Block List",
                                                    details.name ?: "Block List - Single",
                                                    details.artist ?: "RXKNephew"
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Filled.Start, contentDescription = "To Music App", Modifier.size(16.dp, 16.dp))
                                        }
                                    }

                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    Row() {

                        Button(
                            { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Open in music app")
                        }
                        Button(
                            {
                                goToReviewScreen(details.mbid!!, details.name!!, details.artist!!)
                            },
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
        }


        when (postListState.value) {
            is PostDetailsUIState.Loading -> {
                CircularProgressIndicator()
            }

            is PostDetailsUIState.Error -> {
                Text("Error Loading Posts: \n${(postListState.value as PostDetailsUIState.Error).msg}")
                Log.d("ERROR_TEXT", (postListState.value as PostDetailsUIState.Error).msg)
            }

            is PostDetailsUIState.Success -> {
                LazyColumn() {
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

                if (expanded) {
                    Text(post.postBody, fontSize = 16.sp, fontWeight = FontWeight.W200)
                }
            }
        }
    }
}