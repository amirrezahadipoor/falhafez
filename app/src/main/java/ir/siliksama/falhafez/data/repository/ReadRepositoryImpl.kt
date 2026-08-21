package ir.siliksama.falhafez.data.repository

import ir.siliksama.falhafez.data.local.ReadDao
import ir.siliksama.falhafez.data.local.ReadEntity
import ir.siliksama.falhafez.domain.repository.ReadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReadRepositoryImpl @Inject constructor(
    private val readDao: ReadDao
) : ReadRepository {

    override fun observeIds(): Flow<Set<Long>> =
        readDao.observeIds().map { it.toSet() }

    override fun observeIsRead(poemId: Long): Flow<Boolean> =
        readDao.observe(poemId).map { it != null }

    override suspend fun markRead(poemId: Long) {
        readDao.markRead(ReadEntity(poemId, System.currentTimeMillis()))
    }

    override suspend fun unmarkRead(poemId: Long) {
        readDao.unmarkRead(poemId)
    }
}
