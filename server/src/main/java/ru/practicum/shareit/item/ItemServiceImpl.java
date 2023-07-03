package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;


    @Override
    @Transactional
    public List<ItemDtoWithBooking> getAllItems(Long userId, Integer limit, Integer size) {
        List<Item> itemsUserOwner = repository
                .findAllByOwner_IdOrderByIdAsc(userId, PageRequest.of(limit / size, size))
                .getContent();
        List<ItemDtoWithBooking> listItemDtoWithBooking = new ArrayList<>();
        for (Item item : itemsUserOwner) {
            ItemDtoWithBooking itemDtoWithBooking = getItemDtoWithBooking(getCommentListByUser(userId), item);
            listItemDtoWithBooking.add(itemDtoWithBooking);
        }
        return listItemDtoWithBooking;
    }

    @Override
    @Transactional
    public ItemDtoWithBooking getItemById(Long itemId, Long userId) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id"));
        User userOwner = item.getOwner();
        if (userId.equals(userOwner.getId())) {
            return getItemDtoWithBooking(getCommentListByItem(itemId), item);
        } else {
            return itemMapper.toItemDtoBooking(item, getCommentListByItem(itemId));
        }
    }

    @Override
    @Transactional
    public Item createItem(Long userId, Item item) {
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id not found")));
        if (item.getItemRequest() != null) {
            item.setItemRequest(itemRequestRepository.getReferenceById(item.getItemRequest().getId()));
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public Item updateItem(Long userId, Item item, Long itemId) {
        validationDataForUpdateItem(item, itemId);
        if (isCheckOwnerItem(userId, itemId)) {
            item.setId(itemId);
            item.setOwner(userRepository.getReferenceById(userId));
            repository.save(item);
        }
        return repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id not"));
    }

    @Override
    public List<Item> searchItemForText(String text, Integer limit, Integer size) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return repository.searchItemForText(text, PageRequest.of(limit / size, size)).getContent();
    }

    @Override
    public Comment createComment(Long userId, Comment comment, Long itemId) {
        LocalDateTime createdDate = comment.getCreatedDate();
        if (isCheckItemExistsByRenter(itemId, userId, createdDate)) {
            comment.setUser(userRepository.getReferenceById(userId));
            comment.setItem(repository.getReferenceById(itemId));
            return commentRepository.save(comment);
        } else {
            log.error("Could not find item for this renter");
            throw new InvalidDataException("Could not find item for this renter");
        }
    }

    private boolean isCheckItemExistsByRenter(Long itemId, Long userId, LocalDateTime createdDate) {
        List<Booking> listBooking = bookingRepository.findBookingsByBooker_IdOrderByIdAsc(userId);
        return listBooking.stream()
                .filter(status -> status.getStatus().equals(StatusBooking.APPROVED))
                .filter(data -> data.getEndDate().isBefore(createdDate))
                .filter(user -> user.getBooker().getId().equals(userId))
                .map(Booking::getItem)
                .anyMatch(itemQ -> itemQ.getId().equals(itemId));
    }

    private void validationDataForUpdateItem(Item item, Long itemId) {
        if (item.getName() == null) {
            item.setName(getItemById(itemId).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(getItemById(itemId).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(getItemById(itemId).getAvailable());
        }
    }

    private boolean isCheckOwnerItem(Long userId, Long itemId) {
        return getItemById(itemId).getOwner().equals(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Owner not found - update can't be finished")));
    }

    private ItemDtoWithBooking getItemDtoWithBooking(List<CommentDto> commentList, Item item) {
        LocalDateTime currentTime = LocalDateTime.now();
        ItemDtoWithBooking itemDtoWithBooking = itemMapper.toItemDtoBooking(item, commentList);
        Optional<Booking> lastB = bookingRepository
                .findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(item.getId(), currentTime);
        Optional<Booking> nextB = bookingRepository
                .findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(item.getId(), currentTime);
        if (lastB.isEmpty()) {
            itemDtoWithBooking.setLastBooking(null);
            itemDtoWithBooking.setNextBooking(null);
        } else if (nextB.isEmpty()) {
            itemDtoWithBooking.setLastBooking(bookingMapper.toBookingOwnerDto(lastB.get()));
        } else {
            itemDtoWithBooking.setLastBooking(bookingMapper.toBookingOwnerDto(lastB.get()));
            itemDtoWithBooking.setNextBooking(bookingMapper.toBookingOwnerDto(nextB.get()));
        }
        return itemDtoWithBooking;
    }


    private Item getItemById(Long itemId) {
        return repository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Item not found with"));
    }

    private List<CommentDto> getCommentListByItem(Long itemId) {
        List<CommentDto> commentList = commentRepository.findCommentsByItem_Id(itemId).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (commentList.isEmpty()) {
            commentList = Collections.emptyList();
        }
        return commentList;
    }

    private List<CommentDto> getCommentListByUser(Long userId) {
        List<CommentDto> commentList = commentRepository.findCommentsByUser_Id(userId).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (commentList.isEmpty()) {
            commentList = Collections.emptyList();
        }
        return commentList;
    }
}