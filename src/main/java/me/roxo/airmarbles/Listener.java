package me.roxo.airmarbles;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent event){
        final Player player = event.getPlayer();
        final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer.canBend(CoreAbility.getAbility((Class)AirMarbles.class))) {
            Bukkit.getServer().broadcastMessage("click");
            new AirMarbles(player);
        }
    }
    @EventHandler
    public void onClick(final PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        final Player player = event.getPlayer();
        final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer.canBend(CoreAbility.getAbility((Class)AirMarbles.class))) {
            Bukkit.getServer().broadcastMessage("click");
            new AirMarbles(player);
        }
    }


}
