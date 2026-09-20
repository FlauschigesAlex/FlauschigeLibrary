@file:Suppress("unused")

package at.flauschigesalex.lib.database.mongo.extensions

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.FullDocumentBeforeChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bson.Document

private typealias ChangeInvocation<TDocument> = (MongoCollectionChange<TDocument>) -> Unit

context(database: MongoDatabase)
fun <TDocument: Any> MongoCollection<TDocument>.watch(
    scope: CoroutineScope? = null,
    onChange: ChangeInvocation<TDocument>,
) = MongoWatcher.watch(this, scope, onChange)

object MongoWatcher {
    var fallbackScope: CoroutineScope = CoroutineScope(SupervisorJob())
    
    private val hasWatcherEnabled = mutableSetOf<MongoCollection<*>>()

    context(database: MongoDatabase)
    fun <TDocument: Any> watch(
        collection: MongoCollection<TDocument>,
        scope: CoroutineScope? = null,
        onChange: ChangeInvocation<TDocument>,
    ) {
        val scope = scope ?: fallbackScope

        scope.launch {
            if (collection !in hasWatcherEnabled) {
                database.enableFullDocumentWatcher(collection)
                hasWatcherEnabled.add(collection)
            }
            
            collection.watch()
                .fullDocument(FullDocument.UPDATE_LOOKUP)
                .fullDocumentBeforeChange(FullDocumentBeforeChange.WHEN_AVAILABLE)
                .map {
                    MongoCollectionChange(
                        it.fullDocumentBeforeChange,
                        it.fullDocument,
                        it,
                        collection
                    )
                }.forEach { onChange(it) }
        }
    }
}

data class MongoCollectionChange<TDocument>(
    val previous: TDocument?,
    val current: TDocument?,
    val changeStream: ChangeStreamDocument<TDocument>,
    val collection: MongoCollection<TDocument>,
)

fun MongoDatabase.enableFullDocumentWatcher(collection: MongoCollection<*>) {
    this.runCommand(Document("collMod", collection.namespace.collectionName)
        .append("changeStreamPreAndPostImages", Document("enabled", true))
    )
}