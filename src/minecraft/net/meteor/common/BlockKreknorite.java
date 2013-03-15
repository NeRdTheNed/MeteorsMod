package net.meteor.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.world.World;

public class BlockKreknorite extends BlockMeteor
{
	public BlockKreknorite(int i, int j)
	{
		super(i, j);
	}

	public int a(int i, Random random, int j)
	{
		return MeteorsMod.itemKreknoChip.itemID;
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int l)
	{
		if ((!world.isRemote) && (world.rand.nextInt(100) == 95)) {
			EntityBlaze blaze = new EntityBlaze(world);
			blaze.setLocationAndAngles(i, j, k, 0.0F, 0.0F);
			world.spawnEntityInWorld(blaze);
			blaze.spawnExplosionParticle();
		}
	}

	@Override
	public void updateTick(World world, int i, int j, int k, Random random)
	{
		int meta = world.getBlockMetadata(i, j, k);
		if (meta > 0) {
			world.setBlockMetadata(i, j, k, --meta);
			if (meta <= 0) {
				world.setBlockWithNotify(i, j, k, Block.obsidian.blockID);
				triggerLavaMixEffects(world, i, j, k);
			} else {
				checkForHarden(world, i, j, k);
			}
		}
	}

	@Override
	public void onBlockAdded(World world, int i, int j, int k)
	{
		checkForHarden(world, i, j, k);
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l)
	{
		checkForHarden(world, i, j, k);
	}

	private void checkForHarden(World world, int i, int j, int k)
	{
		if (world.getBlockId(i, j, k) != blockID)
        {
            return;
        }
        boolean flag = false;
        if (flag || world.getBlockMaterial(i, j, k - 1) == Material.water)
        {
            flag = true;
        }
        if (flag || world.getBlockMaterial(i, j, k + 1) == Material.water)
        {
            flag = true;
        }
        if (flag || world.getBlockMaterial(i - 1, j, k) == Material.water)
        {
            flag = true;
        }
        if (flag || world.getBlockMaterial(i + 1, j, k) == Material.water)
        {
            flag = true;
        }
        if (flag || world.getBlockMaterial(i, j + 1, k) == Material.water)
        {
            flag = true;
        }
        if (flag)
        {
            world.setBlockWithNotify(i, j, k, Block.obsidian.blockID);
            triggerLavaMixEffects(world, i, j, k);
        }
	}

	protected void triggerLavaMixEffects(World world, int i, int j, int k)
	{
		world.playSoundEffect(i + 0.5F, j + 0.5F, k + 0.5F, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		for (int l = 0; l < 8; l++)
		{
			world.spawnParticle("largesmoke", i + Math.random(), j + 1.2D, k + Math.random(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j)
	{
		return this.blockIndexInTexture;
	}
}