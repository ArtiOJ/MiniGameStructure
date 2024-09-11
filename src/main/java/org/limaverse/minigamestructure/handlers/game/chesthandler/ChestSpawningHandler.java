package org.limaverse.minigamestructure.handlers.game.chesthandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.utilities.ChestGlowUtility;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestSpawningHandler {
    private static final String SPAWNS_FILE_NAME = "chest_group_spawns.yml";
    private static ChestSpawningHandler instance;
    private final Random random = new Random();
    private final List<Location> platformLocations = new ArrayList<>();
    private final List<Location> chestLocations = new ArrayList<>();
    private final List<Location> selectedPlatformLocation = new ArrayList<>();
    private final int numberOfSpawns;
    private final int platformRadius;
    private FileConfiguration config;
    private final World world;
    private final MiniGameStructure plugin;
    private final File configFile;
    private final int CHEST_GLOW_TIME;

    private ChestSpawningHandler() {
        this.world = Bukkit.getWorld("game_map");
        this.plugin = MiniGameStructure.getInstance();
        CHEST_GLOW_TIME = random.nextInt(31) + 120;
        File dataFolder = new File(world.getWorldFolder(), "hunger_games_data");
        configFile = new File(dataFolder, SPAWNS_FILE_NAME);
        config = YamlConfiguration.loadConfiguration(configFile);
        this.numberOfSpawns = config.getInt("number_of_spawns");
        this.platformRadius = config.getInt("platform_radius");
        loadPlatformLocations();
    }

    public static synchronized ChestSpawningHandler getInstance() {
        if (instance == null) {
            instance = new ChestSpawningHandler();
        }
        return instance;
    }
    private void loadPlatformLocations() {
        ConfigurationSection section = config.getConfigurationSection("platform_spawns");
        if (section == null) {
            Bukkit.getLogger().warning("No platform_spawns section found in the configuration.");
            return;
        }

        platformLocations.clear();
        for (String key : section.getKeys(false)) {
            String[] parts = section.getString(key).split(",");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            Location loc = new Location(world, x, y, z);
            platformLocations.add(loc);
        }
    }

    public void setSpawnLocation(int key, double x, double y, double z, Material blockType) {
        x = Math.floor(x) + 0.5;
        z = Math.floor(z) + 0.5;

        String locStr = x + "," + y + "," + z + "," + blockType.name();
        config.set("platform_spawns." + key, locStr);

        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save config file: " + e.getMessage());
        }

        platformLocations.add(new Location(world, x, y, z));
    }

    public void spawnChestPlatforms() {
        Collections.shuffle(platformLocations);

        for (int i = 0; i < Math.min(numberOfSpawns, platformLocations.size()); i++) {
            Location loc = platformLocations.get(i);
            String[] parts = config.getString("platform_spawns." + i).split(",");
            Material blockType = Material.valueOf(parts[3]);
            selectedPlatformLocation.add(loc);
            generatePlatform(loc, blockType, platformRadius);
            spawnContainers(loc);
        }
    }

    public void generatePlatform(Location center, Material blockType, int radius) {
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        // Generate the main circle
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location loc = new Location(center.getWorld(), centerX + x, centerY, centerZ + z);
                    loc.getBlock().setType(blockType);
                }
            }
        }

        // Add extra blocks to smooth the platform
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if ((x * x + z * z > radius * radius) && (x * x + z * z <= (radius + 1) * (radius + 1))) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        Location loc1 = new Location(center.getWorld(), centerX + x, centerY, centerZ + z);
                        Location loc2 = new Location(center.getWorld(), centerX + x + (x == radius ? 1 : (x == -radius ? -1 : 0)), centerY, centerZ + z);
                        Location loc3 = new Location(center.getWorld(), centerX + x, centerY, centerZ + z + (z == radius ? 1 : (z == -radius ? -1 : 0)));
                        loc1.getBlock().setType(blockType);
                        loc2.getBlock().setType(blockType);
                        loc3.getBlock().setType(blockType);
                    }
                }
            }
        }
    }

    private void spawnContainers(Location center) {
        int doubleChestCount = config.getInt("DOUBLE_CHEST");
        int chestCount = config.getInt("CHEST");
        int barrelCount = config.getInt("BARREL");

        List<Location> containerLocations = new ArrayList<>();
        for (int x = -platformRadius; x <= platformRadius; x++) {
            for (int z = -platformRadius; z <= platformRadius; z++) {
                if (x * x + z * z <= platformRadius * platformRadius) {
                    containerLocations.add(center.clone().add(x, 1, z));
                }
            }
        }

        ChestGlowUtility chestGlowEvent = new ChestGlowUtility(plugin);

        Collections.shuffle(containerLocations);
        placeContainers(containerLocations, doubleChestCount, Material.CHEST, true, chestGlowEvent);
        placeContainers(containerLocations, chestCount, Material.CHEST, false, chestGlowEvent);
        placeContainers(containerLocations, barrelCount, Material.BARREL, false, chestGlowEvent);
    }

    private void placeContainers(List<Location> locations, int count, Material material, boolean isDoubleChest, ChestGlowUtility chestGlowEvent) {
        List<BlockFace> faces = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
        for (int i = 0; i < count && !locations.isEmpty(); i++) {
            Location loc = locations.remove(0);
            Block block = loc.getBlock();
            block.setType(material);

            if (isDoubleChest) {
                BlockFace face = faces.get(random.nextInt(faces.size()));

                Chest chestData = (Chest) block.getBlockData();
                chestData.setFacing(face);
                chestData.setType(Chest.Type.LEFT);
                block.setBlockData(chestData);
                chestGlowEvent.summonGlowingMagmaCube(block.getLocation(), CHEST_GLOW_TIME);
                chestLocations.add(block.getLocation());

                Location adjacentLoc = null;
                switch (face) {
                    case NORTH:
                        adjacentLoc = loc.clone().add(1, 0, 0);
                        break;
                    case SOUTH:
                        adjacentLoc = loc.clone().add(-1, 0, 0);
                        break;
                    case EAST:
                        adjacentLoc = loc.clone().add(0, 0, 1);
                        break;
                    case WEST:
                        adjacentLoc = loc.clone().add(0, 0, -1);
                        break;
                }

                if (adjacentLoc != null) {
                    Block adjacentBlock = adjacentLoc.getBlock();
                    adjacentBlock.setType(material);

                    Chest adjacentChestData = (Chest) adjacentBlock.getBlockData();
                    adjacentChestData.setFacing(face);
                    adjacentChestData.setType(Chest.Type.RIGHT);
                    adjacentBlock.setBlockData(adjacentChestData);
                    chestGlowEvent.summonGlowingMagmaCube(adjacentBlock.getLocation(), CHEST_GLOW_TIME);
                    chestLocations.add(adjacentBlock.getLocation());
                }
            } else {
                Directional directional = (Directional) block.getBlockData();
                directional.setFacing(faces.get(random.nextInt(faces.size())));
                block.setBlockData(directional);
                chestGlowEvent.summonGlowingMagmaCube(block.getLocation(), CHEST_GLOW_TIME);
                chestLocations.add(block.getLocation());
            }
        }
    }

    public void deleteChestGroups() {
        if (selectedPlatformLocation.isEmpty() || chestLocations.isEmpty()) return;
        for (Location location : selectedPlatformLocation) {
            generatePlatform(location, Material.AIR, platformRadius);
        }
        for (Location location : chestLocations) {
            location.getBlock().setType(Material.AIR);
        }
    }
}
