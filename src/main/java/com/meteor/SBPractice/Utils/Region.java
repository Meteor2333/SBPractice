package com.meteor.SBPractice.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

//By WoolWars
public class Region {

    private final World world;
    private final int xMin, xMax, yMin, yMax, zMin, zMax;

    public Region(Location first, Location second) {
        this.world = first.getWorld();
        this.xMin = Math.min(first.getBlockX(), second.getBlockX());
        this.xMax = Math.max(first.getBlockX(), second.getBlockX());
        this.yMin = Math.min(first.getBlockY(), second.getBlockY());
        this.yMax = Math.max(first.getBlockY(), second.getBlockY());
        this.zMin = Math.min(first.getBlockZ(), second.getBlockZ());
        this.zMax = Math.max(first.getBlockZ(), second.getBlockZ());
    }

    public World getWorld() {
        return world;
    }

    public int getXMin() {
        return xMin;
    }

    public int getXMax() {
        return xMax;
    }

    public int getYMin() {
        return yMin;
    }

    public int getYMax() {
        return yMax;
    }

    public int getZMin() {
        return zMin;
    }

    public int getZMax() {
        return zMax;
    }

    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>(getBlockCount());
        for (int x = xMin; x <= xMax; ++x) {
            for (int y = yMin; y <= yMax; ++y) {
                for (int z = zMin; z <= zMax; ++z) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public void setBlocks(List<BlockState> blocks) {
        int id = 0;
        for (int x = xMin; x <= xMax; ++x) {
            for (int y = yMin; y <= yMax; ++y) {
                for (int z = zMin; z <= zMax; ++z) {
                    //noinspection deprecation
                    world.getBlockAt(x, y, z).setTypeIdAndData(blocks.get(id).getTypeId(), blocks.get(id).getRawData(), false);
                    id++;
                }
            }
        }
    }

    public void fill(Material type) {
        for (Block block : getBlocks()) {
            block.setType(type, false);
        }
    }

    public boolean isInside(Location location, boolean ignoreYaxis) {
        if (ignoreYaxis) {
            return location.getWorld().equals(world)
                    && location.getBlockX() >= xMin
                    && location.getBlockX() <= xMax
                    && location.getBlockZ() >= zMin
                    && location.getBlockZ() <= zMax;
        } return location.getWorld().equals(world)
                && location.getBlockX() >= xMin
                && location.getBlockX() <= xMax
                && location.getBlockY() >= yMin
                && location.getBlockY() <= yMax
                && location.getBlockZ() >= zMin
                && location.getBlockZ() <= zMax;
    }

    public int getBlockCount() {
        return (xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
    }

}
