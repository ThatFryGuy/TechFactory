# Scalable Menu System - Developer Guide

## Overview

The TechFactory GUI has been refactored to use a **scalable button-based system** instead of material matching and individual listeners. This allows you to:

- ✅ Add new categories in **minutes**
- ✅ Avoid code duplication
- ✅ Type-safe button identification
- ✅ Single centralized event handler
- ✅ Easy pagination support

---

## Architecture

### Core Components

```
MenuButton (interface)
    ↓
MenuButtonRegistry (global button registry)
    ↓
MenuClickHandler (centralized event listener)
    ↓
ButtonInitializer (startup registration)
```

### How It Works

1. Each button implements `MenuButton` interface
2. Buttons are registered with unique IDs in `MenuButtonRegistry`
3. Button IDs are embedded in ItemStack NBT data
4. When clicked, `MenuClickHandler` extracts the ID and routes to the button
5. Button's `onClick()` method handles the action

---

## Adding a New Resource Category

### Step 1: Add to ResourceCategory Enum

File: `gui/categories/ResourceCategory.java`

```java
public enum ResourceCategory {
    // ... existing categories ...
    
    MACHINES("machines", ChatColor.BLUE + "Machines Menu", Material.CRAFTING_TABLE,
             ChatColor.BLUE + "Machines", "machines_btn");
    
    // ... rest of code ...
}
```

### Step 2: Create Category Button Class

File: `gui/menubuttons/Machines.java`

```java
package org.ThefryGuy.techFactory.gui.menubuttons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.gui.framework.MenuButton;
import org.ThefryGuy.techFactory.gui.framework.MenuButtonUtils;
import org.ThefryGuy.techFactory.gui.CategoryMenu;
import org.ThefryGuy.techFactory.gui.categories.ResourceCategory;
import org.ThefryGuy.techFactory.registry.ItemRegistry;

import java.util.List;

public class Machines implements MenuButton {

    private static final String BUTTON_ID = ResourceCategory.MACHINES.getButtonId();

    @Override
    public String getButtonId() {
        return BUTTON_ID;
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Machines");
            meta.setLore(List.of(
                    ChatColor.GRAY + "Click to view available machines",
                    ChatColor.GRAY + "and their crafting recipes."
            ));
            item.setItemMeta(meta);
        }

        return MenuButtonUtils.setButtonId(item, BUTTON_ID);
    }

    @Override
    public void onClick(Player player) {
        // When you add machines to ItemRegistry, uncomment this:
        // CategoryMenu.openCategoryMenu(player, ResourceCategory.MACHINES, ItemRegistry.getMachines());
        player.sendMessage(ChatColor.YELLOW + "Machines coming soon!");
    }

    public static ItemStack getItem() {
        Machines button = new Machines();
        return button.getItemStack();
    }
}
```

### Step 3: Register in ButtonInitializer

File: `gui/framework/ButtonInitializer.java`

```java
public static void initializeButtons() {
    // ... existing registrations ...
    
    // Register new category button
    MenuButtonRegistry.registerButton("machines_btn", new Machines());
}
```

### Step 4: Add to ResourcesMenu

File: `gui/ResourcesMenu.java`

```java
@Override
protected void buildMenu() {
    // ... existing code ...
    
    // Add the new category button
    addCategoryButton(3, new Machines());
}
```

### Step 5: Add to ItemRegistry (when ready)

File: `registry/ItemRegistry.java`

```java
// Create methods to retrieve machines
public static List<RecipeItem> getMachines() {
    // Return your machine items
}

public static RecipeItem getMachine(String name) {
    // Return specific machine
}
```

---

## MenuButton Interface

Every button must implement:

```java
public interface MenuButton {
    String getButtonId();           // Unique identifier
    ItemStack getItemStack();       // ItemStack with embedded ID
    void onClick(Player player);    // Click handler
}
```

---

## Key Classes

### MenuButtonRegistry

Global registry for all buttons. Automatically embeds button IDs in items via NBT.

```java
// Register a button
MenuButtonRegistry.registerButton("my_button_id", new MyButton());

// Get a button
MenuButton button = MenuButtonRegistry.getButton("my_button_id");

// Check if button exists
boolean exists = MenuButtonRegistry.hasButton("my_button_id");
```

### MenuButtonUtils

Handles embedding/retrieving button IDs from ItemStack NBT data:

```java
// Embed ID in item
ItemStack item = MenuButtonUtils.setButtonId(itemStack, "button_id");

// Get ID from item
String id = MenuButtonUtils.getButtonId(itemStack);

// Check if item has ID
boolean hasId = MenuButtonUtils.hasButtonId(itemStack);
```

### MenuClickHandler

Centralized listener that routes ALL menu clicks:

```
InventoryClickEvent
    → Check for button ID
    → Look up in registry
    → Call button.onClick()
```

**Only ONE listener instead of separate listeners per menu!**

### CategoryMenu

Generic menu that displays any category of items with automatic pagination:

```java
// Usage
CategoryMenu.openCategoryMenu(player, ResourceCategory.DUSTS, items);
```

Features:
- Automatic size calculation
- Supports 50+ items with pagination
- Built-in back/home buttons
- Works for any category

---

## Removing Old Listeners (Future)

Once everything works with the new system, you can remove:

- ❌ `listeners/GuideMenuListener.java`
- ❌ `listeners/ResourcesMenuListener.java`
- ❌ `listeners/DustsMenuListener.java`
- ❌ `listeners/RecipeMenuListener.java`

These are already running in parallel, so they won't cause issues.

---

## Complete Example: Adding "Alloys" Category

### 1. Enum Entry (ResourceCategory.java)
```java
ALLOYS("alloys", ChatColor.GOLD + "Alloys Menu", Material.BLAZE_ROD,
       ChatColor.GOLD + "Alloys", "alloys_btn"),
```

### 2. Button Class (Alloys.java)
Already created! Just uncomment when ready.

### 3. Initialize (ButtonInitializer.java)
```java
MenuButtonRegistry.registerButton("alloys_btn", new Alloys());
```

### 4. Add to ResourcesMenu (ResourcesMenu.java)
```java
addCategoryButton(2, new Alloys());
```

### 5. ItemRegistry (when items are ready)
```java
public static List<RecipeItem> getAlloys() {
    return ALLOYS_LIST;
}
```

### 6. Update Alloys.onClick()
```java
@Override
public void onClick(Player player) {
    CategoryMenu.openCategoryMenu(player, ResourceCategory.ALLOYS, ItemRegistry.getAlloys());
}
```

---

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| Add new category | Create new class + listener | Add enum entry + button class |
| Navigation logic | Repeated in each listener | One centralized handler |
| Button identification | Material matching (conflicts) | Unique IDs (type-safe) |
| Scalability | Hardcoded recipes | Generic CategoryMenu |
| Code duplication | High | Low |
| Testing | Difficult (listeners) | Easy (buttons) |

---

## Troubleshooting

**Button click not working?**
1. Check button ID is registered in ButtonInitializer
2. Verify `getButtonId()` returns correct ID
3. Ensure `MenuButtonUtils.setButtonId()` is called in `getItemStack()`

**Item not clickable?**
1. Verify `MenuClickHandler` is registered in `TechFactory.onEnable()`
2. Check inventory title matches menu (affects which items are clickable)

**Category menu not showing items?**
1. Verify items exist in ItemRegistry
2. Check `CategoryItemButton` is being created in `addItemsForCurrentPage()`
3. Ensure items are being registered in `MenuButtonRegistry`

---

## Best Practices

✅ **DO:**
- Keep button IDs unique and descriptive
- Register buttons in `ButtonInitializer`
- Use `MenuButtonUtils` for ID embedding
- Create reusable button classes

❌ **DON'T:**
- Hardcode materials to match items
- Create separate listeners per menu
- Embed click logic in listeners
- Forget to implement `MenuButton` interface

---

## Next Steps

1. Add items to `ItemRegistry` for each category
2. Uncomment category buttons when items are ready
3. Test navigation between menus
4. Remove old listeners once confident
5. Consider adding pagination controls

Happy scaling! 🚀