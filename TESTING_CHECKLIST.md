# Testing Checklist - Async Performance Improvements

## 🧪 Pre-Testing Setup

- [ ] Compile the plugin: `mvn clean package`
- [ ] Backup existing database: `plugins/TechFactory/techfactory.db`
- [ ] Start test server with the new plugin
- [ ] Check console for startup messages

### Expected Startup Messages
```
[TechFactory] Database initialized successfully!
[TechFactory] Loaded X placed blocks and Y multiblocks.
[TechFactory] Energy Manager started (async mode)!
[TechFactory] Smelting Manager started (checking every 5 ticks)
[TechFactory] Auto-save started! Saving every 5 minutes.
```

---

## ✅ Basic Functionality Tests

### 1. Energy Regulator Tests
- [ ] Place an Energy Regulator
  - Should see: "Energy Regulator placed successfully!"
  - Should see: "Energy network created!"
  - Hologram should appear above block
- [ ] Right-click Energy Regulator
  - GUI should open instantly
  - Should show energy stats
- [ ] Break Energy Regulator
  - Should see: "Energy Regulator removed!"
  - Should see: "Energy network destroyed"
  - Hologram should disappear
  - Should drop Energy Regulator item

### 2. Multiblock Tests
- [ ] Build a Smelter multiblock
  - Should see: "✓ Successfully built: Smelter"
  - Should register in database
- [ ] Open Smelter GUI
  - Should open instantly
  - Items should persist after closing
- [ ] Break Smelter
  - Should see: "Multiblock destroyed!"
  - Should remove from database

### 3. Database Persistence Tests
- [ ] Place 5 Energy Regulators
- [ ] Build 3 multiblocks
- [ ] Restart server
- [ ] Verify all blocks loaded correctly
- [ ] Check console: "Loaded X placed blocks and Y multiblocks"

---

## 🚀 Performance Tests

### 1. Load Test - Energy Networks
**Goal:** Verify no lag with many energy networks

Steps:
1. Place 50 Energy Regulators in different locations
2. Monitor TPS: `/tps`
3. Check hologram updates are smooth
4. Verify no console errors

**Expected Results:**
- TPS should stay at 20.0
- No lag spikes
- All holograms update every second
- Console shows: "Loaded 50 energy networks from database"

### 2. Load Test - Multiblocks
**Goal:** Verify no lag with many multiblocks

Steps:
1. Build 20 different multiblocks
2. Open/close GUIs rapidly
3. Monitor TPS: `/tps`
4. Verify no console errors

**Expected Results:**
- TPS should stay at 20.0
- GUIs open instantly
- No lag when opening/closing
- All multiblocks register correctly

### 3. Load Test - Smelting Operations
**Goal:** Verify optimized smelting manager

Steps:
1. Start 10 smelting operations simultaneously
2. Monitor TPS: `/tps`
3. Wait for operations to complete
4. Verify all outputs are correct

**Expected Results:**
- TPS should stay at 20.0
- All operations complete successfully
- No lag during smelting
- Console shows: "Smelting Manager started (checking every 5 ticks)"

### 4. Stress Test - Concurrent Operations
**Goal:** Verify thread safety

Steps:
1. Have 3+ players online
2. All players place/break blocks simultaneously
3. All players build/destroy multiblocks simultaneously
4. Monitor for errors

**Expected Results:**
- No duplicate entries in database
- No race condition errors
- All operations complete successfully
- TPS stays stable

---

## 🔍 Database Integrity Tests

### 1. Check for Duplicates
**SQL Query:**
```sql
-- Check for duplicate placed blocks
SELECT world_name, x, y, z, COUNT(*) as count
FROM placed_blocks
GROUP BY world_name, x, y, z
HAVING count > 1;

-- Check for duplicate multiblocks
SELECT world_name, x, y, z, COUNT(*) as count
FROM multiblocks
GROUP BY world_name, x, y, z
HAVING count > 1;
```

**Expected:** No results (no duplicates)

### 2. Verify Cache Consistency
Steps:
1. Place 10 blocks
2. Check database count
3. Check in-game count
4. Verify they match

**Expected:** Database count = In-game count

### 3. Test Async Rollback
Steps:
1. Simulate database failure (disconnect DB)
2. Try to place a block
3. Verify cache is rolled back

**Expected:** Block placement fails gracefully

---

## 🎯 Edge Case Tests

### 1. Rapid Placement/Breaking
- [ ] Place and immediately break a block
- [ ] Verify no ghost entries in database
- [ ] Verify no memory leaks

### 2. Server Restart During Operation
- [ ] Start a smelting operation
- [ ] Restart server mid-operation
- [ ] Verify operation state is handled correctly

### 3. World Unload
- [ ] Place blocks in multiple worlds
- [ ] Unload a world
- [ ] Verify no errors
- [ ] Reload world and verify blocks still exist

### 4. Chunk Unload
- [ ] Place blocks
- [ ] Move far away (unload chunks)
- [ ] Return and verify blocks still work

---

## 📊 Performance Monitoring

### Commands to Use
```
/tps - Check server TPS
/timings - Detailed performance report
/plugins - Verify TechFactory is loaded
```

### What to Monitor
- **TPS:** Should stay at 20.0
- **Memory:** Should not increase over time
- **CPU:** Should be lower than before
- **Console:** No errors or warnings

### Performance Benchmarks

| Metric | Target | Actual |
|--------|--------|--------|
| TPS with 100 energy blocks | 20.0 | _____ |
| TPS with 50 multiblocks | 20.0 | _____ |
| TPS with 20 active smelters | 20.0 | _____ |
| Block placement lag | <1ms | _____ |
| Database write time | <50ms | _____ |

---

## 🐛 Known Issues to Watch For

### Potential Problems
1. **Race Conditions**
   - Symptom: Duplicate database entries
   - Check: Run duplicate SQL query
   - Fix: Already using ConcurrentHashMap

2. **Memory Leaks**
   - Symptom: Memory usage increases over time
   - Check: Monitor heap usage
   - Fix: Verify cleanup methods are called

3. **Callback Timing**
   - Symptom: Actions happen out of order
   - Check: Console logs for timing
   - Fix: Use callbacks properly

4. **Entity Errors**
   - Symptom: Holograms don't update
   - Check: Verify main thread scheduling
   - Fix: Already scheduled on main thread

---

## ✅ Success Criteria

### Must Pass
- [ ] All basic functionality tests pass
- [ ] TPS stays at 20.0 with 100+ blocks
- [ ] No database duplicates
- [ ] No console errors
- [ ] All blocks persist after restart

### Should Pass
- [ ] No lag with 500+ blocks
- [ ] Smooth operation with 10+ players
- [ ] Memory usage stable over time
- [ ] All edge cases handled gracefully

### Nice to Have
- [ ] TPS stays at 20.0 with 1000+ blocks
- [ ] No lag with 50+ concurrent operations
- [ ] Sub-millisecond block placement

---

## 📝 Test Results Template

### Test Date: ___________
### Tester: ___________
### Server Version: ___________
### Plugin Version: ___________

#### Results Summary
- Basic Functionality: ☐ Pass ☐ Fail
- Performance Tests: ☐ Pass ☐ Fail
- Database Integrity: ☐ Pass ☐ Fail
- Edge Cases: ☐ Pass ☐ Fail

#### Notes:
```
[Add any observations, issues, or comments here]
```

#### Issues Found:
1. 
2. 
3. 

#### Performance Metrics:
- Max TPS: _____
- Min TPS: _____
- Avg TPS: _____
- Max blocks tested: _____
- Max concurrent operations: _____

---

## 🔄 Regression Testing

After any code changes, re-run:
- [ ] Basic functionality tests
- [ ] Database persistence test
- [ ] Performance test with 50+ blocks
- [ ] Verify no new console errors

---

## 📞 Support

If you encounter issues:
1. Check console logs for errors
2. Verify database integrity
3. Test with minimal plugins
4. Report with full error logs and steps to reproduce

