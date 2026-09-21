package net.stalzone.emulator.world.props;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link StalkerStreamCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/stalker/} folder's 3 large sibling audio/music
 * trees swept together in part 94 (676 files total), live-verified on device: complete real listings for
 * every folder EXCEPT 3 very large, flatly-named ones, which use representative subsets following part
 * 92's {@link StalkerSoundsCatalogTest} precedent -- {@code stream/ambience/} (188 real files, flat),
 * {@code streamrandom/frontline/calldown/} (116 real loose files, flat), and {@code
 * music/bossfight/shamanfight/} (54 real files, flat). Only real game-content/asset names are used, per
 * this project's privacy rule -- never a real player username.
 */
public final class StalkerStreamCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // ===== stream/ (331 real files total) =====

            // stream/ambience/ -- representative subset of the real 188-file, flat folder
            "stream/ambience/sc_ambient_mg_1.ogg",
            "stream/ambience/sc_ambient_mg_2.ogg",
            "stream/ambience/sc_ambient_pd_1.ogg",
            "stream/ambience/sc_ambient_td_1.ogg",
            "stream/ambience/sc_ambient_agroprom_1.ogg",
            "stream/ambience/sc_ambient_bolota_1.ogg",
            "stream/ambience/sc_ambient_lubech_1.ogg",
            "stream/ambience/sc_ambient_mogilnik_1.ogg",
            "stream/ambience/sc_ambient_obochina_1.ogg",
            "stream/ambience/sc_ambient_pervomayskiy_1.ogg",
            "stream/ambience/sc_ambient_polesskoe_1.ogg",
            "stream/ambience/sc_ambient_radar_1.ogg",
            "stream/ambience/sc_ambient_reichstag_1.ogg",
            "stream/ambience/sc_ambient_reichstag_2.ogg",
            "stream/ambience/sc_ambient_safezone_1.ogg",
            "stream/ambience/sc_ambient_safezone_18.ogg",
            "stream/ambience/sc_ambient_school_1.ogg",
            "stream/ambience/sc_ambient_svalkatehniki_1.ogg",
            "stream/ambience/sc_ambient_yantar_1.ogg",
            "stream/ambience/sc_ambient_zaton_1.ogg",
            "stream/ambience/sc_ambient_vagant_1.ogg",
            "stream/ambience/sc_ambient_vagant_end.ogg",
            "stream/ambience/sc_ambient_vlasov_bossfight.ogg",
            "stream/ambience/insideout_areal_1.ogg",
            "stream/ambience/iznanka_areal_1.ogg",
            "stream/ambience/sc_vynil_nyguitar_1.ogg",
            "stream/ambience/sc_vynil_nyguitar_10.ogg",
            "stream/ambience/sc_light_ambience_1.ogg",
            "stream/ambience/nywind_1.ogg",
            "stream/ambience/underworld_one.ogg",
            "stream/ambience/underworld_four.ogg",
            "stream/ambience/sc_ambient_silence.ogg",
            "stream/ambience/sc_ambient_zero.ogg",
            "stream/ambience/anomalous_tunnel.ogg",
            "stream/ambience/battlefield_ship.ogg",
            "stream/ambience/dungeon_morok_bar.ogg",
            "stream/ambience/wind_desolate.ogg",

            // stream/attackonperimeter/ -- the complete real 3-file folder
            "stream/attackonperimeter/Stalcraft_Pavl_May_attack_on_perimeter_track_1.ogg",
            "stream/attackonperimeter/Stalcraft_Pavl_May_attack_on_perimeter_track_2.ogg",
            "stream/attackonperimeter/Stalcraft_main_menu_2025_attack_on_perimeter.ogg",

            // stream/blizzard/ -- the complete real 10-file folder
            "stream/blizzard/blizzard_07_00.ogg",
            "stream/blizzard/blizzard_08_01.ogg",
            "stream/blizzard/blizzard_09_02.ogg",
            "stream/blizzard/blizzard_11_04.ogg",
            "stream/blizzard/blizzard_13_03.ogg",
            "stream/blizzard/buran_track_0.ogg",
            "stream/blizzard/buran_track_1.ogg",
            "stream/blizzard/buran_track_2.ogg",
            "stream/blizzard/buran_track_3.ogg",
            "stream/blizzard/buran_track_4.ogg",

            // stream/bossfight/ -- the complete real 4-file folder
            "stream/bossfight/morg1.ogg",
            "stream/bossfight/morg2.ogg",
            "stream/bossfight/shaman1.ogg",
            "stream/bossfight/shaman2.ogg",

            // stream/country/ -- the complete real 5-file folder
            "stream/country/amazing_grace.ogg",
            "stream/country/american_country.ogg",
            "stream/country/this_land_is_your_land.ogg",
            "stream/country/true_country_american_blues.ogg",
            "stream/country/us_national_anthem.ogg",

            // stream/dmz/ -- the complete real 5-file folder
            "stream/dmz/Stalcraft_main_menu_2024_black_raid.ogg",
            "stream/dmz/dmz_fight_music_1.ogg",
            "stream/dmz/dmz_fight_music_2.ogg",
            "stream/dmz/storm_troopers_main_menu.ogg",
            "stream/dmz/storm_troopers_phonograph.ogg",

            // stream/fools_day/ -- the complete real 2-file folder
            "stream/fools_day/april_session_intro.ogg",
            "stream/fools_day/april_session_music.ogg",

            // stream/gribnaya_bylina/ -- the complete real 3-file folder
            "stream/gribnaya_bylina/gribnaya_bylina_1.ogg",
            "stream/gribnaya_bylina/gribnaya_bylina_2.ogg",
            "stream/gribnaya_bylina/gribnaya_bylina_3.ogg",

            // stream/grindanilov/ -- the complete real 3-file folder
            "stream/grindanilov/Grin_Danilov_moi_dozhd.ogg",
            "stream/grindanilov/Grin_Danilov_na_reke.ogg",
            "stream/grindanilov/Grin_Danilov_vi_vse_odinakovie.ogg",

            // stream/incident/ -- the complete real 12-file folder
            "stream/incident/announcements/announcement_1.ogg",
            "stream/incident/announcements/announcement_2.ogg",
            "stream/incident/announcements/announcement_3.ogg",
            "stream/incident/announcements/announcement_4.ogg",
            "stream/incident/announcements/announcement_5.ogg",
            "stream/incident/announcements/announcement_6.ogg",
            "stream/incident/announcements/announcement_7.ogg",
            "stream/incident/announcements/announcement_8.ogg",
            "stream/incident/announcements/announcement_9.ogg",
            "stream/incident/announcements/announcement_10.ogg",
            "stream/incident/lab_past_sc.ogg",
            "stream/incident/lab_present_sc.ogg",

            // stream/insideout/ -- the complete real 2-file folder
            "stream/insideout/Stalcraft_main_menu_2025_inside_out.ogg",
            "stream/insideout/insideout_bubble_out.ogg",

            // stream/main/ -- the complete real 5-file folder
            "stream/main/Ambient1.ogg",
            "stream/main/Ambient4.ogg",
            "stream/main/Ambient7.ogg",
            "stream/main/Ambient8.ogg",
            "stream/main/Ambient9.ogg",

            // stream/newyear/ -- the complete real 1-file folder
            "stream/newyear/new_year_menu_guitar.ogg",

            // stream/operations/ -- the complete real 3-file folder
            "stream/operations/breach_operation.ogg",
            "stream/operations/focus_operation.ogg",
            "stream/operations/sea_alienation_operation.ogg",

            // stream/ravent/ -- the complete real 33-file folder
            "stream/ravent/Stalcraft_main_menu_2024_arena.ogg",
            "stream/ravent/arena_ability.ogg",
            "stream/ravent/arena_boss_music1.ogg",
            "stream/ravent/arena_boss_music2.ogg",
            "stream/ravent/arena_duel_music.ogg",
            "stream/ravent/arena_music_01.ogg",
            "stream/ravent/arena_music_02.ogg",
            "stream/ravent/arena_music_03.ogg",
            "stream/ravent/arena_music_04.ogg",
            "stream/ravent/arena_prepare_music.ogg",
            "stream/ravent/arena_pve_music1.ogg",
            "stream/ravent/arena_pve_music2.ogg",
            "stream/ravent/arena_pve_music3.ogg",
            "stream/ravent/arena_pvp_music.ogg",
            "stream/ravent/desktop.ini",
            "stream/ravent/discoball_hit1.ogg",
            "stream/ravent/discoball_hit2.ogg",
            "stream/ravent/discoball_hit3.ogg",
            "stream/ravent/discoball_hit4.ogg",
            "stream/ravent/discoball_hit5.ogg",
            "stream/ravent/discoball_hit6.ogg",
            "stream/ravent/pvp_lose.ogg",
            "stream/ravent/pvp_win.ogg",
            "stream/ravent/round_lose.ogg",
            "stream/ravent/round_win.ogg",
            "stream/ravent/shop_block.ogg",
            "stream/ravent/shop_draft.ogg",
            "stream/ravent/shop_sell.ogg",
            "stream/ravent/shop_shuffle.ogg",
            "stream/ravent/shop_update.ogg",
            "stream/ravent/shot_buying.ogg",
            "stream/ravent/wave_begins.ogg",
            "stream/ravent/wave_ends.ogg",

            // stream/ravent_quest/ -- the complete real 3-file folder
            "stream/ravent_quest/arena_quest_boss_music.ogg",
            "stream/ravent_quest/arena_quest_music1.ogg",
            "stream/ravent_quest/arena_quest_music2.ogg",

            // stream/records/ -- the complete real 1-file folder
            "stream/records/sample_radio_chatter.ogg",

            // stream/reichstag/ -- the complete real 2-file folder
            "stream/reichstag/reichstag_sovet.ogg",
            "stream/reichstag/reichstag_wehrmacht.ogg",

            // stream/snowstorm/ -- the complete real 1-file folder
            "stream/snowstorm/background.ogg",

            // stream/sujet3/ -- the complete real 26-file folder
            "stream/sujet3/track10_zivcas_base.ogg",
            "stream/sujet3/track10_zivcas_base_calm.ogg",
            "stream/sujet3/track12_boss_fight.ogg",
            "stream/sujet3/track14_calm.ogg",
            "stream/sujet3/track15_twins_nightmare.ogg",
            "stream/sujet3/track15_twins_nightmare_middle.ogg",
            "stream/sujet3/track16_mountain.ogg",
            "stream/sujet3/track17_dunge_palach.ogg",
            "stream/sujet3/track18_mountain_calm.ogg",
            "stream/sujet3/track18_zavod.ogg",
            "stream/sujet3/track1_psikaban_active.ogg",
            "stream/sujet3/track1_psikaban_calm.ogg",
            "stream/sujet3/track20_kuznya11.ogg",
            "stream/sujet3/track2_norakabana_active.ogg",
            "stream/sujet3/track2_norakabana_calm.ogg",
            "stream/sujet3/track2_norakabana_middle.ogg",
            "stream/sujet3/track3_lodochnayastanciya_active.ogg",
            "stream/sujet3/track3_lodochnayastanciya_calm.ogg",
            "stream/sujet3/track3_lodochnayastanciya_palach.ogg",
            "stream/sujet3/track7_twins_fight.ogg",
            "stream/sujet3/track8_nightfight.ogg",
            "stream/sujet3/track9_zivcas_outpost.ogg",
            "stream/sujet3/track_05_mutants_fight_calm.ogg",
            "stream/sujet3/track_06_twins_dunge.ogg",
            "stream/sujet3/track_12_palach_fight_calm.ogg",
            "stream/sujet3/track_13_nina.ogg",

            // stream/terentiy/ -- the complete real 3-file folder
            "stream/terentiy/terentiy_track_01.ogg",
            "stream/terentiy/terentiy_track_02.ogg",
            "stream/terentiy/terentiy_track_03.ogg",

            // stream/undead/ -- the complete real 12-file folder
            "stream/undead/dictor/evacuation_warning.ogg",
            "stream/undead/dictor/humans_left_20_.ogg",
            "stream/undead/dictor/humans_left_half.ogg",
            "stream/undead/dictor/introduction.ogg",
            "stream/undead/dictor/team_mode_enabled.ogg",
            "stream/undead/music/evacuation.ogg",
            "stream/undead/music/game.ogg",
            "stream/undead/music/human_fight.ogg",
            "stream/undead/music/intro.ogg",
            "stream/undead/music/lobby.ogg",
            "stream/undead/music/zombie_fight.ogg",
            "stream/undead/undead_idle.ogg",

            // stream/ -- the complete real 4 loose root files
            "stream/Main Menu Background.ogg",
            "stream/Main Menu Music.ogg",
            "stream/blizzard_loop2.ogg",
            "stream/memagent.ogg",

            // ===== streamrandom/ (240 real files total) =====

            // streamrandom/frontline/briefing_n/ -- the complete real 10-file folder
            "streamrandom/frontline/briefing_n/0briefing.ogg",
            "streamrandom/frontline/briefing_n/1briefing.ogg",
            "streamrandom/frontline/briefing_n/2briefing.ogg",
            "streamrandom/frontline/briefing_n/3briefing.ogg",
            "streamrandom/frontline/briefing_n/4briefing.ogg",
            "streamrandom/frontline/briefing_n/5briefing.ogg",
            "streamrandom/frontline/briefing_n/6briefing.ogg",
            "streamrandom/frontline/briefing_n/7briefing.ogg",
            "streamrandom/frontline/briefing_n/8briefing.ogg",
            "streamrandom/frontline/briefing_n/9briefing.ogg",

            // streamrandom/frontline/briefing_n2/ -- the complete real 15-file folder (0-9, gap, 15-19)
            "streamrandom/frontline/briefing_n2/0briefing.ogg",
            "streamrandom/frontline/briefing_n2/1briefing.ogg",
            "streamrandom/frontline/briefing_n2/2briefing.ogg",
            "streamrandom/frontline/briefing_n2/3briefing.ogg",
            "streamrandom/frontline/briefing_n2/4briefing.ogg",
            "streamrandom/frontline/briefing_n2/5briefing.ogg",
            "streamrandom/frontline/briefing_n2/6briefing.ogg",
            "streamrandom/frontline/briefing_n2/7briefing.ogg",
            "streamrandom/frontline/briefing_n2/8briefing.ogg",
            "streamrandom/frontline/briefing_n2/9briefing.ogg",
            "streamrandom/frontline/briefing_n2/15briefing.ogg",
            "streamrandom/frontline/briefing_n2/16briefing.ogg",
            "streamrandom/frontline/briefing_n2/17briefing.ogg",
            "streamrandom/frontline/briefing_n2/18briefing.ogg",
            "streamrandom/frontline/briefing_n2/19briefing.ogg",

            // streamrandom/frontline/briefing_s/ -- the complete real 10-file folder
            "streamrandom/frontline/briefing_s/0briefing.ogg",
            "streamrandom/frontline/briefing_s/1briefing.ogg",
            "streamrandom/frontline/briefing_s/2briefing.ogg",
            "streamrandom/frontline/briefing_s/3briefing.ogg",
            "streamrandom/frontline/briefing_s/4briefing.ogg",
            "streamrandom/frontline/briefing_s/5briefing.ogg",
            "streamrandom/frontline/briefing_s/6briefing.ogg",
            "streamrandom/frontline/briefing_s/7briefing.ogg",
            "streamrandom/frontline/briefing_s/8briefing.ogg",
            "streamrandom/frontline/briefing_s/9briefing.ogg",

            // streamrandom/frontline/briefing_s2/ -- the complete real 14-file folder (0-9, gap, 15-18)
            "streamrandom/frontline/briefing_s2/0briefing.ogg",
            "streamrandom/frontline/briefing_s2/1briefing.ogg",
            "streamrandom/frontline/briefing_s2/2briefing.ogg",
            "streamrandom/frontline/briefing_s2/3briefing.ogg",
            "streamrandom/frontline/briefing_s2/4briefing.ogg",
            "streamrandom/frontline/briefing_s2/5briefing.ogg",
            "streamrandom/frontline/briefing_s2/6briefing.ogg",
            "streamrandom/frontline/briefing_s2/7briefing.ogg",
            "streamrandom/frontline/briefing_s2/8briefing.ogg",
            "streamrandom/frontline/briefing_s2/9briefing.ogg",
            "streamrandom/frontline/briefing_s2/15briefing.ogg",
            "streamrandom/frontline/briefing_s2/16briefing.ogg",
            "streamrandom/frontline/briefing_s2/17briefing.ogg",
            "streamrandom/frontline/briefing_s2/18briefing.ogg",

            // streamrandom/frontline/calldown/ -- representative subset of the real 116 loose files
            "streamrandom/frontline/calldown/calldown_voice_6p62_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_acacia_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_c24_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_c5_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_c8_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_fab100_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_flamethrower_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_grad_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_gvozdika_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_heal_decor_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_m249_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_pfm1_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_rbk250_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_rbk250_8.ogg",
            "streamrandom/frontline/calldown/calldown_voice_respawn_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_rg6_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_rpg7_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_shield_decor_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_station_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_supply_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_universal_1.ogg",
            "streamrandom/frontline/calldown/calldown_voice_zab100_1.ogg",

            // streamrandom/frontline/calldown/spsh/ -- the complete real 15-file folder
            "streamrandom/frontline/calldown/spsh/spsh_correct_call.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_correct_input.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_draw.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_holster.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_incorrect_call.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_incorrect_input.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_left1.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_left2.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_left3.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_right1.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_right2.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_input_right3.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_reset.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_signal_draw.ogg",
            "streamrandom/frontline/calldown/spsh/spsh_signal_holster.ogg",

            // streamrandom/frontline/calldown/walkietalkie/ -- the complete real 20-file folder
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_correct_call.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_correct_input.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_draw1.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_draw2.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_holster1.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_holster2.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_idle.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_incorrect_call.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_incorrect_input.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_left1.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_left2.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_left3.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_left4.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_right1.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_right2.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_right3.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_input_right4.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_laser_draw.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_laser_holster.ogg",
            "streamrandom/frontline/calldown/walkietalkie/walkietalkie_reset.ogg",

            // streamrandom/frontline/gui/ -- the complete real 8-file folder
            "streamrandom/frontline/gui/colonel_click.ogg",
            "streamrandom/frontline/gui/final_screen_appear.ogg",
            "streamrandom/frontline/gui/map_appear_first_landing.ogg",
            "streamrandom/frontline/gui/play_button_click.ogg",
            "streamrandom/frontline/gui/pointing_on_everything.ogg",
            "streamrandom/frontline/gui/search_game_button_click.ogg",
            "streamrandom/frontline/gui/stratagem_choosing.ogg",
            "streamrandom/frontline/gui/table_decor_click.ogg",

            // streamrandom/frontline/mission/ -- the complete real 6-file folder
            "streamrandom/frontline/mission/mission_finished1.ogg",
            "streamrandom/frontline/mission/mission_finished2.ogg",
            "streamrandom/frontline/mission/scenario_finished1.ogg",
            "streamrandom/frontline/mission/scenario_finished2.ogg",
            "streamrandom/frontline/mission/scenario_finished_during_evac1.ogg",
            "streamrandom/frontline/mission/scenario_finished_during_evac2.ogg",

            // streamrandom/dmz/gui/ -- the complete real 21-file folder
            "streamrandom/dmz/gui/click_decor_other.ogg",
            "streamrandom/dmz/gui/converting.ogg",
            "streamrandom/dmz/gui/final_screen.ogg",
            "streamrandom/dmz/gui/hover_adaptation_in.ogg",
            "streamrandom/dmz/gui/hover_adaptation_out.ogg",
            "streamrandom/dmz/gui/hover_case_in.ogg",
            "streamrandom/dmz/gui/hover_case_out.ogg",
            "streamrandom/dmz/gui/hover_equipment_in.ogg",
            "streamrandom/dmz/gui/hover_equipment_out.ogg",
            "streamrandom/dmz/gui/hover_fame_in.ogg",
            "streamrandom/dmz/gui/hover_fame_out.ogg",
            "streamrandom/dmz/gui/hover_hook_in.ogg",
            "streamrandom/dmz/gui/hover_hook_out.ogg",
            "streamrandom/dmz/gui/hover_other.ogg",
            "streamrandom/dmz/gui/spawn_point.ogg",
            "streamrandom/dmz/gui/start_find_game.ogg",
            "streamrandom/dmz/gui/starting_search.ogg",
            "streamrandom/dmz/gui/tab_dossier.ogg",
            "streamrandom/dmz/gui/tab_main.ogg",
            "streamrandom/dmz/gui/tab_task.ogg",
            "streamrandom/dmz/gui/upgrade.ogg",

            // streamrandom/nytree/ -- the complete real 5-file folder
            "streamrandom/nytree/nytree_music_level1.ogg",
            "streamrandom/nytree/nytree_music_level2.ogg",
            "streamrandom/nytree/nytree_music_level3.ogg",
            "streamrandom/nytree/nytree_music_level4.ogg",
            "streamrandom/nytree/nytree_music_level5.ogg",

            // ===== music/bossfight/ (105 real files total) =====

            // music/bossfight/morgfight/ -- the complete real 41-file folder (morg_* AND shaman_* lines)
            "music/bossfight/morgfight/morg_calling_ambulance_a.ogg",
            "music/bossfight/morgfight/morg_calling_ambulance_b.ogg",
            "music/bossfight/morgfight/morg_calling_ambulance_c.ogg",
            "music/bossfight/morgfight/morg_calling_ambulance_d.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_a.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_b.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_c.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_d.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_e.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_f.ogg",
            "music/bossfight/morgfight/morg_firing_bullets_g.ogg",
            "music/bossfight/morgfight/morg_firing_explosives_a.ogg",
            "music/bossfight/morgfight/morg_firing_explosives_b.ogg",
            "music/bossfight/morgfight/morg_firing_explosives_c.ogg",
            "music/bossfight/morgfight/morg_firing_explosives_d.ogg",
            "music/bossfight/morgfight/morg_remain_20pc_health.ogg",
            "music/bossfight/morgfight/morg_remain_40pc_health.ogg",
            "music/bossfight/morgfight/morg_using_cover_a.ogg",
            "music/bossfight/morgfight/morg_using_cover_b.ogg",
            "music/bossfight/morgfight/morg_using_cover_c.ogg",
            "music/bossfight/morgfight/shaman_bombers_warning_0.ogg",
            "music/bossfight/morgfight/shaman_bombers_warning_0_calm.ogg",
            "music/bossfight/morgfight/shaman_bombers_warning_1.ogg",
            "music/bossfight/morgfight/shaman_bombers_warning_2.ogg",
            "music/bossfight/morgfight/shaman_destroyed_1st_pillar_0.ogg",
            "music/bossfight/morgfight/shaman_destroyed_1st_pillar_0_slow.ogg",
            "music/bossfight/morgfight/shaman_destroyed_1st_pillar_1.ogg",
            "music/bossfight/morgfight/shaman_destroyed_2nd_pillar_0.ogg",
            "music/bossfight/morgfight/shaman_destroyed_2nd_pillar_1.ogg",
            "music/bossfight/morgfight/shaman_destroyed_2nd_pillar_2.ogg",
            "music/bossfight/morgfight/shaman_destroyed_3rd_pillar_0.ogg",
            "music/bossfight/morgfight/shaman_destroyed_3rd_pillar_0_fast.ogg",
            "music/bossfight/morgfight/shaman_destroyed_3rd_pillar_1.ogg",
            "music/bossfight/morgfight/shaman_destroyed_4th_pillar_0.ogg",
            "music/bossfight/morgfight/shaman_destroyed_4th_pillar_0_slow.ogg",
            "music/bossfight/morgfight/shaman_destroyed_4th_pillar_1.ogg",
            "music/bossfight/morgfight/shaman_destroyed_4th_pillar_1_fast.ogg",
            "music/bossfight/morgfight/shaman_destroyed_4th_pillar_2.ogg",
            "music/bossfight/morgfight/shaman_start_fight_0.ogg",
            "music/bossfight/morgfight/shaman_start_fight_1.ogg",
            "music/bossfight/morgfight/shaman_start_fight_2.ogg",

            // music/bossfight/shamanfight/ -- representative subset of the real 54-file folder (also mixes morg_* + shaman_*)
            "music/bossfight/shamanfight/morg_destroy_pillar.ogg",
            "music/bossfight/shamanfight/morg_marked_1st_pillar.ogg",
            "music/bossfight/shamanfight/morg_pay_attention.ogg",
            "music/bossfight/shamanfight/morg_start_fight.ogg",
            "music/bossfight/shamanfight/shaman_burning_threat_a_0.ogg",
            "music/bossfight/shamanfight/shaman_burning_threat_a_0_harsh.ogg",
            "music/bossfight/shamanfight/shaman_burning_threat_b_1.ogg",
            "music/bossfight/shamanfight/shaman_burning_threat_c_2.ogg",
            "music/bossfight/shamanfight/shaman_remain_10pc_health_0.ogg",
            "music/bossfight/shamanfight/shaman_remain_10pc_health_0_anguish.ogg",
            "music/bossfight/shamanfight/shaman_remain_10pc_health_0_anguish_fast.ogg",
            "music/bossfight/shamanfight/shaman_remain_40pc_health_0.ogg",
            "music/bossfight/shamanfight/shaman_remain_70pc_health_0.ogg",
            "music/bossfight/shamanfight/shaman_remain_70pc_health_2_short.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_a_0.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_b_1.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_c_2.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_d_0_harsh.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_d_1_calm.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_e_0.ogg",
            "music/bossfight/shamanfight/shaman_shield_broke_e_0_harsh.ogg",
            "music/bossfight/shamanfight/shaman_start_fight_0.ogg",
            "music/bossfight/shamanfight/shaman_start_fight_0_harsh.ogg",
            "music/bossfight/shamanfight/shaman_start_fight_1_fast.ogg",
            "music/bossfight/shamanfight/shaman_start_fight_2_scream.ogg",

            // music/bossfight/vlasov/ -- the complete real 10-file folder (filenames ARE the literal dialogue)
            "music/bossfight/vlasov/Вот тебе зараза.ogg",
            "music/bossfight/vlasov/Есть еще порох в пороховницах.ogg",
            "music/bossfight/vlasov/И это все что ты можешь.ogg",
            "music/bossfight/vlasov/Получи блаженный ублюдок.ogg",
            "music/bossfight/vlasov/Сдохнешь, блаженная падаль.ogg",
            "music/bossfight/vlasov/Ты не заслуживаешь ничего кроме смерти.ogg",
            "music/bossfight/vlasov/Ты умрешь блаженный.ogg",
            "music/bossfight/vlasov/Что не нравится.ogg",
            "music/bossfight/vlasov/Я отомщу тебе Я вам всем отомщу.ogg",
            "music/bossfight/vlasov/Я тебя уничтожу.ogg",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerStreamCatalog.load finds exactly the 3 known audio/music folders", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertEquals(3, catalog.totalFolderCount(), "folder count");
            for (String known : StalkerStreamCatalog.KNOWN_FOLDERS) {
                assertTrue(catalog.folderNames().contains(known), known + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: stream/ravent is the complete real 33-file Arena-of-Slavs audio folder", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertTrue(files.contains("ravent/wave_begins.ogg"), "wave_begins.ogg present");
            assertTrue(files.contains("ravent/discoball_hit6.ogg"), "discoball_hit6.ogg present");
            assertTrue(files.contains("ravent/desktop.ini"), "desktop.ini outlier present");
        }));

        out.add(test("filesIn: stream/undead is the complete real 12-file zombie-mode folder", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertTrue(files.contains("undead/dictor/evacuation_warning.ogg"), "dictor/evacuation_warning.ogg present");
            assertTrue(files.contains("undead/music/zombie_fight.ogg"), "music/zombie_fight.ogg present");
            assertTrue(files.contains("undead/undead_idle.ogg"), "undead_idle.ogg present");
        }));

        out.add(test("filesIn: stream/operations resolves all 3 real named operations", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertTrue(files.contains("operations/breach_operation.ogg"), "breach_operation.ogg");
            assertTrue(files.contains("operations/focus_operation.ogg"), "focus_operation.ogg");
            assertTrue(files.contains("operations/sea_alienation_operation.ogg"), "sea_alienation_operation.ogg");
        }));

        out.add(test("filesIn: stream root has the 4 loose outlier-named files", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertTrue(files.contains("Main Menu Background.ogg"), "\"Main Menu Background.ogg\" present");
            assertTrue(files.contains("Main Menu Music.ogg"), "\"Main Menu Music.ogg\" present");
            assertTrue(files.contains("memagent.ogg"), "memagent.ogg present");
        }));

        out.add(test("filesIn: streamrandom includes frontline's calldown, briefing, and dmz gui trees", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("streamrandom");
            assertTrue(files.contains("frontline/calldown/calldown_voice_grad_1.ogg"), "calldown_voice_grad_1.ogg");
            assertTrue(files.contains("frontline/calldown/spsh/spsh_draw.ogg"), "calldown/spsh/spsh_draw.ogg");
            assertTrue(files.contains("frontline/calldown/walkietalkie/walkietalkie_idle.ogg"), "calldown/walkietalkie/walkietalkie_idle.ogg");
            assertTrue(files.contains("frontline/briefing_n2/15briefing.ogg"), "briefing_n2/15briefing.ogg (post-gap track)");
            assertTrue(files.contains("dmz/gui/hover_fame_in.ogg"), "dmz/gui/hover_fame_in.ogg");
        }));

        out.add(test("filesIn: music resolves both bossfight phase folders and the vlasov taunt lines", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("music");
            assertTrue(files.contains("bossfight/morgfight/morg_firing_bullets_a.ogg"), "morgfight/morg_firing_bullets_a.ogg");
            assertTrue(files.contains("bossfight/morgfight/shaman_start_fight_0.ogg"), "morgfight/shaman_start_fight_0.ogg (cross-boss file in morgfight/)");
            assertTrue(files.contains("bossfight/shamanfight/morg_start_fight.ogg"), "shamanfight/morg_start_fight.ogg (cross-boss file in shamanfight/)");
            assertTrue(files.contains("bossfight/vlasov/Я тебя уничтожу.ogg"), "vlasov taunt line present");
        }));

        out.add(test("foldersContaining: mg/pd/td real zone-code ambience prefixes resolve under stream", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("sc_ambient_mg_").contains("stream"), "sc_ambient_mg_");
            assertTrue(catalog.foldersContaining("sc_ambient_pd_").contains("stream"), "sc_ambient_pd_");
            assertTrue(catalog.foldersContaining("sc_ambient_td_").contains("stream"), "sc_ambient_td_");
        }));

        out.add(test("foldersContaining: sujet3's zivcas/lodochnayastanciya/twins/palach/nina resolve under stream", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("zivcas").contains("stream"), "zivcas");
            assertTrue(catalog.foldersContaining("lodochnayastanciya").contains("stream"), "lodochnayastanciya");
            assertTrue(catalog.foldersContaining("twins").contains("stream"), "twins");
            assertTrue(catalog.foldersContaining("palach").contains("stream"), "palach");
            assertTrue(catalog.foldersContaining("nina").contains("stream"), "nina");
        }));

        out.add(test("foldersContaining: morgfight/shamanfight cross-boss files resolve under music, both ways", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            List<String> withMorg = catalog.foldersContaining("morg_start_fight");
            List<String> withShaman = catalog.foldersContaining("shaman_start_fight");
            assertTrue(withMorg.contains("music"), "morg_start_fight resolves under music");
            assertTrue(withShaman.contains("music"), "shaman_start_fight resolves under music");
        }));

        out.add(test("load ignores a real folder that is not on the known-audio-tree whitelist", () -> {
            StalkerStreamCatalog catalog = loadFixtureWithUnrelatedFolder();
            assertEquals(3, catalog.totalFolderCount(), "folder count unaffected by unrelated folder");
            assertTrue(!catalog.folderNames().contains("sound"), "sound not indexed (not in whitelist)");
            assertTrue(!catalog.folderNames().contains("textures"), "textures not indexed (not in whitelist)");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerStreamCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerStreamCatalog.load rejects a root with none of the known folders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalker-stream-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerStreamCatalog.load(dir);
            }, "root with no known folders");
        }));

        return out;
    }

    private static StalkerStreamCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalker-stream-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerStreamCatalog.load(dir);
    }

    private static StalkerStreamCatalog loadFixtureWithUnrelatedFolder() throws Exception {
        Path dir = Files.createTempDirectory("stalker-stream-fixture-unrelated");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        Path unrelated = dir.resolve("sound/effects/not_from_this_catalog.ogg");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "");
        unrelated.toFile().deleteOnExit();
        Path unrelated2 = dir.resolve("textures/misc/not_from_this_catalog.tex");
        Files.createDirectories(unrelated2.getParent());
        Files.writeString(unrelated2, "");
        unrelated2.toFile().deleteOnExit();
        return StalkerStreamCatalog.load(dir);
    }
}
