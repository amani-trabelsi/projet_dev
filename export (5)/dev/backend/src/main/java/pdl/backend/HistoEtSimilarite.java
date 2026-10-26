package pdl.backend;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.postgresql.util.PGobject;

import boofcv.alg.color.ColorHsv;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class HistoEtSimilarite {

    public Map<String, Double> histoRGB(Planar<GrayU8> image) {
        if (image == null) {
            throw new IllegalArgumentException("L'image ne peut pas être null");
        }

        int width = image.width;
        int height = image.height;
        int totalPixels = width * height;
        Map<String, Double> histogram = new HashMap<>();

        // Use consistent binning: 8 bins for R and G, 4 bins for B (to match your
        // vector size)
        int rBins = 8, gBins = 8, bBins = 4;

        // Initialize histogram buckets
        for (int r = 0; r < rBins; r++) {
            for (int g = 0; g < gBins; g++) {
                for (int b = 0; b < bBins; b++) {
                    histogram.put(r + "," + g + "," + b, 0.0);
                }
            }
        }

        // Fill histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Map 0-255 values to our bin counts
                int r = Math.min((image.getBand(0).get(x, y) * rBins) / 256, rBins - 1);
                int g = Math.min((image.getBand(1).get(x, y) * gBins) / 256, gBins - 1);
                int b = Math.min((image.getBand(2).get(x, y) * bBins) / 256, bBins - 1);

                String key = r + "," + g + "," + b;
                histogram.put(key, histogram.getOrDefault(key, 0.0) + 1.0);
            }
        }

        // Properly normalize histogram - convert counts to probabilities
        double alpha = 1.0;
        double smoothedTotal = totalPixels + alpha * histogram.size();

        for (String key : histogram.keySet()) {
            double count = histogram.get(key);
            histogram.put(key, (count + alpha) / smoothedTotal);
        }

        return histogram;
    }

    public double[][] histoTeinteSaturation(Planar<GrayU8> image) {
        if (image == null) {
            throw new IllegalArgumentException("L'image ne peut pas être null");
        }

        int width = image.width;
        int height = image.height;
        int totalPixels = width * height;

        // Use 16 bins for both hue and saturation
        double[][] histogram = new double[16][16];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = image.getBand(0).get(x, y);
                int g = image.getBand(1).get(x, y);
                int b = image.getBand(2).get(x, y);

                float[] hsv = new float[3];
                ColorHsv.rgbToHsv(r, g, b, hsv);

                // Scale hue from [0,2π] to [0,15]
                int hue = Math.min(15, (int) (hsv[0] * 16 / (2 * Math.PI)));
                // Scale saturation from [0,1] to [0,15]
                int saturation = Math.min(15, (int) (hsv[1] * 15));

                histogram[hue][saturation]++;
            }
        }

        // Normalize the histogram
        for (int h = 0; h < 16; h++) {
            for (int s = 0; s < 16; s++) {
                histogram[h][s] /= totalPixels;
            }
        }

        return histogram;
    }

    public PGobject convertHistoRGBToVector(Map<String, Double> histo) throws SQLException {
        int rBins = 8, gBins = 8, bBins = 4;
        double[] vectorArray = new double[rBins * gBins * bBins]; // 256 elements

        int index = 0;
        for (int r = 0; r < rBins; r++) {
            for (int g = 0; g < gBins; g++) {
                for (int b = 0; b < bBins; b++) {
                    String key = r + "," + g + "," + b;
                    vectorArray[index++] = histo.getOrDefault(key, 0.0);
                }
            }
        }

        // Convert to PostgreSQL vector format
        StringBuilder vectorString = new StringBuilder("[");
        for (int i = 0; i < vectorArray.length; i++) {
            if (i > 0)
                vectorString.append(",");
            vectorString.append(vectorArray[i]);
        }
        vectorString.append("]");

        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(vectorString.toString());

        return pgObject;
    }

    public PGobject convertHistoTSToVector(double[][] histo) throws SQLException {
        StringBuilder vectorBuilder = new StringBuilder("[");

        // Fill vector with HS histogram values
        int count = 0;
        for (int h = 0; h < 16; h++) {
            for (int s = 0; s < 16; s++) {
                vectorBuilder.append(histo[h][s]);

                count++;
                if (count < 256) {
                    vectorBuilder.append(",");
                }
            }
        }

        vectorBuilder.append("]");

        // Create PGobject and set type and value
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(vectorBuilder.toString());

        return pgObject;
    }

}