package com.playerrealms.droplet;

import com.playerrealms.droplet.lang.Language;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

	@Override
	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
		
		ChunkData data = createChunkData(world);
		
		for(int i = 0; i < 16;i++){
			for(int j = 0; j < 16;j++){
				biome.setBiome(i, j, Biome.VOID);
			}
		}
		
		data.setRegion(0, 0, 0, 16, 256, 16, Material.AIR);
		
		if(x == 0 && z == 0){
			for(int i = -3; i < 4;i++){
				for(int j = -3; j < 4;j++){
					data.setBlock(i + 8, 60, j + 8, Material.BEDROCK);
				}
			}
			data.setBlock(8, 61, 8, Material.BEDROCK);
			
		}
		
		return data;
	}
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 8, 60, 8);
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList(new SignBlockPopulator());
	}
	
	static class SignBlockPopulator extends BlockPopulator {

		@Override
		public void populate(World world, Random random, Chunk source) {
			if(source.getX() == 0 && source.getZ() == 0){
				
				Block block = source.getBlock(8, 62, 8);
				
				block.setType(Material.SIGN_POST);
				
				Sign sign = (Sign) block.getState();
				
				String lang = DropletAPI.getThisServer().getLanguage();
				
				sign.setLine(0, Language.getLanguage(lang).getText("void_sign.1"));
				sign.setLine(1, Language.getLanguage(lang).getText("void_sign.2"));
				sign.setLine(2, Language.getLanguage(lang).getText("void_sign.3"));
				sign.setLine(3, Language.getLanguage(lang).getText("void_sign.4"));
				
				sign.update(true);
				
			}
		}
	}
	
}
