package hu.bme.ait.sean.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import hu.bme.ait.sean.data.SearchResponse.SearchAlbum
import kotlin.math.abs

@Composable
fun SearchScreen(
    viewModel : SearchViewModel = hiltViewModel(),
    navigateToDetailScreen : (String, String) -> Unit
){
    var searchText by remember { mutableStateOf("") }

    Column (
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {searchText = it},
            label = {Text("Search...")},
            trailingIcon = {
                Text("blud", modifier = Modifier.clickable(){
                        viewModel.search(searchText)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(8.dp))

        when (viewModel.searchUIState){
            is SearchUIState.Loading -> {
                Column (
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SearchUIState.Success -> {
                val albums = (viewModel.searchUIState as SearchUIState.Success).res.results?.albummatches?.album!!
                if (albums.isNotEmpty()) {
                    LazyColumn () {
                        items(items = albums) {
                            Column{
                                Spacer(Modifier.height(3.dp))
                                AlbumSummaryCard(it!!, navigateToDetailScreen = navigateToDetailScreen)
                            }
                        }
                    }
                } else {
                    Column (
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No Results", fontWeight = FontWeight.W200)
                    }

                }
            }
            is SearchUIState.Error -> {
                Column (
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ran into Error: ${(viewModel.searchUIState as SearchUIState.Error).msg}", fontWeight = FontWeight.W200)
                }
            }
        }

    }
}

@Composable
fun AlbumSummaryCard (
    album : SearchAlbum,
    modifier : Modifier = Modifier,
    navigateToDetailScreen: (String, String) -> Unit
) {
    Card (
        modifier = modifier.fillMaxWidth().clickable{
            navigateToDetailScreen(album.name?:"slitherman", album.artist?:"RXK Nephew")
        },
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ){
        Row (
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            AsyncImage(
                album.image!!.last()!!.text,
                contentDescription = "album cover",
                modifier = Modifier.weight(2f).padding(5.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier.weight(8f).padding(10.dp)
            ) {
                Text(album.name?:"slitherman", fontSize = 20.sp, fontWeight = FontWeight.W400)
                Text(album.artist?:"slitherman", fontSize = 12.sp, fontWeight = FontWeight.W300)
            }
        }

    }
}