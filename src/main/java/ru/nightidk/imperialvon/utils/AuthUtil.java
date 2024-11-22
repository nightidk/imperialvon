package ru.nightidk.imperialvon.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.configuration.AuthVariables;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;

public class AuthUtil {

    public static boolean isRegistered(String nickname) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonElement>>() {}.getType();
        List<JsonElement> jsonElements = gson.fromJson(AuthVariables.JSON_AUTH.get("users").getAsJsonArray(), listType);
        return jsonElements.stream().anyMatch(el -> {
            JsonObject jsonElement = el.getAsJsonObject();
            if (jsonElement.get("nickname") == null) return false;
            return Objects.equals(jsonElement.get("nickname").getAsString(), nickname);
        });
    }

    public static boolean passwordEquals(String nickname, String pass) throws NoSuchAlgorithmException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonElement>>() {}.getType();
        List<JsonElement> jsonElements = gson.fromJson(AuthVariables.JSON_AUTH.get("users").getAsJsonArray(), listType);
        Optional<JsonElement> playerObject = jsonElements.stream().filter(el -> {
            JsonObject jsonElement = el.getAsJsonObject();
            if (jsonElement.get("nickname") == null) return false;
            return Objects.equals(jsonElement.get("nickname").getAsString(), nickname);
        }).findFirst();
        String md5Password = md5(pass);
        return playerObject.filter(jsonElement -> md5Password.equals(jsonElement.getAsJsonObject().get("password").getAsString())).isPresent();
    }

    public static void register(String nickname, String password, String uuid) throws IOException, NoSuchAlgorithmException {
        JsonObject element = new JsonObject();
        element.addProperty("nickname", nickname);
        element.addProperty("password", md5(password));
        element.addProperty("uuid", uuid);
        AuthVariables.JSON_AUTH.get("users").getAsJsonArray().add(element);
        ConfigUtils.saveAuth(ImperialVon.authFile);
    }

    public static boolean isAuthorized(ServerPlayer player) {
        return AuthVariables.playerList.contains(player.getName().getString());
    }

    public static void setAuthorized(ServerPlayer player, boolean value) {
        if (value) {
//            Optional<Pair<String, Location>> location = AuthVariables.nonAuthPlayer.stream().filter(s -> s.getKey().equals(player.getName().getString())).findFirst();
//            location.ifPresent(stringLocationEntry -> AuthVariables.nonAuthPlayer.remove(stringLocationEntry));
            AuthVariables.playerList.add(player.getName().getString());
            player.setInvisible(false);
            player.setInvulnerable(false);
        } else {
//            AuthVariables.nonAuthPlayer.add(new Pair<>(player.getName().getString(), new Location(player.getX(), player.getY(), player.getZ())));
            AuthVariables.playerList.remove(player.getName().getString());
            player.setInvisible(true);
            player.setInvulnerable(true);
        }
    }

    public static void setAuthorized(ServerPlayer player) {
        setAuthorized(player, true);
    }

    public static MutableComponent getAuthMessage(ServerPlayer player) {
        if (isRegistered(player.getName().getString()))
            return getStyledComponent("Авторизируйтесь: /login <password>", TextStyleUtil.YELLOW.getStyle());
        else
            return getStyledComponent("Зарегистрируйтесь: /register <password> <password>", TextStyleUtil.YELLOW.getStyle());
    }

    protected static String md5(String value) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(value));
        return String.format("%032x", new BigInteger(1, md5.digest()));
    }
}