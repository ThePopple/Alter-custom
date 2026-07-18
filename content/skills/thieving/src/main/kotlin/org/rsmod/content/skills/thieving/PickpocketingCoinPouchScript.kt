package org.rsmod.content.skills.thieving

import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.ItemServerType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeld5
import org.rsmod.game.inv.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private const val COINS = "obj.coins"

// TODO: Add limit to num of pouches you can get
private data class CoinPouch(val item: String, val coins: Int)

private val pickpocketCoinPouches =
    listOf(
        CoinPouch("obj.pickpocket_coin_pouch_citizen", 3),
        CoinPouch("obj.pickpocket_coin_pouch_farmer", 9),
        CoinPouch("obj.pickpocket_coin_pouch_ham", 18),
        CoinPouch("obj.pickpocket_coin_pouch_warrior", 18),
        CoinPouch("obj.pickpocket_coin_pouch_rogue", 35),
        CoinPouch("obj.pickpocket_coin_pouch_cavegoblin", 40),
        CoinPouch("obj.pickpocket_coin_pouch_guard", 30),
        CoinPouch("obj.pickpocket_coin_pouch_fremennik", 40),
        CoinPouch("obj.pickpocket_coin_pouch_bandit", 30),
        CoinPouch("obj.pickpocket_coin_pouch_knight", 50),
        CoinPouch("obj.pickpocket_coin_pouch_watchman", 60),
        CoinPouch("obj.pickpocket_coin_pouch_paladin", 80),
        CoinPouch("obj.pickpocket_coin_pouch_gnome", 70),
        CoinPouch("obj.pickpocket_coin_pouch_hero", 100),
        CoinPouch("obj.pickpocket_coin_pouch_vyre", 60),
        CoinPouch("obj.pickpocket_coin_pouch_elf", 70),
    )

class PickpocketingCoinPouchScript : PluginScript() {

    override fun ScriptContext.startup() {
        pickpocketCoinPouches.forEach { pouch ->
            val type = ServerCacheManager.getItem(pouch.item.asRSCM(RSCMType.OBJ)) ?: return@forEach
            val openSlot = findInvOpSlot(type, "open")
            val openAllSlot = findInvOpSlot(type, "open-all")

            openSlot?.let { registerOpenOp(item = pouch.item, opSlot = it, coins = pouch.coins, openAll = false) }
            openAllSlot
                ?.takeIf { it != openSlot }
                ?.let { registerOpenOp(item = pouch.item, opSlot = it, coins = pouch.coins, openAll = true) }

            if (openSlot == null && openAllSlot != null) {
                registerOpenOp(item = pouch.item, opSlot = openAllSlot, coins = pouch.coins, openAll = true)
            }
        }
    }

    private fun ScriptContext.registerOpenOp(item: String, opSlot: Int, coins: Int, openAll: Boolean) {
        when (opSlot) {
            1 -> onOpHeld1(item) { openCoinPouch(item, coins, slot = it.slot, openAll = openAll) }
            2 -> onOpHeld2(item) { openCoinPouch(item, coins, slot = it.slot, openAll = openAll) }
            3 -> onOpHeld3(item) { openCoinPouch(item, coins, slot = it.slot, openAll = openAll) }
            4 -> onOpHeld4(item) { openCoinPouch(item, coins, slot = it.slot, openAll = openAll) }
            5 -> onOpHeld5(item) { openCoinPouch(item, coins, slot = it.slot, openAll = openAll) }
        }
    }

    private fun findInvOpSlot(type: ItemServerType, target: String): Int? {
        for (slot in 1..5) {
            val action = type.interfaceOptions.getOrNull(slot - 1)?.trim()?.lowercase() ?: continue
            if (action == target) {
                return slot
            }
        }
        return null
    }

    private fun ProtectedAccess.openCoinPouch(item: String, coins: Int, slot: Int, openAll: Boolean) {
        val invObj = inv[slot]?.takeIf { it.isType(item) } ?: return
        val pouchCount = if (openAll) invObj.count else 1
        if (pouchCount <= 0 || invDel(inv, item, pouchCount, slot = slot).failure) {
            return
        }
        val totalCoins = coins * pouchCount
        invAdd(inv, COINS, totalCoins)
        val noun = if (totalCoins == 1) "coin" else "coins"
        mes("You open the coin pouch and find $totalCoins $noun.")
    }
}



