package ca.agnate.EconXP;

import java.util.LinkedList;
import java.util.List;


import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;

public class EconXP extends JavaPlugin {
    
    protected List<Node> permissionOPs;
    protected Server server;
    public OfflineManager offline;
    public int dropOnDeath;
    public int dropOnDeathPlayer;
    public boolean linear;
    public double levelniv;
    public String money; 
    
    public void onDisable() {
        // Show disabled message.
        System.out.println("[" + this + "] EconXP is disabled.");
    }

    public void onEnable() {
        // Add in permission node checks for OPs.
    	permissionOPs = new LinkedList<Node>();
        permissionOPs.add( Node.ADD );
        permissionOPs.add( Node.SUBTRACT );
        permissionOPs.add( Node.BALANCE );
        permissionOPs.add( Node.BALANCESELF );
        permissionOPs.add( Node.CLEAR );
        permissionOPs.add( Node.VERSION );
        permissionOPs.add( Node.SET );
        permissionOPs.add( Node.GIVE );
        permissionOPs.add( Node.TRANSFER );
        permissionOPs.add( Node.MULTIPLY );
        permissionOPs.add( Node.DIVIDE );
        
        // Set plugin defaults.
        offline = new OfflineManager (this);
        
        // Save server object.
        server = getServer();
        
        // Retrieve the config data.
        dropOnDeath = this.getConfig().getInt("death-drop", 27);
        dropOnDeathPlayer = this.getConfig().getInt("death-by-player-drop", 83);
        linear = this.getConfig().getBoolean("linear", true);
        levelniv = this.getConfig().getInt("level-up", 27);
        money = this.getConfig().getString("money-name", "ecu");
        
        // Bind the /econxp, /exp commands to EconXPCommands.
        EconXPCommands commandExecutor = new EconXPCommands (this);
        getCommand("econxp").setExecutor(commandExecutor);
        getCommand("exp").setExecutor(commandExecutor);
        
        // Set up death event checks.
        getServer().getPluginManager().registerEvents(new EconXPListener (this), this);
        
        // Save a default config file.
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        
        // Show enabled message.
        System.out.println("[" + this + "] EconXP is enabled.");
        if(linear) {System.out.println("[" + this + "] the XP is linear");}
    }
    

    
    public int setExp (OfflinePlayer p, int value) {
    	if ( p == null ) { return -1; }
    	
    	// If this is actually an online player,
    	if ( p instanceof Player ) {
    		return setExp( (Player) p, value );
    	}
    	
    	// Set the offline balance.
    	if ( offline.setBalance( p.getName(), value ) ) {
    		return value;
    	}
    	
    	return -1;
    }

    public int setExp (Player p, int value) {

    	if ( p == null ) { return -1; }
    	

    	p.setTotalExperience( 0 );
    	p.setLevel( 0 );
    	p.setExp( 0 );
    	
    	p.giveExp( value);
    	
    	if(linear){
    	double exp = (double)getExp(p) / levelniv;
    	ajouExp(p, exp);
    	}    	
        return value;
   }
    public void ajouExp (Player player, double exp){                                                   //to avoid a bug in setExp
    	  	
    	
        double floor = Math.floor(exp);
        player.setLevel((int) floor);
        player.setExp((float) (exp - floor));
    }

    public int getExpToLevel(int level) {
    	if (!linear){
        //return 7 + (level * 7 >> 1);
    	return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
    	}
    	
    	else{                                                                                          //if is linear,
    		return (int)(level / levelniv);
    	}
    }
    
    public double getExpToLevel100(int level){                                                         //for give a level with two numbers after the decimal point
    	if (!linear){
    	// ???
        return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
        }
    	
        else{
        	
        	double level100 = (int)((level / levelniv) * 100);
        	return level100 / 100;
        }
    }
    
    public int convertLevelToExp (int level) {
    	if (!linear){
        	int total = 0;
        	
        	while ( level > 0 ) {
        		total += getExpToLevel( level - 1 );
        		level--;
        	}
        	
        	return total;
        	
        }
        	else{
        		return (int)(level * levelniv);
        }
    }
    
    public int getExp (OfflinePlayer p) {
    	if ( p == null ) { return -1; }
    	
    	// If this is actually a player,
    	if ( p instanceof Player ) {
    		return getExp( (Player) p );
    	}
    	
    	return offline.getBalance( p.getName() );
    }
    public int getExp (Player p) {
       if ( p == null ) { return -1; }
       
       return p.getTotalExperience();
    }
    
    public int removeExp (Player p, int value) {
    	return removeExp( (OfflinePlayer) p, value );
    }
    public int removeExp (OfflinePlayer p, int value) {
    	int remain = getExp(p);
        
        // Remove the exp from the player.
        if ( remain >= 0 ) {
            remain -= value;
        }
        
        // If there's some exp left over, set it to the remaining.
        if ( remain >= 0 ) {
            // Set exp to remaining.
            setExp( p, remain );
            
            // Set remaining to amount removed (in this case, it's value).
            remain = value;
        }
        else {
            // Set exp to zero.
            setExp( p, 0 );
            
            // If remain < 0, adding it to value will give the true amount removed.
            remain = value + remain;
        }
        
        // Return the amount that was removed.
        return remain;
    }
    
    public int addExp (Player p, int value) {
    	return addExp( (OfflinePlayer) p, value );
    }
    public int addExp (OfflinePlayer p, int value) {
        int exp = getExp(p);
        
        // If it is less than 0 (ie. -1), it means there was a problem.
        if ( exp < 0 ) { return 0; }
        
        // Check the value.
        if ( value < 0 ) { return 0; }
        
        // Add in the experience.
        setExp(p, exp + value);
        
        // Return the value added.
        return value;
    }
    
    public int multiplyExp (Player p, float multiplier) {
    	return multiplyExp( (OfflinePlayer) p, multiplier );
    }
    public int multiplyExp (OfflinePlayer p, float multiplier) {
    	// If player doesn't exist, value can't be multiplied.
    	if ( p == null ) {
    		return 0;
    	}
    	
    	// If the multiplier is negative, cancel transaction.
    	if ( multiplier < 0 ) {
    		return 0;
    	}
    	
    	// Get player's current exp.
        int exp = getExp(p);
        
        // If something is wrong with the exp, cancel.
        if ( exp < 0 ) { return 0; }
    	
        // Multiply the exp by the multiplier, add it to the player's exp, and return the value.
    	return setExp( p, Math.round(exp * multiplier) );
    }
    
    public int divideExp (Player p, float divisor) {
    	return divideExp( (OfflinePlayer) p, divisor );
    }
    public int divideExp (OfflinePlayer p, float divisor) {
    	// If player doesn't exist, value can't be multiplied.
    	if ( p == null ) {
    		return 0;
    	}
    	
    	// If the divisor is negative, cancel transaction.
    	if ( divisor < 0 ) {
    		return 0;
    	}
    	
    	// Get player's current exp.
        int exp = getExp(p);
        
        // If something is wrong with the exp, cancel.
        if ( exp < 0 ) { return 0; }
    	
    	// Divide the exp by the divisor, add it to the player's exp, and return the value.
    	return setExp( p, Math.round(exp / divisor) );
    }
    
    public int clearExp (Player p) {
    	return clearExp( (OfflinePlayer) p );
    }
    public int clearExp (OfflinePlayer p) {
        // Get player's current exp.
        int exp = getExp(p);
        
        // Set exp to zero.
        setExp( p, 0 );
        
        // Return the exp they had.
        return exp;
    }
    
    public boolean hasExp (Player p, int value) {
    	return hasExp( (OfflinePlayer) p, value );
    }
    public boolean hasExp (OfflinePlayer p, int value) {
        return ( getExp(p) >= value );
    }
    
    public int giveExp (Player giver, Player receiver, int value) {
    	return giveExp( (OfflinePlayer) giver, (OfflinePlayer) receiver, value );
    }
    public int giveExp (OfflinePlayer giver, Player receiver, int value) {
    	return giveExp( giver, (OfflinePlayer) receiver, value );
    }
    public int giveExp (Player giver, OfflinePlayer receiver, int value) {
    	return giveExp( (OfflinePlayer) giver, receiver, value );
    }
    public int giveExp (OfflinePlayer giver, OfflinePlayer receiver, int value) {
    	// If one of the players doesn't exist, no transaction was made.
    	if ( giver == null  ||  receiver == null ) {
    		return 0;
    	}
    	
    	// If the value is negative, cancel transaction.
    	if ( value < 0 ) {
    		return 0;
    	}
    	
    	// If  the giver can't afford it, cancel transaction.
    	if ( hasExp(giver, value) == false ) {
    		return 0;
    	}
    	
    	// Remove exp from giver and add it to receiver, and return how much was given.
    	return addExp( receiver, removeExp(giver, value) );
    }
    
    public int calcDroppedExp (Player player, int DropExp) {                                   //for drop the constant
    	if ( player == null ) { return 0; }
    	
    	if(getExp(player) >= DropExp){return DropExp;}
    	
    	else if(getExp(player) < DropExp){return getExp(player);}
    	
    	return 0;
    }
    public int calcRemainingExp (Player player, int drop) {
    	if ( player == null ) { return 0; }
    	
    	return ((int)(getExp(player) - calcDroppedExp(player, drop)));
    }
    
    public boolean isValidPlayer(String player) {
    	// If player name was not given,
        if ( player.isEmpty() ) {
            return false;
        }
        
        // If there's a player,
        if ( getOnlinePlayer( player ) != null ) {
        	return true;
        }
        
        return ( getOfflinePlayer( player ) != null );
    }
    public OfflinePlayer getPlayer(String player) {
    	// If not player name was given,
        if ( player.isEmpty() ) {
            return null;
        }
        
        // Get the target player.
        Player target = getOnlinePlayer( player );
        
        // If there's a target,
        if ( target != null ) {
        	return target;
        }
        
        OfflinePlayer offTarget = getOfflinePlayer( player );
        
        if ( offTarget != null ) {
        	return offTarget;
        }
        
        return null;
    }
    protected Player getOnlinePlayer (String name) {
        return server.getPlayer(name);
    }
    protected OfflinePlayer getOfflinePlayer (String name) {
        return server.getOfflinePlayer(name);
    }
    
    public static boolean sendMsg(CommandSender p, String msg) {
        // If the input sender is null or the string is empty, return.
    	if (msg.equals("")) {
            return false;
    	}
        
    	// If no sender is given, just use the System.out.
    	if (p == null) {
    		System.out.println( "[EconXP] " + msg );
    		return true;
    	}
    	
        // Send the message to the assigned sender.
        p.sendMessage(ChatColor.GREEN + "[" +ChatColor.DARK_GREEN + "EconXP" + ChatColor.GREEN +      //green as the color of the XP
        		                        "] " + ChatColor.GREEN + msg);
        return true;
    }
    
    public boolean has(Permissible p, Node n) {
        // return (permissionHandler == null || permissionHandler.has(p, s));
        // return hasSuperPerms(p, s);
        return hasSuperPerms(p, n.toString()) || hasOPPerm(p, n);
    }
    protected boolean hasOPPerm(Permissible p, Node node) {
        // If the node requires OP status, and the player has OP, then true.
        return (permissionOPs == null || permissionOPs.contains(node) == false || p.isOp());
    }
    protected boolean hasSuperPerms(Permissible p, String s) {
        String[] nodes = s.split("\\.");
        
        // If they have the permission,
        if ( p.hasPermission(s) ) {
        	return true;
        }
        
        // Otherwise, check for any parent * nodes.
        String perm = "";
        for (int i = 0; i < nodes.length; i++) {
            perm += nodes[i] + ".";
            if (p.hasPermission(perm + "*"))
                return true;
        }
        
        return p.hasPermission(s);
    }
}
