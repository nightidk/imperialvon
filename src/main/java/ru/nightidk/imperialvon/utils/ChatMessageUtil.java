package ru.nightidk.imperialvon.utils;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.jdautil.actions.MessageActions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ChatMessageUtil {
    public enum MessageType {
        NOTIFY,
        DISCORD,
        CLEAN,
        RESTART
    }

    public static void sendChatMessageToAll(List<ServerPlayer> playerList, Component message, MessageType type) {
        ListIterator<ServerPlayer> iterator = playerList.listIterator();
        while(true)
        {
            if(iterator.hasNext())
            {
                ServerPlayer TmpPlayer = iterator.next();
                TmpPlayer.sendSystemMessage(message);
            }
            else break;
        }
        ImperialVon.LOG.info("{}", message.getString());
        if (type == MessageType.NOTIFY)
            MessageActions.sendMessage("1250613189658411009", message.getString(), null);
    }

    public static void sendChatMessageToAll(List<ServerPlayer> source, String message, MessageType type) {
        sendChatMessageToAll(source, Component.literal(message), type);
    }

    public static void sendChatMessageToPlayer(ServerPlayer serverPlayer, String message) {
        serverPlayer.sendSystemMessage(Component.literal(message));
    }

    public static void sendChatMessageToPlayer(ServerPlayer serverPlayer, Component message) {
        serverPlayer.sendSystemMessage(message);
    }

    public static String getWordInDeclension(int value, List<String> variables) {
        int result = value % 100;
        if (result >= 10 && result <= 20) return variables.get(2);
        result = value % 10;
        if (result == 0 || result > 4) return variables.get(2);
        if (result > 1) return variables.get(1);
        if (result == 1) return variables.get(0);
        return null;
    }

    public static MutableComponent getStyledComponent(String message, Style format) {
        return Component.literal(message).setStyle(format);
    }

    public static MutableComponent getStyledComponents(List<StyledPair> pairs) {
        MutableComponent textComponent = Component.literal("");

        pairs.forEach(p -> textComponent.append(getStyledComponent(p.getString(), p.getStyle())));

        return textComponent;
    }

    public static MutableComponent getStyledComponentsWithNewLines(StyledPair ...pairs) {
        MutableComponent textComponent = Component.literal("");

        Iterator<StyledPair> i = Arrays.stream(pairs).iterator();

        while(i.hasNext()) {
            StyledPair pair = i.next();

            textComponent.append(getStyledComponent(pair.getString(), pair.getStyle()));
            if (i.hasNext())
                textComponent.append(getNewLine());
        }

        return textComponent;
    }

    public static MutableComponent getNewLine() {
        return Component.literal("\n");
    }
}