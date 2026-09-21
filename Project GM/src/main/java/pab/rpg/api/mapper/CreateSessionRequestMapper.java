package pab.rpg.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pab.rpg.api.dto.CreateGameSessionCommand;
import pab.rpg.api.dto.request.CreateSessionRequest;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CreateSessionRequestMapper {

    @Mapping(target = "playerId", source = "playerId")
    CreateGameSessionCommand toCommand(CreateSessionRequest request, UUID playerId);
}
