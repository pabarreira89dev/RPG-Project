package pab.rpg.auth.service;

import pab.rpg.auth.domain.AppUser;

public interface AppUserService {

    AppUser register(String username, String rawPassword, String email);
}
