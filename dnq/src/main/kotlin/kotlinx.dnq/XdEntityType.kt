package kotlinx.dnq

import jetbrains.exodus.database.TransientEntityStore
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.query.XdQuery
import kotlinx.dnq.query.XdQueryImpl
import kotlinx.dnq.store.container.StoreContainer

abstract class XdEntityType<out T : XdEntity>(val storeContainer: StoreContainer) {
    abstract val entityType: String
    val entityStore: TransientEntityStore
        get() = storeContainer.store

    fun all(): XdQuery<T> {
        return XdQueryImpl(entityStore.queryEngine.queryGetAll(entityType), this)
    }

    open fun new(init: (T.() -> Unit)? = null): T {
        val transaction = (entityStore.threadSession
                ?: throw IllegalStateException("New entities can be created only in transactional block"))
        return wrap(transaction.newEntity(entityType)).apply {
            constructor()
            if (init != null) {
                init()
            }
        }
    }

    open fun wrap(entity: Entity) = entity.toXd<T>()
}