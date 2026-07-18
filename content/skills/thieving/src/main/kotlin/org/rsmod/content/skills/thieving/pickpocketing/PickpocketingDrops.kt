package org.rsmod.content.skills.thieving.pickpocketing

import dtx.rs.RSDropTable
import dtx.rs.RSWeightedTable
import org.rsmod.api.droptable.DropRollItem
import org.rsmod.api.droptable.rsPlayerGuaranteedTable
import org.rsmod.api.droptable.rsPlayerWeightedTable
import org.rsmod.game.entity.Player

internal data class PickpocketDrop(
    val item: String,
    val minAmount: Int,
    val maxAmount: Int,
    val weight: Int,
)

internal data class CoinPouchReward(
    val item: String,
    val guaranteed: Boolean = true,
    val weight: Int = COMMON,
)

private data class PickpocketDropTable(
    val id: String,
    val poolKey: String,
    val coinPouch: CoinPouchReward? = null,
)

internal object PickpocketingDrops {
    private val tablesById: Map<String, RSDropTable<Player, DropRollItem>> =
        listOf(
            PickpocketDropTable(
                id = "citizen",
                poolKey = POOL_CITIZEN,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_citizen"),
            ),
            PickpocketDropTable(
                id = "farmer",
                poolKey = POOL_FARMER,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_farmer"),
            ),
            PickpocketDropTable(
                id = "ham_male",
                poolKey = POOL_HAM_MEMBER,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_ham"),
            ),
            PickpocketDropTable(
                id = "ham_female",
                poolKey = POOL_HAM_MEMBER,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_ham"),
            ),
            PickpocketDropTable(
                id = "warrior",
                poolKey = POOL_WARRIOR,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_warrior"),
            ),
            PickpocketDropTable(id = "villager", poolKey = POOL_VILLAGER),
            PickpocketDropTable(
                id = "rogue",
                poolKey = POOL_ROGUE,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_rogue"),
            ),
            PickpocketDropTable(
                id = "cave_goblin",
                poolKey = POOL_CAVE_GOBLIN,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_cavegoblin"),
            ),
            PickpocketDropTable(id = "master_farmer", poolKey = POOL_MASTER_FARMER),
            PickpocketDropTable(
                id = "guard",
                poolKey = POOL_GUARD,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_guard"),
            ),
            PickpocketDropTable(
                id = "fremennik_citizen",
                poolKey = POOL_FREMENNIK_CITIZEN,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_fremennik"),
            ),
            PickpocketDropTable(
                id = "desert_bandit",
                poolKey = POOL_DESERT_BANDIT,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_bandit"),
            ),
            PickpocketDropTable(
                id = "knight",
                poolKey = POOL_KNIGHT,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_knight"),
            ),
            PickpocketDropTable(
                id = "watchman",
                poolKey = POOL_WATCHMAN,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_watchman"),
            ),
            PickpocketDropTable(
                id = "paladin",
                poolKey = POOL_PALADIN,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_paladin"),
            ),
            PickpocketDropTable(
                id = "gnome",
                poolKey = POOL_GNOME,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_gnome"),
            ),
            PickpocketDropTable(
                id = "hero",
                poolKey = POOL_HERO,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_hero"),
            ),
            PickpocketDropTable(
                id = "vyre",
                poolKey = POOL_VYRE,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_vyre"),
            ),
            PickpocketDropTable(
                id = "elf",
                poolKey = POOL_ELF,
                coinPouch = CoinPouchReward("obj.pickpocket_coin_pouch_elf"),
            ),
            PickpocketDropTable(id = "tzhaar", poolKey = POOL_TZHAAR),
        ).associate { spec -> spec.id to createTable(spec) }

    fun table(id: String): RSDropTable<Player, DropRollItem> {
        return tablesById[id] ?: error("Missing pickpocket drop table: '$id'.")
    }

    private fun createTable(spec: PickpocketDropTable): RSDropTable<Player, DropRollItem> {
        val drops = PickpocketDropPools.pools[spec.poolKey].orEmpty()

        val guaranteed =
            rsPlayerGuaranteedTable {
                spec.coinPouch
                    ?.takeIf { it.guaranteed }
                    ?.let { it.item count 1 }
                drops
                    .filter { it.weight <= ALWAYS }
                    .forEach { drop ->
                        drop.item count (drop.minAmount..drop.maxAmount)
                    }
            }

        val weightedEntries =
            buildList {
                spec.coinPouch
                    ?.takeIf { !it.guaranteed }
                    ?.let { add(PickpocketDrop(it.item, 1, 1, it.weight)) }
                addAll(drops.filter { it.weight > ALWAYS })
            }

        val weighted: RSWeightedTable<Player, DropRollItem> =
            if (weightedEntries.isNotEmpty()) {
                rsPlayerWeightedTable {
                    name("Pickpocket: ${spec.id}")
                    weightedEntries.forEach { drop ->
                        drop.weight weight drop.item count (drop.minAmount..drop.maxAmount)
                    }
                }
            } else {
                RSWeightedTable.Empty()
            }

        return RSDropTable(
            tableIdentifier = "pickpocket.${spec.id}",
            guaranteed = guaranteed,
            mainTable = weighted,
        )
    }
}

internal object PickpocketDropPools {
    val pools: Map<String, List<PickpocketDrop>> =
        mapOf(
            POOL_CITIZEN to emptyList(),
            POOL_FARMER to listOf(PickpocketDrop("obj.potato_seed", 1, 1, RARE)),
            POOL_HAM_MEMBER to
                listOf(
                    PickpocketDrop("obj.bronze_arrow", 1, 15, COMMON),
                    PickpocketDrop("obj.bronze_axe", 1, 1, COMMON),
                    PickpocketDrop("obj.bronze_pickaxe", 1, 1, COMMON),
                    PickpocketDrop("obj.iron_axe", 1, 1, COMMON),
                    PickpocketDrop("obj.iron_dagger", 1, 1, COMMON),
                    PickpocketDrop("obj.iron_pickaxe", 1, 1, COMMON),
                    PickpocketDrop("obj.digsitebuttons", 1, 1, COMMON),
                    PickpocketDrop("obj.feather", 1, 7, COMMON),
                    PickpocketDrop("obj.knife", 1, 1, COMMON),
                    PickpocketDrop("obj.logs", 1, 1, COMMON),
                    PickpocketDrop("obj.needle", 1, 1, COMMON),
                    PickpocketDrop("obj.raw_anchovies", 1, 3, COMMON),
                    PickpocketDrop("obj.raw_chicken", 1, 1, COMMON),
                    PickpocketDrop("obj.thread", 2, 10, COMMON),
                    PickpocketDrop("obj.tinderbox", 1, 1, COMMON),
                    PickpocketDrop("obj.uncut_opal", 1, 1, COMMON),
                    PickpocketDrop("obj.leather_armour", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_boots", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_cloak", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_gloves", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_hood", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_badge", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.ham_shirt", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.steel_arrow", 1, 13, UNCOMMON),
                    PickpocketDrop("obj.steel_axe", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.steel_dagger", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.steel_pickaxe", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.trail_clue_easy_simple001", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.coal", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.cow_hide", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.digsitearmour1", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.unidentified_guam", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.unidentified_marentill", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.unidentified_tarromin", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.iron_ore", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.digsitesword", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.uncut_jade", 1, 1, UNCOMMON),
                ),
            POOL_WARRIOR to emptyList(),
            POOL_VILLAGER to listOf(PickpocketDrop("obj.coins", 5, 5, ALWAYS)),
            POOL_ROGUE to
                listOf(
                    PickpocketDrop("obj.ring_of_dueling_1", 1, 1, RARE),
                    PickpocketDrop("obj.ring_of_dueling_2", 1, 1, RARE),
                    PickpocketDrop("obj.ring_of_dueling_3", 1, 1, RARE),
                    PickpocketDrop("obj.ring_of_dueling_4", 1, 1, RARE),
                    PickpocketDrop("obj.trail_clue_easy_simple001", 1, 1, VERY_RARE),
                ),
            POOL_CAVE_GOBLIN to
                listOf(
                    PickpocketDrop("obj.airrune", 8, 8, COMMON),
                    PickpocketDrop("obj.lockpick", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.jug_wine", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.gold_bar", 1, 1, RARE),
                    PickpocketDrop("obj.iron_dagger_p", 1, 1, RARE),
                ),
            POOL_MASTER_FARMER to
                listOf(
                    PickpocketDrop("obj.potato_seed", 1, 4, COMMON),
                    PickpocketDrop("obj.onion_seed", 1, 3, COMMON),
                    PickpocketDrop("obj.cabbage_seed", 1, 3, COMMON),
                    PickpocketDrop("obj.tomato_seed", 1, 2, COMMON),
                    PickpocketDrop("obj.sweetcorn_seed", 1, 2, UNCOMMON),
                    PickpocketDrop("obj.strawberry_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.watermelon_seed", 1, 1, RARE),
                    PickpocketDrop("obj.barley_seed", 1, 4, COMMON),
                    PickpocketDrop("obj.hammerstone_hop_seed", 1, 3, COMMON),
                    PickpocketDrop("obj.asgarnian_hop_seed", 1, 2, COMMON),
                    PickpocketDrop("obj.jute_seed", 1, 3, COMMON),
                    PickpocketDrop("obj.yanillian_hop_seed", 1, 2, UNCOMMON),
                    PickpocketDrop("obj.krandorian_hop_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.wildblood_hop_seed", 1, 1, RARE),
                    PickpocketDrop("obj.marigold_seed", 1, 1, COMMON),
                    PickpocketDrop("obj.nasturtium_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.rosemary_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.woad_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.limpwurt_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.redberry_bush_seed", 1, 1, COMMON),
                    PickpocketDrop("obj.cadavaberry_bush_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.dwellberry_bush_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.jangerberry_bush_seed", 1, 1, RARE),
                    PickpocketDrop("obj.whiteberry_bush_seed", 1, 1, RARE),
                    PickpocketDrop("obj.poisonivy_bush_seed", 1, 1, RARE),
                    PickpocketDrop("obj.guam_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.marrentill_seed", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.tarromin_seed", 1, 1, RARE),
                    PickpocketDrop("obj.harralander_seed", 1, 1, RARE),
                    PickpocketDrop("obj.ranarr_seed", 1, 1, RARE),
                    PickpocketDrop("obj.toadflax_seed", 1, 1, RARE),
                    PickpocketDrop("obj.irit_seed", 1, 1, RARE),
                    PickpocketDrop("obj.avantoe_seed", 1, 1, RARE),
                    PickpocketDrop("obj.kwuarm_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.snapdragon_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.cadantine_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.lantadyme_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.dwarf_weed_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.torstol_seed", 1, 1, VERY_RARE),
                    PickpocketDrop("obj.mushroom_spore_2", 1, 1, RARE),
                    PickpocketDrop("obj.belladonna_seed", 1, 1, RARE),
                    PickpocketDrop("obj.cactus_seed", 1, 1, VERY_RARE),
                ),
            POOL_GUARD to emptyList(),
            POOL_FREMENNIK_CITIZEN to emptyList(),
            POOL_DESERT_BANDIT to emptyList(),
            POOL_KNIGHT to emptyList(),
            POOL_WATCHMAN to emptyList(),
            POOL_PALADIN to listOf(PickpocketDrop("obj.chaosrune", 2, 2, COMMON)),
            POOL_GNOME to
                listOf(
                    PickpocketDrop("obj.earthrune", 1, 1, COMMON),
                    PickpocketDrop("obj.gold_ore", 1, 1, COMMON),
                    PickpocketDrop("obj.fire_orb", 1, 1, COMMON),
                    PickpocketDrop("obj.swamp_toad", 1, 1, COMMON),
                    PickpocketDrop("obj.king_worm", 1, 1, COMMON),
                    PickpocketDrop("obj.arrow_shaft", 1, 1, COMMON),
                ),
            POOL_HERO to
                listOf(
                    PickpocketDrop("obj.deathrune", 2, 2, UNCOMMON),
                    PickpocketDrop("obj.bloodrune", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.gold_ore", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.jug_wine", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.fire_orb", 1, 1, UNCOMMON),
                    PickpocketDrop("obj.diamond", 1, 1, UNCOMMON),
                ),
            POOL_VYRE to emptyList(),
            POOL_ELF to emptyList(),
            POOL_TZHAAR to
                listOf(
                    PickpocketDrop("obj.tzhaar_token", 1, 16, COMMON),
                    PickpocketDrop("obj.uncut_sapphire", 1, 1, COMMON),
                    PickpocketDrop("obj.uncut_emerald", 1, 1, COMMON),
                    PickpocketDrop("obj.uncut_ruby", 1, 1, COMMON),
                    PickpocketDrop("obj.uncut_diamond", 1, 1, COMMON),
                ),
        )
}

internal const val ALWAYS = 0
internal const val COMMON = 256
internal const val UNCOMMON = 32
internal const val RARE = 8
internal const val VERY_RARE = 1

internal const val POOL_CITIZEN = "pool.citizen"
internal const val POOL_FARMER = "pool.farmer"
internal const val POOL_HAM_MEMBER = "pool.ham_member"
internal const val POOL_WARRIOR = "pool.warrior"
internal const val POOL_VILLAGER = "pool.villager"
internal const val POOL_ROGUE = "pool.rogue"
internal const val POOL_CAVE_GOBLIN = "pool.cave_goblin"
internal const val POOL_MASTER_FARMER = "pool.master_farmer"
internal const val POOL_GUARD = "pool.guard"
internal const val POOL_FREMENNIK_CITIZEN = "pool.fremennik_citizen"
internal const val POOL_DESERT_BANDIT = "pool.desert_bandit"
internal const val POOL_KNIGHT = "pool.knight"
internal const val POOL_WATCHMAN = "pool.watchman"
internal const val POOL_PALADIN = "pool.paladin"
internal const val POOL_GNOME = "pool.gnome"
internal const val POOL_HERO = "pool.hero"
internal const val POOL_VYRE = "pool.vyre"
internal const val POOL_ELF = "pool.elf"
internal const val POOL_TZHAAR = "pool.tzhaar"

