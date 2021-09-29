package net.arcanamod.client.render.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.arcanamod.aspects.AspectUtils;
import net.arcanamod.blocks.pipes.AspectSpeck;
import net.arcanamod.blocks.pipes.PipeWindowTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AspectWindowTileEntityRenderer extends TileEntityRenderer<PipeWindowTileEntity>{
	
	public AspectWindowTileEntityRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}
	
	public void render(PipeWindowTileEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light, int overlay){
		for(AspectSpeck speck : te.getSpecks()){
			stack.push();
			// so
			// x ->
			//   <- x
			// both have pos 0
			// just opposite directions
			// start at centre
			stack.translate(.5, .5, .5);
			// move by -dir*0.5
			stack.translate(-speck.direction.getXOffset() * 0.5, -speck.direction.getYOffset() * 0.5, -speck.direction.getZOffset() * 0.5);
			// move the speck by its progress
			stack.translate(speck.pos * speck.direction.getXOffset(), speck.pos * speck.direction.getYOffset(), speck.pos * speck.direction.getZOffset());
			// debugging
			stack.translate(0, .25, 0);
			stack.scale(0.5f, 0.5f, 0.5f);
			// render
			Minecraft.getInstance().getItemRenderer().renderItem(AspectUtils.getItemStackForAspect(speck.payload.getAspect()), ItemCameraTransforms.TransformType.GROUND, light, overlay, stack, buffer);
			stack.pop();
		}
	}
}