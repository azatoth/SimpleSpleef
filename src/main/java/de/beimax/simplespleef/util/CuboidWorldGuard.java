/**
 * 
 */
package de.beimax.simplespleef.util;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.beimax.simplespleef.game.Game;

/**
 * @author mkalus
 *
 */
public class CuboidWorldGuard implements Cuboid {
	/**
	 * region and world of the cuboid
	 */
	private World world;
	private ProtectedRegion region;

	/**
	 * Constructor
	 * @param region
	 */
	public CuboidWorldGuard(ProtectedRegion region, World world) {
		this.region = region;
		this.world = world;
	}

	/* (non-Javadoc)
	 * @see de.beimax.simplespleef.util.Cuboid#contains(org.bukkit.Location)
	 */
	@Override
	public boolean contains(Location location) {
		if (location == null) return false; //no NPEs!
		// check same world
		if (location.getWorld() != this.world) return false;
		// check region containment
		return this.region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/* (non-Javadoc)
	 * @see de.beimax.simplespleef.util.Cuboid#getSerializedBlocks()
	 */
	@Override
	public SerializableBlockData[][][] getSerializedBlocks() {
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();
		
		// get blocks
		final int[] coords = {(min.getBlockX()<max.getBlockX()?min.getBlockX():max.getBlockX()),
			(min.getBlockY()<max.getBlockY()?min.getBlockY():max.getBlockY()),
			(min.getBlockZ()<max.getBlockZ()?min.getBlockZ():max.getBlockZ()),
			(min.getBlockX()>max.getBlockX()?min.getBlockX():max.getBlockX()),
			(min.getBlockY()>max.getBlockY()?min.getBlockY():max.getBlockY()),
			(min.getBlockZ()>max.getBlockZ()?min.getBlockZ():max.getBlockZ())};
		
		SerializableBlockData[][][] blockData =
			new SerializableBlockData[coords[3]-coords[0]+1][coords[4]-coords[1]+1][coords[5]-coords[2]+1];
		
		// copy data from blocks
		for (int x = coords[0]; x <= coords[3]; x++)
			for (int y = coords[1]; y <= coords[4]; y++)
				for (int z = coords[2]; z <= coords[5]; z++) {
					//if (region.contains(x, y, z)) {
						Block block = this.world.getBlockAt(x, y, z);
						blockData[Math.abs(coords[0]-x)][Math.abs(coords[1]-y)][Math.abs(coords[2]-z)] =
							new SerializableBlockData(block.getTypeId(), block.getData());
					//} else blockData[Math.abs(coords[0]-x)][Math.abs(coords[1]-y)][Math.abs(coords[2]-z)] = null; //null block outside of arena (for none cube arenas)
				}
		return blockData;
	}

	/* (non-Javadoc)
	 * @see de.beimax.simplespleef.util.Cuboid#setSerializedBlocks(de.beimax.simplespleef.util.SerializableBlockData[][][])
	 */
	@Override
	public void setSerializedBlocks(SerializableBlockData[][][] blockData) {
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();

		final int[] coords = {(min.getBlockX()<max.getBlockX()?min.getBlockX():max.getBlockX()),
				(min.getBlockY()<max.getBlockY()?min.getBlockY():max.getBlockY()),
				(min.getBlockZ()<max.getBlockZ()?min.getBlockZ():max.getBlockZ())};

		for (int x = 0; x < blockData.length; x++)
			for (int y = 0; y < blockData[0].length; y++)
				for (int z = 0; z < blockData[0][0].length; z++) {
					if (this.region.contains(coords[0] + x, coords[1] + y, coords[2] + z)) { // only restore, if within the region
						Block block = this.world.getBlockAt(coords[0] + x, coords[1] + y, coords[2] + z);
						block.setTypeId(blockData[x][y][z].getTypeId());
						block.setData(blockData[x][y][z].getData());
					}
				}
	}


	@Override
	public List<Block> getDiggableBlocks(Game game) {
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();
		
		LinkedList<Block> diggableBlocks = new LinkedList<Block>();
		
		// get blocks
		final int[] coords = {(min.getBlockX()<max.getBlockX()?min.getBlockX():max.getBlockX()),
			(min.getBlockY()<max.getBlockY()?min.getBlockY():max.getBlockY()),
			(min.getBlockZ()<max.getBlockZ()?min.getBlockZ():max.getBlockZ()),
			(min.getBlockX()>max.getBlockX()?min.getBlockX():max.getBlockX()),
			(min.getBlockY()>max.getBlockY()?min.getBlockY():max.getBlockY()),
			(min.getBlockZ()>max.getBlockZ()?min.getBlockZ():max.getBlockZ())};

		// copy data from blocks
		for (int x = coords[0]; x <= coords[3]; x++)
			for (int y = coords[1]; y <= coords[4]; y++)
				for (int z = coords[2]; z <= coords[5]; z++) {
					Block block = this.world.getBlockAt(x, y, z);
					if (block != null && this.region.contains(x, y, z) && game.checkMayBreakBlock(block)) { // can this block be broken and within the region
						diggableBlocks.add(block);
					}
				}

		return diggableBlocks;
	}
}
