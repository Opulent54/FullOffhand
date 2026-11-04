package site.xscapesmp.fulloffhand;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.UUID;

public final class FullOffhand implements ModInitializer {
    private static final String PREFIX = "[FullOffhand] ";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("offhand")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    openBedrockMenu(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("offhandui")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    openBedrockMenu(player);
                    return 1;
                }));
    }

    private static void openBedrockMenu(ServerPlayer player) {
        GeyserApi api = GeyserApi.api();
        if (api == null || api.connectionByUuid(player.getUUID()) == null) {
            player.sendSystemMessage(Component.literal(PREFIX +
                    "This menu is only available to Bedrock players connected through Geyser."));
            return;
        }

        SimpleForm.Builder form = SimpleForm.builder()
                .title("Offhand")
                .content("Choose an item from your hotbar to equip in your offhand. " +
                         "The item currently in your offhand moves into that hotbar slot.");

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            String label = (slot + 1) + ". " +
                    (stack.isEmpty() ? "Empty" : stack.getHoverName().getString() + " x" + stack.getCount());
            form.button(label);
        }
        form.button("Swap main hand and offhand");

        UUID playerId = player.getUUID();
        form.validResultHandler(response -> {
            int selected = response.clickedButtonId();
            // Form callbacks may run outside the Minecraft server thread.
            player.getServer().execute(() -> {
                ServerPlayer current = player.getServer().getPlayerList().getPlayer(playerId);
                if (current == null || !current.isAlive()) {
                    return;
                }
                if (selected >= 0 && selected < 9) {
                    moveHotbarSlotToOffhand(current, selected);
                } else if (selected == 9) {
                    swapHands(current);
                }
            });
        });

        api.sendForm(playerId, form.build());
    }

    private static void moveHotbarSlotToOffhand(ServerPlayer player, int slot) {
        if (slot < 0 || slot > 8) return;

        ItemStack selected = player.getInventory().getItem(slot).copy();
        ItemStack previousOffhand = player.getOffhandItem().copy();

        player.getInventory().setItem(slot, previousOffhand);
        player.setItemInHand(InteractionHand.OFF_HAND, selected);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal(PREFIX + "Offhand updated."));
    }

    private static void swapHands(ServerPlayer player) {
        ItemStack main = player.getMainHandItem().copy();
        ItemStack off = player.getOffhandItem().copy();

        player.setItemInHand(InteractionHand.MAIN_HAND, off);
        player.setItemInHand(InteractionHand.OFF_HAND, main);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal(PREFIX + "Hands swapped."));
    }
}
