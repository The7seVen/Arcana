package net.arcanamod.containers;

import net.arcanamod.aspects.Aspect;
import net.arcanamod.aspects.Aspects;
import net.arcanamod.aspects.handlers.AspectHandler;
import net.arcanamod.client.gui.AspectContainerScreen;
import net.arcanamod.containers.slots.AspectSlot;
import net.arcanamod.containers.slots.AspectStoreSlot;
import net.arcanamod.network.Connection;
import net.arcanamod.network.PkAspectClick;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AspectMenu extends AbstractContainerMenu {
	protected List<AspectSlot> aspectSlots = new ArrayList<>();
	protected Aspect heldAspect = null;
	protected boolean symbolic = false;
	protected float heldCount = 0;
	
	protected AspectMenu(@Nullable MenuType<?> type, int id){
		super(type, id);
	}
	
	public List<AspectSlot> getAspectSlots(){
		return aspectSlots;
	}
	
	public void setAspectSlots(List<AspectSlot> aspectSlots){
		this.aspectSlots = aspectSlots;
	}
	
	public Aspect getHeldAspect(){
		return heldAspect;
	}
	
	public void setHeldAspect(Aspect heldAspect){
		this.heldAspect = heldAspect;
	}
	
	public boolean isSymbolic(){
		return symbolic;
	}
	
	public void setSymbolic(boolean symbolic){
		this.symbolic = symbolic;
	}
	
	public float getHeldCount(){
		return heldCount;
	}
	
	public void setHeldCount(float heldCount){
		if(heldCount <= 0) heldAspect = Aspects.EMPTY;
		this.heldCount = heldCount;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void handleClick(int mouseX, int mouseY, int button, AspectContainerScreen<?> gui){
		for(AspectSlot slot : getAspectSlots()){
			if(slot.getInventory().get() != null && gui.isSlotVisible(slot) && isMouseOverSlot(mouseX, mouseY, slot, gui)){
				// gonna send a packet
				PkAspectClick.ClickType type;
				if(button == 0)
					type = PkAspectClick.ClickType.TAKE;
				else if(button == 1)
					type = PkAspectClick.ClickType.PUT;
				else
					return;
				if(Screen.hasShiftDown())
					type = type == PkAspectClick.ClickType.PUT ? PkAspectClick.ClickType.PUT_ALL : PkAspectClick.ClickType.TAKE_ALL;
				// do some quick checking to make sure that the packet won't just do nothing
				// don't actually modify anything though!
				// <blah>
				if(slot.getAspect() == null)
					slot.setAspect(Aspects.EMPTY);
				Connection.sendAspectClick(containerId, aspectSlots.indexOf(slot), type, slot.getAspect());
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	protected boolean isMouseOverSlot(int mouseX, int mouseY, AspectSlot slot, AspectContainerScreen<?> gui){
		return mouseX >= gui.getGuiLeft() + slot.x && mouseY >= gui.getGuiTop() + slot.y && mouseX < gui.getGuiLeft() + slot.x + 16 && mouseY < gui.getGuiTop() + slot.y + 16;
	}
	
	/**
	 * Gets a list of every aspect handler that's open; i.e. can be modified in this GUI and might need syncing.
	 * The contents of this list must be the same on the server and client side.
	 *
	 * @return A list containing all open AspectHandlers.
	 */
	public abstract List<AspectHandler> getOpenHandlers();
	
	public List<AspectHandler> getAllOpenHandlers(){
		List<AspectHandler> handlers = new ArrayList<>(getOpenHandlers());
		for(AspectSlot slot : aspectSlots)
			if(slot instanceof AspectStoreSlot)
				handlers.add(((AspectStoreSlot)slot).getHolder());
		return handlers;
	}
	
	public void removed(@Nonnull Player player){
		super.removed(player);
		//quick fix for disappearing aspects from closing research screen while holding any amount of an aspect
		if(heldCount != 0){
			for(AspectSlot slot : aspectSlots){
				if(slot.getAspect() == heldAspect){
					while(heldCount > 0)
						heldCount -= slot.insert(heldAspect, heldCount);
					break;
				}
			}
		}
		//uncertain whether implementation should be here or somewhere else
		if(shouldReturnAspectsOnClose()){
			aspectSlots.forEach(AspectSlot::onClose);
		}
	}
	
	public boolean shouldReturnAspectsOnClose(){
		return true;
	}
	
	public void onAspectSlotChange(){
	}
}