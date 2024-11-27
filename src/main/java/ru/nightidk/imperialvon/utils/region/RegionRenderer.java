package ru.nightidk.imperialvon.utils.region;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import ru.nightidk.imperialvon.ImperialVon;

import java.util.Objects;

public class RegionRenderer {
    private static RegionPositions currentRegion;

    public static void setRegion(RegionPositions region) {
        currentRegion = region;
    }

    public static void render(RenderLevelStageEvent event) {
        if (currentRegion == null) return;

        Vec3 pos1 = currentRegion.pos1;
        Vec3 pos2 = currentRegion.pos2;

        Minecraft minecraft = Minecraft.getInstance();
        assert minecraft.level != null;

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();

        Matrix4f matrix = event.getPoseStack().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Рендеринг первой точки
        if (!Objects.equals(pos1, Vec3.ZERO)) {
            renderPoint(event.getPoseStack(), tesselator, buffer, pos1, cameraPos, 0.0F, 0.0F, 1.0F);
        }

        // Рендеринг второй точки
        if (!Objects.equals(pos2, Vec3.ZERO)) {
            renderPoint(event.getPoseStack(), tesselator, buffer, pos2, cameraPos, 0.0F, 0.0F, 1.0F);
        }


        if (!Objects.equals(pos1, Vec3.ZERO) && !Objects.equals(pos2, Vec3.ZERO)) {
            AABB box = createAdjustedAABB(pos1, pos2).move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-2.0F, -2.0F);
            RenderSystem.disableCull();


            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            fillAABB(buffer, box, matrix);
            tesselator.end();

            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            drawAABB(buffer, box, matrix);
            tesselator.end();

            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableCull();
        }
    }

    private static void renderPoint(PoseStack poseStack, Tesselator tesselator, BufferBuilder buffer, Vec3 pos, Vec3 cameraPos, float r, float g, float b) {
        AABB box = new AABB(
                pos.x, pos.y, pos.z,
                pos.x + 1, pos.y + 1, pos.z + 1
        ).move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        RenderSystem.lineWidth(3.0F);

        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        drawAABB(buffer, box, matrix, r, g, b, 1.0F, 10, 0.0007f);
        tesselator.end();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);
    }

    private static void drawAABB(BufferBuilder buffer, AABB box, Matrix4f matrix) {
        drawAABB(buffer, box, matrix, 1.0F, 0.0F, 0.0F, 1.0F, 6, 0.0007f);
    }

    private static void drawAABB(BufferBuilder buffer, AABB box, Matrix4f matrix, float r, float g, float b, float a, int lineCopies, float lineSpacing) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Нижняя грань
        drawThickenedLine(buffer, matrix, new Vec3(minX, minY, minZ), new Vec3(maxX, minY, minZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, minY, minZ), new Vec3(maxX, minY, maxZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, minY, maxZ), new Vec3(minX, minY, maxZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(minX, minY, maxZ), new Vec3(minX, minY, minZ), lineCopies, lineSpacing, r, g, b, a);

        // Верхняя грань
        drawThickenedLine(buffer, matrix, new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, minZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, maxY, minZ), new Vec3(maxX, maxY, maxZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, maxY, maxZ), new Vec3(minX, maxY, maxZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(minX, maxY, maxZ), new Vec3(minX, maxY, minZ), lineCopies, lineSpacing, r, g, b, a);

        // Вертикальные рёбра
        drawThickenedLine(buffer, matrix, new Vec3(minX, minY, minZ), new Vec3(minX, maxY, minZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, minY, minZ), new Vec3(maxX, maxY, minZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(maxX, minY, maxZ), new Vec3(maxX, maxY, maxZ), lineCopies, lineSpacing, r, g, b, a);
        drawThickenedLine(buffer, matrix, new Vec3(minX, minY, maxZ), new Vec3(minX, maxY, maxZ), lineCopies, lineSpacing, r, g, b, a);
    }

    private static void drawThickenedLine(BufferBuilder buffer, Matrix4f matrix, Vec3 start, Vec3 end, int copies, float spacing, float r, float g, float b, float a) {
        // Направление линии
        Vec3 direction = end.subtract(start).normalize();
        // Перпендикулярное смещение
        Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();

        // Рисуем несколько параллельных линий
        for (int i = -copies / 2; i <= copies / 2; i++) {
            float offset = i * spacing;
            Vec3 offsetVector = perpendicular.scale(offset);
            Vec3 adjustedStart = start.add(offsetVector);
            Vec3 adjustedEnd = end.add(offsetVector);

            // Рисуем одну линию
            vertex(buffer, matrix, (float) adjustedStart.x, (float) adjustedStart.y, (float) adjustedStart.z, r, g, b, a);
            vertex(buffer, matrix, (float) adjustedEnd.x, (float) adjustedEnd.y, (float) adjustedEnd.z, r, g, b, a);
        }
    }

    private static void fillAABB(BufferBuilder buffer, AABB box, Matrix4f matrix) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float r = 1.0F, g = 1.0F, b = 1.0F, a = 0.4F;

        // Нижняя грань (Y = minY)
        vertex(buffer, matrix, minX, minY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, minY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, minY, minZ, r, g, b, a);
        vertex(buffer, matrix, minX, minY, minZ, r, g, b, a);

        // Верхняя грань (Y = maxY)
        vertex(buffer, matrix, minX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, maxZ, r, g, b, a);
        vertex(buffer, matrix, minX, maxY, maxZ, r, g, b, a);

        // Передняя грань (Z = maxZ)
        vertex(buffer, matrix, minX, minY, maxZ, r, g, b, a);
        vertex(buffer, matrix, minX, maxY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, minY, maxZ, r, g, b, a);

        // Задняя грань (Z = minZ)
        vertex(buffer, matrix, maxX, minY, minZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, minX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, minX, minY, minZ, r, g, b, a);

        // Левая грань (X = minX)
        vertex(buffer, matrix, minX, minY, minZ, r, g, b, a);
        vertex(buffer, matrix, minX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, minX, maxY, maxZ, r, g, b, a);
        vertex(buffer, matrix, minX, minY, maxZ, r, g, b, a);

        // Правая грань (X = maxX)
        vertex(buffer, matrix, maxX, minY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, maxZ, r, g, b, a);
        vertex(buffer, matrix, maxX, maxY, minZ, r, g, b, a);
        vertex(buffer, matrix, maxX, minY, minZ, r, g, b, a);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

    private static AABB createAdjustedAABB(Vec3 pos1, Vec3 pos2) {
        double minX = Math.min(pos1.x, pos2.x);
        double minY = Math.min(pos1.y, pos2.y);
        double minZ = Math.min(pos1.z, pos2.z);

        double maxX = Math.max(pos1.x, pos2.x) + 1.0;
        double maxY = Math.max(pos1.y, pos2.y) + 1.0;
        double maxZ = Math.max(pos1.z, pos2.z) + 1.0;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

}