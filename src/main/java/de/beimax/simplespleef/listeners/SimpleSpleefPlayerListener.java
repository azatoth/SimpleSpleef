/**
 * This file is part of the SimpleSpleef bukkit plugin.
 * Copyright (C) 2011 Maximilian Kalus
 * See http://dev.bukkit.org/server-mods/simple-spleef/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package de.beimax.simplespleef.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.beimax.simplespleef.SimpleSpleef;
import de.beimax.simplespleef.command.SimpleSpleefSignCommandExecutor;
import de.beimax.simplespleef.game.Game;
import de.beimax.simplespleef.util.UpdateChecker;

/**
 * Handle events for all Player related events
 * 
 * @author maxkalus
 */
public class SimpleSpleefPlayerListener implements Listener {
	/**
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// update checker activated
		if (SimpleSpleef.getPlugin().getConfig().getBoolean("settings.updateNotificationOnLogin", true)) {
			final Player player = event.getPlayer();
			// Check for updates whenever an operator or user with the right simplespleef.admin joins the game
			if (player != null && (player.isOp() || SimpleSpleef.checkPermission(player, "simplespleef.admin"))) {
				SimpleSpleef.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(SimpleSpleef.getPlugin(), new Runnable() { // create a new anonymous task/thread that will check the version asyncronously
					@Override
					public void run() {
						UpdateChecker checker = new UpdateChecker();
						try {
							// compare versions
							String oldVersion = SimpleSpleef.getPlugin().getDescription().getVersion();
							String newVersion = checker.checkForUpdate(oldVersion);
							if (newVersion != null) // do we have a version update? => notify player
								player.sendMessage(SimpleSpleef.getPlugin().ll("feedback.update", "[OLDVERSION]", oldVersion, "[NEWVERSION]", newVersion));
						} catch (Exception e) {
							player.sendMessage("SimpleSpleef could not get version update - see log for details.");
							SimpleSpleef.log.warning("[SimpleSpleef] Could not connect to remote server to check for update. Exception said: " + e.getMessage());
						}
					}
				}, 0L);
			}
		}

		// tell games about somebody joining, too
		if (SimpleSpleef.getGameHandler().hasGames()) {
			// tell all games about it
			for (Game game : SimpleSpleef.getGameHandler().getGames()) {
				game.onPlayerJoin(event);
			}
		}
	}

	/**
	 * @param event
	 */
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;

		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) game.onPlayerKick(event);
		}
	}

	/**
	 * @param event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) game.onPlayerQuit(event);
		}
	}	

	/**
	 * @param event
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) game.onPlayerMove(event);
		}
	}

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;

		// clicked on a sign and signs enabled?
		if (event.getPlayer() != null && event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign && SimpleSpleef.getPlugin().getConfig().getBoolean("settings.enableSigns", true)) {
			// only right click allowed?
			boolean signsOnlyRightClick = SimpleSpleef.getPlugin().getConfig().getBoolean("settings.signsOnlyRightClick", false);
			if (!signsOnlyRightClick || (signsOnlyRightClick && event.getAction() == Action.RIGHT_CLICK_BLOCK))
				// let the sign command executor do the rest
				new SimpleSpleefSignCommandExecutor().parseSimpleSpleefSign(event.getPlayer(), (Sign)event.getClickedBlock().getState());
		}
		
		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) game.onPlayerInteract(event);
		}
	}

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.isCancelled()) return;

		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) {
				// check, if arena allows the player's teleportation
				if (!game.playerMayTeleport(event.getPlayer())) {
					event.getPlayer().sendMessage(ChatColor.DARK_RED + SimpleSpleef.getPlugin().ll("errors.teleport", "[ARENA]", game.getName()));
					event.setCancelled(true); //cancel event
				}
			}
		}
	}

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (event.isCancelled()) return;

		if (SimpleSpleef.getGameHandler().hasGames()) {
			// player part of a game?
			Game game = SimpleSpleef.getGameHandler().checkPlayerInGame(event.getPlayer());
			if (game != null) { // generally disallow changes of game modes for spleefers
				event.getPlayer().sendMessage(ChatColor.DARK_RED + SimpleSpleef.getPlugin().ll("errors.gamemodeChange"));
				event.setCancelled(true); //cancel event
			}			
		}
	}
}
