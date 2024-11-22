package ru.nightidk.imperialvon.JDA;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.TextStyleUtil;
import ru.nightidk.jdautil.actions.MessageActions;
import ru.nightidk.jdautil.events.MessageEvent;

import java.util.List;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.*;

@Mod.EventBusSubscriber(modid = ImperialVon.MODID, value = Dist.DEDICATED_SERVER)
public class MessageListener {
    @SubscribeEvent
    public static void onMessageReceived(MessageEvent event) {
        if (event.getMessage().getContent().isEmpty()) return;
        if (!event.getMessage().getChannelId().equals("1250613189658411009")) return;
        if (ImperialVon.server == null) return;
        if (event.getMessage().getContent().startsWith("/")) {
            String content = event.getMessage().getContent();
            String[] command = content.strip().substring(1).split(" ");
            if (command.length == 0) return;
            switch (command[0]) {
                case "list" -> {
                    List<ServerPlayer> entityList = ImperialVon.server.getPlayerList().getPlayers();
                    MessageActions.sendMessage(
                            event.getMessage().getChannelId(),
                            "[Command] В данный момент на сервере %d %s: ".formatted(
                                    entityList.size(), getWordInDeclension(entityList.size(), List.of("игрок", "игрока", "игроков"))
                            ) + String.join(", ", entityList.stream().map(s -> s.getDisplayName().getString()).toList()),
                            null

                    );
                }
                case "info" -> {
                    // pass
                }
                default -> {
                    MessageActions.sendMessage(
                            event.getMessage().getChannelId(),
                            "[Command] Команда не распознана.",
                            null

                    );
                }
            }
            return;
        }
        if (!event.getMessage().getContent().isEmpty() && event.getMessage().getChannelId().equals("1250613189658411009")) {
            String nickname = event.getMessage().getUsername();
            sendChatMessageToAll(
                    ImperialVon.server.getPlayerList().getPlayers(),
                    getStyledComponent("[Discord]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" %s".formatted(nickname), TextStyleUtil.DARK_AQUA.getStyle())
                                    .append(getStyledComponent(": %s".formatted(event.getMessage().getContent()), TextStyleUtil.WHITE.getStyle()))
                            ),
                    MessageType.DISCORD
            );

        }
    }
}
