package pab.rpg.service;

import pab.rpg.api.dto.SubmitActionCommand;
import pab.rpg.api.dto.response.ActionResponse;

public interface ActionService {

    ActionResponse submitAction(SubmitActionCommand command);
}
