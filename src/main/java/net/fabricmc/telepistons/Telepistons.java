package net.fabricmc.telepistons;

import java.util.Random;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.telepistons.simpleLibs.simpleConfig.SimpleConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Telepistons implements ModInitializer {
	
	public static final Block PISTON_ARM = new PistonArm(FabricBlockSettings.of(Material.METAL).hardness(80.0f));
	public static Random random = new Random();
	public static boolean emitSteam;
	public static int particleCount;
	public static boolean squishArm;
	
	@Override
	public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("telepistons", "piston_arm"), PISTON_ARM);
        SimpleConfig CONFIG = SimpleConfig.of( "telepistons" ).provider( this::provider ).request();
        particleCount = CONFIG.getOrDefault("particles", 4);
        particleCount = Math.max(particleCount, 0);
        emitSteam = particleCount > 0;
        squishArm = CONFIG.getOrDefault("squishArm", false);
        System.out.println("[Tec] Ready to animate some piston arms! Steam particles enabled: " + emitSteam
        		+ "\n Piston arm squish enabled: "+ squishArm);
	}
	
	private String provider( String filename ) {
		return "#Steam particle count: (0 to disable)\n"
				+ "particles=4\n"
				+ "#Enable piston arm squish: (to be used with bellows-like model, resourcepack provided on CurseForge page)\n"
				+ "squishArm=false\n";
    }
}
