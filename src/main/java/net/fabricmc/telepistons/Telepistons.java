package net.fabricmc.telepistons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class Telepistons implements ModInitializer {

	public static Identifier pistonArmModel;
	public static BakedModel pistonArmBakedModel;
	public static Random random = new Random();
	public static boolean emitSteam;
	public static boolean steamOverride = true;
	public static int particleCount;
	public static boolean squishArm;

	public static Vector3f squishFactorsX;
	public static Vector3f squishFactorsY;
	public static Vector3f squishFactorsZ;

	private static final float HALF_TURN = (float) Math.PI;
	private static final float QUART_TURN = (float) (Math.PI / 2.0f);

	@Override
	public void onInitialize() {
		Identifier scissorPack = new Identifier("telepistons","scissor_pistons");
		Identifier bellowsPack = new Identifier("telepistons","bellows_pistons");
		Identifier stickySidesPack = new Identifier("telepistons","sticky_sides");
		Identifier enableSteam = new Identifier("telepistons","enable_steam");
		FabricLoader.getInstance().getModContainer("telepistons").ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(scissorPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(bellowsPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(stickySidesPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(enableSteam, container, ResourcePackActivationType.DEFAULT_ENABLED);
		});

		pistonArmModel = new Identifier("telepistons","block/piston_arm");
		ModelLoadingRegistry.INSTANCE.registerModelProvider((modelManager, out) -> out.accept(pistonArmModel));

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId(){
					return new Identifier("telepistons","models");
				}

				@Override
				public void apply(ResourceManager manager){
					Collection<Identifier> resourceCollection = manager.findResources("models", path -> path.toString().endsWith("piston_arm.json"));

					for(Identifier entry : resourceCollection){
						try(InputStream stream = manager.getResource(entry).getInputStream()) {
							BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
							JsonObject json = JsonHelper.deserialize(streamReader);

							JsonObject settings = json.get("telepistons").getAsJsonObject();

							squishArm = settings.get("squish").getAsBoolean();
							particleCount = Math.max(settings.get("particles").getAsInt(), 0);
							if(squishArm) {
								JsonArray factorArr = settings.get("squishedScale").getAsJsonArray();
								squishFactorsZ = new Vector3f(
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat());

								squishFactorsX = new Vector3f(
										squishFactorsZ.getZ(),
										squishFactorsZ.getY(),
										squishFactorsZ.getX());

								squishFactorsY = new Vector3f(
										squishFactorsZ.getX(),
										squishFactorsZ.getZ(),
										squishFactorsZ.getY());
							}

							System.out.println("[Telepistons] Read settings successfully");
						} catch(Exception e) {
							particleCount = 0;
							squishArm = false;
							System.out.println("Error:\n" + e);
							System.out.println("[Telepistons] Error while trying to read settings, using standard values");
						}
					}

					resourceCollection = manager.findResources("models", path -> path.toString().endsWith("piston_particle.json"));

					steamOverride = false;
					for(Identifier entry : resourceCollection){
						try(InputStream stream = manager.getResource(entry).getInputStream()) {
							BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
							JsonObject json = JsonHelper.deserialize(streamReader);

							JsonObject settings = json.get("telepistons").getAsJsonObject();
							steamOverride = settings.get("particleOverride").getAsBoolean();

							System.out.println("[Telepistons] Read particle setting successfully");
						} catch(Exception e) {
							System.out.println("[Telepistons] Particle setting file erroneous");
						}
					}

					emitSteam = steamOverride && (particleCount > 0);

					pistonArmBakedModel = BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), pistonArmModel);
				}
			}
		);
	}

	public static Quaternion getRotationQuaternion(Direction dir){
		// Java 8 does not support switch() expressions, which were only added in Java 14.
		// Refactored to use a classic switch() statement instead.
		switch (dir) {
			case UP:
				return new Quaternion(90f, 0f, 0f, true);
			case DOWN:
				return new Quaternion(-90f, 0f, 0f, true);
			case NORTH:
				return new Quaternion(0f, 0f, 0f, true);
			case SOUTH:
				return new Quaternion(0f, 180f, 0f, true);
			case EAST:
				return new Quaternion(0f, -90f, 0f, true);
			case WEST:
				return new Quaternion(0f, 90f, 0f, true);
			default:
				// ※ This should never happen.
				throw new IllegalArgumentException(String.format("An unknown direction (%s) was provided to getRotationQuaternion()! This should never happen.", dir));
		}
	}
}
