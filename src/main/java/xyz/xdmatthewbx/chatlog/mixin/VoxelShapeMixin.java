package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(VoxelShape.class)
public class VoxelShapeMixin {

	@Unique
	private ArrayList<Integer[]> edgeCache = null;

	@Redirect(method = "forEachEdge", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelSet;forEachEdge(Lnet/minecraft/util/shape/VoxelSet$PositionBiConsumer;Z)V"))
	public void forEachEdge(VoxelSet instance, VoxelSet.PositionBiConsumer positionBiConsumer, boolean bl) {
		if (edgeCache == null) {
			var cache = new ArrayList<Integer[]>();
			instance.forEachEdge(
				(i, j, k, l, m, n) ->
					cache.add(new Integer[] { i, j, k, l, m, n }),
				bl
			);
			cache.trimToSize();
			edgeCache = cache;
		}

		edgeCache.forEach(
			edge ->
				positionBiConsumer.consume(
					edge[0],
					edge[1],
					edge[2],
					edge[3],
					edge[4],
					edge[5]
				)
		);
	}

}
