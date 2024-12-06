package me.mchiappinam.pdghcompass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassNavigation extends JavaPlugin implements Listener {
	
  public String prefix = "";
  public String slot = "0";

  public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	      getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			getServer().getPluginManager().registerEvents(this, this);
    getConfig().options().copyDefaults(true);

    migrateToList();
    migrateFromDesc();

    saveConfig();

    if ((getConfig().getString("Prefix") != null) && (getConfig().getString("Prefix") != ""))
      this.prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix") + " ");
  }

  public void migrateToList()
  {
    for (int number = 0; number < 54; number++)
      if ((getConfig().contains(number + ".Desc")) && 
        (getConfig().isString(number + ".Desc"))) {
        getConfig().set(number + ".Lore", new ArrayList(Arrays.asList(new String[] { getConfig().getString(number + ".Desc") })));
        getConfig().set(number + ".Desc", null);
      }
  }

  public void migrateFromDesc()
  {
    for (int number = 0; number < 54; number++)
      if (getConfig().contains(number + ".Desc")) {
        getConfig().set(number + ".Lore", getConfig().getStringList(number + ".Desc"));
        getConfig().set(number + ".Desc", null);
      }
  }

  public void send(CommandSender sender, String message)
  {
    if ((sender instanceof Player))
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    else
      getLogger().info(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
  }

  public void sendHelpMessage(CommandSender sender)
  {
    send(sender, "§6§lCOMPASSNAV§f | §7/compassnav help");
    send(sender, "§6Oo-----------------------oOo-----------------------oO");
    send(sender, "§2/compassnav help§a - Get command help");
    send(sender, "§2/compassnav reload§a - Reload the plugin");
    send(sender, "§2/compassnav update§a - Download the latest update");
    send(sender, "§2/compassnav setup§a - Set up the compass inventory");
    send(sender, "§6Oo-----------------------oOo-----------------------oO");
  }

  public void sendSetupMessage(CommandSender sender, String slot) {
    if (sender.hasPermission("compassnav.admin.setup")) {
      send(sender, "§6§lSETUP§f | §7/compassnav setup");
      send(sender, "§6Oo-----------------------oOo-----------------------oO");
      if (!slot.equals("0")) {
        send(sender, "§aYou are now setting up slot " + slot + ".");
      }
      send(sender, "§2/compassnav setup loc§a - Sets location");
      send(sender, "§2/compassnav setup bungee <server>§a - Sets BungeeCord server");
      send(sender, "§2/compassnav setup lilypad <server>§a - Sets Lilypad server");
      send(sender, "§2/compassnav setup warp <warp>§a - Sets Essentials warp");
      send(sender, "§2/compassnav setup item§a - Sets item from hand");
      send(sender, "§2/compassnav setup name <name>§a - Sets item name");
      send(sender, "§2/compassnav setup lore [number] <lore>§a - Sets item lore");
      send(sender, "§2/compassnav setup price <price>§a - Sets the price of using the item");
      send(sender, "§2/compassnav setup amount <amount>§a - Sets item amount");
      send(sender, "§2/compassnav setup command <command>§a - Sets the executable command");
      send(sender, "§2/compassnav setup enchant§a - Toggles enchanted status");
      send(sender, "§2/compassnav setup enable§a - Enables slot");
      send(sender, "§6Oo-----------------------oOo-----------------------oO");
    } else {
      send(sender, "§4You do not have access to that command.");
    }
  }

  public String handleString(String[] args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 2; i < args.length; i++) {
      sb.append(args[i]).append(" ");
    }
    return sb.toString().trim();
  }

  public boolean sectionExists(String slot, String path) {
    if (getConfig().contains(slot + path)) {
      return true;
    }
    return false;
  }

  public boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if ((cmd.getName().equalsIgnoreCase("compassnav")) || (cmd.getName().equalsIgnoreCase("cn")) || (cmd.getName().equalsIgnoreCase("compassnavigation"))) {
      if (args.length == 0)
        sendHelpMessage(sender);
      else if (args.length == 1) {
        if (args[0].equalsIgnoreCase("reload")) {
          if (sender.hasPermission("compassnav.admin.reload")) {
            reloadConfig();
            send(sender, this.prefix + "§6CompassNavigation reloaded!");
          } else {
            send(sender, "§4You do not have access to that command.");
          }
        }
        else if (args[0].equalsIgnoreCase("setup")) {
          if ((sender instanceof Player)) {
            Player player = (Player)sender;
            if (player.hasPermission("compassnav.admin.setup")) {
              send(player, "§6§lSETUP§f | §7/compassnav setup");
              send(player, "§6Oo-----------------------oOo-----------------------oO");
              send(player, "§aPlease specify a slot number.");
              send(player, "§2Usage:§a /compassnav setup <1>");
              send(player, "§6Oo-----------------------oOo-----------------------oO");
            } else {
              send(player, "§4You do not have access to that command.");
            }
          } else {
            send(getServer().getConsoleSender(), this.prefix + "Sorry, but consoles can't execute this command.");
          }
        }
        else sendHelpMessage(sender);
      }
      else if (args.length == 2) {
        if ((sender instanceof Player)) {
          Player player = (Player)sender;
          if (args[0].equalsIgnoreCase("setup")) {
            if (player.hasPermission("compassnav.admin.setup")) {
              if (isInteger(args[1])) {
                if (Integer.parseInt(args[1]) <= 54) {
                  this.slot = args[1];
                  sendSetupMessage(player, this.slot);
                }
              } else if (!this.slot.equals("0")) {
                if (args[1].equalsIgnoreCase("loc")) {
                  getConfig().set(this.slot + ".World", player.getWorld().getName());
                  getConfig().set(this.slot + ".X", Double.valueOf(player.getLocation().getX()));
                  getConfig().set(this.slot + ".Y", Double.valueOf(player.getLocation().getY()));
                  getConfig().set(this.slot + ".Z", Double.valueOf(player.getLocation().getZ()));
                  getConfig().set(this.slot + ".Yaw", Float.valueOf(player.getLocation().getYaw()));
                  getConfig().set(this.slot + ".Pitch", Float.valueOf(player.getLocation().getPitch()));
                  send(player, this.prefix + "§6Location set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("item")) {
                  getConfig().set(this.slot + ".Item", player.getItemInHand().getTypeId() + ":" + player.getItemInHand().getDurability());
                  send(player, this.prefix + "§6Item set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("enable")) {
                  if (getConfig().contains(this.slot + ".Enabled")) {
                    if (getConfig().getBoolean(this.slot + ".Enabled")) {
                      send(player, this.prefix + "§6Disabled slot " + this.slot + ".");
                      getConfig().set(this.slot + ".Enabled", Boolean.valueOf(false));
                    } else {
                      send(player, this.prefix + "§6Enabled slot " + this.slot + ".");
                      getConfig().set(this.slot + ".Enabled", Boolean.valueOf(true));
                    }
                  } else {
                    send(player, this.prefix + "§6Enabled slot " + this.slot + ".");
                    getConfig().set(this.slot + ".Enabled", Boolean.valueOf(true));
                  }
                } else if (args[1].equalsIgnoreCase("bungee")) {
                  getConfig().set(this.slot + ".Bungee", null);
                  send(player, this.prefix + "§6Bungee unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("lilypad")) {
                  getConfig().set(this.slot + ".Lilypad", null);
                  send(player, this.prefix + "§6Lilypad unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("lore")) {
                  getConfig().set(this.slot + ".Lore", null);
                  send(player, this.prefix + "§6Lore unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("warp")) {
                  getConfig().set(this.slot + ".Warp", null);
                  send(player, this.prefix + "§6Warp unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("price")) {
                  getConfig().set(this.slot + ".Price", null);
                  send(player, this.prefix + "§6Price unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("amount")) {
                  getConfig().set(this.slot + ".Amount", null);
                  send(player, this.prefix + "§6Amount unset for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("enchant")) {
                  if (getConfig().contains(this.slot + ".Enchant")) {
                    if (getConfig().getBoolean(this.slot + ".Enchant")) {
                      send(player, this.prefix + "§6Removed enchant from slot " + this.slot + ".");
                      getConfig().set(this.slot + ".Enchant", null);
                    } else {
                      send(player, this.prefix + "§6Added enchant to slot " + this.slot + ".");
                      getConfig().set(this.slot + ".Enchant", Boolean.valueOf(true));
                    }
                  } else {
                    send(player, this.prefix + "§6Added enchant to slot " + this.slot + ".");
                    getConfig().set(this.slot + ".Enchant", Boolean.valueOf(true));
                  }
                } else if (args[1].equalsIgnoreCase("command")) {
                  getConfig().set(this.slot + ".Command", null);
                  send(player, this.prefix + "§6Command unset for slot " + this.slot + ".");
                } else {
                  sendSetupMessage(player, this.slot);
                }
                saveConfig();
              } else {
                send(player, this.prefix + "§6You haven't specified a slot to modify.");
              }
            }
            else send(player, "§4You do not have access to that command.");
          }
          else
            sendHelpMessage(sender);
        }
        else {
          send(getServer().getConsoleSender(), this.prefix + "Sorry, but consoles can't execute this command.");
        }
      } else if (args.length >= 3)
        if ((sender instanceof Player)) {
          Player player = (Player)sender;
          if (player.hasPermission("compassnav.admin.setup")) {
            if (!this.slot.equals("0")) {
              if (args[0].equalsIgnoreCase("setup")) {
                if (args[1].equalsIgnoreCase("bungee")) {
                  getConfig().set(this.slot + ".Bungee", handleString(args));
                  send(player, this.prefix + "§6Bungee set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("lilypad")) {
                  getConfig().set(this.slot + ".Lilypad", handleString(args));
                  send(player, this.prefix + "§6Lilypad set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("name")) {
                  getConfig().set(this.slot + ".Name", handleString(args));
                  send(player, this.prefix + "§6Name set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("lore")) {
                  List loreList = new CopyOnWriteArrayList();
                  String loreString;
                  if (args.length >= 4) {
                    String lore = handleString(args);
                    try {
                      int number = Integer.parseInt(args[2]);
                      lore = lore.split(" ", 2)[1];
                      for (String secondLore : getConfig().getStringList(this.slot + ".Lore")) {
                        loreList.add(secondLore);
                      }
                      loreList.set(number - 1, lore);
                      getConfig().set(this.slot + ".Lore", loreList);
                    } catch (Exception e) {
                      loreString = handleString(args);
                      for (String secondLore : getConfig().getStringList(this.slot + ".Lore")) {
                        loreList.add(secondLore);
                      }
                      loreList.add(loreString);
                      getConfig().set(this.slot + ".Lore", loreList);
                    }
                  } else {
                    String loreString1 = handleString(args);
                    for (String secondLore : getConfig().getStringList(this.slot + ".Lore")) {
                      loreList.add(secondLore);
                    }
                    loreList.add(loreString1);
                    getConfig().set(this.slot + ".Lore", loreList);
                  }
                  send(player, this.prefix + "§6Lore set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("warp")) {
                  getConfig().set(this.slot + ".Warp", handleString(args));
                  send(player, this.prefix + "§6Warp set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("price")) {
                  getConfig().set(this.slot + ".Price", Double.valueOf(Double.parseDouble(handleString(args))));
                  send(player, this.prefix + "§6Price set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("amount")) {
                  getConfig().set(this.slot + ".Amount", Integer.valueOf(Integer.parseInt(handleString(args))));
                  send(player, this.prefix + "§6Amount set for slot " + this.slot + "!");
                } else if (args[1].equalsIgnoreCase("command")) {
                  getConfig().set(this.slot + ".Command", handleString(args));
                  send(player, this.prefix + "§6Command set for slot " + this.slot + "!");
                } else {
                  sendSetupMessage(player, this.slot);
                }
                saveConfig();
              } else {
                sendHelpMessage(player);
              }
            }
            else send(player, this.prefix + "§6You haven't specified a slot to modify.");
          }
          else
            send(player, "§4You do not have access to that command.");
        }
        else {
          send(getServer().getConsoleSender(), this.prefix + "Sorry, but consoles can't execute this command.");
        }
    }
    else {
      sendHelpMessage(sender);
    }
    return true;
  }
}