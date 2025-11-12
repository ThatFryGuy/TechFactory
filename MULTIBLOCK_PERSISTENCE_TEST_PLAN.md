# Multiblock Persistence & Click-to-Craft Test Plan

## 🎯 Overview
This test plan verifies that all multiblocks work with Slimefun-style click-to-craft mechanics and that custom inventories persist across server restarts.

---

## ✅ Test 1: Basic Workbench - Click-to-Craft

**Structure:** Crafting Table → Dispenser

### Steps:
1. Build the Basic Workbench structure
2. Right-click any part → Should see "✓ Successfully built: Basic Workbench"
3. Right-click Dispenser → Should see "Basic Workbench opened!" + GUI opens
4. Add a valid recipe (e.g., 9 coal for Compressed Carbon)
5. Close GUI
6. Right-click Crafting Table → Should see "✓ Crafted [item]!"
7. Open Dispenser → Verify crafted item is there

**Expected Results:**
- ✅ "Successfully built" message on first build
- ✅ "Opened" message when clicking dispenser
- ✅ Crafting only happens when clicking crafting table
- ✅ Success message shows crafted item
- ✅ Invalid recipe shows "✗ Invalid recipe!"
- ✅ Empty dispenser shows "The Basic Workbench is empty!"

---

## ✅ Test 2: Compressor - Click-to-Craft

**Structure:** Fence → Dispenser + Pistons

### Steps:
1. Build the Compressor structure
2. Right-click any part → "✓ Successfully built: Compressor"
3. Right-click Dispenser → "Compressor opened!" + GUI opens
4. Add 9 coal
5. Close GUI
6. Right-click Fence → "✓ Compressed 9x Coal into 1x Compressed Carbon!"
7. Open Dispenser → Verify Compressed Carbon is there

**Expected Results:**
- ✅ Click fence triggers crafting (not dispenser)
- ✅ Items persist in dispenser (Minecraft handles this)

---

## ✅ Test 3: Ore Crusher - Click-to-Craft

**Structure:** Nether Brick Fence + Dispenser + Iron Bars

### Steps:
1. Build the Ore Crusher structure
2. Right-click any part → "✓ Successfully built: Ore Crusher"
3. Right-click Dispenser → "Ore Crusher opened!" + GUI opens
4. Add 16 netherrack
5. Close GUI
6. Right-click Nether Brick Fence → "✓ Crushed Sulfate!"
7. Open Dispenser → Verify Sulfate is there

**Expected Results:**
- ✅ Click nether brick fence triggers crafting
- ✅ Items persist in dispenser

---

## ✅ Test 4: Pressure Chamber - Click-to-Craft

**Structure:** Dispenser + Slabs + Glass + Pistons + Cauldron

### Steps:
1. Build the Pressure Chamber structure
2. Right-click any part → "✓ Successfully built: Pressure Chamber"
3. Right-click Dispenser → "Pressure Chamber opened!" + GUI opens
4. Add valid recipe
5. Close GUI
6. Right-click Glass → Should see crafting message
7. Open Dispenser → Verify crafted item is there

**Expected Results:**
- ✅ Click glass triggers crafting
- ✅ Items persist in dispenser

---

## ✅ Test 5: Ore Washer - Click-to-Craft

**Structure:** Dispenser → Fence → Cauldron

### Steps:
1. Build the Ore Washer structure
2. Right-click any part → "✓ Successfully built: Ore Washer"
3. Right-click Dispenser → "Ore Washer opened!" + GUI opens
4. Add valid recipe
5. Close GUI
6. Right-click Fence → Should see crafting message
7. Open Dispenser → Verify crafted item is there

**Expected Results:**
- ✅ Click fence triggers crafting
- ✅ Items persist in dispenser

---

## ✅ Test 6: Smelter - Click-to-Craft + Database Persistence

**Structure:** Blast Furnace + Iron Bars + Bricks + Campfire

### Steps:
1. Build the Smelter structure
2. Right-click any part → "✓ Successfully built: Smelter"
3. Right-click Blast Furnace → "Smelter opened!" + GUI opens
4. Add valid recipe (e.g., Copper Dust + Tin Dust + Copper Ingot for Bronze)
5. Close GUI
6. Right-click Iron Bars → "Smelting Bronze Ingot... Time: 1.3 seconds"
7. Wait for completion → Bronze Ingot appears

**Expected Results:**
- ✅ Click iron bars triggers crafting (not blast furnace)
- ✅ Custom inventory works correctly
- ✅ Items saved when GUI closes

### **CRITICAL TEST - Database Persistence:**
8. Add items to Smelter (don't craft yet)
9. Close GUI
10. **Restart the server** (or reload plugin)
11. Right-click Blast Furnace → GUI opens
12. **Verify items are still there!** ✅

**This is the key test - items MUST survive server restart!**

---

## ✅ Test 7: Automated Panning Machine - Click-to-Craft + Database Persistence

**Structure:** Trapdoor → Cauldron

### Steps:
1. Build the Automated Panning Machine structure
2. Right-click any part → "✓ Successfully built: Automated Panning Machine"
3. Right-click Cauldron → "Automated Panning Machine opened!" + GUI opens
4. Add valid items (e.g., gravel, sand, dirt)
5. Close GUI
6. Right-click Trapdoor → Should see processing messages
7. Open Cauldron → Verify output items

**Expected Results:**
- ✅ Click trapdoor triggers crafting (not cauldron)
- ✅ Custom inventory works correctly
- ✅ Items saved when GUI closes

### **CRITICAL TEST - Database Persistence:**
8. Add items to Automated Panning Machine (don't craft yet)
9. Close GUI
10. **Restart the server** (or reload plugin)
11. Right-click Cauldron → GUI opens
12. **Verify items are still there!** ✅

---

## 🔥 Critical Tests - Must Pass!

### Test A: Server Crash Recovery (Smelter)
1. Add expensive items to Smelter (e.g., rare dusts)
2. Close GUI
3. **Kill server process** (simulate crash)
4. Restart server
5. Open Smelter → **Items MUST be there!**

### Test B: Server Crash Recovery (Automated Panning)
1. Add items to Automated Panning Machine
2. Close GUI
3. **Kill server process**
4. Restart server
5. Open Automated Panning Machine → **Items MUST be there!**

### Test C: Multiple Players
1. Player A opens Smelter, adds items, closes GUI
2. Player B opens same Smelter → Should see Player A's items
3. Player B clicks iron bars → Crafts using Player A's items
4. Both players should see consistent state

### Test D: Empty Inventory Handling
1. Open any multiblock
2. Don't add any items
3. Close GUI
4. Click trigger block → Should see "The [Multiblock] is empty!"

### Test E: Invalid Recipe Handling
1. Open any multiblock
2. Add random items (not a valid recipe)
3. Close GUI
4. Click trigger block → Should see "✗ Invalid recipe! Check the guide..."

---

## 📊 Expected Behavior Summary

| Multiblock | GUI Block | Trigger Block | Persistence Method |
|------------|-----------|---------------|-------------------|
| Basic Workbench | Dispenser | Crafting Table | Minecraft (Physical) |
| Compressor | Dispenser | Fence | Minecraft (Physical) |
| Ore Crusher | Dispenser | Nether Brick Fence | Minecraft (Physical) |
| Pressure Chamber | Dispenser | Glass | Minecraft (Physical) |
| Ore Washer | Dispenser | Fence | Minecraft (Physical) |
| **Smelter** | Blast Furnace | Iron Bars | **Database** |
| **Automated Panning** | Cauldron | Trapdoor | **Database** |

---

## 🐛 Known Issues to Watch For

1. **Items disappearing after restart** → Database persistence not working
2. **Crafting happens on GUI close** → Old behavior, should be fixed
3. **No "opened" message** → Missing feedback
4. **Can't click trigger block** → Event handler not registered
5. **Lag when closing GUI** → Database save should be async
6. **Items duplicating** → Cache/database sync issue

---

## ✅ Success Criteria

All tests must pass:
- ✅ All multiblocks show "Successfully built" on first build
- ✅ All multiblocks show "Opened" when GUI opens
- ✅ Crafting only happens when clicking trigger block
- ✅ Empty inventories show appropriate message
- ✅ Invalid recipes show appropriate message
- ✅ **Smelter items survive server restart**
- ✅ **Automated Panning items survive server restart**
- ✅ No lag when closing inventories
- ✅ Multiple players can use same multiblock
- ✅ Items don't duplicate or disappear

---

## 🎮 Quick Test Commands

```
/techfactory give <player> coal 64
/techfactory give <player> copper_dust 10
/techfactory give <player> tin_dust 10
/techfactory give <player> copper_ingot 10
/techfactory give <player> gravel 64
/techfactory give <player> netherrack 64
```

---

## 📝 Test Results Template

```
[ ] Test 1: Basic Workbench - PASS/FAIL
[ ] Test 2: Compressor - PASS/FAIL
[ ] Test 3: Ore Crusher - PASS/FAIL
[ ] Test 4: Pressure Chamber - PASS/FAIL
[ ] Test 5: Ore Washer - PASS/FAIL
[ ] Test 6: Smelter - PASS/FAIL
[ ] Test 7: Automated Panning - PASS/FAIL
[ ] Test A: Smelter Crash Recovery - PASS/FAIL
[ ] Test B: Panning Crash Recovery - PASS/FAIL
[ ] Test C: Multiple Players - PASS/FAIL
[ ] Test D: Empty Inventory - PASS/FAIL
[ ] Test E: Invalid Recipe - PASS/FAIL
```

---

Good luck with testing! 🚀

