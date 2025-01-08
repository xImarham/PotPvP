package net.frozenorb.potpvp.kt.protocol

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper
import io.netty.buffer.ByteBuf
import net.minecraft.server.v1_8_R3.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PingAdapter : PacketListenerAbstract(), Listener {

    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType === PacketType.Play.Server.KEEP_ALIVE) {
            val user = event.user ?: return
            val buffer = event.byteBuf as ByteBuf
            val id = ByteBufHelper.readInt(buffer)
            callbacks[user.uuid] = object : PingCallback(id) {
                override fun call() {
                    val ping = (System.currentTimeMillis() - this.sendTime).toInt()
                    Companion.ping[user.uuid] = ping
                    lastReply[user.uuid] = MinecraftServer.currentTick
                }
            }
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType === PacketType.Play.Client.KEEP_ALIVE) {
            val user = event.user ?: return
            val buffer = event.byteBuf as ByteBuf
            val id = ByteBufHelper.readInt(buffer)

            val iterator = callbacks.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.id == id) {
                    entry.value.call()
                    iterator.remove()
                    break
                }
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        ping.remove(event.player.uniqueId)
        lastReply.remove(event.player.uniqueId)
        callbacks.remove(event.player.uniqueId)
    }

    abstract class PingCallback(val id: Int) {
        val sendTime: Long = System.currentTimeMillis()
        abstract fun call()
    }

    companion object {
        val callbacks: ConcurrentHashMap<UUID, PingCallback> = ConcurrentHashMap()
        val ping: ConcurrentHashMap<UUID, Int> = ConcurrentHashMap()
        val lastReply: ConcurrentHashMap<UUID, Int> = ConcurrentHashMap()

        fun averagePing(): Int {
            if (ping.isEmpty()) {
                return 0
            }
            var total = 0
            for (p in ping.values) {
                total += p
            }
            return total / ping.size
        }
    }
}
