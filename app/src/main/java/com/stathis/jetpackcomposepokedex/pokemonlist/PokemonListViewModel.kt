package com.stathis.jetpackcomposepokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.stathis.jetpackcomposepokedex.data.models.PokedexListEntry
import com.stathis.jetpackcomposepokedex.network.PokemonApiClient
import com.stathis.jetpackcomposepokedex.util.Constants.PAGE_SIZE
import com.stathis.jetpackcomposepokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonApiClient
) : ViewModel() {

    private var curPage = 0

    var pokemonList = mutableStateOf<List<PokedexListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokedexListEntry>()
    private var isSearchStating = true
    var isSearching = mutableStateOf(false)

    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query : String){
        val listToSearch = if(isSearchStating){
            pokemonList.value
        } else {
            cachedPokemonList
        }

        viewModelScope.launch(Dispatchers.Default){
            if(query.isEmpty()){
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStating = true
                return@launch
            }
            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }

            if(isSearchStating){
                cachedPokemonList = pokemonList.value
                isSearchStating = false
            }

            pokemonList.value = results
            isSearching.value = true
        }
    }

    fun loadPokemonPaginated() {
        viewModelScope.launch {
            isLoading.value = true

            val result = repository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)
            when (result) {
                is Resource.Success -> {
                    endReached.value = curPage * PAGE_SIZE >= result.data!!.count
                    val pokedexEntries = result.data.results.mapIndexed { index, result ->
                        val number = if (result.url.endsWith("/")) {
                            result.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            result.url.takeLastWhile { it.isDigit() }
                        }

                        val url =
                            "ttps://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"

                        PokedexListEntry(result.name.capitalize(Locale.ROOT), url, number.toInt())
                    }
                    curPage++

                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokedexEntries
                }

                is Resource.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
            }
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue -> onFinish(Color(colorValue)) }
        }
    }
}