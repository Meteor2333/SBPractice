package cc.meteormc.sbpractice.api.util;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Region implements Cloneable {
    private Vector pos1, pos2;
    private int width, height, length;
    private int xMin, xMax, yMin, yMax, zMin, zMax;

    public Region(Vector pos1, Vector pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.compute();
    }

    public Region inset(int radius) {
        this.pos1 = this.getMinimumPos().add(new Vector(radius, radius, radius));
        this.pos2 = this.getMaximumPos().subtract(new Vector(radius, radius, radius));
        this.compute();
        return this;
    }

    public Region outset(int radius) {
        this.pos1 = this.getMinimumPos().subtract(new Vector(radius, radius, radius));
        this.pos2 = this.getMaximumPos().add(new Vector(radius, radius, radius));
        this.compute();
        return this;
    }

    public Vector getMinimumPos() {
        return new Vector(this.xMin, this.yMin, this.zMin);
    }

    public Vector getMaximumPos() {
        return new Vector(this.xMax, this.yMax, this.zMax);
    }

    public boolean isInside(Location location) {
        return this.isInsideIgnoreYaxis(location)
            && location.getBlockY() >= this.yMin
            && location.getBlockY() <= this.yMax;
    }

    public boolean isInsideIgnoreYaxis(Location location) {
        return location.getBlockX() >= this.xMin
            && location.getBlockX() <= this.xMax
            && location.getBlockZ() >= this.zMin
            && location.getBlockZ() <= this.zMax;
    }

    public void fillBlock(World world, XMaterial type) {
        for (Vector vector : this.getVectors()) {
            vector.toLocation(world).getBlock().setType(type.parseMaterial(), false);
        }
    }

    public List<Vector> getVectors() {
        List<Vector> vectors = new ArrayList<>(this.getBlockCount());
        for (int x = this.xMin; x <= this.xMax; x++) {
            for (int y = this.yMin; y <= this.yMax; y++) {
                for (int z = this.zMin; z <= this.zMax; z++) {
                    vectors.add(new Vector(x, y, z));
                }
            }
        }
        return vectors;
    }

    public int getBlockCount() {
        return this.width * this.height * this.length;
    }

    private void compute() {
        this.xMin = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX());
        this.xMax = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX());
        this.yMin = Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
        this.yMax = Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
        this.zMin = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        this.zMax = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        this.width =  this.xMax - this.xMin + 1;
        this.height = this.yMax - this.yMin + 1;
        this.length = this.zMax - this.zMin + 1;
    }

    @Override
    public Region clone() {
        return new Region(this.pos1, this.pos2);
    }
}
