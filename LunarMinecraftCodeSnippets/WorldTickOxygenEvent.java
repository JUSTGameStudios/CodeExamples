package com.justgamestudios.lunarminecraft.events.world;

import com.justgamestudios.lunarminecraft.LunarMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;


@Mod.EventBusSubscriber(modid = LunarMinecraft.MOD_ID, bus = Bus.FORGE)
public class WorldTickOxygenEvent {

    //A Collection of the types of helmets we use.
    public static final Collection<ItemStack> helmetCollection = Arrays.asList(
            new ItemStack(Items.LEATHER_HELMET),
            new ItemStack(Items.CHAINMAIL_HELMET),
            new ItemStack(Items.GOLDEN_HELMET),
            new ItemStack(Items.IRON_HELMET),
            new ItemStack(Items.DIAMOND_HELMET)
        );
    private static int count = 0;

    @SubscribeEvent
    public static void tickEvent(TickEvent.WorldTickEvent event) {

        if(!event.world.isRemote){
            if(event.phase.equals(TickEvent.Phase.START)){
                count++;

                ServerWorld world = event.world.getServer().getWorld(DimensionType.OVERWORLD);

                //Ok do some oxygen stuff
                world.getPlayers().forEach(player -> {

                    //check if player wearing helmet
                    boolean wearingHelm = false;
                    ItemStack itemStackHelmSlot = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
                    for(ItemStack helmet : helmetCollection){
                        if (helmet.toString().equals(itemStackHelmSlot.toString())) {
                            wearingHelm = true;
                            break;
                        }
                    }

                    //if has helmet on
                    if(wearingHelm){
                        //show oxygen meter
                        int max = itemStackHelmSlot.getMaxDamage();
                        int cur = itemStackHelmSlot.getDamage();
                        double ratio = (double)cur/(double)max; //0 is undamaged, 1 is broken
                        double thing = 1-ratio;
                        player.getFoodStats().setFoodLevel((int)Math.ceil(thing * 20));

                        //damage all helmets in inventory every once in a while
                        if (count >= 30) {

                            if (itemStackHelmSlot.attemptDamageItem(1, new Random(), null)) {
                                player.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
                            }

                            for (ItemStack playerInvItem : player.inventory.mainInventory) {
                                for (ItemStack helmet : helmetCollection) {
                                    if (helmet.toString().equals(playerInvItem.toString())) {
                                        if (playerInvItem.attemptDamageItem(1, new Random(), null)) {
                                            player.inventory.deleteStack(playerInvItem);
                                        }
                                    }
                                }
                            }

                        }

                    } else {
                        //show empty oxygen meter
                        player.getFoodStats().setFoodLevel(0);

                        //do damage to player
                        player.attackEntityFrom(new DamageSource("nooxygen"), 2);
                    }
                });

                if(count >=30){
                    count = 0;
                }
            }

        }
    }
}
