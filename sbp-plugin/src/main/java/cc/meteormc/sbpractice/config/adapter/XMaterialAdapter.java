package cc.meteormc.sbpractice.config.adapter;

import cc.carm.lib.configuration.adapter.ValueAdapter;
import cc.carm.lib.configuration.adapter.ValueType;
import com.cryptomorin.xseries.XMaterial;

public class XMaterialAdapter extends ValueAdapter<XMaterial> {
    public XMaterialAdapter() {
        super(
                ValueType.of(XMaterial.class),
                (holder, type, value) -> value.name(),
                (holder, type, data) -> XMaterial.matchXMaterial(data.toString()).orElse(XMaterial.AIR)
        );
    }
}
