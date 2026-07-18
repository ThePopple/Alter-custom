package org.rsmod.content.skills.thieving.pickpocketing

import dev.openrune.ServerCacheManager
import dev.openrune.types.NpcServerType
import dtx.core.ArgMap
import dtx.core.RollResult
import dtx.core.flatten
import jakarta.inject.Inject
import kotlin.text.contains
import org.rsmod.api.droptable.DropRollItem
import org.rsmod.api.droptable.rollCount
import org.rsmod.api.player.hit.queueHit
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc2
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpc5
import org.rsmod.game.entity.Npc
import org.rsmod.game.hit.HitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import skillSuccess
import kotlin.collections.iterator

class PickpocketingEvents @Inject constructor(
    private val random: GameRandom,
) : PluginScript() {
    val coinPouchLimit = 28; // TODO: Sort this with Ardy Diaries

    override fun ScriptContext.startup() {
        for ((_, npcType) in ServerCacheManager.getNpcs()) {
            val slot = pickpocketSlot(npcType) ?: continue
            val definition = PickpocketingData.resolve(npcType) ?: continue

            registerPickpocketOp(npcType.internalName, slot, definition)
        }
    }

    private fun ScriptContext.registerPickpocketOp(
        internal: String,
        slot: Int,
        definition: PickpocketDefinition,
    ) {
        when (slot) {
            1 -> onOpNpc1(internal) { attemptPickpocket(it.npc, definition) }
            2 -> onOpNpc2(internal) { attemptPickpocket(it.npc, definition) }
            3 -> onOpNpc3(internal) { attemptPickpocket(it.npc, definition) }
            4 -> onOpNpc4(internal) { attemptPickpocket(it.npc, definition) }
            5 -> onOpNpc5(internal) { attemptPickpocket(it.npc, definition) }
        }
    }

    private fun pickpocketSlot(type: NpcServerType): Int? {
        for (slot in 1..5) {
            val action = type.actions.getOpOrNull(slot - 1)
            if (isPickpocketAction(action)) {
                return slot
            }
        }

        return null
    }

    private fun isPickpocketAction(action: String?): Boolean {
        val normalized = action?.trim()?.lowercase() ?: return false
        return normalized == "pickpocket"
    }

    private suspend fun ProtectedAccess.attemptPickpocket(
        npc: Npc,
        definition: PickpocketDefinition
    ) {
        arriveDelay()

        val blockMessage = cannotPickpocket(npc, definition)
        if (blockMessage != null) {
            mes(blockMessage)
            return
        }

        val npcName = npc.visType.name
        val success = skillSuccess(definition.lowChance, definition.highChance, player.thievingLvl)

        mes("You attempt to pick the $npcName's pocket.")
        anim("seq.human_pickpocket")

        if (success) {
            giveRewards(definition)
            statAdvance("stat.thieving", definition.xp.toDouble())
            mes("You successfully pick the $npcName's pocket.")


            if (npcName.lowercase().contains("tzhaar") && !player.isWearingIceGloves()) {
                player.queueHit(
                    source = npc,
                    delay = 1,
                    type = HitType.Typeless,
                    damage = 4,
                )
            }

            return
        }

        mes("You fail to pick the $npcName's pocket.")
        npc.facePlayer(player)
        npc.anim("seq.human_unarmedpunch")
        player.anim("seq.human_stunned")

        val damage = random.of(definition.stunDamageMin, definition.stunDamageMax)
        player.queueHit(source = npc, delay = 1, type = HitType.Typeless, damage = damage)
        player.applyThievingStun(mapClock, definition.stunDuration)
    }

    private fun ProtectedAccess.cannotPickpocket(
        npc: Npc,
        definition: PickpocketDefinition
    ): String? {
        if (player.isThievingStunned(mapClock)) {
            return "You're stunned!"
        }

        if (player.thievingLvl < definition.level) {
            return "You need a thieving level of ${definition.level} to pickpocket this NPC."
        }

        if (inv.isFull()) {
            return "You don't have enough inventory space to pickpocket."
        }

        if (npc.hitpoints <= 0 || !npc.isVisible) {
            return "You can't pickpocket this NPC right now."
        }

        definition.coinPouch?.let {
            if (player.inv.count(it) >= coinPouchLimit) {
                return "You need to empty your coin pouches before you can continue pickpocketing."
            }
        }

        return null
    }

    private fun ProtectedAccess.giveRewards(definition: PickpocketDefinition) {
        val table = PickpocketingDrops.table(definition.dropTableId)

        when (val result = table.roll(player, ArgMap()).flatten()) {
            is RollResult.Nothing -> Unit
            is RollResult.Single -> giveDrop(result.result)
            is RollResult.ListOf -> result.results.forEach { giveDrop(it) }
        }
    }

    private fun ProtectedAccess.giveDrop(drop: DropRollItem) {
        if (drop.isNothing || !drop.condition(player)) return

        val obj = drop.transformObj(player) ?: drop.obj
        val amount = drop.rollCount(random)
        if (amount <= 0) return

        invAdd(inv, obj, amount)
    }
}
