package net.cgerwyu.playerdeathbell.client;

import net.cgerwyu.playerdeathbell.PlayerDeathBell;
import net.cgerwyu.playerdeathbell.network.DeathCounterPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DeathCounterHud {

    private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(
            PlayerDeathBell.MODID,
            "death_counter"
    );

    private static final int SCREEN_MARGIN = 8;
    private static final int PADDING = 4;
    private static final int HEAD_SIZE = 10;
    private static final int ROW_HEIGHT = 14;
    private static final int HEADER_HEIGHT = 15;
    private static final int GAP = 4;
    private static final float ANIMATION_SPEED = 0.35F;
    private static final float ANNOUNCEMENT_DURATION = 80.0F;
    private static final float ANNOUNCEMENT_FADE_DURATION = 20.0F;

    private static final Map<UUID, Float> animatedRowPositions = new HashMap<>();
    private static String announcedLeaderName;
    private static float announcementTimeLeft;

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, DeathCounterHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        List<DeathCounterPayload.Entry> entries = ClientDeathCounterState.entries();

        if (minecraft.player == null || entries.isEmpty()) {
            animatedRowPositions.clear();
            return;
        }

        if (minecraft.gui.hud.isHidden()) {
            return;
        }

        Font font = minecraft.font;
        Component title = Component.translatable("hud.playerdeathbell.deaths");
        int maxRows = Math.max(0, (graphics.guiHeight() - SCREEN_MARGIN * 2 - HEADER_HEIGHT) / ROW_HEIGHT);
        int shownRows = Math.min(entries.size(), maxRows);

        if (shownRows == 0) {
            return;
        }

        int maxNameWidth = 0;
        int maxDeathsWidth = 0;
        int rankWidth = Math.max(font.width("CHAMP"), font.width("#15"));
        for (int i = 0; i < shownRows; i++) {
            DeathCounterPayload.Entry entry = entries.get(i);
            maxNameWidth = Math.max(maxNameWidth, font.width(entry.playerName()));
            maxDeathsWidth = Math.max(maxDeathsWidth, font.width(Integer.toString(entry.deaths())));
        }

        int rowContentWidth = rankWidth + GAP + HEAD_SIZE + GAP + maxNameWidth + GAP + maxDeathsWidth;
        int tableWidth = Math.max(font.width(title), rowContentWidth) + PADDING * 2;
        int tableHeight = HEADER_HEIGHT + shownRows * ROW_HEIGHT;
        int tableX = graphics.guiWidth() - SCREEN_MARGIN - tableWidth;
        int tableY = SCREEN_MARGIN;

        Set<UUID> visiblePlayers = new HashSet<>();
        for (int i = 0; i < shownRows; i++) {
            visiblePlayers.add(entries.get(i).playerId());
        }
        animatedRowPositions.keySet().retainAll(visiblePlayers);

        graphics.nextStratum();
        graphics.fill(tableX, tableY, tableX + tableWidth, tableY + tableHeight, 0xA0101010);
        graphics.fill(tableX, tableY, tableX + tableWidth, tableY + HEADER_HEIGHT, 0xC02A2A2A);
        graphics.centeredText(font, title, tableX + tableWidth / 2, tableY + 3, 0xFFFFFFFF);

        for (int i = 0; i < shownRows; i++) {
            DeathCounterPayload.Entry entry = entries.get(i);
            float targetRowY = tableY + HEADER_HEIGHT + i * ROW_HEIGHT;
            float currentRowY = animatedRowPositions.getOrDefault(entry.playerId(), targetRowY);
            float animationStep = Math.min(
                    1.0F,
                    deltaTracker.getRealtimeDeltaTicks() * ANIMATION_SPEED
            );
            float nextRowY = currentRowY + (targetRowY - currentRowY) * animationStep;

            if (Math.abs(targetRowY - nextRowY) < 0.05F) {
                nextRowY = targetRowY;
            }

            animatedRowPositions.put(entry.playerId(), nextRowY);
            int rowY = Math.round(nextRowY);

            if ((i & 1) == 1) {
                graphics.fill(tableX, rowY, tableX + tableWidth, rowY + ROW_HEIGHT, 0x40282828);
            }

            int rankX = tableX + PADDING;
            if (i == 0 || i <= 15) {
                String rank = i == 0 ? "CHAMP" : "#" + i;
                int rankColor = i == 0 ? 0xFFFFD700 : 0xFFAAAAAA;
                graphics.text(
                        font,
                        rank,
                        rankX + rankWidth - font.width(rank),
                        rowY + (ROW_HEIGHT - font.lineHeight) / 2,
                        rankColor,
                        true
                );
            }

            renderPlayerHead(
                    graphics,
                    minecraft,
                    entry,
                    rankX + rankWidth + GAP,
                    rowY + (ROW_HEIGHT - HEAD_SIZE) / 2
            );

            int textY = rowY + (ROW_HEIGHT - font.lineHeight) / 2;
            int nameX = rankX + rankWidth + GAP + HEAD_SIZE + GAP;
            String deaths = Integer.toString(entry.deaths());
            int deathsX = tableX + tableWidth - PADDING - font.width(deaths);

            int nameColor = i == 0 ? 0xFFFFD700 : 0xFFFFFFFF;
            graphics.text(font, entry.playerName(), nameX, textY, nameColor, true);
            graphics.text(font, deaths, deathsX, textY, 0xFFFF5555, true);
        }

        renderLeaderAnnouncement(graphics, deltaTracker, font);
    }

    public static void announceNewLeader(String playerName) {
        announcedLeaderName = playerName;
        announcementTimeLeft = ANNOUNCEMENT_DURATION;
    }

    public static void reset() {
        animatedRowPositions.clear();
        announcedLeaderName = null;
        announcementTimeLeft = 0.0F;
    }

    private static void renderLeaderAnnouncement(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker,
            Font font
    ) {
        if (announcedLeaderName == null || announcementTimeLeft <= 0.0F) {
            return;
        }

        float alphaMultiplier = Math.min(
                1.0F,
                announcementTimeLeft / ANNOUNCEMENT_FADE_DURATION
        );
        int textAlpha = Math.round(255.0F * alphaMultiplier);
        int backgroundAlpha = Math.round(180.0F * alphaMultiplier);
        Component message = Component.translatable(
                "hud.playerdeathbell.new_leader",
                announcedLeaderName
        );
        int messageWidth = font.width(message);
        int centerX = graphics.guiWidth() / 2;
        int messageY = 18;

        graphics.fill(
                centerX - messageWidth / 2 - 6,
                messageY - 4,
                centerX + messageWidth / 2 + 6,
                messageY + font.lineHeight + 4,
                backgroundAlpha << 24 | 0x101010
        );
        graphics.centeredText(
                font,
                message,
                centerX,
                messageY,
                textAlpha << 24 | 0xFFD700
        );

        announcementTimeLeft -= deltaTracker.getRealtimeDeltaTicks();
        if (announcementTimeLeft <= 0.0F) {
            announcedLeaderName = null;
        }
    }

    private static void renderPlayerHead(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            DeathCounterPayload.Entry entry,
            int x,
            int y
    ) {
        PlayerInfo playerInfo = minecraft.getConnection() == null
                ? null
                : minecraft.getConnection().getPlayerInfo(entry.playerId());

        if (playerInfo != null) {
            PlayerFaceExtractor.extractRenderState(
                    graphics,
                    playerInfo.getSkin().body().texturePath(),
                    x,
                    y,
                    HEAD_SIZE,
                    playerInfo.showHat(),
                    false,
                    0xFFFFFFFF
            );
        } else {
            PlayerFaceExtractor.extractRenderState(
                    graphics,
                    ResolvableProfile.createUnresolved(entry.playerId()),
                    x,
                    y,
                    HEAD_SIZE
            );
        }
    }

    private DeathCounterHud() {
    }
}
