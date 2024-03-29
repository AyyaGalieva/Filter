package ru.nsu.fit.g16201.galieva.Filter.Model;

import ru.nsu.fit.g16201.galieva.Filter.View.GUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Model {
    private GUI view;

    private BufferedImage imageA, imageB, imageC;

    private int robertsParameter = 50;
    private int sobelParameter = 200;

    private int countRed = 2, countGreen = 2, countBlue = 2;

    private int rotationAngle = 0;
    private int gammaCor = 100;

    private boolean volumeRenderingParametersSet = false;

    private ArrayList<Double[]> absorbtionValues;
    private ArrayList<Integer[]> emissionValues;
    private ArrayList<Double[]> charges;

    private double[] absorbtion;
    private int[][] emission;

    public Model(){}

    public void setView(GUI view) {
        this.view = view;
    }

    public void loadImage(String path) {
        try {
            setImageA(ImageIO.read(new File(path)));
            setImageB(null);
            setImageC(null);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void saveImage(String path) {
        try {
            ImageIO.write(imageC, "bmp", new File(path + ".bmp"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private int getRed(int color) {
        return (color >> 16)&0xff;
    }

    private int getGreen(int color) {
        return (color >> 8)&0xff;
    }

    private int getBlue(int color) {
        return color&0xff;
    }

    public void applyNegativeFilter() {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 0; x < imageB.getWidth(); ++x) {
            for (int y = 0; y < imageB.getHeight(); ++y) {
                int inColor = imageB.getRGB(x, y);
                res.setRGB(x, y, ((0xff - getRed(inColor)) << 16)|((0xff - getGreen(inColor)) << 8)|(getBlue(inColor)&0xff));
            }
        }
        imageC = res;
    }

    public void applyDiscolorFilter() {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 0; x < imageB.getWidth(); ++x) {
            for (int y = 0; y < imageB.getHeight(); ++y) {
                int inColor = imageB.getRGB(x, y);
                int newColor = discolor(inColor);
                newColor = (newColor << 16)|(newColor << 8)|newColor;
                res.setRGB(x, y, newColor);
            }
        }
        imageC = res;
    }

    private int discolor(int color) {
        return (int) (0.299*getRed(color) + 0.587*getGreen(color) + 0.114*getBlue(color));
    }

    private int[][] getNextD(int[][] D) {
        int[][] nextD = new int[2*D.length][2*D.length];
        for (int i = 0; i < D.length; ++i) {
            for (int j = 0; j < D.length; ++j) {
                nextD[i][j] = 4*D[i][j];
            }
            for (int j = D.length; j < 2*D.length; ++j) {
                nextD[i][j] = 4*D[i][j%D.length]+2;
            }
        }
        for (int i = D.length; i < 2*D.length; ++i) {
            for (int j = 0; j < D.length; ++j) {
                nextD[i][j] = 4*D[i%D.length][j]+3;
            }
            for (int j = D.length; j < 2*D.length; ++j) {
                nextD[i][j] = 4*D[i%D.length][j%D.length]+1;
            }
        }
        return nextD;
    }

    public void applyOrderedDithering(int countR, int countG, int countB) {
        countRed = countR;
        countGreen = countG;
        countBlue = countB;

        int[][] D2 = {{0, 2}, {3, 1}};
        int[][] D = getNextD(getNextD(getNextD(D2)));

        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());

        for (int x = 0; x < imageB.getWidth(); ++x) {
            for (int y = 0; y < imageB.getHeight(); ++y) {
                int inColor = imageB.getRGB(x, y);

                int r = (int)(getRed(inColor) + (((double)D[x % D.length][y % D.length]/(D.length*D.length) - 0.5) * (256.0/(countR - 1))));
                int g = (int)(getGreen(inColor) + (((double)D[x % D.length][y % D.length]/(D.length*D.length) - 0.5) * (256.0/(countG - 1))));
                int b = (int)(getBlue(inColor) + (((double)D[x % D.length][y % D.length]/(D.length*D.length) - 0.5) * (256.0/(countB - 1))));

                int newR = getNearestPaletteColor(r, countR);
                int newG = getNearestPaletteColor(g, countG);
                int newB = getNearestPaletteColor(b, countB);

                res.setRGB(x, y, (newR << 16)|(newG << 8)|newB);
            }
        }
        imageC = res;
    }

    public void applyFloydSteinbergDithering(int countR, int countG, int countB) {
        countRed = countR;
        countGreen = countG;
        countBlue = countB;
        BufferedImage oldImage = view.getImagePanel().copyImage(imageB);
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());

        double[] errorPortions = {7.0/16, 3.0/16, 5.0/16, 1.0/16};

        for (int x = 1; x < imageB.getWidth()-1; ++x) {
            for (int y = 0; y < imageB.getHeight()-1; ++y) {
                int r = getRed(oldImage.getRGB(x, y));
                int g = getGreen(oldImage.getRGB(x, y));
                int b = getBlue(oldImage.getRGB(x, y));

                int newR = getNearestPaletteColor(r, countR);
                int newG = getNearestPaletteColor(g, countG);
                int newB = getNearestPaletteColor(b, countB);

                int errorR = r - newR;
                int errorG = g - newG;
                int errorB = b - newB;

                newR = (newR > 0xff) ? 0xff : newR;
                newG = (newG > 0xff) ? 0xff : newG;
                newB = (newB > 0xff) ? 0xff : newB;

                res.setRGB(x, y, (newR << 16) | (newG << 8) | newB);

                setNeighbourColor(oldImage, errorPortions[0], x + 1, y, errorR, errorG, errorB);
                setNeighbourColor(oldImage, errorPortions[1], x - 1, y + 1, errorR, errorG, errorB);
                setNeighbourColor(oldImage, errorPortions[2], x, y + 1, errorR, errorG, errorB);
                setNeighbourColor(oldImage, errorPortions[3], x + 1, y + 1, errorR, errorG, errorB);
            }
        }

        imageC = res;
    }

    private int getNearestPaletteColor(int color, int colorCount) {
        int intervalSize = 0xff / (colorCount - 1);
        int c = (color + intervalSize/2) / intervalSize * intervalSize;
        c = (c < 0x00)?0x00:c;
        c = (c > 0xff)?0xff:c;

        return c;
    }

    private void setNeighbourColor(BufferedImage image, double errorPortion, int x, int y, int errorR, int errorG, int errorB) {
        int r = getRed(image.getRGB(x, y));
        int g = getGreen(image.getRGB(x, y));
        int b = getBlue(image.getRGB(x, y));

        r += errorPortion * errorR;
        r = (r > 0xff)?0xff:r;
        r = (r < 0x00)?0x00:r;

        g += errorPortion * errorG;
        g = (g > 0xff)?0xff:g;
        g = (g < 0x00)?0x00:g;

        b += errorPortion * errorB;
        b = (b > 0xff)?0xff:b;
        b = (b < 0x00)?0x00:b;

        image.setRGB(x, y, (r << 16) | (g << 8) | b);
    }


    public void applyRobertsOperator(int C) {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 1; x < imageB.getWidth() - 1; ++x) {
            for (int y = 1; y < imageB.getHeight() - 1; ++y) {
                double F = Math.abs(discolor(imageB.getRGB(x, y)) - discolor(imageB.getRGB(x+1, y+1))) + Math.abs(discolor(imageB.getRGB(x, y+1)) - discolor(imageB.getRGB(x+1, y)));
                if (F > C)
                    res.setRGB(x, y, Color.white.getRGB());
                else res.setRGB(x, y, Color.black.getRGB());
            }
        }
        imageC = res;
        robertsParameter = C;
    }

    public void applySobelOperator(int C) {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 1; x < imageB.getWidth() - 1; ++x) {
            for (int y = 1; y < imageB.getHeight() - 1; ++y) {
                double S = Math.abs(discolor(imageB.getRGB(x+1, y-1)) + 2*discolor(imageB.getRGB(x+1, y)) +
                        discolor(imageB.getRGB(x+1, y+1)) - discolor(imageB.getRGB(x-1, y-1)) -
                        2*discolor(imageB.getRGB(x-1, y)) - discolor(imageB.getRGB(x-1, y+1))) +
                        Math.abs(discolor(imageB.getRGB(x-1, y+1)) + 2*discolor(imageB.getRGB(x,y+1)) +
                        discolor(imageB.getRGB(x+1, y+1))- discolor(imageB.getRGB(x-1, y-1)) -
                        2*discolor(imageB.getRGB(x, y-1)) - discolor(imageB.getRGB(x+1,y-1)));
                if (S > C)
                    res.setRGB(x, y, Color.white.getRGB());
                else res.setRGB(x, y, Color.black.getRGB());
            }
        }
        imageC = res;
        sobelParameter = C;
    }

    public void applyZoom() {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 0; x < imageB.getWidth()/2; x++) {
            for (int y = 0; y < imageB.getHeight()/2; y++) {
                int color = imageB.getRGB(imageB.getWidth()/4 + x, imageB.getHeight()/4 + y);
                res.setRGB(x * 2, y * 2, color);
                res.setRGB(x * 2 + 1, y * 2, color);
                res.setRGB(x * 2, y * 2 + 1, color);
                res.setRGB(x * 2 + 1, y * 2 + 1, color);
            }
            imageC = res;
        }
    }

    public void applyRotation(int angle) {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());

        double ang = -angle*(Math.PI/180);
        double width = (double)(imageB.getWidth()-1)/2;
        double height = (double)(imageB.getHeight()-1)/2;
        for (int x = 0; x < imageB.getWidth(); ++x) {
            for (int y = 0; y < imageB.getHeight(); ++y) {
                int newX = (int)(Math.cos(ang)*(x-width) - Math.sin(ang)*(y-height) + width);
                int newY = (int)(Math.sin(ang)*(x-width) + Math.cos(ang)*(y-height) + height);
                int color = (newX<0 || newX>=imageB.getWidth() || newY<0 || newY>=imageB.getHeight())?0xffffff:imageB.getRGB(newX, newY);
                res.setRGB(x, y, color);
            }
        }
        rotationAngle = angle;
        imageC = res;
    }

    public BufferedImage applySharpening(BufferedImage image) {
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 1; x < image.getWidth() - 1; ++x){
            for (int y = 1; y < image.getHeight() - 1; ++y) {
                res.setRGB(x, y, applyMatrixTransform(image, x, y, 5, -1, -1, -1, -1, 0));
            }
        }
        imageC = res;
        return res;
    }

    public void applyStamping() {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        for (int x = 1; x < imageB.getWidth() - 1; ++x) {
            for (int y = 1; y < imageB.getHeight() - 1; ++y) {
                res.setRGB(x, y, applyMatrixTransform(imageB, x, y, 0, -1, 1, -1, 1, 128));
            }
        }
        imageC = res;
    }

    public void applyBlurFilter() {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());
        double[][] blurMatrix = {{(double)1/74, (double)2/74, (double)3/74, (double)2/74, (double) 1/74},
                            {(double)2/74, (double)4/74, (double)5/74, (double)4/74, (double)2/74},
                            {(double)3/74, (double)5/74, (double)6/74, (double)5/74, (double)3/74},
                            {(double)2/74, (double)4/74, (double)5/74, (double)4/74, (double)2/74},
                            {(double)1/74, (double)2/74, (double)3/74, (double)2/74, (double)1/74}};
        for (int x = 0; x < imageB.getWidth(); ++x) {
            for (int y = 0; y < imageB.getHeight(); ++y) {
                int r = getRed(imageB.getRGB(x,y));
                int g = getGreen(imageB.getRGB(x,y));
                int b = getBlue(imageB.getRGB(x,y));

                double newR = r*blurMatrix[2][2];
                double newG = g*blurMatrix[2][2];
                double newB = b*blurMatrix[2][2];

                for (int i = -2; i <= 2; ++i) {
                    for (int j = -2; j <= 2; ++j) {
                        if ((x + i > 0)&&(x + i < imageB.getWidth())&&(y + j > 0)&&(y + j < imageB.getHeight())) {
                            newR += getRed(imageB.getRGB(x+i, y+j))*blurMatrix[2+j][2+i];
                            newG += getGreen(imageB.getRGB(x+i, y+j))*blurMatrix[2+j][2+i];
                            newB += getBlue(imageB.getRGB(x+i, y+j))*blurMatrix[2+j][2+i];

                            newR = (newR > 0xff)?0xff:newR;
                            newR = (newR < 0x00)?0x00:newR;
                            newG = (newG > 0xff)?0xff:newG;
                            newG = (newG < 0x00)?0x00:newG;
                            newB = (newB > 0xff)?0xff:newB;
                            newB = (newB < 0x00)?0x00:newB;
                        }
                    }
                }
                res.setRGB(x, y, ((int)newR << 16)|((int)newG << 8)|(int)newB);
            }
        }
        imageC = res;
    }

    public void applyAquarelFilter() {
        BufferedImage res = getMedianFilter(imageB);
        imageC = applySharpening(res);
    }

    public void applyMedianFilter() {
        imageC = getMedianFilter(imageB);
    }

    public BufferedImage getMedianFilter(BufferedImage image) {
        int vicinitySize = 5;
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int newR = getAverageValueInVicinity(image, vicinitySize, x, y, 'R');
                int newG = getAverageValueInVicinity(image, vicinitySize, x, y, 'G');
                int newB = getAverageValueInVicinity(image, vicinitySize, x, y, 'B');
                res.setRGB(x, y, ((newR << 16)|(newG << 8)|newB));
            }
        }
        return res;
    }

    private int getAverageValueInVicinity(BufferedImage image, int vicinitySize, int x, int y, char color) {
        ArrayList<Integer> vicinity = new ArrayList<>();
        for (int i = -vicinitySize/2; i <= vicinitySize/2; ++i) {
            for (int j = -vicinitySize/2; j <= vicinitySize/2; ++j) {
                if (x + i >= 0 && x + i < image.getWidth() && y + j >= 0 && y + j < image.getHeight()) {
                    if (color == 'R')
                        vicinity.add(getRed(image.getRGB(x + i, y + j)));
                    else if (color == 'G')
                        vicinity.add(getGreen(image.getRGB(x + i, y + j)));
                    else if (color == 'B')
                        vicinity.add(getBlue(image.getRGB(x + i, y + j)));
                }
            }
        }
        Collections.sort(vicinity);
        return vicinity.get(vicinity.size()/2);
    }

    private int applyMatrixTransform(BufferedImage image, int x, int y, int m1, int m2, int m3, int m4, int m5, int c) {
        int newR = m1*getRed(image.getRGB(x, y)) + m2*getRed(image.getRGB(x-1, y)) + m3*getRed(image.getRGB(x, y-1)) +
                m4*getRed(image.getRGB(x, y+1)) + m5*getRed(image.getRGB(x+1, y)) + c;
        newR = (newR>0xff)?0xff:newR;
        newR = (newR<0)?0:newR;
        int newG = m1*getGreen(image.getRGB(x, y)) + m2*getGreen(image.getRGB(x-1, y)) + m3*getGreen(image.getRGB(x, y-1)) +
                m4*getGreen(image.getRGB(x, y+1)) + m5*getGreen(image.getRGB(x+1, y)) + c;
        newG = (newG>0xff)?0xff:newG;
        newG = (newG<0)?0:newG;
        int newB = m1*getBlue(image.getRGB(x, y)) + m2*getBlue(image.getRGB(x-1, y)) + m3*getBlue(image.getRGB(x, y-1)) +
                m4*getBlue(image.getRGB(x, y+1)) + m5*getBlue(image.getRGB(x+1, y)) + c;
        newB = (newB>0xff)?0xff:newB;
        newB = (newB<0)?0:newB;

        return (newR << 16)|(newG << 8)|newB;
    }

    public void applyGammaCorrection(int gamma) {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());

        for (int x = 0; x < imageB.getWidth(); x++) {
            for (int y = 0; y < imageB.getHeight(); y++) {
                int r = getRed(imageB.getRGB(x, y));
                int g = getGreen(imageB.getRGB(x, y));
                int b = getBlue(imageB.getRGB(x, y));

                r = (int)(Math.pow(r/((double)0xff), 100.0/gamma)*0xff);
                g = (int)(Math.pow(g/((double)0xff), 100.0/gamma)*0xff);
                b = (int)(Math.pow(b/((double)0xff), 100.0/gamma)*0xff);

                res.setRGB(x, y, (r << 16)|(g << 8)|b);
            }
        }
        gammaCor = gamma;
        imageC = res;
    }

    public void applyVolumeRendering(int gridSizeZ) {
        BufferedImage res = new BufferedImage(imageB.getWidth(), imageB.getHeight(), imageB.getType());

        int gridSizeX = imageB.getWidth();
        int gridSizeY = imageB.getHeight();

        getAbsorbtionInterpolation();
        getEmissionInterpolation();

        double normX = 1.0/gridSizeX;
        double normY = 1.0/gridSizeY;
        double normZ = 1.0/gridSizeZ;

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double influence;
        for (int x = 0; x < gridSizeX; ++x) {
            for (int y = 0; y < gridSizeY; ++y) {
                for (int z = 0; z < gridSizeZ; ++z) {
                    influence = getChargesInfluence(x, y, z, normX, normY, normZ);
                    min = Math.min(min, influence);
                    max = Math.max(max, influence);
                }
            }
        }

        for (int x = 0; x < gridSizeX; ++x) {
            for (int y = 0; y < gridSizeY; ++y) {
                double rI = (double)getRed(imageB.getRGB(x, y));
                double gI = (double)getGreen(imageB.getRGB(x, y));
                double bI = (double)getBlue(imageB.getRGB(x, y));
                for (int z = 0; z < gridSizeZ; ++z) {
                    influence = getChargesInfluence(x, y, z, normX, normY, normZ);
                    int xPos = (int)(Math.round((influence-min)/(max-min)*100));
                    rI = rI*Math.exp(-(absorbtion[xPos]*normZ)) + ((double)emission[xPos][0]*normZ);
                    gI = gI*Math.exp(-(absorbtion[xPos]*normZ)) + ((double)emission[xPos][1]*normZ);
                    bI = bI*Math.exp(-(absorbtion[xPos]*normZ)) + ((double)emission[xPos][2]*normZ);
                }
                rI = (rI>0xff)?0xff:rI;
                rI = (rI<0)?0:rI;
                gI = (gI>0xff)?0xff:gI;
                gI = (gI<0)?0:gI;
                bI = (bI>0xff)?0xff:bI;
                bI = (bI<0)?0:bI;
                res.setRGB(x, y, ((int)rI<<16)|((int)gI<<8)|(int)bI);
            }
        }
        imageC = res;
    }

    private double getChargesInfluence(int x, int y, int z, double normX, double normY, double normZ) {
        double influence = 0;
        for (Double[] charge : charges) {
            double distance = Math.sqrt(Math.abs(normX*(x+0.5) - charge[0]) * Math.abs(normX*(x+0.5) - charge[0]) +
                    Math.abs(normY*(y+0.5) - charge[1])*Math.abs(normY*(y+0.5) - charge[1]) +
                    Math.abs(normZ*(z+0.5) - charge[2])*Math.abs(normZ*(z+0.5) - charge[2]));
            distance = (distance < 0.1)?0.1:distance;
            influence += charge[3]/distance;
        }
        return influence;
    }

    public void loadConfigFile(String path) {
        try {
            Scanner in = new Scanner(new File(path));
            absorbtionValues = new ArrayList<>();
            emissionValues = new ArrayList<>();
            charges = new ArrayList<>();

            absorbtion = new double[101];
            Arrays.fill(absorbtion, 0.0);
            emission = new int[101][3];
            for (int i = 0; i < 101; ++i)
                Arrays.fill(emission[i], 0);

            int absorbtionCount = getNextInt(in);
            if (absorbtionCount == -1) {
                view.showInvalidConfigFile();
            }
            try {
                for (int i = 0; i <= absorbtionCount; ++i) {
                    readAbsorbtionValue(in);
                }
                int emissionCount = getNextInt(in);
                for (int i = 0; i <= emissionCount; ++i) {
                    readEmissionValue(in);
                }
                int chargesCount = getNextInt(in);
                for (int i = 0; i <= chargesCount; ++i) {
                    readCharge(in);
                }
            } catch (Exception e) {
                view.showInvalidConfigFile();
            }
            volumeRenderingParametersSet = true;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private int getNextInt(Scanner in) {
        while (!in.hasNextInt() && in.hasNextLine()) {
            String str[] = (in.nextLine()).split("//");
            try {
                return Integer.parseInt(str[0]);
            } catch (NumberFormatException e) {}
        }
        if (in.hasNextInt())
            return in.nextInt();
        return -1;
    }

    private void readAbsorbtionValue(Scanner in) {
        if (in.hasNextLine()) {
            String str[] = (in.nextLine()).split("//");
            String params[] = str[0].split(" ");
            try {
                int x = Integer.parseInt(params[0]);
                double val = Double.parseDouble(params[1]);
                absorbtion[x] = val;
                absorbtionValues.add(new Double[]{(double)x, val});
            } catch (Exception e) {}
        }
    }

    private void readEmissionValue(Scanner in) {
        if (in.hasNextLine()) {
            String str[] = (in.nextLine()).split("//");
            String params[] = str[0].split(" ");
            try {
                int x = Integer.parseInt(params[0]);
                int r = Integer.parseInt(params[1]);
                int g = Integer.parseInt(params[2]);
                int b = Integer.parseInt(params[3]);
                emissionValues.add(new Integer[]{x, r, g, b});
                emission[x][0] = r;
                emission[x][1] = g;
                emission[x][2] = b;
            } catch (Exception e) {}
        }
    }

    private void readCharge(Scanner in) {
        if (in.hasNextLine()) {
            String str[] = (in.nextLine()).split("//");
            String params[] = str[0].split(" ");
            try {
                double x = Double.parseDouble(params[0]);
                double y = Double.parseDouble(params[1]);
                double z = Double.parseDouble(params[2]);
                double q = Double.parseDouble(params[3]);
                charges.add(new Double[] {x, y, z, q});
            } catch (Exception e) {}
        }
    }

    private void getAbsorbtionInterpolation() {
        int prevAbsorbX = 0;
        double prevAbsorbVal = absorbtion[0];
        for (Double[] absorbtionNode : absorbtionValues) {
            if (absorbtionNode[0] != prevAbsorbX) {
                absorbtion[(int)(double)absorbtionNode[0]] = absorbtionNode[1];
                for (int i = 1; i < absorbtionNode[0] - prevAbsorbX; ++i) {
                    double step = (absorbtionNode[1]-prevAbsorbVal)/(absorbtionNode[0]-prevAbsorbX);
                    absorbtion[i+prevAbsorbX] = prevAbsorbVal + step*i;
                }
            }
            prevAbsorbX = (int)(double)absorbtionNode[0];
            prevAbsorbVal = absorbtionNode[1];
        }
    }

    private void getEmissionInterpolation() {
        int prevEmisX = 0;
        int prevEmisR = emission[0][0];
        int prevEmisG = emission[0][1];
        int prevEmisB = emission[0][2];

        for (Integer[] emissionNode : emissionValues) {
            if (emissionNode[0] != prevEmisX) {
                emission[emissionNode[0]][0] = emissionNode[1];
                emission[emissionNode[0]][1] = emissionNode[2];
                emission[emissionNode[0]][2] = emissionNode[3];
                for (int i = 1; i < emissionNode[0]-prevEmisX; ++i) {
                    double rStep = (double)(emissionNode[1]-prevEmisR)/(emissionNode[0]-prevEmisX);
                    double gStep = (double)(emissionNode[2]-prevEmisG)/(emissionNode[0]-prevEmisX);
                    double bStep = (double)(emissionNode[3]-prevEmisB)/(emissionNode[0]-prevEmisX);
                    emission[i+prevEmisX][0] = (int)(prevEmisR + rStep*i);
                    emission[i+prevEmisX][1] = (int)(prevEmisG + gStep*i);
                    emission[i+prevEmisX][2] = (int)(prevEmisB + bStep*i);

                }
            }
            prevEmisX = emissionNode[0];
            prevEmisR = emissionNode[1];
            prevEmisG = emissionNode[2];
            prevEmisB = emissionNode[3];
        }
    }

    public BufferedImage getImageA() {
        return imageA;
    }

    public BufferedImage getImageB() {
        return imageB;
    }

    public BufferedImage getImageC() {
        return imageC;
    }

    public void setImageA(BufferedImage imageA) {
        this.imageA = imageA;
    }

    public void setImageB(BufferedImage imageB) {
        this.imageB = imageB;
    }

    public void setImageC(BufferedImage imageC) {
        this.imageC = imageC;
    }

    public int getRobertsParameter() {
        return robertsParameter;
    }

    public int getSobelParameter() {
        return sobelParameter;
    }

    public int getCountRed() {
        return countRed;
    }

    public int getCountGreen() {
        return countGreen;
    }

    public int getCountBlue() {
        return countBlue;
    }

    public int getAngle() {
        return rotationAngle;
    }

    public int getGamma() {
        return gammaCor;
    }

    public ArrayList<Double[]> getAbsorbtionValues() {
        return absorbtionValues;
    }

    public ArrayList<Integer[]> getEmissionValues() {
        return emissionValues;
    }

    public boolean isVolumeRenderingParametersSet() {
        return volumeRenderingParametersSet;
    }
}

