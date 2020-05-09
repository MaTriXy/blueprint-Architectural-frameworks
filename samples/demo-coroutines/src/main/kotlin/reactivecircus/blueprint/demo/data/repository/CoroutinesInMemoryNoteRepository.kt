package reactivecircus.blueprint.demo.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import reactivecircus.blueprint.demo.data.cache.NoteCache
import reactivecircus.blueprint.demo.domain.model.Note
import reactivecircus.blueprint.demo.domain.repository.CoroutinesNoteRepository

@FlowPreview
@ExperimentalCoroutinesApi
class CoroutinesInMemoryNoteRepository(
    private val noteCache: NoteCache
) : CoroutinesNoteRepository {

    private val notesChannel: BroadcastChannel<Unit> = BroadcastChannel(BUFFERED)

    override fun streamAllNotes(): Flow<List<Note>> {
        return notesChannel.asFlow()
            .map { noteCache.allNotes() }
            .onStart { emit(noteCache.allNotes()) }
    }

    override suspend fun getNoteByUuid(uuid: String): Note? {
        return noteCache.findNote { it.uuid == uuid }
    }

    override suspend fun addNote(note: Note) {
        noteCache.addNotes(listOf(note))

        // refresh all notes stream
        notesChannel.offer(Unit)
    }

    override suspend fun updateNote(note: Note) {
        noteCache.updateNote(note)

        // refresh all notes stream
        notesChannel.offer(Unit)
    }
}
