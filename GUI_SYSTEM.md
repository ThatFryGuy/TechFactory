# Simple GUI System 🎯

## Overview

Super simple, easy-to-understand GUI system. Just **6 files** total!

---

## 📁 Structure

```
gui/
├── Menu.java              # Base class - all menus extend this
├── MenuManager.java       # Handles clicks & navigation (the only event listener!)
├── GuideMenu.java         # Main menu (/techfactory guide)
├── ResourcesMenu.java     # Shows categories (Dusts, Alloys, etc.)
├── ItemListMenu.java      # Shows items in a category (with pagination)
└── RecipeMenu.java        # Shows how to craft an item
```

---

## 🎮 How It Works

### 1. Player runs command
```
/techfactory guide
```

### 2. GuideMenu opens
- Shows "Resources" button in center
- Click → Opens ResourcesMenu

### 3. ResourcesMenu opens
- Shows "Dusts" and "Alloys" buttons
- Has Back and Home buttons
- Click "Dusts" → Opens ItemListMenu

### 4. ItemListMenu opens
- Shows all items in that category
- 36 items per page (4 rows)
- Has pagination buttons (Previous/Next)
- Has Back and Home buttons
- Click an item → Opens RecipeMenu

### 5. RecipeMenu opens
- Shows the crafting recipe
- Shows machine, ingredients, and output
- Has Back and Home buttons

---

## 🔧 How Each File Works

### Menu.java (Base Class)
Every menu extends this. It provides:
- `build()` - Override this to add items to your menu
- `handleClick(slot, clicked)` - Override this to handle clicks
- `open()` - Call this to open the menu
- `setItem(slot, item)` - Add an item to the menu

**Example:**
```java
public class MyMenu extends Menu {
    public MyMenu(Player player) {
        super(player, "My Menu", 27);
    }
    
    @Override
    protected void build() {
        // Add items here
        setItem(13, new ItemStack(Material.DIAMOND));
    }
    
    @Override
    public void handleClick(int slot, ItemStack clicked) {
        if (slot == 13) {
            player.sendMessage("You clicked the diamond!");
        }
    }
}
```

### MenuManager.java (Event Listener)
This is the **only event listener** for the entire GUI system!
- Listens for inventory clicks
- Calls the correct menu's `handleClick()` method
- Manages navigation history (for back button)
- Manages pagination state (current page per category)

**Methods:**
- `goBack(player)` - Go to previous menu
- `goHome(player)` - Go to main guide menu
- `getCurrentPage(player, category)` - Get current page
- `setCurrentPage(player, category, page)` - Set current page

### GuideMenu.java (Main Menu)
The first menu players see.
- Shows main categories (Resources, etc.)
- Slot 13 = Resources button
- When clicked, opens ResourcesMenu

### ResourcesMenu.java (Category List)
Shows all resource categories.
- Slot 0 = Back button
- Slot 8 = Home button
- Slot 11 = Dusts button
- Slot 13 = Alloys button
- When clicked, opens ItemListMenu with that category's items

### ItemListMenu.java (Generic List)
**This is the magic!** One menu works for ALL categories.
- Shows 36 items per page
- Automatic pagination
- Works for Dusts, Alloys, Machines, Tools, anything!

**Usage:**
```java
new ItemListMenu(player, "Dusts", ItemRegistry.getDusts()).open();
new ItemListMenu(player, "Alloys", ItemRegistry.getAlloys()).open();
new ItemListMenu(player, "Machines", ItemRegistry.getMachines()).open();
```

### RecipeMenu.java (Recipe Display)
Shows how to craft an item.
- Top center = Machine icon
- Center = Recipe ingredients (3x3 grid)
- Right side = Output item
- Arrows point from ingredients to output

---

## ➕ Adding a New Category

Let's say you want to add "Machines":

### Step 1: Add button to ResourcesMenu
```java
// In ResourcesMenu.java build() method:
ItemStack machines = new ItemStack(Material.CRAFTING_TABLE);
ItemMeta meta = machines.getItemMeta();
meta.setDisplayName(ChatColor.BLUE + "Machines");
meta.setLore(List.of(
    ChatColor.GRAY + "View all machines",
    "",
    ChatColor.YELLOW + "Click to open"
));
machines.setItemMeta(meta);
setItem(15, machines); // Choose a slot
```

### Step 2: Handle the click
```java
// In ResourcesMenu.java handleClick() method:
case 15 -> { // Machines
    MenuManager.pushHistory(player, this);
    new ItemListMenu(player, "Machines", ItemRegistry.getMachines()).open();
}
```

### Step 3: Add to ItemRegistry
```java
// In ItemRegistry.java:
public static List<RecipeItem> getMachines() {
    // Return your list of machines
    List<RecipeItem> machines = new ArrayList<>();
    // Add machines here
    return machines;
}
```

**Done!** That's all you need. Pagination, navigation, everything works automatically.

---

## 🎯 Key Concepts

### Slot-Based Click Handling
Each menu knows which slot does what:
```java
@Override
public void handleClick(int slot, ItemStack clicked) {
    switch (slot) {
        case 0 -> MenuManager.goBack(player);      // Back button
        case 8 -> MenuManager.goHome(player);      // Home button
        case 11 -> openDustsMenu();                // Dusts button
        case 13 -> openAlloysMenu();               // Alloys button
    }
}
```

### Navigation History
When you open a new menu, push the current one to history:
```java
MenuManager.pushHistory(player, this);
new NextMenu(player).open();
```

Then the back button works automatically:
```java
MenuManager.goBack(player); // Goes to previous menu
```

### Pagination
ItemListMenu handles pagination automatically:
```java
int currentPage = MenuManager.getCurrentPage(player, categoryName);
// Show items for current page
// When next/prev clicked, update page and rebuild
```

---

## 🧪 Testing

Build the plugin:
```bash
mvn clean package
```

Test in-game:
1. `/techfactory guide` - Opens guide menu
2. Click "Resources" - Opens resources menu
3. Click "Dusts" - Opens dusts list
4. Test pagination (if you have 50+ dusts)
5. Click a dust - Opens recipe menu
6. Test Back button - Goes to previous menu
7. Test Home button - Goes to guide menu

---

## 🚀 Scaling Up

This system easily scales to a massive plugin:

### Add More Categories
Just add buttons to ResourcesMenu and handle the clicks. That's it!

### Add More Main Sections
Add buttons to GuideMenu (like "Machines", "Tools", etc.) and create menus for them.

### Add More Menu Types
Create new classes that extend Menu:
```java
public class ShopMenu extends Menu { ... }
public class UpgradeMenu extends Menu { ... }
public class CraftingMenu extends Menu { ... }
```

---

## 💡 Why This Works

### Simple
- No complex abstractions
- No button classes
- No action strings or NBT tags
- Just slot numbers and switch statements

### Clear
- Each menu is one file
- Easy to see what each slot does
- Easy to add new buttons

### Scalable
- One ItemListMenu works for ALL categories
- MenuManager handles all navigation
- Easy to add new menus and categories

### Maintainable
- Only 6 files total
- Each file has one clear purpose
- Easy to debug and modify

---

## 📝 Quick Reference

### Create a menu:
```java
public class MyMenu extends Menu {
    public MyMenu(Player player) {
        super(player, "Title", 27);
    }
    
    @Override
    protected void build() {
        // Add items
    }
    
    @Override
    public void handleClick(int slot, ItemStack clicked) {
        // Handle clicks
    }
}
```

### Open a menu:
```java
new MyMenu(player).open();
```

### Navigation:
```java
MenuManager.pushHistory(player, this);  // Before opening new menu
MenuManager.goBack(player);             // Back button
MenuManager.goHome(player);             // Home button
```

### Pagination:
```java
int page = MenuManager.getCurrentPage(player, "CategoryName");
MenuManager.setCurrentPage(player, "CategoryName", page + 1);
```

---

## 🎉 Summary

Your GUI system is:
- ✅ **Simple** - Just 6 files, easy to understand
- ✅ **Clean** - No unnecessary abstractions
- ✅ **Scalable** - Ready for 100+ categories
- ✅ **Maintainable** - Easy to modify and debug

**Perfect for a big project!** 🚀

