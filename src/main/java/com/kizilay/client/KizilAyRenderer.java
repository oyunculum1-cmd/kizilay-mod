package com.kizilay.client;

import com.kizilay.entity.KizilAyKrali;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class KizilAyRenderer extends GeoEntityRenderer<KizilAyKrali> {
    public KizilAyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new KizilAyModel());
        this.shadowRadius = 1.8F;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    /** Ölürken yana yatmasın; ölüm animasyonunu model kendisi oynatıyor. */
    @Override
    protected float getDeathMaxRotation(KizilAyKrali animatable) {
        return 0.0F;
    }
}
