package com.ntmcleanroom.compat.tinkers.fuel;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Drains stored HE and fills any adjacent tank exposing {@code IFluidHandler} (in practice, a
 * Tinkers' Seared Tank - already a recognized smeltery wall block) with a registered smeltery fuel
 * fluid. Tinkers' Antique's smeltery-structure validity list is hardcoded with no addon hook to add
 * a genuinely new wall/heater block, so this sits *next to* the structure rather than being part of
 * it - functionally powering the smeltery with HE instead of lava all the same.
 */
public class TileEntityHeSmelteryFueler extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2 {

    public static final long maxPower = 1_000_000;
    /** How much HE it costs to produce 1 mB of fuel fluid, before the speed tier multiplier. */
    private static final long HE_PER_MB = 500;
    private static final int MAX_FILL_PER_TICK_MB = 10;

    public long power = 0;
    private int tier = HeFuelSpeedTier.NORMAL.ordinal();

    public TileEntityHeSmelteryFueler() {
        super(0, false, false);
    }

    @Override
    public String getDefaultName() {
        return "container.he_smeltery_fueler";
    }

    public HeFuelSpeedTier getTier() {
        return HeFuelSpeedTier.VALUES[tier];
    }

    public void cycleTier() {
        tier = (tier + 1) % HeFuelSpeedTier.VALUES.length;
        markDirty();
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
        }

        if (power <= 0) {
            return;
        }

        int wantedMb = Math.min(MAX_FILL_PER_TICK_MB, (int) (power / HE_PER_MB));
        wantedMb = Math.round(wantedMb * getTier().fuelMultiplier);
        if (wantedMb <= 0) {
            return;
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            net.minecraft.tileentity.TileEntity neighbor = world.getTileEntity(pos.offset(facing));
            if (neighbor == null || !neighbor.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                continue;
            }

            IFluidHandler handler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler == null) {
                continue;
            }

            FluidStack toFill = new FluidStack(HeFuelerModule.heFuelFluid, wantedMb);
            int filled = handler.fill(toFill, true);
            if (filled > 0) {
                power -= filled * HE_PER_MB / Math.max(0.1F, getTier().fuelMultiplier);
                if (power < 0) {
                    power = 0;
                }
                markDirty();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        power = compound.getLong("power");
        tier = compound.getInteger("tier") % HeFuelSpeedTier.VALUES.length;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setInteger("tier", tier);
        return super.writeToNBT(compound);
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(new NTMEnergyCapabilityWrapper(this));
        }
        return super.getCapability(capability, facing);
    }
}
