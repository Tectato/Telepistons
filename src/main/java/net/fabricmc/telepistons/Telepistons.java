package net.fabricmc.telepistons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telepistons implements ClientModInitializer {
	public static final String MOD_ID = "telepistons";
	public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

	public static final Block pistonArmBlock = register("piston_arm", PistonArm::new, BlockBehaviour.Properties.ofFullCopy(Blocks.END_ROD).lightLevel(value -> 0));
	public static Random random = new Random();
	public static boolean emitSteam;
	public static boolean steamOverride = true;
	public static int particleCount;
	public static boolean squishArm;

	public static Vec3 squishFactorsX;
	public static Vec3 squishFactorsY;
	public static Vec3 squishFactorsZ;

	@Override
	public void onInitializeClient() {
		Identifier scissorPack = Identifier.fromNamespaceAndPath(MOD_ID,"scissor_pistons");
		Identifier bellowsPack = Identifier.fromNamespaceAndPath(MOD_ID,"bellows_pistons");
		Identifier stickySidesPack = Identifier.fromNamespaceAndPath(MOD_ID,"sticky_sides");
		Identifier enableSteam = Identifier.fromNamespaceAndPath(MOD_ID,"enable_steam");
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
			ResourceLoader.registerBuiltinPack(scissorPack, container, PackActivationType.NORMAL);
			ResourceLoader.registerBuiltinPack(bellowsPack, container, PackActivationType.NORMAL);
			ResourceLoader.registerBuiltinPack(stickySidesPack, container, PackActivationType.NORMAL);
			ResourceLoader.registerBuiltinPack(enableSteam, container, PackActivationType.DEFAULT_ENABLED);
		});

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
			Identifier.fromNamespaceAndPath(MOD_ID,"models"),
			(ResourceManagerReloadListener) manager -> {
				Map<Identifier, Resource> resourceMap = manager.listResources("models", path -> path.toString().endsWith("piston_arm.json"));

				for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()){
					try(InputStream stream = manager.getResource(entry.getKey()).get().open()) {
						BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
						JsonObject json = GsonHelper.parse(streamReader);

						JsonObject settings = json.get(MOD_ID).getAsJsonObject();

						squishArm = settings.get("squish").getAsBoolean();
						particleCount = Math.max(settings.get("particles").getAsInt(), 0);
						if(squishArm) {
							JsonArray factorArr = settings.get("squishedScale").getAsJsonArray();
							squishFactorsZ = new Vec3(
									factorArr.remove(0).getAsFloat(),
									factorArr.remove(0).getAsFloat(),
									factorArr.remove(0).getAsFloat());

							squishFactorsX = new Vec3(
									squishFactorsZ.z(),
									squishFactorsZ.y(),
									squishFactorsZ.x());

							squishFactorsY = new Vec3(
									squishFactorsZ.x(),
									squishFactorsZ.z(),
									squishFactorsZ.y());
						}

						logger.info("[Telepistons] Read settings successfully");
					} catch(Exception e) {
						particleCount = 0;
						squishArm = false;
						logger.error("Error:\n" + e);
						logger.error("[Telepistons] Error while trying to read settings, using standard values");
					}
				}

				resourceMap = manager.listResources("models", path -> path.toString().endsWith("piston_particle.json"));

				steamOverride = false;
				for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()){
					try(InputStream stream = manager.getResource(entry.getKey()).get().open()) {
						BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
						JsonObject json = GsonHelper.parse(streamReader);

						JsonObject settings = json.get(MOD_ID).getAsJsonObject();
						steamOverride = settings.get("particleOverride").getAsBoolean();

						logger.info("[Telepistons] Read particle setting successfully");
					} catch(Exception e) {
						logger.error("[Telepistons] Particle setting file erroneous");
					}
				}

				emitSteam = steamOverride && (particleCount > 0);
			}
        );
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
		Block block = blockFactory.apply(settings.setId(blockKey));

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}
}
