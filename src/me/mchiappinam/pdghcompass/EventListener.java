package me.mchiappinam.pdghcompass;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

public class EventListener
  implements Listener
{
  public CompassNavigation plugin;
  public String title = "CompassNavigation";
  public ArrayList<String> using = new ArrayList();

  public EventListener(CompassNavigation plugin) {
    this.plugin = plugin;
  }

  public boolean sectionExists(int slot, String path) {
    if (this.plugin.getConfig().contains(slot + path)) {
      return true;
    }
    return false;
  }

  public int getID(String id) {
    return Integer.parseInt(id.split(":")[0]);
  }

  public short getDamage(String id) {
    String[] split = id.split(":");
    if (split.length >= 2) {
      return Short.parseShort(split[1]);
    }
    return 0;
  }

  public Inventory handleSlot(Player player, int slot, Inventory chest) {
    if (this.plugin.getConfig().getBoolean(slot + ".Enabled", false)) {
      ArrayList lore = new ArrayList();
      String Name = null;

      if (sectionExists(slot, ".Name")) {
        Name = "§r" + ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString(new StringBuilder(String.valueOf(slot)).append(".Name").toString()));
      }

      String Item = this.plugin.getConfig().getString(slot + ".Item", "2");
      int Amount = this.plugin.getConfig().getInt(slot + ".Amount", 1);
      int ID = getID(Item);
      short Damage = getDamage(Item);

      for (String loreLine : this.plugin.getConfig().getStringList(slot + ".Lore")) {
        lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
      }

      ItemStack stack = new ItemStack(ID, Amount, Damage);

      if ((this.plugin.getConfig().getBoolean(slot + ".Enchanted", false)) && (this.plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib"))) {
        stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 4);
      }

      if ((!player.hasPermission("compassnav.perks.free")) && 
        (!player.hasPermission("compassnav.perks.free." + slot)) && 
        (this.plugin.getServer().getPluginManager().isPluginEnabled("Vault")) && 
        (sectionExists(slot, ".Price"))) {
        lore.add("§2Price: §a$" + this.plugin.getConfig().getDouble(new StringBuilder(String.valueOf(slot)).append(".Price").toString()));
      }

      if ((!player.hasPermission("compassnav.use")) || (!player.hasPermission(new Permission("compassnav.use.slot." + slot, PermissionDefault.TRUE)))) {
        lore.add("§4No permission");
      }

      chest.setItem(slot - 1, setName(stack, Name, lore));
    }
    return chest;
  }

  public String handleModifiers(String string, Player player) {
    string = string.replace("<name>", player.getName());
    string = string.replace("<displayname>", player.getDisplayName());
    string = string.replace("<x>", Integer.toString(player.getLocation().getBlockX()));
    string = string.replace("<y>", Integer.toString(player.getLocation().getBlockY()));
    string = string.replace("<z>", Integer.toString(player.getLocation().getBlockZ()));
    string = string.replace("<yaw>", Integer.toString((int)player.getLocation().getYaw()));
    string = string.replace("<pitch>", Integer.toString((int)player.getLocation().getPitch()));
    return string;
  }

  public void checkBungee(Player player, int slot) {
    if (sectionExists(slot, ".Command")) {
      if (this.plugin.getConfig().getString(slot + ".Command").startsWith("c:"))
        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), handleModifiers(this.plugin.getConfig().getString(slot + ".Command").substring(2), player));
      else {
        this.plugin.getServer().dispatchCommand(player, handleModifiers(this.plugin.getConfig().getString(slot + ".Command"), player));
      }
    }
    if (sectionExists(slot, ".Bungee"))
      try {
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Connect");
        out.writeUTF(this.plugin.getConfig().getString(slot + ".Bungee"));

        player.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
      } catch (Exception e) {
  }
  }

  public void checkWarp(Player player, int slot) {
    if (sectionExists(slot, ".Warp")) {
        checkCoords(player, slot);
      }
    }

  public void checkCoords(Player player, int slot)
  {
    if (sectionExists(slot, ".X")) {
      player.teleport(new Location(this.plugin.getServer().getWorld(this.plugin.getConfig().getString(slot + ".World")), this.plugin.getConfig().getDouble(slot + ".X"), this.plugin.getConfig().getDouble(slot + ".Y"), this.plugin.getConfig().getDouble(slot + ".Z"), this.plugin.getConfig().getInt(slot + ".Yaw"), this.plugin.getConfig().getInt(slot + ".Pitch")));
    }
    player.closeInventory();
  }

  public void openInventory(Player player) {
    if (this.plugin.getConfig().getBoolean("Sounds")) {
      player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
    }

    if (!this.using.contains(player.getName())) {
      this.using.add(player.getName());
    }

    this.title = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("GUIName"));
    Inventory chest = this.plugin.getServer().createInventory(null, this.plugin.getConfig().getInt("Rows") * 9, this.title);

    for (int slot = 0; slot < chest.getSize() + 1; slot++) {
      chest = handleSlot(player, slot, chest);
    }

    player.openInventory(chest);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
      Player player = event.getPlayer();
      if ((player.getItemInHand().getTypeId() == getID(this.plugin.getConfig().getString("Item"))) && ((short)player.getItemInHand().getData().getData() == getDamage(this.plugin.getConfig().getString("Item"))) && 
        (player.hasPermission("compassnav.use")))
        if ((this.plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName())) && (!player.hasPermission("compassnav.perks.use.world"))) {
          this.plugin.send(player, this.plugin.prefix + "§6You can't teleport from this world!");
        }
          openInventory(player);
          event.setCancelled(true);
        }
    }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event)
  {
    if ((event.getPlayer() instanceof Player)) {
      final Player player = (Player)event.getPlayer();
      this.using.remove(player.getName());
      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
        public void run() {
          player.updateInventory();
        }
      }
      , 5L);
    }
  }

  @EventHandler
  public void onInventoryInteract(InventoryClickEvent event) {
    if ((event.getWhoClicked() instanceof Player)) {
      Player player = (Player)event.getWhoClicked();
      if (event.getInventory().getTitle().equals(this.title)) {
        event.setCancelled(true);
        int slot = event.getRawSlot() + 1;
        if ((sectionExists(slot, ".Enabled")) && 
          (this.plugin.getConfig().getBoolean(slot + ".Enabled")))
          if ((player.hasPermission("compassnav.use")) && (player.hasPermission(new Permission("compassnav.use.slot." + slot, PermissionDefault.TRUE)))) {
            if (this.plugin.getConfig().getBoolean("Sounds")) {
              player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
          } else if (this.plugin.getConfig().getBoolean("Sounds")) {
            player.playSound(player.getLocation(), Sound.ZOMBIE_IDLE, 1.0F, 1.0F);
          }
      }
    }
  }

  @EventHandler
  public void onSignInteract(PlayerInteractEvent event)
  {
    if (event.hasBlock()) {
      Block block = event.getClickedBlock();
      if ((event.getPlayer().hasPermission("compassnav.sign.use")) && 
        ((block.getTypeId() == 63) || (block.getTypeId() == 68)) && 
        (event.getAction() == Action.RIGHT_CLICK_BLOCK) && 
        ((block.getState() instanceof Sign))) {
        Sign sign = (Sign)block.getState();
        String line = sign.getLine(0);
        if ((line != "") && (line != null) && 
          (line.equals(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("SignName"))))) {
          Player player = event.getPlayer();
          if ((this.plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName())) && (!player.hasPermission("compassnav.perks.use.world")))
            this.plugin.send(player, this.plugin.prefix + "§6You can't teleport from this world!");
            openInventory(player);
          }
          event.setCancelled(true);
        }
      }
    }

  @EventHandler
  public void onSignCreate(SignChangeEvent event)
  {
    String line = event.getLine(0);
    if ((line != "") && (line != null) && (
      (line.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("SignName"))))) || (line.equals(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("SignName"))))))
      if (event.getPlayer().hasPermission("compassnav.admin.sign.create")) {
        event.setLine(0, ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("SignName")));
        this.plugin.send(event.getPlayer(), this.plugin.prefix + "§6Succesfully created a Teleport sign!");
      } else {
        event.setLine(0, ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("NoPermSignName")));
        this.plugin.send(event.getPlayer(), this.plugin.prefix + "§6You do not have permission to create a Teleport sign.");
      }
  }

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    String cmd = event.getMessage().substring(1);
    if (cmd.equalsIgnoreCase(this.plugin.getConfig().getString("CommandName"))) {
      Player player = event.getPlayer();
      if (player.hasPermission("compassnav.use")) {
        if ((this.plugin.getConfig().getStringList("DisabledWorlds").contains(player.getWorld().getName())) && (!player.hasPermission("compassnav.perks.use.world")))
          this.plugin.send(player, this.plugin.prefix + "§6You can't teleport from this world!");
          openInventory(player);
      }
      else {
        this.plugin.send(player, "§4You do not have access to that command.");
      }
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    Player player = event.getPlayer();
    this.using.remove(player.getName());
  }

  public ItemStack setName(ItemStack item, String name, ArrayList<String> lore) {
    ItemMeta itemMeta = item.getItemMeta();
    if (name != null) {
      itemMeta.setDisplayName(name);
    }
    if (lore != null) {
      itemMeta.setLore(lore);
    }
    item.setItemMeta(itemMeta);
    return item;
  }
}