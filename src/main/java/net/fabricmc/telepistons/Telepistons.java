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
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class Telepistons implements ModInitializer {

	public static Identifier pistonArmModel;
	public static BakedModel pistonArmBakedModel;
	public static Random random = new Random();
	public static boolean emitSteam;
	public static boolean steamOverride = true;
	public static int particleCount;
	public static boolean squishArm;

	public static Vec3f squishFactorsX;
	public static Vec3f squishFactorsY;
	public static Vec3f squishFactorsZ;

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
				public void reload(ResourceManager manager){
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
								squishFactorsZ = new Vec3f(
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat());

								squishFactorsX = new Vec3f(
										squishFactorsZ.getZ(),
										squishFactorsZ.getY(),
										squishFactorsZ.getX());

								squishFactorsY = new Vec3f(
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
		return switch(dir){
			case UP -> Quaternion.fromEulerXyz(QUART_TURN, 0.0f, 0.0f);
			case DOWN -> Quaternion.fromEulerXyz(-QUART_TURN, 0.0f, 0.0f);
			case NORTH -> Quaternion.fromEulerXyz(0.0f, 0.0f, 0.0f);
			case SOUTH -> Quaternion.fromEulerXyz(0.0f, HALF_TURN, 0.0f);
			case EAST -> Quaternion.fromEulerXyz(0.0f, -QUART_TURN, 0.0f);
			case WEST -> Quaternion.fromEulerXyz(0.0f, QUART_TURN, 0.0f);
		};
	}
}
