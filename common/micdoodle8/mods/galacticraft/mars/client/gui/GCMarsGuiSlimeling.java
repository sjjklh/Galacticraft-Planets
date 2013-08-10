package micdoodle8.mods.galacticraft.mars.client.gui;

import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PacketUtil;
import micdoodle8.mods.galacticraft.mars.GalacticraftMars;
import micdoodle8.mods.galacticraft.mars.entities.GCMarsEntitySlimeling;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GCMarsGuiSlimeling extends GuiScreen
{
    private final int xSize;
    private final int ySize;
    private static final ResourceLocation slimelingPanelGui = new ResourceLocation(GalacticraftMars.TEXTURE_DOMAIN, "textures/gui/slimelingPanel0.png");
    private final GCMarsEntitySlimeling slimeling;

    public static RenderItem drawItems = new RenderItem();
    
    public long timeBackspacePressed;
    public int cursorPulse;
    public int backspacePressed;
    public boolean isTextFocused = false;
    public int incorrectUseTimer;
    
    public GuiButton stayButton;
    
    public static boolean renderingOnGui = false;
    
    public GCMarsGuiSlimeling(GCMarsEntitySlimeling slimeling)
    {
        this.slimeling = slimeling;
        this.xSize = 176;
        this.ySize = 147;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonList.clear();
        final int var5 = (this.width - this.xSize) / 2;
        final int var6 = (this.height - this.ySize) / 2;
        this.stayButton = new GuiButton(0, var5 + 120, var6 + 102, 50, 20, "Stay");
        this.stayButton.enabled = this.mc.thePlayer.username.equals(this.slimeling.getOwnerName());
        this.buttonList.add(this.stayButton);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void keyTyped(char keyChar, int keyID)
    {
        if (!this.isTextFocused)
        {
            super.keyTyped(keyChar, keyID);
            return;
        }

        if (keyID == Keyboard.KEY_BACK)
        {
            if (slimeling.getName().length() > 0)
            {
                if (mc.thePlayer.username.equals(this.slimeling.getOwnerName()))
                {
                    slimeling.setName(slimeling.getName().substring(0, slimeling.getName().length() - 1));
                    this.timeBackspacePressed = System.currentTimeMillis();
                }
                else
                {
                    this.incorrectUseTimer = 10;
                }
            }
        }
        else if (keyChar == 22)
        {
            String pastestring = GuiScreen.getClipboardString();

            if (pastestring == null)
            {
                pastestring = "";
            }

            if (this.isValid(slimeling.getName() + pastestring))
            {
                if (mc.thePlayer.username.equals(this.slimeling.getOwnerName()))
                {
                    slimeling.setName(slimeling.getName() + pastestring);
                    slimeling.setName(slimeling.getName().substring(0, Math.min(slimeling.getName().length(), 16)));
                }
                else
                {
                    this.incorrectUseTimer = 10;
                }
            }
        }
        else if (this.isValid(slimeling.getName() + keyChar))
        {
            if (mc.thePlayer.username.equals(this.slimeling.getOwnerName()))
            {
                slimeling.setName(slimeling.getName() + keyChar);
                slimeling.setName(slimeling.getName().substring(0, Math.min(slimeling.getName().length(), 16)));
            }
            else
            {
                this.incorrectUseTimer = 10;
            }
        }
        
        PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftMars.CHANNEL, 0, new Object[] { this.slimeling.entityId, 1, this.slimeling.getName() }));
        
        super.keyTyped(keyChar, keyID);
    }

    public boolean isValid(String string)
    {
        return ChatAllowedCharacters.allowedCharacters.indexOf(string.charAt(string.length() - 1)) >= 0;
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            switch (par1GuiButton.id)
            {
            case 0:
                PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftMars.CHANNEL, 0, new Object[] { this.slimeling.entityId, 0, "" }));
                break;
            }
        }
    }

    @Override
    protected void mouseClicked(int px, int py, int par3)
    {
        final int var5 = (this.width - this.xSize) / 2;
        final int var6 = (this.height - this.ySize) / 2;
        final int startX = -20 + var5 + 60;
        final int startY = 45 + var6 - 13;
        final int width = this.xSize - 45;
        final int height = 18;

        if (px >= startX && px < startX + width && py >= startY && py < startY + height)
        {
            Gui.drawRect(startX, startY, startX + width, startY + height, 0xffA0A0A0);
            this.isTextFocused = true;
        }
        else
        {
            this.isTextFocused = false;
        }

        super.mouseClicked(px, py, par3);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        final int var5 = (this.width - this.xSize) / 2;
        final int var6 = (this.height - this.ySize) / 2;

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, -70.0F);
        Gui.drawRect(var5, var6 - 20, var5 + this.xSize, var6 + this.ySize - 20, 0xFF000000);
        GL11.glPopMatrix();
        
        int yOffset = (int)Math.floor(30.0D * (1.0F - this.slimeling.getScale()));
        
        drawSlimelingOnGui(this, this.slimeling, this.width / 2, var6 + 42 - yOffset, 70, var5 + 51 - par1, var6 + 75 - 50 - par2);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 150.0F);
        this.mc.renderEngine.func_110577_a(slimelingPanelGui);
        this.drawTexturedModalRect(var5, var6 - 20, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(var5 + this.xSize - 15, var6 - 11, 176, 0, 9, 9);
        this.drawTexturedModalRect(var5 + this.xSize - 15, var6 + 2, 185, 0, 9, 9);
        this.drawTexturedModalRect(var5 + this.xSize - 15, var6 + 15, 194, 0, 9, 9);
        String str = "" + Math.round(this.slimeling.getColorRed() * 1000) / 10.0F + "% ";
        this.drawString(this.fontRenderer, str, var5 + this.xSize - 15 - this.fontRenderer.getStringWidth(str), var6 - 10, GCCoreUtil.convertTo32BitColor(255, 255, 0, 0));
        str = "" + Math.round(this.slimeling.getColorGreen() * 1000) / 10.0F + "% ";
        this.drawString(this.fontRenderer, str, var5 + this.xSize - 15 - this.fontRenderer.getStringWidth(str), var6 + 3, GCCoreUtil.convertTo32BitColor(255, 0, 0, 255));
        str = "" + Math.round(this.slimeling.getColorBlue() * 1000) / 10.0F + "% ";
        this.drawString(this.fontRenderer, str, var5 + this.xSize - 15 - this.fontRenderer.getStringWidth(str), var6 + 16, GCCoreUtil.convertTo32BitColor(255, 0, 255, 0));
        
        super.drawScreen(par1, par2, par3);

        this.cursorPulse++;
        
        if (this.timeBackspacePressed > 0)
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && this.slimeling.getName().length() > 0)
            {
                if (System.currentTimeMillis() - this.timeBackspacePressed > 200 / (1 + this.backspacePressed * 0.3F) && mc.thePlayer.username.equals(this.slimeling.getOwnerName()))
                {
                    this.slimeling.setName(this.slimeling.getName().substring(0, this.slimeling.getName().length() - 1));
                    PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftMars.CHANNEL, 0, new Object[] { this.slimeling.entityId, 1, this.slimeling.getName() }));
                    this.timeBackspacePressed = System.currentTimeMillis();
                    this.backspacePressed++;
                }
                else if (!mc.thePlayer.username.equals(this.slimeling.getOwnerName()))
                {
                    this.incorrectUseTimer = 10;
                }
            }
            else
            {
                this.timeBackspacePressed = 0;
                this.backspacePressed = 0;
            }
        }
        
        if (this.incorrectUseTimer > 0)
        {
            this.incorrectUseTimer--;
        }
        
        final int dX = -45;
        final int dY = 45;

        final int startX = -20 + var5 + 60;
        final int startY = dY + var6 - 10;
        final int width = this.xSize - 60;
        final int height = 15;
        Gui.drawRect(startX, startY, startX + width, startY + height, 0xffA0A0A0);
        Gui.drawRect(startX + 1, startY + 1, startX + width - 1, startY + height - 1, 0xFF000000);
        this.drawString(this.fontRenderer, this.slimeling.getName() + (this.cursorPulse / 24 % 2 == 0 && this.isTextFocused ? "_" : ""), startX + 4, startY + 4, this.incorrectUseTimer > 0 ? GCCoreUtil.convertTo32BitColor(255, 255, 20, 20) : 0xe0e0e0);

        this.stayButton.displayString = this.slimeling.isSitting() ? "Follow" : "Sit";
        
        this.fontRenderer.drawString("Name: ", dX + var5 + 55, dY + var6 - 6, 0x404040);
        this.fontRenderer.drawString("Owner: " + this.slimeling.getOwnerName(), dX + var5 + 55, dY + var6 + 7, 0x404040);
        this.fontRenderer.drawString("Kills: " + this.slimeling.getKillCount(), dX + var5 + 55, dY + var6 + 20, 0x404040);
        this.fontRenderer.drawString("Scale: " + (Math.round((this.slimeling.getAge() / (float)this.slimeling.MAX_AGE) * 1000.0F) / 10.0F) + "%", dX + var5 + 55, dY + var6 + 33, 0x404040);
        str = "" + (this.slimeling.isSitting() ? "Sitting" : "Following");
        this.fontRenderer.drawString(str, var5 + 145 - this.fontRenderer.getStringWidth(str) / 2, var6 + 92, 0x404040);
        str = "Attack Damage: " + Math.round(slimeling.getDamage() * 100.0F) / 100.0F;
        this.fontRenderer.drawString(str, dX + var5 + 55, dY + var6 + 33 + 13, 0x404040);
        str = "Favorite Food: ";
        this.fontRenderer.drawString(str, dX + var5 + 55, dY + var6 + 46 + 13, 0x404040);

        GCMarsGuiSlimeling.drawItems.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, new ItemStack(Item.itemsList[this.slimeling.getFavoriteFood()]), dX + var5 + 55 + this.fontRenderer.getStringWidth(str), dY + var6 + 41 + 14);
        GL11.glPopMatrix();
    }

    public static void drawSlimelingOnGui(GCMarsGuiSlimeling screen, GCMarsEntitySlimeling slimeling, int par1, int par2, int par3, float par4, float par5)
    {
        GCMarsGuiSlimeling.renderingOnGui = true;
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(par1, par2, 50.0F);
        GL11.glScalef(-par3 / 2.0F, par3 / 2.0F, par3 / 2.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = slimeling.renderYawOffset;
        float f3 = slimeling.rotationYaw;
        float f4 = slimeling.rotationPitch;
        par4 += 40;
        par5 -= 20;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float) Math.atan(par5 / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        slimeling.renderYawOffset = (float) Math.atan(par4 / 40.0F) * 20.0F;
        slimeling.rotationYaw = (float) Math.atan(par4 / 40.0F) * 40.0F;
        slimeling.rotationPitch = -((float) Math.atan(par5 / 40.0F)) * 20.0F;
        slimeling.rotationYawHead = slimeling.rotationYaw;
        GL11.glTranslatef(0.0F, slimeling.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(slimeling, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        slimeling.renderYawOffset = f2;
        slimeling.rotationYaw = f3;
        slimeling.rotationPitch = f4;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GCMarsGuiSlimeling.renderingOnGui = false;
    }
}