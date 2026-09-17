package pab.rpg.api.mapper;

import org.mapstruct.Mapper;
import pab.rpg.api.dto.CreateGameSessionCommand;
import pab.rpg.api.dto.request.CreateSessionRequest;

@Mapper(componentModel = "spring")
public interface CreateSessionRequestMapper {

    CreateGameSessionCommand toCommand(CreateSessionRequest request);
}
