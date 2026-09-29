package wtf.devil.cengbot.utils.database.repo;

import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.SessionFactory;
import wtf.devil.cengbot.utils.database.Database;

import java.util.List;
import java.util.Optional;

// Spring Data style CrudRepository. Every call runs in its own session and returns detached entities.
// findById + set + save overwrites the whole row, so for read-modify-write (e.g. balances) add a locked
// update method to the entity's repo instead, like UserRepo.update.
public abstract class CrudRepo<T, ID> {

    private final Class<T> entityClass;

    protected CrudRepo(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<T> findById(ID id) {
        return sessions().fromSession(session -> Optional.ofNullable(session.find(entityClass, id)));
    }

    public List<T> findAll() {
        return sessions().fromSession(session -> {
            CriteriaQuery<T> query = session.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return session.createSelectionQuery(query).getResultList();
        });
    }

    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    public long count() {
        return sessions().fromSession(session -> {
            CriteriaQuery<Long> query = session.getCriteriaBuilder().createQuery(Long.class);
            query.select(session.getCriteriaBuilder().count(query.from(entityClass)));
            return session.createSelectionQuery(query).getSingleResult();
        });
    }

    // Inserts or updates, returning the saved copy
    public T save(T entity) {
        return sessions().fromTransaction(session -> session.merge(entity));
    }

    // Returns true if something was deleted
    public boolean deleteById(ID id) {
        return sessions().fromTransaction(session -> {
            T entity = session.find(entityClass, id);
            if (entity == null) {
                return false;
            }
            session.remove(entity);
            return true;
        });
    }

    protected static SessionFactory sessions() {
        return Database.sessions();
    }
}
