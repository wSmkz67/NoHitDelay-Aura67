package br.smkz.hitdelayfix;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * No 1.8.9, ao clicar no ar (errar o golpe) o jogo trava o clique esquerdo por 10 ticks
 * (Minecraft.leftClickCounter = 10). Este mod zera esse contador a cada tick.
 * Nao altera nada do servidor (o intervalo entre dois golpes que acertam continua o do servidor).
 */
@Mod(modid = "hitdelayfix", name = "HitDelayFix", version = "1.0.0", clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class HitDelayFix {
    private final Minecraft mc = Minecraft.getMinecraft();
    private Field counter;
    private boolean enabled = true;

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        try {
            counter = ReflectionHelper.findField(Minecraft.class, "leftClickCounter", "field_71429_W");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override public String getCommandName() { return "nohitdelay"; }
            @Override public List<String> getCommandAliases() { return Arrays.asList("nhd", "hitdelayfix"); }
            @Override public String getCommandUsage(ICommandSender s) { return "/nohitdelay  (liga/desliga)"; }
            @Override public int getRequiredPermissionLevel() { return 0; }
            @Override public boolean canCommandSenderUseCommand(ICommandSender s) { return true; }
            @Override public void processCommand(ICommandSender s, String[] a) {
                if (counter == null) { msg("\u00a7cN\u00e3o encontrei o campo do jogo; o mod n\u00e3o funciona nesta vers\u00e3o."); return; }
                enabled = !enabled;
                msg("Hit delay fix " + (enabled ? "\u00a7aligado" : "\u00a7cdesligado"));
            }
        });
    }

    private void msg(String s) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText("\u00a78[\u00a7bHitDelayFix\u00a78] \u00a7r" + s));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!enabled || counter == null || e.phase != TickEvent.Phase.START || mc.thePlayer == null) return;
        try {
            if (counter.getInt(mc) > 0) counter.setInt(mc, 0);
        } catch (Exception ex) {
            counter = null;   // nao tenta de novo
        }
    }
}
