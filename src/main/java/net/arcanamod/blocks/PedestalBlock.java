package net.arcanamod.blocks;

import net.arcanamod.blocks.bases.WaterloggableBlock;
import net.arcanamod.blocks.tiles.PedestalBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PedestalBlock extends WaterloggableBlock implements EntityBlock {

	protected static final VoxelShape SHAPE = Shapes.or(
			box(1, 0, 1, 15, 4, 15),
			box(6, 0, 6, 10, 16, 10),
			box(3, 12, 3, 13, 16, 13)).optimize();

	public PedestalBlock(Properties properties){
		super(properties);
	}

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return SHAPE;
	}

	public boolean allowsMovement(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type){
		return false;
	}

//	public boolean hasTileEntity(BlockState state){
//		return true;
//	}

	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new PedestalBlockEntity(pos, state);
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTrace) {
		ItemStack itemstack = player.getItemInHand(hand);
		PedestalBlockEntity te = (PedestalBlockEntity) world.getBlockEntity(pos);

		if (te.getItem() == ItemStack.EMPTY) {
			if (!itemstack.isEmpty()) {
				te.setItem(itemstack.split(1));
				te.setChanged();
				return InteractionResult.SUCCESS;
			}
		} else {
			ItemStack pedestalItem = te.getItem();
			if (!pedestalItem.isEmpty() && !player.addItem(pedestalItem)) {
				ItemEntity itementity = new ItemEntity(world,
						player.getX(),
						player.getY(),
						player.getZ(), pedestalItem);
				itementity.setNoPickUpDelay();
				world.addFreshEntity(itementity);
			}
			te.setItem(ItemStack.EMPTY);
			te.setChanged();
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof PedestalBlockEntity)
				Containers.dropItemStack(world, te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ(), ((PedestalBlockEntity)te).getItem());
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}
}