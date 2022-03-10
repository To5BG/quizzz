package server.api;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * Stub repository for testing
 * @param <T> Type of object stored in the DB
 * @param <S> Type used for ID (primary key)
 */
public class StubRepository<T, S> {
    Map<S, T> db;
    List<String> calledMethods;
    private Field idField;
    private boolean autoIncrement;
    private long sequence;

    /**
     * Create a new repository stub to use for testing
     * @param objClass Class of the object stored in the DB
     */
    public StubRepository(Class<T> objClass) {
        this.db = new HashMap<>();
        this.autoIncrement = false;
        this.sequence = 1;
        this.calledMethods = new ArrayList<>();

        for (var field : objClass.getFields()) {
            var ids = field.getAnnotationsByType(javax.persistence.Id.class);
            if (ids.length == 0) continue;
            idField = field;
            this.autoIncrement = (field.getAnnotationsByType(javax.persistence.GeneratedValue.class).length != 0);
            break;
        }
    }

    /**
     * Register a method call
     * @param callName The name of the method called
     */
    private void reg(String callName) {
        this.calledMethods.add(callName);
    }

    /**
     * Sets the ID of a new object if needed
     * @param id The ID of the object
     * @return A correct ID for the object
     */
    private S incrementIfNeeded(S id) {
        if (!this.autoIncrement) return id;
        if (id instanceof Integer) {
            if ((Integer)id != 0) return id;
            return (S)Integer.valueOf(Math.toIntExact(sequence++));
        } else if (id instanceof Long) {
            if ((Long)id != 0L) return id;
            return (S)Long.valueOf(sequence++);
        } else {
            throw new UnsupportedOperationException("Auto increment not supported for the given ID type");
        }
    }

    /**
     * Get the ID of the object
     * @param obj The object to get the ID of
     * @return The ID of the object
     */
    private S getObjectId(T obj) {
        Object idValue;
        try {
            idValue = idField.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return incrementIfNeeded((S)idValue);
    }

    /**
     * Save this object to the repository
     * @param obj The object to store
     * @param <V> Type of the object to store
     * @return The object stored in the DB
     */
    public <V extends T> V save(V obj) {
        reg("save");
        S objId = getObjectId(obj);
        db.put(objId, obj);
        try {
            idField.set(obj, objId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Get the number of items in the DB
     * @return The number of items in the DB
     */
    public long count() {
        reg("count");
        return db.size();
    }

    /**
     * Get all items in the DB
     * @return A list of all items in the DB
     */
    public List<T> findAll() {
        reg("findAll");
        return this.db.values().stream().toList();
    }

    /**
     * Return all items in the DB sorted
     * @param sort Criteria to sort the DB
     * @return Sorted list of all items of the DB
     */
    public List<T> findAll(Sort sort) {
        // Sorting not implemented for now
        reg("findAll");
        return null;
    }

    /**
     * Return a pagable list of all the items in the DB
     * @param pageable The pagable object
     * @return A page of the items in the DB
     */
    public Page<T> findAll(Pageable pageable) {
        // pagable not implemented for now
        reg("findAll");
        return null;
    }

    /**
     * Get the items in the DB with the matching IDs
     * @param ids The IDs to retrieve from the DB
     * @return A list of items matching the IDs
     */
    public List<T> findAllById(Iterable<S> ids) {
        reg("findAllById");
        List<T> result = new ArrayList<>();
        for (S id : ids) {
            if (db.containsKey(id)) result.add(db.get(id));
        }
        return result;
    }

    /**
     * Delete a DB item by its ID
     * @param id The ID of the item to remove
     */
    public void deleteById(S id) {
        reg("deleteById");
        if (id == null) return;
        db.remove(id);
    }

    /**
     * Remove an item from the DB
     * @param entity The item to remove
     */
    public void delete(T entity) {
        reg("delete");
        if (entity == null) return;
        S key = null;
        for (var entry : db.entrySet()) {
            if (entity.equals(entry.getValue())) {
                key = entry.getKey();
            }
        }
        if (key != null) db.remove(key);
    }

    /**
     * Remove all items from the DB with matching IDs
     * @param ids The IDs of the items to remove
     */
    public void deleteAllById(Iterable<? extends S> ids) {
        reg("deleteAllById");
        for (S id : ids) {
            deleteById(id);
        }
    }

    /**
     * Remove a list of items from the DB
     * @param entities The collection of items to remove
     */
    public void deleteAll(Iterable<? extends T> entities) {
        reg("deleteAll");
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     * Remove all items from the DB
     */
    public void deleteAll() {
        reg("deleteAll");
        this.db.clear();
    }

    /**
     * Store multiple items in the DB
     * @param entities A collection of items to store
     * @param <V> Type of the items to store
     * @return A list of the stored items
     */
    public <V extends T> List<V> saveAll(Iterable<V> entities) {
        reg("saveAll");
        List<V> results = new ArrayList<>();
        for (V entity : entities) {
            results.add(save(entity));
        }
        return results;
    }

    /**
     * Find an item by its ID
     * @param id The ID to look for
     * @return Optional with the item matching the given ID
     */
    public Optional<T> findById(S id) {
        reg("findById");
        return Optional.ofNullable(this.db.getOrDefault(id, null));
    }

    /**
     * Check if an item exists with the given ID
     * @param id The ID to check
     * @return True if an item with this ID exists, otherwise false
     */
    public boolean existsById(S id) {
        reg("existsById");
        return this.db.containsKey(id);
    }

    /**
     * Flush pending changes to the DB
     */
    public void flush() {
        reg("flush");
        // empty method, we write directly to the "db", no need to flush
    }

    /**
     * Save the item and flush the DB
     * @param entity The item to save
     * @param <V> The type of the item to save
     * @return The item saved to the DB
     */
    public <V extends T> V saveAndFlush(V entity) {
        reg("saveAndFlush");
        return save(entity);
    }

    /**
     * Save a collection of items and flush the DB
     * @param entities The collection of items to save
     * @param <V> The type of the items to save
     * @return A list of the items saved
     */
    public <V extends T> List<V> saveAllAndFlush(Iterable<V> entities) {
        reg("saveAllAndFlush");
        return saveAll(entities);
    }

    /**
     * Delete a collection of entities from the DB
     * @param entities The collection of entities to remove
     */
    public void deleteAllInBatch(Iterable<T> entities) {
        reg("deleteAllInBatch");
        deleteAll(entities);
    }

    /**
     * Delete items matching the given IDs from the DB
     * @param ids The IDs to remove from the DB
     */
    public void deleteAllByIdInBatch(Iterable<S> ids) {
        reg("deleteAllByIdInBatch");
        deleteAllById(ids);
    }

    /**
     * Remove all items from the DB
     */
    public void deleteAllInBatch() {
        reg("deleteAlInBatch");
        deleteAll();
    }

    /**
     * Get the item with the specified ID from the DB
     * @param id The ID to look for
     * @return The item matching the given ID, or null if not found
     */
    public T getOne(S id) {
        reg("getOne");
        Optional<T> result = findById(id);
        return result.orElse(null);
    }

    /**
     * Get the item with the specified ID from the DB
     * @param id The ID to look for
     * @return The item matching the given ID, or null if not found
     */
    public T getById(S id) {
        reg("getById");
        return getOne(id);
    }

    /**
     * Find an item matching the given item
     * @param example The item to look for
     * @return Optional of the item matching the given object
     */
    public <V extends T> Optional<V> findOne(Example<V> example) {
        reg("findOne");
        V result = null;
        for (var entry : this.db.entrySet()) {
            if (example.equals(entry.getValue())) {
                result = (V) entry.getValue();
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    /**
     * Find all items matching the given object
     * @param example The object to find matching items with
     * @param <V> The type of the object
     * @return A list of all items matching the given object
     */
    public <V extends T> List<V> findAll(Example<V> example) {
        reg("findAll");
        List<V> results = new ArrayList<>();
        for (var entry : this.db.entrySet()) {
            if (example.equals(entry.getValue())) {
                results.add((V)entry.getValue());
            }
        }
        return results;
    }

    /**
     * Find all items matching the given object and return them in a sorted order
     * @param example The object to find matching items with
     * @param sort Criteria to sort the DB
     * @param <V> The type of the object
     * @return A list of all items matching the given object
     */
    public <V extends T> List<V> findAll(Example<V> example, Sort sort) {
        reg("findAll");
        return findAll(example); // ignoring the sort for now
    }

    /**
     * Return a pagable list of all the items in the DB matching the given object
     * @param example The object to find matching items with
     * @param pageable The pagable object
     * @return A page of the items in the DB
     */
    public <V extends T> Page<V> findAll(Example<V> example, Pageable pageable) {
        reg("findAll");
        // ignoring pagable function versions for now
        return null;
    }

    /**
     * Get the number of items matching the given object in the DB
     * @param example The object to find matching items with
     * @param <V> The type of the object
     * @return The number of items matching the object
     */
    public <V extends T> long count(Example<V> example) {
        reg("count");
        return findAll(example).size();
    }

    /**
     * Check if an item matching the given object can be found in the DB
     * @param example The object to find a matching item with
     * @param <V> The type of the object
     * @return True if a matching item is found, otherwise false
     */
    public <V extends T> boolean exists(Example<V> example) {
        reg("exists");
        return findOne(example).isPresent();
    }

    /**
     * Returns entities matching the given {@link Example} applying the {@link Function queryFunction} that defines the
     * query and its result type.
     * @param example       must not be {@literal null}.
     * @param queryFunction the query function defining projection, sorting, and the result type
     */

    public <V extends T, R> R findBy(Example<V> example, Function<FluentQuery.FetchableFluentQuery<V>, R>
            queryFunction) {
        reg("findBy");
        // Implementation skipped
        return null;
    }
}
