package ca.agnate.EconXP;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class EconXPListener implements Listener {
	
	EconXP plugin;
	
	public EconXPListener( EconXP aPlugin ) {
		plugin = aPlugin;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin (PlayerJoinEvent event) {
		// Set the experience so that levels are calculated properly.
		plugin.setExp(event.getPlayer(), plugin.getExp(event.getPlayer()));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn (PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		
		// Delay the setting so that it actually works.
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
            	// Set the experience so that levels are calculated properly.
        		plugin.setExp(player, plugin.getExp(player));
            }
        }, 1 );
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath (EntityDeathEvent evt) {
		if ( evt instanceof PlayerDeathEvent == false ) {
			return;
		}
		if ( evt instanceof PlayerDeathEvent == true ) 
		{}
		
		PlayerDeathEvent event = (PlayerDeathEvent) evt;
		Player player = (event.getEntity() instanceof Player) ? (Player) event.getEntity() : null;
		
		if ( player == null ) {
			return;
		}
		
		                                                                                                          //PVP
		EntityDamageEvent devent = event.getEntity().getLastDamageCause();
		if(devent instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent edevent = (EntityDamageByEntityEvent) devent;
			if(edevent.getEntity().getType()==EntityType.PLAYER)//&& edevent.getDamager().getType().equals(player))
			{
				event.setDroppedExp( plugin.calcDroppedExp(player, plugin.dropOnDeathPlayer) );
				event.setNewExp( plugin.calcRemainingExp(player, plugin.dropOnDeathPlayer) );
				return;
			}
		}
						
		event.setDroppedExp( plugin.calcDroppedExp(player, plugin.dropOnDeath) );
		event.setNewExp( plugin.calcRemainingExp(player, plugin.dropOnDeath) );
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnchantItem (EnchantItemEvent event) {
		if ( event.isCancelled() ) {
			return;
		}
		int newTotal;

		if(!plugin.linear){  
			// Calculate the cost based on level.
		int newLevel = event.getEnchanter().getLevel() - event.getExpLevelCost();
		newTotal = plugin.convertLevelToExp( newLevel );
		
		// Add in what's remaining in the bar.
		newTotal += Math.floor(event.getEnchanter().getExp() * plugin.getExpToLevel( newLevel ));
		}
		
		else{                                                                                                   //for give a decimal
			newTotal = plugin.getExp(event.getEnchanter()) - plugin.convertLevelToExp(event.getExpLevelCost());
		}
		
		
		// Set the experience so that levels are calculated properly.
		plugin.setExp(event.getEnchanter(), newTotal);
		
		// Fake a cost of no levels so that Bukkit doesn't alter the level.
		event.setExpLevelCost( 0 );
	}
	
	@EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event)
	{
		if(plugin.linear){                                                                                    //makes XP leveling linear,
		    Player player = event.getPlayer();
			int amount = event.getAmount();
	        event.setAmount(0);
	        player.giveExp( amount );
	        
	        double exp = plugin.getExp(player) / plugin.levelniv;//show the exp bare
	        plugin.ajouExp(player, exp);
		}
		                                                                                   // if linear is false l'event it is bukkit, expodencielle
            
	}
	
}
