# Gold Pan & Ore Processing - Quick Start Guide

## Overview
TechFactory now has THREE ways to process gravel and ore dust:
1. **Gold Pan** - Manual tool (quick, random outputs)
2. **Automated Panning Machine** - Multiblock (reliable, consistent)
3. **Ore Washer** - Multiblock (converts dust to metals)

---

## 🛠️ Gold Pan (Tool)

### Crafting
```
At: Basic Workbench
Recipe:
  Stone    | Bowl     | Stone
  Stone    | Stone    | Stone
Output: 1x Gold Pan
```

### How to Use
1. Hold the Gold Pan in your hand
2. Right-click on any gravel block
3. Random item drops and gravel disappears

### Outputs (Random)
- **40%** Iron Nugget
- **35%** Sifted Ore Dust ← Most useful!
- **15%** Clay Ball
- **10%** Flint

### Pros & Cons
✅ No building required - instant use
✅ Can carry multiple
✅ Uses gravel (renewable)
❌ Random outputs - only 35% chance for ore dust
❌ Manual - one at a time

---

## ⚙️ Automated Panning Machine (Multiblock)

### Structure
Build this shape:
```
    [Trapdoor]
    [Cauldron]
```

Use ANY type of trapdoor (Oak, Spruce, Iron, etc.)

### Recipe
First, craft at Basic Workbench - shows structure diagram

### How to Use
1. Right-click the **Cauldron** to open inventory
2. Place gravel in the inventory  
3. Close the inventory
4. Gravel → Sifted Ore Dust (1:1 ratio)
5. Output goes to adjacent chest or drops on ground

### Pros & Cons
✅ 100% reliable - all gravel becomes dust
✅ Automatic processing
✅ Can place chest next to it for auto-collection
❌ Requires building
❌ Can only process gravel (not other items)

---

## 💧 Ore Washer (Multiblock)

### Structure
Build this shape:
```
    [Dispenser]
    [Fence]
    [Cauldron]
```

Use ANY type of fence and ANY type of trapdoor

### Recipe
First, craft at Basic Workbench - shows structure diagram

### How to Use
1. Right-click the **Dispenser** to open inventory
2. Place Sifted Ore Dust in the inventory
3. Close the inventory
4. Dust → Random Metal Dusts
5. Output goes to adjacent chest or drops on ground

### Outputs (Random Metal Dusts)
- Iron Dust
- Copper Dust
- Gold Dust
- Tin Dust
- Silver Dust
- Aluminum Dust
- Lead Dust
- Zinc Dust
- Magnesium Dust

### Pros & Cons
✅ High-yield - each dust gets converted
✅ Automatic processing
✅ Can chain with Automated Panning Machine
❌ Requires building

---

## 📊 Processing Chain

### Method 1: Manual (Slow)
```
Gravel → (Gold Pan) → Random Items
                   ↓ (35% chance)
              Sifted Ore Dust
```

### Method 2: Automated (Best for large scale)
```
Gravel → [Automated Panning Machine] → Sifted Ore Dust
                                             ↓
                                      [Ore Washer]
                                             ↓
                                      Metal Dusts
```

### Recommended Setup
```
    [Chest] ← outputs ore dust
       ↑
[Automated Panning Machine]
       ↑
Gravel hopper input

    [Chest] ← outputs dusts
       ↑
    [Ore Washer]
       ↑
Dust hopper input
```

---

## 📝 Useful Tips

1. **Chest Placement**
   - Place a chest touching any part of the multiblock
   - Output will go to the chest instead of ground
   - Perfect for auto-sorting systems

2. **Gold Pan Efficiency**
   - Use when you need quick items early game
   - Combine with Automated Panning Machine later
   - Iron Nuggets are useful for crafting

3. **Scaling**
   - Build multiple Automated Panning Machines for faster processing
   - Use hoppers on top for automatic input
   - Use droppers for timed feeding

4. **Item Storage**
   - NBT-tagged items - can't fake with anvils
   - Store in chests or hoppers normally
   - Works with Shulker boxes

---

## ⚡ Next Steps

After processing ore dust:
1. Use Ore Washer to get metal dusts
2. Smelt at a Smelter (grind dustto get metals)
3. Craft into ingots and tools
4. Build industrial machines

---

## 🐛 Troubleshooting

**Gold Pan doesn't work?**
- Make sure you're holding it in your main hand
- Right-click ON gravel, not air
- Check that the gravel is fully loaded (not underground chunks)

**Multiblocks not working?**
- Check structure exactly - blocks must be in perfect alignment
- Try breaking and rebuilding
- Make sure you're clicking the right block (see "How to Use")

**Items not outputting to chest?**
- Chest must be adjacent (touching sides)
- Try different sides/directions
- Items will drop on ground if no chest available

---

## 📚 Related Items

- **Sifted Ore Dust** - Made by Automated Panning Machine
- **Metal Dusts** - Made by Ore Washer (Iron, Copper, Gold, etc.)
- **Metal Ingots** - Made by Smelter from dusts
- **Ore Blocks** - Mined with pickaxe for raw materials

---

**Last Updated:** Post-Implementation
**Version:** TechFactory with Gold Pan System