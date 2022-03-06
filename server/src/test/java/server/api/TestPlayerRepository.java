package server.api;

import commons.Player;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import server.database.PlayerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TestPlayerRepository implements PlayerRepository {

    public final List<Player> players = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<Player> findAll() {
        calledMethods.add("findAll");
        return players;
    }

    @Override
    public List<Player> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pageable
     * @return a page of entities
     */
    @Override
    public Page<Player> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Player> findAllById(Iterable<Long> longs) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Deletes the entity with the given id.
     *
     * @param aLong must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
     */
    @Override
    public void deleteById(Long aLong) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes a given entity.
     *
     * @param entity must not be {@literal null}.
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    public void delete(Player entity) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes all instances of the type {@code T} with the given IDs.
     *
     * @param longs must not be {@literal null}. Must not contain {@literal null} elements.
     * @throws IllegalArgumentException in case the given {@literal ids} or one of its elements is {@literal null}.
     * @since 2.5
     */
    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes the given entities.
     *
     * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
     * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}.
     */
    @Override
    public void deleteAll(Iterable<? extends Player> entities) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes all entities managed by the repository.
     */
    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub
    }

    /**
     * @param entity
     * @param <S>
     * @return
     */
    private <S extends Player> boolean replaceIfExists(S entity) {
        int idx = -1;
        for (int i = 0; i < players.size(); ++i) {
            if (players.get(i).id == entity.id) {
                idx = i;
                break;
            }
        }

        if (idx == -1) return false;
        players.set(idx, entity);
        return true;
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed
     * the entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity; will never be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
     */
    @Override
    public <S extends Player> S save(S entity) {
        call("save");
        if (entity.id != 0 && replaceIfExists(entity)) return entity;
        entity.id = (long) players.size() + 1;
        players.add(entity);
        return entity;
    }

    @Override
    public <S extends Player> List<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    @Override
    public Optional<Player> findById(Long id) {
        return find(id);
    }

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param aLong must not be {@literal null}.
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    @Override
    public boolean existsById(Long aLong) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Flushes all pending changes to the database.
     */
    @Override
    public void flush() {
        // TODO Auto-generated method stub
    }

    /**
     * Saves an entity and flushes changes instantly.
     *
     * @param entity entity to be saved. Must not be {@literal null}.
     * @return the saved entity
     */
    @Override
    public <S extends Player> S saveAndFlush(S entity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Saves all entities and flushes changes instantly.
     *
     * @param entities entities to be saved. Must not be {@literal null}.
     * @return the saved entities
     * @since 2.5
     */
    @Override
    public <S extends Player> List<S> saveAllAndFlush(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities entities to be deleted. Must not be {@literal null}.
     * @since 2.5
     */
    @Override
    public void deleteAllInBatch(Iterable<Player> entities) {
        // TODO Auto-generated method stub
    }

    /**
     * @param longs the ids of the entities to be deleted. Must not be {@literal null}.
     * @since 2.5
     */
    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes all entities in a batch call.
     */
    @Override
    public void deleteAllInBatch() {
        // TODO Auto-generated method stub
    }

    /**
     * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
     * implemented this is very likely to always return an instance and throw an
     * {@link EntityNotFoundException} on first access. Some of them will reject invalid identifiers
     * immediately.
     *
     * @param aLong must not be {@literal null}.
     * @return a reference to the entity with the given identifier.
     */
    @Override
    public Player getOne(Long aLong) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
     * implemented this is very likely to always return an instance and throw an
     * {@link EntityNotFoundException} on first access. Some of them will reject invalid identifiers
     * immediately.
     *
     * @param aLong must not be {@literal null}.
     * @return a reference to the entity with the given identifier.
     */
    @Override
    public Player getById(Long aLong) {
        // TODO Auto-generated method stub
        return null;
    }

    private Optional<Player> find(Long id) {
        return players.stream().filter(q -> q.id == id).findFirst();
    }

    /**
     * Returns a single entity matching the given {@link Example} or {@link Optional#empty()} if none was found.
     *
     * @param example must not be {@literal null}.
     * @return a single entity matching the given {@link Example} or {@link Optional#empty()} if none was found.
     * @throws IncorrectResultSizeDataAccessException if the Example yields more than one result.
     */
    @Override
    public <S extends Player> Optional<S> findOne(Example<S> example) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public <S extends Player> List<S> findAll(Example<S> example) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <S extends Player> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns a {@link Page} of entities matching the given {@link Example}. In case no match could be found, an empty
     * {@link Page} is returned.
     *
     * @param example  must not be {@literal null}.
     * @param pageable can be {@literal null}.
     * @return a {@link Page} of entities matching the given {@link Example}.
     */
    @Override
    public <S extends Player> Page<S> findAll(Example<S> example, Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the number of instances matching the given {@link Example}.
     *
     * @param example the {@link Example} to count instances for. Must not be {@literal null}.
     * @return the number of instances matching the {@link Example}.
     */
    @Override
    public <S extends Player> long count(Example<S> example) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Checks whether the data store contains elements that match the given {@link Example}.
     *
     * @param example the {@link Example} to use for the existence check. Must not be {@literal null}.
     * @return {@literal true} if the data store contains elements that match the given {@link Example}.
     */
    @Override
    public <S extends Player> boolean exists(Example<S> example) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Returns entities matching the given {@link Example} applying the {@link Function queryFunction} that defines the
     * query and its result type.
     *
     * @param example       must not be {@literal null}.
     * @param queryFunction the query function defining projection, sorting, and the result type
     */
    @Override
    public <S extends Player, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R>
            queryFunction) {
        // TODO Auto-generated method stub
        return null;
    }
}
