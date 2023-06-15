package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(" SELECT i FROM Item i " +
            "WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%')) " +
            " OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%'))) AND i.available != FALSE")
    List<Item> searchItemForText(String text);

    @Query(" SELECT i FROM Item i " +
            "WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%')) " +
            " OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%'))) AND i.available != FALSE")
    Page<Item> searchItemForText(String text, Pageable pageable);

    List<Item> findAllByOwner_IdOrderByIdAsc(Long ownerId);

    Page<Item> findAllByOwner_IdOrderByIdAsc(Long ownerId, Pageable pageable);

    List<Item> findAllByItemRequest_Id(Long requestId);
}
