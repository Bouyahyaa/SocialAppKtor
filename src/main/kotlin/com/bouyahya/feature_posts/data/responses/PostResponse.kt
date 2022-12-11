package com.bouyahya.feature_posts.data.responses

import com.bouyahya.feature_posts.data.models.Post

data class PostResponse(
    val success: Boolean,
    val data: List<Post>
)