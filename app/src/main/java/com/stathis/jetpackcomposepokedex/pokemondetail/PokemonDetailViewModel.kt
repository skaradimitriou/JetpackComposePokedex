package com.stathis.jetpackcomposepokedex.pokemondetail

import androidx.lifecycle.ViewModel
import com.stathis.jetpackcomposepokedex.data.Pokemon
import com.stathis.jetpackcomposepokedex.network.PokemonApiClient
import com.stathis.jetpackcomposepokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor (
    private val repository : PokemonApiClient
) : ViewModel() {

    suspend fun getPokemonInfo(pokemonName : String) : Resource<Pokemon> {
        return repository.getPokemonInfo(pokemonName)
    }
}