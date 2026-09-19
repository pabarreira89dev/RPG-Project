SELECT * FROM combat;
SELECT * FROM combat_participant;
SELECT * FROM game_event;
SELECT * FROM game_session;
SELECT * FROM location;
SELECT * FROM npc;
SELECT * FROM npc_knowledge_fact;
SELECT * FROM player_character;
SELECT * FROM processed_action;
SELECT * FROM quest;
SELECT * FROM quest_stage;
SELECT * FROM quest_stage_transition;
SELECT * FROM quest_state;
SELECT * FROM relationship;

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;