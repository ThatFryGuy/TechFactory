# Simplified GUI System 🎯

## Overview

The GUI package has been completely simplified. No more confusing folders, button classes, or over-engineering.

---

## 📁 Package Structure

```
gui/
├── Menu.java              # Base class for all menus
├── MenuManager.java       # Handles navigation & pagination
├── MenuClickHandler.java  # Single event handler
│
├── menus/                 # All menu implementations
│   ├── GuideMenu.java     # Main entry menu
│   ├── ResourcesMenu.java # Category selection
│   ├── ItemListMenu.java  # Generic paginated list
│   └── RecipeMenu.java    # Recipe display
│
└── layout/
    └── MenuLayout.java    # Position helpers (if needed)
```

**That's it!** Just 4 core files + 4 menu implementations.

---

## 🎮 How It Works

### 1. Player runs command
```
/techfactory guide
```

### 2. GuideMenu opens
- Shows "Resources" button
- Click → Opens ResourcesMenu

### 3. ResourcesMenu opens
- Shows category buttons: Dusts, Alloys, (future: Machines, Tools, etc.)
- Has Back and Home buttons
- Click "Dusts" → Opens ItemListMenu with dusts

### 4. ItemListMenu opens
- Generic menu that works for ANY category
- Shows 36 items per page (4 rows)
- Has pagination (Previous/Next page buttons)
- Has Back and Home buttons
- Click an item → Opens RecipeMenu

### 5. RecipeMenu opens
- Shows how to craft the item
- Displays machine, ingredients, and output
- Has Back and Home buttons

---

## 🔧 How Actions Work

Instead of creating a button class for every single button, we use **NBT tags**:

```java
// Create an item
ItemStack item = new ItemStack(Material.CHEST);
ItemMeta meta = item.getItemMeta();
meta.setDisplayName(ChatColor.GOLD + "Resources");
item.setItemMeta(meta);

// Set the action (stored in NBT)
MenuClickHandler.setAction(item, "open_resources");

// Add to menu
setItem(13, item);
```

When clicked, the menu's `handleClick()` method receives the action:

```java
@Override
public void handleClick(Player player, String action, ItemStack clicked) {
    if (action.equals("open_resources")) {
        new ResourcesMenu(player).open();
    }
}
```

**Simple!** No button classes needed.

---

## ➕ Adding a New Category

Let's say you want to add "Machines" category:

### Step 1: Add button to ResourcesMenu

```java
// In ResourcesMenu.java build() method:

ItemStack machines = new ItemStack(Material.CRAFTING_TABLE);
ItemMeta meta = machines.getItemMeta();
meta.setDisplayName(ChatColor.BLUE + "Machines");
meta.setLore(List.of(
    ChatColor.GRAY + "View all machines",
    ChatColor.GRAY + "Click to open"
));
machines.setItemMeta(meta);
MenuClickHandler.setAction(machines, "open_machines");
setItem(15, machines); // Position in menu
```

### Step 2: Handle the click

```java
// In ResourcesMenu.java handleClick() method:

case "open_machines" -> new ItemListMenu(player, "Machines", ItemRegistry.getMachines()).open();
```

### Step 3: Add to ItemRegistry

```java
// In ItemRegistry.java:

public static List<RecipeItem> getMachines() {
    // Return list of machine items
    return new ArrayList<>(); // Add your machines here
}
```

**Done!** Pagination, navigation, everything works automatically.

---

## 📊 Key Classes Explained

### Menu.java
- Base class for all menus
- Handles inventory creation
- Has `build()` method (override to add items)
- Has `handleClick()` method (override to handle clicks)
- Has `open()` method (call to open the menu)

### MenuManager.java
- Tracks menu navigation stack (for back button)
- Tracks pagination state (current page per category)
- Methods:
  - `pushMenu()` - Add menu to stack
  - `goBack()` - Go to previous menu
  - `goHome()` - Clear stack and go to guide
  - `getCurrentPage()` / `setCurrentPage()` - Pagination

### MenuClickHandler.java
- Single event listener for ALL menus
- Reads NBT action from clicked item
- Calls menu's `handleClick()` method
- Helper methods:
  - `setAction(item, action)` - Set action on item
  - `getAction(item)` - Get action from item

### ItemListMenu.java
- **Generic menu** for any category
- Works for Dusts, Alloys, Machines, Tools, etc.
- Automatically handles pagination
- Shows 36 items per page
- Has Previous/Next page buttons
- Constructor: `new ItemListMenu(player, "CategoryName", itemList)`

---

## 🎯 Benefits of This Approach

| Before | After |
|--------|-------|
| 22 files | 8 files |
| 6 packages | 2 packages |
| Button classes for everything | NBT actions (simple strings) |
| Separate menu for each category | 1 generic ItemListMenu |
| Confusing structure | Clear and simple |
| Hard to add categories | 3 steps to add category |

---

## 🧪 Testing

### Build the plugin:
```bash
mvn clean package
```

### Test in-game:
1. `/techfactory guide` - Opens guide menu
2. Click "Resources" - Opens resources menu
3. Click "Dusts" - Opens dusts list
4. Test pagination (if you have 50+ dusts)
5. Click a dust - Opens recipe menu
6. Test Back button - Goes to previous menu
7. Test Home button - Goes to guide menu

---

## 🚀 Future Expansion

When you're ready to add more categories:

1. **Alloys** - Already has placeholder in ResourcesMenu
2. **Machines** - Follow the 3-step guide above
3. **Tools** - Same pattern
4. **Armor** - Same pattern
5. **Weapons** - Same pattern

The system scales to **100+ categories** without any structural changes!

---

## 💡 Design Philosophy

**Keep it simple:**
- No unnecessary abstractions
- No button classes for simple actions
- One generic list menu instead of many specific ones
- Clear, flat package structure
- Easy to understand and modify

**Make it scalable:**
- Generic ItemListMenu works for any category
- NBT actions can represent any action
- MenuManager handles all navigation
- Easy to add new categories

---

## 📝 Code Examples

### Creating a simple menu:

```java
public class MyMenu extends Menu {
    
    public MyMenu(Player player) {
        super(player, "My Menu Title", 27);
    }
    
    @Override
    protected void build() {
        // Add items to inventory
        ItemStack item = new ItemStack(Material.DIAMOND);
        MenuClickHandler.setAction(item, "my_action");
        setItem(13, item);
    }
    
    @Override
    public void handleClick(Player player, String action, ItemStack clicked) {
        if (action.equals("my_action")) {
            player.sendMessage("You clicked the diamond!");
        }
    }
}
```

### Opening a menu:

```java
new MyMenu(player).open();
```

**That's all you need!**

---

## 🎉 Summary

Your GUI system is now:
- ✅ **Simple** - 8 files total, easy to understand
- ✅ **Clean** - No confusing packages or abstractions
- ✅ **Scalable** - Ready for 100+ categories
- ✅ **Maintainable** - Easy to modify and extend
- ✅ **Professional** - Follows best practices without over-engineering

**Ready to build your massive plugin!** 🚀

