package org.test.minigamestructure.handlers.chesthandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.test.minigamestructure.MiniGameStructure;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChestHandler implements Listener {
    private final Map<String, Integer> containerWeights = new HashMap<>();
    private final Map<String, List<LootItem>> containerLootItems = new HashMap<>();
    private final Set<Location> openedChests = new HashSet<>();
    private final File worldFolder;
    private final File loottablesFile;

    public ChestHandler(MiniGameStructure plugin) {
        this.worldFolder = plugin.getServer().getWorld("world").getWorldFolder();
        File dataFolder = new File(worldFolder, "hunger_games_data");
        this.loottablesFile = new File(dataFolder, "loottables.yml");

        if (!loottablesFile.exists()) {
            Bukkit.getLogger().severe("Loot tables file does not exist: " + loottablesFile.getAbsolutePath());
            return;
        }

        YamlConfiguration lootConfig = YamlConfiguration.loadConfiguration(loottablesFile);
        loadConfig(lootConfig);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadConfig(YamlConfiguration lootConfig) {
        ConfigurationSection lootItemsSection = lootConfig.getConfigurationSection("lootItems");
        if (lootItemsSection == null) {
            Bukkit.getLogger().severe("No 'lootItems' section found in the loottables.yml!");
            return;
        }

        for (String key : lootItemsSection.getKeys(false)) {
            Bukkit.getLogger().info("Reading key: " + key);
            ConfigurationSection section = lootItemsSection.getConfigurationSection(key);
            if (section == null) {
                Bukkit.getLogger().severe("Missing section for key: " + key);
                continue;
            }

            String[] containers = key.split(",");
            int weight = section.getInt("weight", -1);
            if (weight == -1) {
                Bukkit.getLogger().severe("Missing 'weight' for key: " + key);
                continue;
            }
            Bukkit.getLogger().info("Weight for " + key + ": " + weight);

            List<LootItem> lootItems = new ArrayList<>();
            ConfigurationSection itemsSection = section.getConfigurationSection("lootItems");

            if (itemsSection == null) {
                Bukkit.getLogger().severe("Missing 'lootItems' section for key: " + key);
                continue;
            }

            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                if (itemSection != null) {
                    lootItems.add(new LootItem(itemSection));
                    Bukkit.getLogger().info("Added loot item: " + itemSection.getString("material"));
                } else {
                    Bukkit.getLogger().severe("Missing item section for itemKey: " + itemKey + " under key: " + key);
                }
            }

            for (String container : containers) {
                container = container.trim().toUpperCase();
                containerWeights.put(container, weight);
                containerLootItems.put(container, lootItems);
                Bukkit.getLogger().info("Added container type: " + container);
            }
        }
    }

    public void fill(Inventory inventory, String containerType, Player player) {
        inventory.clear();

        List<LootItem> lootItems = containerLootItems.get(containerType);
        Integer targetWeight = containerWeights.get(containerType);

        if (lootItems == null || targetWeight == null) {
            Bukkit.getLogger().severe("No loot items or weight found for container type: " + containerType);
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int currentWeight = 0;
        Set<LootItem> used = new HashSet<>();
        Map<String, LootItem> itemTypeMap = new HashMap<>();

        while (currentWeight < targetWeight - 3) {
            LootItem randomItem = lootItems.get(random.nextInt(lootItems.size()));
            if (used.contains(randomItem) && used.size() < lootItems.size()) continue;

            if (randomItem.shouldFill(random)) {
                String itemType = randomItem.getType();
                int itemWeight = randomItem.getWeight();

                if (itemType != null) {
                    if (itemTypeMap.containsKey(itemType)) {
                        LootItem existingItem = itemTypeMap.get(itemType);
                        if (existingItem.getWeight() < itemWeight) {
                            currentWeight -= existingItem.getWeight();
                            itemTypeMap.put(itemType, randomItem);
                            used.remove(existingItem);
                            used.add(randomItem);
                            currentWeight += itemWeight;
                        }
                    } else {
                        itemTypeMap.put(itemType, randomItem);
                        used.add(randomItem);
                        currentWeight += itemWeight;
                    }
                } else {
                    if (currentWeight + itemWeight <= targetWeight + 3) {
                        int slotIndex;
                        do {
                            slotIndex = random.nextInt(inventory.getSize());
                        } while (inventory.getItem(slotIndex) != null);

                        inventory.setItem(slotIndex, randomItem.make(random));
                        currentWeight += itemWeight;
                        used.add(randomItem);
                    }
                }
            }
        }

        // Place items in inventory
        for (LootItem lootItem : used) {
            if (lootItem.getType() != null) {
                int slotIndex;
                do {
                    slotIndex = random.nextInt(inventory.getSize());
                } while (inventory.getItem(slotIndex) != null);
                inventory.setItem(slotIndex, lootItem.make(random));
            }
        }
    }

    @EventHandler
    private void onChestOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            if (hasBeenOpened(chest.getLocation())) return;

            markAsOpened(chest.getLocation());
            fill(chest.getBlockInventory(), "CHEST", player);
        } else if (holder instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) holder;
            if (hasBeenOpened(chest.getLocation())) return;
            markAsOpened(chest.getLocation());
            fill(chest.getInventory(), "DOUBLE_CHEST", player);
        } else if (holder instanceof Barrel) {
            Barrel barrel = (Barrel) holder;
            if (hasBeenOpened(barrel.getLocation())) return;

            markAsOpened(barrel.getLocation());
            fill(barrel.getInventory(), "BARREL", player);
        }
    }

    public void markAsOpened(Location location) {
        openedChests.add(location);
    }

    public boolean hasBeenOpened(Location location) {
        return openedChests.contains(location);
    }

    public void resetChests() {
        openedChests.clear();
    }
}
