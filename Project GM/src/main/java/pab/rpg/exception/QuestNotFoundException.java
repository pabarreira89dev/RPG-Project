package pab.rpg.exception;

public class QuestNotFoundException extends RuntimeException {

    public QuestNotFoundException(String questCode) {
        super("Quest " + questCode + " was not found");
    }
}
