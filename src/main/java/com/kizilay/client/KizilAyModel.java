package com.kizilay.client;

import com.kizilay.KizilAyMod;
import com.kizilay.entity.KizilAyKrali;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KizilAyModel extends GeoModel<KizilAyKrali> {
    private static final ResourceLocation MODEL = new ResourceLocation(KizilAyMod.MODID, "geo/kizil_ay_krali.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation(KizilAyMod.MODID, "animations/kizil_ay_krali.animation.json");
    private static final ResourceLocation TEX1 = new ResourceLocation(KizilAyMod.MODID, "textures/entity/kizil_ay_krali.png");
    private static final ResourceLocation TEX2 = new ResourceLocation(KizilAyMod.MODID, "textures/entity/kizil_ay_krali_mavi.png");

    @Override
    public ResourceLocation getModelResource(KizilAyKrali animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KizilAyKrali animatable) {
        return animatable.getPhase() >= 2 ? TEX2 : TEX1;
    }

    @Override
    public ResourceLocation getAnimationResource(KizilAyKrali animatable) {
        return ANIM;
    }
}
