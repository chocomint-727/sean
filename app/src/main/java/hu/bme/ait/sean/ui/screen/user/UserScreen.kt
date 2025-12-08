package hu.bme.ait.sean.ui.screen.user

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.revenuecat.placeholder.placeholder
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.StoredAlbumDataID
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.album.PostDetailsUIState
import kotlinx.coroutines.flow.Flow

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel(),
    toDetailsScreen : (String, String) -> Unit
) {
    val userPosts = viewModel.loadReviewsForUser().collectAsState(
        initial = UserUIState.Init
    )

    Column (
        modifier = Modifier.padding(10.dp)
    ){
        when (viewModel.userUIState) {
            is UserUIState.Init -> {
                UserCard(User())
            }
            is UserUIState.Loading -> {
                CircularProgressIndicator()
            }
            is UserUIState.Error -> {
                Text("Error Loading User Details")
            }
            is UserUIState.Success -> {
                UserCard((viewModel.userUIState as UserUIState.Success).user)
            }
        }

        HorizontalDivider()

        when (val state = userPosts.value) {
            is PostDetailsUIState.Loading -> {
                CircularProgressIndicator()
            }
            is PostDetailsUIState.Error -> {
                Text("Error Loading posts for user with ${state.msg}")
                Log.d("ERROR_TEXT", state.msg)
            }
            is PostDetailsUIState.Success -> {
                LazyColumn {
                    items(state.posts) {
                        UserReviewCard(it.post, { id -> viewModel.getAlbumData(id) }, toDetailsScreen)
                    }
                }
            }
        }
    }

}

@Composable
fun UserCard(
    user : User
) {
    Card (
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            //AsyncImage()
            Column (
                horizontalAlignment = Alignment.Start,
            ){
                Text(user.name, fontSize = 24.sp)
                Text(user.email, fontSize = 16.sp)
                Text(user.uid, fontSize = 12.sp, fontWeight = FontWeight.W200)
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
    Card (
        modifier = Modifier.fillMaxWidth()
    ){
        val albumData = getAlbumData(post.albumID).collectAsState(
            StoredAlbumData()
        )

        Row(

        ) {
            AsyncImage(
                albumData.value?.img_url,
                "Album Cover",
                modifier = Modifier.placeholder(
                    enabled = albumData.value == null,
                )
                    .size(70.dp, 70.dp)
                    .padding(10.dp)
                    .clickable{
                        toDetailsScreen(albumData.value?.name ?: "throw error here i think", albumData.value?.artist ?: "but it has to be graaceful and not crash the whole app")
                    }
            )
            Column (
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(10.dp)
            ){
                Row {
                    Text(albumData.value?.name ?: "")
                }
                Text(post.postBody)
            }

        }


    }
}