package ru.nightidk.imperialvon.listeners.serverside;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.configuration.AuthVariables;
import ru.nightidk.imperialvon.utils.AuthUtil;
import ru.nightidk.imperialvon.utils.Pair;
import ru.nightidk.imperialvon.utils.TextStyleUtil;

import java.util.Objects;

import static ru.nightidk.imperialvon.utils.AuthUtil.isAuthorized;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.sendChatMessageToPlayer;

public class AuthEventListener {

    public static boolean canChangeGameMode(ServerPlayer player) {
        if (ImperialVon.server == null) return false;
        return isAuthorized(player);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            if (isAuthorized(player)) return;
            Pair<String, Integer> playerAuthInfo = AuthVariables.authTicks.stream().filter(p -> Objects.equals(p.getKey(), player.getName().getString())).findFirst().orElse(new Pair<>(player.getName().getString(), -1));
            int indexPlayer = AuthVariables.authTicks.indexOf(playerAuthInfo);
            if (indexPlayer == -1) return;
            int kickTimer = playerAuthInfo.getValue();

            if (kickTimer <= 0 && player.connection.getConnection().isConnected()) {
                player.connection.disconnect(getStyledComponent("Время на авторизацию вышло.", TextStyleUtil.RED.getStyle()));
            } else {
                if (kickTimer % 1200 == 0 && kickTimer != 300 * 20)
                    sendChatMessageToPlayer(player, AuthUtil.getAuthMessage(player));
                AuthVariables.authTicks.set(indexPlayer, new Pair<>(player.getName().getString(), kickTimer - 1));
            }
        }
    }

    public static boolean onPlayerCommand(ServerPlayer player, String command) {
        if (player == null) return false;
        if (isAuthorized(player))
            return false;
        else {
            return !command.startsWith("login")
                    && !command.startsWith("register")
                    && !command.startsWith("l ")
                    && !command.startsWith("reg ");
        }
    }
}