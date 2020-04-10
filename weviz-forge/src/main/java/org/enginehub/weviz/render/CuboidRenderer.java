package org.enginehub.weviz.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.enginehub.weviz.state.VizState;
import org.enginehub.weviz.util.Color;
import org.lwjgl.opengl.GL11;

public class CuboidRenderer implements ShapeRenderer {

    private static final Color REGION_COLOR = Color.of(255, 0, 0, 255);
    private static final Color POS1_COLOR = Color.of(0, 255, 0, 255);
    private static final Color POS2_COLOR = Color.of(0, 0, 255, 255);

    @Override
    public void render(VizState state) {
        ImmutableList<Vector3> points = state.getPoints();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Vec3d cam = ShapeRenderer.getActiveRenderInfo().getProjectedView();

        if (points.size() >= 1) {
            RenderSystem.lineWidth(4.0F);
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            CuboidRegion pos1 = new CuboidRegion(points.get(0).toBlockPoint(), points.get(0).toBlockPoint());
            renderRegion(buffer, cam, pos1, POS1_COLOR);

            tessellator.draw();
        }
        if (points.size() == 2) {
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            CuboidRegion pos2 = new CuboidRegion(points.get(1).toBlockPoint(), points.get(1).toBlockPoint());
            renderRegion(buffer, cam, pos2, POS2_COLOR);

            tessellator.draw();


            RenderSystem.lineWidth(1.0F);
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            CuboidRegion region = new CuboidRegion(points.get(0).toBlockPoint(), points.get(1).toBlockPoint());
            renderRegion(buffer, cam, region, REGION_COLOR);

            tessellator.draw();
        }
    }

    private static final int CHUNK_SIZE = 10;
    private static final int MIN_SPACING = 3;

    private IntList chunkSides(int min, int max) {
        int len = max - min;
        IntList result = new IntArrayList((len / CHUNK_SIZE) + 2);
        result.add(min);

        int cursor = CHUNK_SIZE;
        while (cursor < (len - MIN_SPACING)) {
            result.add(min + cursor);
            cursor += CHUNK_SIZE;
        }

        result.add(max);
        return result;
    }

    private void renderRegion(BufferBuilder buffer, Vec3d cam, CuboidRegion region, Color color) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint().add(1, 1, 1);

        for (IntIterator iter = chunkSides(min.getX(), max.getX()).iterator(); iter.hasNext(); ) {
            int x = iter.nextInt();
            doDrawLine(buffer, cam,
                x, min.getY(), min.getZ(),
                x, min.getY(), max.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                x, max.getY(), min.getZ(),
                x, max.getY(), max.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                x, min.getY(), min.getZ(),
                x, max.getY(), min.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                x, min.getY(), max.getZ(),
                x, max.getY(), max.getZ(),
                color
            );
        }
        for (IntIterator iter = chunkSides(min.getY(), max.getY()).iterator(); iter.hasNext(); ) {
            int y = iter.nextInt();
            doDrawLine(buffer, cam,
                min.getX(), y, min.getZ(),
                min.getX(), y, max.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                max.getX(), y, min.getZ(),
                max.getX(), y, max.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                min.getX(), y, min.getZ(),
                max.getX(), y, min.getZ(),
                color
            );
            doDrawLine(buffer, cam,
                min.getX(), y, max.getZ(),
                max.getX(), y, max.getZ(),
                color
            );
        }
        for (IntIterator iter = chunkSides(min.getZ(), max.getZ()).iterator(); iter.hasNext(); ) {
            int z = iter.nextInt();
            doDrawLine(buffer, cam,
                min.getX(), min.getY(), z,
                min.getX(), max.getY(), z,
                color
            );
            doDrawLine(buffer, cam,
                max.getX(), min.getY(), z,
                max.getX(), max.getY(), z,
                color
            );
            doDrawLine(buffer, cam,
                min.getX(), min.getY(), z,
                max.getX(), min.getY(), z,
                color
            );
            doDrawLine(buffer, cam,
                min.getX(), max.getY(), z,
                max.getX(), max.getY(), z,
                color
            );
        }
    }

    private void doDrawLine(BufferBuilder buffer, Vec3d cam,
                            int x, int y, int z, int x2, int y2, int z2,
                            Color color) {
        double camX = cam.getX();
        double camY = cam.getY();
        double camZ = cam.getZ();
        buffer.pos(x - camX, y - camY, z - camZ)
            .color(0, 0, 0, 0)
            .endVertex();
        buffer.pos(x - camX, y - camY, z - camZ)
            .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
            .endVertex();
        buffer.pos(x2 - camX, y2 - camY, z2 - camZ)
            .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
            .endVertex();
        buffer.pos(x2 - camX, y2 - camY, z2 - camZ)
            .color(0, 0, 0, 0)
            .endVertex();
    }
}
