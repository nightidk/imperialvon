package ru.nightidk.imperialvon.configuration;

import com.google.gson.JsonObject;
import ru.nightidk.imperialvon.utils.Location;
import ru.nightidk.imperialvon.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class AuthVariables {
    public static JsonObject JSON_AUTH = new JsonObject();
    public static List<String> playerList = new ArrayList<>();
    public static List<Pair<String, Integer>> authTicks = new ArrayList<>();
}
