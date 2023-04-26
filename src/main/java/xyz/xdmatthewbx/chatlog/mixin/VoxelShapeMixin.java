package xyz.xdmatthewbx.chatlog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(VoxelShape.class)
public class VoxelShapeMixin {

	private ArrayList<Integer[]> edgeCache = null;

	@Redirect(method = "forAllEdges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/DiscreteVoxelShape;forAllEdges(Lnet/minecraft/world/phys/shapes/DiscreteVoxelShape$IntLineConsumer;Z)V"))
	public void forEachEdge(DiscreteVoxelShape instance, DiscreteVoxelShape.IntLineConsumer positionBiConsumer, boolean bl) {
		if (edgeCache == null) {
			var cache = new ArrayList<Integer[]>();
			instance.forAllEdges(
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
