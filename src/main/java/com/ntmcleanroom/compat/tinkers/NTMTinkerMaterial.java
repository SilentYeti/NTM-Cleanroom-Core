package com.ntmcleanroom.compat.tinkers;

import net.minecraftforge.fluids.Fluid;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

/**
 * Describes one hbm metal being bridged into Tinkers' Antique: its ore dict ingot name, molten
 * fluid color, and (for "full" materials) tool stats and traits. Placeholder materials just have
 * {@code headStats}/{@code handleStats} left {@code null}, which registers them as meltable/castable
 * with Tinkers' own generic default stats and no special traits.
 */
public class NTMTinkerMaterial {

    public final String id;
    public final String displayName;
    public final int color;
    public final String ingotOreDict;
    public final HeadMaterialStats headStats;
    public final HandleMaterialStats handleStats;
    public final ITrait[] traits;

    public Material material;
    public Fluid fluid;

    public NTMTinkerMaterial(String id, String displayName, int color, String ingotOreDict) {
        this(id, displayName, color, ingotOreDict, null, null);
    }

    public NTMTinkerMaterial(String id, String displayName, int color, String ingotOreDict,
            HeadMaterialStats headStats, HandleMaterialStats handleStats, ITrait... traits) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.ingotOreDict = ingotOreDict;
        this.headStats = headStats;
        this.handleStats = handleStats;
        this.traits = traits;
    }

    public boolean isFull() {
        return headStats != null;
    }
}
