package com.universeprojects.miniup.server;

import java.util.Random;

/**
 * Generator to aid with 2D word generation.
 *
 * I DID NOT WRITE THE FRACTAL GENERATION THANKS STACK OVERFLOW, modified and removed some parts, and hardcoded some values
 * @author Fayyne
 */
public class NoiseGenerator {
    /** Source of entropy */
    private Random rand_;

    /** Amount of roughness */
    private float roughness_;

    /** Plasma fractal grid */
    private float[][] grid_;


    /** Generate a noise source based upon the midpoint displacement fractal.
     *
     * @param rand The random number generator
     * @param width the width of the grid
     * @param height the height of the grid
     */
    private NoiseGenerator(Random rand, int width, int height) {
        roughness_ = 1.0f / width;
        grid_ = new float[width][height];
        rand_ = (rand == null) ? new Random() : rand;
    }


    private void initialise() {
        int xh = grid_.length - 1;
        int yh = grid_[0].length - 1;

        // set the corner points
        grid_[0][0] = rand_.nextFloat() - 0.5f;
        grid_[0][yh] = rand_.nextFloat() - 0.5f;
        grid_[xh][0] = rand_.nextFloat() - 0.5f;
        grid_[xh][yh] = rand_.nextFloat() - 0.5f;

        // generate the fractal
        generate(0, 0, xh, yh);
    }


    // Add a suitable amount of random displacement to a point
    private float roughen(float v, int l, int h) {
        return v + roughness_ * (float) (rand_.nextGaussian() * (h - l));
    }


    // generate the fractal
    private void generate(int xl, int yl, int xh, int yh) {
        int xm = (xl + xh) / 2;
        int ym = (yl + yh) / 2;
        if ((xl == xm) && (yl == ym)) return;

        grid_[xm][yl] = 0.5f * (grid_[xl][yl] + grid_[xh][yl]);
        grid_[xm][yh] = 0.5f * (grid_[xl][yh] + grid_[xh][yh]);
        grid_[xl][ym] = 0.5f * (grid_[xl][yl] + grid_[xl][yh]);
        grid_[xh][ym] = 0.5f * (grid_[xh][yl] + grid_[xh][yh]);

        float v = roughen(0.5f * (grid_[xm][yl] + grid_[xm][yh]), xl + yl, yh
                + xh);
        grid_[xm][ym] = v;
        grid_[xm][yl] = roughen(grid_[xm][yl], xl, xh);
        grid_[xm][yh] = roughen(grid_[xm][yh], xl, xh);
        grid_[xl][ym] = roughen(grid_[xl][ym], yl, yh);
        grid_[xh][ym] = roughen(grid_[xh][ym], yl, yh);

        generate(xl, yl, xm, ym);
        generate(xm, yl, xh, ym);
        generate(xl, ym, xm, yh);
        generate(xm, ym, xh, yh);
    }

    //May look into refactoring this, it's ugly but gets the job done for now.
    private static BuildingCell[][] getBuildingCells(NoiseGenerator noiseGeneratorForImage, NoiseGenerator noiseGeneratorForZIndex) {
        int row = noiseGeneratorForImage.grid_.length;
        int col = noiseGeneratorForImage.grid_[0].length;
        BuildingCell[][] buildingCells = new BuildingCell[row][col];
        for(int i = 0;i < row;i++) {
            for(int j = 0;j < col;j++) {
                //TODO: Turn this into a int and mod(%) it down, will need to take in number of possible tiles, and change BuildingCells filename to a index.
                float imageNoise = noiseGeneratorForImage.grid_[i][j] ;
                BuildingCell buildingCell = new BuildingCell();
                buildingCell.zIndex = (int)(noiseGeneratorForZIndex.grid_[i][j] * 10);

                if(imageNoise > 0.75)
                    buildingCell.fileName = "tile-grass0.png";
                else if (imageNoise <= 0.75 && imageNoise > 0.5)
                    buildingCell.fileName = "tile-grass1.png";
                else if (imageNoise <= 0.50 && imageNoise > 0.25)
                    buildingCell.fileName = "tile-grass2.png";
                else if (imageNoise <= 0.25 && imageNoise > 0)
                    buildingCell.fileName = "tile-grass3.png";
                else if (imageNoise <= 0 && imageNoise > -0.25)
                    buildingCell.fileName = "tile-grass4.png";
                else if (imageNoise <= -0.25 && imageNoise > -0.5)
                    buildingCell.fileName = "tile-grass5.png";
                else if (imageNoise <= -0.5 && imageNoise > -0.75)
                    buildingCell.fileName = "tile-grass6.png";
                else if (imageNoise < -0.75)
                    buildingCell.fileName = "tile-grass7.png";

                buildingCells[i][j] = buildingCell;
            }
        }
        return buildingCells;
    }

    /** For testing */
    public static BuildingCell[][] getBuildingCells(int seed, int width, int height) {
        NoiseGenerator noiseGeneratorForImage = new NoiseGenerator(new Random(seed), width, height);
        NoiseGenerator noiseGeneratorForZIndex = new NoiseGenerator(new Random(seed + seed), width, height);
        noiseGeneratorForImage.initialise();
        noiseGeneratorForZIndex.initialise();

        return  getBuildingCells(noiseGeneratorForImage, noiseGeneratorForZIndex);
    }
}

class BuildingCell {
    String fileName;
    int zIndex;
}