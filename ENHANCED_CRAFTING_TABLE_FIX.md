# Enhanced Crafting Table Display Name Fix

## 🐛 Issue
Recipe GUI was showing "Basic Workbench" instead of "Enhanced Crafting Table" for recipes that require the Enhanced Crafting Table.

## ✅ Root Cause
Recipe items were returning `"Basic Workbench"` in their `getMachineType()` method, but the GUI was looking for `"Enhanced Crafting Table"` to display the correct icon and enable navigation.

---

## 🔧 Files Modified

### 1. GUI System
**File:** `src/main/java/org/ThefryGuy/techFactory/gui/RecipeMenu.java`

**Change:** Added "Enhanced Crafting Table" case to `getMachineIcon()` method

```java
// Before:
case "Basic Workbench" -> Material.CRAFTING_TABLE;

// After:
case "Enhanced Crafting Table" -> Material.CRAFTING_TABLE;
```

**Impact:** Recipe GUI now correctly displays "Enhanced Crafting Table" icon

---

### 2. Component Recipes (7 files)
All component recipes updated to return `"Enhanced Crafting Table"`:

1. `src/main/java/org/ThefryGuy/techFactory/recipes/components/AdvancedCircuitBoard.java`
2. `src/main/java/org/ThefryGuy/techFactory/recipes/components/BasicCircuitBoard.java`
3. `src/main/java/org/ThefryGuy/techFactory/recipes/components/Battery.java`
4. `src/main/java/org/ThefryGuy/techFactory/recipes/components/CopperWire.java`
5. `src/main/java/org/ThefryGuy/techFactory/recipes/components/ElectricMotor.java`
6. `src/main/java/org/ThefryGuy/techFactory/recipes/components/Electromagnet.java`
7. `src/main/java/org/ThefryGuy/techFactory/recipes/components/HeatingCoil.java`

**Change:**
```java
@Override
public String getMachineType() {
    return "Enhanced Crafting Table";  // Was "Basic Workbench"
}
```

---

### 3. Energy Recipes (1 file)
**File:** `src/main/java/org/ThefryGuy/techFactory/recipes/energy/EnergyRegulator.java`

**Change:** Same as components - returns `"Enhanced Crafting Table"`

---

### 4. Resource Recipes (1 file)
**File:** `src/main/java/org/ThefryGuy/techFactory/recipes/resources/CarbonChunk.java`

**Changes:**
1. Updated `getMachineType()` to return `"Enhanced Crafting Table"`
2. Updated lore text:
```java
// Before:
ChatColor.GRAY + "Crafted in: Basic Workbench"

// After:
ChatColor.GRAY + "Crafted in: Enhanced Crafting Table"
```

---

### 5. Tool Recipes (1 file)
**File:** `src/main/java/org/ThefryGuy/techFactory/recipes/tools/GoldPan.java`

**Changes:**
1. Updated `getMachineType()` to return `"Enhanced Crafting Table"`
2. Updated lore text:
```java
// Before:
ChatColor.DARK_GRAY + "Crafted at: Basic Workbench"

// After:
ChatColor.DARK_GRAY + "Crafted at: Enhanced Crafting Table"
```

---

### 6. Multiblock Listener
**File:** `src/main/java/org/ThefryGuy/techFactory/listeners/MultiblockListener.java`

**Change:** Updated display name mapping in `getMultiblockDisplayName()` method

```java
// Added:
case "enhanced_crafting_table" -> "Enhanced Crafting Table";
case "basic_workbench" -> "Enhanced Crafting Table"; // Legacy support
```

**Impact:** When players build the Enhanced Crafting Table, they now see:
```
✓ Successfully built: Enhanced Crafting Table
```
Instead of:
```
✓ Successfully built: Basic Workbench
```

---

## 📊 Summary

| Category | Files Changed | Change Type |
|----------|---------------|-------------|
| GUI | 1 | Icon mapping |
| Components | 7 | getMachineType() |
| Energy | 1 | getMachineType() |
| Resources | 1 | getMachineType() + lore |
| Tools | 1 | getMachineType() + lore |
| Listeners | 1 | Display name mapping |
| **TOTAL** | **12 files** | **Consistent naming** |

---

## 🎯 What's Fixed

### Before:
- ❌ Recipe GUI showed "Basic Workbench"
- ❌ Clicking machine icon didn't navigate properly
- ❌ Item lore said "Basic Workbench"
- ❌ Build message said "Basic Workbench"
- ❌ Inconsistent naming throughout codebase

### After:
- ✅ Recipe GUI shows "Enhanced Crafting Table"
- ✅ Clicking machine icon navigates to Enhanced Crafting Table recipe
- ✅ Item lore says "Enhanced Crafting Table"
- ✅ Build message says "Enhanced Crafting Table"
- ✅ Consistent naming throughout codebase

---

## 📝 Technical Notes

### Class Names (Not Changed)
The following class names were **intentionally NOT changed** for backwards compatibility:
- `BasicWorkbench.java` - Recipe definition class
- `BasicWorkbenchMachine.java` - Machine functionality class

These are internal class names and don't affect the player-facing display names.

### Database Compatibility
The `DatabaseManager.java` already has legacy support for both IDs:
```java
case "enhanced_crafting_table":
case "basic_workbench": // Legacy support
    return BasicWorkbenchMachine.isValidStructure(block);
```

This ensures old databases with `"basic_workbench"` entries will still work.

### Multiblock Registration
The multiblock is registered with ID `"enhanced_crafting_table"`:
```java
registerMultiblock(clicked, "enhanced_crafting_table", player);
```

---

## 🧪 Testing Checklist

After rebuilding the plugin, verify:

### Recipe GUI
- [ ] Open `/tf guide`
- [ ] Click on "Battery" recipe
- [ ] Verify machine shows: **"Enhanced Crafting Table"** (not "Basic Workbench")
- [ ] Click the machine icon
- [ ] Verify it navigates to Enhanced Crafting Table recipe

### Item Lore
- [ ] Get a Battery item
- [ ] Check lore shows: **"Crafted in: Enhanced Crafting Table"**
- [ ] Get a Gold Pan item
- [ ] Check lore shows: **"Crafted at: Enhanced Crafting Table"**

### Building Multiblock
- [ ] Place Dispenser on ground
- [ ] Place Crafting Table on top
- [ ] Right-click Crafting Table
- [ ] Verify message: **"✓ Successfully built: Enhanced Crafting Table"**

### Recipe Functionality
- [ ] Place items in Enhanced Crafting Table
- [ ] Close inventory
- [ ] Verify crafting still works correctly
- [ ] All recipes should function normally

---

## 🔄 Build Instructions

To update the class files:

### Using IntelliJ IDEA:
1. **Build > Build Project** (Ctrl+F9)
2. Wait for compilation to complete
3. Check for any errors in the Build output

### Using Gradle:
```bash
# Clean and rebuild
gradlew.bat clean build

# Or just compile
gradlew.bat compileJava
```

### Deploy:
1. Copy `build/libs/TechFactory-1.0.jar` to your server's `plugins/` folder
2. Restart server or use `/reload confirm`
3. Test the changes

---

## ✨ Result

All Enhanced Crafting Table recipes now display correctly with consistent naming:
- **Recipe GUI:** "Enhanced Crafting Table"
- **Item Lore:** "Enhanced Crafting Table"
- **Build Message:** "Enhanced Crafting Table"
- **Machine Navigation:** Works correctly

The naming is now consistent across the entire plugin! 🎉

