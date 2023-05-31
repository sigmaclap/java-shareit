package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;


    @Override
    public List<ItemDtoWithBooking> getAllItems(Long userId) {
        List<Item> itemsUserOwner = repository.findAllByOwner_IdOrderByIdAsc(userId);
        List<ItemDtoWithBooking> listItemDtoWithBooking = new ArrayList<>();
        for (Item item : itemsUserOwner) {
            ItemDtoWithBooking itemDtoWithBooking = getItemDtoWithBooking(getCommentListByUser(userId), item);
            listItemDtoWithBooking.add(itemDtoWithBooking);
        }
        return listItemDtoWithBooking;
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

    @Override
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

    private List<CommentDto> getCommentListByItem(Long itemId) {
        List<CommentDto> commentList = commentRepository.findCommentsByItem_Id(itemId).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (commentList.isEmpty()) {
            commentList = Collections.emptyList();
        }
        return commentList;
    }

    private ItemDtoWithBooking getItemDtoWithBooking(List<CommentDto> commentList, Item item) {
        ItemDtoWithBooking itemDtoWithBooking = itemMapper.toItemDtoBooking(item, commentList);
        Optional<Booking> lastB = bookingRepository
                .findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(item.getId(), LocalDateTime.now());
        Optional<Booking> nextB = bookingRepository
                .findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(item.getId(), LocalDateTime.now());
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


    @Override
    public Item createItem(Long userId, Item item) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id not found"));
        item.setOwner(user);
        return repository.save(item);
    }

    @Override
    public Item updateItem(Long userId, Item item, Long itemId) {
        validationDataForUpdateItem(item, itemId);
        if (isCheckOwnerItem(userId, itemId)) {
            item.setId(itemId);
            item.setOwner(userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with id not found")));
            repository.save(item);
        }
        return repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id not"));
    }

    @Override
    public List<Item> searchItemForText(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return repository.searchItemForText(text);
    }

    @Override
    public Comment createComment(Long userId, Comment comment, Long itemId) {
        LocalDateTime createdDate = comment.getCreatedDate();
        if (isCheckItemExistsByRenter(itemId, userId, createdDate)) {
            comment.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with id " + userId + "not found")));
            comment.setItem(repository.findById(itemId)
                    .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + "not found")));
            return commentRepository.save(comment);
        } else {
            log.error("Could not find item for this renter");
            throw new ItemNotFoundException("Could not find item for this renter");
        }
    }

    private boolean isCheckItemExistsByRenter(Long itemId, Long userId, LocalDateTime createdDate) {
        List<Booking> listBooking = bookingRepository.findBookingsByBooker_IdOrderByIdAsc(userId);
        return listBooking.stream()
                .filter(status -> status.getStatus().equals(StatusBooking.APPROVED))
                .filter(data -> data.getEndDate().isBefore(createdDate))
                .filter(user -> user.getBooker().getId().equals(userId))
                .map(Booking::getItem)
                .filter(itemQ -> itemQ.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InvalidDataException("Item not found.")).getAvailable();
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
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }
}
