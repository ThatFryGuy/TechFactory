# Gold Pan & Ore Washer Implementation

## Summary of Changes

This implementation completes the renaming and restructuring of the Auto Panning Machine into two separate multiblocks and adds a Gold Pan tool.

---

## 1. **Ore Washer** (Renamed from Auto Panning Machine)
**Purpose**: Convert Sifted Ore Dust → Random Metal Dusts

### Structure
```
[Dispenser]  ← Player interacts here
[Fence]      ← Any type of fence
[Cauldron]   ← Bottom
```

### Files
- **Recipe**: `recipes/workstations/multiblocks/OreWasher.java`
- **Functionality**: `workstations/multiblocks/OreWasherMachine.java`
- **Crafting**: Basic Workbench (shows structure diagram)

### Recipe
- Input: Sifted Ore Dust
- Output: Random Metal Dusts (Iron, Copper, Gold, Tin, Silver, Aluminum, Lead, Zinc, Magnesium)

---

## 2. **Automated Panning Machine** (New)
**Purpose**: Convert Gravel → Sifted Ore Dust

### Structure
```
[Trapdoor]   ← Any type of trapdoor
[Cauldron]   ← Player interacts here
```

### Files
- **Recipe**: `recipes/workstations/multiblocks/AutomatedPanningMachine.java`
- **Functionality**: `workstations/multiblocks/AutomatedPanningMachineMachine.java`
- **Crafting**: Basic Workbench (shows structure diagram)

### Recipe
- Input: Gravel
- Output: Sifted Ore Dust (1:1 ratio)

### Implementation Details
- Uses custom inventory with 9 slots
- Stores cauldron location in custom `AutomatedPanningInventoryHolder`
- Outputs to adjacent chest or drops on ground if no chest available

---

## 3. **Gold Pan** (New Tool)
**Purpose**: Manual tool for panning gravel into resources

### Recipe
```
[Stone] [Bowl]  [Stone]
[Stone] [Stone] [Stone]
```

### Crafting
- Crafted at: Basic Workbench
- Result: 1x Gold Pan

### Functionality
Right-click on gravel blocks while holding Gold Pan to get random outputs:
- 40% chance: Iron Nugget
- 35% chance: Sifted Ore Dust
- 15% chance: Clay Ball
- 10% chance: Flint

### Files
- **Recipe**: `recipes/tools/GoldPan.java`
- **Handler**: `listeners/ToolListener.java` (new)

### How It Works
1. Player right-clicks gravel while holding Gold Pan
2. Random item drops at the gravel location
3. The gravel block is removed
4. Tool has no cooldown (instant use)

---

## 4. **System Integration**

### Event Listeners
Updated `listeners/MultiblockListener.java` to handle:
- Ore Washer (dispenser on fence on cauldron)
- Automated Panning Machine (trapdoor on cauldron)

Created new `listeners/ToolListener.java` for:
- Gold Pan right-click on gravel

### Plugin Registration
Updated `TechFactory.java` to register ToolListener on plugin startup.

---

## 5. **Backward Compatibility**

### Auto Panning Machine (Legacy)
The original Auto Panning Machine still exists but is kept only for backward compatibility with existing worlds. It is NOT the primary implementation anymore.

- Still exists in codebase for worlds that have it built
- New builds should use **Ore Washer** + **Automated Panning Machine** instead
- Both machines have same structure (Dispenser/Fence/Cauldron)

---

## 6. **Crafting Recipes Summary**

### Gold Pan
```
Material: Stone Bowl
Text: "A tool for panning gravel"
Crafting Pattern:
  Stone | Bowl  | Stone
  Stone | Stone | Stone
Crafted at: Basic Workbench
```

### Ore Washer (Structure)
```
Material: Cauldron  
Text: "A multiblock machine for washing ore dust"
Pattern:
  [empty] | Dispenser | [empty]
  [empty] | Fence     | [empty]
  [empty] | Cauldron  | [empty]
Built In-Game
```

### Automated Panning Machine (Structure)
```
Material: Cauldron
Text: "Automated version of the Gold Pan"
Pattern:
  [empty] | Trapdoor | [empty]
  [empty] | Cauldron | [empty]
Built In-Game
```

---

## 7. **Usage Flow**

### Getting Sifted Ore Dust
**Option A - Manual (Quick)**
1. Hold Gold Pan
2. Right-click gravel
3. Get random resource (40% chance for Sifted Ore Dust)

**Option B - Automated Panning Machine (Reliable)**
1. Build multiblock: Trapdoor on Cauldron
2. Right-click cauldron
3. Place gravel in inventory
4. Close inventory
5. Get Sifted Ore Dust (100% conversion)

### Converting Sifted Ore Dust to Metal Dusts
1. Build Ore Washer: Dispenser on Fence on Cauldron
2. Right-click dispenser
3. Place Sifted Ore Dust
4. Close inventory
5. Get random metal dusts

---

## 8. **Testing Checklist**

- [ ] Gold Pan crafts correctly at Basic Workbench
- [ ] Gold Pan right-click on gravel works and drops items
- [ ] Automated Panning Machine structure validates correctly
- [ ] Automated Panning Machine opens inventory correctly
- [ ] Automated Panning Machine converts gravel to sifted ore dust
- [ ] Ore Washer structure validates correctly
- [ ] Ore Washer opens inventory correctly
- [ ] Ore Washer converts sifted ore dust to metal dusts
- [ ] Output to adjacent chest works for both machines
- [ ] Items drop on ground if no chest available

---

## 9. **Files Modified/Created**

### Created
- `listeners/ToolListener.java` - New tool event handler
- `workstations/multiblocks/AutomatedPanningMachineMachine.java` - New (already created by assistant)
- `recipes/workstations/multiblocks/AutomatedPanningMachine.java` - New (already created by assistant)
- `workstations/multiblocks/OreWasherMachine.java` - Renamed/refactored (already created by assistant)
- `recipes/workstations/multiblocks/OreWasher.java` - Renamed (already created by assistant)
- `recipes/tools/GoldPan.java` - New tool (already created by assistant)

### Modified
- `listeners/MultiblockListener.java` - Added handlers for new multiblocks
- `TechFactory.java` - Registered ToolListener

---

## 10. **Notes**

1. **Item ID Validation**: All custom items use NBT data for validation (PersistentDataContainer) to prevent players from faking items with anvil renames.

2. **Inventory Tracking**: AutomatedPanningMachine uses a custom InventoryHolder to properly track which cauldron the player opened.

3. **Output Flexibility**: Both multiblocks prefer to output to adjacent chests but will fall back to dropping items on the ground or storing in the machine if no chest is available.

4. **Performance**: No significant performance impact - all operations are instant (no async tasks).