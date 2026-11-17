package com.chronoarsenal.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;

/** Common item settings: single stack, epic rarity, fire/lava proof and unbreakable. */
public final class WeaponProps {

    private WeaponProps() {}

    public static Item.Properties base() {
        return new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    public static Item.Properties sword(int attackDamage, float attackSpeed) {
        return base().attributes(SwordItem.createAttributes(Tiers.NETHERITE, attackDamage, attackSpeed));
    }

    public static Item.Properties axe(float attackDamage, float attackSpeed) {
        return base().attributes(AxeItem.createAttributes(Tiers.NETHERITE, attackDamage, attackSpeed));
    }
}
