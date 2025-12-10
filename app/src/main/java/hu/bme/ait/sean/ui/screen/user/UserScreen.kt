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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel(),
    toDetailsScreen : (String, String) -> Unit
) {
    val userPosts = viewModel.loadReviewsForUser().collectAsState(
        initial = UserUIState.Init
    )

    val snackbarHostState = remember { SnackbarHostState() }

    DoubleBackPressExit(snackbarHostState = snackbarHostState)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    when (val state = viewModel.userUIState) {
                        is UserUIState.Init -> {
                            UserCard(
                                user = User(),
                                onNameChange = {})
                        }

                        is UserUIState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is UserUIState.Error -> {
                            Text("Error Loading User Details")
                        }

                        is UserUIState.Success -> {
                            UserCard(
                                user = state.user,
                                onNameChange = { newName ->
                                    viewModel.updateUsername(newName)
                                }
                            )
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
    })
}

@Composable
fun UserCard(
    user : User,
    onNameChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var nameText by remember(user.name) {mutableStateOf(user.name)}

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
                if(isEditing){
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {nameText = it},
                        singleLine = true,
                        label = {Text("Username")},
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (nameText.isNotBlank()) {
                                    onNameChange(nameText)
                                    isEditing = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                    TextButton(
                        onClick = {
                            nameText = user.name
                            isEditing = false
                        }
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(user.name, fontSize = 24.sp)
                        IconButton(onClick = {isEditing = true}) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = ""
                            )
                        }
                    }
                }
                Text(user.email, fontSize = 16.sp)
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

        Row(

        ) {
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
                                ?: "but it has to be graaceful and not crash the whole app"
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
/**
 * Implements the "Press back again to exit" pattern using a Snackbar.
 *
 * @param snackbarHostState The state used to control the Snackbar's visibility.
 * @param exitDelayMillis The time window (in milliseconds) for the second press.
 * @param snackbarMessage The message to display in the Snackbar.
 */
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
            (context as? androidx.activity.ComponentActivity)?.finish()
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