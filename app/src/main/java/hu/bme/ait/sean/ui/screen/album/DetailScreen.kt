package hu.bme.ait.sean.ui.screen.album

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.revenuecat.placeholder.PlaceholderDefaults
import com.revenuecat.placeholder.placeholder
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.theme.Primary
import kotlinx.coroutines.flow.Flow

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this ?: "")
        .map { it.range.first }
        .toList()

fun Modifier.realOffset(y: Dp) = layout { measurable, constraints ->
    val yPx = y.roundToPx()
    val newConst = constraints.copy(
        maxHeight = constraints.maxHeight - yPx,
        minHeight = constraints.minHeight - yPx
    )
    val placeable = measurable.measure(newConst)

    // expand layout to allow upward movement
    layout(placeable.width, placeable.height) {
        placeable.place(0, yPx / 2)
    }
}


@Composable
fun DetailScreen(
    album: String,
    artist: String,
    modifier: Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    goToReviewScreen: (String, String, String, String) -> Unit,
    goToUserScreen: (String) -> Unit
) {

    val ctx = LocalContext.current

    var showInfo by remember { mutableStateOf(false) }

    var albumCoverTargetSize by remember { mutableFloatStateOf(1f) }
    val albumCoverSize: Float by animateFloatAsState(
        albumCoverTargetSize,
        keyframes {
            durationMillis = 300
            albumCoverTargetSize * 0.5f at 150 with EaseOut
            albumCoverTargetSize at 500 with EaseIn
        },
        label = "size"
    )

    LaunchedEffect(Unit) {
        viewModel.getDetails(album, artist)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .realOffset(albumCoverSize.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewModel.albumDetailsUIState) {
                is AlbumDetailsUIState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(180.dp),
                        color = Primary
                    )
                }

                is AlbumDetailsUIState.Error -> {
                    Text("Error Loading Album details")
                }

                is AlbumDetailsUIState.Success -> {
                    val details =
                        (viewModel.albumDetailsUIState as AlbumDetailsUIState.Success).res.album!!

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        AsyncImage(
                            model = details.image?.last()?.text
                                ?: "",
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .aspectRatio(1f)
                                .graphicsLayer { alpha = 0.99f }
                                .drawWithContent() {
                                    val colors = listOf(
                                        Color.Black,
                                        Color.Transparent
                                    )
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(colors),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            contentDescription = "Album Cover"
                        )

                        Spacer(Modifier.height(10.dp))

                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .clickable {
                                    showInfo = !showInfo
                                    albumCoverTargetSize = if (showInfo) -250f else 0f
                                }
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        ) {
                            Text(
                                details.name ?: "SLITHERMAN VS NEPHEW",
                                fontSize = 24.sp,
                                maxLines = 1,
                                fontWeight = FontWeight.W400
                            )
                            Row {
                                Text(
                                    details.artist ?: "RXKNephew",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.W300
                                )
                                Icon(
                                    if (showInfo) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                                    contentDescription = "",
                                )
                            }
                        }

                        Button(
                            {
                                goToReviewScreen(
                                    details.mbid!!,
                                    details.name!!,
                                    details.artist!!,
                                    details.image?.last()?.text ?: ""
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Filled.BorderColor, contentDescription = "Write a Review")
                        }
                    }

                    Spacer(Modifier.height(10.dp))


                    AnimatedVisibility(
                        visible = showInfo,
                        enter = expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                        ),
                        exit = shrinkVertically(
                            animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                details.wiki?.summary?.take(
                                    details.wiki?.summary.indexesOf(
                                        "<a",
                                        true
                                    ).firstOrNull() ?: details.wiki?.summary?.length ?: 0
                                ) ?: "SLITHERMAN VS NEPHEW",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W300,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )

                            Spacer(Modifier.height(10.dp))

                            if (details.tracks != null) {
                                LazyColumn(
                                    modifier = Modifier
                                        .border(1.dp, Color.Black)
                                        .padding(2.dp)
                                        .heightIn(max = 150.dp)
                                        .fillMaxWidth(0.8f)
                                ) {
                                    itemsIndexed(items = details.tracks!!.track!!) { ix, track ->
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "$ix. ${track?.name ?: "SLITHERMAN VS NEPHEW"}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.W300,
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
                                                Icon(
                                                    Icons.Filled.Start,
                                                    contentDescription = "To Music App",
                                                    Modifier.size(16.dp, 16.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                }
            }

            HorizontalDivider()
        }


        if (viewModel.storedAlbumData == null) {
            CircularProgressIndicator()
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                ) {
                    items(items = viewModel.storedAlbumData!!.albumData.reviewUids) {
                        ReviewCard(
                            it,
                            {
                                viewModel.loadReviews(it)
                            },
                            {
                                viewModel.getUserData(it)
                            },
                            { uid ->
                                goToUserScreen(uid)
                                Log.d("DETAIL_SCREEN", "PASSING THE UID?")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    uid: String,
    uidPostsFlowFunction: (String) -> Flow<PostDetailsUIState>,
    userDataFlowFunction: (String) -> Flow<User?>,
    goToUserScreen: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val userData = userDataFlowFunction(uid).collectAsState(
        User()
    )
    val postListState = uidPostsFlowFunction(uid).collectAsState(
        initial = PostDetailsUIState.Loading
    )

    val duration = 300

    when (val postState = postListState.value) {
        is PostDetailsUIState.Loading -> {
            CircularProgressIndicator(color = Primary)
        }

        is PostDetailsUIState.Error -> {
            Text(postState.msg)
        }

        is PostDetailsUIState.Success -> {
            if (postState.posts.isNotEmpty()) {
                Card(
                    elevation = CardDefaults.cardElevation(10.dp),
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable(
                            onClick = {
                                expanded = !expanded
                            }
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        AnimatedVisibility(
                            visible = !expanded,
                            enter = expandHorizontally(
                                expandFrom = Alignment.Start,
                                animationSpec = tween(
                                    delayMillis = duration,
                                    durationMillis = duration,
                                    easing = EaseInOut
                                )
                            ),
                            exit = shrinkHorizontally(
                                shrinkTowards = Alignment.Start,
                                animationSpec = tween(durationMillis = duration, easing = EaseInOut)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    userData.value!!.pfpURL,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(40.dp, 40.dp)
                                        .clickable(
                                            true,
                                            onClick = {
                                                goToUserScreen(uid)
                                                Log.d("REVIEW_CARD", "PFP CLICKED")
                                            }
                                        )
                                )

                                Text(
                                    userData.value!!.name,
                                    fontSize = 12.sp,
                                    maxLines = if (!expanded) 1 else Int.MAX_VALUE,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .weight(6f)
                        ) {

                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically(
                                    expandFrom = Alignment.Bottom,
                                    animationSpec = tween(
                                        delayMillis = duration,
                                        durationMillis = duration,
                                        easing = EaseInOut
                                    )
                                ),
                                exit = shrinkVertically(
                                    shrinkTowards = Alignment.Bottom,
                                    animationSpec = tween(
                                        durationMillis = duration,
                                        easing = EaseInOut
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        userData.value!!.pfpURL,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .size(40.dp, 40.dp)
                                            .placeholder(
                                                enabled = userData.value!!.pfpURL.isEmpty(),
                                                shape = CircleShape,
                                                highlight = PlaceholderDefaults.fade
                                            )
                                    )// profile pic
                                    Text(
                                        userData.value!!.name,
                                        fontSize = 12.sp,
                                        maxLines = if (!expanded) 1 else Int.MAX_VALUE,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }


                            val post = postState.posts[0].post
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (i in 1..5) {
                                        Icon(
                                            if (post.rating >= i.toFloat()) Icons.Filled.Star else Icons.Sharp.Clear,
                                            contentDescription = "star"
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "%.1f".format(post.rating) + " / 5",
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    post.postBody,
                                    fontSize = 16.sp,
                                    maxLines = if (!expanded) 4 else Int.MAX_VALUE,
                                    fontWeight = FontWeight.W300,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            AnimatedVisibility(
                                expanded,
                                enter = expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween(
                                        delayMillis = duration,
                                        durationMillis = duration,
                                        easing = EaseInOut
                                    )
                                ),
                                exit = shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween(
                                        durationMillis = duration,
                                        easing = EaseInOut
                                    )
                                )
                            ) {

                                Column {
                                    postState.posts.drop(1).forEach {
                                        HorizontalDivider(Modifier.padding(12.dp))
                                        Column {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                for (i in 1..5) {
                                                    Icon(
                                                        if (it.post.rating >= i.toFloat()) Icons.Filled.Star else Icons.Sharp.Clear,
                                                        contentDescription = "star"
                                                    )
                                                }
                                                Spacer(Modifier.weight(1f))
                                                Text(
                                                    "%.1f".format(it.post.rating) + " / 5",
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Text(
                                                it.post.postBody,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.W300
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//                        Row(Modifier.fillMaxWidth(0.8f)) {
//                            Text(
//                                "Plays: ",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W600,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(3f)
//                            )
//                            Text(
//                                details.playcount ?: "SLITHERMAN VS NEPHEW",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W300,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(7f)
//                            )
//                        }
//                        Row(Modifier.fillMaxWidth(0.8f)) {
//                            Text(
//                                "Listeners: ",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W600,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(3f)
//                            )
//                            Text(
//                                details.listeners ?: "SLITHERMAN VS NEPHEW",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W300,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(7f)
//                            )
//                        }
//                        Row(Modifier.fillMaxWidth(0.8f)) {
//                            Text(
//                                "MBID: ",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W600,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(3f)
//                            )
//                            Text(
//                                details.mbid ?: "SLITHERMAN VS NEPHEW",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.W300,
//                                modifier = Modifier
//                                    .border(1.dp, Color.Black)
//                                    .padding(2.dp)
//                                    .weight(7f)
//                            )
//                        }