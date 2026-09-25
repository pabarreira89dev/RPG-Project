package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.item.Item;
import pab.rpg.domain.item.ItemTemplate;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.repository.ItemRepository;
import pab.rpg.domain.repository.ItemTemplateRepository;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.exception.ItemNotAllowedException;
import pab.rpg.exception.ItemNotFoundException;
import pab.rpg.service.GameEventService;
import pab.rpg.service.ItemService.ItemView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemTemplateRepository itemTemplateRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private GameEventService gameEventService;

    private ItemServiceImpl service() {
        return new ItemServiceImpl(itemRepository, itemTemplateRepository, locationRepository, gameSessionRepository, gameEventService);
    }

    private GameSession session(UUID id, UUID characterId, UUID locationId) {
        Character character = new Character(
                characterId, "Aren", 1, 0,
                new AttributeSet(10, 10, 10, 10, 10, 10),
                new HealthState(20, 20, 0)
        );
        return new GameSession(
                id, UUID.randomUUID(), UUID.randomUUID(), locationId,
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character, null
        );
    }

    @Test
    void getInventoryReturnsItemsOwnedByCharacter() {
        UUID sessionId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        GameSession session = session(sessionId, characterId, UUID.randomUUID());
        UUID templateId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), sessionId, templateId, characterId, null, 1, null);
        ItemTemplate template = new ItemTemplate(templateId, "rusty_dagger", "Daga oxidada", "desc");

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findAllBySessionIdAndOwnerId(sessionId, characterId)).thenReturn(List.of(item));
        when(itemTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        List<ItemView> result = service().getInventory(sessionId);

        assertEquals(1, result.size());
        assertEquals("rusty_dagger", result.get(0).templateCode());
    }

    @Test
    void pickUpItemMarksItemAsOwnedAndAppendsEvent() {
        UUID sessionId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        GameSession session = session(sessionId, characterId, locationId);
        Item item = new Item(itemId, sessionId, templateId, null, locationId, 1, null);
        ItemTemplate template = new ItemTemplate(templateId, "rusty_dagger", "Daga oxidada", "desc");

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findByIdAndSessionId(itemId, sessionId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        ItemView result = service().pickUpItem(sessionId, itemId);

        assertEquals("rusty_dagger", result.templateCode());
        assertEquals(characterId, item.getOwnerId());
        assertEquals(null, item.getLocationId());
        verify(gameEventService).append(any(), any(), any(), any(), any());
    }

    @Test
    void pickUpItemThrowsWhenItemDoesNotExist() {
        UUID sessionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        GameSession session = session(sessionId, UUID.randomUUID(), UUID.randomUUID());

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findByIdAndSessionId(itemId, sessionId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> service().pickUpItem(sessionId, itemId));
    }

    @Test
    void pickUpItemThrowsWhenItemIsNotAtCurrentLocation() {
        UUID sessionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID otherLocationId = UUID.randomUUID();
        GameSession session = session(sessionId, UUID.randomUUID(), currentLocationId);
        Item item = new Item(itemId, sessionId, UUID.randomUUID(), null, otherLocationId, 1, null);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findByIdAndSessionId(itemId, sessionId)).thenReturn(Optional.of(item));

        assertThrows(ItemNotAllowedException.class, () -> service().pickUpItem(sessionId, itemId));
    }

    @Test
    void pickUpItemThrowsWhenItemIsAlreadyOwned() {
        UUID sessionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        GameSession session = session(sessionId, UUID.randomUUID(), currentLocationId);
        Item item = new Item(itemId, sessionId, UUID.randomUUID(), UUID.randomUUID(), null, 1, null);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(itemRepository.findByIdAndSessionId(itemId, sessionId)).thenReturn(Optional.of(item));

        assertThrows(ItemNotAllowedException.class, () -> service().pickUpItem(sessionId, itemId));
    }

    @Test
    void seedInitialItemsSavesEachSeedEntryAtItsLocation() {
        UUID sessionId = UUID.randomUUID();
        ItemTemplate daggerTemplate = new ItemTemplate(UUID.randomUUID(), "rusty_dagger", "Daga oxidada", "desc");
        ItemTemplate herbsTemplate = new ItemTemplate(UUID.randomUUID(), "healing_herbs", "Hierbas curativas", "desc");
        ItemTemplate pouchTemplate = new ItemTemplate(UUID.randomUUID(), "old_coin_pouch", "Bolsa de monedas antiguas", "desc");
        Location forestEdge = new Location(UUID.randomUUID(), "forest_edge", "Lindero del Bosque", "desc");
        Location tavern = new Location(UUID.randomUUID(), "tavern", "Taberna", "desc");

        when(itemTemplateRepository.findByCode("rusty_dagger")).thenReturn(Optional.of(daggerTemplate));
        when(itemTemplateRepository.findByCode("healing_herbs")).thenReturn(Optional.of(herbsTemplate));
        when(itemTemplateRepository.findByCode("old_coin_pouch")).thenReturn(Optional.of(pouchTemplate));
        when(locationRepository.findByCode("forest_edge")).thenReturn(Optional.of(forestEdge));
        when(locationRepository.findByCode("tavern")).thenReturn(Optional.of(tavern));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service().seedInitialItems(sessionId);

        verify(itemRepository, org.mockito.Mockito.times(3)).save(any());
    }
}
