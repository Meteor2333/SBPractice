package cc.meteormc.sbpractice.api.helper;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@SerializableAs("Area")
public class Area implements Cloneable, ConfigurationSerializable {
    private Vector pos1, pos2;
    private int width, height, length;
    private int xMin, xMax, yMin, yMax, zMin, zMax;

    public Area(Vector pos1, Vector pos2) {
        this.pos1 = pos1.clone();
        this.pos2 = pos2.clone();
        this.compute();
    }

    public Area inset(int radius) {
        this.pos1 = this.getMinimumPos().add(new Vector(radius, radius, radius));
        this.pos2 = this.getMaximumPos().subtract(new Vector(radius, radius, radius));
        this.compute();
        return this;
    }

    public Area outset(int radius) {
        this.pos1 = this.getMinimumPos().subtract(new Vector(radius, radius, radius));
        this.pos2 = this.getMaximumPos().add(new Vector(radius, radius, radius));
        this.compute();
        return this;
    }

    public Area add(Vector vector) {
        this.pos1.add(vector);
        this.pos2.add(vector);
        this.compute();
        return this;
    }

    public Area subtract(Vector vector) {
        this.pos1.subtract(vector);
        this.pos2.subtract(vector);
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

    public List<Vector> getPoints() {
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
        this.width = this.xMax - this.xMin + 1;
        this.height = this.yMax - this.yMin + 1;
        this.length = this.zMax - this.zMin + 1;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pos1", this.pos1.serialize());
        result.put("pos2", this.pos2.serialize());
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Area deserialize(Map<String, Object> args) {
        Vector pos1 = new Vector();
        Vector pos2 = new Vector();

        if (args.containsKey("pos1")) {
            pos1 = Vector.deserialize((Map<String, Object>) args.get("pos1"));
        }

        if (args.containsKey("pos2")) {
            pos2 = Vector.deserialize((Map<String, Object>) args.get("pos2"));
        }

        return new Area(pos1, pos2);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Area && pos1.equals(((Area) obj).getPos1()) && pos2.equals(((Area) obj).getPos2());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + pos1.hashCode();
        hash = 79 * hash + pos2.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return pos1.toString() + "->" + pos2.toString();
    }

    @Override
    public Area clone() {
        try {
            Area clone = (Area) super.clone();
            clone.pos1 = this.pos1.clone();
            clone.pos2 = this.pos2.clone();
            clone.compute();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
