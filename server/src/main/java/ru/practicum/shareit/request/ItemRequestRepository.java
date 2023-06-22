package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.entity.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequester_IdOrderByCreatedDesc(Long ownerId);

    List<ItemRequest> findAllByRequester_IdNotOrderByCreatedDesc(Long ownerId);

    Page<ItemRequest> findAllByRequester_IdNotOrderByCreatedDesc(Long ownerId, Pageable pageable);
}
