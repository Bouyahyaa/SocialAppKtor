package com.bouyahya.feature_posts.data.repository

import com.bouyahya.feature_posts.data.models.Post
import com.bouyahya.feature_posts.data.responses.PostResponse
import com.bouyahya.util.Constants.DEFAULT_PAGE_SIZE

interface PostRepository {
    suspend fun createPost(post: Post): Boolean

    suspend fun deletePost(postId: String)

    suspend fun getPostsByFollows(
        userId: String,
        page: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<PostResponse>
}