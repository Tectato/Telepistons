package net.fabricmc.telepistons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Telepistons implements ClientModInitializer {

	public static ExtraModelKey<BlockStateModel> pistonArmModel;
	public static BlockStateModel pistonArmBakedModel;
	public static Random random = new Random();
	public static boolean emitSteam;
	public static boolean steamOverride = true;
	public static int particleCount;
	public static boolean squishArm;

	public static Vec3 squishFactorsX;
	public static Vec3 squishFactorsY;
	public static Vec3 squishFactorsZ;

	private static final float HALF_TURN = (float) Math.PI;
	private static final float QUART_TURN = (float) (Math.PI / 2.0f);

	private static final Vector3f UP = new Vector3f(0,1,0);
	private static final Vector3f FORWARD = new Vector3f(0,0,1);
	private static final Vector3f RIGHT = new Vector3f(1,0,0);

	@Override
	public void onInitializeClient() {
		ResourceLocation scissorPack = ResourceLocation.fromNamespaceAndPath("telepistons","scissor_pistons");
		ResourceLocation bellowsPack = ResourceLocation.fromNamespaceAndPath("telepistons","bellows_pistons");
		ResourceLocation stickySidesPack = ResourceLocation.fromNamespaceAndPath("telepistons","sticky_sides");
		ResourceLocation enableSteam = ResourceLocation.fromNamespaceAndPath("telepistons","enable_steam");
		FabricLoader.getInstance().getModContainer("telepistons").ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(scissorPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(bellowsPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(stickySidesPack, container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(enableSteam, container, ResourcePackActivationType.DEFAULT_ENABLED);
		});

		var pistonArm = ResourceLocation.fromNamespaceAndPath("telepistons","block/piston_arm");
		pistonArmModel = ExtraModelKey.create(pistonArm::toString);
		ModelLoadingPlugin.register(pluginContext -> {pluginContext.addModel(pistonArmModel, SimpleUnbakedExtraModel.blockStateModel(pistonArm));});

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public ResourceLocation getFabricId(){
					return ResourceLocation.fromNamespaceAndPath("telepistons","models");
				}

				@Override
				public void onResourceManagerReload(ResourceManager manager){
					Map<ResourceLocation, Resource> resourceMap = manager.listResources("models", path -> path.toString().endsWith("piston_arm.json"));

					for(Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()){
						try(InputStream stream = manager.getResource(entry.getKey()).get().open()) {
							BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
							JsonObject json = GsonHelper.parse(streamReader);

							JsonObject settings = json.get("telepistons").getAsJsonObject();

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

							System.out.println("[Telepistons] Read settings successfully");
						} catch(Exception e) {
							particleCount = 0;
							squishArm = false;
							System.out.println("Error:\n" + e);
							System.out.println("[Telepistons] Error while trying to read settings, using standard values");
						}
					}

					resourceMap = manager.listResources("models", path -> path.toString().endsWith("piston_particle.json"));

					steamOverride = false;
					for(Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()){
						try(InputStream stream = manager.getResource(entry.getKey()).get().open()) {
							BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
							JsonObject json = GsonHelper.parse(streamReader);

							JsonObject settings = json.get("telepistons").getAsJsonObject();
							steamOverride = settings.get("particleOverride").getAsBoolean();

							System.out.println("[Telepistons] Read particle setting successfully");
						} catch(Exception e) {
							System.out.println("[Telepistons] Particle setting file erroneous");
						}
					}

					emitSteam = steamOverride && (particleCount > 0);

					ModelManager modelManager = net.minecraft.client.Minecraft.getInstance().getModelManager();
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
