package com.stathis.jetpackcomposepokedex.data

data class HeldItem(
    val item: Item,
    val version_details: List<VersionDetail>
)