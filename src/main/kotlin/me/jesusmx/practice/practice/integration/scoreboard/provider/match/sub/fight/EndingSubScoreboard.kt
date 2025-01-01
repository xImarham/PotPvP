package me.jesusmx.practice.practice.integration.scoreboard.provider.match.sub.fight

import me.jesusmx.practice.practice.integration.scoreboard.provider.match.variable.DurationVariable
import me.jesusmx.practice.practice.integration.scoreboard.provider.match.variable.OpponentVariable
import me.jesusmx.practice.practice.integration.scoreboard.other.SubScoreboard
import net.frozenorb.potpvp.PotPvPSI
import net.frozenorb.potpvp.game.match.Match
import org.bukkit.entity.Player

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class EndingSubScoreboard : SubScoreboard<Match>() {

    private val durationVariable = DurationVariable()
    private val opponentVariable = OpponentVariable()

    override fun accept(player: Player, scores: MutableList<String>, match: Match) {
        // Get the config for the ending scoreboard
        val config = PotPvPSI.instance.scoreboardConfig

        // Replace the %duration% and %opponent% placeholders
        config.getStringList("IN-MATCH-END").forEach { line ->
            val lineWithDuration = line.replace("%duration%", durationVariable.format(player, match))
            val lineWithOpponent = lineWithDuration.replace("%opponent%", opponentVariable.format(player, match))
            scores.add(lineWithOpponent)
        }
    }
}
