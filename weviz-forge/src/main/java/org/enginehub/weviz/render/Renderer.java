package org.enginehub.weviz.render;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.enginehub.weviz.state.Shape;
import org.enginehub.weviz.state.VizState;
import org.lwjgl.opengl.GL11;

public class Renderer {

    private static final Map<Shape, ShapeRenderer> RENDERERS = Maps.immutableEnumMap(
        ImmutableMap.<Shape, ShapeRenderer>builder()
            .put(Shape.CUBOID, new CuboidRenderer())
            .build()
    );

    private final AtomicReference<VizState> currentState;

    public Renderer(AtomicReference<VizState> currentState) {
        this.currentState = currentState;
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        VizState state = currentState.get();
        if (state == null) {
            return;
        }

        ShapeRenderer renderer = RENDERERS.get(state.getShape());
        if (renderer != null) {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(event.getMatrixStack().getLast().getPositionMatrix());
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            renderer.render(state);
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(GL11.GL_FLAT);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }
}
