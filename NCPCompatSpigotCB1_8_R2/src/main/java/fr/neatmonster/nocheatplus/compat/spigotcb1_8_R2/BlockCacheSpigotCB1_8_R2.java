/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.compat.spigotcb1_8_R2;

import java.util.Iterator;
import java.util.List;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import net.minecraft.server.v1_8_R2.AxisAlignedBB;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.EntityBoat;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IBlockAccess;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.TileEntity;

public class BlockCacheSpigotCB1_8_R2 extends BlockCache implements IBlockAccess{

    protected net.minecraft.server.v1_8_R2.WorldServer world;
    protected World bukkitWorld; // WHACKS

    public BlockCacheSpigotCB1_8_R2(World world) {
        setAccess(world);
    }

    @Override
    public BlockCache setAccess(World world) {
        if (world != null) {
            this.maxBlockY = world.getMaxHeight() - 1;
            this.world = ((CraftWorld) world).getHandle();
            this.bukkitWorld = world;
        } else {
            this.world = null;
            this.bukkitWorld = null;
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int fetchTypeId(final int x, final int y, final int z) {
        return bukkitWorld.getBlockTypeIdAt(x, y, z);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int fetchData(final int x, final int y, final int z) {
        return bukkitWorld.getBlockAt(x, y, z).getData();
    }

    @Override
    public double[] fetchBounds(final int x, final int y, final int z){
        final int id = getTypeId(x, y, z);		
        final net.minecraft.server.v1_8_R2.Block block = net.minecraft.server.v1_8_R2.Block.getById(id);
        if (block == null) {
            // TODO: Convention for null bounds -> full ?
            return null;
        }
        block.updateShape(this, new BlockPosition(x, y, z));

        // minX, minY, minZ, maxX, maxY, maxZ
        return new double[]{block.B(), block.D(), block.F(), block.C(),  block.E(),  block.G()};
    }

    @Override
    public boolean standsOnEntity(final Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
        try{
            // TODO: Find some simplification!

            final net.minecraft.server.v1_8_R2.Entity mcEntity  = ((CraftEntity) entity).getHandle();

            final AxisAlignedBB box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
            @SuppressWarnings("rawtypes")
            final List list = world.getEntities(mcEntity, box);
            @SuppressWarnings("rawtypes")
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final net.minecraft.server.v1_8_R2.Entity other = (net.minecraft.server.v1_8_R2.Entity) iterator.next();
                if (!(other instanceof EntityBoat)){ // && !(other instanceof EntityMinecart)) continue;
                    continue;
                }
                if (minY >= other.locY && minY - other.locY <= 0.7){
                    return true;
                }
                // Still check this for some reason.
                final AxisAlignedBB otherBox = other.getBoundingBox();
                if (box.a > otherBox.d || box.d < otherBox.a || box.b > otherBox.e || box.e < otherBox.b || box.c > otherBox.f || box.f < otherBox.c) {
                    continue;
                }
                else {
                    return true;
                }
            }
        }
        catch (Throwable t){
            // Ignore exceptions (Context: DisguiseCraft).
        }
        return false;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.utilities.BlockCache#cleanup()
     */
    @Override
    public void cleanup() {
        super.cleanup();
        world = null;
        bukkitWorld = null;
    }

    @Override
    public int getBlockPower(BlockPosition pos, EnumDirection dir) {
        return world.getBlockPower(pos, dir);
    }

    @Override
    public TileEntity getTileEntity(BlockPosition pos) {
        return world.getTileEntity(pos);
    }

    @Override
    public IBlockData getType(BlockPosition pos) {
        return world.getType(pos);
    }

    @Override
    public boolean isEmpty(BlockPosition pos) {
        return world.isEmpty(pos);
    }

}
