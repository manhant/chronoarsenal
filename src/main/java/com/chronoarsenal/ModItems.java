package com.chronoarsenal;

import com.chronoarsenal.item.BloodRageSwordItem;
import com.chronoarsenal.item.ClawsOfPainItem;
import com.chronoarsenal.item.LionSunAxeItem;
import com.chronoarsenal.item.PrideKnifeItem;
import com.chronoarsenal.item.RagnarokSpearItem;
import com.chronoarsenal.item.StaffOfMikulakItem;
import com.chronoarsenal.item.TimeScytheItem;
import com.chronoarsenal.item.TraGodBowItem;
import com.chronoarsenal.item.VeidKatanaItem;
import com.chronoarsenal.item.VenomGateGunItem;
import com.chronoarsenal.item.WeaponProps;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChronoArsenalMod.MODID);

    public static final DeferredItem<Item> TIME_SCYTHE = ITEMS.register("time_scythe",
            () -> new TimeScytheItem(WeaponProps.sword(4, -2.6f)));

    public static final DeferredItem<Item> VENOM_GATE_GUN = ITEMS.register("venom_gate_gun",
            () -> new VenomGateGunItem(WeaponProps.base()));

    public static final DeferredItem<Item> LION_SUN_AXE = ITEMS.register("lion_sun_axe",
            () -> new LionSunAxeItem(WeaponProps.axe(6.0f, -3.0f)));

    public static final DeferredItem<Item> BLOOD_RAGE_SWORD = ITEMS.register("blood_rage_sword",
            () -> new BloodRageSwordItem(WeaponProps.sword(4, -2.4f)));

    public static final DeferredItem<Item> RAGNAROK_SPEAR = ITEMS.register("ragnarok_spear",
            () -> new RagnarokSpearItem(WeaponProps.sword(5, -2.8f)));

    public static final DeferredItem<Item> CLAWS_OF_PAIN = ITEMS.register("claws_of_pain",
            () -> new ClawsOfPainItem(WeaponProps.sword(2, -1.0f)));

    public static final DeferredItem<Item> STAFF_OF_MIKULAK = ITEMS.register("staff_of_mikulak",
            () -> new StaffOfMikulakItem(WeaponProps.base()));

    public static final DeferredItem<Item> VEID_KATANA = ITEMS.register("veid_katana",
            () -> new VeidKatanaItem(WeaponProps.sword(3, -1.8f)));

    public static final DeferredItem<Item> TRA_GOD_BOW = ITEMS.register("tra_god_bow",
            () -> new TraGodBowItem(WeaponProps.base()));

    public static final DeferredItem<Item> PRIDE_KNIFE = ITEMS.register("pride_knife",
            () -> new PrideKnifeItem(WeaponProps.sword(1, -1.2f)));
}
