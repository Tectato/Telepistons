package net.fabricmc.telepistons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

	private static final Vector3f UP = new Vector3f(0,1,0);
	private static final Vector3f FORWARD = new Vector3f(0,0,1);
	private static final Vector3f RIGHT = new Vector3f(1,0,0);

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
								squishFactorsZ = new Vector3f(
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat(),
										factorArr.remove(0).getAsFloat());

								squishFactorsX = new Vector3f(
										squishFactorsZ.z(),
										squishFactorsZ.y(),
										squishFactorsZ.x());

								squishFactorsY = new Vector3f(
										squishFactorsZ.x(),
										squishFactorsZ.z(),
										squishFactorsZ.y());
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

					pistonArmBakedModel = BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), pistonArmModel);
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
