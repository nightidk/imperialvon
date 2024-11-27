package ru.nightidk.imperialvon.content.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.region.RegionFlags;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RegionFlagArgumentType implements ArgumentType<RegionFlags.FlagsEnum> {

    public static RegionFlagArgumentType flag() {
        return new RegionFlagArgumentType();
    }

    @Override
    public RegionFlags.FlagsEnum parse(com.mojang.brigadier.StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String value = reader.readString();
        if (value.isEmpty()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedBool().createWithContext(reader);
        try {
            RegionFlags.FlagsEnum flag = RegionFlags.FlagsEnum.fromString(value);
            ImperialVon.LOG.info("Input flag arg found: {}", flag.getFlagName());
            return flag;
        } catch (IllegalArgumentException e) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, value);
        }
    }

    public static RegionFlags.FlagsEnum getFlag(CommandContext<?> context, String name) {
        return context.getArgument(name, RegionFlags.FlagsEnum.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Arrays.stream(RegionFlags.FlagsEnum.values())
                .filter((f) -> f.getFlagName().startsWith(builder.getRemainingLowerCase()))
                .toList().forEach((f) -> builder.suggest(f.getFlagName()));
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return RegionFlags.FlagsEnum.getAllFlagNames();
    }
}