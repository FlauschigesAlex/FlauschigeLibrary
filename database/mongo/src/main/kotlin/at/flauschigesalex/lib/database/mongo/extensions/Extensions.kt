@file:Suppress("unused")

package at.flauschigesalex.lib.database.mongo.extensions

import at.flauschigesalex.lib.base.file.JsonManager
import org.bson.BsonDocument
import org.bson.Document

operator fun JsonManager.Companion.invoke(document: Document): JsonManager = invoke(document.toJson())!!
fun JsonManager.Companion.ofList(document: Document): List<JsonManager> = listOf(document.toJson())

fun JsonManager.toDocument(): Document = Document.parse(this.toString())
fun JsonManager.toBsonDocument(): BsonDocument = this.toDocument().toBsonDocument()