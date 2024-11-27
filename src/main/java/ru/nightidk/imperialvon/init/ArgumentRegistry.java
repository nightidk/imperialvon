package ru.nightidk.imperialvon.init;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.content.args.RegionFlagArgumentType;

public class ArgumentRegistry {
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, ImperialVon.MODID);

    public static final RegistryObject<SingletonArgumentInfo<RegionFlagArgumentType>> FLAG_ARG = ARGS.register("flag_arg", () -> SingletonArgumentInfo.contextFree(RegionFlagArgumentType::flag));


    public static void registerArgumentTypes(IEventBus eventBus) {
        ImperialVon.LOG.info("Запись аргументов в Registry...");

        ARGS.register(eventBus);
    }

    public static void registerArguments() {
        ImperialVon.LOG.info("Регистрация аргументов по классу...");

        ArgumentTypeInfos.registerByClass(
                RegionFlagArgumentType.class,
                FLAG_ARG.get()
        );
    }
}