package pab.rpg.domain.rules;

// A rule the Game Engine enforces before resolving an action; must throw ActionNotAllowedException to reject.
public interface GameRule {

    void check(ActionContext context);
}
