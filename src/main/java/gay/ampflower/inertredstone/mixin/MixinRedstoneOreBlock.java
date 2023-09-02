/*
 * Copyright (C) 2023, Ampflower
 *
 * This software is subject to the terms of the Zlib License.
 * If a copy was not distributed with this file, you can obtain one at
 * https://github.com/Modflower/inert-redstone/blob/trunk/LICENSE
 *
 * Source: https://github.com/Modflower/inert-redstone
 * SPDX-License-Identifier: Zlib
 */

package gay.ampflower.inertredstone.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.RedstoneOreBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes redstone ore inert to the environment.
 *
 * @author Ampflower
 * @since 1.0.0
 **/
@Mixin(RedstoneOreBlock.class)
public abstract class MixinRedstoneOreBlock extends Block {

	public MixinRedstoneOreBlock(final Settings settings) {
		super(settings);
	}

	@Inject(method = "light", at = @At("HEAD"), cancellable = true)
	private static void inertredstone$onLight(CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "spawnParticles", at = @At("HEAD"), cancellable = true)
	private static void inertredstone$spawnParticles(CallbackInfo ci) {
		ci.cancel();
	}
}
