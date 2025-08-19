package cc.meteormc.sbpractice.api.bukkitfix.blockstate;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.material.MaterialData;

@Setter
@Getter
public class Stairs extends org.bukkit.material.Stairs {
    private StairShape shape = StairShape.STRAIGHT;

    public Stairs(MaterialData data) {
        super(data.getItemType(), data.getData());
    }

    public enum StairShape {
        STRAIGHT,
        INNER_LEFT,
        INNER_RIGHT,
        OUTER_LEFT,
        OUTER_RIGHT
    }
}
