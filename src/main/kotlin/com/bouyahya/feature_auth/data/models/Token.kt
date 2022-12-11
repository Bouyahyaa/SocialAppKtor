package com.bouyahya.feature_auth.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Token(
    @BsonId
    val id: String = ObjectId().toString(),
    val userId: String,
    val code: String,
    val type: String
)