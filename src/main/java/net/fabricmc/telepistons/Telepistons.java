package net.fabricmc.telepistons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Telepistons implements ClientModInitializer {

	public static Identifier pistonArmModel;
	public static BakedModel pistonArmBakedModel;
	public static Random random = new Random();
	public static boolean emitSteam;
	public static boolean steamOverride = true;
	public static int particleCount;
	public static boolean squishArm;

	public static Vec3d squishFactorsX;
	public static Vec3d squishFactorsY;
	public static Vec3d squishFactorsZ;

	private static final float HALF_TURN = (float) Math.PI;
	private static final float QUART_TURN = (float) (Math.PI / 2.0f);

	private static final Vector3f UP = new Vector3f(0,1,0);
	private static final Vector3f FORWARD = new Vector3f(0,0,1);
	private static final Vector3f RIGHT = new Vector3f(1,0,0);

	@Override
	public void onInitializeClient() {
		Identifier scissorPack = Identifier.of("telepistons","scissor_pistons");
		Identifier bellowsPack = Identifier.of("telepistons","bellows_pistons");
		Identifier stickySidesPack = Identifier.of("telepistons","sticky_sides");
		Identifier enableSteam = Identifier.of("telepistons","enable_steam");
		FabricLoader.getInstance().getModContainer("telepistons").ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(scissorPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(bellowsPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(stickySidesPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(enableSteam, container, ResourcePackActivationType.DEFAULT_ENABLED);
		});

		pistonArmModel = Identifier.of("telepistons","block/piston_arm");
		ModelLoadingPlugin.register(pluginContext -> {pluginContext.addModels(pistonArmModel);});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId(){
					return Identifier.of("telepistons","models");
				}

				@Override
				public void reload(ResourceManager manager){
					Map<Identifier, Resource> resourceMap = manager.findResources("models", path -> path.toString().endsWith("piston_arm.json"));

					for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()){
						try(InputStream stream = manager.getResource(entry.getKey()).get().getInputStream()) {
							BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
							JsonObject json = JsonHelper.deserialize(streamReader);

							JsonObject settings = json.get("telepistons").getAsJsonObject();

							squishArm = settings.get("squish").getAsBoolean();
							particleCount = Math.max(settings.get("particles").getAsInt(), 0);
							if(squishArm) {
								JsonArray factorArr = settings.get("squishedScale").getAsJsonArray();
								squishFactorsZ = new Vec3d(
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat());

								squishFactorsX = new Vec3d(
										squishFactorsZ.getZ(),
										squishFactorsZ.getY(),
										squishFactorsZ.getX());

								squishFactorsY = new Vec3d(
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

					resourceMap = manager.findResources("models", path -> path.toString().endsWith("piston_particle.json"));

					steamOverride = false;
					for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()){
						try(InputStream stream = manager.getResource(entry.getKey()).get().getInputStream()) {
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

					BakedModelManager modelManager = net.minecraft.client.MinecraftClient.getInstance().getBakedModelManager();
					pistonArmBakedModel = modelManager.getModel(pistonArmModel);
					if(pistonArmBakedModel == null) System.out.println("Baked Model is Null!");
				}
			}
		);
	}

	public static Quaternionf getRotationQuaternion(Direction dir){
		return switch(dir){
			case UP -> new Quaternionf(new AxisAngle4f(QUART_TURN, RIGHT));
			case DOWN -> new Quaternionf(new AxisAngle4f(-QUART_TURN, RIGHT));
			case NORTH -> new Quaternionf();
			case SOUTH -> new Quaternionf(new AxisAngle4f(HALF_TURN, UP));
			case EAST ->  new Quaternionf(new AxisAngle4f(-QUART_TURN, UP));
			case WEST ->  new Quaternionf(new AxisAngle4f(QUART_TURN, UP));
		};
	}
}
