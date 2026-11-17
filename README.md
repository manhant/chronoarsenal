# Chrono Arsenal (NeoForge 1.21.1)

Ten weapons. Hold one and press **I** or **R** (rebindable in Controls > Chrono Arsenal).
Everything is server-side with cooldowns, so it works in multiplayer.

| Weapon | I | R |
|---|---|---|
| Time Scythe | Time Domain (white 10x10x10 room, no leaving / teleporting / block editing, toggle to close) | Aerial cross slash, 30 blocks, 7 hearts |
| Venom Gate Gun | Poison cloud at aim point | 5-shot burst (right click = single shot) |
| Lion Sun Axe | Solar Roar: fire blast + knockback | Sun Leap + slam |
| Blood Rage Sword | Blood Rage buffs (costs 3 hearts) | Crimson Sweep, heals per hit (lifesteal on every hit) |
| Ragnarok Spear | 100 lightning strikes in an area | Chain lightning on target |
| Claws of Pain | 6 rapid slashes (hits apply Wither) | Pounce to target |
| Staff of Mikulak | Evoker fang lines | Arcane Storm |
| Veid Katana | Phantom Dash | Iaido slash |
| Tra God Bow | Arrow Rain (right click = fast arrow, no ammo) | Triple volley |
| Pride Knife | Shadow Step (invisible + speed) | Backstab teleport (backstab bonus damage) |

All weapons: unbreakable, fire/lava proof, never drop on death (returned on respawn).
The old dash from your first version lives in `core/DashUtil.java` (now used by nobody, kept for reuse).

## Build
Needs JDK 21 and internet (first build downloads NeoForge + Minecraft).

    gradle wrapper      # once, creates ./gradlew
    ./gradlew build     # jar appears in build/libs/
    ./gradlew runClient # test in game

Textures are 16x16 placeholders: replace the PNGs in `assets/chronoarsenal/textures/item/`.
Numbers (damage, cooldowns in ticks, ranges) are at the top of each file in `item/`.
