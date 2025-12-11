package hu.bme.ait.sean.ui.screen.otheruser

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
            .fillMaxWidth(),
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
                Spacer(modifier = Modifier.padding(2.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.padding(2.dp))

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
    Card (
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                user?.pfpURL,
                contentDescription = ""
            )
            Column (
                horizontalAlignment = Alignment.Start,
            ) {
                Text(user?.name ?: "User could not be loaded", fontSize = 24.sp, modifier = Modifier.padding(10.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        val albumData = getAlbumData(post.albumID).collectAsState(
            StoredAlbumData()
        )
        Row {
            AsyncImage(
                albumData.value?.img_url,
                "Album Cover",
                modifier = Modifier
                    .placeholder(
                        enabled = albumData.value == null,
                    )
                    .size(70.dp, 70.dp)
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
                modifier = Modifier.padding(10.dp)
            ) {
                Row {
                    Text(albumData.value?.name ?: "")
                }
                Text(post.postBody)
            }

        }


    }
}