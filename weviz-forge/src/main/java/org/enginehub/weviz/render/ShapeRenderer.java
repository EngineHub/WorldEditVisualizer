package org.enginehub.weviz.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import org.enginehub.weviz.state.VizState;

public interface ShapeRenderer {

    static ActiveRenderInfo getActiveRenderInfo() {
        return Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
    }

    void render(VizState state);

}
