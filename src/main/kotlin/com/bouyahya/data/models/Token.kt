package com.bouyahya.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Token(
    @BsonId
    val id: String = ObjectId().toString(),
    val userId: String,
    val Code: Long
)