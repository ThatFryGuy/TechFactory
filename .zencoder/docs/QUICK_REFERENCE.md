# Quick Reference Guide - Menu Framework

## 🚀 Adding a New Item Type in 3 Steps

### Step 1: Create Item Classes
```java
// src/main/java/org/ThefryGuy/techFactory/recipes/machines/GrindingStone.java
public class GrindingStone implements RecipeItem {
    @Override
    public String getId() { return "grinding_stone"; }
    // ... implement other methods
}
```

### Step 2: Register in ItemRegistry
```java
// In ItemRegistry.java
private static final Map<String, Supplier<? extends RecipeItem>> MACHINE_REGISTRY = Map.ofEntries(
    Map.entry("Grinding Stone", GrindingStone::new),
    Map.entry("Sifter", Sifter::new)
);

// Add to initialize()
MenuManager.registerRegistry("machines", MACHINE_REGISTRY);

// Add getter method
public static RecipeItem getMachine(String name) {
    Supplier<? extends RecipeItem> supplier = MACHINE_REGISTRY.get(name);
    return supplier != null ? supplier.get() : null;
}
```

### Step 3: Create Menu & Listener
```java
// MachinesMenu.java
public class MachinesMenu extends Menu {
    private static final MachinesMenu INSTANCE = new MachinesMenu();

    private MachinesMenu() {
        super(ChatColor.DARK_GRAY + "Machines Menu", 27);
    }

    public static void openMachinesMenu(Player player) {
        INSTANCE.openForPlayer(player);
    }

    @Override
    protected void buildMenu() {
        setItem(0, BackButton.getItem());
        setItem(1, ItemRegistry.getMachine("Grinding Stone").getItemStack());
        setItem(2, ItemRegistry.getMachine("Sifter").getItemStack());
    }
}

// MachinesMenuListener.java - same pattern as DustsMenuListener
public class MachinesMenuListener implements Listener {
    @EventHandler
    public void onMachinesMenuClick(InventoryClickEvent event) {
        // ... same pattern
    }
}
```

---

## 📍 Grid Positioning Reference

### Common Positions
```java
// Single row (9 slots)
MenuLayout.Positions.TOP_LEFT    // slot 0
MenuLayout.Positions.TOP_CENTER  // slot 4
MenuLayout.Positions.TOP_RIGHT   // slot 8

// Three rows (27 slots)
MenuLayout.Positions.CENTER      // slot 13 (middle of menu)
MenuLayout.Positions.BOTTOM_LEFT // slot 18
MenuLayout.Positions.BOTTOM_RIGHT // slot 26
```

### Custom Positions
```java
// Row 0, Column 4 (5th item in first row)
setItem(MenuLayout.position(0, 4), itemStack);

// Row 1, Column 0 (first item in second row)
setItem(MenuLayout.rowStart(1), itemStack);

// Row 2, Column 8 (last item in third row)
setItem(MenuLayout.rowEnd(2), itemStack);
```

---

## 🔗 Navigation Reference

### Opening a Menu
```java
// This adds menu to navigation stack
DustsMenu.openDustsMenu(player);

// Internally calls:
menu.openForPlayer(player);
// Which pushes to stack and opens inventory
```

### Back Button Handling
```java
// Automatically handles navigation stack
MenuManager.popMenu(player);

// This:
// 1. Pops current menu from stack
// 2. Retrieves previous menu from peek()
// 3. Opens previous menu (without re-tracking it)
```

### Checking Current Menu
```java
Menu currentMenu = MenuManager.getCurrentMenu(player);
if (currentMenu != null) {
    // Player has an open menu
}
```

---

## 🎯 Listener Pattern

### For Any Menu Click Listener
```java
@EventHandler
public void onMenuClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    
    // Check menu title
    if (event.getView().getTitle().equals(ChatColor.AQUA + "Your Menu Name")) {
        event.setCancelled(true); // Prevent item taking
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        Material type = clicked.getType();
        
        // Handle back button
        if (type == Material.ARROW && event.getSlot() == 0) {
            MenuManager.popMenu(player);
            return;
        }
        
        // Handle items
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        RecipeItem item = ItemRegistry.get*(itemName);
        if (item != null) {
            // Do something with item
        }
    }
}
```

---

## 📦 ItemRegistry Usage

### Getting Items
```java
// Dusts
RecipeItem dust = ItemRegistry.getDust("Iron Dust");

// Ingots (when added)
RecipeItem ingot = ItemRegistry.getIngot("Iron Ingot");

// Machines (when added)
RecipeItem machine = ItemRegistry.getMachine("Grinding Stone");
```

### Getting All Items of a Type
```java
Map<String, Supplier<? extends RecipeItem>> dustRegistry = ItemRegistry.getDustRegistry();
// Useful for pagination, search, filters, etc.
```

### Accessing Items from Listeners
```java
// All listeners have access to static ItemRegistry methods
RecipeItem item = ItemRegistry.getDust(itemName);
if (item != null) {
    RecipeMenu.openRecipeMenu(player, item);
}
```

---

## 🔧 Common Tasks

### Change Menu Size
```java
// In your Menu constructor
super(ChatColor.YELLOW + "My Menu", 27); // 9 slots per row

// Options: 9, 18, 27, 36, 45, 54 (1-6 rows)
```

### Change Button Position
```java
// Use MenuLayout helpers
setItem(MenuLayout.position(row, col), buttonItem);

// Or specific positions
setItem(MenuLayout.Positions.TOP_CENTER, buttonItem);
```

### Add Lore/Description to Button
```java
ItemStack button = new ItemStack(Material.IRON_INGOT);
ItemMeta meta = button.getItemMeta();
if (meta != null) {
    meta.setDisplayName(ChatColor.YELLOW + "Iron Ingot");
    meta.setLore(List.of(
        ChatColor.GRAY + "Line 1",
        ChatColor.GRAY + "Line 2"
    ));
    button.setItemMeta(meta);
}
```

### Handle Different Item Clicks
```java
Material type = clicked.getType();

switch(type) {
    case ARROW -> MenuManager.popMenu(player); // Back
    case OAK_LOG -> DustsMenu.openDustsMenu(player); // Dusts button
    case IRON_INGOT -> IngotsMenu.openIngotsMenu(player); // Ingots button
    default -> {} // Do nothing
}
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ DON'T: Create new menu every time
```java
// WRONG
public static void openMenu(Player player) {
    Menu menu = new MyMenu();
    menu.openForPlayer(player);
}
```

### ✅ DO: Use singleton pattern
```java
// RIGHT
private static final MyMenu INSTANCE = new MyMenu();
public static void openMenu(Player player) {
    INSTANCE.openForPlayer(player);
}
```

---

### ❌ DON'T: Open menu without tracking navigation
```java
// WRONG (lost in history)
menu.openForPlayerWithoutTracking(player);
```

### ✅ DO: Use openForPlayer
```java
// RIGHT (tracked in stack)
menu.openForPlayer(player);
```

---

### ❌ DON'T: Hardcode back button behavior
```java
// WRONG
if (Material.ARROW) {
    ResourcesMenu.openResourcesMenu(player);
}
```

### ✅ DO: Use MenuManager
```java
// RIGHT
if (Material.ARROW) {
    MenuManager.popMenu(player);
}
```

---

### ❌ DON'T: Use hardcoded if-else for items
```java
// WRONG (20+ lines)
if (name.equals("Iron Dust")) {
    // ...
} else if (name.equals("Gold Dust")) {
    // ...
}
```

### ✅ DO: Use ItemRegistry
```java
// RIGHT (1 line)
RecipeItem item = ItemRegistry.getDust(name);
```

---

## 📊 Navigation Stack Example

```
# Initial state
Stack: []
Player sees nothing

# Player opens Guide
Player runs /guide
Stack: [GuideMenu]
Player sees: TechFactory Guide

# Player clicks Resources button
Stack: [ResourcesMenu, GuideMenu]
Player sees: Resources Menu

# Player clicks Dusts button
Stack: [DustsMenu, ResourcesMenu, GuideMenu]
Player sees: Dusts Menu

# Player clicks Iron Dust
Stack: [RecipeMenu, DustsMenu, ResourcesMenu, GuideMenu]
Player sees: Iron Dust Recipe

# Player clicks Back button
Stack: [DustsMenu, ResourcesMenu, GuideMenu]
Player sees: Dusts Menu

# Player clicks Back button
Stack: [ResourcesMenu, GuideMenu]
Player sees: Resources Menu

# Player clicks Back button
Stack: [GuideMenu]
Player sees: TechFactory Guide

# Player clicks Back button
Stack: []
Inventory closes
```

---

## 🧪 How to Test Your New Item Type

1. **Build project** in IntelliJ: Build → Build Project
2. **Run server** with updated plugin
3. **Test navigation**:
   - Open guide: `/guide`
   - Navigate to your new menu
   - Click items, verify recipes open
   - Click back button, verify stack works
4. **Test performance**: 
   - Add 100+ items to registry
   - Verify menus still load instantly

---

## 📝 Checklist for New Items

- [ ] Created item classes implementing `RecipeItem`
- [ ] Added items to `ItemRegistry`
- [ ] Added getter method to `ItemRegistry`
- [ ] Created Menu class extending `Menu`
- [ ] Created Listener class implementing `Listener`
- [ ] Registered listener in `TechFactory.java`
- [ ] Added button to parent menu
- [ ] Tested navigation (forward & back)
- [ ] Tested with multiple items
- [ ] Checked for compile errors

---

**Last Updated**: Now
**Framework Version**: 1.0