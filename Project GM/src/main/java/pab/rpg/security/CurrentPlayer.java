package pab.rpg.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Resolves the authenticated player's id from the security context (JWT subject or dev identity);
// controllers must never accept playerId from the request body/query (TDD MVP v0.2 §12).
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentPlayer {
}
