package hu.bme.ait.sean.ui.screen.otheruser

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.revenuecat.placeholder.placeholder
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.album.PostDetailsUIState
import kotlinx.coroutines.flow.Flow
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.revenuecat.placeholder.PlaceholderDefaults
import hu.bme.ait.sean.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserScreen(
    viewModel: OtherUserViewModel = viewModel(),
    uid: String,
    toDetailsScreen : (String, String) -> Unit,
) {
    val userPosts = viewModel.loadReviewsForUser(uid).collectAsState(
        Post()
    )

    val user = viewModel.getUser(uid).collectAsState(User()).value


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(180.dp),
                color = Primary
            )
        }
        else {
            Column {
                OtherUserCard(
                    user = user
                )
                HorizontalDivider(Modifier.padding(10.dp))

                when (val state = userPosts.value) {
                    is PostDetailsUIState.Loading -> {
                        CircularProgressIndicator(
                            color = Primary
                        )
                    }

                    is PostDetailsUIState.Error -> {
                        Text("Error Loading posts for user with ${state.msg}")
                        Log.d("ERROR_TEXT", state.msg)
                    }

                    is PostDetailsUIState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.posts) {
                                UserReviewCard(
                                    it.post,
                                    { id -> viewModel.getAlbumData(id) },
                                    toDetailsScreen
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun OtherUserCard(
    user: User?
) {
    var showFavAlbum by remember { mutableStateOf(false) }

    Box (
        modifier = Modifier.fillMaxWidth(),
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                user?.pfpURL,
                contentDescription = "",
                modifier = Modifier
                    .size(100.dp, 100.dp)
                    .placeholder(
                        enabled = (user?.pfpURL ?: "").isEmpty(),
                        shape = CircleShape,
                        highlight = PlaceholderDefaults.fade
                    )
                    .padding(10.dp)
                    .clip(CircleShape)
                    .clickable{
                        showFavAlbum = true
                    }
            )

            if (showFavAlbum) {
                Dialog(
                    {showFavAlbum = false}
                ) {
                    Surface (
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(size = 6.dp)
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            AsyncImage(
                                user?.pfpURL,
                                contentDescription = "",
                                modifier = Modifier
                                    .placeholder(
                                        enabled = (user?.pfpURL ?: "").isEmpty(),
                                        shape = CircleShape,
                                        highlight = PlaceholderDefaults.fade
                                    )
                                    .padding(10.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }

            Column (
                horizontalAlignment = Alignment.Start,
            ) {
                Text(user?.name ?: "User could not be loaded", fontSize = 24.sp, fontWeight = FontWeight.W600, modifier = Modifier.padding(10.dp))
                Text(user?.bio ?: "Bio could not be loaded", fontSize = 16.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun UserReviewCard (
    post : Post,
    getAlbumData : (String) -> Flow<StoredAlbumData?>,
    toDetailsScreen: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
            }
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = EaseInOut)
            )
    ) {
        val albumData = getAlbumData(post.albumID).collectAsState(
            StoredAlbumData()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp), // small right padding so icon isn't glued to edge
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                albumData.value?.img_url,
                "Album Cover",
                modifier = Modifier
                    .placeholder(
                        enabled = albumData.value == null,
                    )
                    .size(80.dp, 80.dp)
                    .padding(10.dp)
                    .clickable {
                        toDetailsScreen(
                            albumData.value?.name ?: "throw error here i think",
                            albumData.value?.artist
                                ?: "but it has to be graceful and not crash the whole app"
                        )
                    }
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)          // 🔑 constrain width so text wraps
                    .padding(10.dp)
            ) {

                Row (
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(albumData.value?.name ?: "", fontWeight = FontWeight.W600, modifier = Modifier.fillMaxWidth(0.65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "%.1f".format(post.rating) + " / 5",
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.padding(8.dp))
                Text(
                    text = post.postBody,
                    // optional: limit lines so the card doesn’t get huge
                    maxLines = if (!expanded) 5 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}