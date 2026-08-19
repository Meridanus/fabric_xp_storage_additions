package com.notker.xps_additions.screen;/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.regestry.AdditionMenus;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;


public class BoxScreenHandler extends Generic3x3ContainerScreenHandler {
    PropertyDelegate propertyDelegate;

    public BoxScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(XpsAdditions.ITEM_SLOTS), new ArrayPropertyDelegate(1));
        //super(syncId, playerInventory);
    }

    public BoxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(syncId, playerInventory, inventory);
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);
    }

    public int getSyncedNumber(){
        return propertyDelegate.get(0);
    }



    @Override
    public ScreenHandlerType<?> getType() {
        return AdditionMenus.BOX_SCREEN_HANDLER.get();
    }
}