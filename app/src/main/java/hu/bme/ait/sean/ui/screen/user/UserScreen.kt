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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
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

    val showUsernameEditDialog by rememberSaveable { mutableStateOf(true)}

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
                    onNameChange = {})
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
                Column() {
                    UserCard(
                        user = state.user,
                        onNameChange = { newName ->
                            viewModel.updateUsername(newName)
                        }
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.padding(2.dp))

                    when (val state = userPosts.value) {
                        is PostDetailsUIState.Loading -> {
                            CircularProgressIndicator()
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
}

@Composable
fun UserCard(
    user : User,
    onNameChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var nameText by rememberSaveable (user.name) { mutableStateOf(user.name) }

    Card (
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                user.pfpURL,
                contentDescription = ""
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
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
                                    TextButton(
                                        onClick = {
                                            nameText = user.name
                                            isEditing = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(user.name, fontSize = 24.sp, modifier = Modifier.padding(10.dp))
                        IconButton(onClick = {isEditing = true}) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = ""
                            )
                        }
                    }
                }
                Text(user.bio, fontSize = 16.sp, modifier = Modifier.padding(10.dp))
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
    Box(
        modifier = Modifier.size(0.dp)
    ) {
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
}