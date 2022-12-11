package com.bouyahya.feature_posts.data.repository

import com.bouyahya.feature_posts.data.models.Post
import com.bouyahya.feature_posts.data.responses.PostResponse
import org.litote.kmongo.coroutine.CoroutineDatabase

class PostRepositoryImpl(
    db: CoroutineDatabase
) : PostRepository {

    private val posts = db.getCollection<Post>()
    override suspend fun createPost(post: Post): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deletePost(postId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getPostsByFollows(userId: String, page: Int, pageSize: Int): List<PostResponse> {
        TODO("Not yet implemented")
    }
}