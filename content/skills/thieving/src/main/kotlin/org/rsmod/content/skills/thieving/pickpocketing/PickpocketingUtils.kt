package org.rsmod.content.skills.thieving.pickpocketing

import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

internal val debugEnabled = false;

private val rogueOutfitPieces =
    setOf(
        "obj.roguesden_helm",
        "obj.roguesden_body",
        "obj.roguesden_legs",
        "obj.roguesden_boots",
        "obj.roguesden_gloves",
    )

internal fun normalizeThievingName(value: String): String =
    value.lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

internal fun Player.isThievingStunned(currentMapClock: Int): Boolean = actionDelay > currentMapClock

internal fun Player.applyThievingStun(currentMapClock: Int, cycles: Int) {
    actionDelay = currentMapClock + cycles
}

internal fun Player.isWearingDodgyNecklace(): Boolean = "obj.dodgy_necklace" in worn

internal fun Player.isWearingRogueOutfit(): Boolean = rogueOutfitPieces.all { it in worn }

internal fun Player.isWearingGlovesOfSilence(): Boolean = "obj.hunting_silent_gloves" in worn

internal fun Player.isWearingThievingCape(): Boolean =
    "obj.skillcape_thieving" in worn ||
        "obj.skillcape_thieving_trimmed" in worn

internal fun Player.isWearingIceGloves(): Boolean = "obj.ice_gloves" in worn

class PickpocketingUtils constructor() : PluginScript() {
    override fun ScriptContext.startup() {

    }
}
