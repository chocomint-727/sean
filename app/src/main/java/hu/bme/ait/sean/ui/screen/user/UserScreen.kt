package hu.bme.ait.sean.ui.screen.user

import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.revenuecat.placeholder.PlaceholderDefaults
import hu.bme.ait.sean.data.PostID
import hu.bme.ait.sean.ui.theme.Background2
import hu.bme.ait.sean.ui.theme.Background3
import hu.bme.ait.sean.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel(),
    toDetailsScreen : (String, String) -> Unit
) {
    val userPosts = viewModel.loadReviewsForUser().collectAsState(
        initial = UserUIState.Init
    )

    val snackbarHostState = remember { SnackbarHostState() }

    DoubleBackPressExit(snackbarHostState)

    Column(
            modifier = Modifier
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        when (val state = viewModel.userUIState) {
            is UserUIState.Init -> {
                UserCard(
                    user = User(),
                    onBioChange = { newName, newBio ->
                        viewModel.updateUsername(newName)
                        viewModel.updateBio(newBio)
                    }
                )
            }

            is UserUIState.Loading -> {
                Spacer(Modifier.size(300.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(180.dp),
                    color = Primary
                )
            }

            is UserUIState.Error -> {
                Text("Error Loading User Details")
            }

            is UserUIState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserCard(
                        user = state.user,
                        onBioChange = { newName, newBio ->
                            viewModel.updateUsername(newName)
                            viewModel.updateBio(newBio)
                        }
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.padding(2.dp))

                    when (val state = userPosts.value) {
                        is PostDetailsUIState.Loading -> {
                            Spacer(Modifier.size(240.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(100.dp),
                                color = Primary
                            )
                        }

                        is PostDetailsUIState.Error -> {
                            Text("Error Loading posts for user with ${state.msg}")
                            Log.d("ERROR_TEXT", state.msg)
                        }

                        is PostDetailsUIState.Success -> {
                            if (state.posts.isEmpty()) {
                                Column {
                                    Spacer(Modifier.size(80.dp))
                                    Text("Review an Album to get started!")
                                }
                            }
                            else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(state.posts) { postWithId: PostID ->
                                        UserReviewCard(
                                            post = postWithId.post,
                                            getAlbumData = { id -> viewModel.getAlbumData(id) },
                                            toDetailsScreen = toDetailsScreen,
                                            onDelete = {
                                                viewModel.deleteReview(postWithId.id)
                                            }
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

@Composable
fun UserCard(
    user : User,
    onBioChange: (String, String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var nameText by rememberSaveable (user.name) { mutableStateOf(user.name) }
    var bioText by rememberSaveable (user.bio) { mutableStateOf(user.bio) }
    
    Card (
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                user.pfpURL,
                contentDescription = "",
                modifier = Modifier
                    .size(100.dp, 100.dp)
                    .placeholder(
                        enabled = user.pfpURL.isEmpty(),
                        shape = CircleShape,
                        highlight = PlaceholderDefaults.fade
                    )
            )
            Column (
                horizontalAlignment = Alignment.Start,
            ){
                if (isEditing){
                    Dialog(onDismissRequest = {
                        isEditing = false
                    }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(size = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(15.dp)
                            ) {
                                OutlinedTextField(
                                    value = nameText,
                                    onValueChange = {nameText = it},
                                    singleLine = true,
                                    label = {Text("Username")},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = bioText,
                                    onValueChange = {bioText = it},
                                    singleLine = true,
                                    label = {Text("Bio")},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        onClick = {
                                            nameText = user.name
                                            bioText = user.bio
                                            isEditing = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        onClick = {
                                            if (nameText.isNotEmpty()) {
                                                onBioChange(nameText, bioText)
                                                isEditing = false
                                            }
                                        }
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(user.name, fontSize = 24.sp, modifier = Modifier.padding(10.dp))
                    IconButton(onClick = {isEditing = true}) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "",
                            tint = Primary
                        )
                    }
                }
                Text(user.bio, fontSize = 16.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun UserReviewCard(
    post: Post,
    getAlbumData: (String) -> Flow<StoredAlbumData?>,
    toDetailsScreen: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Background2
        )
    ) {
        val albumData = getAlbumData(post.albumID).collectAsState(
            StoredAlbumData()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp), // small right padding so icon isn't glued to edge
            verticalAlignment = Alignment.CenterVertically
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
                Row {
                    Text(albumData.value?.name ?: "")
                }
                Text(
                    text = post.postBody,
                    // optional: limit lines so the card doesn’t get huge
                    // maxLines = 5,
                    // overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                modifier = Modifier.padding(top = 10.dp),
                onClick = onDelete
            ) {
                Icon(Icons.Default.Delete, "Delete Review?")
            }
        }
    }
}

@Composable
fun DoubleBackPressExit(
    snackbarHostState: SnackbarHostState,
    exitDelayMillis: Long = 2000L,
    snackbarMessage: String = "Press back again to exit"
) {
    // State to track if the back button has been pressed once
    var backPressedOnce by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Handle the system back button press
    BackHandler(enabled = true) {
        if (backPressedOnce) {
            // Second press within the time window: Exit the app/Activity
            (context as? ComponentActivity)?.finish()
        } else {
            // First press: Set the flag and show the Snackbar
            backPressedOnce = true

            // Show the Snackbar
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = snackbarMessage,
                    duration = SnackbarDuration.Short
                )
                // We don't necessarily need to check the result,
                // but we launch a separate coroutine to reset the flag
                // after the delay, regardless of the Snackbar's status.
            }

            // Start a coroutine to reset the flag after the delay
            coroutineScope.launch {
                delay(exitDelayMillis)
                backPressedOnce = false
            }
        }
    }
}