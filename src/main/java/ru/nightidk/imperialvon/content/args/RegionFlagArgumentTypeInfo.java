package ru.nightidk.imperialvon.content.args;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class RegionFlagArgumentTypeInfo implements ArgumentTypeInfo<RegionFlagArgumentType, RegionFlagArgumentTypeInfo.Template> {

    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
        // Ничего не пишем, так как у аргумента нет состояния
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
        return new Template();
    }

    @Override
    public void serializeToJson(Template template, JsonObject json) {
        // JSON не требуется для этого аргумента
    }

    @Override
    public Template unpack(RegionFlagArgumentType argumentType) {
        return new Template();
    }

    public static class Template implements ArgumentTypeInfo.Template<RegionFlagArgumentType> {
        @Override
        public RegionFlagArgumentType instantiate(CommandBuildContext context) {
            return RegionFlagArgumentType.flag();
        }

        @Override
        public ArgumentTypeInfo<RegionFlagArgumentType, ?> type() {
            return new RegionFlagArgumentTypeInfo();
        }
    }
}