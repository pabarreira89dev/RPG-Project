package pab.rpg.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.item.Item;
import pab.rpg.domain.item.ItemTemplate;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.repository.ItemRepository;
import pab.rpg.domain.repository.ItemTemplateRepository;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.exception.ItemNotAllowedException;
import pab.rpg.exception.ItemNotFoundException;
import pab.rpg.service.GameEventService;
import pab.rpg.service.ItemService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// World items a new session starts with (V7 seed template codes/locations), lying uncollected until picked up.
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private static final List<SeedEntry> SEED_ITEMS = List.of(
            new SeedEntry("rusty_dagger", "forest_edge", 1),
            new SeedEntry("healing_herbs", "forest_edge", 2),
            new SeedEntry("old_coin_pouch", "tavern", 1)
    );

    private final ItemRepository itemRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final LocationRepository locationRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameEventService gameEventService;

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> getInventory(UUID sessionId) {
        GameSession session = requireSession(sessionId);
        return itemRepository.findAllBySessionIdAndOwnerId(sessionId, session.getCharacter().getId()).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> getItemsAtLocation(UUID sessionId, UUID locationId) {
        return itemRepository.findAllBySessionIdAndLocationId(sessionId, locationId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public ItemView pickUpItem(UUID sessionId, UUID itemId) {
        GameSession session = requireSession(sessionId);
        Item item = itemRepository.findByIdAndSessionId(itemId, sessionId)
                .orElseThrow(() -> new ItemNotFoundException("El objeto " + itemId + " no existe en esta partida."));

        if (item.getLocationId() == null || !item.getLocationId().equals(session.getCurrentLocationId())) {
            throw new ItemNotAllowedException("Ese objeto no está en tu localización actual.");
        }

        item.pickUp(session.getCharacter().getId());
        item = itemRepository.save(item);

        ItemTemplate template = requireTemplate(item.getTemplateId());
        gameEventService.append(sessionId, "ITEM_ACQUIRED", session.getPlayerId(),
                Map.of("itemId", item.getId(), "templateCode", template.getCode(), "quantity", item.getQuantity()),
                session.getWorldTime());

        return toView(item);
    }

    @Override
    public void seedInitialItems(UUID sessionId) {
        for (SeedEntry seed : SEED_ITEMS) {
            ItemTemplate template = itemTemplateRepository.findByCode(seed.templateCode())
                    .orElseThrow(() -> new IllegalStateException("Missing seeded item template: " + seed.templateCode()));
            Location location = locationRepository.findByCode(seed.locationCode())
                    .orElseThrow(() -> new IllegalStateException("Missing seeded location: " + seed.locationCode()));

            itemRepository.save(new Item(null, sessionId, template.getId(), null, location.getId(), seed.quantity(), null));
        }
    }

    private GameSession requireSession(UUID sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Session must already be validated by the caller"));
    }

    private ItemTemplate requireTemplate(UUID templateId) {
        return itemTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalStateException("Item points to a missing template"));
    }

    private ItemView toView(Item item) {
        ItemTemplate template = requireTemplate(item.getTemplateId());
        return new ItemView(item.getId(), template.getCode(), template.getName(), template.getDescription(),
                item.getQuantity(), item.getDurability());
    }

    private record SeedEntry(String templateCode, String locationCode, int quantity) {
    }
}
