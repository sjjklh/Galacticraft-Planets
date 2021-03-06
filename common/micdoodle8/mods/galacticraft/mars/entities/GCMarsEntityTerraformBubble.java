package micdoodle8.mods.galacticraft.mars.entities;

import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.GCCorePacketManager;
import micdoodle8.mods.galacticraft.mars.tile.GCMarsTileEntityTerraformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GCMarsEntityTerraformBubble extends Entity implements IPacketReceiver
{
    private double size;

    protected long ticks = 0;

    public GCMarsTileEntityTerraformer terraformer;
    
    public final double MAX_SIZE = 15.0D;

    public GCMarsEntityTerraformBubble(World world, Vector3 mainBlockVec, GCMarsTileEntityTerraformer terraformer)
    {
        this(world);
        this.posX = mainBlockVec.x + 0.5D;
        this.posY = mainBlockVec.y + 1.0D;
        this.posZ = mainBlockVec.z + 0.5D;
        this.terraformer = terraformer;
    }

    public GCMarsEntityTerraformBubble(World world)
    {
        super(world);
        this.noClip = true;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    protected boolean pushOutOfBlocks(double par1, double par3, double par5)
    {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    public void moveEntity(double par1, double par3, double par5)
    {
        ;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
    {
        this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);
    }

    @Override
    public void onEntityUpdate()
    {
        if (this.ticks >= Long.MAX_VALUE)
        {
            this.ticks = 1;
        }

        this.ticks++;

        if (this.terraformer != null)
        {
            final Vector3 vec = new Vector3(this.terraformer);

            this.posX = vec.x + 0.5D;
            this.posY = vec.y + 1.0D;
            this.posZ = vec.z + 0.5D;
        }

        super.onEntityUpdate();

        final TileEntity tileAt = this.worldObj.getBlockTileEntity(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY - 1.0), MathHelper.floor_double(this.posZ));

        if (tileAt instanceof GCMarsTileEntityTerraformer)
        {
            final GCMarsTileEntityTerraformer terraformer = (GCMarsTileEntityTerraformer) tileAt;

            this.terraformer = terraformer;
        }

        if (this.terraformer != null && (this.terraformer.terraformBubble == null || this.terraformer.terraformBubble.equals(this)) && !this.worldObj.isRemote)
        {
            this.terraformer.terraformBubble = this;
        }
        else if (!this.worldObj.isRemote)
        {
            this.setDead();
        }

        if (tileAt == null && !this.worldObj.isRemote)
        {
            this.setDead();
        }

        if (!this.worldObj.isRemote && this.terraformer != null)
        {
            this.size = Math.min(Math.max(0, this.size + 5F), this.MAX_SIZE);
        }

        if (this.terraformer != null)
        {
            final Vector3 vec = new Vector3(this.terraformer);

            this.posX = vec.x + 0.5D;
            this.posY = vec.y + 1.0D;
            this.posZ = vec.z + 0.5D;
        }

        if (!this.worldObj.isRemote && this.ticks % 5 == 0)
        {
            PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 50);
        }
    }

    public Packet getDescriptionPacket()
    {
        final Packet p = GCCorePacketManager.getPacket(GalacticraftCore.CHANNELENTITIES, this, this.size);
        return p;
    }

    @Override
    public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                this.size = dataStream.readDouble();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    public void setSize(double bubbleSize)
    {
        this.size = bubbleSize;
    }

    public double getSize()
    {
        return this.size;
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        this.size = nbttagcompound.getDouble("bubbleSize");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setDouble("bubbleSize", this.size);
    }
}
