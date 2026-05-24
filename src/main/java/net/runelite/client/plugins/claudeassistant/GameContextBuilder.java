package net.runelite.client.plugins.claudeassistant;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;

import java.util.ArrayList;
import java.util.List;

public class GameContextBuilder
{
    private final Client client;
    private final ItemManager itemManager;

    public GameContextBuilder(Client client, ItemManager itemManager)
    {
        this.client = client;
        this.itemManager = itemManager;
    }

    public String build(ClaudeAssistantConfig config)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) return "";

        sb.append("Player: ").append(localPlayer.getName()).append("\n");
        sb.append("Combat Level: ").append(localPlayer.getCombatLevel()).append("\n");

        int hp      = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp   = client.getRealSkillLevel(Skill.HITPOINTS);
        int prayer  = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPray = client.getRealSkillLevel(Skill.PRAYER);
        int run     = client.getEnergy();
        sb.append("HP: ").append(hp).append("/").append(maxHp)
          .append(" | Prayer: ").append(prayer).append("/").append(maxPray)
          .append(" | Run: ").append(run / 100).append("%\n");

        if (config.sendLocation())
        {
            WorldPoint wp = localPlayer.getWorldLocation();
            sb.append("Location: ").append(getRegionName(wp.getRegionID()))
              .append(" (").append(wp.getX()).append(", ").append(wp.getY())
              .append(", plane ").append(wp.getPlane()).append(")\n");
        }

        if (config.sendPlayerStats())
        {
            sb.append("\nSkill Levels:\n");
            for (Skill skill : Skill.values())
            {
                if (skill == Skill.OVERALL) continue;
                int real    = client.getRealSkillLevel(skill);
                int boosted = client.getBoostedSkillLevel(skill);
                if (boosted != real)
                    sb.append("  ").append(skill.getName()).append(": ").append(real)
                      .append(" (").append(boosted).append(" boosted)\n");
                else
                    sb.append("  ").append(skill.getName()).append(": ").append(real).append("\n");
            }
        }

        if (config.sendInventory())
        {
            ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory != null)
            {
                List<String> items = new ArrayList<>();
                for (Item item : inventory.getItems())
                {
                    if (item.getId() <= 0) continue;
                    String name = itemManager.getItemComposition(item.getId()).getName();
                    items.add(item.getQuantity() > 1 ? name + " x" + item.getQuantity() : name);
                }
                sb.append("\nInventory: ").append(items.isEmpty() ? "Empty" : String.join(", ", items)).append("\n");
            }
        }

        if (config.sendEquipment())
        {
            ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
            if (equipment != null)
            {
                String[] slotNames = {
                    "Head", "Cape", "Neck", "Weapon", "Body", "Shield",
                    null, "Legs", null, "Hands", "Feet", null, "Ring", "Ammo"
                };
                sb.append("\nEquipment:\n");
                Item[] items = equipment.getItems();
                for (int i = 0; i < items.length && i < slotNames.length; i++)
                {
                    if (slotNames[i] == null) continue;
                    Item item = items[i];
                    if (item == null || item.getId() <= 0) continue;
                    sb.append("  ").append(slotNames[i]).append(": ")
                      .append(itemManager.getItemComposition(item.getId()).getName()).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    private String getRegionName(int regionId)
    {
        switch (regionId)
        {
            case 12850: return "Lumbridge";
            case 12598: return "Varrock";
            case 11828: return "Falador";
            case 10806: return "East Ardougne";
            case 9265:  return "Yanille";
            case 11062: return "Catherby";
            case 11571: return "Camelot / Seers Village";
            case 13150: return "Canifis";
            case 11322: return "Barbarian Village";
            case 12342: return "Edgeville";
            case 13362: return "Slayer Tower";
            case 9043:  return "God Wars Dungeon";
            case 11588: return "Chambers of Xeric";
            case 9033:  return "TzHaar / Fight Caves";
            case 6964:  return "The Inferno";
            case 12889: return "Al Kharid";
            case 13105: return "Draynor Village";
            case 11057: return "Tree Gnome Stronghold";
            default:    return "Region " + regionId;
        }
    }
}
